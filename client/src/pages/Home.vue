<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex flex-col lg:flex-row gap-8">
      
      <!-- Cột chính: Danh sách tin tức -->
      <div class="flex-grow lg:w-3/4">
        <h1 class="text-2xl font-black border-l-4 border-red-700 pl-4 mb-8 uppercase">Tin mới nhất - Báo Lao Động</h1>
        <div v-if="loading" class="text-center py-20 animate-pulse text-gray-500">Đang tải tin tức...</div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <NewsCard v-for="(item, index) in news" :key="index" :news="item" />
        </div>
      </div>

      <!-- Cột phụ: Bảng xếp hạng và Quảng cáo -->
      <div class="lg:w-1/4 flex-shrink-0 space-y-6">
        
        <!-- Bảng xếp hạng Cây bút Vàng -->
        <div class="bg-gradient-to-br from-amber-50 to-orange-50/50 p-5 rounded-2xl border border-amber-200 shadow-sm">
          <h3 class="text-xs font-black text-gray-900 mb-4 uppercase tracking-wider flex items-center gap-1.5 border-b border-amber-200 pb-2.5">
            <span>🏆</span> Cây Bút Vàng Hội Viên
          </h3>
          <div v-if="leaderboardLoading" class="text-center py-4 text-xs text-gray-400 animate-pulse">Đang nạp bảng xếp hạng...</div>
          <div v-else-if="leaderboard.length === 0" class="text-center py-4 text-xs text-gray-400 italic">Chưa có xếp hạng thành viên.</div>
          <ul v-else class="space-y-3">
            <li 
              v-for="(user, idx) in leaderboard" 
              :key="idx"
              class="flex items-center justify-between bg-white px-3.5 py-2.5 rounded-xl border border-gray-150 shadow-sm"
            >
              <div class="flex items-center gap-2.5">
                <span 
                  :class="[
                    'w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-black',
                    idx === 0 ? 'bg-yellow-400 text-white shadow-sm' :
                    idx === 1 ? 'bg-gray-300 text-white shadow-sm' :
                    idx === 2 ? 'bg-amber-600 text-white shadow-sm' : 'bg-gray-100 text-gray-500'
                  ]"
                >
                  {{ idx + 1 }}
                </span>
                <span class="font-bold text-gray-800 text-xs md:text-sm">{{ user.username }}</span>
              </div>
              <span class="text-[10px] font-black text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full border border-amber-100">{{ user.points }} điểm</span>
            </li>
          </ul>
        </div>

        <!-- Box giới thiệu Hội viên -->
        <div class="bg-red-50 p-5 rounded-2xl border border-red-100 shadow-sm text-center">
          <span class="text-3xl">⭐</span>
          <h4 class="font-black text-red-950 mt-2 text-sm uppercase">Quyền lợi Hội Viên</h4>
          <p class="text-[11px] text-gray-600 mt-2 leading-relaxed mb-4">Đọc không giới hạn các tin độc quyền, bình luận hai chiều, và tham gia viết bài nhận điểm nhuận bút.</p>
          <router-link to="/account-management" class="bg-red-700 hover:bg-red-800 text-white font-bold text-xs px-5 py-2 rounded-full shadow-sm transition inline-block">NÂNG CẤP NGAY</router-link>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NewsCard from '../components/NewsCard.vue'

const news = ref([])
const loading = ref(true)

const leaderboard = ref([])
const leaderboardLoading = ref(true)

const fetchLeaderboard = () => {
  fetch("http://localhost:5000/api/news/leaderboard")
    .then(res => res.json())
    .then(data => {
      leaderboard.value = Array.isArray(data) ? data : []
    })
    .catch(err => console.error("Lỗi tải BXH:", err))
    .finally(() => {
      leaderboardLoading.value = false
    })
}

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

  fetchLeaderboard()
})
</script>
