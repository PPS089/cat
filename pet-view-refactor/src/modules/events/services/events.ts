import request from '@/shared/utils/request'
import { resolveAssetUrl } from '@/shared/utils/url'
import { filterAdoptions } from '@/shared/utils/adoptions'
import type { ApiResponse } from '@/types/api'
import type { MediaEntry, RecordEvent } from '../types'

interface RawEvent {
  eid?: number
  record_id?: number
  pid: number
  eventType?: string
  event_type?: string
  description: string
  eventTime?: string
  record_time?: string
  mood?: string
  location?: string
  pet_name?: string
  createTime?: string
  updateTime?: string
  mediaList?: RawMedia[]
  media_list?: RawMedia[]
}

interface RawMedia {
  id?: number
  mediaId?: number
  mid?: number
  filePath?: string
  url?: string
  mediaType?: string
  fileName?: string
  createTime?: string
  updateTime?: string
}

const resolveMediaUrl = (media: RawMedia) => {
  if (media.url) {
    return resolveAssetUrl(media.url)
  }
  if (media.filePath) {
    return resolveAssetUrl(media.filePath)
  }
  return ''
}

const resolveMediaType = (media: RawMedia) => {
  if (!media.mediaType) {
    return 'image'
  }
  const lowered = media.mediaType.toLowerCase()
  if (lowered.startsWith('video')) {
    return 'video'
  }
  if (lowered.startsWith('image')) {
    return 'image'
  }
  return media.mediaType as MediaEntry['type']
}

export const mapMedia = (media: RawMedia): MediaEntry => {
  const resolvedId = (media as any).id ?? media.mid ?? (media as any).mediaId ?? 0
  return {
    id: resolvedId,
    url: resolveMediaUrl(media),
    type: resolveMediaType(media),
    name: media.fileName,
    createdAt: media.createTime,
    updatedAt: media.updateTime,
  }
}

export const mapRecord = (event: RawEvent): RecordEvent => ({
  id: event.eid ?? event.record_id ?? 0,
  petId: event.pid,
  petName: event.pet_name ?? '',
  eventType: event.eventType ?? event.event_type ?? '',
  description: event.description ?? '',
  recordTime: event.eventTime ?? event.record_time ?? '',
  mood: event.mood ?? '',
  location: event.location ?? '',
  createdAt: event.createTime ?? event.eventTime ?? '',
  updatedAt: event.updateTime ?? event.createTime ?? '',
  media: (event.mediaList || event.media_list || []).map(mapMedia),
})

const parseResponse = async <T>(promise: Promise<any>): Promise<ApiResponse<T>> => {
  const response = await promise
  return response as unknown as ApiResponse<T>
}

export const eventsService = {
  fetchPets() {
    return parseResponse<{
      records: Array<{
        pid: number
        name: string
        adoptionStatus?: string
        status?: string
      }>
    }>(
      request.get('/user/adoptions', {
        params: { current_page: 1, per_page: 200, status: 'APPROVED' },
      }),
    )
    .then(res => ({
      ...res,
      data: {
        records: filterAdoptions(res.data?.records || []),
      },
    }))
  },
  fetchEvents() {
    return parseResponse<RawEvent[]>(request.get('/events'))
  },
  createEvent(payload: {
    pid: string
    eventType: string
    recordTime: string
    description: string
    mood?: string
    location?: string
  }) {
    return parseResponse<{ eid: number }>(request.post('/events', payload))
  },
  updateEvent(id: number, payload: {
    pid: string
    eventType: string
    recordTime: string
    description: string
    mood?: string
    location?: string
  }) {
    return parseResponse(request.put(`/events/${id}`, payload))
  },
  deleteEvent(id: number) {
    return parseResponse(request.delete(`/events/${id}`))
  },
  uploadMedia(recordId: number, files: File[]) {
    if (!files.length) {
      return Promise.resolve({ code: 200, data: null } as ApiResponse<null>)
    }
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    formData.append('recordId', String(recordId))
    formData.append('batchUpload', 'true')
    return parseResponse(request.post('/media/upload', formData))
  },
  fetchRecordMedia(recordId: number) {
    return parseResponse<RawMedia[] | RawMedia>(request.get(`/media/record/${recordId}`))
  },
  deleteMedia(mediaId: number) {
    return parseResponse(request.delete(`/media/${mediaId}`))
  },
}
