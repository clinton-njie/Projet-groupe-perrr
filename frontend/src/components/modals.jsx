import { useState, useEffect } from 'react'
import { Modal, StarsInput } from './ui.jsx'
import { useToast } from './toast-context.jsx'
import { CRITERES_AVIS, fmtPrix } from '../lib/constants.js'
import { visitesApi } from '../api/visites.js'
import { reservationsApi } from '../api/reservations.js'
import { avisApi } from '../api/avis.js'
import { signalementsApi } from '../api/signalements.js'
import { apiError } from '../api/http.js'

// Bornes calendrier réelles (heure locale)
const pad = (n) => String(n).padStart(2, '0')
const nowLocalDT = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}
const todayLocal = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const fmtSlot = (s) => (s ? new Date(s).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' }) : '')

function OperateurTel({ operateur, setOperateur, tel, setTel }) {
  return (
    <>
      <p style={{ fontSize: 11, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--muted)', margin: '18px 0 10px' }}>
        Opérateur Mobile Money
      </p>
      <div className="ops">
        <button type="button" className={`op orange ${operateur === 'ORANGE' ? 'on' : ''}`} onClick={() => setOperateur('ORANGE')}>Orange Money</button>
        <button type="button" className={`op mtn ${operateur === 'MTN' ? 'on' : ''}`} onClick={() => setOperateur('MTN')}>MTN MoMo</button>
      </div>
      <div className="field" style={{ marginTop: 16 }}>
        <label>Numéro de téléphone</label>
        <input value={tel} onChange={(e) => setTel(e.target.value)} placeholder="6XX XX XX XX" inputMode="tel" required />
      </div>
    </>
  )
}

// -------- Réserver une VISITE (payante, escrow) --------
export function VisiteModal({ open, onClose, bien, clientEmail, prixVisite, onDone }) {
  const [date, setDate] = useState('')
  const [heure, setHeure] = useState('')
  const [slots, setSlots] = useState([])
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [slotSel, setSlotSel] = useState(null) // { debut, fin }
  const [operateur, setOperateur] = useState('ORANGE')
  const [tel, setTel] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const notify = useToast()

  useEffect(() => {
    if (!open) { setError(''); setBusy(false); setDate(''); setHeure(''); setSlots([]); setSlotSel(null) }
  }, [open])

  // Créneaux publiés par l'agent pour la date choisie (si l'agent en a définis)
  useEffect(() => {
    setSlots([]); setSlotSel(null)
    if (!open || !bien || !date) return
    setLoadingSlots(true)
    visitesApi.availableSlots(bien.id, date)
      .then((s) => setSlots(Array.isArray(s) ? s : []))
      .catch(() => setSlots([]))
      .finally(() => setLoadingSlots(false))
  }, [open, bien, date])

  const slotVal = (s) => ({ debut: s.dateHeureDebut || s.debut || s.heureDebut, fin: s.dateHeureFin || s.fin || s.heureFin })
  const manualSlot = () => {
    if (!date || !heure) return null
    const d = new Date(`${date}T${heure}`)
    const f = new Date(d.getTime() + 60 * 60000)
    const iso = (x) => `${x.getFullYear()}-${pad(x.getMonth() + 1)}-${pad(x.getDate())}T${pad(x.getHours())}:${pad(x.getMinutes())}:00`
    return { debut: iso(d), fin: iso(f) }
  }

  const submit = async (e) => {
    e.preventDefault()
    const chosen = slotSel || manualSlot()
    if (!chosen || !chosen.debut) { setError('Choisissez une date et une heure de visite.'); return }
    if (new Date(chosen.debut) <= new Date()) { setError('La date de visite doit être dans le futur.'); return }
    setBusy(true); setError('')
    try {
      await visitesApi.create({ bienId: bien.id, clientEmail, dateHeureDebut: chosen.debut, dateHeureFin: chosen.fin, operateur, telephone: tel })
      onClose()
      notify(`Visite demandée — paiement ${operateur === 'ORANGE' ? 'Orange Money' : 'MTN MoMo'} de ${fmtPrix(prixVisite)} FCFA.`)
      onDone && onDone()
    } catch (err) {
      setError(apiError(err, "La demande de visite a échoué (créneau déjà pris ?)."))
    } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title="Réserver une visite" subtitle={bien ? `${bien.nom} — ${fmtPrix(prixVisite)} FCFA` : ''}>
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="field"><label>Jour de la visite</label><input type="date" min={todayLocal()} value={date} onChange={(e) => { setDate(e.target.value); setHeure(''); setSlotSel(null) }} required /></div>

        {date && (
          <>
            {loadingSlots && <p className="muted" style={{ fontSize: 13 }}>Recherche des créneaux…</p>}
            {!loadingSlots && slots.length > 0 && (
              <>
                <p style={{ fontSize: 11, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--muted)', margin: '4px 0 10px' }}>Créneaux disponibles</p>
                <div className="pill-row" style={{ marginBottom: 8 }}>
                  {slots.map((s, i) => {
                    const v = slotVal(s)
                    const on = slotSel && slotSel.debut === v.debut
                    return <button type="button" key={i} className={`pill ${on ? 'on' : ''}`} onClick={() => setSlotSel(v)}>{fmtSlot(v.debut)}</button>
                  })}
                </div>
              </>
            )}
            {!loadingSlots && slots.length === 0 && (
              <div className="field">
                <label>Heure souhaitée (durée 1 h)</label>
                <input type="time" value={heure} onChange={(e) => setHeure(e.target.value)} required />
                <p className="muted" style={{ fontSize: 11.5, marginTop: 6 }}>Aucun créneau publié par l'agent — proposez une heure, l'agent confirmera.</p>
              </div>
            )}
          </>
        )}

        <OperateurTel operateur={operateur} setOperateur={setOperateur} tel={tel} setTel={setTel} />
        <div className="row" style={{ borderBottom: '1px solid var(--line)', marginTop: 8 }}>
          <span>Frais de visite</span><b>{fmtPrix(prixVisite)} FCFA</b>
        </div>
        <button className="btn-block" disabled={busy}>{busy ? 'Traitement…' : `Payer ${fmtPrix(prixVisite)} FCFA`}</button>
        <p className="center muted" style={{ fontSize: 12, marginTop: 12 }}>Paiement sécurisé Mobile Money — <b>non remboursable</b>.</p>
      </form>
    </Modal>
  )
}

// -------- Réserver le BIEN (location par période) --------
export function ReservationModal({ open, onClose, bien, onDone }) {
  const [startDate, setStart] = useState('')
  const [endDate, setEnd] = useState('')
  const [operateur, setOperateur] = useState('ORANGE')
  const [tel, setTel] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const notify = useToast()

  useEffect(() => { if (!open) { setError(''); setBusy(false); setStart(''); setEnd('') } }, [open])

  const months = (() => {
    if (!startDate || !endDate) return 0
    const a = new Date(startDate), b = new Date(endDate)
    return Math.max(0, (b.getFullYear() - a.getFullYear()) * 12 + (b.getMonth() - a.getMonth()))
  })()
  const estimate = months * (bien?.prixMensuel || 0)

  const submit = async (e) => {
    e.preventDefault()
    const today = todayLocal()
    if (startDate < today) { setError("La date de début ne peut pas être dans le passé."); return }
    if (!(endDate > startDate)) { setError('La date de fin doit être après la date de début.'); return }
    setBusy(true); setError('')
    try {
      await reservationsApi.create({ propertyId: bien.id, startDate, endDate, operateur, telephone: tel })
      onClose()
      notify(`Réservation demandée — paiement Mobile Money.`)
      onDone && onDone()
    } catch (err) {
      setError(apiError(err, 'La réservation a échoué (bien déjà réservé sur cette période ?).'))
    } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title="Réserver le bien" subtitle={bien ? `${bien.nom} — location` : ''}>
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="field two">
          <div><label>Début</label><input type="date" min={todayLocal()} value={startDate} onChange={(e) => setStart(e.target.value)} required /></div>
          <div><label>Fin</label><input type="date" min={startDate || todayLocal()} value={endDate} onChange={(e) => setEnd(e.target.value)} required /></div>
        </div>
        <div className="row"><span>Loyer mensuel</span><b>{fmtPrix(bien?.prixMensuel)} FCFA</b></div>
        {months > 0 && (
          <div className="row" style={{ borderBottom: '1px solid var(--line)' }}>
            <span>Estimation ({months} mois)</span><b>{fmtPrix(estimate)} FCFA</b>
          </div>
        )}
        <OperateurTel operateur={operateur} setOperateur={setOperateur} tel={tel} setTel={setTel} />
        <button className="btn-block" disabled={busy}>{busy ? 'Traitement…' : 'Confirmer & payer'}</button>
        <p className="center muted" style={{ fontSize: 12, marginTop: 12 }}>Montant calculé par le serveur (loyer × durée) — <b>paiement non remboursable</b>.</p>
      </form>
    </Modal>
  )
}

// -------- Notation d'une transaction (6 critères) --------
export function NotationModal({ open, onClose, opportunite, onDone }) {
  const [notes, setNotes] = useState({})
  const [commentaire, setCommentaire] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const notify = useToast()
  useEffect(() => { if (!open) { setNotes({}); setCommentaire(''); setError('') } }, [open])
  const setNote = (k, v) => setNotes((n) => ({ ...n, [k]: v }))

  const submit = async () => {
    if (CRITERES_AVIS.some((c) => !notes[c.key])) { setError('Merci de noter chaque critère.'); return }
    setBusy(true); setError('')
    try {
      await avisApi.noterTransaction({ opportuniteId: opportunite?.opportuniteId ?? opportunite?.id, ...notes, commentaire })
      onClose(); notify('Merci ! Votre note a bien été enregistrée.'); onDone && onDone()
    } catch (err) { setError(apiError(err, "L'envoi de la note a échoué.")) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title="Noter votre expérience" subtitle={opportunite?.libelle || 'Notez l’agent et le bien'}>
      {error && <div className="form-error">{error}</div>}
      {CRITERES_AVIS.map((c) => (
        <div className="crit" key={c.key}>
          <span className="crit-l">{c.label}</span>
          <StarsInput value={notes[c.key] || 0} onChange={(v) => setNote(c.key, v)} />
        </div>
      ))}
      <div className="field" style={{ marginTop: 20 }}>
        <label>Commentaire (facultatif)</label>
        <textarea rows={3} maxLength={1000} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} placeholder="Partagez votre expérience…" />
      </div>
      <button className="btn-block" onClick={submit} disabled={busy}>{busy ? 'Envoi…' : 'Envoyer mon avis'}</button>
    </Modal>
  )
}

// -------- Avis sur la plateforme --------
export function AvisPlateformeModal({ open, onClose }) {
  const [note, setNote] = useState(0)
  const [commentaire, setCommentaire] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const notify = useToast()
  useEffect(() => { if (!open) { setNote(0); setCommentaire(''); setError('') } }, [open])

  const submit = async () => {
    if (!note) { setError('Choisissez une note.'); return }
    setBusy(true); setError('')
    try {
      await avisApi.noterPlateforme(note, commentaire)
      onClose(); notify('Merci pour votre retour sur ImmoAPP !')
    } catch (err) { setError(apiError(err, "L'envoi a échoué.")) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title="Votre avis sur ImmoAPP" subtitle="Aidez-nous à améliorer la plateforme">
      {error && <div className="form-error">{error}</div>}
      <div className="crit"><span className="crit-l">Note globale</span><StarsInput value={note} onChange={setNote} /></div>
      <div className="field" style={{ marginTop: 20 }}>
        <label>Commentaire (facultatif)</label>
        <textarea rows={4} maxLength={1000} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} placeholder="Qu'avez-vous pensé de votre expérience ?" />
      </div>
      <button className="btn-block" onClick={submit} disabled={busy}>{busy ? 'Envoi…' : 'Envoyer'}</button>
    </Modal>
  )
}

// -------- Signaler un agent --------
export function SignalerModal({ open, onClose, agentId, agentNom = 'cet agent' }) {
  const [objets, setObjets] = useState([])
  const [objetId, setObjetId] = useState('')
  const [description, setDescription] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const notify = useToast()

  useEffect(() => {
    if (!open) return
    setError('')
    signalementsApi.objets('AGENT')
      .then((list) => { const arr = Array.isArray(list) ? list.filter((o) => o.actif !== false) : []; setObjets(arr); if (arr[0]) setObjetId(String(arr[0].id)) })
      .catch(() => setObjets([]))
  }, [open])

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      await signalementsApi.create({ type: 'AGENT', objetSignalementId: Number(objetId), description, agentId })
      onClose(); setDescription(''); notify('Signalement transmis à la modération.')
    } catch (err) { setError(apiError(err, "L'envoi du signalement a échoué.")) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title="Signaler un agent" subtitle={`À propos de ${agentNom}`}>
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="field">
          <label>Motif</label>
          <select value={objetId} onChange={(e) => setObjetId(e.target.value)} required>
            {objets.length === 0 && <option value="">Aucun motif disponible</option>}
            {objets.map((o) => <option key={o.id} value={o.id}>{o.libelle}</option>)}
          </select>
        </div>
        <div className="field">
          <label>Description</label>
          <textarea rows={4} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Décrivez le problème rencontré…" required />
        </div>
        <button className="btn-block" disabled={busy || !objetId}>{busy ? 'Envoi…' : 'Envoyer le signalement'}</button>
      </form>
    </Modal>
  )
}
