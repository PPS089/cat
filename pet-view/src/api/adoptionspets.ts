import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import request from '@/utils/request'
import type { Pet, PetFilter } from '@/types/mypets'

// 导航函数
export const useAddNavigation = () => {
  const { t } = useI18n() 
  const router = useRouter()
  
  // 查看宠物详情 - 直接跳转到详情页，让详情页自己获取数据
  const viewPetDetail = (pid: number) => {
    if (pid) {
      try {
        // 直接跳转到详情页，pet-detail.vue 获取数据
        router.push({
          name: 'user-pet-detail',
          params: { id: pid }
        })
      } catch (error: any) {
        ElMessage.error(t('message.loadPetDetailFailed') || '加载宠物详情失败')
        console.error('跳转到宠物详情页失败:', error)
      }
    } else {
      ElMessage.error(t('message.petInfoIncomplete'))
      console.error('尝试查看宠物详情时，宠物ID不完整', pid)
    }
  }
  
  return { viewPetDetail }
}

// 宠物数据管理函数
export const usePetData = () => {
  const loading = ref<boolean>(false)
  const availablePets = ref<Pet[]>([])
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(12)
  const total = ref<number>(0)
  const { t } = useI18n()
  
  // 筛选条件
  const breed = ref<string>('')
  const gender = ref<string>('')
  const minAge = ref<number | null>(null)
  const maxAge = ref<number | null>(null)
  
  // 获取可领养的宠物列表 - 查询所有未被领养的宠物
  const fetchAvailablePets = async () => {
    loading.value = true
    try {
      // 构建查询参数
      const params: any = {
        currentPage: currentPage.value,
        pageSize: pageSize.value
      }
      
      // 添加筛选条件
      if (breed.value) params.breed = breed.value
      if (gender.value) params.gender = gender.value
      if (minAge.value !== null) params.minAge = minAge.value
      if (maxAge.value !== null) params.maxAge = maxAge.value
      
      // 使用数据库查询API获取所有未领养宠物
      const response = await request.get('/pets/info/available', {
        params
      })
      
      
      if (response.code === 200) {
        // 使用分页数据
        const pageData = response.data
        
        // 将宠物数据转换为前端格式
        availablePets.value = pageData.records.map((pet: any) => {
          return {
            pid: pet.pid ,
            name: pet.name,
            species: pet.species ,
            breed: pet.breed || t('message.unknown'),
            age: pet.age,
            gender: pet.gender === '公' ? 'male' : (pet.gender === '母' ? 'female' : pet.gender),
            status: pet.status === 'UNADOPTED' ? 'available' : pet.status,
            shelterName: pet.shelterName || t('message.unknownShelter'),
            shelterAddress: pet.shelterAddress || t('message.unknownAddress')
          }
        })
        total.value = pageData.total
        console.log('获取到未领养宠物数量:', availablePets.value.length, '总数:', pageData.total)
      } else {
        ElMessage.error(t('message.dataLoadFailed'))
      }
    } catch (error: any) {
      console.error('获取可领养宠物失败:', error)
      ElMessage.error(t('message.dataLoadFailedRetry'))
    } finally {
      loading.value = false
    }
  }
  
  // 重置筛选条件
  const resetFilters = () => {
    breed.value = ''
    gender.value = ''
    minAge.value = null
    maxAge.value = null
    currentPage.value = 1
    fetchAvailablePets()
  }
  
  // 应用筛选条件
  const applyFilters = () => {
    currentPage.value = 1
    fetchAvailablePets()
  }
  
  // 分页处理
  const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchAvailablePets()
  }
  
  const handleSizeChange = (size: number) => {
    pageSize.value = size
    currentPage.value = 1 // 重置到第一页
    fetchAvailablePets()
  }
  
  return {
    loading,
    availablePets,
    currentPage,
    pageSize,
    total,
    breed,
    gender,
    minAge,
    maxAge,
    fetchAvailablePets,
    resetFilters,
    applyFilters,
    handlePageChange,
    handleSizeChange
  }
}

// 领养功能函数
export const useAdoptPet = (availablePets: Ref<Pet[]> | null = null) => {
  const { t } = useI18n()
  const { fetchAvailablePets } = usePetData()
  
  // 领养宠物
  const adoptPet = async (pet: Pet) => {
    try {
      await ElMessageBox.confirm(
        `${t('message.confirmAdoption')} ${pet.name}？`,
        t('message.confirmAdoption'),
        {
          confirmButtonText: t('message.confirmButton'),
          cancelButtonText: t('message.cancelButton'),
          type: 'warning'
        }
      )
      
      const response = await request.post('/pets/adopt', null, {
        params: { petId: pet.pid }
      })
      
      // response 已经是解析后的数据，直接检查 code
      if (response.code === 200) {
        ElMessage.success(t('message.adoptionSuccess'))
        // 重新获取宠物列表
        await fetchAvailablePets()
        // 如果提供了availablePets引用，立即更新本地列表以刷新UI
        if (availablePets && availablePets.value) {
          availablePets.value = availablePets.value.filter((p) => p.pid !== pet.pid)
        }

        return true
      } else {
        ElMessage.error(response.message || t('message.adoptFailed'))
        return false
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        // 处理特定错误情况
        const errorMessage = error.response?.data?.message
        if (errorMessage?.includes('已被领养') || errorMessage?.includes('已有领养记录')) {
          ElMessage.error(t('message.petAlreadyAdopted') || '该宠物已被其他人领养，页面将自动刷新')
          // 自动刷新宠物列表
          await fetchAvailablePets()
        } else {
          ElMessage.error(errorMessage || t('message.adoptFailed'))
        }
        return false
      }
      return false
    }
  }
  
  return { adoptPet }
}



// 主要的useAdd组合式函数
export const useAdd = () => {
  const { viewPetDetail } = useAddNavigation()
  const { loading, availablePets, currentPage, pageSize, total, breed, gender, minAge, maxAge, fetchAvailablePets, resetFilters, applyFilters, handlePageChange, handleSizeChange } = usePetData()
  const { adoptPet } = useAdoptPet(availablePets)
  const themeStore = useThemeStore()
  
  return {
    viewPetDetail,
    loading,
    availablePets,
    currentPage,
    pageSize,
    total,
    breed,
    gender,
    minAge,
    maxAge,
    fetchAvailablePets,
    resetFilters,
    applyFilters,
    handlePageChange,
    handleSizeChange,
    adoptPet,
    themeStore
  }
}

