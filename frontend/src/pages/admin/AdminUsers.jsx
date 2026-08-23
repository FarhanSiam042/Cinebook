import { useEffect, useState } from 'react'
import * as usersApi from '../../api/users'
import { extractErrorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import {
  Badge,
  Button,
  Card,
  ErrorState,
  Input,
  Modal,
  PageLoader,
  SectionHeading,
} from '../../components/ui'

const emptyForm = {
  fullName: '',
  email: '',
  username: '',
  password: '',
  roles: { CUSTOMER: true, ADMIN: false },
}

export default function AdminUsers() {
  const toast = useToast()
  const [users, setUsers] = useState(null)
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    setError('')
    try {
      setUsers(await usersApi.listUsers())
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  function toggleRole(role) {
    setForm((f) => ({ ...f, roles: { ...f.roles, [role]: !f.roles[role] } }))
  }

  async function submit(e) {
    e.preventDefault()
    setFormError('')
    const roles = Object.entries(form.roles)
      .filter(([, v]) => v)
      .map(([k]) => k)
    if (roles.length === 0) {
      setFormError('Select at least one role.')
      return
    }
    setSaving(true)
    try {
      await usersApi.adminCreateUser({
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password,
        roles,
      })
      toast.success('User created.')
      setCreating(false)
      setForm(emptyForm)
      load()
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!users) return <PageLoader label="Loading users…" />

  return (
    <div>
      <SectionHeading
        title="Users"
        description="Everyone registered in user-service, with their roles."
        action={<Button onClick={() => setCreating(true)}>+ New user</Button>}
      />

      <Card className="overflow-x-auto">
        <table className="w-full min-w-[560px] text-left text-sm">
          <thead className="border-b border-slate-800 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Username</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Roles</th>
              <th className="px-4 py-3">Status</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-slate-900 last:border-0">
                <td className="px-4 py-3 text-white">{u.fullName}</td>
                <td className="px-4 py-3 text-slate-400">{u.username}</td>
                <td className="px-4 py-3 text-slate-400">{u.email}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-1">
                    {u.roles?.map((r) => (
                      <Badge key={r} tone={r === 'ADMIN' ? 'info' : 'default'}>
                        {r}
                      </Badge>
                    ))}
                  </div>
                </td>
                <td className="px-4 py-3">
                  <Badge tone={u.enabled ? 'success' : 'danger'}>
                    {u.enabled ? 'Enabled' : 'Disabled'}
                  </Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>

      <Modal open={creating} onClose={() => setCreating(false)} title="New user">
        <form onSubmit={submit} className="flex flex-col gap-4">
          <Input
            label="Full name"
            required
            value={form.fullName}
            onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
          />
          <Input
            label="Email"
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
          />
          <Input
            label="Username"
            required
            value={form.username}
            onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
          />
          <Input
            label="Password"
            type="password"
            required
            value={form.password}
            onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
          />
          <div className="flex flex-col gap-1.5 text-sm">
            <span className="font-medium text-slate-300">Roles</span>
            <div className="flex gap-4">
              {['CUSTOMER', 'ADMIN'].map((role) => (
                <label key={role} className="flex items-center gap-2 text-slate-300">
                  <input
                    type="checkbox"
                    checked={form.roles[role]}
                    onChange={() => toggleRole(role)}
                  />
                  {role}
                </label>
              ))}
            </div>
          </div>
          {formError && <p className="text-sm text-red-400">{formError}</p>}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setCreating(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Creating…' : 'Create user'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
