import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { dispatchNavStatsRefresh } from '@/shared/utils/events'
import { petsMappers, petsService, mapPagination } from '../services/pets'
import type { AdoptedPet, PaginationState, Shelter, FosterRecord } from '../types'

const createPagination = (): PaginationState => ({
  currentPage: 1,
  pageSize: 6,
  total: 0,
  pages: 0,
})

export const useMyPetsStore = defineStore('my-pets', () => {
  const { t } = useI18n()
  const loading = ref(false)
  const list = ref<AdoptedPet[]>([])
  const pagination = reactive(createPagination())
  const shelters = ref<Shelter[]>([])
  const sheltersLoading = ref(false)

  const hasPets = computed(() => list.value.length > 0)

  const fetchShelters = async () => {
    if (shelters.value.length) return shelters.value
    sheltersLoading.value = true
    try {
      const response = await petsService.fetchShelters()
      if (response.code === 200) {
        const shelterList = (response.data && Array.isArray(response.data) ? response.data : [])
        shelters.value = shelterList.map((shelter: Shelter) => ({
          sid: shelter.sid,
          shelterName: shelter.shelterName,
          shelterAddress: shelter.shelterAddress,
        }))
      }
    } catch (error) {
      console.error('[MyPets] fetchShelters failed', error)
    } finally {
      sheltersLoading.value = false
    }
    return shelters.value
  }

  const fetchPets = async (page = pagination.currentPage, pageSize = pagination.pageSize) => {
    loading.value = true
    try {
      const response = await petsService.fetchAdoptedPets(page, pageSize)
      const fosterResp = await petsService.fetchFosters(1, 200)
      const fosterMap = new Map<number, FosterRecord>()
      if (fosterResp.code === 200 && fosterResp.data?.records) {
        fosterResp.data.records.forEach((rec: any) => {
          const mapped = petsMappers.toFosterRecord(rec)
          if (mapped?.pet?.pid) {
            const existing = fosterMap.get(mapped.pet.pid)
            const currentTime = mapped.createTime ? new Date(mapped.createTime).getTime() : 0
            const existingTime = existing?.createTime ? new Date(existing.createTime).getTime() : 0
            if (!existing || currentTime > existingTime) {
              fosterMap.set(mapped.pet.pid, mapped)
            }
          }
        })
      }

      if (response.code === 200 && response.data) {
        const mapped = (response.data.records || []).map(petsMappers.toAdoptedPet)
        const withFoster = mapped.map(pet => {
          const foster = fosterMap.get(pet.pid)
          const currentStatus = list.value.find(item => item.pid === pet.pid)?.status
          const adoptionFosterStatus = (pet.fosterStatus || '').toString().toUpperCase()

          if (foster) {
            const fosterStatus = (foster.status || '').toString().toUpperCase()
            if (fosterStatus === 'PENDING') return { ...pet, status: 'FOSTER_PENDING' }
            if (fosterStatus === 'APPROVED' || fosterStatus === 'ONGOING') return { ...pet, status: 'FOSTERING' }
          } else if (adoptionFosterStatus === 'PENDING') {
            return { ...pet, status: 'FOSTER_PENDING' }
          } else if (adoptionFosterStatus === 'APPROVED' || adoptionFosterStatus === 'ONGOING') {
            return { ...pet, status: 'FOSTERING' }
          } else if (currentStatus === 'FOSTER_PENDING') {
            // 若服务器未返回最新寄养记录，但本地正处于待审核，保持禁用状态
            return { ...pet, status: 'FOSTER_PENDING' }
          }
          return pet
        })
        // “我的宠物”仅展示已领养成功的宠物，并按 pid 去重（历史领养记录不应导致重复卡片）
        const approved = withFoster.filter(
          pet =>
            (pet.adoptionStatus || pet.status) === 'APPROVED' ||
            pet.status === 'FOSTER_PENDING' ||
            pet.status === 'FOSTERING',
        )
        const unique = new Map<number, AdoptedPet>()
        approved.forEach(pet => {
          const existing = unique.get(pet.pid)
          if (!existing) {
            unique.set(pet.pid, pet)
            return
          }
          const currentTime = pet.adoptionDate ? new Date(pet.adoptionDate).getTime() : 0
          const existingTime = existing.adoptionDate ? new Date(existing.adoptionDate).getTime() : 0
          if (currentTime >= existingTime) {
            unique.set(pet.pid, pet)
          }
        })
        list.value = Array.from(unique.values())
        Object.assign(pagination, mapPagination(response.data, pagination))
      } else {
        list.value = []
        pagination.total = 0
      }
    } catch (error) {
      console.error('[MyPets] fetchPets failed', error)
      list.value = []
      pagination.total = 0
    } finally {
      loading.value = false
    }
  }

  const getTodayLocalDate = () => {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const startFoster = async (petId: number, shelterId: number) => {
    try {
      const startDate = getTodayLocalDate()
      const payload = {
        shelterId,
        startDate,
      }
      const response = await petsService.startFoster(petId, payload)
      if (response.code === 200) {
        ElMessage.success(t('fosterRequestSuccess') || t('fosterSuccess'))
        list.value = list.value.map(pet =>
          pet.pid === petId ? { ...pet, status: 'FOSTER_PENDING' } : pet,
        )
        dispatchNavStatsRefresh({ reason: 'start-foster' })
        await fetchPets()
        return true
      }
    } catch (error: any) {
      console.error('[MyPets] startFoster failed', error)
    }
    return false
  }

  const endFoster = async (petId: number) => {
    try {
      const response = await petsService.endFoster(petId)
      if (response.code === 200) {
        ElMessage.success(t('message.endFosterSuccess'))
        list.value = list.value.map(pet =>
          pet.pid === petId ? { ...pet, status: 'ADOPTED' } : pet,
        )
        dispatchNavStatsRefresh({ reason: 'end-foster' })
        await fetchPets()
        return true
      }
    } catch (error) {
      console.error('[MyPets] endFoster failed', error)
    }
    return false
  }

  const setPage = (page: number) => {
    pagination.currentPage = page
    return fetchPets()
  }

  const setPageSize = (size: number) => {
    pagination.pageSize = size
    pagination.currentPage = 1
    return fetchPets(1, size)
  }

  const reset = () => {
    list.value = []
    Object.assign(pagination, createPagination())
  }

  return {
    loading,
    list,
    pagination,
    shelters,
    sheltersLoading,
    hasPets,
    fetchPets,
    fetchShelters,
    startFoster,
    endFoster,
    setPage,
    setPageSize,
    reset,
  }
})
