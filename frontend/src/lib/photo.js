// Placeholders d'images 100% hors-ligne (aucun appel réseau).
// On génère des dégradés déterministes "éditoriaux" (marine / doré / sable)
// à partir d'une graine, pour donner de la variété façon photo plein cadre.
// Les vraies photos (Cloudinary) viendront à l'étape API.

const PAIRS = [
  ['#102C5C', '#0B1E40'],
  ['#1B3A6B', '#0B1E40'],
  ['#C6862A', '#7A4E17'],
  ['#DEA23C', '#8A5A1E'],
  ['#2E4A72', '#101F3A'],
  ['#8C7A5B', '#3A2F1E'],
  ['#20406E', '#0B1E40'],
  ['#B98A3E', '#3A2A12'],
  ['#37568A', '#132747'],
  ['#6E5A3A', '#241C10'],
]

function hash(str = '') {
  let h = 0
  for (let i = 0; i < String(str).length; i++) h = (h * 31 + String(str).charCodeAt(i)) & 0xffffffff
  return Math.abs(h)
}

// Renvoie une valeur CSS background (dégradé + vignette) pour un .ph
export function photo(seed = 'x') {
  const h = hash(seed)
  const [a, b] = PAIRS[h % PAIRS.length]
  const angle = 110 + (h % 8) * 15
  return `linear-gradient(${angle}deg, ${a} 0%, ${b} 100%),
          radial-gradient(120% 90% at 30% 20%, rgba(255,255,255,.10), transparent 55%)`
}

// Style prêt à l'emploi pour un fond de photo
export function photoStyle(seed) {
  return { backgroundImage: photo(seed), backgroundSize: 'cover', backgroundPosition: 'center' }
}

// Avatar rond déterministe
export function avatarStyle(seed) {
  const h = hash(seed)
  const [a, b] = PAIRS[(h + 3) % PAIRS.length]
  return {
    backgroundImage: `linear-gradient(135deg, ${a}, ${b})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  }
}
