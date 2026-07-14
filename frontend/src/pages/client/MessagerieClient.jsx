import { Link, useLocation } from 'react-router-dom'
import MessagerieView from '../../components/MessagerieView.jsx'

export default function MessagerieClient() {
  const loc = useLocation()
  const draftTarget = loc.state?.receiverId ? loc.state : null
  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>Messagerie</b></div>
          <h1 className="page-title serif" style={{ marginBottom: 24 }}>Messagerie</h1>
          <MessagerieView height="620px" draftTarget={draftTarget} />
        </div>
      </div>
    </div>
  )
}
