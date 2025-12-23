import request from '@/shared/utils/request'
import { resolveAssetUrl } from '@/shared/utils/url'
import type { ApiResponse } from '@/types/api'
import type {
  AdoptablePet,
  AdoptedPet,
  AdoptionRecord,
  AdoptionTimelineResponse,
  BreedItem,
  FosterRecord,
  PaginationState,
  PetDetail,
  PetFormPayload,
  Shelter,
  SpeciesItem,
} from '../types'

interface PageResult<T> {
  records: T[]
  total: number
  pages?: number
  current?: number
  size?: number
}

const toPageParams = (page: number, pageSize: number) => ({
  current_page: page,
  per_page: pageSize,
})

const resolve = async <T>(promise: Promise<any>): Promise<ApiResponse<T>> => {
  const response = await promise
  return response as unknown as ApiResponse<T>
}

const normalizeGender = (value?: string | null) => {
  const g = (value || '').trim()
  if (g === '雌') return '雌'
  if (g === '雄') return '雄'
  return ''
}

export const petsService = {
  fetchSpecies() {
    return resolve<SpeciesItem[]>(request.get<ApiResponse<SpeciesItem[]>>('/pets/info/species'))
  },
  fetchBreedsBySpecies(speciesId: number) {
    return resolve<BreedItem[]>(
      request.get<ApiResponse<BreedItem[]>>(`/pets/info/species/${speciesId}/breeds`),
    )
  },
  fetchAdoptedPets(page: number, pageSize: number) {
    return resolve<PageResult<any>>(
      // “我的宠物”只展示已领养成功（APPROVED）的记录，避免与“领养记录”历史接口混用导致重复
      request.get<ApiResponse<PageResult<any>>>('/user/adoptions/approved', {
        params: toPageParams(page, pageSize),
      }),
    )
  },
  fetchShelters() {
    return resolve<Shelter[]>(request.get<ApiResponse<Shelter[]>>('/shelters'))
  },
  startFoster(petId: number, payload: { shelterId: number; startDate: string }) {
    return resolve(request.post<ApiResponse<any>>(`/pets/${petId}/foster`, payload))
  },
  endFoster(petId: number) {
    return resolve(request.post<ApiResponse<any>>(`/pets/${petId}/foster/end`))
  },
  fetchAdoptionTimeline(petId: number) {
    return resolve<AdoptionTimelineResponse>(
      request.get<ApiResponse<AdoptionTimelineResponse>>(`/pets/${petId}/adoption-timeline`),
    )
  },
  fetchAdoptionRecords(page: number, pageSize: number, status?: string) {
    return resolve<PageResult<any>>(
      request.get<ApiResponse<PageResult<any>>>('/user/adoptions', {
        params: {
          ...toPageParams(page, pageSize),
          ...(status && status !== 'ALL' ? { status } : {}),
        },
      }),
    )
  },
  fetchAdoptablePets(page: number, pageSize: number, filters?: { 
    species?: string;
    breed?: string;
    gender?: string;
    minAge?: number;
    maxAge?: number;
  }) {
    const params: Record<string, any> = toPageParams(page, pageSize);
    
    if (filters?.species) params.species = filters.species;
    if (filters?.breed) params.breed = filters.breed;
    if (filters?.gender) params.gender = filters.gender;
    if (filters?.minAge !== undefined) params.min_age = filters.minAge;
    if (filters?.maxAge !== undefined) params.max_age = filters.maxAge;
    
    return resolve<PageResult<any>>(
      request.get<ApiResponse<PageResult<any>>>('/pets/info/available', {
        params,
      }),
    )
  },
  adoptPet(petId: number) {
    return resolve(
      request.post<ApiResponse<any>>('/pets/adopt', null, {
        params: { petId },
      }),
    )
  },
  fetchFosters(page: number, pageSize: number, status?: string) {
    return resolve<PageResult<any>>(
      request.get<ApiResponse<PageResult<any>>>('/user/fosters', {
        params: {
          ...toPageParams(page, pageSize),
          ...(status && status !== 'ALL' ? { status } : {}),
        },
      }),
    )
  },
  deleteFoster(id: number) {
    return resolve(request.delete<ApiResponse<any>>(`/fosters/delete/${id}`))
  },
  fetchPetDetail(petId: number) {
    return resolve<PetDetail>(request.get<ApiResponse<PetDetail>>(`/pets/details/${petId}`))
  },
  updatePet(petId: number, payload: PetFormPayload) {
    return resolve(request.put<ApiResponse<any>>(`/pets/${petId}`, payload))
  },
}

export const mapPagination = (data: PageResult<any>, fallback: PaginationState): PaginationState => ({
  currentPage: data.current ?? fallback.currentPage,
  pageSize: data.size ?? fallback.pageSize,
  total: data.total ?? fallback.total,
  pages: data.pages ?? fallback.pages,
})

