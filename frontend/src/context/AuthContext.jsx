import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react'
import * as authApi from '../api/auth'
import * as usersApi from '../api/users'
import { setUnauthorizedHandler } from '../lib/api'

const AuthContext = createContext(null)

const TOKEN_KEY = 'cinebook_token'
const USER_KEY = 'cinebook_user'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  })
  const [ready, setReady] = useState(false)

  const persist = useCallback((nextToken, nextUser) => {
    if (nextToken) localStorage.setItem(TOKEN_KEY, nextToken)
    else localStorage.removeItem(TOKEN_KEY)
    if (nextUser) localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
    else localStorage.removeItem(USER_KEY)
    setToken(nextToken)
    setUser(nextUser)
  }, [])

  const logout = useCallback(() => {
    persist(null, null)
  }, [persist])

  useEffect(() => {
    setUnauthorizedHandler(() => logout())
  }, [logout])

  // Refresh profile once on load if we have a token, to catch role/profile drift.
  useEffect(() => {
    let cancelled = false
    async function bootstrap() {
      if (token) {
        try {
          const me = await usersApi.getMe()
          if (!cancelled) persist(token, me)
        } catch {
          if (!cancelled) persist(null, null)
        }
      }
      if (!cancelled) setReady(true)
    }
    bootstrap()
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(
    async (username, password) => {
      const res = await authApi.login(username, password)
      persist(res.token, res.user)
      return res.user
    },
    [persist]
  )

  const register = useCallback(
    async (fullName, email, username, password) => {
      const res = await authApi.register(fullName, email, username, password)
      persist(res.token, res.user)
      return res.user
    },
    [persist]
  )

  const value = useMemo(
    () => ({
      token,
      user,
      ready,
      isAuthenticated: !!token,
      isAdmin: !!user?.roles?.includes('ADMIN'),
      login,
      register,
      logout,
      setUser: (u) => persist(token, u),
    }),
    [token, user, ready, login, register, logout, persist]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
