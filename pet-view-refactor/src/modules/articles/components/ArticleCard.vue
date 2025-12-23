<template>
  <WebCard class="article-card" clickable @click="$emit('view', article.id)">
    <div class="article-card__cover">
      <img :src="article.coverImage" :alt="article.title" loading="lazy" @error="handleError" />
      <span class="article-card__views">
        <el-icon><View /></el-icon>
        {{ article.viewCount }}
      </span>
    </div>
    <div class="article-card__content">
      <h3>{{ article.title }}</h3>
      <p>{{ summary }}</p>
      <footer>
        <span><el-icon><User /></el-icon>{{ article.author }}</span>
        <span><el-icon><Calendar /></el-icon>{{ formattedDate }}</span>
      </footer>
    </div>
  </WebCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { View, User, Calendar } from '@element-plus/icons-vue'
import WebCard from '@/shared/components/common/WebCard.vue'
import type { Article } from '../types'

const props = defineProps<{ article: Article }>()
const emit = defineEmits<{ (e: 'view', id: number): void }>()

const summary = computed(() => {
  const text = props.article.content.replace(/<[^>]*>/g, '')
  return text.length > 90 ? `${text.slice(0, 90)}...` : text
})

const formattedDate = computed(() => {
  const date = new Date(props.article.createTime)
  return date.toLocaleDateString('zh-CN')
})

const handleError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=600&q=80'
}
</script>

<style scoped>
.article-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%;
}

.article-card__cover {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  aspect-ratio: 16 / 9;
}

.article-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover img {
  transform: scale(1.05);
}

.article-card__views {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.6rem;
  background: rgba(15, 23, 42, 0.75);
  color: #fff;
  border-radius: 999px;
  font-size: 0.85rem;
}

.article-card__content h3 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--app-text-color);
}

.article-card__content p {
  margin: 0;
  color: var(--app-text-secondary);
  min-height: 58px;
}

.article-card__content footer {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.9rem;
  color: var(--app-text-secondary);
}

.article-card__content footer span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
</style>
