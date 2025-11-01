import request from '@/utils/request'
import { ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import type { Pet, PetFormData, UpdatePetResponse } from '@/types/editpet'
import { convertBackendToFrontendGender, convertFrontendToBackendGender } from '@/utils/genderConverter'


const petFormRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false) // 提交加载状态

// 宠物表单数据 - 用于编辑页面
const petForm = ref<PetFormData>({
  name: '',
  species: '',
  breed: '',
  age: 0,
  gender: 'male' as 'male' | 'female'
})


// 提交表单 - 处理宠物信息更新或添加
export const submitForm = async (t: Function, router: any, route: any) => {
  const petId = Number(route.params.id)
  
  try {
    // 只发送后端接受的字段
    const petData: PetFormData = {
      name: petForm.value.name,
      species: petForm.value.species,
      breed: petForm.value.breed,
      age: petForm.value.age,
      gender: petForm.value.gender
    }
    
    // 判断是添加新宠物还是编辑现有宠物
    if (petId && petId > 0) {
      // 编辑现有宠物
      await updatePet(petId, petData)
      ElMessage.success(t('message.updateSuccess'))
    } else {
      // 添加新宠物 - 目前系统不支持直接添加宠物，提示用户去领养
      ElMessage.warning(t('message.pleaseAdoptPetInstead') || '请通过领养方式获取宠物，而不是直接添加')
      router.push('/user/adoptions')
      return
    }
    
    router.push('/user/pets')
  } catch (error: any) {
    console.error('操作宠物失败:', error)
    ElMessage.error(error.response?.data?.message || t('message.operationFailed') || '操作失败')
    throw error // 重新抛出错误供组件捕获
  }
}






// 更新宠物信息 - 调用PUT API
export const updatePet = async (id: number, petData: PetFormData): Promise<UpdatePetResponse> => {
  try {
    // 转换性别格式为后端接受的格式
    const backendData = {
      ...petData,
      gender: convertFrontendToBackendGender(petData.gender as 'male' | 'female')
    }
    const response = await request.put(`/pets/${id}`, backendData)
    return response.data
  } catch (error) {
    console.error('更新宠物失败:', error)
    throw error
  }
}




// 根据ID获取宠物信息 - 调用GET API
export const getPetById = async (pid: number): Promise<Pet | null> => {
    try {
      console.log(`Fetching pet with ID: ${pid}`)
      
      // 使用自定义宠物API获取完整的宠物详情
      console.log('Using custom pet details API...')
      const response = await request.get(`/pets/details/${pid}`)
      console.log('Custom pet details response:', response)
      
      if (response && response.data) {
        const customPet = response.data
        console.log('Found custom pet:', customPet)
        
        // 转换后端数据为前端格式
        const pet = {
          pid: customPet.pid,
          name: customPet.name,
          species: customPet.species,
          breed: customPet.breed,
          age: customPet.age,
          gender: convertBackendToFrontendGender(customPet.gender as '公' | '母'),
        }
        console.log('Successfully converted custom pet data:', pet)
        return pet as Pet
      }
      
      console.log(`Pet with ID ${pid} not found in custom pets`)
      return null
    } catch (error) {
      console.error('获取宠物信息失败:', error)
      throw error
    }
  }





// 获取宠物详情 - 填充表单数据
export const fetchPetDetail = async (t: Function, router: any, route: any) => {
  const petId = Number(route.params.id)
  console.log('获取宠物详情，ID:', petId)
  
  // 如果没有ID，说明是添加新宠物，不需要获取详情
  if (!petId || petId === 0) {
    console.log('添加新宠物模式，不获取宠物详情')
    // 重置表单为默认值
    petForm.value = {
      name: '',
      species: '',
      breed: '',
      age: 0,
      gender: 'male' as 'male' | 'female'
    }
    return
  }
  
  loading.value = true
  try {
    // 调用API获取宠物详情
    const pet = await getPetById(petId)
    console.log('宠物API响应:', pet)
    
    if (!pet) {
      console.error(`未找到ID为 ${petId} 的宠物`)
      ElMessage.error(t('message.petNotFound'))
      router.push('/user/adoption-pets')
      return
    }
    
    // 填充表单数据，只填充后端支持的字段
    // 转换性别格式以匹配表单期望 (male/female)
    const gender = convertBackendToFrontendGender(pet.gender as '公' | '母')
    
    petForm.value = {
      name: pet.name,
      species: pet.species,
      breed: pet.breed,
      age: pet.age,
      gender: gender
    }
    
    console.log('表单数据填充完成:', petForm.value)
  } catch (error: any) {
    console.error('获取宠物详情失败:', error)
    ElMessage.error(error.response?.data?.message || t('message.loadPetDetailFailed'))
    router.push('/user/adoption-pets')
  } finally {
    loading.value = false
  }
}

// 导出表单数据和引用，供组件使用
export { petForm, petFormRef, loading, submitLoading }