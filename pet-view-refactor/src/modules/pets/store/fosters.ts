import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { petsMappers, petsService, mapPagination } from '../services/pets'
import type { FosterRecord, PaginationState } from '../types'

const defaultPagination = (): PaginationState => ({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  pages: 0,
})

export const useFostersStore = defineStore('fosters', () => {
  const { t } = useI18n()
  const loading = ref(false)
  const records = ref<FosterRecord[]>([])
  const pagination = reactive(defaultPagination())
  const statusFilter = ref<'ONGOING' | 'PENDING' | 'REJECTED' | 'ALL'>('ONGOING')

  const fetchRecords = async (
    page = pagination.currentPage,
    size = pagination.pageSize,
    status: 'ONGOING' | 'PENDING' | 'REJECTED' | 'ALL' = statusFilter.value,
  ) => {
    statusFilter.value = status
    loading.value = true
    try {
      const response = await petsService.fetchFosters(page, size, status === 'ALL' ? undefined : status)
      if (response.code === 200 && response.data) {
        records.value = (response.data.records || []).map(petsMappers.toFosterRecord)
        Object.assign(pagination, mapPagination(response.data, pagination))
      } else {
        records.value = []
      }
    } catch (error) {
      console.error('[Fosters] fetchRecords failed', error)
      records.value = []
    } finally {
      loading.value = false
    }
  }

  const deleteRecord = async (id: number) => {
    try {
      await ElMessageBox.confirm(t('user.confirmDeleteFoster'), t('user.confirmDeleteFosterTitle'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      })
      const response = await petsService.deleteFoster(id)
      if (response.code === 200) {
        ElMessage.success(t('user.fosterDeleted'))
        records.value = records.value.filter(record => record.id !== id)
        if (pagination.total > 0) {
          pagination.total = Math.max(0, pagination.total - 1)
        }
        const shouldLoadPrevPage = records.value.length === 0 && pagination.currentPage > 1
        const targetPage = shouldLoadPrevPage ? pagination.currentPage - 1 : pagination.currentPage
        pagination.currentPage = targetPage
        await fetchRecords(targetPage, pagination.pageSize)
        return true
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('[Fosters] deleteRecord failed', error)
      }
    }
    return false
  }

  const setPage = (page: number) => {
    pagination.currentPage = page
    return fetchRecords()
  }

  const setPageSize = (size: number) => {
    pagination.pageSize = size
    pagination.currentPage = 1
    return fetchRecords(1, size)
  }

  const setStatusFilter = (status: 'ONGOING' | 'PENDING' | 'REJECTED' | 'ALL') => {
    pagination.currentPage = 1
    return fetchRecords(1, pagination.pageSize, status)
  }

  return {
    loading,
    records,
    pagination,
    fetchRecords,
    deleteRecord,
    setPage,
    setPageSize,
    statusFilter,
    setStatusFilter,
  }
})
