<template>
  <div class="edit-pet-container" :class="{ 'dark': themeStore.preferences.theme === 'dark' }">
    <header class="edit-pet-header">
      <h1>{{ t('pets.editPet') }}</h1>
    </header>
    
    <main class="edit-pet-main">
      <el-form
        ref="petFormRef"
        :model="petForm"
        :rules="petRules"
        label-width="120px"
        class="pet-form"
        v-loading="loading"
      >
        <el-form-item :label="t('user.name')" prop="name">
          <el-input v-model="petForm.name" :placeholder="t('user.pleaseEnterPetName')" />
        </el-form-item>
        
        <el-form-item :label="t('user.species')" prop="species">
          <el-input v-model="petForm.species" :placeholder="t('user.pleaseEnterSpecies')" />
        </el-form-item>
        
        <el-form-item :label="t('user.breed')" prop="breed">
          <el-input v-model="petForm.breed" :placeholder="t('user.pleaseEnterBreed')" />
        </el-form-item>
        
        <el-form-item :label="t('user.age')" prop="age">
          <el-input-number v-model="petForm.age" :min="0" :max="30" />
          <span style="margin-left: 10px;">{{ t('user.yearsOld') }}</span>
        </el-form-item>
        
        <el-form-item :label="t('user.gender')" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio label="male">{{ t('user.male') }}</el-radio>
            <el-radio label="female">{{ t('user.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ t('common.save') }}
          </el-button>
          <el-button @click="resetForm">
            {{ t('common.reset') }}
          </el-button>
          <el-button @click="$router.back()">
            {{ t('common.cancel') }}
          </el-button>
        </el-form-item>
      </el-form>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import { submitForm , petForm ,fetchPetDetail} from '@/api/editpet'

const { t } = useI18n()
const themeStore = useThemeStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const petFormRef = ref<FormInstance>()


// 表单验证规则
const petRules: FormRules = {
  name: [
    { required: true, message: t('common.pleaseEnter'), trigger: 'blur' },
    { min: 2, max: 20, message: t('message.nameLengthError'), trigger: 'blur' }
  ],
  species: [
    { required: true, message: t('common.pleaseEnter'), trigger: 'blur' }
  ],
  breed: [
    { required: true, message: t('common.pleaseEnter'), trigger: 'blur' }
  ],
  age: [
    { required: true, message: t('common.pleaseEnter'), trigger: 'blur' },
    { type: 'number', min: 0, max: 30, message: t('message.ageRangeError'), trigger: 'blur' }
  ],
  gender: [
    { required: true, message: t('message.pleaseSelectGender'), trigger: 'change' }
  ]
}



// 处理表单提交 - 包装函数以传递正确的参数
const handleSubmit = async () => {
  if (!petFormRef.value) return
  
  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await submitForm(t, router, route)
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.warning(t('message.pleaseCompleteForm'))
    }
  })
}

// 重置表单 - 重新获取宠物详情数据
const resetForm = () => {
  fetchPetDetail(t, router, route)
}

// 组件挂载时获取宠物详情
onMounted(() => {
  fetchPetDetail(t, router, route)
})
</script>

<style scoped >
@import '@/styles/editpet.css';
</style>
