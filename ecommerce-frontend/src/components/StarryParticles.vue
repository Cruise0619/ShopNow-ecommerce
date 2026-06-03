<template>
  <canvas ref="canvasRef" class="starry-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  palette: {
    type: Array,
    default: () => [
      '#ffffff', '#ffffff', '#ffffff', '#ffffff',
      '#fffdf5', '#fffdf5',
      '#ffe8c0',
      '#ffd166', '#ffd166', '#ffd166',
      '#ffb366',
      '#ffb3b3',
      '#ff8888',
      '#ff6666',
      '#ffa060',
      '#ef4444',
    ]
  }
})

const canvasRef = ref(null)
let animationId = null
let particles = []
let w, h

function randColor() { return props.palette[Math.floor(Math.random() * props.palette.length)] }

function flowAngle(x, y, t) {
  // 更强的涡旋：产生梵高式的明显大尺度漩涡
  const n = Math.sin(x * 0.003 + t * 0.25) * Math.cos(y * 0.004 - t * 0.2)
          + Math.sin((x + y) * 0.0025 + t * 0.18) * Math.cos((x - y) * 0.003 + t * 0.3)
          + Math.cos(x * 0.005 - y * 0.002 + t * 0.12) * Math.sin(y * 0.004 + x * 0.0015 - t * 0.22)
          + Math.sin(x * 0.006 + y * 0.005 + t * 0.08) * 0.7
  return n / 3.7 * Math.PI * 3
}

function drawStar(ctx, x, y, r, color, alpha) {
  // 外层光晕 — 紧凑
  const g1 = ctx.createRadialGradient(x, y, r * 0.15, x, y, r * 3)
  g1.addColorStop(0, color)
  g1.addColorStop(0.3, color)
  g1.addColorStop(1, 'transparent')
  ctx.fillStyle = g1
  ctx.globalAlpha = alpha * 0.15
  ctx.beginPath(); ctx.arc(x, y, r * 3, 0, Math.PI * 2); ctx.fill()

  // 中层柔和辉光
  const g2 = ctx.createRadialGradient(x, y, r * 0.08, x, y, r * 1.6)
  g2.addColorStop(0, 'rgba(255,255,255,0.9)')
  g2.addColorStop(0.35, color)
  g2.addColorStop(1, 'transparent')
  ctx.fillStyle = g2
  ctx.globalAlpha = alpha * 0.5
  ctx.beginPath(); ctx.arc(x, y, r * 1.6, 0, Math.PI * 2); ctx.fill()

  // 亮核
  const g3 = ctx.createRadialGradient(x, y, 0, x, y, r * 0.8)
  g3.addColorStop(0, 'white')
  g3.addColorStop(0.4, color)
  g3.addColorStop(1, 'transparent')
  ctx.fillStyle = g3
  ctx.globalAlpha = alpha * 0.85
  ctx.beginPath(); ctx.arc(x, y, r * 0.8, 0, Math.PI * 2); ctx.fill()

  // 纯白针尖核心
  const g4 = ctx.createRadialGradient(x, y, 0, x, y, r * 0.2)
  g4.addColorStop(0, 'white')
  g4.addColorStop(1, 'transparent')
  ctx.fillStyle = g4
  ctx.globalAlpha = alpha
  ctx.beginPath(); ctx.arc(x, y, r * 0.2, 0, Math.PI * 2); ctx.fill()

  ctx.globalAlpha = 1
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const dpr = Math.min(window.devicePixelRatio, 2)

  function resize() {
    w = window.innerWidth
    h = window.innerHeight
    canvas.width = w * dpr
    canvas.height = h * dpr
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
    ctx.setTransform(1, 0, 0, 1, 0, 0)
    ctx.scale(dpr, dpr)

    particles = []
    const count = 1400
    for (let i = 0; i < count; i++) {
      const x = Math.random() * w
      const y = Math.random() * h
      particles.push({
        x, y,
        r: 0.2 + Math.pow(Math.random(), 2.5) * 4.0,
        color: randColor(),
        speed: 0.1 + Math.random() * 0.45,
        phase: Math.random() * Math.PI * 2,
        twinkleSpeed: 0.2 + Math.random() * 3.5,
        twinklePhase: Math.random() * Math.PI * 2,
      })
    }
    particles.sort((a, b) => a.r - b.r)
  }

  resize()
  window.addEventListener('resize', resize)

  let mx = w / 2, my = h / 2
  window.addEventListener('mousemove', (e) => { mx = e.clientX; my = e.clientY })

  function animate(timestamp) {
    animationId = requestAnimationFrame(animate)
    const t = timestamp * 0.001

    ctx.clearRect(0, 0, w, h)

    particles.forEach((p) => {
      const angle = flowAngle(p.x, p.y, t)
      p.x += Math.cos(angle) * p.speed
      p.y += Math.sin(angle) * p.speed

      const dx = mx - p.x
      const dy = my - p.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < 160 && dist > 1) {
        const f = (1 - dist / 160) * 0.18
        p.x += dx * f
        p.y += dy * f
      }

      if (p.x < -60) p.x = w + 60
      if (p.x > w + 60) p.x = -60
      if (p.y < -60) p.y = h + 60
      if (p.y > h + 60) p.y = -60

      const twinkle = 0.35 + 0.65 * Math.sin(t * p.twinkleSpeed + p.twinklePhase)
      const alpha = 0.4 + twinkle * 0.6

      drawStar(ctx, p.x, p.y, p.r, p.color, alpha)
    })
  }

  requestAnimationFrame(animate)

  canvas._cleanup = () => {
    window.removeEventListener('resize', resize)
    cancelAnimationFrame(animationId)
  }
})

onBeforeUnmount(() => {
  canvasRef.value?._cleanup?.()
})
</script>

<style scoped>
.starry-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}
</style>
