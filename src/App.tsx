import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  askAiAssistant,
  getProducts,
  mapProduct,
  searchAi,
  type ApiProduct,
  type Product,
} from './services/productService'
import { useCommerce } from './context/CommerceContext'
import './interaction.css'

type Panel = 'assistant' | null

function App() {
  const navigate = useNavigate()
  const { cart, wishlist, addToCart, toggleWishlist, isInWishlist } = useCommerce()
  const [query, setQuery] = useState('')
  const [panel, setPanel] = useState<Panel>(null)
  const [feedback, setFeedback] = useState('')
  const [aiMessage, setAiMessage] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const [products, setProducts] = useState<Product[]>([])
  const [productsLoading, setProductsLoading] = useState(true)
  const [productsError, setProductsError] = useState(false)
  const aiRequestId = useRef(0)

  useEffect(() => {
    const controller = new AbortController()
    void getProducts({ page: 0, size: 8, sort: 'rating', direction: 'desc' }, controller.signal)
      .then(({ products: loadedProducts }) => {
        setProducts(loadedProducts)
        setProductsError(false)
      })
      .catch((reason: unknown) => {
        if (reason instanceof DOMException && reason.name === 'AbortError') return
        setProductsError(true)
      })
      .finally(() => setProductsLoading(false))
    return () => controller.abort()
  }, [])

  const addProductToCart = async (product: Product) => {
    if (!product.id) return
    try {
      await addToCart(product.id)
      navigate('/cart')
      setFeedback(`${product.name} added to your bag`)
    } catch (reason) {
      setFeedback(reason instanceof Error ? reason.message : 'Please sign in to add products to your bag.')
    }
  }

  const handleToggleWishlist = async (product: Product) => {
    if (!product.id) return
    const wasSaved = isInWishlist(product.id)
    try {
      await toggleWishlist(product.id)
      setFeedback(wasSaved ? `${product.name} removed from wishlist` : `${product.name} saved to wishlist`)
    } catch (reason) {
      setFeedback(reason instanceof Error ? reason.message : 'Please sign in to save products.')
    }
  }

  const runSearch = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!query.trim()) {
      setPanel('assistant')
      setAiMessage('Tell me a category, budget, or feature and I will find a grounded shortlist.')
      return
    }
    setPanel('assistant')
    setAiLoading(true)
    setAiMessage('')
    setProducts([])
    const requestId = ++aiRequestId.current
    try {
      const response = await searchAi(query.trim())
      if (requestId !== aiRequestId.current) return
      setProducts(response.products.map(mapProduct))
      setAiMessage(`${response.explanation} ${response.products.length ? 'The product picks below now match your request.' : ''}`)
    } catch {
      setAiMessage('AI search is temporarily unavailable. You can still browse the real catalog.')
    } finally {
      setAiLoading(false)
    }
  }

  const runAssistant = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!query.trim()) return
    setPanel('assistant')
    setAiLoading(true)
    setAiMessage('')
    setProducts([])
    const requestId = ++aiRequestId.current
    try {
      const response = await askAiAssistant(query.trim())
      if (requestId !== aiRequestId.current) return
      if (response.products.length) {
        setAiMessage(response.answer)
        setProducts(response.products.map((item: ApiProduct) => mapProduct(item, 0)))
      } else {
        const grounded = await searchAi(query.trim())
        if (requestId !== aiRequestId.current) return
        setAiMessage(grounded.explanation)
        setProducts(grounded.products.map((item) => mapProduct(item, 0)))
      }
    } catch {
      try {
        const response = await searchAi(query.trim())
        if (requestId !== aiRequestId.current) return
        setProducts(response.products.map((item) => mapProduct(item, 0)))
        setAiMessage(`${response.explanation} I used the live catalog to keep your shortlist grounded.`)
      } catch {
        setAiMessage('The assistant is temporarily unavailable. Browse the real catalog while it reconnects.')
      }
    } finally {
      setAiLoading(false)
    }
  }

  const choosePrompt = (prompt: string) => {
    setQuery(prompt)
    setPanel('assistant')
    setAiLoading(true)
    setAiMessage('')
    setProducts([])
    const requestId = ++aiRequestId.current
    void searchAi(prompt).then((response) => {
      if (requestId !== aiRequestId.current) return
      setProducts(response.products.map(mapProduct))
      setAiMessage(response.explanation)
    }).catch(() => {
      if (requestId === aiRequestId.current) setAiMessage('AI search is temporarily unavailable.')
    }).finally(() => {
      if (requestId === aiRequestId.current) setAiLoading(false)
    })
  }

  return (
    <div className="app-shell">
      <div className="announcement">Free delivery on orders above ₹1,499 <span>·</span> Real catalog data, smarter decisions</div>
      <header className="nav-wrap">
        <Link className="brand" to="/" aria-label="ShopSense AI home"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link>
        <nav className="main-nav" aria-label="Main navigation"><a href="#discover">Discover</a><a href="#collections">Collections</a><a href="#how-it-works">How it works</a></nav>
        <div className="nav-actions">
          <button className="icon-button" aria-label="Search" onClick={() => document.getElementById('ai-search')?.focus()}>⌕</button>
          <Link className="icon-button" aria-label="Wishlist" to="/wishlist">♡<small>{wishlist?.itemCount ?? 0}</small></Link>
          <Link className="cart-button" aria-label="Shopping cart" to="/cart">Bag <b>{cart?.itemCount ?? 0}</b></Link>
          <Link className="avatar" aria-label="Account" to="/login">AR</Link>
        </div>
      </header>

      <main id="top">
        <section className="hero-section">
          <div className="hero-copy">
            <p className="eyebrow"><span className="eyebrow-dot" /> Curated by intelligence, chosen by you</p>
            <h1>Shopping,<br /><em>but smarter.</em></h1>
            <p className="hero-subtitle">Tell ShopSense what you need. Our AI finds the right fit from the real catalog.</p>
            <form className="search-box" onSubmit={runSearch}><span className="search-symbol">⌕</span><input id="ai-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Try wireless headphones under ₹5,000" aria-label="Describe what you are looking for" /><button type="submit">Ask AI <span>↗</span></button></form>
            <div className="prompt-row"><span>Try asking:</span><button type="button" onClick={() => choosePrompt('Wireless headphones under ₹5000')}>Wireless headphones under ₹5000</button><button type="button" onClick={() => choosePrompt('Running shoes for beginners')}>Running shoes for beginners</button><button type="button" onClick={() => choosePrompt('Travel essentials under ₹10000')}>Travel essentials under ₹10000</button></div>
          </div>
          <div className="hero-art" aria-label="Featured collection preview"><div className="art-card art-card-main"><img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=85" alt="Wireless headphones from the featured collection" /><span className="image-tag">REAL CATALOG / 01</span></div><div className="art-card art-card-float"><img src="https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=85" alt="Running shoe from the featured collection" /></div><div className="art-note"><strong>ShopSense AI</strong><span>Useful picks, grounded in what is available.</span></div></div>
        </section>

        <section className="trust-strip"><strong>AI-powered discovery</strong><strong>Real catalog data</strong><strong>Smarter shopping decisions</strong></section>

        <section className="content-section" id="discover">
          <div className="section-heading"><div><p className="eyebrow">A little more personal</p><h2>Picked for your <em>now.</em></h2></div><Link className="view-all-products-button" to="/shop">View all products <span>↗</span></Link></div>
          {productsLoading ? <div className="product-grid" aria-label="Loading products">{[1, 2, 3, 4].map((item) => <div className="product-card product-skeleton" key={item}><div className="product-image" /><div className="skeleton-line" /><div className="skeleton-line short" /></div>)}</div> : productsError ? <div className="catalog-state"><strong>Catalog unavailable</strong><p>We could not reach the live catalog.</p><button className="dark-button" onClick={() => window.location.reload()}>Retry <span>↗</span></button></div> : <><div className="product-status">{products.length} real products selected for you</div><div className="product-grid">{products.map((product) => { const saved = product.id ? isInWishlist(product.id) : false; return <article className={`product-card ${product.tone}`} key={product.id || product.name}><div className="product-image"><Link to={`/products/${product.slug}`}><img src={product.image} alt={product.name} loading="lazy" /></Link><button type="button" className={`heart-button ${saved ? 'liked' : ''}`} aria-label={`${saved ? 'Remove' : 'Save'} ${product.name} ${saved ? 'from' : 'to'} wishlist`} onClick={() => void handleToggleWishlist(product)}>{saved ? '♥' : '♡'}</button>{product.oldPrice && <span className="sale-label">Sale</span>}</div><div className="product-meta"><div><p className="product-category">{product.category}</p><Link className="product-name-button" to={`/products/${product.slug}`}><h3>{product.name}</h3></Link></div><span className="rating">★ {product.rating}</span></div><div className="product-price"><strong>{product.price}</strong>{product.oldPrice && <del>{product.oldPrice}</del>}<button type="button" className="dark-button add-to-bag" onClick={() => void addProductToCart(product)}>Add to bag</button></div></article>})}</div></>}
        </section>

        <section className="split-section" id="how-it-works"><div className="split-visual"><div className="mini-window"><div className="mini-header"><span className="mini-avatar">✦</span><span>ShopSense AI</span><span className="online-dot" /></div><div className="chat-bubble">Looking for something<br />for slow Sunday mornings.</div><div className="chat-bubble ai-bubble">I found real products<br />that fit your request.</div><div className="mini-products"><span>◌</span><span>▰</span><span>◒</span></div></div></div><div className="split-copy"><p className="eyebrow">A better way to browse</p><h2>Your taste,<br /><em>translated.</em></h2><p>Ask naturally, compare confidently, and move from a useful shortlist to a real product page and bag.</p><ol className="steps"><li><b>01</b><span><strong>Say what you mean</strong><small>Natural language that feels natural.</small></span></li><li><b>02</b><span><strong>We make sense of it</strong><small>Your intent becomes catalog criteria.</small></span></li><li><b>03</b><span><strong>You make the call</strong><small>Real products, prices, and stock.</small></span></li></ol><button className="dark-button" onClick={() => document.getElementById('ai-search')?.focus()}>Ask ShopSense AI <span>↗</span></button></div></section>
        <section className="category-section" id="collections"><div className="section-heading"><div><p className="eyebrow">Explore by feeling</p><h2>What are you <em>into?</em></h2></div></div><div className="category-grid"><Link to="/categories/electronics" className="category-card category-tech"><span>01</span><strong>Into focus</strong><small>Tools for your best work</small></Link><Link to="/categories/travel" className="category-card category-outdoor"><span>02</span><strong>Into the wild</strong><small>Go further, feel grounded</small></Link><Link to="/categories/furniture-decor" className="category-card category-home"><span>03</span><strong>Into slow living</strong><small>Make space for the good stuff</small></Link></div></section>
      </main>

      <footer className="footer"><div className="brand footer-brand"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></div><p>The thoughtful way to shop online.</p><div className="footer-links"><Link to="/shop">Shop</Link><a href="#how-it-works">Our story</a><a href="#collections">Collections</a></div><small>© 2026 ShopSense AI. Made for better choices.</small></footer>
      {feedback && <div className="feedback-toast" role="status"><span>✓</span>{feedback}<button type="button" onClick={() => setFeedback('')} aria-label="Dismiss notification">×</button></div>}

      {panel === 'assistant' && <aside className="assistant-panel" aria-live="polite"><div className="assistant-heading"><span className="assistant-icon">✦</span><div><strong>ShopSense AI</strong><small>Your personal shopper</small></div><button onClick={() => setPanel(null)} aria-label="Close assistant">×</button></div><div className="assistant-message">{aiLoading ? 'Understanding your request and finding real matches...' : aiMessage || 'Ask about products, budgets, alternatives, or categories.'}</div><div className="assistant-suggestions"><button onClick={() => choosePrompt('Comfortable running shoes under ₹5000')}>Comfortable running shoes</button><button onClick={() => choosePrompt('Travel essentials under ₹3000')}>Travel essentials</button></div><form className="assistant-input" onSubmit={runAssistant}><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Ask me anything..." aria-label="Chat with ShopSense AI" /><button type="submit" aria-label="Send message">↗</button></form></aside>}
    </div>
  )
}

export default App
