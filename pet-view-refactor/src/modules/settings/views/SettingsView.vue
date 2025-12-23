<template>
  <PageContainer :title="t('settings.title')" :description="t('settings.accountSecuritySubtitle')">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <el-tab-pane :label="t('settings.accountSecurity')" name="security">
        <section class="settings-card">
          <header class="settings-card__header">
            <div>
              <p class="eyebrow">{{ t('settings.security') }}</p>
              <h2>{{ t('settings.changePassword') }}</h2>
              <p>{{ t('settings.changePasswordTip') }}</p>
            </div>
          </header>
          <el-form label-position="top" class="password-form">
            <el-form-item :label="t('settings.currentPassword')">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                autocomplete="off"
                show-password
                :placeholder="t('settings.currentPasswordPlaceholder')"
              />
            </el-form-item>
            <el-form-item :label="t('settings.newPassword')">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                autocomplete="off"
                show-password
                :placeholder="t('settings.newPasswordPlaceholder')"
              />
            </el-form-item>
            <el-form-item :label="t('settings.confirmPassword')">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                autocomplete="off"
                show-password
                :placeholder="t('settings.confirmPasswordPlaceholder')"
              />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                {{ t('settings.changePassword') }}
              </el-button>
              <el-button text @click="resetPasswordForm" :disabled="passwordLoading">
                {{ t('user.reset') }}
              </el-button>
            </div>
          </el-form>
        </section>

        <section class="settings-card">
          <header class="settings-card__header">
            <div>
              <p class="eyebrow">{{ t('settings.securityTips') }}</p>
              <h2>{{ t('settings.securityTipsSubtitle') }}</h2>
            </div>
          </header>
          <ul class="security-tips">
            <li>{{ t('settings.securityTip1') }}</li>
            <li>{{ t('settings.securityTip2') }}</li>
            <li>{{ t('settings.securityTip3') }}</li>
          </ul>
        </section>
      </el-tab-pane>

      <el-tab-pane :label="t('settings.loginHistory')" name="history">
        <section class="settings-card">
          <header class="settings-card__header">
            <div>
              <p class="eyebrow">{{ t('settings.recentLoginRecords') }}</p>
              <h2>{{ t('settings.viewLoginHistory') }}</h2>
            </div>
            <div class="history-actions">
              <el-button text @click="loadLoginHistory" :loading="historyLoading">
                {{ t('common.refresh') }}
              </el-button>
              <el-button
                type="danger"
                text
                :loading="historyClearing"
                :disabled="!loginHistory.length"
                @click="handleClearHistory"
              >
                {{ t('settings.clearHistory') }}
              </el-button>
            </div>
          </header>

          <el-skeleton v-if="historyLoading" animated :count="2" />
          <el-empty v-else-if="!loginHistory.length" :description="t('settings.loginHistoryFailed')" />
          <div v-else class="history-list">
            <article v-for="item in loginHistory" :key="item.id" class="history-item">
              <div>
                <p class="history-time">{{ formatDateTime(item.loginTime) }}</p>
                <p class="history-device">{{ item.device || t('settings.unknownLocation') }}</p>
                <p class="history-location">{{ item.location || t('settings.unknownLocation') }}</p>
              </div>
              <div class="history-meta">
                <span>IP {{ item.ipAddress }}</span>
                <el-tag :type="item.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                  {{ item.status === 'SUCCESS' ? t('settings.loginSuccess') : t('settings.loginFailed') }}
                </el-tag>
              </div>
            </article>
          </div>
        </section>
      </el-tab-pane>
    </el-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/shared/components/common/PageContainer.vue'
import { useSettings } from '@/modules/settings/composables/useSettings'

const {
  t,
  activeTab,
  passwordForm,
  passwordLoading,
  loginHistory,
  historyLoading,
  historyClearing,
  handleChangePassword,
  handleClearHistory,
  loadLoginHistory,
  formatDateTime,
} = useSettings()

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 1.25rem;
}

.settings-card {
  border-radius: 24px;
  padding: 1.5rem;
  background: var(--app-surface-color);
  border: 1px solid var(--app-border-color);
  box-shadow: var(--app-shadow-card);
  color: var(--app-text-color);
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.settings-card + .settings-card {
  margin-top: 1rem;
}

.settings-card__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
}

.settings-card__header h2 {
  margin: 0.15rem 0;
}

.password-form {
  max-width: 520px;
}

.password-form :deep(.el-form-item) {
  margin-bottom: 1rem;
}

.password-form :deep(.el-form-item__label) {
  color: var(--app-text-secondary);
  font-weight: 600;
  padding-bottom: 0.25rem;
}

.password-form :deep(.el-input__wrapper) {
  background: var(--app-bg-color);
  border: 1px solid var(--app-border-color);
  border-radius: 14px;
  box-shadow: none;
}

.password-form :deep(.el-input__inner) {
  color: var(--app-text-color);
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-start;
}

.security-tips {
  margin: 0;
  padding-left: 1.2rem;
  color: var(--app-text-secondary);
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.history-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.history-item {
  border-radius: 18px;
  padding: 0.95rem 1.1rem;
  border: 1px solid var(--app-border-color);
  background: var(--app-bg-color);
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.history-time {
  margin: 0 0 0.25rem;
  font-weight: 600;
}

.history-device,
.history-location {
  margin: 0;
  color: var(--app-text-secondary);
  font-size: 0.9rem;
}

.history-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.35rem;
  font-size: 0.9rem;
  color: var(--app-text-secondary);
}

@media (max-width: 768px) {
  .history-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-meta {
    align-items: flex-start;
  }

  .form-actions {
    justify-content: flex-start;
  }
}
</style>
