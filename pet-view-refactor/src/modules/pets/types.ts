export interface Shelter {
  sid: number
  shelterName: string
  shelterAddress: string
}

export interface SpeciesItem {
  id: number
  name: string
  description?: string | null
}

export interface BreedItem {
  id: number
  speciesId: number
  name: string
  description?: string | null
}

export interface AdoptedPet {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: string
  status: string
  adoptionStatus?: string
  fosterStatus?: string
  fosterId?: number
  image?: string
  adoptionDate?: string
  shelterName?: string
  shelterAddress?: string
}

export interface AdoptablePet {
  pid: number
  name: string
  species: string
  breed: string
  age: number
  gender: string
  shelterName: string
  shelterAddress: string
  status: string
  image?: string
}

export interface AdoptionRecord {
  id: number
  adoptDate?: string
  adoptionStatus?: string
  reviewNote?: string
  pet: AdoptedPet
  shelter: {
    sid?: number
    sname?: string
    location?: string
  }
}

export interface AdoptionTimelineEvent {
  action: string
  timestamp: string
  description?: string
}

export interface AdoptionTimelineResponse {
  timeline: AdoptionTimelineEvent[]
}

export interface FosterRecord {
  id: number
  pet: AdoptedPet
  shelter?: {
    sid?: number
    name?: string
    location?: string
  }
  createTime: string
  updateTime?: string | null
  status: 'PENDING' | 'APPROVED' | 'ONGOING' | 'COMPLETED' | 'REJECTED'
  reviewNote?: string
}

export interface PetDetail {
  pid: number
  name: string
  speciesId: number
  species: string
  speciesCode?: string
  breedId: number
  breed: string
  age: number
  ageYears?: number
  gender: string
  sex?: string
  image?: string
  shelterId?: number
  shelterName?: string
  shelterAddress?: string
  status?: string
  description?: string
}

export interface PetFormPayload {
  name: string
  speciesId: number
  breedId: number
  age: number
  gender: string
  shelterId?: number
  imageUrl?: string
}

export interface PaginationState {
  currentPage: number
  pageSize: number
  total: number
  pages: number
}
