// Small shared UI primitives used across the app. Kept in one file to avoid
// import sprawl given the number of pages in this project.

export function Button({
  as: Tag = 'button',
  variant = 'primary',
  size = 'md',
  className = '',
  ...props
}) {
  const base =
    'inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950 focus-visible:ring-indigo-400'
  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-5 py-2.5 text-base',
  }
  const variants = {
    primary: 'bg-indigo-500 text-white hover:bg-indigo-400',
    secondary: 'bg-slate-800 text-slate-100 hover:bg-slate-700 border border-slate-700',
    danger: 'bg-red-600 text-white hover:bg-red-500',
    ghost: 'text-slate-300 hover:bg-slate-800 hover:text-white',
    outline: 'border border-slate-700 text-slate-200 hover:border-indigo-400 hover:text-white',
  }
  return (
    <Tag className={`${base} ${sizes[size]} ${variants[variant]} ${className}`} {...props} />
  )
}

export function Card({ className = '', ...props }) {
  return (
    <div
      className={`rounded-xl border border-slate-800 bg-slate-900/60 shadow-sm ${className}`}
      {...props}
    />
  )
}

export function Badge({ tone = 'default', className = '', ...props }) {
  const tones = {
    default: 'bg-slate-800 text-slate-300',
    success: 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30',
    warning: 'bg-amber-500/15 text-amber-300 border border-amber-500/30',
    danger: 'bg-red-500/15 text-red-300 border border-red-500/30',
    info: 'bg-indigo-500/15 text-indigo-300 border border-indigo-500/30',
  }
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${tones[tone]} ${className}`}
      {...props}
    />
  )
}

export function Spinner({ className = '' }) {
  return (
    <svg
      className={`animate-spin ${className}`}
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
      />
    </svg>
  )
}

export function PageLoader({ label = 'Loading…' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-24 text-slate-400">
      <Spinner className="h-8 w-8" />
      <p className="text-sm">{label}</p>
    </div>
  )
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-red-500/20 bg-red-500/5 py-16 text-center">
      <p className="text-red-300">{message || 'Something went wrong.'}</p>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-800 py-16 text-center">
      <p className="font-medium text-slate-200">{title}</p>
      {description && <p className="max-w-sm text-sm text-slate-500">{description}</p>}
      {action}
    </div>
  )
}

export function Input({ label, error, className = '', id, ...props }) {
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      {label && <span className="font-medium text-slate-300">{label}</span>}
      <input
        id={id}
        className={`rounded-lg border bg-slate-950 px-3 py-2 text-slate-100 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-400/60 ${
          error ? 'border-red-500/60' : 'border-slate-700'
        } ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-red-400">{error}</span>}
    </label>
  )
}

export function Textarea({ label, error, className = '', ...props }) {
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      {label && <span className="font-medium text-slate-300">{label}</span>}
      <textarea
        className={`rounded-lg border bg-slate-950 px-3 py-2 text-slate-100 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-400/60 ${
          error ? 'border-red-500/60' : 'border-slate-700'
        } ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-red-400">{error}</span>}
    </label>
  )
}

export function Select({ label, error, className = '', children, ...props }) {
  return (
    <label className="flex flex-col gap-1.5 text-sm">
      {label && <span className="font-medium text-slate-300">{label}</span>}
      <select
        className={`rounded-lg border bg-slate-950 px-3 py-2 text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-400/60 ${
          error ? 'border-red-500/60' : 'border-slate-700'
        } ${className}`}
        {...props}
      >
        {children}
      </select>
    </label>
  )
}

export function Modal({ open, onClose, title, children, footer, wide = false }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-950/80 backdrop-blur-sm" onClick={onClose} />
      <div
        className={`relative max-h-[90vh] w-full ${wide ? 'max-w-2xl' : 'max-w-md'} overflow-y-auto rounded-xl border border-slate-800 bg-slate-900 p-6 shadow-2xl`}
      >
        <div className="mb-4 flex items-start justify-between gap-4">
          <h3 className="text-lg font-semibold text-white">{title}</h3>
          <button
            onClick={onClose}
            className="rounded-md p-1 text-slate-400 hover:bg-slate-800 hover:text-white"
            aria-label="Close"
          >
            ✕
          </button>
        </div>
        <div>{children}</div>
        {footer && <div className="mt-6 flex justify-end gap-2">{footer}</div>}
      </div>
    </div>
  )
}

export function SectionHeading({ eyebrow, title, description, action }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        {eyebrow && (
          <p className="mb-1 text-xs font-semibold uppercase tracking-wider text-indigo-400">
            {eyebrow}
          </p>
        )}
        <h1 className="text-2xl font-bold text-white sm:text-3xl">{title}</h1>
        {description && <p className="mt-1 max-w-2xl text-sm text-slate-400">{description}</p>}
      </div>
      {action}
    </div>
  )
}
