import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'
import https from 'node:https'
import tls from 'node:tls'

// Cible back-end (VPS). Surchargée par VITE_API_URL si défini.
const DEFAULT_TARGET = 'https://immobapp.duckdns.org'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_PROXY_TARGET || env.VITE_API_URL || DEFAULT_TARGET

  // Ne proxifie que les préfixes back. `/admin` est aussi une route SPA :
  // on laisse passer les navigations navigateur (Accept: text/html) vers l'app,
  // et on proxifie uniquement les appels API (axios → Accept: application/json).
  const passthroughHtml = (req) =>
    req.headers.accept && req.headers.accept.includes('text/html') ? '/index.html' : undefined

  // Contournement DNS local (VITE_PROXY_IP) : si le résolveur local n'arrive plus
  // à résoudre le domaine du VPS, on épingle son IP tout en conservant le SNI/Host
  // du domaine (TLS valide). À retirer dès que le DNS refonctionne.
  const pinIp = env.VITE_PROXY_IP
  const targetUrl = new URL(target)
  let pinnedAgent
  if (pinIp) {
    pinnedAgent = new https.Agent({ keepAlive: true })
    pinnedAgent.createConnection = (opts, cb) =>
      tls.connect({ host: pinIp, port: Number(targetUrl.port || 443), servername: targetUrl.hostname, rejectUnauthorized: false }, cb)
  }

  // Le proxy parle au VPS en serveur-à-serveur : on retire Origin/Referer pour que
  // le CORS du back ne rejette pas les requêtes venues d'un autre appareil (ex.
  // téléphone sur http://192.168.x.x:5173, origine non autorisée par le back).
  const stripOrigin = (proxyServer) => {
    proxyServer.on('proxyReq', (proxyReq) => {
      proxyReq.removeHeader('origin')
      proxyReq.removeHeader('referer')
    })
  }

  const proxy = (extra = {}) => ({
    target, changeOrigin: true, secure: !pinIp,
    configure: stripOrigin,
    ...(pinnedAgent ? { agent: pinnedAgent } : {}),
    ...extra,
  })

  return {
    plugins: [
      react(),
      VitePWA({
        registerType: 'autoUpdate',
        includeAssets: ['favicon.svg', 'apple-touch-icon.png'],
        manifest: {
          name: 'ImmoAPP — Plateforme immobilière',
          short_name: 'ImmoAPP',
          description: 'Trouvez, visitez et réservez des biens immobiliers au Cameroun.',
          lang: 'fr',
          theme_color: '#102C5C',
          background_color: '#102C5C',
          display: 'standalone',
          orientation: 'portrait',
          start_url: '/',
          scope: '/',
          icons: [
            { src: 'pwa-192.png', sizes: '192x192', type: 'image/png' },
            { src: 'pwa-512.png', sizes: '512x512', type: 'image/png' },
            { src: 'pwa-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
          ],
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,svg,png,webp,woff2}'],
          navigateFallbackDenylist: [/^\/api/, /^\/payments/, /^\/signalements/, /^\/visits/, /^\/admin\//],
          runtimeCaching: [
            // API : network-first (données fraîches, tolérance hors-ligne courte).
            {
              urlPattern: ({ url }) => /\/(api|payments|signalements|visits)(\/|$)/.test(url.pathname),
              handler: 'NetworkFirst',
              method: 'GET',
              options: {
                cacheName: 'immoapp-api',
                networkTimeoutSeconds: 6,
                expiration: { maxEntries: 120, maxAgeSeconds: 300 },
                cacheableResponse: { statuses: [0, 200] },
              },
            },
            // JAMAIS de cache pour l'auth et le paiement.
            {
              urlPattern: ({ url }) => /\/api\/auth\//.test(url.pathname) || /\/payments/.test(url.pathname) || /\/api\/abonnements/.test(url.pathname),
              handler: 'NetworkOnly',
            },
            // Images Cloudinary : cache-first (elles sont immuables).
            {
              urlPattern: ({ url }) => url.hostname.includes('res.cloudinary.com'),
              handler: 'CacheFirst',
              options: { cacheName: 'immoapp-images', expiration: { maxEntries: 80, maxAgeSeconds: 604800 } },
            },
          ],
        },
        devOptions: { enabled: false },
      }),
    ],
    server: {
      port: 5173,
      open: false,
      // Proxy de développement : le navigateur ne parle qu'à localhost:5173
      // (même origine) → aucune erreur CORS. Le back n'est pas modifié.
      proxy: {
        '/api': proxy(),
        '/payments': proxy(),
        '/signalements': proxy(),
        '/visits': proxy(),
        '/admin': proxy({ bypass: passthroughHtml }),
      },
    },
  }
})
