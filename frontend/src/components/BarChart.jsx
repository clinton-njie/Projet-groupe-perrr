// Mini graphiques 100% CSS/SVG (aucune librairie, aucun réseau).
import { fmtPrix } from '../lib/constants.js'

// Barres horizontales. data = [{ label, value }]
export function BarChart({ data = [], accent = 'var(--gold)', money = false }) {
  const max = Math.max(1, ...data.map((d) => d.value))
  return (
    <div className="barchart">
      {data.length === 0 && <p className="muted" style={{ fontSize: 13 }}>Aucune donnée.</p>}
      {data.map((d) => (
        <div className="bc-row" key={d.label}>
          <span className="bc-label">{d.label}</span>
          <span className="bc-track"><span className="bc-fill" style={{ width: `${(d.value / max) * 100}%`, background: accent }} /></span>
          <span className="bc-val">{money ? `${fmtPrix(d.value)}` : d.value}</span>
        </div>
      ))}
    </div>
  )
}

// Donut simple pour une répartition. data = [{ label, value, color }]
export function Donut({ data = [], size = 132 }) {
  const total = data.reduce((s, d) => s + d.value, 0) || 1
  const r = size / 2 - 12
  const c = 2 * Math.PI * r
  let offset = 0
  return (
    <div className="donut-wrap">
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="donut">
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--line)" strokeWidth="14" />
        {data.map((d, i) => {
          const len = (d.value / total) * c
          const el = (
            <circle key={i} cx={size / 2} cy={size / 2} r={r} fill="none" stroke={d.color} strokeWidth="14"
              strokeDasharray={`${len} ${c - len}`} strokeDashoffset={-offset}
              transform={`rotate(-90 ${size / 2} ${size / 2})`} />
          )
          offset += len
          return el
        })}
        <text x="50%" y="50%" textAnchor="middle" dominantBaseline="central" className="donut-total">{total}</text>
      </svg>
      <div className="donut-legend">
        {data.map((d) => (
          <div className="dl-row" key={d.label}><span className="dl-dot" style={{ background: d.color }} />{d.label} <b>{d.value}</b></div>
        ))}
      </div>
    </div>
  )
}
