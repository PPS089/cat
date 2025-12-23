<template>
  <section class="auth-shell">
    <canvas ref="particlesCanvas" class="auth-shell__particles" aria-hidden="true"></canvas>
    <div class="auth-shell__glow auth-shell__glow--one" aria-hidden="true"></div>
    <div class="auth-shell__glow auth-shell__glow--two" aria-hidden="true"></div>
    <div class="auth-card">
      <div class="auth-card__media">
        <img :src="heroImage" alt="宠物" loading="lazy" />
        <div class="auth-card__overlay">
          <p class="overlay-eyebrow">Pet Moments</p>
          <h2>{{ heroHeadline }}</h2>
          <p v-if="heroCopy" class="overlay-description">{{ heroCopy }}</p>
        </div>
      </div>
      <div class="auth-card__panel">
        <header class="auth-panel__header">
          <p v-if="subtitle" class="eyebrow">{{ subtitle }}</p>
          <h1>{{ title }}</h1>
          <p v-if="description" class="description">{{ description }}</p>
        </header>
        <div class="auth-panel__body">
          <slot />
        </div>
        <footer v-if="$slots.footer" class="auth-panel__footer">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    description?: string
    heroTitle?: string
    heroDescription?: string
    heroImage?: string
  }>(),
  {
    heroTitle: '',
    heroDescription: '',
    heroImage: new URL('@/assets/img/dog.jpg', import.meta.url).href,
  },
)

const heroHeadline = computed(() => props.heroTitle || props.title)
const heroCopy = computed(() => props.heroDescription || props.description)
const heroImage = computed(() => props.heroImage)

interface Particle {
  x: number
  y: number
  size: number
  alpha: number
  speedX: number
  speedY: number
}

const particlesCanvas = ref<HTMLCanvasElement | null>(null)
let particles: Particle[] = []
let animationFrame = 0
let canvasWidth = 0
let canvasHeight = 0
let ctx: CanvasRenderingContext2D | null = null

const randomBetween = (min: number, max: number) => Math.random() * (max - min) + min

const setCanvasSize = () => {
  const canvas = particlesCanvas.value
  const context = ctx
  if (!canvas || !context) return
  const dpr = window.devicePixelRatio || 1
  canvasWidth = window.innerWidth
  canvasHeight = window.innerHeight
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  canvas.style.width = `${canvasWidth}px`
  canvas.style.height = `${canvasHeight}px`
  context.setTransform(1, 0, 0, 1, 0, 0)
  context.scale(dpr, dpr)
}

const createParticles = () => {
  const count = window.innerWidth < 768 ? 25 : 50
  particles = Array.from({ length: count }, () => ({
    x: randomBetween(0, canvasWidth),
    y: randomBetween(0, canvasHeight),
    size: randomBetween(1, 3),
    alpha: randomBetween(0.1, 0.4),
    speedX: randomBetween(-0.1, 0.1),
    speedY: randomBetween(0.15, 0.35),
  }))
}

const drawParticles = () => {
  const context = ctx
  if (!context) return
  context.clearRect(0, 0, canvasWidth, canvasHeight)
  particles.forEach(p => {
    context.globalAlpha = p.alpha
    context.fillStyle = '#fbbf24'
    context.beginPath()
    context.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    context.fill()
    p.x += p.speedX
    p.y -= p.speedY
    if (p.y < -10) {
      p.y = canvasHeight + randomBetween(0, 20)
    }
    if (p.x < -10) {
      p.x = canvasWidth + 10
    } else if (p.x > canvasWidth + 10) {
      p.x = -10
    }
  })
  context.globalAlpha = 1
}

const animateParticles = () => {
  drawParticles()
  animationFrame = requestAnimationFrame(animateParticles)
}

const initParticles = () => {
  const canvas = particlesCanvas.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  if (!ctx) return
  setCanvasSize()
  createParticles()
  animateParticles()
}

const handleResize = () => {
  if (!ctx) return
  setCanvasSize()
  createParticles()
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrame)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
:global(html) {
  box-sizing: border-box;
  min-height: 100%;
}

:global(*),
:global(*::before),
:global(*::after) {
  box-sizing: inherit;
}

:global(body.auth-shell-body) {
  margin: 0;
  min-height: 100vh;
  background: linear-gradient(135deg, #f9fbff 0%, #eef2ff 35%, #fdf2f8 100%);
}

:global(body.auth-shell-body #app) {
  min-height: 100vh;
  background: linear-gradient(135deg, #f9fbff 0%, #eef2ff 35%, #fdf2f8 100%) !important;
}

.auth-shell {
  min-height: 100vh;
  height: 100%;
  padding: clamp(1.25rem, 4vw, 2.5rem);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Inter', 'Segoe UI', sans-serif;
  width: 100%;
  overflow: hidden;
  position: relative;
}

.auth-shell__particles {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.8;
  mix-blend-mode: screen;
}

.auth-shell__glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.6;
  mix-blend-mode: screen;
}

.auth-shell__glow--one {
  top: -120px;
  left: -80px;
  background: radial-gradient(circle, rgba(248, 186, 120, 0.9), transparent 70%);
}

.auth-shell__glow--two {
  bottom: -140px;
  right: -100px;
  background: radial-gradient(circle, rgba(255, 228, 188, 0.85), transparent 75%);
}

.auth-card {
  width: 100%;
  min-height: 0;
  height: clamp(620px, 90vh, 700px);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 36px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 35px 80px rgba(169, 88, 41, 0.2);
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(380px, 1fr);
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(14px);
}

.auth-card::after,
.auth-card::before {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4), transparent 70%);
  z-index: 0;
}

