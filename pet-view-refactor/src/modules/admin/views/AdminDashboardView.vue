<template>
  <div class="admin-page">
    <div class="bg-orb orb-1" />
    <div class="bg-orb orb-2" />
    <section class="hero">
      <div class="hero__meta">
        <span class="eyebrow">Admin Control · 审核中枢</span>
        <span class="pill">ADMIN ONLY</span>
      </div>
      <div class="hero__title">
        <h1>管理员控制台</h1>
        <p>专为高阶运营与审核而生，整合文章、宠物、领养/寄养的全链路管理。</p>
      </div>
      <div class="hero__grid">
        <div class="hero-card">
          <div class="hero-card__icon">✦</div>
          <div>
            <p class="hero-card__label">文章管理</p>
            <p class="hero-card__desc">新增 / 编辑 / 删除，维护社区调性</p>
          </div>
        </div>
        <div class="hero-card">
          <div class="hero-card__icon">⟠</div>
          <div>
            <p class="hero-card__label">宠物运营</p>
            <p class="hero-card__desc">上架/下架（枚举状态）、多图维护</p>
          </div>
        </div>
        <div class="hero-card">
          <div class="hero-card__icon">✓</div>
          <div>
            <p class="hero-card__label">审核流</p>
            <p class="hero-card__desc">领养/寄养审批，PENDING → APPROVED / REJECTED</p>
          </div>
        </div>
        <div class="hero-card">
          <div class="hero-card__icon">⚑</div>
          <div>
            <p class="hero-card__label">权限域</p>
            <p class="hero-card__desc">仅 ADMIN 角色可访问与操作</p>
          </div>
        </div>
      </div>
    </section>

    <section class="panels">
      <article class="panel">
        <header class="panel__header">
          <div>
            <p class="panel__eyebrow">Content Ops</p>
            <h3>文章管理</h3>
          </div>
          <span class="tag">社区</span>
        </header>
        <ul>
          <li>新增、编辑、删除社区文章</li>
          <li>保持风格一致，支持富文本与封面图</li>
          <li>状态控管：草稿 / 已发布 / 下线</li>
        </ul>
      </article>

      <article class="panel">
        <header class="panel__header">
          <div>
            <p class="panel__eyebrow">Pets Supply</p>
            <h3>宠物管理</h3>
          </div>
          <span class="tag">库存/陈列</span>
        </header>
        <ul>
          <li>新增/编辑宠物，批量上架/下架（枚举状态）</li>
          <li>多图配图与主图优先级维护</li>
          <li>基础信息校验：品种、年龄、性格、健康标记</li>
        </ul>
      </article>

      <article class="panel">
        <header class="panel__header">
          <div>
            <p class="panel__eyebrow">Adoption Flow</p>
            <h3>领养审核</h3>
          </div>
          <span class="tag success">流程</span>
        </header>
        <ul>
          <li>审批用户领养申请，调整状态 PENDING → APPROVED / REJECTED</li>
          <li>记录操作人与时间，确保审计链路</li>
          <li>支持备注/拒绝原因追踪</li>
        </ul>
      </article>

      <article class="panel">
        <header class="panel__header">
          <div>
            <p class="panel__eyebrow">Foster Ops</p>
            <h3>寄养管理</h3>
          </div>
          <span class="tag warning">进程</span>
        </header>
        <ul>
          <li>审批寄养、标记进行中/结束/拒绝/取消</li>
          <li>时间轴式状态流转，减少遗漏</li>
          <li>异常场景快速标记与通知</li>
        </ul>
      </article>

      <article class="panel">
        <header class="panel__header">
          <div>
            <p class="panel__eyebrow">Access</p>
            <h3>用户与角色</h3>
          </div>
          <span class="tag neutral">安全</span>
        </header>
        <ul>
          <li>仅 ADMIN 角色可访问此区</li>
          <li>基于角色的入口与操作隔离</li>
          <li>支持后续扩展子角色（阅读/操作）</li>
        </ul>
      </article>
    </section>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700&family=Playfair+Display:wght@600&display=swap');

:global(body) {
  font-family: 'Manrope', 'Segoe UI', system-ui, -apple-system, sans-serif;
  background: #0b111a;
}

