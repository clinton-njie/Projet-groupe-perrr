import { useOnline, useInstallPrompt } from '../hooks/usePwa.js'

export function Loader({ label = 'Chargement…', pad = 60 }) {
  return (
    <div className="center muted" style={{ padding: pad }}>
      <div className="spinner" aria-hidden />
      <div style={{ marginTop: 14, fontSize: 14, letterSpacing: '.04em' }}>{label}</div>
    </div>
  )
}

export function ErrorState({ error = 'Une erreur est survenue.', onRetry }) {
  return (
    <div className="center" style={{ padding: 50 }}>
      <div style={{ fontSize: 30, color: '#B23A3A' }}>⚠</div>
      <p className="muted" style={{ margin: '10px 0 18px', fontSize: 14, maxWidth: 420, marginInline: 'auto' }}>{error}</p>
      {onRetry && <button className="btn-outline" style={{ width: 'auto', padding: '11px 24px', marginTop: 0 }} onClick={onRetry}>Réessayer</button>}
    </div>
  )
}

export function EmptyState({ title = 'Rien à afficher', hint, action }) {
  return (
    <div className="center" style={{ padding: 50 }}>
      <div style={{ fontSize: 26, color: 'var(--gold)' }}>◍</div>
      <p className="serif" style={{ fontSize: 20, color: 'var(--navy)', margin: '10px 0 4px' }}>{title}</p>
      {hint && <p className="muted" style={{ fontSize: 14 }}>{hint}</p>}
      {action && <div style={{ marginTop: 16 }}>{action}</div>}
    </div>
  )
}

// Combine chargement / erreur / vide autour d'un contenu.
export function AsyncState({ loading, error, empty, onRetry, loadingLabel, emptyTitle, emptyHint, children }) {
  if (loading) return <Loader label={loadingLabel} />
  if (error) return <ErrorState error={error} onRetry={onRetry} />
  if (empty) return <EmptyState title={emptyTitle} hint={emptyHint} />
  return children
}

// Bandeau global « Pas de connexion »
export function OfflineBanner() {
  const online = useOnline()
  if (online) return null
  return <div className="offline-banner">● Pas de connexion — certaines actions sont indisponibles.</div>
}

// Bouton d'installation PWA (visible seulement si installable)
export function InstallButton({ className = 'btn-outline', style }) {
  const { canInstall, promptInstall } = useInstallPrompt()
  if (!canInstall) return null
  return (
    <button className={className} style={{ width: 'auto', ...style }} onClick={promptInstall}>
      ⤓ Installer l'app
    </button>
  )
}
