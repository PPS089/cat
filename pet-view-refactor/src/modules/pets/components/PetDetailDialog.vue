<template>
  <el-dialog
    class="pet-detail-dialog"
    :model-value="visible"
    width="520px"
    :close-on-click-modal="false"
    @close="close"
  >
    <template #header>
      <div class="dialog-header">
        <h3>{{ detail?.name || t('nav.viewDetails') }}</h3>
        <p v-if="detail?.breed">{{ detail.breed }}</p>
      </div>
    </template>
    <div v-if="loading" class="dialog-loading">
      <el-skeleton :rows="4" animated />
    </div>
    <div v-else-if="detail" class="dialog-body">
      <img :src="petImage" :alt="detail.name" class="pet-image" />
      <ul class="pet-info">
        <li>
          <span>{{ t('nav.age') }}</span>
          <strong>{{ detail.age }} {{ t('message.yearsOld') }}</strong>
        </li>
        <li>
          <span>{{ t('nav.gender') }}</span>
          <strong>{{ detail.gender }}</strong>
        </li>
        <li>
          <span>{{ t('nav.shelter') }}</span>
          <strong>{{ detail.shelterName || t('message.unknown') }}</strong>
        </li>
        <li>
          <span>{{ t('nav.address') }}</span>
          <strong>{{ detail.shelterAddress || t('message.unknownAddress') }}</strong>
        </li>
      </ul>
      <p v-if="detail.description" class="pet-description">{{ detail.description }}</p>
    </div>
    <template #footer>
      <el-button @click="close">{{ t('common.close') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { petsService } from '@/modules/pets/services/pets'
import { resolveAssetUrl } from '@/shared/utils/url'
import type { PetDetail } from '@/modules/pets/types'
import dogImage from '@/assets/img/dog.jpg'

const props = defineProps<{
  visible: boolean
  petId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const { t } = useI18n()
const detail = ref<PetDetail | null>(null)
const loading = ref(false)

const petImage = computed(() => {
  const src = detail.value?.image
  return src ? resolveAssetUrl(src) : dogImage
})

const fetchDetail = async () => {
  if (!props.petId) {
    detail.value = null
    return
  }
  loading.value = true
  try {
    const response = await petsService.fetchPetDetail(props.petId)
    detail.value = response?.data || null
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  visible => {
    if (visible) {
      fetchDetail()
    }
  },
  { immediate: true },
)

watch(
  () => props.petId,
  () => {
    if (props.visible) {
      fetchDetail()
    }
  },
)

const close = () => emit('update:visible', false)
</script>

<style scoped>
:deep(.pet-detail-dialog .el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
  padding-top: 1rem;
}

.dialog-header h3 {
  margin: 0;
  color: var(--app-text-color);
}

.dialog-header p {
  margin: 4px 0 0;
  color: var(--app-text-secondary);
}

.dialog-loading {
  padding: 1rem 0;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  color: var(--app-text-color);
}

.pet-image {
  width: 100%;
  border-radius: 14px;
  object-fit: cover;
  max-height: 240px;
  box-shadow: var(--app-shadow-card);
}

.pet-info {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem 1.25rem;
}

.pet-info li {
  display: flex;
  flex-direction: column;
  font-size: 0.95rem;
  line-height: 1.4;
}

.pet-info span {
  color: var(--app-text-secondary);
  font-size: 0.85rem;
  letter-spacing: 0.01em;
}

.pet-info strong {
  color: var(--app-text-color);
  font-weight: 600;
}

.pet-description {
  margin: 0;
  color: var(--app-text-secondary);
  line-height: 1.6;
  font-size: 0.95rem;
}
</style>
