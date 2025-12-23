<template>
  <PageContainer :title="t('article.explorePetKnowledge')" :description="t('message.findYourPerfectCompanion')">
    <ArticleCarousel class="mb-5" @cta="scrollToGrid" />

    <section ref="gridRef" class="articles-section" v-loading="loading">
      <transition-group name="fade" tag="div" class="articles-grid">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @view="viewArticle"
        />
      </transition-group>
      <el-empty v-if="!loading && !articles.length" :description="t('article.noArticles')" />
    </section>

    <AppPagination
      v-if="pagination.total > 0"
      :current-page="pagination.currentPage"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      @pageChange="setPage"
      @sizeChange="setPageSize"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import PageContainer from '@/shared/components/common/PageContainer.vue'
import AppPagination from '@/shared/components/common/AppPagination.vue'
import ArticleCard from '@/modules/articles/components/ArticleCard.vue'
import ArticleCarousel from '@/modules/articles/components/ArticleCarousel.vue'
import { useArticlesStore } from '@/modules/articles/store/articles'

const { t } = useI18n()
const router = useRouter()
const articlesStore = useArticlesStore()
const { articles, pagination, loading } = storeToRefs(articlesStore)
const gridRef = ref<HTMLElement | null>(null)

onMounted(() => {
  void articlesStore.fetchList()
})

const viewArticle = (id: number) => {
  router.push(`/user/articles-detail/${id}`)
}

const setPage = (page: number) => {
  void articlesStore.setPage(page)
}

const setPageSize = (size: number) => {
  void articlesStore.setPageSize(size)
}

const scrollToGrid = () => {
  gridRef.value?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.mb-5 {
  margin-bottom: 2.5rem;
}

.articles-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.5rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
