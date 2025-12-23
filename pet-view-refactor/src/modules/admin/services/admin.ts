import request from '@/shared/utils/request'

const toPageParams = (page: number, pageSize: number) => ({
  current_page: page,
  per_page: pageSize,
})

const normalizeStatusParam = (status?: string) => {
  const value = (status ?? '').trim()
  return value ? value : undefined
}

export const adminApi = {
  listSpecies() {
    return request.get('/admin/species')
  },
  createSpecies(payload: { name: string; description?: string | null }) {
    return request.post('/admin/species', payload)
  },
  updateSpecies(id: number, payload: { name: string; description?: string | null }) {
    return request.put(`/admin/species/${id}`, payload)
  },
  listBreeds(speciesId: number) {
    return request.get(`/admin/species/${speciesId}/breeds`)
  },
  createBreed(speciesId: number, payload: { name: string; description?: string | null }) {
    return request.post(`/admin/species/${speciesId}/breeds`, payload)
  },
  updateBreed(
    speciesId: number,
    breedId: number,
    payload: { name: string; description?: string | null },
  ) {
    return request.put(`/admin/species/${speciesId}/breeds/${breedId}`, payload)
  },
  listAdoptions(status: string | undefined, page: number, size: number) {
    const normalized = normalizeStatusParam(status)
    return request.get('/admin/adoptions', {
      params: { ...toPageParams(page, size), ...(normalized ? { status: normalized } : {}) },
    })
  },
  approveAdoption(id: number, note?: string) {
    return request.post(`/admin/adoptions/${id}/approve`, note ? { note } : undefined)
  },
  rejectAdoption(id: number, note: string) {
    const normalized = note.trim()
    if (!normalized) {
      return Promise.reject(new Error('拒绝原因不能为空'))
    }
    return request.post(`/admin/adoptions/${id}/reject`, { note: normalized })
  },
  listFosters(status: string | undefined, page: number, size: number) {
    const normalized = normalizeStatusParam(status)
    return request.get('/admin/fosters', {
      params: { ...toPageParams(page, size), ...(normalized ? { status: normalized } : {}) },
    })
  },
  approveFoster(id: number, note?: string) {
    return request.post(`/admin/fosters/${id}/approve`, note ? { note } : undefined)
  },
  rejectFoster(id: number, note: string) {
    const normalized = note.trim()
    if (!normalized) {
      return Promise.reject(new Error('拒绝原因不能为空'))
    }
    return request.post(`/admin/fosters/${id}/reject`, { note: normalized })
  },
  completeFoster(id: number) {
    return request.post(`/admin/fosters/${id}/complete`)
  },
  listPets(status: string | undefined, page: number, size: number) {
    const normalized = normalizeStatusParam(status)
    return request.get('/admin/pets', {
      params: { ...toPageParams(page, size), ...(normalized ? { status: normalized } : {}) },
    })
  },
  createPet(payload: any) {
    return request.post('/admin/pets', payload)
  },
  updatePet(id: number, payload: any) {
    return request.put(`/admin/pets/${id}`, payload)
  },
  updatePetStatus(id: number, status: string) {
    return request.put(`/admin/pets/${id}/status`, null, {
      params: { status },
    })
  },
  deletePet(id: number) {
    return request.delete(`/admin/pets/${id}`)
  },
  listArticles(status: string | undefined, page: number, size: number) {
    const normalized = normalizeStatusParam(status)
    return request.get('/admin/articles', {
      params: { ...toPageParams(page, size), ...(normalized ? { status: normalized } : {}) },
    })
  },
  saveArticle(payload: any) {
    if (payload.id) {
      return request.put(`/admin/articles/${payload.id}`, payload)
    }
    return request.post('/admin/articles', payload)
  },
  createArticle(payload: any) {
    return request.post('/admin/articles', payload)
  },
  updateArticle(id: number, payload: any) {
    return request.put(`/admin/articles/${id}`, payload)
  },
  deleteArticle(id: number) {
    return request.delete(`/admin/articles/${id}`)
  },
}
