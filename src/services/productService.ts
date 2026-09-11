export type Product = {
  id?: string
  slug?: string
  name: string
  category: string
  categoryId?: string
  productType?: string
  price: string
  oldPrice?: string
  rating: string
  image: string
  tone: string
  stock?: number
  brand?: string
  reviewCount?: number
  inStock?: boolean
  description?: string
  shortDescription?: string
  sku?: string
  material?: string
  color?: string
  tags?: string
}

export type ApiProduct = {
  id: string
  slug: string
  name: string
  categoryName: string
  productType: string
  categoryId: string
  price: number
  discountPrice?: number
  rating: number
  imageUrl?: string
  stock: number
  brand: string
  reviewCount: number
  description: string
  shortDescription: string
  sku: string
  material?: string
  color?: string
  tags?: string
}

export type ProductFilters = { search?: string; category?: string; type?: string; brand?: string; minPrice?: string; maxPrice?: string; minRating?: string; inStock?: boolean; outOfStock?: boolean; page: number; size: number; sort: string; direction: string }
export type ProductPage = { content: ApiProduct[]; totalElements: number; totalPages: number; number: number; size: number }

const formatPrice = (value: number) => `₹${value.toLocaleString('en-IN')}`
let demoSessionPromise: Promise<string | null> | null = null

export const mapProduct = (product: ApiProduct, index = 0): Product => ({
  id: product.id,
  slug: product.slug,
  name: product.name,
  category: product.categoryName,
  categoryId: product.categoryId,
  productType: product.productType,
  price: formatPrice(product.discountPrice ?? product.price),
  oldPrice: product.discountPrice ? formatPrice(product.price) : undefined,
  rating: product.rating.toFixed(1),
  image: product.imageUrl || '/product-image-fallback.svg',
  tone: ['coral', 'blue', 'sand', 'green'][index % 4],
  stock: product.stock,
  brand: product.brand,
  reviewCount: product.reviewCount,
  inStock: product.stock > 0,
  description: product.description,
  shortDescription: product.shortDescription,
  sku: product.sku,
  material: product.material,
  color: product.color,
  tags: product.tags,
})

export const getAuthHeaders = (includeJson = false) => {
  const token = localStorage.getItem('shopsense-token')
  const headers: Record<string, string> = {}
  if (includeJson) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

export type AuthResponse = { token: string; tokenType: string; userId: string; email: string; firstName: string; role: string }

export async function login(email: string, password: string): Promise<AuthResponse> {
  const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password }) })
  if (!response.ok) throw new Error('Invalid email or password')
  const data = await response.json() as AuthResponse
  localStorage.setItem('shopsense-token', data.token)
  localStorage.setItem('shopsense-user', JSON.stringify(data))
  return data
}

export async function register(firstName: string, lastName: string, email: string, password: string): Promise<AuthResponse> {
  const response = await fetch('/api/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ firstName, lastName, email, password }) })
  if (!response.ok) throw new Error(response.status === 409 ? 'An account with this email already exists' : 'Unable to create your account')
  const data = await response.json() as AuthResponse
  localStorage.setItem('shopsense-token', data.token)
  localStorage.setItem('shopsense-user', JSON.stringify(data))
  return data
}

export function logout() {
  localStorage.removeItem('shopsense-token')
  localStorage.removeItem('shopsense-user')
}

export async function ensureDemoSession(): Promise<string | null> {
  const saved = localStorage.getItem('shopsense-token')
  const demoMode = import.meta.env.VITE_DEMO_MODE !== 'false'
  if (!demoMode) return null
  if (demoSessionPromise) return demoSessionPromise

  demoSessionPromise = (async () => {
    if (saved) {
      try {
        const response = await fetch('/api/cart', { headers: { Authorization: `Bearer ${saved}` } })
        if (response.ok) return saved
      } catch {
        return saved
      }
      localStorage.removeItem('shopsense-token')
      localStorage.removeItem('shopsense-user')
    }

    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'demo@shopsense.ai', password: 'DemoPass!23' }),
    })

    if (!response.ok) return null
    const data = await response.json() as { token?: string }
    if (data.token) {
      localStorage.setItem('shopsense-token', data.token)
      return data.token
    }
    return null
  })()

  try {
    return await demoSessionPromise
  } finally {
    demoSessionPromise = null
  }
}

export async function getProducts(filters: Partial<ProductFilters> = {}, signal?: AbortSignal): Promise<{ products: Product[]; totalElements: number; totalPages: number; page: number }> {
  const params = new URLSearchParams({ page: String(filters.page ?? 0), size: String(filters.size ?? 12), sort: filters.sort || 'createdAt', direction: filters.direction || 'desc' })
  Object.entries(filters).forEach(([key, value]) => { if (value !== undefined && value !== '' && !['page', 'size', 'sort', 'direction'].includes(key)) params.set(key, String(value)) })
  const response = await fetch(`/api/products?${params.toString()}`, { signal })
  if (!response.ok) throw new Error('Unable to load products')
  const page = await response.json() as ProductPage
  return { products: page.content.map(mapProduct), totalElements: page.totalElements, totalPages: page.totalPages, page: page.number }
}

