import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  createOrder,
  createReview,
  getAiCompare,
  getAiRecommendations,
  getAiSimilar,
  getOrder,
  getProducts,
  getOrders,
  getProductReviews,
  mapProduct,
  type ApiProduct,
  type Order,
  type Product,
  type ProductFilters,
  type ReviewSummary,
} from '../services/productService'
import { useCommerce } from '../context/CommerceContext'
import '../interaction.css'
import '../compare.css'

export function RoutePage({ title, description, action }: { title: string; description: string; action: string }) {
  const navigate = useNavigate()

  return (
    <main className="route-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <p className="eyebrow">ShopSense AI</p>
      <h1>{title}</h1>
      <p className="route-description">{description}</p>
      <button className="dark-button route-action" onClick={() => navigate(action === 'Return home' ? '/' : '/shop')}>
        {action} <span>↗</span>
      </button>
    </main>
  )
}

export function ShopPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [compareIds, setCompareIds] = useState<string[]>(() => {
    try {
      return JSON.parse(localStorage.getItem('shopsense-compare') || '[]') as string[]
    } catch {
      return []
    }
  })

  const { addToCart, toggleWishlist, isInWishlist } = useCommerce()
  const query = searchParams.get('q') || searchParams.get('search') || ''
  const queryString = searchParams.toString()
  const currentPage = Number(searchParams.get('page') || 0)

  const filterValues: ProductFilters = {
    search: query,
    category: searchParams.get('category') || '',
    brand: searchParams.get('brand') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    minRating: searchParams.get('minRating') || '',
    inStock: searchParams.get('availability') === 'inStock',
    outOfStock: searchParams.get('availability') === 'outOfStock',
    page: currentPage,
    size: 12,
    sort: searchParams.get('sort') || 'createdAt',
    direction: searchParams.get('direction') || 'desc',
  }

  useEffect(() => {
    const controller = new AbortController()
    const params = new URLSearchParams(queryString)
    const availability = params.get('availability')

    const requestFilters: ProductFilters = {
      search: params.get('q') || params.get('search') || '',
      category: params.get('category') || '',
      brand: params.get('brand') || '',
      minPrice: params.get('minPrice') || '',
      maxPrice: params.get('maxPrice') || '',
      minRating: params.get('minRating') || '',
      inStock: availability === 'inStock',
      outOfStock: availability === 'outOfStock',
      page: Number(params.get('page') || 0),
      size: 12,
      sort: params.get('sort') || 'createdAt',
      direction: params.get('direction') || 'desc',
    }

    void getProducts(requestFilters, controller.signal)
      .then((result) => {
        setProducts(result.products)
        setError(false)
      })
      .catch(() => {
        setProducts([])
        setError(true)
      })
      .finally(() => setLoading(false))

    return () => {
      controller.abort()
    }
  }, [queryString])

  const updateFilters = (changes: Partial<ProductFilters>) => {
    const next = new URLSearchParams(searchParams)
    Object.entries(changes).forEach(([key, value]) => {
      if (value === '' || value === false) next.delete(key)
      else next.set(key, String(value))
    })
    next.delete('page')
    setSearchParams(next)
  }

  const clearFilters = () => setSearchParams(query ? { q: query } : {})

  const sortChange = (sortString: string) => {
    const [sort, direction] = sortString.split(':')
    updateFilters({ sort, direction: direction as 'asc' | 'desc' })
  }

  const sortValue = `${filterValues.sort}:${filterValues.direction}`

  const toggleCompare = (id: string) => {
    const next = compareIds.includes(id)
      ? compareIds.filter((selected) => selected !== id)
      : compareIds.length >= 3
        ? compareIds
        : [...compareIds, id]
    setCompareIds(next)
    localStorage.setItem('shopsense-compare', JSON.stringify(next))
  }

  return (
    <main className="route-page catalog-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>

      <div className="section-heading">
        <div>
          <p className="eyebrow">{query ? `Search results for ${query}` : 'The full edit'}</p>
          <h1>Find your <em>fit.</em></h1>
        </div>
        <Link className="text-link" to="/">Back home ↗</Link>
      </div>

      <div className="catalog-search">
        <input
          defaultValue={query}
          placeholder="Search real products..."
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              const next = new URLSearchParams(searchParams)
              next.set('q', event.currentTarget.value)
              next.delete('page')
              setSearchParams(next)
            }
          }}
          aria-label="Search products"
        />
      </div>

      <div className="mobile-filter-actions">
        <button className="dark-button" onClick={() => undefined}>Filters</button>
        <label>
          Sort
          <select value={sortValue} onChange={(event) => sortChange(event.target.value)}>
            <option value="createdAt:desc">Featured</option>
            <option value="price:asc">Price: Low to High</option>
            <option value="price:desc">Price: High to Low</option>
            <option value="rating:desc">Rating</option>
          </select>
        </label>
      </div>

      <div className="catalog-layout">
        <aside className="filter-sidebar" aria-label="Product filters">
          <div className="filter-heading"><strong>Filter products</strong></div>

          <label>
            Category
            <select value={filterValues.category} onChange={(event) => updateFilters({ category: event.target.value })}>
              <option value="">All categories</option>
              {['electronics', 'home-kitchen', 'groceries', 'fashion', 'beauty', 'sports-fitness', 'travel', 'furniture-decor', 'gaming', 'books-stationery', 'automotive', 'pet-supplies'].map((category) => (
                <option value={category} key={category}>{category[0].toUpperCase() + category.slice(1)}</option>
              ))}
            </select>
          </label>

          <div className="price-fields">
            <label>
              Min price
              <input type="number" min="0" value={filterValues.minPrice} onChange={(event) => updateFilters({ minPrice: event.target.value })} placeholder="₹0" />
            </label>
            <label>
              Max price
              <input type="number" min="0" value={filterValues.maxPrice} onChange={(event) => updateFilters({ maxPrice: event.target.value })} placeholder="₹10,000" />
            </label>
          </div>

          <label>
            Rating
            <select value={filterValues.minRating} onChange={(event) => updateFilters({ minRating: event.target.value })}>
              <option value="">Any rating</option>
              <option value="4">4★ and above</option>
              <option value="4.5">4.5★ and above</option>
            </select>
          </label>

          <label>
            Availability
            <select value={filterValues.inStock ? 'inStock' : filterValues.outOfStock ? 'outOfStock' : ''} onChange={(event) => {
              const value = event.target.value
              if (value === 'inStock') updateFilters({ inStock: true, outOfStock: false })
              else if (value === 'outOfStock') updateFilters({ inStock: false, outOfStock: true })
              else updateFilters({ inStock: false, outOfStock: false })
            }}>
              <option value="">All products</option>
              <option value="inStock">In stock</option>
              <option value="outOfStock">Out of stock</option>
            </select>
          </label>

          <button className="clear-filters" onClick={clearFilters}>Clear all filters</button>
        </aside>

        <section className="catalog-results">
          <div className="catalog-toolbar">
            <span>{loading ? 'Searching...' : `${products.length} products found`}</span>
            <label>
              Sort
              <select value={sortValue} onChange={(event) => sortChange(event.target.value)}>
                <option value="createdAt:desc">Featured</option>
                <option value="price:asc">Price: Low to High</option>
                <option value="price:desc">Price: High to Low</option>
                <option value="rating:desc">Rating</option>
              </select>
            </label>
          </div>

          {error ? (
            <div className="catalog-state">
              <strong>Product catalog unavailable.</strong>
              <p>Check the backend status and try again.</p>
            </div>
          ) : (
            <div className="product-grid">
              {products.map((product) => (
                <article className={`product-card ${product.tone}`} key={product.id || product.name}>
                  <div className="product-image">
                    <Link to={`/products/${product.slug}`}>
                      <img src={product.image} alt={product.name} loading="lazy" />
                    </Link>
                    <button
                      type="button"
                      className={`heart-button ${product.id && isInWishlist(product.id) ? 'liked' : ''}`}
                      aria-label="Save to wishlist"
                      onClick={() => product.id && void toggleWishlist(product.id)}
                    >
                      ♥
                    </button>
                  </div>

                  <div className="product-meta">
                    <div>
                      <p className="product-category">{product.category}</p>
                      <Link className="product-name-button" to={`/products/${product.slug}`}>
                        <h3>{product.name}</h3>
                      </Link>
                    </div>
                    <span className="rating">★ {product.rating}</span>
                  </div>

                  <div className="product-price">
                    <strong>{product.price}</strong>
                    {product.oldPrice && <del>{product.oldPrice}</del>}
                    <button type="button" className="dark-button add-to-bag" onClick={() => product.id && void addToCart(product.id)}>
                      + Bag
                    </button>
                  </div>

                  <button type="button" className="compare-toggle" onClick={() => product.id && toggleCompare(product.id)}>
                    {product.id && compareIds.includes(product.id) ? 'Remove from compare' : 'Add to compare'}
                  </button>
                </article>
              ))}
            </div>
          )}

          {compareIds.length > 0 && (
            <Link className="compare-bar" to={`/compare?ids=${compareIds.join(',')}`}>
              Compare {compareIds.length} selected product{compareIds.length > 1 ? 's' : ''} ↗
            </Link>
          )}
        </section>
      </div>
    </main>
  )
}

