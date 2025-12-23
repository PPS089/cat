<template>
  <section class="welcome">
    <canvas ref="particlesCanvas" class="welcome__particles" aria-hidden="true"></canvas>
    <div class="welcome__backdrop" aria-hidden="true"></div>
    <div class="welcome__glow welcome__glow--one" aria-hidden="true"></div>
    <div class="welcome__glow welcome__glow--two" aria-hidden="true"></div>
    <div class="welcome__container">
      <div class="welcome__viewport">
        <nav class="welcome__nav">
          <div class="welcome__brand">
            <div class="welcome__brand-mark">Pet</div>
            <div>
              <p class="welcome__brand-title">PetCare 智慧平台</p>
              <p class="welcome__brand-desc">以科技守护每一次呼吸</p>
            </div>
          </div>
          <ul class="welcome__nav-links">
            <li v-for="item in navLinks" :key="item">
            <button type="button" @click="scrollToMore">{{ item }}</button>
            </li>
          </ul>
          <div class="welcome__nav-actions">
            <button type="button" class="welcome__nav-btn is-ghost" @click="goLogin">用户登录</button>
            <button type="button" class="welcome__nav-btn is-primary" @click="goAdminLogin">管理员登录</button>
          </div>
        </nav>

        <main class="welcome__hero">
          <div class="welcome__hero-content">
            <p class="welcome__announcement">创新 · 专业 · 值得信赖</p>
            <h1>为每一次相伴，提供温暖而专业的宠物守护云</h1>
            <p class="welcome__subtitle">
              以数据和设计为纽带，把身份、健康、寄养、运营全部打通。实时可视、智能告警、温柔提醒，让宠物守护变得更有仪式感与安全感。
            </p>

            <div class="welcome__cta">
              <button type="button" class="welcome__cta-button is-primary" @click="goRegister">立即体验</button>
              <button type="button" class="welcome__cta-button is-ghost" @click="scrollToMore">了解更多</button>
            </div>

            <div class="welcome__hero-stats">
              <div v-for="metric in heroMetrics" :key="metric.label" class="welcome__stat">
                <p class="welcome__stat-value">{{ metric.value }}</p>
                <p class="welcome__stat-label">{{ metric.label }}</p>
              </div>
            </div>
          </div>

          <div class="welcome__hero-panel" aria-hidden="true">
            <div class="welcome__hero-card">
              <p class="welcome__hero-card-title">守护力面板</p>
              <ul>
                <li v-for="step in journeySteps" :key="step">
                  <span></span>
                  {{ step }}
                </li>
              </ul>
            </div>
            <div class="welcome__hero-badge">
              <p>Guardian Cloud</p>
              <strong>宠物安心指数 98%</strong>
              <small>实时监测·告警延迟 &lt; 1.5s</small>
            </div>
          </div>
        </main>
      </div>

      <div ref="moreSection" class="welcome__below">
        <div class="welcome__cta-cards">
          <article
            v-for="card in ctaCards"
            :key="card.id"
            :class="['welcome__cta-card', { 'is-primary': card.primary }]"
          >
            <header>
              <p class="welcome__cta-tag">{{ card.tag }}</p>
              <h2>{{ card.title }}</h2>
            </header>
            <p class="welcome__cta-desc">{{ card.description }}</p>
            <span class="welcome__cta-note">{{ card.note }}</span>
          </article>
        </div>

        <footer class="welcome__footer">
          <div class="welcome__footer-brand">
            <p class="welcome__brand-title">PetCare</p>
            <p>© 2025 PetCare 宠物生态科技 · 连接每位守护者</p>
            <div class="welcome__footer-icons">
              <span>邮箱服务</span>
              <span>社区论坛</span>
              <span>客服支持</span>
            </div>
          </div>
          <div class="welcome__footer-groups">
            <section v-for="group in footerGroups" :key="group.title">
              <h3>{{ group.title }}</h3>
              <ul>
                <li v-for="item in group.items" :key="item">
                  <button type="button">{{ item }}</button>
                </li>
              </ul>
            </section>
          </div>
        </footer>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

interface CtaCard {
  id: string
  tag: string
  title: string
  description: string
  note: string
  primary?: boolean
}

