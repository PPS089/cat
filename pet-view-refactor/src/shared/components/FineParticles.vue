<template>
  <canvas ref="canvasRef" class="fine-particles" aria-hidden="true"></canvas>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

interface Particle {
  x: number
  y: number
  radius: number
  alpha: number
  vx: number
  vy: number
  phase: number
  lifespan: number
}

const canvasRef = ref<HTMLCanvasElement | null>(null)

let ctx: CanvasRenderingContext2D | null = null
let animationFrame = 0
let particles: Particle[] = []
let canvasWidth = 0
let canvasHeight = 0
let devicePixelRatio = 1
let lastFrameTime = 0
let reduceMotion = false
let motionQuery: MediaQueryList | null = null

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(max, value))
const randomBetween = (min: number, max: number) => Math.random() * (max - min) + min

const readCssVar = (name: string) => {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return value || '#e0e0e0'
}

const hexToRgba = (hex: string, alpha: number) => {
  const normalized = hex.replace('#', '').trim()
  const full = normalized.length === 3 ? normalized.split('').map((c) => c + c).join('') : normalized
  const parsed = Number.parseInt(full, 16)
  const r = (parsed >> 16) & 255
  const g = (parsed >> 8) & 255
  const b = parsed & 255
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const getParticleCount = () => {
  const area = canvasWidth * canvasHeight
  const base = Math.round(area / 25000)
  if (reduceMotion) return clamp(Math.round(base * 0.4), 8, 25)
  return clamp(base, 15, 60)
}

const createParticle = (): Particle => {
  const angle = randomBetween(0, Math.PI * 2)
  const speed = randomBetween(0.3, 1.2)
  return {
    x: randomBetween(0, canvasWidth),
    y: randomBetween(0, canvasHeight),
    radius: randomBetween(0.4, 1.2),
    alpha: randomBetween(0.15, 0.4),
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    phase: randomBetween(0, Math.PI * 2),
    lifespan: randomBetween(3000, 8000),
  }
}

const createParticles = () => {
  const count = getParticleCount()
  particles = Array.from({ length: count }, () => createParticle())
}

const setCanvasSize = () => {
  const canvas = canvasRef.value
  const context = ctx
  if (!canvas || !context) return

  devicePixelRatio = Math.min(window.devicePixelRatio || 1, 2)
  canvasWidth = window.innerWidth
  canvasHeight = window.innerHeight

  canvas.width = canvasWidth * devicePixelRatio
  canvas.height = canvasHeight * devicePixelRatio
  canvas.style.width = `${canvasWidth}px`
  canvas.style.height = `${canvasHeight}px`
  context.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0)
}

const render = (now: number) => {
  const context = ctx
  if (!context) return

  if (!lastFrameTime) lastFrameTime = now
  const dt = Math.min((now - lastFrameTime) / 16.6667, 2)
  lastFrameTime = now

  context.clearRect(0, 0, canvasWidth, canvasHeight)

  const primaryColor = readCssVar('--app-text-color')
  const accentColor = readCssVar('--accent-color') || '#6366f1'

  particles.forEach((particle, index) => {
    particle.x += particle.vx * dt
    particle.y += particle.vy * dt

    // Oscillate
    const oscillation = Math.sin(now * 0.0008 + particle.phase) * 0.08
    particle.vy += oscillation * dt

    // Fade animation
    const fade = Math.sin((now / particle.lifespan) * Math.PI) * 0.8
    const alpha = particle.alpha * clamp(fade, 0, 1)

    // Wrap around
    if (particle.x < -particle.radius) particle.x = canvasWidth + particle.radius
    else if (particle.x > canvasWidth + particle.radius) particle.x = -particle.radius

    if (particle.y < -particle.radius) {
      const respawn = createParticle()
      particle.x = respawn.x
      particle.y = respawn.y
      particle.vx = respawn.vx
      particle.vy = respawn.vy
      particle.lifespan = respawn.lifespan
    } else if (particle.y > canvasHeight + particle.radius) {
      particle.y = -particle.radius
    }

    // Draw particle
    context.fillStyle = index % 3 === 0 ? hexToRgba(accentColor, alpha) : hexToRgba(primaryColor, alpha * 0.6)
    context.beginPath()
    context.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2)
    context.fill()
  })

  animationFrame = requestAnimationFrame(render)
}

const start = () => {
  cancelAnimationFrame(animationFrame)
  lastFrameTime = 0
  animationFrame = requestAnimationFrame(render)
}

const stop = () => cancelAnimationFrame(animationFrame)

const handleResize = () => {
  if (!ctx) return
  setCanvasSize()
  createParticles()
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
}

const handleVisibilityChange = () => {
  if (document.hidden) {
    stop()
    return
  }
  start()
}

const handleMotionChange = (event: MediaQueryListEvent) => {
  reduceMotion = event.matches
  createParticles()
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  if (!ctx) return

  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  reduceMotion = motionQuery.matches

  setCanvasSize()
  createParticles()
  start()

  window.addEventListener('resize', handleResize, { passive: true })
  document.addEventListener('visibilitychange', handleVisibilityChange)
  motionQuery?.addEventListener('change', handleMotionChange)
})

onBeforeUnmount(() => {
  stop()
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  motionQuery?.removeEventListener('change', handleMotionChange)
})
</script>

<style scoped lang="scss">
.fine-particles {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.5;
}
</style>
