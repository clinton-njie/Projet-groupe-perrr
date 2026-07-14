import { Outlet } from 'react-router-dom'
import Nav from './Nav.jsx'
import Footer from './Footer.jsx'
import { ToastProvider } from './toast-context.jsx'

export default function ClientLayout() {
  return (
    <ToastProvider>
      <Nav />
      <Outlet />
      <Footer />
    </ToastProvider>
  )
}
