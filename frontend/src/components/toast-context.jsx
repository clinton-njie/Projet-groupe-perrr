import { createContext, useContext, useState, useCallback } from 'react'
import { Toast } from './ui.jsx'

const ToastCtx = createContext(() => {})
export const useToast = () => useContext(ToastCtx)

export function ToastProvider({ children }) {
  const [msg, setMsg] = useState('')
  const notify = useCallback((m) => setMsg(m), [])
  return (
    <ToastCtx.Provider value={notify}>
      {children}
      <Toast message={msg} onDone={() => setMsg('')} />
    </ToastCtx.Provider>
  )
}
