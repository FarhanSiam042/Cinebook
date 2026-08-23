import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { extractErrorMessage } from '../lib/api'
import { Button, Card, Input } from '../components/ui'

export default function Register() {
  const { register } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const [form, setForm] = useState({ fullName: '', email: '', username: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function update(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const user = await register(form.fullName, form.email, form.username, form.password)
      toast.success(`Account created — welcome, ${user.fullName}!`)
      navigate('/', { replace: true })
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 py-8">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-white">Create an account</h1>
        <p className="mt-1 text-sm text-slate-400">
          Already have one?{' '}
          <Link to="/login" className="text-indigo-400 hover:underline">
            Log in
          </Link>
        </p>
      </div>

      <Card className="p-6">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input label="Full name" required value={form.fullName} onChange={update('fullName')} />
          <Input
            label="Email"
            type="email"
            required
            value={form.email}
            onChange={update('email')}
          />
          <Input label="Username" required value={form.username} onChange={update('username')} />
          <Input
            label="Password"
            type="password"
            required
            value={form.password}
            onChange={update('password')}
          />
          {error && <p className="text-sm text-red-400">{error}</p>}
          <Button type="submit" disabled={submitting} className="mt-1">
            {submitting ? 'Creating account…' : 'Sign up'}
          </Button>
          <p className="text-center text-xs text-slate-500">
            New accounts are created as regular customers. Admin access is granted separately.
          </p>
        </form>
      </Card>
    </div>
  )
}
