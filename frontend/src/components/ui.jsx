import { useEffect } from 'react'
import { photoStyle, avatarStyle } from '../lib/photo.js'

// Une URL est utilisable si c'est une vraie image distante (https).
// Les URLs de test back (ex. "http://img/p.jpg") retombent sur le placeholder.
export const isRealImg = (url) => typeof url === 'string' && /^https:\/\//.test(url)

// Fond photo : vraie image si dispo, sinon dégradé placeholder (hors-ligne).
export function Ph({ seed, url, className = '', style = {}, children }) {
  const bg = isRealImg(url)
    ? { backgroundImage: `url(${url})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : photoStyle(seed)
  return (
    <div className={`ph ${className}`} style={{ ...bg, ...style }}>
      {children}
    </div>
  )
}

// Avatar rond
export function Avatar({ seed, size = 44, className = '', style = {} }) {
  return (
    <div
      className={`av ${className}`}
      style={{ width: size, height: size, borderRadius: '50%', ...avatarStyle(seed), ...style }}
    />
  )
}

// Badge de statut
export function Badge({ cls, children }) {
  return <span className={`badge ${cls}`}>{children}</span>
}

// Étoiles statiques (affichage)
export function StarsStatic({ note = 0 }) {
  const full = Math.round(note)
  return <span className="stars-static">{'★'.repeat(full)}{'☆'.repeat(5 - full)}</span>
}

// Étoiles interactives (saisie)
export function StarsInput({ value = 0, onChange }) {
  return (
    <span className="stars" role="radiogroup">
      {[1, 2, 3, 4, 5].map((n) => (
        <span
          key={n}
          className={n <= value ? 'on' : ''}
          role="radio"
          aria-checked={n === value}
          tabIndex={0}
          onClick={() => onChange && onChange(n)}
          onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && onChange && onChange(n)}
        >
          ★
        </span>
      ))}
    </span>
  )
}

// Modale générique (overlay + carte)
export function Modal({ open, onClose, title, subtitle, children, footer }) {
  useEffect(() => {
    if (!open) return
    const onEsc = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onEsc)
    return () => window.removeEventListener('keydown', onEsc)
  }, [open, onClose])
  if (!open) return null
  return (
    <div className="overlay show" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true">
        <button className="modal-close" onClick={onClose} aria-label="Fermer">×</button>
        {title && <h3 className="serif">{title}</h3>}
        {subtitle && <p className="msub">{subtitle}</p>}
        {children}
        {footer}
      </div>
    </div>
  )
}

// Toast éphémère
export function Toast({ message, onDone }) {
  useEffect(() => {
    if (!message) return
    const t = setTimeout(onDone, 2600)
    return () => clearTimeout(t)
  }, [message, onDone])
  if (!message) return null
  return (
    <div className="toast show">
      <span className="ok">✓</span> {message}
    </div>
  )
}
