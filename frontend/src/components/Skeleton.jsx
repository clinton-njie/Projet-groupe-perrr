// Skeleton loaders (shimmer) — perception de rapidité vs spinner.
export function Skeleton({ w = '100%', h = 16, r = 6, style = {} }) {
  return <span className="skl" style={{ width: w, height: h, borderRadius: r, display: 'block', ...style }} />
}

// Grille de cartes de biens en chargement
export function ListingSkeletonGrid({ count = 6 }) {
  return (
    <div className="flist-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i}>
          <Skeleton h={0} style={{ aspectRatio: '4 / 5', height: 'auto', paddingTop: '125%' }} r={2} />
          <div style={{ paddingTop: 14 }}>
            <Skeleton w="55%" h={11} />
            <Skeleton w="80%" h={20} style={{ marginTop: 10 }} />
            <Skeleton w="40%" h={16} style={{ marginTop: 12 }} />
          </div>
        </div>
      ))}
    </div>
  )
}