export function ComparePage() {
  const [params] = useSearchParams()
  const [products, setProducts] = useState<Product[]>([])
  const [summary, setSummary] = useState('')
  const [aiAnalysis, setAiAnalysis] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const { addToCart } = useCommerce()

  useEffect(() => {
    const selectedIds = (new URLSearchParams(params.toString()).get('ids') || '').split(',').filter(Boolean).slice(0, 3)
    if (!selectedIds.length) return

    void Promise.all(selectedIds.map((id) => fetch(`/api/products/${encodeURIComponent(id)}`).then((response) => response.ok ? response.json() as Promise<ApiProduct> : Promise.reject())))
      .then((result) => {
        const mappedProducts = result.map((item) => mapProduct(item, 0))
        setProducts(mappedProducts)
        setSummary(mappedProducts.length ? `Comparing ${mappedProducts.length} live catalog products side by side.` : '')
        setAiAnalysis('')
      })
      .catch(() => {
        setProducts([])
        setSummary('')
        setAiAnalysis('')
      })
  }, [params])

  const handleAiCompare = async () => {
    if (!products.length || !products[0].id) return
    setAiLoading(true)
    try {
      const result = await getAiCompare(products.map((p) => p.id!).filter(Boolean))
      setAiAnalysis(result.summary)
    } catch {
      setAiAnalysis('AI comparison is temporarily unavailable.')
    } finally {
      setAiLoading(false)
    }
  }

  return (
    <main className="route-page compare-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <p className="eyebrow">Decision support, without the noise</p>
      <h1>Compare <em>clearly.</em></h1>
      <p className="route-description">Real catalog data side by side, so the best fit is easier to see.</p>

      {!products.length ? (
        <div className="catalog-state">
          <strong>Select up to three products from the shop.</strong>
          <Link className="dark-button route-action" to="/shop">Browse products <span>↗</span></Link>
        </div>
      ) : (
        <>
          {summary && <p className="route-description">{summary}</p>}
          <div className="comparison-table">
            <div className="comparison-row comparison-head">
              <span>Product</span>
              {products.map((product) => <strong key={product.id}>{product.name}</strong>)}
            </div>

            {[
              ['Brand', (product: Product) => product.brand || '-'],
              ['Price', (product: Product) => product.price],
              ['Rating', (product: Product) => `★ ${product.rating}`],
              ['Availability', (product: Product) => product.stock && product.stock > 0 ? 'In stock' : 'Out of stock'],
              ['Category', (product: Product) => product.category],
            ].map(([label, value]) => (
              <div className="comparison-row" key={String(label)}>
                <span>{String(label)}</span>
                {products.map((product) => <strong key={product.id}>{String((value as (item: Product) => string)(product))}</strong>)}
              </div>
            ))}

            <div className="comparison-row">
              <span>Action</span>
              {products.map((product) => (
                <div key={product.id} style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <Link className="compare-product-link" to={`/products/${product.slug}`}>View product ↗</Link>
                  <button className="dark-button" type="button" onClick={() => void addToCart(product.id!)}>Add to bag</button>
                </div>
              ))}
            </div>
          </div>

          <div style={{ marginTop: 24 }}>
            <button className="dark-button" onClick={() => void handleAiCompare()} disabled={aiLoading} style={{ marginBottom: 12 }}>
              ✦ {aiLoading ? 'Analyzing...' : 'Compare with AI'}
            </button>
            {aiAnalysis && (
              <div style={{ border: '1px solid #e7dfd5', borderRadius: 12, padding: 16, marginTop: 12 }}>
                <strong>AI Analysis</strong>
                <p>{aiAnalysis}</p>
              </div>
            )}
          </div>
        </>
      )}
    </main>
  )
}