.auth-card::before {
  width: 220px;
  height: 220px;
  top: -60px;
  right: -60px;
}

.auth-card::after {
  width: 160px;
  height: 160px;
  bottom: -40px;
  left: -40px;
}

.auth-card__media {
  position: relative;
  overflow: hidden;
  height: 100%;
}

.auth-card__media img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.6s ease;
}

.auth-card__media:hover img {
  transform: scale(1.05);
}

.auth-card__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(200deg, rgba(20, 11, 4, 0.35), rgba(20, 11, 4, 0.85));
  color: #fff4eb;
  padding: 3rem 2.5rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 0.85rem;
}

.overlay-eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.35em;
  font-size: 0.75rem;
  opacity: 0.65;
  margin: 0;
}

.auth-card__overlay h2 {
  margin: 0;
  font-size: clamp(1.8rem, 2vw, 2.4rem);
  line-height: 1.2;
  color: #ffe8d2;
}

.overlay-description {
  margin: 0;
  opacity: 0.95;
  line-height: 1.6;
}

.auth-card__panel {
  position: relative;
  background: rgba(255, 255, 255, 0.82);
  padding: 3.25rem 3rem 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  z-index: 1;
  height: 100%;
  overflow: hidden;
}

.auth-panel__header {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.3em;
  font-size: 0.75rem;
  color: #f0b989;
  margin: 0;
}

.auth-panel__header h1 {
  margin: 0;
  font-size: 2rem;
  color: #2f1406;
}

.description {
  margin: 0;
  color: #8b6247;
}

.auth-panel__body {
  display: flex;
  justify-content: center;
  flex: 1;
  align-items: flex-start;
  overflow: visible;
  padding-top: 0.5rem;
}

.auth-panel__body :deep(.el-form) {
  width: 100%;
  max-width: 360px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.auth-panel__body :deep(.el-form-item) {
  margin-bottom: 1rem;
}

.auth-panel__body :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

.auth-panel__body :deep(.el-input__wrapper) {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: rgba(255, 255, 255, 0.95);
  transition: all 0.25s ease;
  padding: 0.65rem 1.05rem;
  overflow: visible;
}

.auth-panel__body :deep(.el-input__wrapper:hover),
.auth-panel__body :deep(.el-input__wrapper.is-focus) {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.25);
}

.auth-panel__body :deep(.el-input__prefix) {
  color: #f97316;
  margin-left: 0.15rem;
  margin-right: 0.5rem;
  display: inline-flex;
  align-items: center;
}

.auth-panel__body :deep(.el-input__inner) {
  font-size: 1rem;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.auth-panel__body :deep(.auth-form__meta-row) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #475569;
  margin-bottom: 0;
}

.auth-panel__body :deep(.auth-form__meta-row .el-checkbox__label) {
  color: #475569;
}

.auth-panel__body :deep(.auth-form__meta-row .el-link) {
  font-weight: 600;
}

.auth-panel__body :deep(.el-button--primary) {
  border-radius: 16px;
  border: none;
  width: 100%;
  background: linear-gradient(135deg, #f97316, #facc15);
  box-shadow: 0 18px 35px rgba(249, 115, 22, 0.35);
  height: 48px;
  font-weight: 700;
}

.auth-panel__footer {
  text-align: center;
  color: #475569;
}

.auth-panel__footer a {
  color: #b45c28;
  font-weight: 600;
}

.auth-panel__body :deep(.auth-ghost-button) {
  width: 100%;
  border-radius: 16px;
  border: 1px solid rgba(210, 166, 140, 0.5);
  background: rgba(255, 255, 255, 0.65);
  color: #8d5e3c;
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;
}

.auth-panel__body :deep(.auth-ghost-button:hover) {
  border-color: rgba(249, 115, 22, 0.4);
  background: rgba(255, 255, 255, 0.9);
  color: #b45c28;
  box-shadow: 0 16px 32px rgba(75, 33, 7, 0.15);
}

.auth-panel__footer :deep(.auth-form__footer-text) {
  color: #7a4d33;
  display: flex;
  justify-content: center;
  gap: 0.4rem;
  align-items: center;
  margin: 0;
  font-size: 0.95rem;
}

.auth-panel__footer :deep(.auth-form__footer-text .el-link) {
  font-weight: 600;
  color: #b45c28;
}

@media (max-width: 960px) {
  .auth-card {
    grid-template-columns: 1fr;
    height: auto;
  }

  .auth-card__media {
    min-height: 260px;
    height: auto;
  }

  .auth-card__panel {
    padding: 2rem;
    overflow: visible;
  }
}

@media (max-width: 520px) {
  .auth-shell {
    padding: 1.25rem 1rem;
  }

  .auth-card__panel {
    padding: 1.75rem 1.25rem;
  }
}
</style>
