<template>
  <div class="profile-container">
    <header class="profile-header">
      <h1>{{ t('user.editProfile') }}</h1>
    </header>
    
    <main class="profile-main">
      <div class="form-container">
        <el-form :model="profileForm" :rules="computedRules" ref="profileFormRef" label-width="140px" label-position="left">
          <el-form-item :label="t('user.username')" prop="userName">
            <el-input v-model="profileForm.userName" :placeholder="t('common.pleaseEnter') + t('user.username')" />
          </el-form-item>
          
          <el-form-item :label="t('user.email')" prop="email">
            <el-input v-model="profileForm.email" :placeholder="t('common.pleaseEnter') + t('user.email') + t('common.address')" />
          </el-form-item>
          
          <el-form-item :label="t('user.phone')" prop="phone">
            <el-input v-model="profileForm.phone" :placeholder="t('common.pleaseEnter') + t('user.phone')" />
          </el-form-item>
          
          <el-form-item :label="t('user.avatar')" prop="avatar">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :http-request="handleImageUpload"
            >
              <img 
                v-if="userAvatar" 
                :src="userAvatar" 
                class="avatar"
                @error="profileForm.headPic = '/src/assets/img/dog.jpg'"
              />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          
          <el-form-item :label="t('user.bio')" prop="bio">
            <el-input 
              v-model="profileForm.introduce" 
              type="textarea" 
              :rows="4"
              :placeholder="t('common.pleaseEnter') + t('user.bio') + t('common.optional')"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="loading">
              {{ t('user.saveChanges') }}
            </el-button>
            <el-button @click="resetForm">{{ t('user.reset') }}</el-button>
          </el-form-item>
        </el-form>
        
        <el-divider />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { useProfile } from '@/api/profile'
import { computed } from 'vue'


// 使用 profile 组合式函数
const {
  // 翻译函数
  t,
  
  // 响应式状态
  profileFormRef,
  loading,
  
  // 表单数据
  profileForm,
  
  // 计算属性
  computedRules,
  
  // 方法
  beforeImageUpload,
  handleImageUpload,
  submitForm,
  resetForm
} = useProfile()

// 创建计算属性，确保头像预览优先显示
const userAvatar = computed(() => {
  // 如果有待上传的文件，优先显示本地预览
  if (profileForm.headPic && profileForm.headPic.startsWith('data:')) {
    return profileForm.headPic
  }
  // 否则从store获取
  return profileForm.headPic || '/src/assets/img/dog.jpg'
})
</script>


<style scoped>
@import '@/styles/profile.css'
</style>
