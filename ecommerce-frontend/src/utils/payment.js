/**
 * Get the reachable base URL for QR code payment.
 * Queries backend for the server's LAN IP so the QR code works
 * when the phone and computer are on the same network.
 */
export async function getPaymentBaseUrl() {
  const envUrl = (import.meta.env.VITE_PAYMENT_BASE_URL || '').trim()
  if (envUrl) return envUrl

  const origin = window.location.origin
  const hostname = window.location.hostname
  if (hostname !== 'localhost' && hostname !== '127.0.0.1' && hostname !== '::1') {
    return origin
  }

  try {
    const resp = await fetch('/api/system/lan-ip')
    const json = await resp.json()
    if (json.code === 200 && json.data && json.data.ip) {
      const ip = json.data.ip
      const port = json.data.port || window.location.port || '5173'
      return `http://${ip}:${port}`
    }
  } catch { /* fall through */ }

  return origin
}
