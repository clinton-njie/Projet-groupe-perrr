import { useState, useCallback } from 'react'

// Recherches sauvegardées (localStorage) — aucun back.
const KEY = 'immoapp_searches'
const read = () => { try { return JSON.parse(localStorage.getItem(KEY)) || [] } catch { return [] } }

export function useSavedSearches() {
  const [items, setItems] = useState(read)
  const persist = useCallback((next) => { setItems(next); localStorage.setItem(KEY, JSON.stringify(next)) }, [])
  const save = useCallback((label, params) => {
    const entry = { id: Date.now(), label, params }
    const next = [entry, ...read().filter((s) => s.label !== label)].slice(0, 8)
    persist(next)
  }, [persist])
  const remove = useCallback((id) => persist(read().filter((s) => s.id !== id)), [persist])
  return { items, save, remove }
}

// Biens récemment consultés (localStorage)
const RKEY = 'immoapp_recent'
export function pushRecent(bienId) {
  try {
    const arr = JSON.parse(localStorage.getItem(RKEY)) || []
    const next = [Number(bienId), ...arr.filter((x) => Number(x) !== Number(bienId))].slice(0, 8)
    localStorage.setItem(RKEY, JSON.stringify(next))
  } catch {}
}
export function getRecent() {
  try { return JSON.parse(localStorage.getItem(RKEY)) || [] } catch { return [] }
}
