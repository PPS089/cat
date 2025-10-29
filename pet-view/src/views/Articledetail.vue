<template>
  <div class="article-detail-container" v-loading="loading" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <div v-if="article" class="article-content">
      <!-- 文章头部 -->
      <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-meta">
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>{{ article.author || '管理员' }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(article.createdAt) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ article.viewCount }} 次阅读</span>
          </div>
        </div>
        
        <div v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" :alt="article.title" @error="handleImageError" />
        </div>
      </header>

      <!-- 文章摘要 -->
      <div v-if="article.summary" class="article-summary">
        <el-alert :title="article.summary" type="info" :closable="false" />
      </div>

      <!-- 文章内容 -->
      <main class="article-body">
        <div class="article-text" v-html="formatContent(article.content)"></div>
      </main>

      <!-- 文章底部 -->
      <footer class="article-footer">
        <div class="article-tags">
          <el-tag v-for="tag in articleTags" :key="tag" size="small" type="info">
            {{ tag }}
          </el-tag>
        </div>
        
        <div class="article-actions">
          <el-button @click="likeArticle" :icon="Star" :type="isLiked ? 'warning' : 'default'">
            {{ isLiked ? '已收藏' : '收藏' }}
          </el-button>
          <el-button @click="shareArticle" :icon="Share" type="primary">
            分享
          </el-button>
        </div>
      </footer>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-icon><Document /></el-icon>
      <p>文章不存在或已删除</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { User, Calendar, View, Star, Share, Document } from '@element-plus/icons-vue'
import { useArticleDetail } from '@/api/articledetail'

// 使用文章详情组合式函数
const {
  // 响应式数据
  loading,
  article,
  isLiked,
  articleTags,
  themeStore,
  
  // 方法
  fetchArticleDetail,
  formatDate,
  formatContent,
  likeArticle,
  shareArticle,
  handleImageError
} = useArticleDetail()

// 组件挂载时加载数据
onMounted(() => {
  fetchArticleDetail()
})
</script>

<style>
@import '@/styles/articledetail.css'
</style>