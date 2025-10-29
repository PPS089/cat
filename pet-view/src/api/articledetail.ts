import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import request from '@/utils/request'
import type { Article } from '@/types/articledetail'
import { useI18n } from 'vue-i18n'

// 文章详情组合式函数
export const useArticleDetail = () => {
  const { t } = useI18n()
  const route = useRoute()
  const themeStore = useThemeStore()

  // 响应式数据
  const loading = ref<boolean>(false)
  const article = ref<Article | null>(null)
  const isLiked = ref<boolean>(false)
  const articleTags = ref<string[]>([t('article.petCare'), t('article.knowledgeSharing'), t('article.experienceSummary')])

  // 获取文章详情
  const fetchArticleDetail = async () => {
    const articleId = Number(route.params.id)
    if (!articleId) {
      ElMessage.error(t('api.invalidArticleId'))
      return
    }

    loading.value = true
    try {
      const response = await request.get(`/articles/${articleId}`)
      // 检查响应结构 - 支持不同的响应格式
      if ((response as any).code === 200 && (response as any).data) {
        // 将后端camelCase字段转换为前端snake_case字段
        const articleData = (response as any).data
        article.value = {
          id: articleData.id || 0,
          title: articleData.title || '',
          content: articleData.content || '',
          summary: articleData.summary || '',
          createdAt: articleData.createdAt || '',
          viewCount: articleData.viewCount || 0,
          coverImage: articleData.coverImage || '',
          author: articleData.author || '',
        }
      } else {
        ElMessage.error(t('api.getArticleDetailFailed'))
      }
    } catch (error) {
      console.error(t('api.getArticleDetailFailed'), error)
      ElMessage.error(t('api.getArticleDetailFailed'))
    } finally {
      loading.value = false
    }
  }


  // 格式化日期
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  // 格式化内容
  const formatContent = (content: string): string => {
    // 简单的文本格式化，将换行转换为段落
    const paragraphs = content.split('\n\n')
    return paragraphs.map(paragraph => `<p>${paragraph}</p>`).join('')
  }

  // 收藏文章
  const likeArticle = (): void => {
    isLiked.value = !isLiked.value
    ElMessage.success(isLiked.value ? t('api.likeSuccess') : t('api.unlikeSuccess'))
  }

  // 分享文章
  const shareArticle = (): void => {
    if (navigator.share) {
      navigator.share({
        title: article.value?.title || '',
        text: article.value?.summary || '',
        url: window.location.href
      })
    } else {
      // 复制链接到剪贴板
      navigator.clipboard.writeText(window.location.href)
      ElMessage.success(t('api.linkCopied'))
    }
  }

  // 处理图片加载错误
  const handleImageError = (event: Event): void => {
    const img = event.target as HTMLImageElement
    img.style.display = 'none'
  }



  return {
    // 响应式数据
    loading,
    article,
    isLiked,
    articleTags,
    themeStore,
    
    // 翻译函数
    t,
    
    // 方法
    fetchArticleDetail,
    formatDate,
    formatContent,
    likeArticle,
    shareArticle,
    handleImageError
  }
}