export async function getBrands(signal?: AbortSignal): Promise<string[]> {
  const response = await fetch('/api/products/brands', { signal })
  if (!response.ok) throw new Error('Unable to load brands')
  return response.json() as Promise<string[]>
}

export type ProductTypeSummary = { name: string; productCount: number; images: string[] }
export async function getProductTypes(category: string, signal?: AbortSignal): Promise<ProductTypeSummary[]> {
  const response = await fetch(`/api/products/types?category=${encodeURIComponent(category)}`, { signal })
  if (!response.ok) throw new Error('Unable to load product types')
  return response.json() as Promise<ProductTypeSummary[]>
}

export async function getTypeBrands(category: string, type?: string, signal?: AbortSignal): Promise<string[]> {
  const params = new URLSearchParams({ category })
  if (type) params.set('type', type)
  const response = await fetch(`/api/products/brands?${params.toString()}`, { signal })
  if (!response.ok) throw new Error('Unable to load type brands')
  return response.json() as Promise<string[]>
}

export type CartItem = { id: string; productId: string; productName: string; imageUrl?: string; unitPrice: number; quantity: number; lineTotal: number }
export type Cart = { items: CartItem[]; subtotal: number; shipping: number; total: number; itemCount: number }

export async function addCartItem(productId: string, quantity = 1): Promise<Cart> {
  await ensureDemoSession()
  const request = () => fetch('/api/cart/items', {
    method: 'POST',
    headers: { ...getAuthHeaders(true) },
    body: JSON.stringify({ productId, quantity }),
  })
  let response = await request()
  if ((response.status === 401 || response.status === 403) && import.meta.env.VITE_DEMO_MODE !== 'false') {
    localStorage.removeItem('shopsense-token')
    localStorage.removeItem('shopsense-user')
    await ensureDemoSession()
    response = await request()
  }
  if (!response.ok) throw new Error(response.status === 401 ? 'Please sign in before adding items to your bag.' : 'Unable to update your bag')
  return response.json() as Promise<Cart>
}

export async function getCart(): Promise<Cart> {
  await ensureDemoSession()
  const response = await fetch('/api/cart', { headers: getAuthHeaders() })
  if (!response.ok) throw new Error(response.status === 401 ? 'Please sign in to view your bag.' : 'Unable to load your bag')
  return response.json() as Promise<Cart>
}

export async function updateCartItem(itemId: string, quantity: number): Promise<Cart> {
  await ensureDemoSession()
  const response = await fetch(`/api/cart/items/${itemId}`, { method: 'PUT', headers: { ...getAuthHeaders(true) }, body: JSON.stringify({ quantity }) })
  if (!response.ok) throw new Error('Unable to update item quantity')
  return response.json() as Promise<Cart>
}

