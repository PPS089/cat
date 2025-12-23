<template>
  <PageContainer
    v-loading="loading"
    :title="article?.title ?? t('article.explorePetKnowledge')"
    :description="undefined"
  >
    <template v-if="article">
      <article class="article-detail">
        <div v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" :alt="article.title" loading="lazy" @error="handleImageError" />
        </div>

        <div class="article-meta">
          <span>{{ t('article.author') }}：{{ article.author }}</span>
          <span>{{ t('events.eventDate') }}：{{ formatDate(article.createTime) }}</span>
          <span v-if="article.updateTime && article.updateTime !== article.createTime">
            {{ t('article.lastUpdated') }}：{{ formatDate(article.updateTime) }}
          </span>
          <span>{{ t('article.views') }}：{{ article.viewCount }}</span>
        </div>

        <div class="article-tags">
          <el-tag v-for="tag in articleTags" :key="tag" type="info" effect="dark" round>
            {{ tag }}
          </el-tag>
        </div>

        <div class="article-body" v-html="formattedContent" />

        <footer class="article-footer">
          <div class="article-tags secondary">
            <el-tag v-for="tag in articleTags" :key="`footer-${tag}`" type="info" size="small" round>
              {{ tag }}
            </el-tag>
          </div>
        </footer>
      </article>
    </template>
    <el-empty v-else-if="!loading" :description="t('article.detailNotFound')" />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import { useArticlesStore } from '@/modules/articles/store/articles'

const route = useRoute()
const { t } = useI18n()
const articlesStore = useArticlesStore()
const { currentArticle, loading } = storeToRefs(articlesStore)
const article = computed(() => currentArticle.value)

const articleTags = computed(() => [
  t('article.petCare'),
  t('article.knowledgeSharing'),
  t('article.experienceSummary'),
])

const formattedContent = computed(() => {
  const content = article.value?.content?.trim()
  if (!content) return ''
  return content
    .split(/\n{2,}/)
    .map(paragraph => `<p>${paragraph}</p>`)
    .join('')
})

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=800&q=80'
}

const loadArticle = (rawId: string | string[] | undefined) => {
  const idValue = Array.isArray(rawId) ? rawId[0] : rawId
  const id = Number(idValue)
  articlesStore.clearCurrent()
  if (!id || Number.isNaN(id)) {
    ElMessage.error(t('api.invalidArticleId'))
    return
  }
  void articlesStore.fetchById(id)
}

watch(
  () => route.params.id,
  newId => loadArticle(newId as string | string[] | undefined),
)

onMounted(() => {
  loadArticle(route.params.id as string | string[] | undefined)
})
</script>

<style scoped>
.article-detail {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.article-cover img {
  width: 100%;
  border-radius: 24px;
  object-fit: cover;
  max-height: 420px;
  box-shadow: var(--app-shadow-card);
}

.article-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  color: var(--app-text-secondary);
  font-size: 0.95rem;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.article-tags.secondary {
  gap: 0.35rem;
}

.article-body :deep(p) {
  line-height: 1.8;
  margin: 1rem 0;
  color: var(--app-text-color);
}

.article-footer {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

</style>
