import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { petsMappers, petsService, mapPagination } from '../services/pets'
import type { AdoptionRecord, AdoptionTimelineEvent, PaginationState } from '../types'

const normalizeTimeline = (events: any[] = []): AdoptionTimelineEvent[] =>
  events.map(event => ({
    action: event.action,
    timestamp: event.timestamp || event.actionTime || event.time || event.createdAt || '',
    description: event.description || event.statusLabel || '',
  }))

const defaultPagination = (): PaginationState => ({
  currentPage: 1,
  pageSize: 8,
  total: 0,
  pages: 0,
})

export const useAdoptionsStore = defineStore('adoptions', () => {
  const loading = ref(false)
  const records = ref<AdoptionRecord[]>([])
  const pagination = reactive(defaultPagination())
  const statusFilter = ref<'APPROVED' | 'PENDING' | 'REJECTED' | 'ALL'>('APPROVED')
  const timeline = ref<AdoptionTimelineEvent[]>([])
  const timelineLoading = ref(false)
  const timelinePet = reactive<{ id: number | null; name: string }>({ id: null, name: '' })
  const timelineVisible = ref(false)

  const fetchRecords = async (
    page = pagination.currentPage,
    size = pagination.pageSize,
    status: 'APPROVED' | 'PENDING' | 'REJECTED' | 'ALL' = statusFilter.value,
  ) => {
    statusFilter.value = status
    loading.value = true
    try {
      const response = await petsService.fetchAdoptionRecords(page, size, status === 'ALL' ? undefined : status)
      if (response.code === 200 && response.data) {
        records.value = (response.data.records || []).map(petsMappers.toAdoptionRecord)
        Object.assign(pagination, mapPagination(response.data, pagination))
      } else {
        records.value = []
      }
    } catch (error) {
      console.error('[Adoptions] fetchRecords failed', error)
      records.value = []
    } finally {
      loading.value = false
    }
  }

const fetchTimeline = async (petId: number, petName: string) => {
    timelineLoading.value = true
    try {
      const response = await petsService.fetchAdoptionTimeline(petId)
      if (response.code === 200 && response.data) {
        timeline.value = normalizeTimeline(response.data.timeline || [])
        timelinePet.id = petId
        timelinePet.name = petName
        if (!timeline.value.length) {
          timelineVisible.value = false
          return
        }
        timelineVisible.value = true
      } else {
        timeline.value = []
      }
    } catch (error) {
      console.error('[Adoptions] fetchTimeline failed', error)
    } finally {
      timelineLoading.value = false
    }
  }

  const closeTimeline = () => {
    timelineVisible.value = false
    timeline.value = []
    timelinePet.id = null
    timelinePet.name = ''
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

  const setStatusFilter = (status: 'APPROVED' | 'PENDING' | 'REJECTED' | 'ALL') => {
    pagination.currentPage = 1
    return fetchRecords(1, pagination.pageSize, status)
  }

  return {
    loading,
    records,
    pagination,
    statusFilter,
    timeline,
    timelineLoading,
    timelinePet,
    timelineVisible,
    fetchRecords,
    fetchTimeline,
    closeTimeline,
    setPage,
    setPageSize,
    setStatusFilter,
  }
})
