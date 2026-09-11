package com.shopsense.api.service;

import com.shopsense.api.dto.OrderDtos.*;
import com.shopsense.api.entity.*;
import com.shopsense.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final UserRepository users;
    private final CartRepository carts;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, UserRepository users, CartRepository carts, ProductRepository products) {
        this.orders = orders;
        this.users = users;
        this.carts = carts;
        this.products = products;
    }

    @Transactional
    public OrderResponse create(String email, CreateOrderRequest request) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Cart cart = carts.findByUserEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty"));
        if (cart.getItems().isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = products.findById(item.getProduct().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product no longer available"));
            if (product.getStock() < item.getQuantity()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough stock for " + product.getName());
            BigDecimal unitPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(1499)) >= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(99);
        BigDecimal total = subtotal.add(shipping);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setSubtotal(subtotal);
        order.setShipping(shipping);
        order.setTotal(total);
        order.setFullName(request.shipping().fullName());
        order.setPhone(request.shipping().phone());
        order.setAddress(request.shipping().address());
        order.setCity(request.shipping().city());
        order.setState(request.shipping().state());
        order.setPostalCode(request.shipping().postalCode());
        order.setCountry(request.shipping().country());

        for (CartItem cartItem : cart.getItems()) {
            Product product = products.findById(cartItem.getProduct().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            product.setStock(product.getStock() - cartItem.getQuantity());
            products.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal unitPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(orderItem);
        }

        order = orders.saveAndFlush(order);
        cart.getItems().clear();
        carts.save(cart);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String email) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return orders.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(String email, UUID orderId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this order");
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        ShippingDetails shipping = new ShippingDetails(order.getFullName(), order.getPhone(), order.getAddress(), order.getCity(), order.getState(), order.getPostalCode(), order.getCountry());
        List<OrderItemResponse> items = order.getItems().stream().map(item -> new OrderItemResponse(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getImageUrl(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getLineTotal()
        )).toList();
        return new OrderResponse(order.getId(), order.getStatus().name(), order.getCreatedAt(), order.getSubtotal(), order.getShipping(), order.getTotal(), items.stream().mapToInt(OrderItemResponse::quantity).sum(), shipping, items);
    }
}
