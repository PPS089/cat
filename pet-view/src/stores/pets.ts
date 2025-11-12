import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import type { Pet } from '@/types/mypets'
import type { PageResult } from '@/types/api'

// 统一使用模块类型定义

export const usePetsStore = defineStore('pets', () => {
  // 状态
  const pets = ref<Pet[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 方法
  const loadPets = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await request.get<PageResult<Pet>>('/user/adoptions', {
        params: { current_page: 1, per_page: 100 }
      })
      const page = response.data
      pets.value = (page?.records || []) as Pet[]
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载宠物列表失败'
      pets.value = []
    } finally {
      loading.value = false
    }
  }

  const getPetById = (pid: number): Pet | undefined => {
    return pets.value.find(pet => pet.pid === pid)
  }

  const getPetName = (pid: number): string => {
    const pet = getPetById(pid)
    return pet ? pet.name : '未知宠物'
  }

  const getPetSpecies = (pid: number): string => {
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