interface FooterGroup {
  title: string
  items: string[]
}

interface HeroMetric {
  label: string
  value: string
}

interface Particle {
  x: number
  y: number
  radius: number
  alpha: number
  driftX: number
  rise: number
  colorIndex: number
  phase: number
}

const router = useRouter()

const navLinks = ['平台优势', '产品方案', '支持与服务']

const heroMetrics: HeroMetric[] = [
  { label: '活跃宠物档案', value: '12.3k+' },
  { label: '覆盖城市', value: '48' },
  { label: '告警响应', value: '1.5s' },
]

const ctaCards: CtaCard[] = [
  {
    id: 'login',
    tag: '信任与安全',
    title: '一站式宠物健康云',
    description: '身份档案、免疫记录、体征追踪到寄养/领养全链路闭环，用数据创造安心体验。',
    note: '适合宠物医院、寄养中心与救助机构。',
    primary: true,
  },
  {
    id: 'register',
    tag: '团队协作',
    title: '敏捷的多角色协同',
    description: '分派巡诊、喂养、康复任务，跨店同步进度，重要时刻自动提醒养护人。',
    note: '子账号独立权限，适配连锁与多点位运营。',
  },
]

const journeySteps = ['健康画像生成', '跨店任务派发', 'AI 巡检告警', '家属关怀同步']

const footerGroups: FooterGroup[] = [
  {
    title: '研究',
    items: ['健康洞察', 'AI 模型实验室', '数据白皮书'],
  },
  {
    title: '产品',
    items: ['PetCare App', '守护日历', '开放能力'],
  },
  {
    title: '支持',
    items: ['帮助中心', '安全响应', '联系我们'],
  },
  {
    title: '加入我们',
    items: ['合作计划', '校园招聘', '社会招聘'],
  },
]

const goLogin = () => router.push('/login')
const goAdminLogin = () => router.push('/admin/login')
const goRegister = () => router.push('/register')

const moreSection = ref<HTMLElement | null>(null)