export function ProductDetailsPage() {
  const { slug = '' } = useParams()
  const { addToCart, toggleWishlist, isInWishlist } = useCommerce()
  const [product, setProduct] = useState<Product | null>(null)
  const [error, setError] = useState(false)
  const [quantity, setQuantity] = useState(1)
  const [message, setMessage] = useState('')
  const [recommendations, setRecommendations] = useState<Product[]>([])
  const [similar, setSimilar] = useState<Product[]>([])
  const [reviews, setReviews] = useState<ReviewSummary | null>(null)
  const [reviewText, setReviewText] = useState('')
  const [reviewRating, setReviewRating] = useState(5)

  useEffect(() => {
    fetch(`/api/products/slug/${encodeURIComponent(slug)}`)
      .then((response) => (response.ok ? response.json() as Promise<ApiProduct> : Promise.reject()))
      .then(async (data) => {
        const mapped = mapProduct(data, 0)
        setProduct(mapped)

        try {
          const rec = await getAiRecommendations(mapped.category, mapped.name, mapped.id, 4)
          setRecommendations(rec.products.map(mapProduct))
        } catch {
          setRecommendations([])
        }

        try {
          const sim = await getAiSimilar(mapped.id ?? '', 4)
          setSimilar(sim.products.map(mapProduct))
        } catch {
          setSimilar([])
        }

        try {
          const reviewSummary = await getProductReviews(mapped.id ?? '')
          setReviews(reviewSummary)
        } catch {
          setReviews({ averageRating: 0, reviewCount: 0, reviews: [] })
        }
      })
      .catch(() => setError(true))
  }, [slug])

  const addToBag = async () => {
    if (!product?.id) return
    try {
      await addToCart(product.id, quantity)
      setMessage('Added to your bag.')
    } catch (reason) {
      setMessage(reason instanceof Error ? reason.message : 'Unable to update your bag.')
    }
  }

  const handleToggleWishlist = async () => {
    if (!product?.id) return
    const wasSaved = isInWishlist(product.id)
    try {
      await toggleWishlist(product.id)
      setMessage(wasSaved ? 'Removed from wishlist.' : 'Saved to wishlist.')
    } catch {
      setMessage('Wishlist update failed.')
    }
  }

  const submitReview = async () => {
    if (!product?.id || !reviewText.trim()) return
    try {
      await createReview(product.id, reviewRating, reviewText.trim())
      const nextReviews = await getProductReviews(product.id)
      setReviews(nextReviews)
      setReviewText('')
      setReviewRating(5)
      setMessage('Thanks for reviewing this product.')
    } catch {
      setMessage('Could not submit your review right now.')
    }
  }

  if (error) return <RoutePage title="Product unavailable" description="We could not find that product in the live catalog." action="Explore products" />
  if (!product) return <RoutePage title="Loading product" description="We are bringing the details into focus." action="Return home" />

  const discount = product.oldPrice ? Math.round((1 - Number(product.price.replace(/[^0-9]/g, '')) / Number(product.oldPrice.replace(/[^0-9]/g, ''))) * 100) : 0
  const wishlistSaved = isInWishlist(product.id!)

  return (
    <main className="route-page product-detail-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <Link className="text-link" to="/shop">← Back to products</Link>

      <div className="detail-layout">
        <div className="detail-image">
          <img src={product.image} alt={`${product.name} product image`} />
        </div>

        <div className="detail-copy">
          <p className="eyebrow">{product.category} · {product.brand}</p>
          <h1>{product.name}</h1>
          <p className="detail-short-description">{product.shortDescription}</p>
          <p className="rating">★ {product.rating} · {reviews?.reviewCount ?? product.reviewCount ?? 0} reviews</p>

          <div className="detail-price">
            <strong>{product.price}</strong>
            {product.oldPrice && <><del>{product.oldPrice}</del><span className="discount-badge">{discount}% off</span></>}
          </div>

          <p className="stock-status">{product.inStock ? `${product.stock} available` : 'Out of stock'}</p>
          <div className="quantity-control">
            <button onClick={() => setQuantity(Math.max(1, quantity - 1))} aria-label="Decrease quantity">−</button>
            <span>{quantity}</span>
            <button onClick={() => setQuantity(Math.min(product.stock || 1, quantity + 1))} aria-label="Increase quantity">+</button>
          </div>

          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            <button className="dark-button route-action" disabled={!product.inStock} onClick={() => void addToBag()}>Add to bag <span>↗</span></button>
            <button className="dark-button route-action" onClick={() => void handleToggleWishlist()}>{wishlistSaved ? 'Saved to wishlist' : 'Save to wishlist'} <span>♡</span></button>
          </div>

          {message && <p className="route-description detail-message" role="status">{message}</p>}

          <div className="spec-grid">
            <span>Material<strong>{product.material || 'Not specified'}</strong></span>
            <span>Color<strong>{product.color || 'Not specified'}</strong></span>
            <span>SKU<strong>{product.sku || 'Catalog item'}</strong></span>
          </div>

          <p className="route-description detail-description">{product.description}</p>
          <p className="tag-list">{product.tags?.split(',').map((tag) => <span key={tag}>{tag.trim()}</span>)}</p>
        </div>
      </div>

      <section style={{ marginTop: 32 }}>
        <h2>AI recommendations</h2>
        {recommendations.length ? (
          <div className="product-grid">
            {recommendations.map((item) => (
              <article className="product-card" key={item.id}>
                <div className="product-image"><Link to={`/products/${item.slug}`}><img src={item.image} alt={item.name} /></Link><button type="button" className="heart-button" aria-label="Save to wishlist" onClick={() => void toggleWishlist(item.id!)}>♡</button></div>
                <div className="product-meta">
                  <p className="product-category">{item.category}</p>
                  <Link className="product-name-button" to={`/products/${item.slug}`}><h3>{item.name}</h3></Link>
                </div>
                <div className="product-price">
                  <strong>{item.price}</strong>
                  <button className="dark-button add-to-bag" onClick={() => item.id && void addToCart(item.id, 1)}>+ Bag</button>
                </div>
              </article>
            ))}
          </div>
        ) : <p className="route-description">No AI recommendations were produced for this product.</p>}
      </section>

      <section style={{ marginTop: 32 }}>
        <h2>Similar items</h2>
        {similar.length ? (
          <div className="product-grid">
            {similar.map((item) => (
              <article className="product-card" key={item.id}>
                <div className="product-image"><Link to={`/products/${item.slug}`}><img src={item.image} alt={item.name} /></Link><button type="button" className="heart-button" aria-label="Save to wishlist" onClick={() => void toggleWishlist(item.id!)}>♡</button></div>
                <div className="product-meta">
                  <p className="product-category">{item.category}</p>
                  <Link className="product-name-button" to={`/products/${item.slug}`}><h3>{item.name}</h3></Link>
                </div>
                <div className="product-price">
                  <strong>{item.price}</strong>
                  <button className="dark-button add-to-bag" onClick={() => item.id && void addToCart(item.id, 1)}>+ Bag</button>
                </div>
              </article>
            ))}
          </div>
        ) : <p className="route-description">No similar items were found.</p>}
      </section>

      <section style={{ marginTop: 32 }}>
        <h2>Customer reviews</h2>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 16 }}>
          <label>
            Rating
            <select value={reviewRating} onChange={(event) => setReviewRating(Number(event.target.value))}>
              <option value={5}>5</option>
              <option value={4}>4</option>
              <option value={3}>3</option>
              <option value={2}>2</option>
              <option value={1}>1</option>
            </select>
          </label>
          <input value={reviewText} onChange={(event) => setReviewText(event.target.value)} placeholder="Write a review" aria-label="Write a review" style={{ flex: 1 }} />
          <button className="dark-button" onClick={() => void submitReview()}>Post review</button>
        </div>

        {reviews?.reviews.length ? (
          reviews.reviews.map((review) => (
            <div key={review.id} style={{ border: '1px solid #e7dfd5', borderRadius: 12, padding: 12, marginBottom: 10 }}>
              <strong>{review.userName}</strong>
              <div>★ {review.rating}</div>
              <p>{review.reviewText}</p>
            </div>
          ))
        ) : <p className="route-description">No reviews yet—be the first to share your take.</p>}
      </section>
    </main>
  )
}

