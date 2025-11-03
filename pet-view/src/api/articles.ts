import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from '@/stores/theme'
import request from '@/utils/request'
import type { Article, ArticlePageData } from '@/types/articles'

/**
 * 文章列表组合式函数
 */
export const useArticles = () => {
  const router = useRouter()
  const { t } = useI18n()
  const themeStore = useThemeStore()

  // 响应式数据
  const loading = ref<boolean>(false)
  const articles = ref<Article[]>([])
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(12)
  const total = ref<number>(0)
  const totalPages = ref<number>(0)

  // 获取文章列表
  const fetchArticles = async (): Promise<void> => {
    loading.value = true
    try {
      const response = await request.get('/articles', {
        params: {
          currentPage: currentPage.value,
          pageSize: pageSize.value
        }
      })
      
      // 检查响应结构 - 支持不同的响应格式
      if (response.code === 200) {
        const data = response.data as ArticlePageData
        const records = data.records || [] 
        
        // 映射后端字段到前端期望的字段名
        articles.value = records.map((article: Article) => ({
          id: article.id,
          title: article.title,
          content: article.content,
          summary: article.summary || '',
          coverImage: article.coverImage,
          author: article.author || t('admin'),
          viewCount: article.viewCount || 0,
          createdAt: article.createdAt || '',
          updatedAt: article.updatedAt || '',
        }))
        
        // 处理分页数据
        total.value = response.data.total || 0
        totalPages.value = response.data.pages || 0
      } else {
        ElMessage.error(t('api.getArticleListFailed'))
      }
    } catch (error) {
      console.error(t('api.getArticleListFailed'), error)
      ElMessage.error(t('api.getArticleListFailed'))
    } finally {
      loading.value = false
    }
  }

  // 获取文章详情
  const fetchArticleDetail = async (id: number): Promise<Article | null> => {
    try {
      const response = await request.get(`/articles/${id}`)
      const data = response.data as Article
      
      if (response.code === 200 && data) {
        return {
          id: data.id,
          title: data.title,
          content: data.content,
          summary: data.summary || '',
          coverImage: data.coverImage,
          author: data.author || t('admin'),
          viewCount: data.viewCount || 0,
          createdAt: data.createdAt || '',
          updatedAt: data.updatedAt || '',
        }
      } else {
        ElMessage.error(t('api.getArticleListFailed'))
        return null
      }
    } catch (error) {
      console.error(t('api.getArticleDetailFailed'), error)
      ElMessage.error(t('api.getArticleDetailFailed'))
      return null
    }
  }

  // 查看文章详情
  const viewArticle = (id: number): void => {
    router.push(`/user/articles-detail/${id}`)
  }

  // 分页变化
  const handlePageChange = (page: number): void => {
    currentPage.value = page
    fetchArticles()
  }

  // 格式化日期
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  }

  // 获取文章摘要
  const getArticleSummary = (content: string): string => {
    // 移除HTML标签
    const plainText = content.replace(/<[^>]*>/g, '')
    // 截取前100个字符
    return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
  }

  // 处理图片加载错误
  const handleImageError = (event: Event): void => {
    const img = event.target as HTMLImageElement
    img.src = '/images/default-article.jpg'
  }



  // 组件初始化
  const initialize = (): void => {
    fetchArticles()
  }

  return {
    // 响应式数据
    loading,
    articles,
    currentPage,
    pageSize,
    total,
    totalPages,
    themeStore,
    
    // 方法
    fetchArticles,
    fetchArticleDetail,
    viewArticle,
    handlePageChange,
    formatDate,
    getArticleSummary,
    handleImageError,
    initialize
  }
}