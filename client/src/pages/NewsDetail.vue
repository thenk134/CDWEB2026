<template>
  <div v-if="loading" class="container mx-auto px-4 py-20 text-center animate-pulse">
    <div class="h-10 bg-gray-200 rounded w-3/4 mx-auto mb-6"></div>
    <div class="h-4 bg-gray-200 rounded w-full mb-3"></div>
    <div class="h-4 bg-gray-200 rounded w-5/6 mx-auto mb-3"></div>
    <p class="mt-4 text-gray-400 font-medium">Đang tải nội dung bài viết...</p>
  </div>

  <div v-else-if="!article || !article.title" class="text-center py-20">
    <p class="text-red-500 mb-4">Không tìm thấy nội dung bài viết.</p>
    <router-link to="/" class="text-blue-600 underline">Quay lại trang chủ</router-link>
  </div>

  <main v-else class="container mx-auto px-4 py-10 max-w-3xl bg-white mt-5 shadow-2xl rounded-xl border border-gray-100">
    <div class="flex justify-between items-center mb-8 border-b pb-4 border-gray-100">
      <router-link to="/" class="text-blue-600 font-bold mb-8 inline-flex items-center hover:translate-x-[-4px] transition-transform">
        <span class="mr-2">←</span> Quay lại
      </router-link>
      <div class="flex justify-end mb-4">
        <div class="bg-gray-100 px-4 py-2 rounded-full flex items-center space-x-2 text-sm text-gray-600 font-medium">
          <span>📅</span>
          <span>{{ formatDate(articleDate) }}</span>
        </div>
      </div>
    </div>
    
    <header class="mb-8">
      <h1 class="text-3xl md:text-4xl font-black text-gray-900 mb-6 leading-tight">
        {{ article.title }}
      </h1>
      
      <!-- Tóm tắt bài viết -->
      <p v-if="article.description" class="font-bold text-gray-700 mb-8 text-xl leading-relaxed border-l-4 border-red-700 pl-5 bg-gray-50 py-4 rounded-r-lg">
        {{ article.description }}
      </p>
    </header>
    
    <!-- Nội dung chi tiết - Thêm class Tailwind Typography để định dạng ảnh và chữ -->
    <div 
      class="news-content-body text-gray-800 text-lg leading-relaxed space-y-6"
      v-html="article.content" 
    />
    
    <footer class="mt-12 pt-8 border-t border-gray-100 flex justify-between items-center text-gray-400 italic text-sm">
      <span>Nguồn: {{ getHostname(targetUrl) }}</span>
      <button 
        @click="scrollToTop"
        class="text-gray-500 hover:text-red-700 not-italic font-bold"
      >
        Lên đầu trang ↑
      </button>
    </footer>
  </main>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

const targetUrl = computed(() => route.query.url)
const articleDate = computed(() => route.query.date)

const fetchDetail = (url) => {
  if (!url) return
  loading.value = true
  fetch(`http://localhost:5000/api/news-detail?url=${encodeURIComponent(url)}`)
    .then(res => res.json())
    .then(data => {
      article.value = data
      loading.value = false
    })
    .catch(err => {
      console.error("Lỗi:", err)
      loading.value = false
    })
}

watch(targetUrl, (newUrl) => {
  fetchDetail(newUrl)
}, { immediate: true })

const formatDate = (dateString) => {
  if (!dateString) return ""
  try {
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN', {
      weekday: 'long',
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch (e) {
    return dateString
  }
}

const getHostname = (urlStr) => {
  if (!urlStr) return ""
  try {
    return new URL(urlStr).hostname
  } catch (e) {
    return urlStr
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>
