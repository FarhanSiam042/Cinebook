import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import * as usersApi from '../api/users'
import { extractErrorMessage } from '../lib/api'
import { Badge, Button, Card, Input, SectionHeading } from '../components/ui'

export default function Profile() {
  const { user, setUser } = useAuth()
  const toast = useToast()

  const [profileForm, setProfileForm] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
  })
  const [profileError, setProfileError] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)

  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '' })
  const [pwError, setPwError] = useState('')
  const [savingPw, setSavingPw] = useState(false)

  async function saveProfile(e) {
    e.preventDefault()
    setProfileError('')
    setSavingProfile(true)
    try {
      const updated = await usersApi.updateMe(profileForm.fullName, profileForm.email)
      setUser(updated)
      toast.success('Profile updated.')
    } catch (err) {
      setProfileError(extractErrorMessage(err))
    } finally {
      setSavingProfile(false)
    }
  }

  async function savePassword(e) {
    e.preventDefault()
    setPwError('')
    setSavingPw(true)
    try {
      await usersApi.changePassword(pwForm.currentPassword, pwForm.newPassword)
      setPwForm({ currentPassword: '', newPassword: '' })
      toast.success('Password changed.')
    } catch (err) {
      setPwError(extractErrorMessage(err))
    } finally {
      setSavingPw(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <SectionHeading
        eyebrow="Account"
        title="Your profile"
        action={
          <div className="flex gap-1.5">
            {user?.roles?.map((r) => (
              <Badge key={r} tone={r === 'ADMIN' ? 'info' : 'default'}>
                {r}
              </Badge>
            ))}
          </div>
        }
      />

      <Card className="mb-6 p-6">
        <h2 className="mb-4 font-semibold text-white">Profile details</h2>
        <form onSubmit={saveProfile} className="flex flex-col gap-4">
          <Input label="Username" value={user?.username || ''} disabled className="opacity-60" />
          <Input
            label="Full name"
            required
            value={profileForm.fullName}
            onChange={(e) => setProfileForm((p) => ({ ...p, fullName: e.target.value }))}
          />
          <Input
            label="Email"
            type="email"
            required
            value={profileForm.email}
            onChange={(e) => setProfileForm((p) => ({ ...p, email: e.target.value }))}
          />
          {profileError && <p className="text-sm text-red-400">{profileError}</p>}
          <Button type="submit" disabled={savingProfile} className="self-start">
            {savingProfile ? 'Saving…' : 'Save changes'}
          </Button>
        </form>
      </Card>

      <Card className="p-6">
        <h2 className="mb-4 font-semibold text-white">Change password</h2>
        <form onSubmit={savePassword} className="flex flex-col gap-4">
          <Input
            label="Current password"
            type="password"
            required
            value={pwForm.currentPassword}
            onChange={(e) => setPwForm((p) => ({ ...p, currentPassword: e.target.value }))}
          />
          <Input
            label="New password"
            type="password"
            required
            value={pwForm.newPassword}
            onChange={(e) => setPwForm((p) => ({ ...p, newPassword: e.target.value }))}
          />
          {pwError && <p className="text-sm text-red-400">{pwError}</p>}
          <Button type="submit" disabled={savingPw} className="self-start">
            {savingPw ? 'Updating…' : 'Update password'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
