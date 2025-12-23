<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="420px"
    append-to-body
    :close-on-click-modal="false"
  >
    <p class="reminder-message">
      {{ reminder?.description || t('healthAlerts.attentionStatus') }}
    </p>
    <ul class="reminder-meta">
      <li v-if="reminder?.petName">
        {{ t('healthAlerts.petName') }}：{{ reminder.petName }}
      </li>
      <li v-if="reminder?.healthType">
        {{ t('healthAlerts.healthType') }}：{{ reminder.healthType }}
      </li>
      <li v-if="reminder?.reminderTime">
        {{ t('healthAlerts.reminderTime') }}：{{ formatDate(reminder.reminderTime) }}
      </li>
    </ul>
    <template #footer>
      <el-button @click="close">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="goHealthAlerts">
        {{ t('healthAlerts.viewDetails') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

interface ReminderPayload {
  petName?: string
  healthType?: string
  description?: string
  reminderTime?: string
}

const router = useRouter()
const { t } = useI18n()

const visible = ref(false)
const reminder = ref<ReminderPayload | null>(null)

const dialogTitle = computed(() => {
  const name = reminder.value?.petName
  return name ? `${name} · ${t('healthAlerts.realtimeTitle')}` : t('healthAlerts.realtimeTitle')
})

const formatDate = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

const handleReminder = (event: Event) => {
  reminder.value = ((event as CustomEvent).detail || null) as ReminderPayload | null
  visible.value = Boolean(reminder.value)
}

const close = () => {
  visible.value = false
}

const goHealthAlerts = () => {
  visible.value = false
  router.push('/user/health-alerts')
}

onMounted(() => {
  window.addEventListener('health-reminder', handleReminder as EventListener)
})

onBeforeUnmount(() => {
  window.removeEventListener('health-reminder', handleReminder as EventListener)
})
</script>

<style scoped>
.reminder-message {
  margin: 0 0 0.75rem;
  color: var(--app-text-color);
  line-height: 1.5;
}

.reminder-meta {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  color: var(--app-text-secondary);
  font-size: 0.9rem;
}
</style>
