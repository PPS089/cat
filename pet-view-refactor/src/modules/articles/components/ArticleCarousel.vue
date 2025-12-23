<template>
  <el-carousel height="360px" indicator-position="outside" arrow="hover">
    <el-carousel-item v-for="slide in slides" :key="slide.title">
      <div class="carousel-item" :style="{ backgroundImage: `url(${slide.image})` }">
        <div class="carousel-content">
          <p>{{ slide.category }}</p>
          <h2>{{ slide.title }}</h2>
          <el-button type="primary" @click="$emit('cta')">{{ slide.cta }}</el-button>
        </div>
      </div>
    </el-carousel-item>
  </el-carousel>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const slides = computed(() => [
  {
    category: t('article.carousel.tipsCategory'),
    title: t('article.carousel.tipsTitle'),
    image:
      'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1600&q=80',
    cta: t('article.carousel.tipsCta'),
  },
  {
    category: t('article.carousel.storiesCategory'),
    title: t('article.carousel.storiesTitle'),
    image:
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1600&q=80',
    cta: t('article.carousel.storiesCta'),
  },
  {
    category: t('article.carousel.guidesCategory'),
    title: t('article.carousel.guidesTitle'),
    image:
      'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=1600&q=80',
    cta: t('article.carousel.guidesCta'),
  },
])
</script>

<style scoped>
.carousel-item {
  width: 100%;
  height: 100%;
  border-radius: 16px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding: 2rem;
}

.carousel-content {
  background: rgba(15, 23, 42, 0.65);
  padding: 1.5rem;
  border-radius: 16px;
  color: #fff;
  max-width: 60%;
}

.carousel-content p {
  margin: 0;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.2em;
}

.carousel-content h2 {
  margin: 0.5rem 0 1rem;
  font-size: clamp(1.5rem, 3vw, 2.4rem);
}
</style>