export function CartPage() {
  const navigate = useNavigate()
  const { cart, updateQuantity, removeFromCart } = useCommerce()

  if (!cart) return <RoutePage title="Your bag" description="Loading your selected products." action="Return home" />

  return (
    <main className="route-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <h1>Your bag</h1>

      {cart.items.length ? (
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24 }}>
          <div>
            {cart.items.map((item) => (
              <div key={item.id} style={{ display: 'grid', gridTemplateColumns: '120px 1fr auto', alignItems: 'center', gap: 16, border: '1px solid #e7dfd5', borderRadius: 12, padding: 12, marginBottom: 12 }}>
                <img src={item.imageUrl || '/product-image-fallback.svg'} alt={item.productName} style={{ width: 120, height: 120, objectFit: 'cover', borderRadius: 8 }} />
                <div>
                  <strong>{item.productName}</strong>
                  <div>₹{item.unitPrice.toLocaleString('en-IN')}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 8 }}>
                    <button onClick={() => void updateQuantity(item.id, Math.max(1, item.quantity - 1))}>−</button>
                    <span>{item.quantity}</span>
                    <button onClick={() => void updateQuantity(item.id, item.quantity + 1)}>+</button>
                  </div>
                </div>
                <div>
                  <strong>₹{item.lineTotal.toLocaleString('en-IN')}</strong>
                  <div><button onClick={() => void removeFromCart(item.id)}>Remove</button></div>
                </div>
              </div>
            ))}
          </div>

          <aside style={{ border: '1px solid #e7dfd5', borderRadius: 12, padding: 16, height: 'fit-content' }}>
            <h3>Order summary</h3>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Subtotal</span><strong>₹{cart.subtotal.toLocaleString('en-IN')}</strong></div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Shipping</span><strong>₹{cart.shipping.toLocaleString('en-IN')}</strong></div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12 }}><span>Total</span><strong>₹{cart.total.toLocaleString('en-IN')}</strong></div>
            <button className="dark-button" style={{ marginTop: 16, width: '100%' }} onClick={() => navigate('/checkout')}>Proceed to checkout</button>
          </aside>
        </div>
      ) : <p className="route-description">Your bag is empty. Start with a product you love.</p>}
    </main>
  )
}

