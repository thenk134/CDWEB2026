<template>
  <main class="container mx-auto px-4 py-8 max-w-5xl min-h-screen">
    <h2 class="text-2xl font-bold mb-6">
      Kết quả tìm kiếm cho: <span class="text-red-700">"{{ query }}"</span>
    </h2>

    <div v-if="loading" class="text-center py-10">Đang tìm kiếm tin tức từ hệ thống...</div>
    
    <div v-else-if="results.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <NewsCard v-for="(item, index) in results" :key="index" :news="item" />
    </div>
    
    <div v-else class="text-center py-20 border rounded-lg bg-gray-50">
      <p class="text-gray-500 italic">Không tìm thấy tin tức nào khớp với từ khóa của bạn.</p>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import NewsCard from '../components/NewsCard.vue'

const route = useRoute()
const results = ref([])
const loading = ref(true)

const query = computed(() => route.query.q || "")

const fetchSearch = (searchQuery) => {
  if (!searchQuery.trim()) {
    results.value = []
    loading.value = false
    return
  }

  loading.value = true
  fetch(`http://localhost:5000/api/news/search?query=${encodeURIComponent(searchQuery.trim())}`)
    .then(res => res.json())
    .then(data => {
      results.value = Array.isArray(data) ? data : []
      loading.value = false
    })
    .catch(err => {
      console.error("Lỗi tìm kiếm:", err)
      results.value = []
      loading.value = false
    })
}

watch(query, (newQuery) => {
  fetchSearch(newQuery)
}, { immediate: true })
</script>
