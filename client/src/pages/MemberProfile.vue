<template>
  <main class="container mx-auto px-4 py-8 min-h-screen max-w-4xl">
    <!-- Nút quay lại -->
    <div class="mb-6">
      <button 
        @click="$router.back()" 
        class="text-gray-500 hover:text-gray-800 font-bold inline-flex items-center hover:-translate-x-1 transition-transform cursor-pointer"
      >
        <span class="mr-2">←</span> Quay lại
      </button>
    </div>

    <!-- Khung tải dữ liệu -->
    <div v-if="loading" class="text-center py-20 animate-pulse text-gray-500">
      <div class="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-4"></div>
      <div class="h-6 bg-gray-200 rounded w-1/3 mx-auto mb-2"></div>
      <div class="h-4 bg-gray-200 rounded w-1/4 mx-auto"></div>
      <p class="mt-4 text-sm font-medium">Đang tải thông tin thành viên...</p>
    </div>

    <!-- Thông báo lỗi -->
    <div v-else-if="error" class="text-center py-20 bg-red-50 rounded-2xl border border-red-100 max-w-lg mx-auto">
      <p class="text-red-600 font-bold mb-4">{{ error }}</p>
      <router-link to="/" class="bg-red-700 hover:bg-red-800 text-white font-bold px-6 py-2 rounded-full transition shadow">
        Quay lại trang chủ
      </router-link>
    </div>

    <!-- Hiển thị nội dung -->
    <div v-else-if="member" class="space-y-8">
      <!-- Thẻ thông tin Member -->
      <div class="bg-white p-8 rounded-3xl border border-gray-200 shadow-sm flex flex-col md:flex-row items-center gap-6 relative overflow-hidden">
        <!-- Background trang trí -->
        <div class="absolute -right-10 -top-10 w-40 h-40 bg-red-50 rounded-full opacity-50 pointer-events-none"></div>
        
        <!-- Avatar đại diện -->
        <div class="w-24 h-24 rounded-full bg-gradient-to-tr from-red-600 to-orange-500 text-white flex items-center justify-center text-4xl font-black shadow-md border-4 border-white select-none">
          {{ member.username.charAt(0).toUpperCase() }}
        </div>

        <!-- Chi tiết -->
        <div class="text-center md:text-left space-y-2.5 flex-1">
          <div class="flex flex-wrap items-center justify-center md:justify-start gap-2.5">
            <h1 class="text-3xl font-black text-gray-900 leading-none">@{{ member.username }}</h1>
            
            <!-- Badge Role -->
            <span 
              :class="[
                'text-[10px] font-black uppercase px-2.5 py-0.5 rounded tracking-wider shadow-sm border',
                member.role === 'ADMIN' ? 'bg-red-100 text-red-800 border-red-200' : 
                member.role === 'MEMBER' ? 'bg-purple-100 text-purple-800 border-purple-200' : 
                'bg-gray-100 text-gray-800 border-gray-200'
              ]"
            >
              {{ member.role }}
            </span>
          </div>

          <!-- Badge Điểm tích lũy -->
          <div class="flex items-center justify-center md:justify-start gap-1.5 text-sm text-gray-500 font-bold">
            <Star class="w-4 h-4 text-amber-500 fill-amber-500" />
            <span>Điểm tích lũy:</span>
            <span class="text-amber-600 font-black text-base">{{ Number(member.points || 0).toFixed(1) }} điểm</span>
          </div>
        </div>
      </div>

      <!-- Danh sách bài viết đã xuất bản -->
      <div class="space-y-6">
        <div class="flex justify-between items-center border-b pb-4 border-gray-100">
          <h2 class="text-xl font-black text-gray-800 uppercase tracking-tight flex items-center gap-2">
            <FileText class="w-5 h-5 text-red-700" />
            <span>Bài viết đã xuất bản ({{ articles.length }})</span>
          </h2>
        </div>

        <div v-if="articles.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Card bài viết -->
          <div 
            v-for="article in articles" 
            :key="article.id" 
            class="relative group bg-white rounded-2xl shadow-sm hover:shadow-md transition border border-gray-200 overflow-hidden flex flex-col"
          >
            <!-- Label VIP/Hội viên nếu có -->
            <div v-if="article.exclusive" class="absolute top-2 left-2 bg-gradient-to-r from-amber-500 to-orange-500 text-white font-black text-[10px] px-2.5 py-1 rounded-full uppercase shadow tracking-wider z-10 flex items-center gap-1">
              <Star class="w-3 h-3 text-white fill-white" />
              <span>Hội Viên</span>
            </div>

            <!-- Thumbnail -->
            <img 
              :src="article.image" 
              :alt="article.title" 
              class="w-full h-44 object-cover" 
            />

            <!-- Body -->
            <div class="p-5 flex-1 flex flex-col justify-between">
              <div>
                <div class="flex items-center gap-3 text-xs text-gray-400 mb-2">
                  <span class="flex items-center gap-1"><Clock class="w-3.5 h-3.5 text-gray-400" /> {{ formatDate(article.pubDate) }}</span>
                  <span class="flex items-center gap-1"><Eye class="w-3.5 h-3.5 text-gray-400" /> {{ article.views }} lượt xem</span>
                </div>
                <h3 class="font-bold text-gray-900 mb-2 line-clamp-2 group-hover:text-red-700 transition">
                  {{ article.title }}
                </h3>
                <p class="text-gray-500 text-xs line-clamp-2 mb-4 leading-relaxed">
                  {{ article.description }}
                </p>
              </div>

              <router-link 
                :to="{
                  path: '/news-detail',
                  query: {
                    url: article.link,
                    date: article.pubDate
                  }
                }" 
                class="text-red-700 font-bold flex items-center hover:text-red-800 transition text-sm cursor-pointer"
              >
                Đọc bài viết <span class="ml-1.5 transition-transform group-hover:translate-x-1">→</span>
              </router-link>
            </div>
          </div>
        </div>

        <!-- Trạng thái trống -->
        <div v-else class="text-center py-16 bg-gray-50 rounded-2xl border border-dashed border-gray-200 text-gray-400">
          <p class="font-medium text-base mb-1">Thành viên này chưa có bài viết nào được xuất bản.</p>
          <p class="text-xs">Các bài viết mới sẽ xuất hiện tại đây sau khi được kiểm duyệt.</p>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Star, FileText, Clock, Eye } from 'lucide-vue-next'

const props = defineProps({
  username: {
    type: String,
    required: true
  }
})

const member = ref(null)
const articles = ref([])
const loading = ref(true)
const error = ref("")

const fetchMemberProfile = async () => {
  loading.value = true
  error.value = ""
  try {
    const res = await fetch(`http://localhost:5000/api/members/${props.username}`)
    const data = await res.json()
    if (!res.ok) {
      throw new Error(data.message || "Lỗi tải thông tin thành viên")
    }
    member.value = data.profile
    articles.value = data.articles || []
  } catch (err) {
    console.error(err)
    error.value = err.message
  } finally {
    fontDateCheck()
  }
}

const fontDateCheck = () => {
  loading.value = false
}

const formatDate = (dateStr) => {
  if (!dateStr) return ""
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    })
  } catch (e) {
    return dateStr
  }
}

onMounted(() => {
  fetchMemberProfile()
})

watch(() => props.username, () => {
  fetchMemberProfile()
})
</script>
