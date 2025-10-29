<template>
  <div class="articles-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <!-- 轮播图 -->
    <div class="carousel-container">
      <el-carousel height="400px" indicator-position="outside" arrow="always">
        <el-carousel-item>
          <img src="@/assets/img/dog.jpg" alt="可爱的宠物" style="width: 100%; height: 100%; object-fit: cover;">
        </el-carousel-item>
        <el-carousel-item>
          <img src="@/assets/img/dog.jpg" alt="可爱的宠物" style="width: 100%; height: 100%; object-fit: cover;">
        </el-carousel-item>
        <el-carousel-item>
          <img src="@/assets/img/dog.jpg" alt="可爱的宠物" style="width: 100%; height: 100%; object-fit: cover;">
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
        <!-- 文章封面 --   article.coverImage  -->>
        <div class="article-cover">
          <img 
            :src=" '/src/assets/img/dog.jpg'" 
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
      v-if="totalPages > 1"
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
import { View, User, Calendar, Document } from '@element-plus/icons-vue'
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
  handleImageError,
  initialize
} = useArticles()

// 组件挂载时初始化
onMounted(() => {
  initialize()
})
</script>


<style scoped >
@import '@/styles/articles.css';
</style>
