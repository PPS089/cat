import { ref, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'

import type { AdoptionRecord, HealthAlert, HealthAlertForm } from '@/types/health'

export const useHealth = () => {
  const { t } = useI18n()
  
  const healthAlerts = ref<HealthAlert[]>([])
  const pets = ref<AdoptionRecord[]>([])
  const loading = ref(false)

  const selectedPet = ref('')
  const selectedHealthType = ref('')
  const selectedStatus = ref('')
  const viewMode = ref('list')
  const showAddModal = ref(false)
  const showEditModal = ref(false)

  const formData: HealthAlertForm = reactive({
    healthId: 0,
    pid: 0,
    healthType: '',
    checkDate: '',
    description: '',
    reminderTime: undefined,
    status: 'normal'
  })

  const showAddModalDialog = () => {
    if (pets.value.length === 0) {
      ElMessage.warning(t('noAssociatedPetPleaseAdoptOrFoster'))
      return
    }
    showAddModal.value = true
  }

  const totalAlerts = computed(() => healthAlerts.value.length)
  const normalAlerts = computed(() => 
    healthAlerts.value.filter(alert => alert.status === 'normal')
  )
  const criticalAlerts = computed(() => 
    healthAlerts.value.filter(alert => alert.status === 'critical')
  )

  const pendingAlerts = computed(() => normalAlerts.value)
  const completedAlerts = computed(() => criticalAlerts.value)
  const expiredAlerts = computed(() => criticalAlerts.value)

  const filteredAlerts = computed(() => {
    let filtered = healthAlerts.value

    if (selectedPet.value) {
      filtered = filtered.filter(alert => alert.pid === Number(selectedPet.value))
    }

    if (selectedHealthType.value) {
      filtered = filtered.filter(alert => alert.healthType === selectedHealthType.value)
    }

    if (selectedStatus.value) {
      filtered = filtered.filter(alert => alert.status === selectedStatus.value)
    }

    return filtered.sort((a, b) => new Date(b.checkDate).getTime() - new Date(a.checkDate).getTime())
  })

  const groupedAlerts = computed(() => {
    const groups: Record<string, HealthAlert[]> = {}
    filteredAlerts.value.forEach(alert => {
      const date = new Date(alert.checkDate).toDateString()
      if (!groups[date]) {
        groups[date] = []
      }
      groups[date].push(alert)
    })
    return groups
  })

  const fetchHealthAlerts = async () => {
    loading.value = true
    try {
      const response = await request.get<HealthAlert[]>('/user/health-alerts')
      
      await fetchPets()
      
      const userPetIds = pets.value.map(pet => pet.pid)
      console.log('用户拥有的宠物ID列表:', userPetIds)
      
      if (response && response.data && Array.isArray(response.data)) {
        const allAlerts = response.data
      
        healthAlerts.value = allAlerts
          .filter((alert: any) => {
            const alertPid = Number(alert.pid)
            const isUserPet = userPetIds.includes(alertPid)
            console.log(`过滤检查 - alertPid: ${alertPid}, 用户宠物: ${isUserPet}`)
            return isUserPet
          })
          .map((alert: any) => {
            console.log('健康提醒alert数据:', alert)
            return {
              healthId: alert.healthId,
              pid: Number(alert.pid),
              checkDate: alert.checkDate,
              healthType: alert.healthType,
              description: alert.description,
              reminderTime: alert.reminderTime,
              status: alert.status,
              createdAt: alert.createdAt,
              updatedAt: alert.updatedAt,
            } as HealthAlert
          })
      }

      console.log('过滤后的健康警报数据:', healthAlerts.value)
    } catch (error) {
      ElMessage.error(t('api.getPetListFailed'))
      healthAlerts.value = []
    } finally {
      loading.value = false
    }
  }

  const fetchPets = async () => {
    try {
      let adoptionsResponse
      
      try {
        adoptionsResponse = await request.get<import('@/types/api').PageResult<AdoptionRecord>>('/user/adoptions', {
          params: {
            current_page: 1,
            per_page: 100
          }
        })
      } catch (adoptionError) {
        ElMessage.error(t('api.getAdoptionRecordsFailed') + ': ' + ((adoptionError as any).message || t('unknownError')))
        adoptionsResponse = { success: false, data: { adoptions: [] } }
      }
      
      if (!adoptionsResponse) {
        adoptionsResponse = { success: false, data: { adoptions: [] } }
      }
      
      const petsList: AdoptionRecord[] = []
      
      console.log('领养记录API响应:', adoptionsResponse)
      
      if (adoptionsResponse.code === 200) {
        const adoptions = adoptionsResponse.data?.records ?? []
        console.log('处理领养记录数据:', adoptions)
      
        adoptions.forEach((adoption: AdoptionRecord) => {
          if (adoption.pid) {
            petsList.push({
              pid: Number(adoption.pid),
              name: adoption.name,
              species: adoption.species || '',
              breed: adoption.breed
            })
          }
        })
      }
      
      console.log('提取的宠物列表:', petsList)
      pets.value = petsList
    } catch (error) {
      console.error(t('api.getPetListFailed'), error)
      ElMessage.error(t('api.getPetListFailed'))
      pets.value = []
    }
  }

  const updateAlertStatus = async (healthId: number, status: 'normal' | 'critical') => {
    try {
      await request.put(`/user/health-alerts/${healthId}/status?status=${status}`)
      await fetchHealthAlerts()
      ElMessage.success(t('api.updateHealthAlertSuccess'))
    } catch (error: any) {
      console.error(t('api.updateHealthAlertFailed'), error)
      if (error.response && error.response.status === 400) {
        ElMessage.error(t('api.updateHealthAlertFailed') + ': ' + (error.response.data?.message || t('common.validationError')))
      } else {
        ElMessage.error(t('api.updateHealthAlertFailed'))
      }
    }
  }

  const deleteAlert = async (healthId: number) => {
    try {
      await ElMessageBox.confirm(
        t('api.confirmDeleteHealthAlert'),
        t('api.deleteConfirmation'),
        {
          confirmButtonText: t('api.confirm'),
          cancelButtonText: t('api.cancel'),
          type: 'warning'
        }
      )
      
      await request.delete(`/user/health-alerts/${healthId}`)
      await fetchHealthAlerts()
      ElMessage.success(t('api.deleteSuccess'))
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(t('api.deleteFailed'))
        console.error('删除健康提醒失败:', error)
      }
    }
  }

  const saveAlert = async () => {
    try {
      if (!formData.pid) {
        ElMessage.error(t('api.pleaseSelectPet'))
        return
      }

      let formattedCheckDate = formData.checkDate
      if (formData.checkDate && formData.checkDate.includes('T')) {
        // 将日期格式转换为 yyyy-MM-dd HH:mm:ss
        const date = new Date(formData.checkDate)
        formattedCheckDate = date.getFullYear() + '-' + 
          String(date.getMonth() + 1).padStart(2, '0') + '-' + 
          String(date.getDate()).padStart(2, '0') + ' ' + 
          String(date.getHours()).padStart(2, '0') + ':' + 
          String(date.getMinutes()).padStart(2, '0') + ':' + 
          String(date.getSeconds()).padStart(2, '0')
      }

      let formattedReminderTime = formData.reminderTime || null
      if (formData.reminderTime && formData.reminderTime.includes('T')) {
        // 将日期格式转换为 yyyy-MM-dd HH:mm:ss
        const date = new Date(formData.reminderTime)
        formattedReminderTime = date.getFullYear() + '-' + 
          String(date.getMonth() + 1).padStart(2, '0') + '-' + 
          String(date.getDate()).padStart(2, '0') + ' ' + 
          String(date.getHours()).padStart(2, '0') + ':' + 
          String(date.getMinutes()).padStart(2, '0') + ':' + 
          String(date.getSeconds()).padStart(2, '0')
      }

      const payload = {
        pid: formData.pid,
        healthType: formData.healthType,
        checkDate: formattedCheckDate,
        reminderTime: formattedReminderTime, // 允许为null
        status: formData.status,
        description: formData.description
      }

      let response
      if (formData.healthId && formData.healthId > 0) {
        response = await request.put(`/user/health-alerts/${formData.healthId}`, payload)
      } else {
        response = await request.post('/user/health-alerts', payload)
      }

      if (response.data) {
        ElMessage.success(formData.healthId ? t('api.updateHealthAlertSuccess') : t('api.createHealthAlertSuccess'))
        showAddModal.value = false
        showEditModal.value = false
        await fetchHealthAlerts()
      } else {
        ElMessage.error(t('api.operationFailed'))
      }
    } catch (error: any) {
      console.error(t('api.saveHealthAlertFailed'), error)
      // 区分创建和更新操作的错误提示
      if (formData.healthId && formData.healthId > 0) {
        // 更新操作
        if (error.response && error.response.status === 400) {
          ElMessage.error(t('api.updateHealthAlertFailed') + ': ' + (error.response.data?.message || t('common.validationError')))
        } else {
          ElMessage.error(t('api.updateHealthAlertFailed'))
        }
      } else {
        // 创建操作
        if (error.response && error.response.status === 400) {
          ElMessage.error(t('api.saveHealthAlertFailed') + ': ' + (error.response.data?.message || t('common.validationError')))
        } else {
          ElMessage.error(t('api.saveHealthAlertFailed'))
        }
      }
    }
  }

  const closeModal = () => {
    showAddModal.value = false
    showEditModal.value = false
    
    formData.healthId = 0
    formData.pid = 0
    formData.healthType = ''
    formData.checkDate = ''
    formData.reminderTime = undefined
    formData.status = 'normal'
    formData.description = ''
  }

  const initializeHealthAlerts = async () => {
    console.log('开始初始化健康提醒数据')
    
    await fetchPets()
    console.log('宠物数据加载完成:', pets.value)
    
    if (pets.value.length > 0) {
      await fetchHealthAlerts()
      console.log('健康提醒数据加载完成:', healthAlerts.value)
    } else {
      console.log('没有宠物数据，不加载健康提醒')
      healthAlerts.value = []
    }
    
    console.log('initializeHealthAlerts: 初始化完成，宠物数量:', pets.value.length, '健康提醒数量:', healthAlerts.value.length)
  }

  const getStatusClass = (status: string): Record<string, boolean> => {
    const normalizeStatus = status === 'normal' ? 'normal' : status === 'critical' ? 'critical' : status
    return {
      'pending': normalizeStatus === 'normal',
      'completed': normalizeStatus === 'normal',
      'expired': normalizeStatus === 'critical',
      'normal': normalizeStatus === 'normal',
      'critical': normalizeStatus === 'critical'
    }
  }

  const getHealthTypeLabel = (type: string | null | undefined): string => {
    if (!type) return t('healthAlerts.noType', '未分类')
    const typeMap: Record<string, string> = {
      'VACCINE': t('healthAlerts.vaccine'),
      'CHECKUP': t('healthAlerts.checkup'),
      'SURGERY': t('healthAlerts.surgery'),
      'DISEASE': t('healthAlerts.disease')
    }
    return typeMap[type] || type
  }

  const getStatusLabel = (status: string | null | undefined): string => {
    if (!status) return t('healthAlerts.unknown', '未知')
    const statusMap: Record<string, string> = {
      'normal': t('healthAlerts.normal'),
      'critical': t('healthAlerts.critical'),
      'pending': t('healthAlerts.pending'),
      'completed': t('healthAlerts.completed'),
      'expired': t('healthAlerts.expired')
    }
    return statusMap[status] || status
  }

  const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return '未设置'
    
    try {
      const date = new Date(dateString)
      if (isNaN(date.getTime())) {
        return dateString
      }
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch (error) {
      return dateString
    }
  }

  const editAlert = (alert: HealthAlert) => {
    formData.healthId = alert.healthId || alert.hid || 0
    formData.pid = Number(alert.pid) || 0
    formData.healthType = alert.healthType || alert.alertType || ''
    
    formData.checkDate = (alert.checkDate || '').replace(' ', 'T')
    formData.reminderTime = (alert.reminderTime || '').replace(' ', 'T')
    formData.status = alert.status || 'normal'
    formData.description = alert.description || alert.content || ''
    
    showEditModal.value = true
  }

  const getPetName = (pid: number): string => {
    const pet = pets.value.find(p => {
      return p.pid === pid
    })
    const result = pet ? pet.name : '未知宠物'
    return result
  }

  return {
    healthAlerts,
    pets,
    loading,
    selectedPet,
    selectedHealthType,
    selectedStatus,
    viewMode,
    showAddModal,
    showEditModal,
    formData,
    totalAlerts,
    normalAlerts,
    criticalAlerts,
    pendingAlerts,
    completedAlerts,
    expiredAlerts,
    filteredAlerts,
    groupedAlerts,
    showAddModalDialog,
    updateAlertStatus,
    deleteAlert,
    saveAlert,
    closeModal,
    initializeHealthAlerts,
    getStatusClass,
    getHealthTypeLabel,
    getStatusLabel,
    formatDate,
    editAlert,
    getPetName
  }
}
