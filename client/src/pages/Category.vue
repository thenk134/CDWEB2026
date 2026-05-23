<template>
  <div class="container mx-auto px-4 py-8 min-h-screen">
    <!-- Tiêu đề chuyên mục với màu sắc NLĐ -->
    <h2 class="text-2xl font-black border-l-4 border-red-700 pl-4 mb-8 uppercase text-red-700 tracking-tight">
      Chuyên mục: {{ slug ? slug.replace(/-/g, ' ') : '' }}
    </h2>

    <div v-if="loading">
      <!-- Hiệu ứng Loading Skeleton đơn giản -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="bg-gray-200 h-64 rounded-xl animate-pulse"></div>
      </div>
    </div>
    
    <div v-else-if="news.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <NewsCard v-for="(item, index) in news" :key="index" :news="item" />
    </div>

    <!-- Thông báo khi không có tin -->
    <div v-else class="text-center py-20 text-gray-500">
      <p class="text-xl">Hiện chưa có tin mới trong chuyên mục này.</p>
      <p class="text-sm italic">Vui lòng quay lại sau.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import NewsCard from '../components/NewsCard.vue'

const route = useRoute()
const news = ref([])
const loading = ref(true)

const slug = computed(() => route.params.slug)

const fetchNews = (categorySlug) => {
  if (!categorySlug) return
  loading.value = true
  fetch(`http://localhost:5000/api/news/tuoitre/${categorySlug}`)
    .then(res => res.json())
    .then(data => {
      news.value = Array.isArray(data) ? data : []
      loading.value = false
    })
    .catch(err => {
      console.error("Lỗi:", err)
      news.value = []
      loading.value = false
    })
}

watch(slug, (newSlug) => {
  fetchNews(newSlug)
}, { immediate: true })
</script>