export function CheckoutPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', phone: '', address: '', city: '', state: '', postalCode: '', country: 'India' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (field: string, value: string) => setForm((current) => ({ ...current, [field]: value }))

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const order = await createOrder({
        fullName: form.fullName,
        phone: form.phone,
        address: form.address,
        city: form.city,
        state: form.state,
        postalCode: form.postalCode,
        country: form.country,
      })
      navigate('/orders', { state: { lastOrder: order.id } })
    } catch {
      setError('Your order could not be placed. Please check your bag and shipping details.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="route-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <p className="eyebrow">Secure delivery details</p>
      <h1>Complete your order.</h1>
      <p className="route-description">A calm final step. Your catalog prices and delivery total are confirmed before the order is placed.</p>
      <form className="checkout-layout" onSubmit={submit}>
        <section className="checkout-form-panel">
          <h2>Where should we deliver?</h2>
          <div className="checkout-fields">
            <label>Full name<input placeholder="Your name" value={form.fullName} onChange={(event) => handleChange('fullName', event.target.value)} required /></label>
            <label>Phone number<input placeholder="10-digit phone number" value={form.phone} onChange={(event) => handleChange('phone', event.target.value)} required /></label>
            <label className="checkout-wide">Address<input placeholder="House number and street" value={form.address} onChange={(event) => handleChange('address', event.target.value)} required /></label>
            <label>City<input placeholder="City" value={form.city} onChange={(event) => handleChange('city', event.target.value)} required /></label>
            <label>State<input placeholder="State" value={form.state} onChange={(event) => handleChange('state', event.target.value)} required /></label>
            <label>Postal code<input placeholder="Postal code" value={form.postalCode} onChange={(event) => handleChange('postalCode', event.target.value)} required /></label>
            <label>Country<input placeholder="Country" value={form.country} onChange={(event) => handleChange('country', event.target.value)} required /></label>
          </div>
        </section>
        <aside className="checkout-summary"><span className="eyebrow">Ready when you are</span><h2>Secure checkout</h2><p>Your order is created from the live bag and will appear in your order history immediately.</p>{error && <p className="form-error">{error}</p>}<button className="dark-button" type="submit" disabled={submitting}>{submitting ? 'Placing order...' : 'Place order'} <span>↗</span></button></aside>
      </form>
    </main>
  )
}

