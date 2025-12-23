<template>
  <section class="page-container">
    <header v-if="showHeader" class="page-container__header">
      <div>
        <h1>{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </div>
      <div class="page-container__actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="page-container__body" :class="{ 'no-padding': !padded }">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
    padded?: boolean
  }>(),
  {
    padded: true,
  },
)

const showHeader = computed(() => Boolean(props.title || props.description || useSlots().actions))
</script>

<style scoped>
.page-container {
  background: var(--app-surface-color);
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--app-border-color);
  box-shadow: var(--app-shadow-card);
  padding: 1.5rem 2rem;
}

.page-container__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.page-container__header h1 {
  margin: 0;
  font-size: 1.75rem;
  color: var(--app-text-color);
}

.page-container__header p {
  margin: 0.25rem 0 0;
  color: var(--app-text-secondary);
}

.page-container__actions :deep(*) {
  margin-left: 0.5rem;
}

.page-container__body.no-padding {
  padding: 0;
}
</style>
