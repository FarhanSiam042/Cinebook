import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { extractErrorMessage } from '../lib/api'
import { Button, Card, Input } from '../components/ui'

export default function Login() {
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const from = location.state?.from?.pathname || '/'

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const user = await login(username, password)
      toast.success(`Welcome back, ${user.fullName || user.username}!`)
      navigate(from, { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  function fillAdmin() {
    setUsername('admin')
    setPassword('Admin123!')
  }

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 py-8">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-white">Log in</h1>
        <p className="mt-1 text-sm text-slate-400">
          New here?{' '}
          <Link to="/register" className="text-indigo-400 hover:underline">
            Create an account
          </Link>
        </p>
      </div>

      <Card className="p-6">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input
            label="Username"
            required
            autoFocus
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <Input
            label="Password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {error && <p className="text-sm text-red-400">{error}</p>}
          <Button type="submit" disabled={submitting} className="mt-1">
            {submitting ? 'Logging in…' : 'Log in'}
          </Button>
        </form>
      </Card>

      <Card className="border-indigo-500/20 bg-indigo-500/5 p-4 text-sm text-slate-300">
        <p className="font-medium text-indigo-300">Testing as an admin?</p>
        <p className="mt-1 text-slate-400">
          This project seeds a default administrator account: <code className="rounded bg-slate-900 px-1.5 py-0.5">admin</code> /{' '}
          <code className="rounded bg-slate-900 px-1.5 py-0.5">Admin123!</code>
        </p>
        <button
          type="button"
          onClick={fillAdmin}
          className="mt-2 text-indigo-400 underline underline-offset-2 hover:text-indigo-300"
        >
          Fill in admin credentials
        </button>
      </Card>
    </div>
  )
}
