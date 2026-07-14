import { Link } from 'react-router-dom'
import AccountShell from '../../components/AccountShell.jsx'
import ListingCard from '../../components/ListingCard.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useFavorites } from '../../hooks/useFavorites.js'
import { biensApi } from '../../api/biens.js'

export default function MesFavoris() {
  const { ids } = useFavorites()
  const { data, loading, error, reload } = useApi(
    () => Promise.all(ids.map((id) => biensApi.get(id).catch(() => null))).then((r) => r.filter(Boolean)),
    [ids.join(',')]
  )
  const favoris = Array.isArray(data) ? data : []

  return (
    <AccountShell title="Mes favoris">
      <AsyncState loading={loading} error={error} onRetry={reload}
        empty={!favoris.length}
        emptyTitle="Aucun favori"
        emptyHint="Ajoutez des biens à vos favoris depuis leur fiche (icône ♥).">
        <div className="flist-grid">{favoris.map((b) => <ListingCard key={b.id} bien={b} />)}</div>
      </AsyncState>
    </AccountShell>
  )
}