const resolveImage = (value?: string | null) => resolveAssetUrl(value || '')

const toAdoptedPet = (record: any): AdoptedPet => {
  const adoptionStatus = (record.adoptionStatus || record.status || '').toString().toUpperCase()
  const petStatus = (record.status || record.petStatus || '').toString().toUpperCase()
  const fosterStatus = (record.fosterStatus || '').toString().toUpperCase()
  const fosterId = record.fosterId ?? record.fid
  
  // 【统一状态处理】使用通用工具解析状态，避免状态映射逻辑分散
  // 后端返回的状态优先级：adoptionStatus > petStatus > fosterStatus
  let resolvedStatus = adoptionStatus || petStatus || 'AVAILABLE'
  
  // 【考虑寄养状态】如果有寄养记录，寄养状态优先于宠物状态
  // 但劣于已批准的领养
  if (adoptionStatus !== 'APPROVED' && adoptionStatus !== 'REJECTED') {
    if (fosterStatus === 'PENDING') {
      resolvedStatus = 'FOSTER_PENDING'
    } else if (fosterStatus === 'APPROVED' || fosterStatus === 'ONGOING') {
      resolvedStatus = 'FOSTERING'
    }
  }
  
  return {
    pid: record.pid,
    name: record.name,
    species: record.species ?? '',
    breed: record.breed ?? '',
    age: record.age ?? 0,
    gender: normalizeGender(record.gender),
    status: resolvedStatus,
    adoptionStatus: adoptionStatus || undefined,
    fosterStatus: fosterStatus || undefined,
    fosterId,
    image: resolveImage(record.image),
    adoptionDate: record.adoptionDate,
    shelterName: record.sname || record.shelterName,
    shelterAddress: record.location || record.shelterAddress,
  }
}

const toAdoptionRecord = (record: any): AdoptionRecord => ({
  id: record.aid ?? record.id ?? record.pid,
  adoptDate: record.adoptionDate,
  adoptionStatus: (record.adoptionStatus || record.status || '').toString().toUpperCase(),
  reviewNote: record.reviewNote,
  pet: toAdoptedPet(record),
  shelter: {
    sid: record.sid,
    sname: record.sname,
    location: record.location,
  },
})

const toAdoptablePet = (record: any): AdoptablePet => ({
  pid: record.pid,
  name: record.name,
  species: record.species ?? '',
  breed: record.breed ?? '',
  age: record.age ?? 0,
  gender: normalizeGender(record.gender),
  status: record.status ?? 'AVAILABLE',
  shelterName: record.shelterName ?? '',
  shelterAddress: record.shelterAddress ?? '',
  image: resolveImage(record.image),
})

const normalizeFosterStatus = (record: any): FosterRecord['status'] => {
  const raw = (record.status ?? record.fosterStatus ?? '').toString().toUpperCase()
  if (raw === 'ACTIVE') return 'ONGOING'   // 后端用户端查询返回 active 表示进行中
  if (raw === 'ENDED') return 'COMPLETED'  // 后端用户端查询返回 ended 表示已结束
  if (raw === 'APPROVED') return 'APPROVED'
  if (raw === 'ONGOING') return 'ONGOING'
  if (raw === 'COMPLETED') return 'COMPLETED'
  if (raw === 'REJECTED') return 'REJECTED'
  if (raw === 'PENDING') return 'PENDING'
  return 'PENDING'
}

const toFosterRecord = (record: any): FosterRecord => ({
  id: record.fid ?? record.id,
  pet: {
    pid: record.pet?.pid ?? record.pid ?? 0,
    name: record.pet?.name ?? record.name ?? '',
    species: record.pet?.species ?? record.species ?? '',
    breed: record.pet?.breed ?? record.breed ?? '',
    age: record.pet?.age ?? record.age ?? 0,
    gender: normalizeGender(record.pet?.gender ?? record.gender),
    status: record.pet?.status ?? record.petStatus ?? '',
    image: resolveImage(record.pet?.image ?? record.image),
    shelterName: record.pet?.shelterName,
    shelterAddress: record.pet?.shelterAddress,
  },
  shelter: {
    sid: record.shelter?.sid ?? record.sid,
    name: record.shelter?.name ?? record.sname,
    location: record.shelter?.location ?? record.location,
  },
  createTime: record.createTime ?? record.startDate ?? '',
  updateTime: record.updateTime ?? record.endDate ?? null,
  status: normalizeFosterStatus(record),
  reviewNote: record.reviewNote,
})

export const petsMappers = {
  toAdoptedPet,
  toAdoptionRecord,
  toAdoptablePet,
  toFosterRecord,
}
