<template>
  <div class="welcome">
    <div class="hero-section">
      <video class="hero-video" autoplay loop muted playsinline>
        <source src="../assets/video/pet.mp4" type="video/mp4">
      </video>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1>宠物管理系统</h1>
        <p>专业的宠物信息管理平台，让宠物管理更简单</p>
        <div class="hero-buttons">

          <router-link to="/login" class="btn btn-primary">立即登录</router-link>
          <router-link to="/register" class="btn btn-secondary">注册账户</router-link>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2>系统特色</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🐱</div>
            <h3>宠物管理</h3>
            <p>轻松管理宠物信息，包括品种、年龄、健康状况等详细数据</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">👥</div>
            <h3>用户管理</h3>
            <p>完善的用户权限系统，支持管理员和普通用户不同权限</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>数据统计</h3>
            <p>实时统计数据展示，帮助了解系统使用情况</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>安全可靠</h3>
            <p>采用安全的用户认证机制，保护用户数据安全</p>
          </div>
        </div>
      </div>
    </div>

    <div class="article-section">
      <div class="container">
        <div class="article-grid">
          <div 
            v-for="(article, index) in articles" 
            :key="index"
            class="article-item"
            :class="{ reverse: index % 2 === 1 }"
          >
            <div class="article-image">
              <img :src="article.image" :alt="article.alt" />
            </div>
            <div class="article-content">
              <h3>{{ article.title }}</h3>
              <p v-for="(paragraph, pIndex) in article.content" :key="pIndex">
                {{ paragraph }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import petHealth from '../assets/img/welcome/pet-health.jpg'
import petTraining from '../assets/img/welcome/pet-training.jpg'
import petCare from '../assets/img/welcome/pet-care.jpg'


// 文章数据可以直接在这里定义
const articles = [
  {
    title: '专业宠物护理',
    content: [
      '我们提供专业的宠物护理服务，包括定期健康检查、疫苗接种、营养指导等。每只宠物都能得到个性化的护理方案，确保它们健康快乐地成长。',
      '我们的护理团队由经验丰富的兽医和护理人员组成，为您的爱宠提供24小时的专业照护。'
    ],
    image: petCare,
    alt: '宠物护理'
  },
  {
    title: '科学训练方法',
    content: [
      '采用科学的正向训练方法，帮助宠物建立良好的行为习惯。我们的训练课程涵盖基础服从训练、社交技能培养、问题行为矫正等多个方面。',
      '通过系统化的训练，让宠物成为您生活中的好伙伴，与家人和谐相处。'
    ],
    image: petTraining,
    alt: '宠物训练'
  },
  {
    title: '健康管理体系',
    content: [
      '建立完善的健康管理体系，为每只宠物建立健康档案，记录生长发育、疫苗接种、疾病治疗等完整信息。',
      '通过数据分析，及时发现健康隐患，提供预防性医疗服务，让宠物远离疾病困扰。'
    ],
    image: petHealth,
    alt: '宠物健康'
  }
]
</script>



<style scoped>
@import '../styles/welcome.css';
</style>