export function WishlistPage() {
  const { wishlist, addToCart, toggleWishlist } = useCommerce()

  return (
    <main className="route-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <h1>Your wishlist</h1>
      {wishlist?.items.length ? (
        <div className="product-grid">
          {wishlist.items.map((item) => (
            <article className="product-card" key={item.productId}>
              <div className="product-image">
                <Link to={`/products/${item.productSlug}`}>
                  <img src={item.imageUrl || '/product-image-fallback.svg'} alt={item.productName} />
                </Link>
                <button type="button" className="heart-button liked" aria-label="Remove from wishlist" onClick={() => void toggleWishlist(item.productId)}>♥</button>
              </div>
              <div className="product-meta">
                <p className="product-category">{item.brand}</p>
                <Link className="product-name-button" to={`/products/${item.productSlug}`}><h3>{item.productName}</h3></Link>
              </div>
              <div className="product-price"><strong>{item.price}</strong><button type="button" className="dark-button add-to-bag" onClick={() => void addToCart(item.productId)}>+ Bag</button></div>
            </article>
          ))}
        </div>
      ) : <p className="route-description">No saved products yet.</p>}
    </main>
  )
}

export function OrderHistoryPage() {
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => {
    void (async () => {
      try {
        const result = await getOrders()
        setOrders(result)
      } catch {
        setOrders([])
      }
    })()
  }, [])

  return (
    <main className="route-page">
      <Link className="brand route-brand" to="/">
        <span className="brand-mark">S</span>
        <span>ShopSense <i>AI</i></span>
      </Link>
      <h1>Order history</h1>
      {orders.length ? (
        orders.map((order) => (
          <div key={order.id} style={{ border: '1px solid #e7dfd5', borderRadius: 12, padding: 16, marginBottom: 12 }}>
            <Link to={`/orders/${order.id}`}><strong>Order #{order.id.slice(0, 8)}</strong></Link>
            <div className="order-meta"><span>{order.status}</span><span>{order.itemCount} items</span><span>₹{order.total.toLocaleString('en-IN')}</span><span>{new Date(order.createdAt).toLocaleDateString()}</span></div>
          </div>
        ))
      ) : <p className="route-description">No orders yet.</p>}
    </main>
  )
}

