<template>
  <div class="container mx-auto px-4 py-8 min-h-screen">
    <h1 class="text-2xl font-black border-l-4 border-red-700 pl-4 mb-8 uppercase text-red-700 tracking-tight">
      Tin đã lưu ({{ savedNews.length }})
    </h1>

    <div v-if="loading" class="text-center py-20 text-gray-500">
      Đang tải tin tức đã lưu...
    </div>

    <div v-else-if="!isLoggedIn" class="text-center py-20 bg-gray-50 rounded-xl border border-dashed border-gray-300">
      <p class="text-gray-500 mb-4 font-medium"> 
        Bạn cần đăng nhập để sử dụng tính năng lưu tin tức đồng bộ.
      </p>
      <router-link to="/login" class="bg-red-700 text-white px-6 py-2.5 rounded-lg hover:bg-red-800 inline-block font-bold shadow transition">
        Đăng nhập ngay
      </router-link>
    </div>

    <div v-else-if="savedNews.length === 0" class="text-center py-20 bg-gray-50 rounded-xl">
      <p class="text-gray-500 mb-4"> 
        Bạn chưa lưu tin tức nào trong tài khoản cả.
      </p>
      <router-link to="/" class="bg-red-700 text-white px-6 py-2.5 rounded-lg hover:bg-red-800 inline-block font-bold shadow transition">
        Quay về trang chủ đọc báo
      </router-link>
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <NewsCard v-for="item in savedNews" :key="item.id" :news="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NewsCard from '../components/NewsCard.vue'

const savedNews = ref([])
const loading = ref(true)
const isLoggedIn = ref(false)

const fetchBookmarks = () => {
  const token = localStorage.getItem("token")
  if (!token) {
    isLoggedIn.value = false
    loading.value = false
    return
  }

  isLoggedIn.value = true
  loading.value = true

  fetch("http://localhost:5000/api/bookmarks", {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => {
      if (res.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("username")
        localStorage.removeItem("user_role")
        isLoggedIn.value = false
        return []
      }
      return res.json()
    })
    .then(data => {
      savedNews.value = Array.isArray(data) ? data : []
    })
    .catch(err => {
      console.error("Lỗi lấy bookmarks:", err)
      savedNews.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(() => {
  fetchBookmarks()
})
</script>
