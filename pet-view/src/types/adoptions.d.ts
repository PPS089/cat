
// 领养记录时间线项
export interface AdoptionTimelineItem {
  id: number
  petId: number
  petName: string
  petBreed: string
  action: 'adopted' | 'fostered' | 'foster_ended' | 'returned'
  actionDate: string
  description: string
  shelterName?: string
  status: string
}

// 领养记录时间线响应 - 匹配后端Result格式
export interface AdoptionTimelineResponse {
    timeline: AdoptionTimelineItem[]
    total: number
    petName?: string
    petBreed?: string
}


export interface Adoption {
  id: number
  pet: Pet
  shelter: Shelter
  adoptDate: string
  status?: string
  foster_status?: string
}

