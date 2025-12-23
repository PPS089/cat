<template>
  <el-dialog
    :model-value="visible"
    class="pet-editor-dialog"
    width="520px"
    :close-on-click-modal="false"
    @close="close"
  >
    <template #header>
      <h3>{{ t('nav.editPetInfo') }}</h3>
    </template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" v-loading="loading">
      <el-form-item :label="t('user.name')" prop="name">
        <el-input v-model="form.name" :placeholder="t('user.pleaseEnterPetName')" />
      </el-form-item>
      <el-form-item :label="t('user.species')">
        <el-input :model-value="displaySpecies" disabled />
      </el-form-item>
      <el-form-item :label="t('user.breed')">
        <el-input :model-value="displayBreed" disabled />
      </el-form-item>
      <el-form-item :label="t('user.age')" prop="age">
        <el-input
          v-model.number="form.age"
          type="number"
          :min="1"
          :max="30"
          :placeholder="t('message.ageRangeError')"
          @input="onAgeInput"
        />
        <!-- <span class="unit">{{ t('message.yearsOld') }}</span> -->
      </el-form-item>
      <el-form-item :label="t('user.gender')" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio label="雄">{{ t('user.male') }}</el-radio>
          <el-radio label="雌">{{ t('user.female') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { petsService } from '@/modules/pets/services/pets'
import type { PetFormPayload } from '@/modules/pets/types'

const props = defineProps<{
  visible: boolean
  petId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saved'): void
}>()

const { t } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const displaySpecies = ref('')
const displayBreed = ref('')
const form = reactive<PetFormPayload>({
  name: '',
  speciesId: 0,
  breedId: 0,
  age: 1,
  gender: '雄',
  shelterId: undefined,
  imageUrl: '',
})

const resolveGender = (value?: string | null) => {
  if (value === '雌') return '雌'
  return '雄'
}

const validateAgeRange = (_rule: any, value: number, callback: (error?: Error) => void) => {
  const age = Number(value)
  if (!Number.isFinite(age) || age < 1 || age > 30) {
    callback(new Error(t('message.ageRangeError')))
    return
  }
  callback()
}

const onAgeInput = (val: string | number) => {
  const age = Number(val)
  form.age = Number.isFinite(age) ? age : 1
}

const rules: FormRules = {
  name: [
    { required: true, message: t('user.pleaseEnterPetName'), trigger: 'blur' },
    { min: 2, max: 20, message: t('message.nameLengthError'), trigger: 'blur' },
  ],
  age: [
    { required: true, message: t('message.ageRangeError'), trigger: 'change' },
    { validator: validateAgeRange, trigger: ['change', 'blur'] },
  ],
  gender: [{ required: true, message: t('message.pleaseSelectGender'), trigger: 'change' }],
}

const loadDetail = async () => {
  if (!props.petId) return
  loading.value = true
  try {
    const response = await petsService.fetchPetDetail(props.petId)
    if (response.code === 200 && response.data) {
      const data = response.data
      form.name = data.name ?? ''
      form.speciesId = Number(data.speciesId ?? 0)
      form.breedId = Number(data.breedId ?? 0)
      displaySpecies.value = data.species ?? data.speciesCode ?? ''
      displayBreed.value = data.breed ?? ''
      form.age = data.age ?? data.ageYears ?? 0
      form.gender = resolveGender(data.gender ?? data.sex)
      form.shelterId = data.shelterId
      form.imageUrl = data.image ?? ''
    } else {
      console.warn('[PetEditor] load detail returned empty data')
      close()
    }
  } catch (error) {
    console.error('[PetEditor] load detail failed', error)
    close()
  } finally {
    loading.value = false
  }
}

watch(
  () => props.petId,
  () => {
    if (props.visible) {
      loadDetail()
    }
  },
)

watch(
  () => props.visible,
  visible => {
    if (visible) {
      loadDetail()
    }
  },
  { immediate: true },
)

const submit = () => {
  if (!formRef.value || !props.petId) return
  const age = Number(form.age)
  if (!Number.isFinite(age) || age < 0 || age > 30) {
    ElMessage.error(t('message.ageRangeError'))
    return
  }
  if (!Number.isFinite(form.speciesId) || form.speciesId <= 0) {
    ElMessage.error(t('message.invalidDataFormat'))
    return
  }
  if (!Number.isFinite(form.breedId) || form.breedId <= 0) {
    ElMessage.error(t('message.invalidDataFormat'))
    return
  }
  formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      const payload: PetFormPayload = {
        name: form.name,
        speciesId: form.speciesId,
        breedId: form.breedId,
        age: form.age,
        gender: form.gender,
        shelterId: form.shelterId,
        imageUrl: form.imageUrl,
      }
      const response = await petsService.updatePet(props.petId!, payload)
      if (response.code === 200) {
        ElMessage.success(t('message.updateSuccess'))
        emit('saved')
        close()
      }
    } catch (error: any) {
      console.error('[PetEditor] save failed', error)
    } finally {
      saving.value = false
    }
  })
}

const close = () => emit('update:visible', false)
</script>

<style scoped>
.unit {
  margin-left: 0.5rem;
  color: var(--app-text-secondary);
}
</style>