const scrollToMore = () => {
  moreSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const particlesCanvas = ref<HTMLCanvasElement | null>(null)
let particles: Particle[] = []
let animationFrame = 0
let ctx: CanvasRenderingContext2D | null = null
let canvasWidth = 0
let canvasHeight = 0
let dpr = 1
let lastFrameTime = 0
let scrollVelocity = 0
let lastScrollY = 0
let pointerX = 0
let pointerY = 0
let lastPointerTime = 0
let reduceMotion = false
let motionQuery: MediaQueryList | null = null
let sprites: HTMLCanvasElement[] = []

const palette = ['#FFD700', '#FF9500', '#FFFFFF']

const randomBetween = (min: number, max: number) => Math.random() * (max - min) + min

const hexToRgba = (hex: string, alpha: number) => {
  const normalized = hex.replace('#', '').trim()
  const full = normalized.length === 3 ? normalized.split('').map((c) => c + c).join('') : normalized
  const int = Number.parseInt(full, 16)
  const r = (int >> 16) & 255
  const g = (int >> 8) & 255
  const b = int & 255
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const createSprite = (color: string) => {
  const spriteSize = 64
  const sprite = document.createElement('canvas')
  sprite.width = spriteSize
  sprite.height = spriteSize
  const spriteCtx = sprite.getContext('2d')
  if (!spriteCtx) return sprite

  const center = spriteSize / 2
  const gradient = spriteCtx.createRadialGradient(center, center, 0, center, center, center)
  gradient.addColorStop(0, hexToRgba(color, 0.82))
  gradient.addColorStop(0.35, hexToRgba(color, 0.24))
  gradient.addColorStop(0.75, hexToRgba(color, 0.06))
  gradient.addColorStop(1, hexToRgba(color, 0))

  spriteCtx.fillStyle = gradient
  spriteCtx.beginPath()
  spriteCtx.arc(center, center, center, 0, Math.PI * 2)
  spriteCtx.fill()

  return sprite
}

const rebuildSprites = () => {
  sprites = palette.map((color) => createSprite(color))
}

const pickColorIndex = () => {
  const r = Math.random()
  if (r < 0.52) return 0
  if (r < 0.96) return 1
  return 2
}

const getParticleCount = () => {
  if (reduceMotion) return window.innerWidth < 768 ? 18 : 34
  if (window.innerWidth < 768) return 34
  if (window.innerWidth < 1200) return 68
  return 86
}

const createParticle = (spawnFromBottom = false): Particle => {
  const colorIndex = pickColorIndex()
  const baseRadius = randomBetween(window.innerWidth < 768 ? 2.2 : 2.6, window.innerWidth < 768 ? 5.6 : 6.6)
  const scale = colorIndex === 2 ? randomBetween(0.65, 0.82) : colorIndex === 1 ? randomBetween(0.8, 0.98) : randomBetween(0.9, 1.05)
  const radius = baseRadius * scale
  const alphaScale = colorIndex === 2 ? 0.72 : 1
  return {
    x: randomBetween(0, canvasWidth),
    y: spawnFromBottom ? canvasHeight + randomBetween(0, 80) : randomBetween(0, canvasHeight),
    radius,
    alpha: randomBetween(0.06, 0.18) * alphaScale,
    driftX: randomBetween(-0.12, 0.12),
    rise: randomBetween(0.12, 0.36),
    colorIndex,
    phase: randomBetween(0, Math.PI * 2),
  }
}

const createParticles = () => {
  const count = getParticleCount()
  particles = Array.from({ length: count }, () => createParticle())
}

const setCanvasSize = () => {
  const canvas = particlesCanvas.value
  const context = ctx
  if (!canvas || !context) return
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvasWidth = window.innerWidth
  canvasHeight = window.innerHeight
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  canvas.style.width = `${canvasWidth}px`
  canvas.style.height = `${canvasHeight}px`
  context.setTransform(dpr, 0, 0, dpr, 0, 0)
}

const renderParticles = (now: number) => {
  const context = ctx
  if (!context) return

  if (!lastFrameTime) lastFrameTime = now
  const dt = Math.min((now - lastFrameTime) / 16.6667, 2)
  lastFrameTime = now

  context.clearRect(0, 0, canvasWidth, canvasHeight)

  const motionScale = reduceMotion ? 0.35 : 1
  const pointerActive = !reduceMotion && now - lastPointerTime < 2000
  const influenceRadius = window.innerWidth < 768 ? 110 : 160
  const influenceRadius2 = influenceRadius * influenceRadius
  const parallax = scrollVelocity * 0.06 * motionScale
  scrollVelocity *= 0.88

  context.globalCompositeOperation = 'lighter'

  particles.forEach((particle) => {
    particle.y += parallax

    particle.x += (particle.driftX + Math.sin(now * 0.001 + particle.phase) * 0.06) * dt * motionScale
    particle.y -= particle.rise * dt * motionScale

    if (pointerActive) {
      const dx = particle.x - pointerX
      const dy = particle.y - pointerY
      const dist2 = dx * dx + dy * dy
      if (dist2 > 0 && dist2 < influenceRadius2) {
        const dist = Math.sqrt(dist2)
        const strength = Math.pow(1 - dist / influenceRadius, 2.1)
        const push = strength * (window.innerWidth < 768 ? 1.6 : 2.2)
        particle.x += (dx / dist) * push
        particle.y += (dy / dist) * push
      }
    }

    if (particle.y + particle.radius < -40) {
      const respawn = createParticle(true)
      particle.x = respawn.x
      particle.y = respawn.y
      particle.radius = respawn.radius
      particle.alpha = respawn.alpha
      particle.driftX = respawn.driftX
      particle.rise = respawn.rise
      particle.colorIndex = respawn.colorIndex
      particle.phase = respawn.phase
    }

    if (particle.x < -particle.radius) {
      particle.x = canvasWidth + particle.radius
    } else if (particle.x > canvasWidth + particle.radius) {
      particle.x = -particle.radius
    }

    const sprite = sprites[particle.colorIndex]
    if (!sprite) return

    const pulse = 0.84 + Math.sin(now * 0.0014 + particle.phase) * 0.1
    const size = particle.radius * pulse

    context.globalAlpha = particle.alpha
    context.drawImage(sprite, particle.x - size, particle.y - size, size * 2, size * 2)
  })

  context.globalAlpha = 1
  context.globalCompositeOperation = 'source-over'

  animationFrame = requestAnimationFrame(renderParticles)
}

const startParticles = () => {
  cancelAnimationFrame(animationFrame)
  lastFrameTime = 0
  animationFrame = requestAnimationFrame(renderParticles)
}

const initParticles = () => {
  const canvas = particlesCanvas.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  if (!ctx) return
  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  reduceMotion = motionQuery.matches

  setCanvasSize()
  rebuildSprites()
  createParticles()
  startParticles()
}

const handleResize = () => {
  if (!ctx) return
  setCanvasSize()
  createParticles()
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
}

const handleScroll = () => {
  const y = window.scrollY || 0
  const delta = y - lastScrollY
  lastScrollY = y
  scrollVelocity = Math.max(Math.min(scrollVelocity + delta, 80), -80)
}

const handlePointerMove = (event: PointerEvent) => {
  pointerX = event.clientX
  pointerY = event.clientY
  lastPointerTime = performance.now()
}

const handleVisibilityChange = () => {
  if (document.hidden) {
    cancelAnimationFrame(animationFrame)
    return
  }
  startParticles()
}

const handleMotionChange = (event: MediaQueryListEvent) => {
  reduceMotion = event.matches
  cancelAnimationFrame(animationFrame)
  ctx?.clearRect(0, 0, canvasWidth, canvasHeight)
  createParticles()
  startParticles()
}

onMounted(() => {
  document.body.classList.add('landing-page-body')
  initParticles()
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('pointermove', handlePointerMove, { passive: true })
  document.addEventListener('visibilitychange', handleVisibilityChange)
  motionQuery?.addEventListener('change', handleMotionChange)
})

onUnmounted(() => {
  document.body.classList.remove('landing-page-body')
  cancelAnimationFrame(animationFrame)
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('pointermove', handlePointerMove)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  motionQuery?.removeEventListener('change', handleMotionChange)
})
</script>

<style scoped>
:global(body.landing-page-body) {
  --welcome-gold: #ffd700;
  --welcome-amber: #ff9500;
  --welcome-sand: #f5f0e8;
  --welcome-brown: #4a3c2c;
  --welcome-ink: rgba(255, 250, 240, 0.98);
  --welcome-ink-muted: rgba(255, 250, 240, 0.78);
  --welcome-gutter: clamp(1.25rem, 4vw, 4rem);

  margin: 0;
  min-height: 100dvh;
  background:
    radial-gradient(900px 680px at 12% 12%, rgba(255, 255, 255, 0.55), transparent 62%),
    radial-gradient(1100px 760px at 84% 72%, rgba(255, 149, 0, 0.22), transparent 64%),
    linear-gradient(135deg, var(--welcome-sand) 0%, rgba(255, 215, 0, 0.38) 42%, rgba(255, 149, 0, 0.42) 100%);
  color: var(--welcome-ink);
  font-family: 'Poppins', 'Montserrat', 'HarmonyOS Sans', 'PingFang SC', 'Helvetica Neue', system-ui, -apple-system,
    BlinkMacSystemFont, sans-serif;
}

.welcome {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

.welcome__backdrop {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(1200px 900px at 18% 24%, rgba(74, 60, 44, 0.68), rgba(74, 60, 44, 0.22) 58%, rgba(74, 60, 44, 0.08) 100%),
    linear-gradient(180deg, rgba(74, 60, 44, 0.62) 0%, rgba(74, 60, 44, 0.34) 55%, rgba(74, 60, 44, 0.18) 100%);
}

.welcome__particles {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  display: block;
  pointer-events: none;
  mix-blend-mode: screen;
  opacity: 0.68;
  z-index: 1;
}

.welcome__glow {
  position: fixed;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.65;
  mix-blend-mode: screen;
  z-index: 0;
}

.welcome__glow--one {
  top: -120px;
  left: -80px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.55), transparent 72%);
}

.welcome__glow--two {
  bottom: -140px;
  right: -100px;
  background: radial-gradient(circle, rgba(255, 149, 0, 0.45), transparent 76%);
}

.welcome__container {
  position: relative;
  z-index: 2;
  width: min(1200px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: clamp(2.2rem, 5vw, 4rem);
}

.welcome__viewport {
  min-height: 100dvh;
  padding: clamp(1.25rem, 3.5vw, 3.75rem) var(--welcome-gutter);
  display: flex;
  flex-direction: column;
  gap: clamp(1.25rem, 3.2vw, 2.2rem);
}

.welcome__below {
  padding: 0 var(--welcome-gutter) clamp(2.5rem, 6vw, 5rem);
}

.welcome__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.welcome__brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.welcome__brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.22), rgba(255, 149, 0, 0.2));
  border: 1px solid rgba(255, 215, 0, 0.45);
  color: rgba(255, 250, 240, 0.95);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.05em;
  box-shadow: 0 16px 36px rgba(255, 149, 0, 0.18), inset 0 0 0 1px rgba(255, 255, 255, 0.12);
}

