import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { askAiAssistant, getAiBudget, mapProduct, searchAi, type Product } from '../services/productService'
import { useCommerce } from '../context/CommerceContext'

function AiLayout({ eyebrow, title, description, children }: { eyebrow: string; title: string; description: string; children: React.ReactNode }) {
  return <main className="route-page ai-page"><Link className="brand route-brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="route-description">{description}</p>{children}</main>
}

function AiProductCard({ product }: { product: Product }) {
  const navigate = useNavigate()
  const { addToCart, toggleWishlist, isInWishlist } = useCommerce()
  const [message, setMessage] = useState('')
  const saved = Boolean(product.id && isInWishlist(product.id))

  const addToCompare = () => {
    if (!product.id) return
    const selected = JSON.parse(localStorage.getItem('shopsense-compare') || '[]') as string[]
    const next = selected.includes(product.id) ? selected : selected.length < 3 ? [...selected, product.id] : selected
    localStorage.setItem('shopsense-compare', JSON.stringify(next))
    navigate(`/compare?ids=${next.join(',')}`)
  }

  return <article className="product-card" key={product.id}><Link className="product-card-link" to={`/products/${product.slug}`}><div className="product-image"><img src={product.image} alt={product.name} /></div><div className="product-meta"><div><p className="product-category">{product.category}</p><h3>{product.name}</h3></div><span className="rating">★ {product.rating}</span></div><div className="product-price"><strong>{product.price}</strong></div></Link>{product.id && <div className="ai-product-actions"><button type="button" className="dark-button add-to-bag" onClick={() => void addToCart(product.id!).then(() => setMessage('Added to bag')).catch(() => setMessage('Unable to add to bag'))}>Add to bag</button><button type="button" aria-label={`${saved ? 'Remove' : 'Save'} ${product.name} ${saved ? 'from' : 'to'} wishlist`} onClick={() => void toggleWishlist(product.id!).then(() => setMessage(saved ? 'Removed from wishlist' : 'Saved to wishlist')).catch(() => setMessage('Unable to update wishlist'))}>{saved ? '♥' : '♡'}</button><button type="button" onClick={addToCompare}>Compare</button></div>}{message && <small role="status">{message}</small>}</article>
}

export function AssistantPage() {
  const [query, setQuery] = useState('')
  const [answer, setAnswer] = useState('')
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!query.trim()) return
    setLoading(true)
    setError('')
    try {
      const response = await askAiAssistant(query.trim())
      if (response.products.length) {
        setAnswer(response.answer)
        setProducts(response.products.map(mapProduct))
      } else {
        const grounded = await searchAi(query.trim())
        setAnswer(grounded.explanation)
        setProducts(grounded.products.map(mapProduct))
      }
    } catch {
      try {
        const response = await searchAi(query.trim())
        setAnswer(`${response.explanation} I used the live catalog to keep these suggestions grounded.`)
        setProducts(response.products.map(mapProduct))
      } catch {
        setError('The assistant is temporarily unavailable. You can still browse the live catalog.')
      }
    } finally {
      setLoading(false)
    }
  }

  return <AiLayout eyebrow="Your shopping sidekick" title="Ask ShopSense AI." description="Ask about products, alternatives, budgets, or the right category. Answers stay grounded in the live catalog."><form className="ai-query-form" onSubmit={submit}><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="What are you looking for?" aria-label="Ask ShopSense AI" /><button className="dark-button" type="submit" disabled={loading}>{loading ? 'Thinking...' : 'Ask assistant'} <span>↗</span></button></form>{error && <p className="form-error" role="alert">{error}</p>}{answer && <section className="ai-answer"><p className="eyebrow">ShopSense AI</p><p>{answer}</p></section>}{products.length > 0 && <section><h2>Products mentioned</h2><div className="product-grid">{products.map((product) => <AiProductCard product={product} key={product.id} />)}</div></section>}</AiLayout>
}

export function BudgetBuilderPage() {
  const [query, setQuery] = useState('Travel essentials under ₹10000')
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [budget, setBudget] = useState<number | undefined>()
  const [explanation, setExplanation] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const { addToCart } = useCommerce()

  const build = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setMessage('')
    try {
      const response = await getAiBudget(query.trim(), 6)
      setProducts(response.products.map(mapProduct))
      setTotal(response.total)
      setBudget(response.budget)
      setExplanation(response.explanation)
    } catch {
      setMessage('Budget builder is temporarily unavailable.')
    } finally {
      setLoading(false)
    }
  }

  const addAll = async () => {
    try {
      await Promise.all(products.filter((product) => product.id).map((product) => addToCart(product.id!, 1)))
      setMessage('The bundle was added to your bag.')
    } catch {
      setMessage('Some products could not be added. Open your bag to review it.')
    }
  }

  return <AiLayout eyebrow="Build a considered bundle" title="Shop by budget." description="Describe what you need and ShopSense will assemble a real catalog bundle within your budget."><form className="budget-form" onSubmit={build}><label>What do you need?<input value={query} onChange={(event) => setQuery(event.target.value)} required /></label><button className="dark-button" type="submit" disabled={loading}>{loading ? 'Building...' : 'Build with AI'} <span>↗</span></button></form>{explanation && <p className="route-description">{explanation}</p>}{products.length > 0 && <section className="budget-result"><div className="budget-total"><span>Bundle total</span><strong>₹{total.toLocaleString('en-IN')}</strong>{budget !== undefined && <small>₹{Math.max(0, budget - total).toLocaleString('en-IN')} remaining</small>}</div><div className="product-grid">{products.map((product) => <AiProductCard product={product} key={product.id} />)}</div><button className="dark-button" onClick={() => void addAll()}>Add all to bag <span>↗</span></button></section>}{message && <p className="route-description" role="status">{message}</p>}</AiLayout>
}