import { defineStore } from 'pinia'
import request from '@/shared/utils/request'

interface PageResult<T> {
  records: T[]
  total: number
}

export const useNavStatsStore = defineStore('nav-stats', {
  state: () => ({
    loading: false,
    petCount: 0,
    adoptionCount: 0,
    fosterCount: 0,
    healthAlertCount: 0,
    eventsCount: 0,
  }),
  actions: {
    async fetchAll() {
      if (this.loading) return
      this.loading = true
      try {
        const [adoptions, fosters, healthAlerts, events] = await Promise.all([
          request.get<PageResult<any>>('/user/adoptions', { params: { current_page: 1, per_page: 100 } }),
          request.get<PageResult<any>>('/user/fosters', { params: { current_page: 1, per_page: 100 } }),
          request.get<any[]>('/user/health-alerts'),
          request.get<any[]>('/events'),
        ])
        const rawAdoptions = adoptions?.data?.records || []
        const getStatus = (record: any) => (record?.adoptionStatus ?? record?.status ?? '').toString().toUpperCase()
        const approvedAdoptions = rawAdoptions.filter(record => getStatus(record) === 'APPROVED')

        // “领养记录”角标：统计当前用户所有申请记录（包含已拒绝/失败等历史记录）
        this.adoptionCount = adoptions?.data?.total ?? rawAdoptions.length
        this.fosterCount = fosters?.data?.records?.length ?? fosters?.data?.total ?? 0
        this.healthAlertCount = Array.isArray(healthAlerts?.data) ? healthAlerts.data.length : 0
        // “我的宠物”仅统计已通过领养的记录；不要把“领养申请(待审核等)”算进去
        this.petCount = approvedAdoptions.length
        this.eventsCount = Array.isArray(events?.data) ? events.data.length : 0
      } catch (error) {
        console.error('[NavStats] fetchAll error', error)
      } finally {
        this.loading = false
      }
    },
    reset() {
      this.petCount = 0
      this.adoptionCount = 0
      this.fosterCount = 0
      this.healthAlertCount = 0
      this.eventsCount = 0
    },
  },
})
