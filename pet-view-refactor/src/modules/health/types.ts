export type HealthViewMode = 'list' | 'timeline'

export type HealthType = 'VACCINE' | 'CHECKUP' | 'SURGERY' | 'DISEASE'
export type HealthStatus = 'attention' | 'expired' | 'reminded' | 'pending'

export interface PetSummary {
  pid: number
  name: string
  image?: string
}

export interface HealthAlert {
  healthId: number
  pid: number
  healthType: HealthType | string
  status: HealthStatus | string
  description: string
  checkDate: string
  reminderTime?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface HealthAlertForm {
  healthId: number | null
  pid: number | ''
  healthType: HealthType | ''
  status: HealthStatus
  description: string
  checkDate: string
  reminderTime: string | null
}

export interface HealthAlertFilters {
  petId: string
  healthType: string
  status: string
}

export interface HealthAlertStats {
  total: number
  attention: number
  expired: number
  reminded: number
}

export interface RawHealthAlert {
  healthId?: number
  hid?: number
  pid: number | string
  healthType?: string
  alertType?: string
  status?: string
  description?: string
  content?: string
  checkDate?: string
  time?: string
  reminderTime?: string | null
  createdAt?: string
  updatedAt?: string
}

