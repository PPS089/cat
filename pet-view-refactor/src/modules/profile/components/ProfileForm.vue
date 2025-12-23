<template>
  <div class="profile-panel" v-loading="loading">
    <div class="profile-section">
      <div class="avatar-row">
        <span class="avatar-label">{{ t('user.currentAvatar') }}</span>
        <div class="avatar-content">
          <div class="avatar-frame">
            <img :src="avatarPreview" alt="avatar" decoding="async" fetchpriority="high" @error="onAvatarError" />
          </div>
          <el-upload
            class="avatar-upload"
            action="#"
            accept="image/*"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarSelect"
            :disabled="saving"
          >
            <el-button plain :disabled="saving" class="avatar-button">
              {{ t('user.changeAvatar') }}
            </el-button>
          </el-upload>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="formModel"
        :rules="rules"
        label-width="120px"
        class="profile-form"
      >
        <el-form-item :label="t('user.username')" prop="userName">
          <el-input
            v-model="formModel.userName"
            :placeholder="t('common.pleaseEnter') + t('user.username')"
            @input="markDirty"
          />
        </el-form-item>
        <el-form-item :label="t('user.email')" prop="email">
          <el-input
            v-model="formModel.email"
            :placeholder="t('common.pleaseEnter') + t('user.email')"
            @input="markDirty"
          />
        </el-form-item>
        <el-form-item :label="t('user.phone')" prop="phone">
          <el-input
            v-model="formModel.phone"
            :placeholder="t('common.pleaseEnter') + t('user.phone')"
            @input="markDirty"
          />
        </el-form-item>
        <el-form-item prop="introduce" :label="t('user.bio')">
          <el-input
            v-model="formModel.introduce"
            type="textarea"
            :rows="4"
            :placeholder="t('common.pleaseEnter') + t('user.bio') + t('common.optional')"
            @input="markDirty"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button
            type="primary"
            :loading="saving"
            :disabled="saving || !isDirty"
            class="primary-action"
            @click="submit"
          >
            {{ t('user.saveChanges') }}
          </el-button>
          <el-button text :disabled="saving" @click="reset">
            {{ t('user.reset') }}
          </el-button>
        </div>
      </el-form>
      <el-alert
        v-if="submitError"
        :title="submitError"
        type="error"
        :closable="false"
        show-icon
        class="form-alert"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useProfileForm } from '@/modules/profile/composables/useProfileForm'
import dogImage from '@/assets/img/dog.jpg'

const {
  t,
  formRef,
  formModel,
  rules,
  loading,
  saving,
  avatarPreview,
  submitError,
  isDirty,
  handleAvatarSelect,
  markDirty,
  submit,
  reset,
} = useProfileForm()

const onAvatarError = () => {
  avatarPreview.value = dogImage
}
</script>

<style scoped>
.profile-panel {
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--app-border-color);
  background: var(--app-surface-color);
  padding: 1.5rem;
  box-shadow: var(--app-shadow-card);
}

.profile-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.25rem 0 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.avatar-label {
  width: 120px;
  color: var(--app-text-secondary);
  font-size: 0.95rem;
  text-align: right;
}

.avatar-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.avatar-frame {
  width: 150px;
  height: 150px;
  border-radius: 14px;
  border: 1px solid var(--app-border-color);
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  display: none;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

.avatar-upload :deep(.el-upload) {
  width: auto;
}

.avatar-button {
  min-width: 160px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
  justify-content: center;
}

.primary-action {
  background: var(--app-primary);
  border: none;
}

.primary-action:disabled {
  opacity: 0.6;
}

.form-alert {
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .avatar-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .avatar-label {
    width: auto;
    text-align: left;
  }
  .avatar-frame {
    width: 120px;
    height: 120px;
  }
}
</style>
