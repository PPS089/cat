import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { dispatchNavStatsRefresh } from '@/shared/utils/events'
import { healthAlertsService } from '../services/healthAlerts'
import type {
  HealthAlert,
  HealthAlertFilters,
  HealthAlertForm,
  HealthAlertStats,
  HealthViewMode,
  PetSummary,
} from '../types'

const createFilters = (): HealthAlertFilters => ({
  petId: '',
  healthType: '',
  status: '',
})

const createForm = (): HealthAlertForm => ({
  healthId: null,
  pid: '',
  healthType: '',
  status: 'attention',
  description: '',
  checkDate: '',
  reminderTime: null,
})

const formatDateTime = (value: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const toInputValue = (value?: string | null) => {
  if (!value) return ''
  return value.replace(' ', 'T').slice(0, 16)
}

export const useHealthAlertsStore = defineStore('health-alerts', () => {
  const { t } = useI18n()

  const alerts = ref<HealthAlert[]>([])
  const pets = ref<PetSummary[]>([])
  const loading = ref(false)
  const submitting = ref(false)
  const filters = reactive(createFilters())
  const viewMode = ref<HealthViewMode>('list')
  const dialogVisible = ref(false)
  const dialogMode = ref<'create' | 'edit'>('create')
  const form = reactive(createForm())
  const listenerAttached = ref(false)

  const stats = computed<HealthAlertStats>(() => {
    const base = { total: 0, attention: 0, expired: 0, reminded: 0 }
    return alerts.value.reduce((acc, alert) => {
      acc.total += 1
      if (alert.status === 'attention') acc.attention += 1
      if (alert.status === 'expired') acc.expired += 1
      if (alert.status === 'reminded') acc.reminded += 1
      return acc
    }, base)
  })

  const filteredAlerts = computed(() => {
    return alerts.value
      .filter(alert => (filters.petId ? alert.pid === Number(filters.petId) : true))
      .filter(alert => (filters.healthType ? alert.healthType === filters.healthType : true))
      .filter(alert => (filters.status ? alert.status === filters.status : true))
      .sort(
        (a, b) => new Date(b.checkDate).getTime() - new Date(a.checkDate).getTime(),
      )
  })

  const groupedAlerts = computed(() => {
    return filteredAlerts.value.reduce<Record<string, HealthAlert[]>>((groups, alert) => {
      const key = new Date(alert.checkDate).toDateString()
      if (!groups[key]) groups[key] = []
      groups[key].push(alert)
      return groups
    }, {})
  })

  const showReminderField = computed(() => form.status === 'attention')

  const healthTypeOptions = computed(() => [
    { value: 'VACCINE', label: t('healthAlerts.vaccine') },
    { value: 'CHECKUP', label: t('healthAlerts.checkup') },
    { value: 'SURGERY', label: t('healthAlerts.surgery') },
    { value: 'DISEASE', label: t('healthAlerts.disease') },
  ])

  const statusOptions = computed(() => [
    { value: 'attention', label: t('healthAlerts.attention') },
    { value: 'expired', label: t('healthAlerts.expired') },
    { value: 'reminded', label: t('healthAlerts.reminded') },
  ])

  const resetForm = () => {
    Object.assign(form, createForm())
  }

  const fetchPets = async () => {
    try {
      const response = await healthAlertsService.fetchPets()
      pets.value = response.code === 200 ? response.data : []
    } catch (error) {
      pets.value = []
      console.error('[Health] fetchPets failed', error)
    }
  }

  const fetchAlerts = async () => {
    loading.value = true
    try {
      const response = await healthAlertsService.fetchAlerts()
      alerts.value = response.code === 200 ? response.data : []
    } catch (error) {
      alerts.value = []
      console.error('[Health] fetchAlerts failed', error)
    } finally {
      loading.value = false
    }
  }

  const bootstrap = async () => {
    await Promise.all([fetchPets(), fetchAlerts()])
  }

  const ensureRealtime = () => {
    if (listenerAttached.value || typeof window === 'undefined') return
    window.addEventListener('health-reminder', handleRealtime as EventListener)
    listenerAttached.value = true
  }

  const disposeRealtime = () => {
    if (!listenerAttached.value || typeof window === 'undefined') return
    window.removeEventListener('health-reminder', handleRealtime as EventListener)
    listenerAttached.value = false
  }

  const handleRealtime = async (event?: Event) => {
    const detail = (event as CustomEvent)?.detail as Record<string, any> | undefined

    if (detail) {
      ElNotification({
        title: detail.petName || t('healthAlerts.reminderTime'),
        message: detail.description || t('healthAlerts.attentionStatus'),
        type: 'warning',
        duration: 5000,
      })
    }

    await fetchAlerts()
  }

  const openCreate = () => {
    resetForm()
    dialogMode.value = 'create'
    dialogVisible.value = true
  }

  const openEdit = (alert: HealthAlert) => {
    form.healthId = alert.healthId
    form.pid = alert.pid
    form.healthType = (alert.healthType as any) || ''
    form.status = (alert.status as any) || 'attention'
    form.description = alert.description
    form.checkDate = toInputValue(alert.checkDate)
    form.reminderTime = alert.reminderTime ? toInputValue(alert.reminderTime) : null
    dialogMode.value = 'edit'
    dialogVisible.value = true
  }

  const closeDialog = () => {
    dialogVisible.value = false
    resetForm()
  }

  const buildPayload = () => ({
    pid: Number(form.pid),
    healthType: form.healthType,
    status: form.status,
    description: form.description.trim(),
    checkDate: formatDateTime(form.checkDate),
    reminderTime: showReminderField.value && form.reminderTime ? formatDateTime(form.reminderTime) : null,
  })

  const saveAlert = async () => {
    if (submitting.value) return
    if (!form.pid || !form.healthType || !form.checkDate || !form.description.trim()) {
      ElMessage.error(t('healthAlerts.completeForm'))
      return
    }
    submitting.value = true
    try {
      const payload = buildPayload()
      if (form.healthId) {
        await healthAlertsService.updateAlert(form.healthId, payload)
      } else {
        await healthAlertsService.createAlert(payload)
      }
      ElMessage.success(
        form.healthId ? t('healthAlerts.updateSuccess') : t('healthAlerts.createSuccess'),
      )
      await fetchAlerts()
      dispatchNavStatsRefresh({ reason: 'health-alerts-change' })
      closeDialog()
    } catch (error) {
      console.error('[Health] saveAlert failed', error)
    } finally {
      submitting.value = false
    }
  }

  const deleteAlert = async (healthId: number) => {
    try {
      await ElMessageBox.confirm(
        t('healthAlerts.deleteConfirm'),
        t('common.deleteConfirmation'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning',
        },
      )
      await healthAlertsService.deleteAlert(healthId)
      ElMessage.success(t('healthAlerts.deleteSuccess'))
      await fetchAlerts()
      dispatchNavStatsRefresh({ reason: 'health-alerts-change' })
    } catch (error) {
      if (error !== 'cancel') {
        console.error('[Health] deleteAlert failed', error)
      }
    }
  }

  const formatDate = (value?: string | null) => {
    if (!value) return t('healthAlerts.noDate')
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return date.toLocaleString()
  }

  const getPetName = (pid: number) => {
    const pet = pets.value.find(item => item.pid === pid)
    return pet?.name || t('healthAlerts.unknownPet')
  }

  const getStatusLabel = (status?: string | null) => {
    const map: Record<string, string> = {
      attention: t('healthAlerts.attention'),
      expired: t('healthAlerts.expired'),
      reminded: t('healthAlerts.reminded'),
    }
    return map[status || ''] || t('healthAlerts.unknownStatus')
  }

  return {
    // state
    alerts,
    pets,
    loading,
    submitting,
    filters,
    viewMode,
    dialogVisible,
    dialogMode,
    form,
    // computed
    stats,
    filteredAlerts,
    groupedAlerts,
    healthTypeOptions,
    statusOptions,
    showReminderField,
    // actions
    bootstrap,
    ensureRealtime,
    disposeRealtime,
    openCreate,
    openEdit,
    closeDialog,
    saveAlert,
    deleteAlert,
    formatDate,
    getPetName,
    getStatusLabel,
  }
})
