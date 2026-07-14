import { useState, useEffect, useCallback, useRef } from 'react'
import { apiError } from '../api/http.js'

// Exécute une fonction async et expose { data, loading, error, reload }.
// `deps` = dépendances qui relancent l'appel.
export function useApi(fn, deps = [], { immediate = true } = {}) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(immediate)
  const [error, setError] = useState(null)
  const fnRef = useRef(fn)
  fnRef.current = fn

  const run = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const res = await fnRef.current()
      setData(res)
      return res
    } catch (e) {
      setError(apiError(e))
      return null
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => { if (immediate) run() }, [run, immediate])

  return { data, loading, error, reload: run, setData }
}
