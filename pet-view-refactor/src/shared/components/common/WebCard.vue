<template>
  <section class="web-card" :class="{ clickable }" @click="$emit('click')">
    <slot />
  </section>
</template>

<script setup lang="ts">
defineProps<{ clickable?: boolean }>()
</script>

<style scoped>
.web-card {
  background: var(--app-surface-color);
  border-radius: var(--app-radius-md);
  border: 1px solid var(--app-border-color);
  box-shadow: var(--app-shadow-card);
  padding: 1.25rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.web-card.clickable {
  cursor: pointer;
}

.web-card.clickable:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.15);
}
</style>
