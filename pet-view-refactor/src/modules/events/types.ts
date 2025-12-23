export type ViewMode = 'grid' | 'list' | 'timeline'

export interface RecordFilters {
  selectedPet: string
  selectedEventType: string
  selectedMood: string
  selectedDate: string
}

export interface RecordForm {
  recordId: number | null
  pid: string
  eventType: string
  mood: string
  description: string
  location: string
  recordTime: string
}

export interface PetSummary {
  pid: number
  name: string
}

export interface MediaEntry {
  id: number
  url: string
  type: 'image' | 'video'
  name?: string
  createdAt?: string
  updatedAt?: string
}

export interface RecordEvent {
  id: number
  petId: number
  petName: string
  eventType: string
  description: string
  recordTime: string
  mood?: string
  location?: string
  createdAt?: string
  updatedAt?: string
  media: MediaEntry[]
}

export interface FilePreview {
  file: File
  url: string
  type: 'image' | 'video'
}
