<template>
  <section class="profile-page">
    <header class="page-header">
      <p class="eyebrow">{{ t('user.profile') }}</p>
      <h1>{{ t('user.editProfile') }}</h1>
      <p class="description">{{ t('user.profileDescription') }}</p>
    </header>
    <ProfileForm />
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ProfileForm from '@/modules/profile/components/ProfileForm.vue'

const { t } = useI18n()
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 1.5rem;
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--app-border-color);
  background: var(--app-surface-color);
  box-shadow: var(--app-shadow-card);
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.page-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: var(--app-text-color);
}

.page-header .description,
.page-header .eyebrow {
  margin: 0;
  color: var(--app-text-secondary);
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-size: 0.8rem;
}
</style>
