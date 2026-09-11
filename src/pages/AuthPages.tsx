import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login, register } from '../services/productService'

function AuthShell({ children, title, description }: { children: React.ReactNode; title: string; description: string }) {
  return <main className="route-page auth-page"><Link className="brand route-brand" to="/"><span className="brand-mark">S</span><span>ShopSense <i>AI</i></span></Link><div className="auth-panel"><p className="eyebrow">ShopSense AI</p><h1>{title}</h1><p className="route-description">{description}</p>{children}</div></main>
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      await login(email, password)
      navigate((location.state as { from?: string } | null)?.from || '/')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to sign in')
    } finally {
      setLoading(false)
    }
  }

  return <AuthShell title="Welcome back" description="Sign in to keep your bag, wishlist, and orders together."><form className="auth-form" onSubmit={submit}><label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" /></label><label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" /></label>{error && <p className="form-error" role="alert">{error}</p>}<button className="dark-button" type="submit" disabled={loading}>{loading ? 'Signing in...' : 'Sign in'} <span>↗</span></button><p className="auth-switch">New to ShopSense? <Link to="/register">Create an account</Link></p></form></AuthShell>
}

export function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const update = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }))

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      await register(form.firstName, form.lastName, form.email, form.password)
      navigate('/')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to create your account')
    } finally {
      setLoading(false)
    }
  }

  return <AuthShell title="Create your account" description="Save the products and ideas worth coming back to."><form className="auth-form" onSubmit={submit}><div className="auth-row"><label>First name<input value={form.firstName} onChange={(event) => update('firstName', event.target.value)} required autoComplete="given-name" /></label><label>Last name<input value={form.lastName} onChange={(event) => update('lastName', event.target.value)} required autoComplete="family-name" /></label></div><label>Email<input type="email" value={form.email} onChange={(event) => update('email', event.target.value)} required autoComplete="email" /></label><label>Password<input type="password" minLength={8} value={form.password} onChange={(event) => update('password', event.target.value)} required autoComplete="new-password" /></label>{error && <p className="form-error" role="alert">{error}</p>}<button className="dark-button" type="submit" disabled={loading}>{loading ? 'Creating account...' : 'Create account'} <span>↗</span></button><p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p></form></AuthShell>
}