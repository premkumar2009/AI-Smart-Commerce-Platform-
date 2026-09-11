import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { deleteAdminProduct, getAdminProducts, getAdminOrders, login, type ApiProduct, type Order } from '../services/productService'
import './admin.css'

export function AdminPage() {
  const [email, setEmail] = useState('admin@shopsense.ai')
  const [password, setPassword] = useState('AdminPass!23')
  const [authenticated, setAuthenticated] = useState(Boolean(localStorage.getItem('shopsense-admin-token')))
  const [products, setProducts] = useState<ApiProduct[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadDashboard = async () => {
    setLoading(true)
    setError('')
    try {
      const [catalog, orderList] = await Promise.all([getAdminProducts(), getAdminOrders()])
      setProducts(catalog.content)
      setOrders(orderList)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to load dashboard')
      setAuthenticated(false)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!authenticated) return
    const refresh = window.setTimeout(() => void loadDashboard(), 0)
    return () => window.clearTimeout(refresh)
  }, [authenticated])

  const signIn = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    try {
      const response = await login(email, password)
      if (response.role !== 'ADMIN') throw new Error('This account does not have admin access')
      localStorage.setItem('shopsense-admin-token', response.token)
      setAuthenticated(true)
    } catch (reason) {
      localStorage.removeItem('shopsense-admin-token')
      setError(reason instanceof Error ? reason.message : 'Unable to sign in')
    }
  }

  const removeProduct = async (product: ApiProduct) => {
    if (!window.confirm(`Delete ${product.name} from the catalog?`)) return
    try {
      await deleteAdminProduct(product.id)
      setProducts((current) => current.filter((item) => item.id !== product.id))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to delete product')
    }
  }

  if (!authenticated) return <main className="route-page admin-login"><Link className="brand route-brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link><div className="admin-login-card"><p className="eyebrow">Operations workspace</p><h1>Admin console.</h1><p className="route-description">Manage the live catalog and monitor orders from one quiet view.</p><form onSubmit={signIn}><label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></label><label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required /></label>{error && <p className="form-error">{error}</p>}<button className="dark-button" type="submit">Open dashboard <span>↗</span></button></form></div></main>

  const revenue = orders.reduce((sum, order) => sum + order.total, 0)
  const inventory = products.reduce((sum, product) => sum + product.stock, 0)
  return <main className="admin-shell"><header className="admin-header"><Link className="brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link><div><span className="admin-badge">ADMIN</span><Link className="text-link" to="/">Exit console</Link></div></header><div className="admin-heading"><div><p className="eyebrow">Live operations</p><h1>Commerce control room.</h1><p>Catalog health, order flow, and inventory at a glance.</p></div><button className="dark-button" onClick={() => void loadDashboard()} disabled={loading}>{loading ? 'Refreshing...' : 'Refresh data'} ↻</button></div>{error && <p className="form-error">{error}</p>}<section className="admin-stats"><div><span>Revenue</span><strong>₹{revenue.toLocaleString('en-IN')}</strong><small>Across {orders.length} orders</small></div><div><span>Orders</span><strong>{orders.length}</strong><small>Most recent first</small></div><div><span>Catalog</span><strong>{products.length}</strong><small>Products listed</small></div><div><span>Units available</span><strong>{inventory.toLocaleString('en-IN')}</strong><small>Current stock</small></div></section><div className="admin-grid"><section className="admin-panel"><div className="admin-panel-heading"><div><span className="eyebrow">Catalog</span><h2>Product inventory</h2></div><span>{products.length} items</span></div><div className="admin-table">{products.slice(0, 20).map((product) => <div className="admin-row" key={product.id}><img src={product.imageUrl} alt="" /><div><strong>{product.name}</strong><span>{product.brand} · {product.categoryName}</span></div><span className={product.stock === 0 ? 'stock-low' : ''}>{product.stock} in stock</span><button type="button" aria-label={`Delete ${product.name}`} onClick={() => void removeProduct(product)}>×</button></div>)}</div></section><section className="admin-panel"><div className="admin-panel-heading"><div><span className="eyebrow">Fulfilment</span><h2>Recent orders</h2></div><span>{orders.length} total</span></div><div className="admin-order-list">{orders.slice(0, 12).map((order) => <Link to={`/orders/${order.id}`} className="admin-order" key={order.id}><div><strong>#{order.id.slice(0, 8)}</strong><span>{order.itemCount} items · {new Date(order.createdAt).toLocaleDateString()}</span></div><div><strong>₹{order.total.toLocaleString('en-IN')}</strong><span className="status-pill">{order.status}</span></div></Link>)}</div></section></div></main>
}