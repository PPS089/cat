<template>
  <div class="articles-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <!-- 轮播图 -->
    <div class="carousel-container">
      <el-carousel height="450px" indicator-position="outside" arrow="always">
        <el-carousel-item>
          <div class="carousel-image-container">
            <img src="https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1600&q=80" alt="自然风景" class="carousel-image">
          </div>
        </el-carousel-item>
        <el-carousel-item>
          <div class="carousel-image-container">
            <img src="https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1600&q=80" alt="自然风景" class="carousel-image">
          </div>
        </el-carousel-item>
        <el-carousel-item>
          <div class="carousel-image-container">
            <img src="https://images.unsplash.com/photo-1469474968028-56623f02e42e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1600&q=80" alt="自然风景" class="carousel-image">
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 文章列表 -->
    <div class="articles-grid" v-loading="loading">
      <div 
        v-for="article in articles" 
        :key="article.id" 
        class="article-card"
        @click="viewArticle(article.id)"
      >
        <!-- 文章封面 -->
        <div class="article-cover">
          <img 
            :src="article.coverImage || 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80'" 
            :alt="article.title"
            @error="handleImageError"
          />
          <div class="view-count">
            <el-icon><View /></el-icon>
            {{ article.viewCount }}
          </div>
        </div>
        
        <!-- 文章内容 -->
        <div class="article-content">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary || getArticleSummary(article.content) }}</p>
          
          <div class="article-meta">
            <span class="article-author">
              <el-icon><User /></el-icon>
              {{ article.author || '管理员' }}
            </span>
            <span class="article-date">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(article.createdAt) }}
            </span>
            <span v-if="article.updatedAt && article.updatedAt !== article.createdAt" class="article-updated">
              <el-icon><Refresh /></el-icon>
              {{ formatDate(article.updatedAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && articles.length === 0" class="empty-state">
      <el-icon><Document /></el-icon>
      <p>暂无文章</p>
    </div>

    <!-- 分页 -->
    <Pagination
      v-if="totalPages >=1"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      @update:current-page="currentPage = $event"
      @pageChange="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { View, User, Calendar, Document, Refresh } from '@element-plus/icons-vue'
import { useArticles } from '@/api/articles'
import Pagination from '@/components/Pagination.vue'

// 使用文章列表组合式函数
const {
  // 响应式数据
  loading,
  articles,
  currentPage,
  pageSize,
  total,
  totalPages,
  themeStore,
  
  // 方法
  viewArticle,
  handlePageChange,
  formatDate,
  getArticleSummary,
  initialize
} = useArticles()

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80'
}

// 组件挂载时初始化
onMounted(() => {
  initialize()
})
</script>


<style scoped >
@import '@/styles/articles.css';
</style>
