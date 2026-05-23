<template>
  <div class="container mx-auto px-4 py-8 min-h-screen">
    <h1 class="text-2xl font-black border-l-4 border-blue-600 pl-4 mb-8 uppercase text-blue-800">
      Tin đã lưu ({{ savedNews.length }})
    </h1>

    <div v-if="savedNews.length === 0" class="text-center py-20 bg-gray-50 rounded-xl">
      <p class="text-gray-500 mb-4"> 
        Bạn chưa lưu tin tức nào cả.
      </p>
      <router-link to="/" class="bg-red-700 text-white px-6 py-2 rounded hover:bg-red-800 inline-block">
        Quay về trang chủ đọc báo
      </router-link>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- Dùng :key="item.link" vì index có thể thay đổi khi xóa tin khỏi Bookmarks -->
      <NewsCard v-for="item in savedNews" :key="item.link" :news="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NewsCard from '../components/NewsCard.vue'

const savedNews = ref([])

onMounted(() => {
  const data = JSON.parse(localStorage.getItem("saved_news")) || []
  savedNews.value = data
})
</script>
