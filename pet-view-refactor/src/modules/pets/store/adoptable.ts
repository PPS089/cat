import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { dispatchNavStatsRefresh } from '@/shared/utils/events'
import { petsMappers, petsService, mapPagination } from '../services/pets'
import type { AdoptablePet, PaginationState } from '../types'

interface Filters {
  species?: string
  breed?: string
  gender?: string
  minAge?: number
  maxAge?: number
}

const defaultPagination = (): PaginationState => ({
  currentPage: 1,
  pageSize: 12,
  total: 0,
  pages: 0,
})

export const useAdoptablePetsStore = defineStore('adoptable-pets', () => {
  const { t } = useI18n()
  const loading = ref(false)
  const items = ref<AdoptablePet[]>([])
  const pagination = reactive(defaultPagination())
  const filters = reactive<Filters>({})

  const fetchList = async (page = pagination.currentPage, pageSize = pagination.pageSize) => {
    loading.value = true
    try {
      const response = await petsService.fetchAdoptablePets(page, pageSize, filters)
      if (response.code === 200 && response.data) {
        items.value = (response.data.records || []).map(petsMappers.toAdoptablePet)
        Object.assign(pagination, mapPagination(response.data, pagination))
      } else {
        items.value = []
      }
    } catch (error) {
      console.error('[AdoptablePets] fetchList failed', error)
      items.value = []
    } finally {
      loading.value = false
    }
  }

  const adoptPet = async (pet: AdoptablePet) => {
    try {
      await ElMessageBox.confirm(`${t('message.confirmAdoption')} ${pet.name}?`, t('message.confirm'), {
        confirmButtonText: t('message.confirmButton'),
        cancelButtonText: t('message.cancelButton'),
        type: 'warning',
      })
      const response = await petsService.adoptPet(pet.pid)
      if (response.code === 200) {
        ElMessage.success(t('message.adoptionRequestSuccess') || t('message.adoptionSuccess'))
        dispatchNavStatsRefresh({ reason: 'adopt-pet' })
        await fetchList()
        return true
      }
    } catch (error: any) {
      if (error === 'cancel') {
        return false
      }
      console.error('[AdoptablePets] adopt pet failed', error)
    }
    return false
  }

  const setPage = (page: number) => {
    pagination.currentPage = page
    return fetchList()
  }

  const setPageSize = (size: number) => {
    pagination.pageSize = size
    pagination.currentPage = 1
    return fetchList(1, size)
  }

  const setFilters = (newFilters: Partial<Filters>) => {
    Object.assign(filters, newFilters)
    pagination.currentPage = 1
    return fetchList(1, pagination.pageSize)
  }

  const clearFilters = () => {
    Object.keys(filters).forEach(key => {
      delete (filters as any)[key]
    })
    pagination.currentPage = 1
    return fetchList(1, pagination.pageSize)
  }

  return {
    loading,
    items,
    pagination,
    filters,
    fetchList,
    adoptPet,
    setPage,
    setPageSize,
    setFilters,
    clearFilters,
  }
})