export function OrderDetailPage() {
  const { id = '' } = useParams()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState('')
  useEffect(() => { void getOrder(id).then(setOrder).catch((reason) => setError(reason instanceof Error ? reason.message : 'Unable to load order')) }, [id])
  if (error) return <RoutePage title="Order unavailable" description={error} action="Return home" />
  if (!order) return <RoutePage title="Loading order" description="Preparing your order details." action="Return home" />
  return <main className="route-page order-detail-page"><Link className="brand route-brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link><Link className="text-link" to="/orders">← Order history</Link><div className="order-detail-heading"><div><p className="eyebrow">Order placed {new Date(order.createdAt).toLocaleDateString()}</p><h1>Order #{order.id.slice(0, 8)}</h1></div><span className="status-pill">{order.status}</span></div><div className="order-detail-layout"><section><h2>Items in this order</h2>{order.items.map((item) => <div className="order-detail-item" key={item.id}><img src={item.imageUrl} alt="" /><div><strong>{item.productName}</strong><span>{item.quantity} × ₹{item.unitPrice.toLocaleString('en-IN')}</span></div><strong>₹{item.lineTotal.toLocaleString('en-IN')}</strong></div>)}</section><aside className="order-summary-card"><h2>Delivery</h2><p>{order.shippingDetails.fullName}<br />{order.shippingDetails.address}<br />{order.shippingDetails.city}, {order.shippingDetails.state} {order.shippingDetails.postalCode}<br />{order.shippingDetails.country}</p><hr /><div><span>Subtotal</span><strong>₹{order.subtotal.toLocaleString('en-IN')}</strong></div><div><span>Shipping</span><strong>{order.shipping ? `₹${order.shipping.toLocaleString('en-IN')}` : 'Free'}</strong></div><div className="order-total"><span>Total</span><strong>₹{order.total.toLocaleString('en-IN')}</strong></div></aside></div></main>
}
