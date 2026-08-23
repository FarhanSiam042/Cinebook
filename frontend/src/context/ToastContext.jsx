import { createContext, useCallback, useContext, useRef, useState } from 'react'

const ToastContext = createContext(null)

let idCounter = 0

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const timers = useRef({})

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
    clearTimeout(timers.current[id])
    delete timers.current[id]
  }, [])

  const push = useCallback(
    (message, type = 'info', duration = 4500) => {
      const id = ++idCounter
      setToasts((prev) => [...prev, { id, message, type }])
      timers.current[id] = setTimeout(() => dismiss(id), duration)
      return id
    },
    [dismiss]
  )

  const value = {
    success: (msg) => push(msg, 'success'),
    error: (msg) => push(msg, 'error'),
    info: (msg) => push(msg, 'info'),
    dismiss,
  }

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex w-full max-w-sm flex-col gap-2">
        {toasts.map((t) => (
          <div
            key={t.id}
            onClick={() => dismiss(t.id)}
            className={`cursor-pointer rounded-lg border px-4 py-3 text-sm shadow-lg backdrop-blur transition-all animate-in
              ${
                t.type === 'success'
                  ? 'border-emerald-400/30 bg-emerald-950/90 text-emerald-100'
                  : t.type === 'error'
                    ? 'border-red-400/30 bg-red-950/90 text-red-100'
                    : 'border-slate-500/30 bg-slate-900/90 text-slate-100'
              }`}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within a ToastProvider')
  return ctx
}
