import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'
import {
  getCart,
  addCartItem,
  updateCartItem,
  removeCartItem,
  clearCart as clearCartApi,
  getWishlist,
  addWishlistItem,
  removeWishlistItem,
  ensureDemoSession,
  type Cart,
  type WishlistResponse,
} from '../services/productService'

type CommerceContextType = {
  cart: Cart | null
  wishlist: WishlistResponse | null
  cartLoading: boolean
  cartError: string | null
  wishlistLoading: boolean
  wishlistError: string | null
  refreshCart: () => Promise<void>
  refreshWishlist: () => Promise<void>
  addToCart: (productId: string, quantity?: number) => Promise<void>
  updateQuantity: (itemId: string, quantity: number) => Promise<void>
  removeFromCart: (itemId: string) => Promise<void>
  clearCart: () => Promise<void>
  toggleWishlist: (productId: string) => Promise<void>
  isInWishlist: (productId: string) => boolean
}

const CommerceContext = createContext<CommerceContextType | undefined>(undefined)

export function CommerceProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart | null>(null)
  const [wishlist, setWishlist] = useState<WishlistResponse | null>(null)
  const [cartLoading, setCartLoading] = useState(false)
  const [cartError, setCartError] = useState<string | null>(null)
  const [wishlistLoading, setWishlistLoading] = useState(false)
  const [wishlistError, setWishlistError] = useState<string | null>(null)

  const refreshCart = useCallback(async () => {
    setCartLoading(true)
    setCartError(null)
    try {
      const data = await getCart()
      setCart(data)
    } catch (reason) {
      setCartError(reason instanceof Error ? reason.message : 'Unable to load cart')
      setCart(null)
    } finally {
      setCartLoading(false)
    }
  }, [])

  const refreshWishlist = useCallback(async () => {
    setWishlistLoading(true)
    setWishlistError(null)
    try {
      const data = await getWishlist()
      setWishlist(data)
    } catch (reason) {
      setWishlistError(reason instanceof Error ? reason.message : 'Unable to load wishlist')
      setWishlist(null)
    } finally {
      setWishlistLoading(false)
    }
  }, [])

  useEffect(() => {
    const initializeCommerce = async () => {
      await ensureDemoSession()
      await Promise.all([refreshCart(), refreshWishlist()])
    }
    void initializeCommerce()
  }, [refreshCart, refreshWishlist])

  const addToCart = useCallback(
    async (productId: string, quantity = 1) => {
      setCartError(null)
      try {
        const data = await addCartItem(productId, quantity)
        setCart(data)
      } catch (reason) {
        const message = reason instanceof Error ? reason.message : 'Unable to add to cart'
        setCartError(message)
        throw new Error(message, { cause: reason })
      }
    },
    []
  )

  const updateQuantity = useCallback(async (itemId: string, quantity: number) => {
    setCartError(null)
    try {
      const data = await updateCartItem(itemId, quantity)
      setCart(data)
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : 'Unable to update quantity'
      setCartError(message)
      throw new Error(message, { cause: reason })
    }
  }, [])

  const removeFromCart = useCallback(async (itemId: string) => {
    setCartError(null)
    try {
      await removeCartItem(itemId)
      await refreshCart()
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : 'Unable to remove from cart'
      setCartError(message)
      throw new Error(message, { cause: reason })
    }
  }, [refreshCart])

  const clearCart = useCallback(async () => {
    setCartError(null)
    try {
      await clearCartApi()
      setCart(null)
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : 'Unable to clear cart'
      setCartError(message)
      throw new Error(message, { cause: reason })
    }
  }, [])

  const toggleWishlist = useCallback(
    async (productId: string) => {
      setWishlistError(null)
      try {
        const alreadySaved = wishlist?.items.some((item) => item.productId === productId)
        if (alreadySaved) {
          await removeWishlistItem(productId)
          setWishlist((current) =>
            current
              ? {
                  ...current,
                  items: current.items.filter((item) => item.productId !== productId),
                  itemCount: Math.max(0, current.itemCount - 1),
                }
              : current
          )
        } else {
          const data = await addWishlistItem(productId)
          setWishlist(data)
        }
      } catch (reason) {
        const message = reason instanceof Error ? reason.message : 'Unable to update wishlist'
        setWishlistError(message)
        throw new Error(message, { cause: reason })
      }
    },
    [wishlist]
  )

  const isInWishlist = useCallback(
    (productId: string) => Boolean(wishlist?.items.some((item) => item.productId === productId)),
    [wishlist]
  )

  return (
    <CommerceContext.Provider
      value={{
        cart,
        wishlist,
        cartLoading,
        cartError,
        wishlistLoading,
        wishlistError,
        refreshCart,
        refreshWishlist,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
        toggleWishlist,
        isInWishlist,
      }}
    >
      {children}
    </CommerceContext.Provider>
  )
}

// The provider and hook intentionally share this module as one context API.
// eslint-disable-next-line react-refresh/only-export-components
export function useCommerce(): CommerceContextType {
  const context = useContext(CommerceContext)
  if (!context) {
    throw new Error('useCommerce must be used within CommerceProvider')
  }
  return context
}
