import { useEffect, useState } from 'react'
import * as theatersApi from '../../api/theaters'
import { extractErrorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Modal,
  PageLoader,
  SectionHeading,
  Select,
} from '../../components/ui'

const emptyTheaterForm = { name: '', city: '', address: '', active: true }
const emptyCategoryForm = { name: '', code: '', priceMultiplier: '1.0', active: true }
const emptyScreenForm = { name: '', totalRows: '', seatsPerRow: '', active: true }

export default function AdminTheaters() {
  const [tab, setTab] = useState('theaters')

  return (
    <div>
      <div className="mb-6 flex gap-1 rounded-lg bg-slate-900 p-1 w-fit">
        {['theaters', 'categories'].map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
              tab === t ? 'bg-indigo-500 text-white' : 'text-slate-400 hover:text-white'
            }`}
          >
            {t === 'theaters' ? 'Theaters & screens' : 'Seat categories'}
          </button>
        ))}
      </div>
      {tab === 'theaters' ? <TheatersPanel /> : <SeatCategoriesPanel />}
    </div>
  )
}

function TheatersPanel() {
  const toast = useToast()
  const [theaters, setTheaters] = useState(null)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyTheaterForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [managing, setManaging] = useState(null)

  async function load() {
    setError('')
    try {
      setTheaters(await theatersApi.listTheaters())
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  function openNew() {
    setForm(emptyTheaterForm)
    setFormError('')
    setEditing('new')
  }

  function openEdit(t) {
    setForm({ name: t.name, city: t.city, address: t.address, active: t.active })
    setFormError('')
    setEditing(t)
  }

  async function submit(e) {
    e.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      if (editing === 'new') {
        await theatersApi.createTheater(form)
        toast.success('Theater created.')
      } else {
        await theatersApi.updateTheater(editing.id, form)
        toast.success('Theater updated.')
      }
      setEditing(null)
      load()
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(t) {
    if (!confirm(`Deactivate "${t.name}"?`)) return
    try {
      await theatersApi.deleteTheater(t.id)
      toast.success('Theater deactivated.')
      load()
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!theaters) return <PageLoader label="Loading theaters…" />

  return (
    <div>
      <SectionHeading
        title="Theaters"
        description="Manage theater locations and their screens."
        action={<Button onClick={openNew}>+ New theater</Button>}
      />
      {theaters.length === 0 && <EmptyState title="No theaters yet" />}
      <div className="flex flex-col gap-3">
        {theaters.map((t) => (
          <Card key={t.id} className="flex flex-wrap items-center justify-between gap-3 p-4">
            <div>
              <p className="flex items-center gap-2 font-semibold text-white">
                {t.name}
                {!t.active && <Badge tone="danger">Inactive</Badge>}
              </p>
              <p className="text-sm text-slate-400">
                {t.city} · {t.address}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button size="sm" variant="outline" onClick={() => setManaging(t)}>
                Manage screens
              </Button>
              <Button size="sm" variant="secondary" onClick={() => openEdit(t)}>
                Edit
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleDelete(t)}>
                Deactivate
              </Button>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        open={!!editing}
        onClose={() => setEditing(null)}
        title={editing === 'new' ? 'New theater' : `Edit "${editing?.name}"`}
      >
        <form onSubmit={submit} className="flex flex-col gap-4">
          <Input
            label="Name"
            required
            value={form.name}
            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
          />
          <Input
            label="City"
            required
            value={form.city}
            onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))}
          />
          <Input
            label="Address"
            required
            value={form.address}
            onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))}
          />
          <label className="flex items-center gap-2 text-sm text-slate-300">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Active
          </label>
          {formError && <p className="text-sm text-red-400">{formError}</p>}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </Button>
          </div>
        </form>
      </Modal>

      {managing && <ManageScreens theater={managing} onClose={() => setManaging(null)} />}
    </div>
  )
}

function ManageScreens({ theater, onClose }) {
  const toast = useToast()
  const [screens, setScreens] = useState(null)
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(emptyScreenForm)
  const [editingScreen, setEditingScreen] = useState(null)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [seatsFor, setSeatsFor] = useState(null)
  const [seats, setSeats] = useState([])
  const [genCategory, setGenCategory] = useState('')

  async function load() {
    try {
      setScreens(await theatersApi.listScreens(theater.id))
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
    theatersApi.listSeatCategories().then(setCategories).catch(() => setCategories([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function openNew() {
    setForm(emptyScreenForm)
    setFormError('')
    setEditingScreen('new')
  }

  function openEdit(s) {
    setForm({ name: s.name, totalRows: s.totalRows, seatsPerRow: s.seatsPerRow, active: s.active })
    setFormError('')
    setEditingScreen(s)
  }

  async function submitScreen(e) {
    e.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        totalRows: Number(form.totalRows),
        seatsPerRow: Number(form.seatsPerRow),
        active: form.active,
      }
      if (editingScreen === 'new') {
        await theatersApi.createScreen(theater.id, payload)
        toast.success('Screen created.')
      } else {
        await theatersApi.updateScreen(editingScreen.id, payload)
        toast.success('Screen updated.')
      }
      setEditingScreen(null)
      load()
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDeleteScreen(s) {
    if (!confirm(`Deactivate screen "${s.name}"?`)) return
    try {
      await theatersApi.deleteScreen(s.id)
      toast.success('Screen deactivated.')
      load()
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  async function openSeats(s) {
    setSeatsFor(s)
    try {
      setSeats(await theatersApi.listSeats(s.id))
    } catch {
      setSeats([])
    }
  }

  async function generate(s) {
    if (!genCategory) {
      toast.error('Choose a seat category first.')
      return
    }
    try {
      await theatersApi.generateSeats(s.id, genCategory)
      toast.success(`Seats generated for ${s.name}.`)
      if (seatsFor?.id === s.id) openSeats(s)
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  return (
    <Modal open onClose={onClose} title={`Screens — ${theater.name}`} wide>
      <div className="flex justify-end">
        <Button size="sm" onClick={openNew}>
          + New screen
        </Button>
      </div>

      {!screens ? (
        <PageLoader label="Loading screens…" />
      ) : screens.length === 0 ? (
        <p className="mt-4 text-sm text-slate-500">No screens yet.</p>
      ) : (
        <div className="mt-4 flex flex-col gap-3">
          {screens.map((s) => (
            <Card key={s.id} className="p-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="flex items-center gap-2 font-medium text-white">
                    {s.name}
                    {!s.active && <Badge tone="danger">Inactive</Badge>}
                  </p>
                  <p className="text-xs text-slate-500">
                    {s.totalRows} rows × {s.seatsPerRow} seats/row
                  </p>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <Select
                    className="!py-1 text-xs"
                    value={genCategory}
                    onChange={(e) => setGenCategory(e.target.value)}
                  >
                    <option value="">Category…</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.code}>
                        {c.name}
                      </option>
                    ))}
                  </Select>
                  <Button size="sm" variant="outline" onClick={() => generate(s)}>
                    Generate seats
                  </Button>
                  <Button size="sm" variant="ghost" onClick={() => openSeats(s)}>
                    {seatsFor?.id === s.id ? 'Hide seats' : 'View seats'}
                  </Button>
                  <Button size="sm" variant="secondary" onClick={() => openEdit(s)}>
                    Edit
                  </Button>
                  <Button size="sm" variant="danger" onClick={() => handleDeleteScreen(s)}>
                    Deactivate
                  </Button>
                </div>
              </div>
              {seatsFor?.id === s.id && (
                <div className="mt-3 border-t border-slate-800 pt-3">
                  {seats.length === 0 ? (
                    <p className="text-xs text-slate-500">
                      No seats generated yet — use "Generate seats" above.
                    </p>
                  ) : (
                    <div className="flex flex-wrap gap-1">
                      {seats.map((seat) => (
                        <span
                          key={seat.id}
                          title={`${seat.categoryName} · ×${seat.priceMultiplier}`}
                          className="rounded border border-slate-800 bg-slate-950 px-1.5 py-0.5 text-[11px] text-slate-400"
                        >
                          {seat.rowLabel}
                          {seat.seatNumber}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={!!editingScreen}
        onClose={() => setEditingScreen(null)}
        title={editingScreen === 'new' ? 'New screen' : `Edit "${editingScreen?.name}"`}
      >
        <form onSubmit={submitScreen} className="flex flex-col gap-4">
          <Input
            label="Name"
            required
            value={form.name}
            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
          />
          <Input
            label="Total rows (1-26)"
            type="number"
            min={1}
            max={26}
            required
            value={form.totalRows}
            onChange={(e) => setForm((f) => ({ ...f, totalRows: e.target.value }))}
          />
          <Input
            label="Seats per row (1-60)"
            type="number"
            min={1}
            max={60}
            required
            value={form.seatsPerRow}
            onChange={(e) => setForm((f) => ({ ...f, seatsPerRow: e.target.value }))}
          />
          <label className="flex items-center gap-2 text-sm text-slate-300">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Active
          </label>
          {formError && <p className="text-sm text-red-400">{formError}</p>}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setEditingScreen(null)}>
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </Button>
          </div>
        </form>
      </Modal>
    </Modal>
  )
}

function SeatCategoriesPanel() {
  const toast = useToast()
  const [categories, setCategories] = useState(null)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyCategoryForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    setError('')
    try {
      setCategories(await theatersApi.listSeatCategories())
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  function openNew() {
    setForm(emptyCategoryForm)
    setFormError('')
    setEditing('new')
  }

  function openEdit(c) {
    setForm({ name: c.name, code: c.code, priceMultiplier: c.priceMultiplier, active: c.active })
    setFormError('')
    setEditing(c)
  }

  async function submit(e) {
    e.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        code: form.code,
        priceMultiplier: Number(form.priceMultiplier),
        active: form.active,
      }
      if (editing === 'new') {
        await theatersApi.createSeatCategory(payload)
        toast.success('Seat category created.')
      } else {
        await theatersApi.updateSeatCategory(editing.id, payload)
        toast.success('Seat category updated.')
      }
      setEditing(null)
      load()
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(c) {
    if (!confirm(`Deactivate seat category "${c.name}"?`)) return
    try {
      await theatersApi.deleteSeatCategory(c.id)
      toast.success('Seat category deactivated.')
      load()
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!categories) return <PageLoader label="Loading seat categories…" />

  return (
    <div>
      <SectionHeading
        title="Seat categories"
        description="Pricing tiers used when generating a screen's seat map (e.g. Standard, Premium, VIP)."
        action={<Button onClick={openNew}>+ New category</Button>}
      />
      {categories.length === 0 && <EmptyState title="No seat categories yet" />}
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {categories.map((c) => (
          <Card key={c.id} className="p-4">
            <p className="flex items-center gap-2 font-semibold text-white">
              {c.name}
              {!c.active && <Badge tone="danger">Inactive</Badge>}
            </p>
            <p className="text-sm text-slate-400">
              Code: {c.code} · ×{c.priceMultiplier}
            </p>
            <div className="mt-3 flex gap-2">
              <Button size="sm" variant="secondary" onClick={() => openEdit(c)}>
                Edit
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleDelete(c)}>
                Deactivate
              </Button>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        open={!!editing}
        onClose={() => setEditing(null)}
        title={editing === 'new' ? 'New seat category' : `Edit "${editing?.name}"`}
      >
        <form onSubmit={submit} className="flex flex-col gap-4">
          <Input
            label="Name"
            required
            value={form.name}
            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
          />
          <Input
            label="Code"
            required
            value={form.code}
            onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))}
          />
          <Input
            label="Price multiplier"
            type="number"
            step="0.01"
            min={0.01}
            required
            value={form.priceMultiplier}
            onChange={(e) => setForm((f) => ({ ...f, priceMultiplier: e.target.value }))}
          />
          <label className="flex items-center gap-2 text-sm text-slate-300">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Active
          </label>
          {formError && <p className="text-sm text-red-400">{formError}</p>}
          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