.admin-page {
  position: relative;
  min-height: 100vh;
  padding: 56px 28px 64px;
  color: #eaf0f6;
  background: radial-gradient(circle at 20% 20%, rgba(255, 199, 125, 0.12), transparent 35%),
    radial-gradient(circle at 80% 10%, rgba(107, 231, 186, 0.12), transparent 30%),
    linear-gradient(140deg, #0b111a 0%, #0c1a1b 40%, #0d0f1b 100%);
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  width: 320px;
  height: 320px;
  filter: blur(110px);
  opacity: 0.5;
  z-index: 0;
  border-radius: 50%;
}
.orb-1 {
  background: #8ad6b5;
  top: -80px;
  left: -40px;
}
.orb-2 {
  background: #f6c78d;
  top: 120px;
  right: -80px;
}

.hero {
  position: relative;
  z-index: 1;
  padding: 32px 32px 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(15, 25, 36, 0.75);
  backdrop-filter: blur(10px);
  border-radius: 28px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.38);
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(255, 215, 160, 0.12), rgba(95, 248, 186, 0.08));
  opacity: 0.8;
  pointer-events: none;
}

.hero__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(234, 240, 246, 0.76);
}

.pill {
  font-size: 11px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
  color: #f6c78d;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero__title h1 {
  margin: 0;
  font-size: 36px;
  font-family: 'Playfair Display', 'Manrope', serif;
  letter-spacing: 0.01em;
}

.hero__title p {
  margin: 10px 0 18px;
  color: rgba(234, 240, 246, 0.82);
  max-width: 760px;
  line-height: 1.65;
}

.hero__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.hero-card {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.hero-card__icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(246, 199, 141, 0.35), rgba(138, 214, 181, 0.35));
  color: #0b111a;
  font-weight: 700;
}

.hero-card__label {
  margin: 0 0 4px;
  font-weight: 700;
  color: #fdf8f1;
}

.hero-card__desc {
  margin: 0;
  color: rgba(234, 240, 246, 0.72);
  line-height: 1.5;
  font-size: 14px;
}

.panels {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 28px;
}

.panel {
  padding: 20px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.24);
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.panel:hover {
  transform: translateY(-6px);
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
}

.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel__eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(234, 240, 246, 0.65);
}

.panel h3 {
  margin: 4px 0 0;
  font-size: 20px;
  color: #fdf8f1;
}

.tag {
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f6c78d;
  background: rgba(246, 199, 141, 0.08);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.tag.success {
  color: #8ad6b5;
  background: rgba(138, 214, 181, 0.1);
}

.tag.warning {
  color: #f7c66c;
  background: rgba(247, 198, 108, 0.1);
}

.tag.neutral {
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.06);
}

.panel ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: rgba(234, 240, 246, 0.78);
  line-height: 1.6;
}

.panel li + li {
  margin-top: 6px;
}

@media (max-width: 768px) {
  .admin-page {
    padding: 32px 18px 48px;
  }

  .hero {
    padding: 24px 18px;
  }

  .hero__title h1 {
    font-size: 28px;
  }

  .panels {
    grid-template-columns: 1fr;
  }
}
</style>



<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'

// 监听新的申请通知
let newApplicationListener: ((event: Event) => void) | null = null

onMounted(() => {
  // 添加新申请通知的监听器
  newApplicationListener = (event: Event) => {
    const detail = (event as CustomEvent).detail || {}
    const applicationType = detail.data?.applicationType || ''
    const petName = detail.data?.petName || '未知宠物'
    const applicantName = detail.data?.applicantName || '未知用户'
    
    // 可以在这里添加额外的处理逻辑，比如更新UI上的统计数据等
    console.log(`收到新的${applicationType === 'adoption' ? '领养' : '寄养'}申请: ${applicantName} 申请${applicationType === 'adoption' ? '领养' : '寄养'} ${petName}`)
  }
  
  window.addEventListener('new-application', newApplicationListener)
})

onBeforeUnmount(() => {
  // 移除事件监听器
  if (newApplicationListener) {
    window.removeEventListener('new-application', newApplicationListener)
  }
})
</script>
