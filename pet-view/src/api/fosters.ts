import { ref, computed, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import { useThemeStore } from '@/stores/theme'
import type { Foster } from '@/types/fosters'
import dogImage from '@/assets/img/dog.jpg'


/**
 * 删除寄养记录函数
 */
export const useDeleteFoster = () => {
    const { t } = useI18n()
  
    const deleteFoster = async (fosterId: number, fosters: Ref<Foster[]>, total: Ref<number>, currentPage: Ref<number>, loadFosters: () => Promise<void>): Promise<void> => {
        try {

        // 确保fosterId是数字类型
        const validFosterId = Number(fosterId)
        
        await ElMessageBox.confirm(
        t('user.confirmDeleteFoster'),
        t('user.confirmDeleteFosterTitle'),
        {
            confirmButtonText: t('common.confirm'),
            cancelButtonText: t('common.cancel'),
            type: 'warning',
        }
        )
      
    const response = await request.delete(`/fosters/delete/${validFosterId}`)
      
      // 检查删除是否成功    
      if (response.code === 200) {
        // 从本地数据中移除该记录
        const index = fosters.value.findIndex(f => f.id === validFosterId)
        if (index !== -1) {
          fosters.value.splice(index, 1)
          
          // 更新总记录数
          total.value = Math.max(0, total.value - 1)
        }
        ElMessage.success(t('user.fosterDeleted'))
        
        // 如果删除后当前页没有数据了，且不是第一页，则回到上一页
        if (fosters.value.length === 0 && currentPage.value > 1) {
          currentPage.value--
        }
        // 重新加载当前页数据
        setTimeout(() => loadFosters(), 500)
      } 
    } catch (error: any) {
      console.error('删除寄养记录失败:', error)
    }
  }
  
  return { deleteFoster }
}

/**
 * 加载寄养记录函数
 */
export const useLoadFosters = () => {
  const { t } = useI18n()
  const fosters = ref<Foster[]>([])
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const loading = ref(false)
  
  const loadFosters = async (): Promise<void> => {
    try {
      // 调用API获取寄养记录 - 使用新添加的用户寄养记录端点
      const response = await request.get('/user/fosters', {
        params: {
          current_page: currentPage.value,
          per_page: pageSize.value
        }
      })
    
      // 检查实际的响应数据结构
      const actualData = response.data as import('@/types/api').PageResult<any>
      
      // MyBatis-Plus IPage分页格式 (records + total)
      if (actualData.records && actualData.total !== undefined) {
        total.value = actualData.total
      
        
        // 转换API数据格式到组件需要的格式
        fosters.value = actualData.records.map((foster: any) => {
       
          
          return {
            id: foster.fid || foster.id,
            pet: {
              pid: foster.pet?.pid || 0,
              name: foster.pet?.name || '',
              breed: foster.pet?.breed || '',
              age: foster.pet?.age || 0,
              gender: foster.pet?.gender || '',
              image: foster.pet?.image ? (foster.pet.image.startsWith('http') ? foster.pet.image : `/api/images/${foster.pet.image}`) : dogImage
            },
            shelter: {
              sid: foster.shelter?.sid || 0,
              name: foster.shelter?.name || '',
              location: foster.shelter?.location || ''
            },
            startDate: foster.startDate || '',
            endDate: foster.endDate || null,
            status: !foster.endDate  ? 'ongoing' : 'ended'
          }
        })
        
        
      } 
      else {
        fosters.value = []
      }
    } catch (error: any) {
      fosters.value = []
      total.value = 0
      ElMessage.error(t('user.loadFosterFailed'))
    } finally {
      loading.value = false
    }
  }
  
  // 处理页码变化
  const handlePageChange = (page: number): void => {
    currentPage.value = page
    loadFosters()
  }
  
  // 处理每页条数变化
  const handleSizeChange = (size: number): void => {
    pageSize.value = size
    currentPage.value = 1 // 重置到第一页
    loadFosters()
  }
  
  return { fosters, loadFosters, currentPage, pageSize, total, loading, handlePageChange, handleSizeChange }
}


  

/**
 * 主要的组合式函数 - 整合所有功能
 */
export const useFosters = () => {
  const { deleteFoster } = useDeleteFoster()
  const { fosters, loadFosters, currentPage, pageSize, total, loading, handlePageChange, handleSizeChange } = useLoadFosters()
  const themeStore = useThemeStore()
  
  // 获取主题状态
  const isDark = computed(() => themeStore.preferences.theme === 'dark')
  
  // 应用主题背景
  const applyTheme = (): void => {
    themeStore.applyTheme()
  }
  
  
  return {
    // 响应式数据
    fosters,
    isDark,
    currentPage,
    pageSize,
    total,
    loading,
    
    // 函数
    deleteFoster,
    loadFosters,
    handlePageChange,
    handleSizeChange,
    applyTheme,
  }
}
