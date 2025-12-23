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

const redirectToAdoptionList = () => {
  const petId = resolveId()
  const query = { ...route.query }
  if (petId) {
    query.pet = String(petId)
  } else {
    delete query.pet
  }
  router.replace({
    path: '/user/adoption-pets',
    query,
  })
}

onMounted(() => redirectToAdoptionList())

watch(
  () => route.params.id,
  () => redirectToAdoptionList(),
)
</script>
