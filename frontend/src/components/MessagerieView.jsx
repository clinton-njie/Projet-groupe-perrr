import { useState, useEffect, useCallback, useRef } from 'react'
import { Avatar, isRealImg } from './ui.jsx'
import { Loader, EmptyState, ErrorState } from './states.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { messagesApi } from '../api/messages.js'
import { usersApi } from '../api/users.js'
import { apiError } from '../api/http.js'

const time = (s) => (s ? new Date(s).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' }) : '')

export default function MessagerieView({ height, draftTarget }) {
  const { authUserId, user } = useAuth()
  // Id numérique fiable de l'utilisateur courant. Fallback si authUserId n'a pas
  // pu être résolu à l'ouverture de session (sinon toutes les bulles vont du même côté).
  const [meId, setMeId] = useState(authUserId != null ? Number(authUserId) : null)
  useEffect(() => {
    if (authUserId != null) { setMeId(Number(authUserId)); return }
    if (user?.email) usersApi.idByEmail(user.email).then((id) => setMeId(Number(id))).catch(() => {})
  }, [authUserId, user?.email])
  const isMine = (id) => meId != null && Number(id) === meId

  const [convs, setConvs] = useState([])
  const [, forceTick] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeId, setActiveId] = useState(null)
  const [messages, setMessages] = useState([])
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const [composing, setComposing] = useState(!!draftTarget)
  const nameCache = useRef({})
  const photoCache = useRef({})

  const other = (c) => (isMine(c.senderId) ? c.receiverId : c.senderId)
  const nameOf = (uid) => nameCache.current[uid] || `Utilisateur #${uid}`
  // Avatar avec la vraie photo de profil si connue, sinon dégradé
  const avatarFor = (uid, size, seedPrefix = 'u') => {
    const photo = photoCache.current[uid]
    return isRealImg(photo)
      ? <div className="av" style={{ width: size, height: size, borderRadius: '50%', flex: 'none', backgroundImage: `url(${photo})`, backgroundSize: 'cover', backgroundPosition: 'center' }} />
      : <Avatar seed={`${seedPrefix}${uid}`} size={size} />
  }

  const loadInbox = useCallback(async () => {
    setLoading(true); setError('')
    try {
      const list = await messagesApi.inbox()
      const arr = Array.isArray(list) ? list : []
      setConvs(arr)
      // Résout les noms des interlocuteurs (best-effort)
      await Promise.all(arr.map(async (c) => {
        const uid = other(c)
        if (uid != null && !nameCache.current[uid]) {
          try { const p = await usersApi.getProfile(uid); nameCache.current[uid] = `${p.firstname || ''} ${p.lastname || ''}`.trim() || p.email; photoCache.current[uid] = p.photoProfil } catch { /* ignore */ }
        }
      }))
      setConvs([...arr])
      if (!composing && arr.length && activeId == null) openConv(arr[0])
    } catch (e) { setError(apiError(e, 'Impossible de charger les conversations.')) }
    finally { setLoading(false) }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [meId, composing])

  useEffect(() => { loadInbox() }, [loadInbox])

  // Nouvelle conversation (mode composing) : résout nom + photo du destinataire.
  useEffect(() => {
    const uid = draftTarget?.receiverId
    if (uid == null || nameCache.current[uid]) return
    usersApi.getProfile(uid)
      .then((p) => { nameCache.current[uid] = `${p.firstname || ''} ${p.lastname || ''}`.trim() || p.email; photoCache.current[uid] = p.photoProfil; forceTick((x) => x + 1) })
      .catch(() => {})
  }, [draftTarget?.receiverId])

  const openConv = async (c) => {
    setComposing(false); setActiveId(c.id)
    try {
      const msgs = await messagesApi.conversation(c.id)
      setMessages(Array.isArray(msgs) ? msgs : [])
      // marque comme lus les messages reçus non lus (best-effort)
      ;(msgs || []).filter((m) => !m.isRead && !isMine(m.senderId)).forEach((m) => messagesApi.markRead(m.id).catch(() => {}))
    } catch { setMessages([]) }
  }

  const refresh = () => { loadInbox(); if (activeId) { const c = convs.find((x) => x.id === activeId); if (c) openConv(c) } }

  const send = async (e) => {
    e.preventDefault()
    if (!draft.trim()) return
    setSending(true)
    try {
      if (composing && draftTarget) {
        await messagesApi.send({ receiverId: draftTarget.receiverId, bienId: draftTarget.bienId, contenu: draft })
      } else if (activeId) {
        await messagesApi.send({ conversationId: activeId, contenu: draft })
      }
      setDraft('')
      await loadInbox()
    } catch (err) { setError(apiError(err, "L'envoi a échoué.")) }
    finally { setSending(false) }
  }

  if (loading) return <div className="msg" style={height ? { height } : undefined}><Loader label="Chargement de la messagerie…" /></div>
  if (error) return <div className="msg" style={height ? { height } : undefined}><ErrorState error={error} onRetry={loadInbox} /></div>

  const activeConv = convs.find((c) => c.id === activeId)

  return (
    <div className="msg" style={height ? { height } : undefined}>
      <div className="msg-list">
        {composing && draftTarget && (
          <div className="msg-item on">
            {avatarFor(draftTarget.receiverId, 44, 'r')}
            <div className="mi-txt"><div className="mn">Nouveau message</div><div className="mb">À propos de : {draftTarget.bienNom || `Bien #${draftTarget.bienId}`}</div><div className="mp">Rédigez votre message…</div></div>
          </div>
        )}
        {convs.length === 0 && !composing && <div style={{ padding: 24 }}><EmptyState title="Aucune conversation" hint="Contactez un agent depuis la fiche d'un bien." /></div>}
        {convs.map((c) => (
          <div key={c.id} className={`msg-item ${c.id === activeId ? 'on' : ''} ${c.unreadCount ? 'unread' : ''}`} onClick={() => openConv(c)}>
            {avatarFor(other(c), 44)}
            <div className="mi-txt">
              <div className="mn">{nameOf(other(c))}</div>
              <div className="mb">Bien #{c.bienId}</div>
              <div className="mp">{c.lastMessage || '—'}</div>
            </div>
            <div className="mi-meta"><span className="mt">{time(c.lastMessageAt)}</span>{c.unreadCount ? <span className="badge-unread">{c.unreadCount}</span> : null}</div>
          </div>
        ))}
      </div>

      <div className="msg-thread">
        <div className="thread-top">
          {composing ? avatarFor(draftTarget?.receiverId, 40, 'r') : avatarFor(activeConv ? other(activeConv) : 'x', 40)}
          <div>
            <div className="tn serif">{composing ? (draftTarget?.receiverId != null ? nameOf(draftTarget.receiverId) : 'Nouveau message') : (activeConv ? nameOf(other(activeConv)) : 'Sélectionnez une conversation')}</div>
            <div className="ts">{composing ? (draftTarget?.bienNom || 'Nouveau message') : (activeConv ? `Bien #${activeConv.bienId}` : '')}</div>
          </div>
          <button className="msg-refresh" onClick={refresh}>↻ Actualiser</button>
        </div>
        <div className="thread-note">Messagerie non temps réel — cliquez « Actualiser » pour voir les nouveaux messages.</div>
        <div className="thread-body">
          {composing && <p className="muted center" style={{ margin: 'auto' }}>Écrivez le premier message pour démarrer la conversation.</p>}
          {!composing && messages.map((m) => (
            <div key={m.id} className={`bub ${isMine(m.senderId) ? 'me' : 'them'}`}>{m.contenu}</div>
          ))}
          {!composing && activeConv && messages.length === 0 && <p className="muted center" style={{ margin: 'auto' }}>Aucun message.</p>}
        </div>
        {(activeConv || composing) && (
          <form className="thread-input" onSubmit={send}>
            <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder="Écrire un message…" />
            <button type="submit" disabled={sending}>{sending ? '…' : 'Envoyer'}</button>
          </form>
        )}
      </div>
    </div>
  )
}
