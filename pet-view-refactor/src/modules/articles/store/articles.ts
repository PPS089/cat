import { defineStore } from 'pinia'
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { articlesService } from '../services/articles'
import type { Article } from '../types'

const fallbackCover =
  'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=800&q=80'

export const useArticlesStore = defineStore('articles', () => {
  const { t } = useI18n()
  const loading = ref(false)
  const list = ref<Article[]>([])
  const pagination = reactive({
    currentPage: 1,
    pageSize: 12,
    total: 0,
    pages: 0,
  })
  const currentArticle = ref<Article | null>(null)

  const articles = computed(() => list.value)

  const mapArticle = (article: Partial<Article>): Article => ({
    id: article.id ?? 0,
    title: article.title ?? '',
    content: article.content ?? '',
    coverImage: article.coverImage || fallbackCover,
    author: article.author || t('common.admin'),
    viewCount: article.viewCount || 0,
    createTime: article.createTime || '',
    updateTime: article.updateTime || '',
  })

  const fetchList = async () => {
    if (loading.value) return
    loading.value = true
    try {
      const response = await articlesService.list({
        currentPage: pagination.currentPage,
        pageSize: pagination.pageSize,
      })
      if (response?.code === 200 && response.data) {
        const data = response.data
        list.value = (data.records || []).map(mapArticle)
        pagination.total = data.total || 0
        pagination.pages = data.pages || 0
        if (typeof data.current === 'number') {
          pagination.currentPage = data.current
        }
        if (typeof data.size === 'number') {
          pagination.pageSize = data.size
        }
      } else {
        throw new Error('FETCH_FAILED')
      }
    } catch (error) {
      console.error('[Articles] list error', error)
    } finally {
      loading.value = false
    }
  }

  const fetchById = async (id: number) => {
    if (!id || Number.isNaN(id)) {
      ElMessage.error(t('api.invalidArticleId'))
      return null
    }
    loading.value = true
    try {
      const response = await articlesService.detail(id)
      if (response?.code === 200 && response.data) {
        currentArticle.value = mapArticle(response.data)
        return currentArticle.value
      }
      throw new Error('FETCH_FAILED')
    } catch (error) {
      console.error('[Articles] detail error', error)
      return null
    } finally {
      loading.value = false
    }
  }

  const setPage = (page: number) => {
    pagination.currentPage = page
    return fetchList()
  }

  const setPageSize = (size: number) => {
    pagination.pageSize = size
    pagination.currentPage = 1
    return fetchList()
  }

  const clearCurrent = () => {
    currentArticle.value = null
  }

  return {
    loading,
    articles,
    pagination,
    currentArticle,
    fetchList,
    fetchById,
    setPage,
    setPageSize,
    clearCurrent,
  }
})
