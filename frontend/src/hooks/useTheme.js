import { useState, useEffect } from 'react'

const KEY = 'immoapp_theme'

// Applique le thème au plus tôt (évite le flash) — appelé depuis main.jsx aussi.
export function applyStoredTheme() {
  const t = localStorage.getItem(KEY) || 'light'
  document.documentElement.setAttribute('data-theme', t)
  return t
}

export function useTheme() {
  const [theme, setTheme] = useState(() => localStorage.getItem(KEY) || 'light')
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem(KEY, theme)
  }, [theme])
  const toggle = () => setTheme((t) => (t === 'light' ? 'dark' : 'light'))
  return { theme, toggle, isDark: theme === 'dark' }
}
