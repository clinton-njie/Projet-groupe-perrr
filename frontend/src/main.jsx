import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext.jsx'
import { OfflineBanner } from './components/states.jsx'
import AppRouter from './routes/AppRouter.jsx'
import { applyStoredTheme } from './hooks/useTheme.js'
import './styles/immoapp.css'
import './styles/overrides.css'

applyStoredTheme()

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <OfflineBanner />
        <AppRouter />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)
