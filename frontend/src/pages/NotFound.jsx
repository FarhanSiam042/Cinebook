import { Link } from 'react-router-dom'
import { Button } from '../components/ui'

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center gap-4 py-24 text-center">
      <p className="text-5xl">🎬</p>
      <h1 className="text-2xl font-bold text-white">Page not found</h1>
      <p className="text-slate-500">The page you're looking for doesn't exist.</p>
      <Button as={Link} to="/">
        Back to movies
      </Button>
    </div>
  )
}
