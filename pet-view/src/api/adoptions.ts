import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import type { AdoptionTimelineItem, AdoptionTimelineResponse, Adoption } from '../types/adoptions'
import type { PageResult } from '@/types/api'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'



/**
 * 获取宠物领养记录时间线的组合式函数
 */
export const useAdoptionTimeline = () => {
  const { t } = useI18n()
  
  // 时间线数据
  const timeline = ref<AdoptionTimelineItem[]>([])
  const loading = ref(false)
  const dialogVisible = ref(false)
  const selectedPetId = ref<number | null>(null)
  const selectedPetName = ref('')

  /**
   * 获取宠物的领养记录时间线
   */
  const loadAdoptionTimeline = async (petId: number, petName: string) => {
    try {
      loading.value = true
      selectedPetId.value = petId
      selectedPetName.value = petName
      
      const response = await request.get<AdoptionTimelineResponse>(`/pets/${petId}/adoption-timeline`)
      
      if (response.code === 200 && response.data.timeline !== undefined) {
        const result = response.data as AdoptionTimelineResponse
        timeline.value = result.timeline
        dialogVisible.value = true
      } else {
        ElMessage.error(response.message || t('user.loadTimelineFailed'))
      }
    } catch (error) {
      ElMessage.error(t('user.loadTimelineFailed'))
    } finally {
      loading.value = false
    }
  }

  /**
   * 关闭对话框
   */
  const closeDialog = () => {
    dialogVisible.value = false
    timeline.value = []
    selectedPetId.value = null
    selectedPetName.value = ''
  }

  /**
   * 获取动作类型的显示文本
   */
  const getActionTypeText = (action: string) => {
    const actionMap: Record<string, string> = {
      'adopted': t('user.adopted'),
      'fostered': t('user.fostered'),
      'foster_ended': t('user.fosterEnded'),
      'returned': t('user.returned')
    }
    return actionMap[action] || action
  }

  return {
    // 数据
    timeline,
    loading,
    dialogVisible,
    selectedPetId,
    selectedPetName,
    
    // 方法
    loadAdoptionTimeline,
    closeDialog,
    getActionTypeText
  }
}



/**
 * 路由导航函数，没有领养记录跳转领养界面
 */
export const useAdoptionNavigation = () => {
  const router = useRouter()
  
  const browsePets = () => {
    router.push('/user/adoption-pets')
  }
  
  return { browsePets }
}



/**
 * 加载领养记录函数 
 * 使用API端点 /api/user/adoptions 只获取领养记录
 */
export const useLoadAdoptions = () => {
  const adoptions = ref<Adoption[]>([])
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const loading = ref(false)
  const { t } = useI18n()
  
  const loadAdoptions = async (page = 1, size = 10) => {
    try {
      currentPage.value = page
      pageSize.value = size
      
      // 获取领养记录
      const response = await request.get<PageResult<Adoption>>(`/user/adoptions`, {
        params: {
          current_page: page,
          per_page: size
        }
      })
      if (response.code == 200 ) {
       
        // 检查实际的响应数据结构
        const result = response.data
        
        total.value = result.total
        
        // 转换API数据格式到组件需要的格式
        const convertedAdoptions = result.records.map((item: any) => {
          return {
            id: item.aid,
            adoptDate: item.adoptionDate,
            foster_status: 'available',
            pet: {
              pid: item.pid,
              name: item.name,
              species: item.species || '',
              breed: item.breed,
              age: item.age,
              gender: item.gender,
              image: item.image ? (item.image.startsWith('http') ? item.image : `/api/images/${item.image}`) : '/src/assets/img/dog.jpg',
              petStatus: item.petStatus || ''
            },
            shelter: {
              sid: item.sid,
              sname: item.sname || '',
              location: item.location || ''
            }
          }
        })
        adoptions.value = convertedAdoptions
      }
    } catch (error) {
      ElMessage.error(t('user.loadAdoptionsFailed'))
    }
  }


  
  const handlePageChange = (page: number) => {
    loadAdoptions(page, pageSize.value)
  }
  
  const handleSizeChange = (size: number) => {
    loadAdoptions(1, size)
  }
  
  return { 
    adoptions, 
    currentPage, 
    pageSize, 
    total, 
    loading,
    loadAdoptions,
    handlePageChange,
    handleSizeChange
  }
}



/**
 * 主组合式函数
 */
export const useAdoptions = () => {
  const { browsePets } = useAdoptionNavigation()
  const { adoptions, loadAdoptions, currentPage, pageSize, total,handlePageChange, handleSizeChange } = useLoadAdoptions()

  
  const themeStore = useThemeStore()
  
  return {
    // 响应式数据
    adoptions,
    themeStore, 
    
    // 分页相关数据
    currentPage,
    pageSize,
    total,
    
    
    // 方法
    browsePets,
    loadAdoptions,
    handlePageChange,
    handleSizeChange,

  }
}
