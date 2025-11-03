import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import type { AdoptionResponse, EndFosterResponse, Pet, Shelter } from '@/types/mypets'

export function usePets() {
  const router = useRouter()
  const { t } = useI18n()
  
  const pets = ref<Pet[]>([])
  const shelters = ref<Shelter[]>([])
  const showFosterDialog = ref(false)
  const selectedPet = ref<Pet | null>(null)
  const selectedShelter = ref<number | null>(null)
  
  
  // 分页相关状态
  const currentPage = ref(1)
  const pageSize = ref(5)
  const total = ref(0)
  const totalPages = ref(0)
  // t 函数已在函数顶部声明，组件可以直接使用

  const addPet = () => {
    router.push('/user/adoption-pets')
  }


  const editPet = (id: number) => {
    router.push(`pets/edit/${id}`)
  }

  // 开始寄养
  const startFoster = async (pet: Pet) => {
    try {
      
      selectedPet.value = pet
      showFosterDialog.value = true
      selectedShelter.value = null // 重置选择
      
      // 加载收容所列表
      const response = await request.get('/shelters')
      
      // 处理 shelters API 响应 
      if (response.code === 200) {
       const sheltersData = response.data as Shelter[]
     
      
      // 转换数据结构 - API返回的是 sid 字段，但模板期望 id 字段
      shelters.value = sheltersData.map((shelter : Shelter) => ({
        sid: shelter.sid,
        shelterName: shelter.shelterName,
        shelterAddress: shelter.shelterAddress,
      }))
      ElMessage.success(t('common.pleaseSelectShelter'))
    }
    } catch (error: any) {
        ElMessage.error(t('getShelterListFailed'))
    }
  }

  // 确认开始寄养
  const confirmStartFoster = async () => {
    if (!selectedShelter.value) {
      ElMessage.warning(t('common.pleaseSelectShelter'))
      return
    }
    
    if (!selectedPet.value) {
      ElMessage.warning(t('petNotSelected'))
      return
    }
    
    try {
      // 使用新的寄养API端点
      const requestData = {
        shelterId: selectedShelter.value,
        startDate: new Date().toISOString().split('T')[0] 
      }
      
      const response = await request.post(
        `/pets/${selectedPet.value.pid}/foster`,
        requestData
      )
      
      // 检查响应状态
      if (response.code === 200) {
        ElMessage.success(t('fosterSuccess'))
        
        // 更新本地状态
        const targetPetId = selectedPet.value.pid
        if (targetPetId) {
          pets.value = pets.value.map(pet => 
            pet.pid === targetPetId 
              ? { ...pet, isFostering: true }
              : pet
          )
        }
        
        // 重置表单和状态
        showFosterDialog.value = false
        selectedPet.value = null
        selectedShelter.value = null
        
        // 刷新数据
        await fetchPets()
      } else {
        ElMessage.error(t('fosterFailed'))
      }
    } catch (error: any) {
        ElMessage.error(error.response?.data?.msg || t('fosterFailed'))
    }
  }

  
  // 分页事件处理函数
  const handlePageChange = async (page: number) => {
    console.log('分页切换请求 - 页码:', page, '当前页:', currentPage.value, '总页数:', totalPages.value)
    
    if (page < 1 || (totalPages.value > 0 && page > totalPages.value)) {
      console.log('分页切换被拒绝 - 页码超出范围')
      return
    }
    
    // 允许切换到相同页码（用于刷新）
    await fetchPets(page, pageSize.value)
  }
  
  const handlePageSizeChange = async (size: number) => {
    console.log('每页数量变更 - 新尺寸:', size)
    // 重置到第一页
    await fetchPets(1, size)
  }

  // 结束寄养
  const endFoster = async (pet: Pet) => {
    try {
      
      await ElMessageBox.confirm(
        t('message.confirmEndFoster'),
        t('message.warning'),
        {
          confirmButtonText: t('message.confirm'),
          cancelButtonText: t('message.cancel'),
          type: 'warning'
        }
      )
      
      // 结束寄养API
      console.log(t('callingSimplifiedEndFosterApi'), pet.pid)
      const response = await request.post<EndFosterResponse>(`/pets/${pet.pid}/foster/end`)
      
      // 检查响应状态 
      if (response.code === 200) {
        ElMessage.success(t('message.endFosterSuccess'))
        
        // 更新本地状态 - 将寄养状态设置为false
        const targetPetId = pet.pid
        if (targetPetId) {
          pets.value = pets.value.map(p => 
            p.pid === targetPetId 
              ? { ...p, isFostering: false }
              : p
          )
        }
        
      // 刷新数据以获取最新的寄养状态
      await fetchPets()
      }
      
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error(t('endFosterApiFailed'), error)
          ElMessage.error( t('endFosterFailed'))
      }
    }
  }

 

  // 获取宠物列表
  const fetchPets = async (page = currentPage.value, size = pageSize.value) => {
    
    try {
      console.log(`开始获取用户领养宠物数据 - 页码: ${page}, 每页数量: ${size}`)
      
      // 参数验证
      if (page < 1) {
        console.warn('页码不能小于1，自动调整为1')
        page = 1
      }
      
      if (size < 1) {
        console.warn('每页数量不能小于1，自动调整为5')
        size = 5
      }
      
      // 使用新的API直接获取用户已领养的宠物（带分页）
      const response = await request.get<AdoptionResponse>(`/user/adoptions?current_page=${page}&per_page=${size}`)
      
      console.log('获取到用户领养宠物分页结果:', response)
      
      
      const responseData = response.data
      
      console.log('响应数据结构:', JSON.stringify(responseData, null, 2))
      
      if (!responseData.records || responseData.records.length === 0) {
        // 用户还没有领养任何宠物，显示空状态
        console.log('用户还没有领养任何宠物')
        pets.value = []
        total.value = responseData.total || 0
        totalPages.value = responseData.pages || 0
      } else {
        // 使用新的API获取的数据
        pets.value = responseData.records || []
        console.log('获取到用户领养宠物数量:', responseData.records?.length || 0)
        // 使用API返回的真实分页数据
        total.value = responseData.total || 0
        totalPages.value = responseData.pages || 0
      }
      
      // 更新当前页码和每页数量
      currentPage.value = page
      pageSize.value = size
      
      console.log(`分页状态更新完成 - 当前页: ${currentPage.value}, 每页: ${pageSize.value}, 总数: ${total.value}, 总页数: ${totalPages.value}`)
      
    } catch (error: any) {
      console.error('获取宠物数据失败:', error)
      if (error.name === 'AbortError') {
        return
      }
      
      // 重置数据
      pets.value = []
      total.value = 0
      totalPages.value = 0
      
      // 显示错误消息
      ElMessage.error(t('getPetDataFailed') || '获取宠物数据失败')
    } 
  }

  return {
    // 响应式状态
    pets,
    shelters,
    showFosterDialog,
    selectedShelter,
    
    // 分页状态
    currentPage,
    pageSize,
    total,
    
    // i18n 函数
    t,
    
    // 方法
    addPet,
    editPet,
    startFoster,
    endFoster,
    confirmStartFoster,
    fetchPets,
    handlePageChange,
    handlePageSizeChange
  }
}




