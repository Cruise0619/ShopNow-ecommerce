/**
 * Detect the local network IP using RTCPeerConnection.
 * Used so QR codes encode a reachable IP instead of localhost.
 */
function detectLocalIP() {
  return new Promise((resolve) => {
    const pc = new RTCPeerConnection({ iceServers: [] })
    pc.createDataChannel('')
    pc.createOffer()
      .then(offer => pc.setLocalDescription(offer))
      .catch(() => {})
    pc.onicecandidate = (ice) => {
      if (!ice || !ice.candidate || !ice.candidate.candidate) return
      const m = ice.candidate.candidate.match(/([0-9]{1,3}\.){3}[0-9]{1,3}/)
      if (m) {
        pc.close()
        resolve(m[0])
      }
    }
    setTimeout(() => { pc.close(); resolve(null) }, 3000)
  })
}

export async function getPaymentBaseUrl() {
  const envUrl = (import.meta.env.VITE_PAYMENT_BASE_URL || '').trim()
  if (envUrl) return envUrl

  const origin = window.location.origin
  const hostname = window.location.hostname
  if (hostname !== 'localhost' && hostname !== '127.0.0.1' && hostname !== '::1') {
    return origin
  }

  const port = window.location.port || '5173'
  const ip = await detectLocalIP()
  if (ip) return `http://${ip}:${port}`

  return origin
}
