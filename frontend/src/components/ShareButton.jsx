import { useState } from 'react'

// Partage : Web Share API si dispo, sinon menu (copier le lien / WhatsApp).
export default function ShareButton({ title = 'ImmoAPP', text = '', className = 'btn-outline' }) {
  const [open, setOpen] = useState(false)
  const [copied, setCopied] = useState(false)
  const url = typeof window !== 'undefined' ? window.location.href : ''

  const nativeShare = async () => {
    if (navigator.share) {
      try { await navigator.share({ title, text, url }); return true } catch { return true }
    }
    return false
  }
  const onClick = async () => {
    const done = await nativeShare()
    if (!done) setOpen((o) => !o)
  }
  const copy = async () => {
    try { await navigator.clipboard.writeText(url); setCopied(true); setTimeout(() => setCopied(false), 1800) } catch {}
  }
  const wa = `https://wa.me/?text=${encodeURIComponent(`${text} ${url}`)}`

  return (
    <div style={{ position: 'relative' }}>
      <button className={className} style={{ width: '100%' }} onClick={onClick}>↗ Partager</button>
      {open && (
        <div className="share-menu">
          <a onClick={copy}>{copied ? '✓ Lien copié' : 'Copier le lien'}</a>
          <a href={wa} target="_blank" rel="noreferrer" onClick={() => setOpen(false)}>Partager sur WhatsApp</a>
        </div>
      )}
    </div>
  )
}
