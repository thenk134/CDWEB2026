<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-black border-l-4 border-red-700 pl-4 mb-8 uppercase">Tin mới nhất - Báo Lao Động</h1>
    <div v-if="loading" class="text-center py-20 animate-pulse text-gray-500">Đang tải tin tức...</div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <NewsCard v-for="(item, index) in news" :key="index" :news="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NewsCard from '../components/NewsCard.vue'

const news = ref([])
const loading = ref(true)

onMounted(() => {
  fetch("http://localhost:5000/api/news/home-laodong")
    .then(res => res.json())
    .then(data => {
      news.value = Array.isArray(data) ? data : []
      loading.value = false
    })
    .catch(err => {
      console.error("Lỗi:", err)
      loading.value = false
    })
})
</script>
