import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute, { GuestOnly } from './ProtectedRoute.jsx'

import ClientLayout from '../components/ClientLayout.jsx'
import AgentLayout from '../components/AgentLayout.jsx'
import AdminLayout from '../components/AdminLayout.jsx'

// Client
import Home from '../pages/client/Home.jsx'
import Biens from '../pages/client/Biens.jsx'
import BienDetail from '../pages/client/BienDetail.jsx'
import Connexion from '../pages/client/Connexion.jsx'
import Inscription from '../pages/client/Inscription.jsx'
import Profil from '../pages/client/Profil.jsx'
import MesVisites from '../pages/client/MesVisites.jsx'
import MesReservations from '../pages/client/MesReservations.jsx'
import MesFavoris from '../pages/client/MesFavoris.jsx'
import MessagerieClient from '../pages/client/MessagerieClient.jsx'
import DevenirAgent from '../pages/client/DevenirAgent.jsx'
import AccepterInvitation from '../pages/client/AccepterInvitation.jsx'
import AgentProfil from '../pages/client/AgentProfil.jsx'
import NosAgents from '../pages/client/NosAgents.jsx'
import Faq from '../pages/client/Faq.jsx'

// Agent
import AgentDashboard from '../pages/agent/Dashboard.jsx'
import AgentBiens from '../pages/agent/Biens.jsx'
import AgentPublier from '../pages/agent/Publier.jsx'
import AgentVisites from '../pages/agent/Visites.jsx'
import AgentReservations from '../pages/agent/Reservations.jsx'
import AgentAbonnement from '../pages/agent/Abonnement.jsx'
import AgentMessagerie from '../pages/agent/Messagerie.jsx'
import AgentProfilSettings from '../pages/agent/Profil.jsx'

// Admin
import AdminDashboard from '../pages/admin/Dashboard.jsx'
import AdminDemandesRole from '../pages/admin/DemandesRole.jsx'
import AdminUtilisateurs from '../pages/admin/Utilisateurs.jsx'
import AdminSignalements from '../pages/admin/Signalements.jsx'
import AdminParametres from '../pages/admin/Parametres.jsx'
import AdminAdministrateurs from '../pages/admin/Administrateurs.jsx'
import AdminCreerAdmin from '../pages/admin/CreerAdmin.jsx'
import AdminProfilSettings from '../pages/admin/Profil.jsx'

const CLIENT = ['ROLE_CLIENT', 'ROLE_AGENT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']
const AGENT = ['ROLE_AGENT']
const ADMIN = ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']

export default function AppRouter() {
  return (
    <Routes>
      <Route element={<ClientLayout />}>
        {/* Public */}
        <Route path="/" element={<Home />} />
        <Route path="/biens" element={<Biens />} />
        <Route path="/biens/:id" element={<BienDetail />} />
        <Route path="/agents" element={<NosAgents />} />
        <Route path="/agents/:email" element={<AgentProfil />} />
        <Route path="/faq" element={<Faq />} />
        <Route path="/connexion" element={<GuestOnly><Connexion /></GuestOnly>} />
        <Route path="/inscription" element={<GuestOnly><Inscription /></GuestOnly>} />
        <Route path="/invitation/:token" element={<AccepterInvitation />} />
        <Route path="/creer-compte-admin" element={<AccepterInvitation />} />

        {/* Client connecté */}
        <Route path="/profil" element={<ProtectedRoute roles={CLIENT}><Profil /></ProtectedRoute>} />
        <Route path="/mes-visites" element={<ProtectedRoute roles={CLIENT}><MesVisites /></ProtectedRoute>} />
        <Route path="/mes-reservations" element={<ProtectedRoute roles={CLIENT}><MesReservations /></ProtectedRoute>} />
        <Route path="/mes-favoris" element={<ProtectedRoute roles={CLIENT}><MesFavoris /></ProtectedRoute>} />
        <Route path="/messagerie" element={<ProtectedRoute roles={CLIENT}><MessagerieClient /></ProtectedRoute>} />
        <Route path="/devenir-agent" element={<ProtectedRoute roles={CLIENT}><DevenirAgent /></ProtectedRoute>} />
      </Route>

      {/* Espace agent */}
      <Route path="/agent" element={<ProtectedRoute roles={AGENT}><AgentLayout /></ProtectedRoute>}>
        <Route index element={<AgentDashboard />} />
        <Route path="biens" element={<AgentBiens />} />
        <Route path="publier" element={<AgentPublier />} />
        <Route path="visites" element={<AgentVisites />} />
        <Route path="reservations" element={<AgentReservations />} />
        <Route path="abonnement" element={<AgentAbonnement />} />
        <Route path="messagerie" element={<AgentMessagerie />} />
        <Route path="profil" element={<AgentProfilSettings />} />
      </Route>

      {/* Espace admin / super-admin */}
      <Route path="/admin" element={<ProtectedRoute roles={ADMIN}><AdminLayout /></ProtectedRoute>}>
        <Route index element={<AdminDashboard />} />
        <Route path="demandes-role" element={<AdminDemandesRole />} />
        <Route path="utilisateurs" element={<AdminUtilisateurs />} />
        <Route path="signalements" element={<AdminSignalements />} />
        <Route path="parametres" element={<AdminParametres />} />
        <Route path="administrateurs" element={<AdminAdministrateurs />} />
        <Route path="creer-admin" element={<AdminCreerAdmin />} />
        <Route path="profil" element={<AdminProfilSettings />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
