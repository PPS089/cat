import request from '@/shared/utils/request'
import type { ApiResponse } from '@/types/api'
import { resolveAssetUrl } from '@/shared/utils/url'
import { filterAdoptions } from '@/shared/utils/adoptions'
import type { HealthAlert, PetSummary, RawHealthAlert } from '../types'

const parseResponse = async <T>(promise: Promise<any>): Promise<ApiResponse<T>> => {
  const response = await promise
  return response as ApiResponse<T>
}

const mapAlert = (raw: RawHealthAlert): HealthAlert => ({
  healthId: raw.healthId ?? raw.hid ?? 0,
  pid: Number(raw.pid) || 0,
  healthType: raw.healthType || raw.alertType || '',
  status: raw.status || 'attention',
  description: raw.description || raw.content || '',
  checkDate: raw.checkDate || raw.time || '',
  reminderTime: raw.reminderTime ?? null,
  createdAt: raw.createdAt,
  updatedAt: raw.updatedAt,
})

const mapPet = (raw: any): PetSummary => ({
  pid: raw.pid,
  name: raw.name,
  image: resolveAssetUrl(raw.image),
})

export const healthAlertsService = {
  async fetchAlerts() {
    const response = await parseResponse<RawHealthAlert[]>(request.get('/user/health-alerts'))
    return {
      code: response.code,
      data: Array.isArray(response.data) ? response.data.map(mapAlert) : [],
    }
  },

  async fetchPets() {
    const response = await parseResponse<{
      records: Array<{
        pid: number
        name: string
        image?: string
        adoptionStatus?: string
        status?: string
      }>
    }>(
      request.get('/user/adoptions', {
        params: { current_page: 1, per_page: 100, status: 'APPROVED' },
      }),
    )

    const records = filterAdoptions(response.data?.records ?? [])
    return {
      code: response.code,
      data: records.map(mapPet),
    }
  },

  createAlert(payload: {
    pid: number
    healthType: string
    checkDate: string
    reminderTime: string | null
    status: string
    description: string
  }) {
    return parseResponse(request.post('/user/health-alerts', payload))
  },

  updateAlert(id: number, payload: {
    pid: number
    healthType: string
    checkDate: string
    reminderTime: string | null
    status: string
    description: string
  }) {
    return parseResponse(request.put(`/user/health-alerts/${id}`, payload))
  },

  deleteAlert(id: number) {
    return parseResponse(request.delete(`/user/health-alerts/${id}`))
  },
}
