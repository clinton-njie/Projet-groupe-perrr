import { useState, useEffect, useCallback } from 'react'

// Favoris côté client (localStorage) — le back n'expose pas d'endpoint favoris.
const KEY = 'immoapp_favoris'
const read = () => { try { return JSON.parse(localStorage.getItem(KEY)) || [] } catch { return [] } }

export function useFavorites() {
  const [ids, setIds] = useState(read)

  useEffect(() => {
    const onStorage = (e) => { if (e.key === KEY) setIds(read()) }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  const persist = useCallback((next) => { setIds(next); localStorage.setItem(KEY, JSON.stringify(next)) }, [])
  const has = useCallback((id) => ids.includes(Number(id)) || ids.includes(String(id)), [ids])
  const toggle = useCallback((id) => {
    const n = Number(id)
    persist(has(n) ? ids.filter((x) => Number(x) !== n) : [...ids, n])
  }, [ids, has, persist])

  return { ids, has, toggle }
}