.welcome__brand-title {
  margin: 0;
  font-weight: 700;
  color: rgba(255, 250, 240, 0.96);
}

.welcome__brand-desc {
  margin: 0;
  color: rgba(255, 250, 240, 0.72);
  font-size: 0.9rem;
}

.welcome__nav-links {
  display: flex;
  gap: 0.75rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.welcome__nav-links button {
  border: none;
  background: transparent;
  color: rgba(255, 250, 240, 0.8);
  font-weight: 600;
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.welcome__nav-links button:hover {
  color: rgba(255, 250, 240, 0.96);
  background: rgba(255, 255, 255, 0.08);
}

.welcome__nav-actions {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.welcome__nav-btn {
  padding: 0.55rem 1.2rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 215, 0, 0.22);
  background: rgba(245, 240, 232, 0.12);
  backdrop-filter: blur(12px);
  color: rgba(255, 250, 240, 0.9);
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.18);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.welcome__nav-btn.is-primary {
  background: linear-gradient(135deg, var(--welcome-amber), var(--welcome-gold));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 16px 50px rgba(255, 149, 0, 0.35);
}

.welcome__nav-btn.is-ghost {
  background: rgba(245, 240, 232, 0.14);
}

.welcome__nav-btn:hover {
  transform: translateY(-1px);
}

.welcome__hero {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: clamp(1.5rem, 4vw, 3rem);
  align-items: center;
}

.welcome__announcement {
  font-size: 0.95rem;
  color: rgba(255, 215, 0, 0.92);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 0.75rem;
}

.welcome__hero h1 {
  font-size: clamp(3.4rem, 7vw, 7.5rem);
  line-height: 1.02;
  margin: 0 0 1.1rem;
  color: rgba(255, 250, 240, 0.98);
  letter-spacing: -0.02em;
  text-shadow: 0 18px 55px rgba(0, 0, 0, 0.35), 0 1px 0 rgba(255, 215, 0, 0.16);
}

.welcome__subtitle {
  margin: 0;
  max-width: 720px;
  color: var(--welcome-ink-muted);
  font-size: 1.1rem;
  line-height: 1.8;
}

.welcome__cta {
  margin-top: 1.75rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.9rem;
}

.welcome__cta-button {
  border: none;
  cursor: pointer;
  padding: 0.95rem 1.65rem;
  border-radius: 999px;
  font-weight: 700;
  letter-spacing: 0.02em;
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease, background 0.2s ease;
}

.welcome__cta-button.is-primary {
  background: linear-gradient(135deg, var(--welcome-amber) 0%, var(--welcome-gold) 70%, rgba(255, 250, 240, 0.92) 140%);
  color: rgba(255, 250, 240, 0.98);
  box-shadow: 0 22px 70px rgba(255, 149, 0, 0.34);
}

.welcome__cta-button.is-ghost {
  background: rgba(245, 240, 232, 0.12);
  color: rgba(255, 250, 240, 0.9);
  border: 1px solid rgba(255, 215, 0, 0.22);
  backdrop-filter: blur(12px);
}

.welcome__cta-button:hover {
  transform: translateY(-2px) scale(1.015);
  filter: brightness(1.05);
}

.welcome__cta-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(255, 215, 0, 0.26), 0 22px 70px rgba(255, 149, 0, 0.34);
}

.welcome__hero-stats {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.welcome__stat {
  flex: 1 1 120px;
  min-width: 120px;
  background: rgba(245, 240, 232, 0.12);
  border-radius: 20px;
  padding: 1rem 1.25rem;
  border: 1px solid rgba(255, 215, 0, 0.18);
  backdrop-filter: blur(12px);
  box-shadow: inset 0 0 0 1px rgba(255, 215, 0, 0.08), 0 16px 40px rgba(0, 0, 0, 0.14);
}

.welcome__stat-value {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: rgba(255, 215, 0, 0.95);
}

.welcome__stat-label {
  margin: 0;
  color: rgba(255, 250, 240, 0.78);
  font-size: 0.9rem;
}

.welcome__hero-panel {
  position: relative;
  padding: 2rem;
  background: linear-gradient(145deg, rgba(74, 60, 44, 0.88), rgba(43, 34, 25, 0.78));
  border-radius: 32px;
  color: #fff8f1;
  overflow: hidden;
  border: 1px solid rgba(255, 215, 0, 0.16);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.35);
}

.welcome__hero-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 1.5rem;
  backdrop-filter: blur(6px);
}

