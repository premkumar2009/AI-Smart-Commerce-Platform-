import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './shopsense.css'
import './button-overrides.css'
import App from './App.tsx'
import { CartPage, CheckoutPage, ComparePage, OrderDetailPage, OrderHistoryPage, ProductDetailsPage, RoutePage, WishlistPage } from './pages/StorePages.tsx'
import { LoginPage, RegisterPage } from './pages/AuthPages.tsx'
import { AssistantPage, BudgetBuilderPage } from './pages/AiPages.tsx'
import { ShopCatalogPage } from './pages/ShopCatalogPage.tsx'
import { AdminPage } from './pages/AdminPage.tsx'
import { CommerceProvider } from './context/CommerceContext.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CommerceProvider>
      <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/shop" element={<ShopCatalogPage />} />
        <Route path="/products/:slug" element={<ProductDetailsPage />} />
        <Route path="/categories/:category" element={<ShopCatalogPage />} />
        <Route path="/search" element={<ShopCatalogPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/wishlist" element={<WishlistPage />} />
        <Route path="/orders" element={<OrderHistoryPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/assistant" element={<AssistantPage />} />
        <Route path="/budget" element={<BudgetBuilderPage />} />
        <Route path="/compare" element={<ComparePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<RoutePage title="Page not found" description="That ShopSense destination does not exist yet." action="Return home" />} />
      </Routes>
      </BrowserRouter>
    </CommerceProvider>
  </StrictMode>,
)
