import { useState, useEffect, useCallback } from 'react'
import { isRealImg } from './ui.jsx'
import { photo } from '../lib/photo.js'

// Galerie plein écran. images = [{ url, seed }]. start = index initial.
export default function Lightbox({ images = [], start = 0, onClose }) {
  const [i, setI] = useState(start)
  const n = images.length
  const prev = useCallback(() => setI((x) => (x - 1 + n) % n), [n])
  const next = useCallback(() => setI((x) => (x + 1) % n), [n])

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'Escape') onClose()
      if (e.key === 'ArrowLeft') prev()
      if (e.key === 'ArrowRight') next()
    }
    window.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
    return () => { window.removeEventListener('keydown', onKey); document.body.style.overflow = '' }
  }, [onClose, prev, next])

  if (!n) return null
  const cur = images[i]
  const bg = isRealImg(cur.url)
    ? { backgroundImage: `url(${cur.url})` }
    : { backgroundImage: photo(cur.seed) }

  return (
    <div className="lightbox" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <button className="lb-close" onClick={onClose} aria-label="Fermer">×</button>
      {n > 1 && <button className="lb-arrow lb-prev" onClick={prev} aria-label="Précédent">‹</button>}
      <div className="lb-stage" style={{ ...bg, backgroundSize: 'contain', backgroundPosition: 'center', backgroundRepeat: 'no-repeat' }} />
      {n > 1 && <button className="lb-arrow lb-next" onClick={next} aria-label="Suivant">›</button>}
      <div className="lb-counter">{i + 1} / {n}</div>
    </div>
  )
}