.welcome__hero-card-title {
  margin: 0 0 1rem;
  font-size: 1.1rem;
  letter-spacing: 0.12em;
  color: rgba(255, 215, 0, 0.9);
}

.welcome__hero-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.welcome__hero-card li {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  font-size: 0.95rem;
}

.welcome__hero-card li span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--welcome-amber);
  box-shadow: 0 0 14px rgba(255, 149, 0, 0.7);
}

.welcome__hero-badge {
  margin-top: 1.5rem;
  border-radius: 999px;
  padding: 1rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.welcome__hero-badge p {
  margin: 0;
  font-size: 0.9rem;
  letter-spacing: 0.08em;
  color: #fde9cf;
}

.welcome__hero-badge strong {
  font-size: 1.2rem;
}

.welcome__hero-badge small {
  color: rgba(255, 255, 255, 0.8);
}

.welcome__cta-cards {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.welcome__cta-card {
  padding: 1.75rem;
  border-radius: 28px;
  background: rgba(245, 240, 232, 0.92);
  border: 1px solid rgba(255, 215, 0, 0.22);
  box-shadow: 0 26px 70px rgba(0, 0, 0, 0.14);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: left;
}

.welcome__cta-card.is-primary {
  border: 1px solid rgba(255, 149, 0, 0.32);
  background: linear-gradient(135deg, rgba(245, 240, 232, 0.95), rgba(255, 236, 204, 0.92));
}

.welcome__cta-tag {
  margin: 0;
  font-size: 0.85rem;
  letter-spacing: 0.2em;
  color: rgba(74, 60, 44, 0.72);
}

.welcome__cta-card h2 {
  margin: 0.15rem 0 0.4rem;
  font-size: 1.4rem;
  color: #2f1406;
}

.welcome__cta-desc {
  margin: 0;
  color: #765344;
  line-height: 1.6;
}

.welcome__cta-note {
  font-size: 0.9rem;
  color: #a87860;
}

.welcome__footer {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
  border-top: 1px solid rgba(255, 215, 0, 0.18);
  padding-top: 2rem;
}

.welcome__footer-brand {
  flex: 1 1 220px;
}

.welcome__footer-icons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  color: var(--welcome-ink-muted);
}

.welcome__footer-groups {
  flex: 2 1 460px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.welcome__footer-groups h3 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
  color: var(--welcome-ink);
}

.welcome__footer-groups ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.welcome__footer-groups button {
  border: none;
  background: transparent;
  color: var(--welcome-ink-muted);
  text-align: left;
  padding: 0;
  font-size: 0.95rem;
  cursor: pointer;
}

.welcome__footer-groups button:hover {
  color: var(--welcome-ink);
}

@media (max-width: 768px) {
  .welcome__cta-card {
    padding: 1.5rem;
  }

  .welcome__nav {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome__viewport {
    padding: clamp(1.1rem, 5vw, 1.5rem) var(--welcome-gutter);
  }

  .welcome__hero-panel {
    padding: 1.5rem;
  }

  .welcome__hero-stats {
    flex-direction: column;
  }
}
</style>
