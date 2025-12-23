<template></template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const resolveId = () => {
  const raw = route.params.id
  const value = Array.isArray(raw) ? raw[0] : raw
  const id = Number(value)
  return Number.isNaN(id) ? null : id
}

const redirectToList = () => {
  const petId = resolveId()
  const query = { ...route.query }
  if (petId) {
    query.edit = String(petId)
  } else {
    delete query.edit
  }
  router.replace({
    path: '/user/pets',
    query,
  })
}

onMounted(() => redirectToList())

watch(
  () => route.params.id,
  () => redirectToList(),
)
</script>
