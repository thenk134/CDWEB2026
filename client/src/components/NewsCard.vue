<template>
  <div class="relative group bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow border border-gray-100">
    <button 
      @click="handleBookmark" 
      :class="[
        'absolute top-2 right-2 p-2 rounded-full shadow-md z-10 transition-colors',
        isSaved ? 'bg-red-600 text-white' : 'bg-white text-gray-400 hover:text-red-500'
      ]"
      :title="isSaved ? 'Bỏ lưu' : 'Lưu tin này'"
    >
      📌
    </button>
    <img 
      :src="news.image" 
      :alt="news.title" 
      class="w-full h-48 object-cover" 
    />
    <div class="p-5">
      <div class="flex items-center text-xs text-gray-400 mb-2">
         <span class="mr-1">🕒</span>
         <span>{{ formatDate(news.date || news.pubDate) }}</span>
      </div>
      <h3 class="font-bold text-lg text-gray-900 mb-3 line-clamp-2">
        {{ news.title }}
      </h3>
      <router-link 
        :to="{
          path: '/news-detail',
          query: {
            url: news.link,
            date: news.date || news.pubDate
          }
        }" 
        class="text-red-700 font-bold flex items-center hover:text-red-800 transition-colors text-sm"
      >
        Xem chi tiết <span class="ml-2">→</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  news: {
    type: Object,
    required: true
  }
})

const isSaved = ref(false)
const dbArticleId = ref(null)

const checkSavedStatus = () => {
  const token = localStorage.getItem("token")
  if (token) {
    const url = props.news.link
    if (!url) return
    
    fetch(`http://localhost:5000/api/bookmarks/check?link=${encodeURIComponent(url)}`, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
      .then(res => res.json())
      .then(data => {
        isSaved.value = data.bookmarked
        if (data.articleId) {
          dbArticleId.value = data.articleId
        }
      })
      .catch(err => console.error("Lỗi check bookmark card:", err))
  } else {
    const savedList = JSON.parse(localStorage.getItem('saved_news')) || []
    isSaved.value = savedList.some(item => item.link === props.news.link)
  }
}

onMounted(() => {
  checkSavedStatus()
})

watch(() => props.news.link, () => {
  checkSavedStatus()
})

const handleBookmark = () => {
  const token = localStorage.getItem("token")
  if (!token) {
    alert("Vui lòng đăng nhập để lưu tin tức vào tài khoản!")
    return
  }

  if (isSaved.value) {
    if (!dbArticleId.value) return
    fetch(`http://localhost:5000/api/bookmarks/${dbArticleId.value}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
      .then(res => res.json())
      .then(() => {
        isSaved.value = false
        dbArticleId.value = null
        alert("Đã xóa khỏi danh sách lưu tin!")
      })
      .catch(err => console.error("Lỗi xoá bookmark card:", err))
  } else {
    fetch("http://localhost:5000/api/bookmarks", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({
        link: props.news.link
      })
    })
      .then(res => res.json())
      .then(() => {
        isSaved.value = true
        alert("Lưu bài viết thành công!")
        checkSavedStatus()
      })
      .catch(err => console.error("Lỗi lưu bookmark card:", err))
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ""
  try {
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN', {
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
</script>
