import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { getBrands, getProductTypes, getTypeBrands, getProducts, type Product, type ProductFilters, type ProductTypeSummary } from '../services/productService'
import '../interaction.css'
import '../compare.css'
import './shop-catalog.css'
import './category-discovery.css'

const categories = ['electronics', 'home-kitchen', 'groceries', 'fashion', 'beauty', 'sports-fitness', 'travel', 'furniture-decor', 'gaming', 'books-stationery', 'automotive', 'pet-supplies']
const discoveryCategories = [
  { slug: 'electronics', name: 'Electronics', target: 'electronics', image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=700&q=85', examples: 'Headphones, Earbuds, Smartphones, Laptops, Keyboards, Mice, Speakers, Cameras, Monitors, Smartwatches' },
  { slug: 'home-kitchen', name: 'Home & Kitchen', target: 'home-kitchen', image: 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=700&q=85', examples: 'Cookware, Coffee Makers, Mixers, Storage Boxes, Kitchen Tools, Dinnerware, Air Fryers, Lighting' },
  { slug: 'groceries', name: 'Groceries', target: 'groceries', image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=700&q=85', examples: 'Rice, Pulses, Snacks, Cereals, Coffee, Tea, Spices, Cooking Oil, Beverages, Dry Fruits' },
  { slug: 'fashion', name: 'Fashion', target: 'fashion', image: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=700&q=85', examples: 'T-Shirts, Shirts, Jeans, Trousers, Jackets, Dresses, Hoodies, Shoes, Sandals, Bags' },
  { slug: 'beauty', name: 'Beauty & Personal Care', target: 'beauty', image: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=700&q=85', examples: 'Face Wash, Moisturizers, Serums, Shampoo, Hair Oil, Lip Tint, Body Lotion, Makeup, Grooming Kits' },
  { slug: 'sports-fitness', name: 'Sports & Fitness', target: 'sports-fitness', image: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=700&q=85', examples: 'Running Shoes, Sportswear, Yoga Mats, Dumbbells, Resistance Bands, Gym Bags, Water Bottles, Fitness Accessories' },
  { slug: 'travel-luggage', name: 'Travel & Luggage', target: 'travel', image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=700&q=85', examples: 'Backpacks, Suitcases, Duffel Bags, Travel Organizers, Passport Holders, Neck Pillows, Travel Bottles' },
  { slug: 'furniture-decor', name: 'Furniture & Decor', target: 'furniture-decor', image: 'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=700&q=85', examples: 'Desks, Chairs, Tables, Lamps, Cushions, Rugs, Wall Decor, Shelves, Storage Units' },
  { slug: 'gaming', name: 'Gaming', target: 'gaming', image: 'https://images.unsplash.com/photo-1593305841991-05c297ba4575?auto=format&fit=crop&w=700&q=85', examples: 'Gaming Keyboards, Gaming Mice, Controllers, Headsets, Webcams, Mouse Pads, Gaming Chairs, Accessories' },
  { slug: 'books-stationery', name: 'Books & Stationery', target: 'books-stationery', image: 'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?auto=format&fit=crop&w=700&q=85', examples: 'Fiction Books, Textbooks, Notebooks, Journals, Pens, Pencils, Art Supplies, Planners, Study Materials' },
  { slug: 'automotive', name: 'Automotive', target: 'automotive', image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=700&q=85', examples: 'Car Phone Mounts, Seat Covers, Cleaning Kits, Air Fresheners, Chargers, Car Organizers, Tools, Accessories' },
  { slug: 'pet-supplies', name: 'Pet Supplies', target: 'pet-supplies', image: 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=700&q=85', examples: 'Dog Food, Cat Food, Pet Toys, Collars, Leashes, Beds, Grooming Supplies, Feeding Bowls, Pet Accessories' },
]
const pageSize = 12

export function ShopCatalogPage() {
  const [params, setParams] = useSearchParams()
  const { category: routeCategory } = useParams()
  const [products, setProducts] = useState<Product[]>([])
  const [brands, setBrands] = useState<string[]>([])
  const [types, setTypes] = useState<ProductTypeSummary[]>([])
  const [total, setTotal] = useState(0)
  const [pages, setPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [mobileFilters, setMobileFilters] = useState(false)
  const [compare, setCompare] = useState<string[]>(() => {
    try {
      return JSON.parse(localStorage.getItem('shopsense-compare') || '[]') as string[]
    } catch {
      return []
    }
  })

  const queryString = params.toString()
  const page = Number(params.get('page') || 0)
  const collection = params.get('collection') || ''
  const values: ProductFilters = {
    search: params.get('q') || '',
    category: params.get('category') || routeCategory || '',
    type: params.get('type') || '',
    brand: params.get('brand') || '',
    minPrice: params.get('minPrice') || '',
    maxPrice: params.get('maxPrice') || '',
    minRating: params.get('minRating') || '',
    inStock: params.get('availability') === 'inStock',
    outOfStock: params.get('availability') === 'outOfStock',
    page,
    size: pageSize,
    sort: params.get('sort') || 'createdAt',
    direction: params.get('direction') || 'desc',
  }

  useEffect(() => {
    const controller = new AbortController()
    const currentParams = new URLSearchParams(queryString)
    const category = currentParams.get('category') || routeCategory || ''
    const type = currentParams.get('type') || ''
    if (category) void getTypeBrands(category, type || undefined, controller.signal).then(setBrands).catch(() => setBrands([]))
    else void getBrands(controller.signal).then(setBrands).catch(() => setBrands([]))
    return () => controller.abort()
  }, [queryString, routeCategory])

  useEffect(() => {
    const controller = new AbortController()
    const currentParams = new URLSearchParams(queryString)
    const category = currentParams.get('category') || routeCategory || ''
    if (category) void getProductTypes(category, controller.signal).then(setTypes).catch(() => setTypes([]))
    else window.setTimeout(() => setTypes([]), 0)
    return () => controller.abort()
  }, [queryString, routeCategory])

  useEffect(() => {
    const controller = new AbortController()
    const currentParams = new URLSearchParams(queryString)
    const availability = currentParams.get('availability')
    const requestValues: ProductFilters = {
      search: currentParams.get('q') || '',
      category: currentParams.get('category') || routeCategory || '',
      type: currentParams.get('type') || '',
      brand: currentParams.get('brand') || '',
      minPrice: currentParams.get('minPrice') || '',
      maxPrice: currentParams.get('maxPrice') || '',
      minRating: currentParams.get('minRating') || '',
      inStock: availability === 'inStock',
      outOfStock: availability === 'outOfStock',
      page: Number(currentParams.get('page') || 0),
      size: pageSize,
      sort: currentParams.get('sort') || 'createdAt',
      direction: currentParams.get('direction') || 'desc',
    }

    void getProducts(requestValues, controller.signal)
      .then((result) => {
        setProducts(result.products)
        setTotal(result.totalElements)
        setPages(result.totalPages)
        setError(false)
      })
      .catch((reason: unknown) => {
        if (!(reason instanceof DOMException && reason.name === 'AbortError')) setError(true)
      })
      .finally(() => setLoading(false))

    return () => controller.abort()
  }, [queryString, routeCategory])

  const change = (key: string, value: string) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    if (key === 'category') {
      next.delete('type')
      next.delete('brand')
    }
    if (key === 'type') next.delete('brand')
    if (key !== 'page') next.delete('page')
    setParams(next)
  }

  const clear = () => setParams(params.get('q') ? { q: params.get('q')! } : {})
  const sortValue = `${values.sort}:${values.direction}`
  const isDiscovery = !routeCategory && !params.get('category') && !values.search
  const isTypeDiscovery = Boolean(values.category && !values.type && !values.search)
  const activeCollection = discoveryCategories.find((item) => item.slug === collection) || discoveryCategories.find((item) => item.target === values.category)
  const sortChange = (value: string) => {
    const [sort, direction] = value.split(':')
    const next = new URLSearchParams(params)
    next.set('sort', sort)
    next.set('direction', direction)
    next.delete('page')
    setParams(next)
  }

  const addToCompare = (id: string) => {
    if (compare.includes(id) || compare.length >= 3) return
    const next = [...compare, id]
    setCompare(next)
    localStorage.setItem('shopsense-compare', JSON.stringify(next))
  }

  const removeFromCompare = (id: string) => {
    const next = compare.filter((item) => item !== id)
    setCompare(next)
    localStorage.setItem('shopsense-compare', JSON.stringify(next))
  }

  const filters = (
    <>
      <label>Category<select value={values.category} onChange={(event) => change('category', event.target.value)}><option value="">All categories</option>{categories.map((item) => <option key={item} value={item}>{item[0].toUpperCase() + item.slice(1)}</option>)}</select></label>
      {values.category && <label>Product type<select value={values.type} onChange={(event) => change('type', event.target.value)}><option value="">All product types</option>{types.map((type) => <option key={type.name} value={type.name.toLowerCase().replaceAll(' ', '-')}>{type.name} ({type.productCount})</option>)}</select></label>}
      <label>Brand<select value={values.brand} onChange={(event) => change('brand', event.target.value)}><option value="">All brands</option>{brands.map((brand) => <option key={brand} value={brand}>{brand}</option>)}</select></label>
      <div className="price-fields"><label>Min price<input type="number" min="0" value={values.minPrice} onChange={(event) => change('minPrice', event.target.value)} placeholder="₹0" /></label><label>Max price<input type="number" min="0" value={values.maxPrice} onChange={(event) => change('maxPrice', event.target.value)} placeholder="₹10,000" /></label></div>
      <label>Rating<select value={values.minRating} onChange={(event) => change('minRating', event.target.value)}><option value="">Any rating</option><option value="4">4★ and above</option><option value="4.5">4.5★ and above</option><option value="3">3★ and above</option></select></label>
      <label>Availability<select value={values.inStock ? 'inStock' : values.outOfStock ? 'outOfStock' : ''} onChange={(event) => change('availability', event.target.value)}><option value="">All products</option><option value="inStock">In stock</option><option value="outOfStock">Out of stock</option></select></label>
      <button className="clear-filters" onClick={clear}>Clear all filters</button>
    </>
  )

  return (
    <main className="route-page catalog-page">
      <Link className="brand route-brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link>
      <div className="section-heading"><div><p className="eyebrow">{values.search ? `Search results for ${values.search}` : values.type ? `${values.category} / ${values.type}` : values.category ? `${values.category} product types` : 'The full edit'}</p><h1>Find your <em>fit.</em></h1></div><Link className="text-link" to="/">Back home ↗</Link></div>
      <div className="catalog-search"><input defaultValue={values.search} placeholder="Search real products..." onKeyDown={(event) => { if (event.key === 'Enter') change('q', event.currentTarget.value) }} aria-label="Search products" /></div>
      <div className="mobile-filter-actions"><button className="dark-button" onClick={() => setMobileFilters(true)}>Filters</button><label>Sort <select value={sortValue} onChange={(event) => sortChange(event.target.value)}><option value="createdAt:desc">Featured</option><option value="price:asc">Price: Low to High</option><option value="price:desc">Price: High to Low</option><option value="rating:desc">Rating</option><option value="reviewCount:desc">Most popular</option></select></label></div>
      <div className={`catalog-layout ${mobileFilters ? 'filter-drawer-open' : ''}`}>
        <aside className="filter-sidebar" aria-label="Product filters"><div className="filter-heading"><strong>Filter products</strong><button onClick={() => setMobileFilters(false)} aria-label="Close filters">×</button></div>{filters}</aside>
        <section className="catalog-results">
          <div className="catalog-toolbar"><span>{loading ? 'Searching...' : `${total} products found`}</span><label>Sort <select value={sortValue} onChange={(event) => sortChange(event.target.value)}><option value="createdAt:desc">Featured</option><option value="price:asc">Price: Low to High</option><option value="price:desc">Price: High to Low</option><option value="rating:desc">Rating</option><option value="reviewCount:desc">Most popular</option></select></label></div>
          {compare.length > 0 && <Link className="compare-bar" to={`/compare?ids=${compare.join(',')}`}>Compare {compare.length} selected product{compare.length > 1 ? 's' : ''} <span>↗</span></Link>}
          {isDiscovery ? <div className="category-discovery"><div className="category-discovery-heading"><span className="eyebrow">Browse the real catalog</span><h2>Choose a category.</h2><p>Start broad, then refine with the filters on the left.</p></div><div className="category-discovery-grid">{discoveryCategories.map((category) => <Link className="discovery-card" to={`/shop?category=${category.target}&collection=${category.slug}`} key={category.slug}><img src={category.image} alt="" /><div><span className="discovery-card-index">{String(discoveryCategories.indexOf(category) + 1).padStart(2, '0')}</span><h3>{category.name}</h3><p>{category.examples}</p><span className="discovery-card-link">Explore category ↗</span></div></Link>)}</div></div> : isTypeDiscovery ? <div className="category-discovery"><div className="category-discovery-heading"><span className="eyebrow">{values.category} collection</span><h2>Choose a product type.</h2><p>Every card is backed by available products in the live catalog.</p></div><div className="category-discovery-grid type-card-grid">{types.map((type) => <Link className="discovery-card type-card" to={`/shop?category=${values.category}&type=${encodeURIComponent(type.name.toLowerCase().replaceAll(' ', '-'))}`} key={type.name}><div className="type-card-images">{type.images.slice(0, 3).map((image, index) => <img src={image} alt={`${type.name} representative ${index + 1}`} loading="lazy" key={`${image}-${index}`} />)}</div><div><span className="discovery-card-index">{type.productCount} products</span><h3>{type.name}</h3><p>Real {type.name.toLowerCase()} from the current catalog.</p><span className="discovery-card-link">View {type.name} ↗</span></div></Link>)}</div></div> : error ? <div className="catalog-state"><strong>Catalog unavailable</strong><p>Check the backend status and try again.</p></div> : !loading && products.length === 0 ? <div className="catalog-state"><strong>No products found.</strong><p>Try adjusting your filters.</p><button className="clear-filters" onClick={clear}>Clear all filters</button></div> : <>{activeCollection && <section className="category-guide"><div><span className="eyebrow">Category guide</span><h2>{activeCollection.name}</h2><p>Explore the real {activeCollection.name.toLowerCase()} catalog, then refine it with the filters.</p></div><div className="category-examples"><strong>Examples</strong><span>{activeCollection.examples}</span></div></section>}<div className="product-grid">
            {products.map((product) => {
              const selected = Boolean(product.id && compare.includes(product.id))
              return <article className="product-card" key={product.id}>
                <Link className="product-card-link" to={`/products/${product.slug}`} aria-label={`View ${product.name}`}>
                  <div className="product-image"><img src={product.image} alt={product.name} loading="lazy" />{product.stock === 0 && <span className="stock-label">Out of stock</span>}</div>
                  <div className="product-meta"><div><p className="product-category">{product.brand}</p><h3>{product.name}</h3></div><span className="rating">★ {product.rating} · {product.reviewCount} reviews</span></div>
                  <div className="product-price"><strong>{product.price}</strong>{product.oldPrice && <del>{product.oldPrice}</del>}</div>
                </Link>
                <button type="button" className="compare-toggle" aria-pressed={selected} title={selected ? 'Double-click to remove from comparison' : 'Add to comparison'} onMouseDown={(event) => { event.preventDefault(); event.stopPropagation(); if (product.id) addToCompare(product.id) }} onClick={(event) => { event.stopPropagation(); if (product.id) addToCompare(product.id) }} onDoubleClick={(event) => { event.stopPropagation(); if (product.id && selected) removeFromCompare(product.id) }}>{selected ? 'Selected' : 'Compare'}</button>
              </article>
            })}
          </div></>}
          {pages > 1 && <nav className="pagination" aria-label="Product pages"><button disabled={page === 0} onClick={() => change('page', String(page - 1))}>Previous</button>{Array.from({ length: Math.min(pages, 5) }, (_, index) => <button key={index} className={index === page ? 'active' : ''} onClick={() => change('page', String(index))}>{index + 1}</button>)}<button disabled={page >= pages - 1} onClick={() => change('page', String(page + 1))}>Next</button></nav>}
        </section>
      </div>
    </main>
  )
}
