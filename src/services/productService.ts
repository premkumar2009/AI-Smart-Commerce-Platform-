export type Product = {
  id?: string
  name: string
  category: string
  categoryId?: string
  price: string
  oldPrice?: string
  rating: string
  image: string
  tone: string
  stock?: number
}

type ApiProduct = {
  id: string
  name: string
  categoryName: string
  price: number
  discountPrice?: number
  rating: number
  imageUrl?: string
  stock: number
}

type ProductPage = { content: ApiProduct[] }

const formatPrice = (value: number) => `₹${value.toLocaleString('en-IN')}`

const mapProduct = (product: ApiProduct, index: number): Product => ({
  id: product.id,
  name: product.name,
  category: product.categoryName,
  categoryId: product.id,
  price: formatPrice(product.discountPrice ?? product.price),
  oldPrice: product.discountPrice ? formatPrice(product.price) : undefined,
  rating: product.rating.toFixed(1),
  image: product.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=85',
  tone: ['coral', 'blue', 'sand', 'green'][index % 4],
  stock: product.stock,
})

export async function getProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await fetch('/api/products?page=0&size=12&sort=createdAt&direction=desc', { signal })
  if (!response.ok) throw new Error('Unable to load products')
  const page = await response.json() as ProductPage
  return page.content.map(mapProduct)
}
