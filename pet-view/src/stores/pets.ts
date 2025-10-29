import { defineStore } from 'pinia'
import { ref } from 'vue'

// 宠物接口定义
export interface Pet {
  pid: string
  name: string
  species: string
  breed?: string
  age?: number
  weight?: number
  gender?: 'male' | 'female'
  color?: string
  description?: string
  healthStatus?: string
  adoptionStatus?: 'available' | 'pending' | 'adopted'
  fosterStatus?: 'available' | 'pending' | 'fostered'
  image?: string
  createdAt?: string
  updatedAt?: string
}

export const usePetsStore = defineStore('pets', () => {
  // 状态
  const pets = ref<Pet[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 方法
  const loadPets = async () => {
    try {
      loading.value = true
      error.value = null
      
      // 从API获取用户领养的宠物数据
      const response = await fetch('/api/user/adoptions', {
        credentials: 'include', // 包含cookies用于会话验证
        headers: {
          'Content-Type': 'application/json',
        }
      })
      const result = await response.json()
      
      if (result.success) {
        // 转换API数据格式到Pet接口格式
        pets.value = result.data.adoptions.map((adoption: any) => ({
          pid: adoption.pet.id.toString(), // 确保pid是字符串
          name: adoption.pet.name,
          species: adoption.pet.species,
          breed: adoption.pet.breed,
          age: adoption.pet.age,
          gender: adoption.pet.gender,
          healthStatus: '健康', // 领养的宠物默认为是健康的
          adoptionStatus: 'adopted', // 用户领养的宠物
          fosterStatus: 'available', // 默认值
          description: `${adoption.pet.name} - ${adoption.pet.species} ${adoption.pet.breed || ''}`,
          createdAt: adoption.adopt_date,
          updatedAt: new Date().toISOString()
        }))
      } else {
        // 如果用户没有登录或没有领养记录，显示空列表
        if (result.message === '请先登录') {
          // 用户未登录，无法获取领养的宠物
          pets.value = []
        } else {
          throw new Error(result.message || '获取宠物数据失败')
        }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载宠物列表失败'
      // 如果API调用失败，使用空数组
      pets.value = []
    } finally {
      loading.value = false
    }
  }

  const getPetById = (pid: string): Pet | undefined => {
    return pets.value.find(pet => pet.pid === pid)
  }

  const getPetName = (pid: string): string => {
    const pet = getPetById(pid)
    return pet ? pet.name : '未知宠物'
  }

  const getPetSpecies = (pid: string): string => {
    const pet = getPetById(pid)
    return pet ? pet.species : '未知'
  }

  // 初始化
  const init = async () => {
    await loadPets()
  }

  return {
    // 状态
    pets,
    loading,
    error,
    
    // 方法
    loadPets,
    getPetById,
    getPetName,
    getPetSpecies,
    init
  }
})