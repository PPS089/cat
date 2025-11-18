import request from '@/utils/request'
import { ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import type { Pet, PetFormData, UpdatePetResponse } from '@/types/editpet'
import { safeConvertGender, convertToPetGender } from '@/utils/genderConverter'


const petFormRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false) // 提交加载状态

// 宠物表单数据 - 用于编辑页面
const petForm = ref<PetFormData>({
  name: '',
  species: '',
  breed: '',
  age: 0,
  gender: 'male'
})


// 提交表单 - 处理宠物信息更新或添加
export const submitForm = async (t: Function, router: any, route: any) => {
  const petId = Number(route.params.id)
  
  try {
    // 创建提交数据，使用转换函数将性别值转换为后端格式
    // 使用安全转换函数处理可能的类型不匹配问题
    const genderValue = safeConvertGender(petForm.value.gender, 'frontend', 'backend')
    
    const submitData = {
      name: petForm.value.name,
      species: petForm.value.species,
      breed: petForm.value.breed,
      age: petForm.value.age,
      gender: genderValue
    }
    
    // 判断是添加新宠物还是编辑现有宠物
    if (petId && petId > 0) {
      // 编辑现有宠物
      await updatePet(petId, submitData)
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
export const updatePet = async (id: number, petData: any): Promise<UpdatePetResponse> => {
  try {
    // 直接发送已经转换好的数据，不再进行重复转换
    const response = await request.put(`/pets/${id}`, petData)
    return response.data
  } catch (error) {
    console.error('更新宠物失败:', error)
    throw error
  }
}




// 根据ID获取宠物信息 - 调用GET API
export const getPetById = async (pid: number): Promise<Pet | null> => {
    try {
      // 使用自定义宠物API获取完整的宠物详情
      const response = await request.get(`/pets/details/${pid}`)
      
      if (response && response.data) {
        const customPet = response.data
        
        // 使用转换函数确保性别值符合PetGender类型
        const genderValue = convertToPetGender(customPet.gender)
        
        // 转换后端数据为前端格式
        const pet = {
          pid: customPet.pid,
          name: customPet.name,
          species: customPet.species,
          breed: customPet.breed,
          age: customPet.age,
          gender: genderValue,
        }
        return pet as Pet
      }
      
      return null
    } catch (error) {
      throw error
    }
  }





// 获取宠物详情 - 填充表单数据
export const fetchPetDetail = async (t: Function, router: any, route: any) => {
  const petId = Number(route.params.id)
  
  // 如果没有ID，说明是添加新宠物，不需要获取详情
  if (!petId || petId === 0) {
    // 重置表单为默认值
    petForm.value = {
      name: '',
      species: '',
      breed: '',
      age: 0,
      gender: 'male' // 默认值为male
    }
    return
  }
  
  loading.value = true
  try {
    // 调用API获取宠物详情
    const pet = await getPetById(petId)
    
    if (!pet) {
      ElMessage.error(t('message.petNotFound'))
      router.push('/user/adoption-pets')
      return
    }
    
    // 填充表单数据，只填充后端支持的字段
    // 使用转换函数确保性别值符合PetGender类型
    const gender = convertToPetGender(pet.gender)
    
    petForm.value = {
      name: pet.name,
      species: pet.species,
      breed: pet.breed,
      age: pet.age,
      gender: gender
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || t('message.loadPetDetailFailed'))
    router.push('/user/adoption-pets')
  } finally {
    loading.value = false
  }
}

// 导出表单数据和引用，供组件使用
export { petForm, petFormRef, loading, submitLoading }