export async function removeCartItem(itemId: string): Promise<void> {
  await ensureDemoSession()
  const response = await fetch(`/api/cart/items/${itemId}`, { method: 'DELETE', headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to remove item from bag')
}

export async function clearCart(): Promise<void> {
  await ensureDemoSession()
  const response = await fetch('/api/cart', { method: 'DELETE', headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to clear bag')
}

export type WishlistItem = { id: string; productId: string; productName: string; productSlug: string; imageUrl?: string; brand: string; price: string }
export type WishlistResponse = { items: WishlistItem[]; itemCount: number }

export async function getWishlist(): Promise<WishlistResponse> {
  await ensureDemoSession()
  const response = await fetch('/api/wishlist', { headers: getAuthHeaders() })
  if (!response.ok) throw new Error(response.status === 401 ? 'Please sign in to view your wishlist.' : 'Unable to load wishlist')
  return response.json() as Promise<WishlistResponse>
}

export async function addWishlistItem(productId: string): Promise<WishlistResponse> {
  await ensureDemoSession()
  const response = await fetch('/api/wishlist', {
    method: 'POST',
    headers: { ...getAuthHeaders(true) },
    body: JSON.stringify({ productId }),
  })
  if (!response.ok) throw new Error(response.status === 401 ? 'Please sign in to save items.' : 'Unable to save product')
  return response.json() as Promise<WishlistResponse>
}

export async function removeWishlistItem(productId: string): Promise<void> {
  await ensureDemoSession()
  const response = await fetch(`/api/wishlist/products/${productId}`, { method: 'DELETE', headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to remove item from wishlist')
}

export type Review = { id: string; productId: string; userName: string; rating: number; reviewText: string; createdAt: string }
export type ReviewSummary = { averageRating: number; reviewCount: number; reviews: Review[] }
export async function getProductReviews(productId: string): Promise<ReviewSummary> {
  const response = await fetch(`/api/products/${productId}/reviews`)
  if (!response.ok) throw new Error('Unable to load reviews')
  return response.json() as Promise<ReviewSummary>
}

export async function createReview(productId: string, rating: number, reviewText: string): Promise<Review> {
  const response = await fetch('/api/reviews', {
    method: 'POST',
    headers: { ...getAuthHeaders(true) },
    body: JSON.stringify({ productId, rating, reviewText }),
  })
  if (!response.ok) throw new Error('Unable to post your review')
  return response.json() as Promise<Review>
}

export type AiSearchResponse = { criteria: { category?: string; maxPrice?: number; minPrice?: number; minRating?: number; keywords?: string[] }; products: ApiProduct[]; explanation: string; aiEnhanced: boolean }
export async function searchAi(query: string): Promise<AiSearchResponse> {
  const response = await fetch('/api/ai/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query }),
  })
  if (!response.ok) throw new Error('AI search is unavailable')
  return response.json() as Promise<AiSearchResponse>
}

export async function askAiAssistant(query: string): Promise<{ answer: string; products: ApiProduct[]; suggestedQueries: string[] }> {
  const response = await fetch('/api/ai/assistant', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query }),
  })
  if (!response.ok) throw new Error('AI assistant is unavailable')
  return response.json() as Promise<{ answer: string; products: ApiProduct[]; suggestedQueries: string[] }>
}

export async function getAiRecommendations(category: string, query: string, productId?: string, limit = 4): Promise<{ products: ApiProduct[]; explanation: string }> {
  const response = await fetch('/api/ai/recommendations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ category, query, productId, limit }),
  })
  if (!response.ok) throw new Error('Recommendations are unavailable')
  return response.json() as Promise<{ products: ApiProduct[]; explanation: string }>
}

export async function getAiSimilar(productId: string, limit = 4): Promise<{ products: ApiProduct[]; explanation: string }> {
  const response = await fetch('/api/ai/similar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, limit }),
  })
  if (!response.ok) throw new Error('Similar-product AI is unavailable')
  return response.json() as Promise<{ products: ApiProduct[]; explanation: string }>
}

export async function getAiCompare(productIds: string[]): Promise<{ bestProductId?: string; bestValueId?: string; summary: string; products: ApiProduct[] }> {
  const response = await fetch('/api/ai/compare', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productIds }),
  })
  if (!response.ok) throw new Error('AI comparison is unavailable')
  return response.json() as Promise<{ bestProductId?: string; bestValueId?: string; summary: string; products: ApiProduct[] }>
}

export async function getAiBudget(query: string, limit = 4): Promise<{ products: ApiProduct[]; total: number; budget?: number; explanation: string }> {
  const response = await fetch('/api/ai/budget', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, limit }),
  })
  if (!response.ok) throw new Error('Budget builder is unavailable')
  return response.json() as Promise<{ products: ApiProduct[]; total: number; budget?: number; explanation: string }>
}

export type OrderItem = { id: string; productId: string; productName: string; imageUrl?: string; quantity: number; unitPrice: number; lineTotal: number }
export type ShippingDetails = { fullName: string; phone: string; address: string; city: string; state: string; postalCode: string; country: string }
export type Order = { id: string; status: string; createdAt: string; subtotal: number; shipping: number; total: number; itemCount: number; shippingDetails: ShippingDetails; items: OrderItem[] }

export async function createOrder(shipping: ShippingDetails): Promise<Order> {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: { ...getAuthHeaders(true) },
    body: JSON.stringify({ shipping }),
  })
  if (!response.ok) throw new Error('Unable to place order')
  return response.json() as Promise<Order>
}

export async function getOrders(): Promise<Order[]> {
  const response = await fetch('/api/orders', { headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to load order history')
  return response.json() as Promise<Order[]>
}

export async function getOrder(orderId: string): Promise<Order> {
  const response = await fetch(`/api/orders/${encodeURIComponent(orderId)}`, { headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to load order details')
  return response.json() as Promise<Order>
}

export async function getAdminOrders(): Promise<Order[]> {
  const response = await fetch('/api/admin/orders', { headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to load admin orders')
  return response.json() as Promise<Order[]>
}

export async function getAdminProducts(): Promise<ProductPage> {
  const response = await fetch('/api/products?page=0&size=50&sort=createdAt&direction=desc')
  if (!response.ok) throw new Error('Unable to load admin products')
  return response.json() as Promise<ProductPage>
}

export async function deleteAdminProduct(productId: string): Promise<void> {
  const response = await fetch(`/api/products/${encodeURIComponent(productId)}`, { method: 'DELETE', headers: getAuthHeaders() })
  if (!response.ok) throw new Error('Unable to delete product')
}
