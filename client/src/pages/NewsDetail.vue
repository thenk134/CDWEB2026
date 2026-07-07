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
      <router-link to="/" class="text-blue-600 font-bold inline-flex items-center hover:translate-x-[-4px] transition-transform">
        <span class="mr-2">←</span> Quay lại
      </router-link>
      
      <div class="flex items-center space-x-2">
        <!-- Nút Lưu tin đồng bộ CSDL -->
        <button 
          @click="toggleBookmark" 
          class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-full flex items-center space-x-1.5 text-xs font-semibold shadow-sm transition"
        >
          <span>{{ isBookmarked ? '📌 Đã lưu' : '⭐ Lưu tin' }}</span>
        </button>

        <div class="bg-gray-100 px-4 py-2 rounded-full flex items-center space-x-1 text-xs text-gray-600 font-semibold shadow-sm">
          <span>📅</span>
          <span>{{ formatDate(articleDate || article.date) }}</span>
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
    
    <!-- Thử nghiệm đọc thử banner -->
    <div v-if="article.isExclusive && !article.isLocked && article.freeTrialsLeft >= 0 && article.freeTrialsLeft < 999" class="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-lg text-xs md:text-sm text-amber-800 font-bold flex items-center gap-2">
      💡 Bạn đang xem tin độc quyền bằng lượt đọc thử. Lượt đọc thử còn lại trong tháng: <span class="text-red-700 text-base font-black">{{ article.freeTrialsLeft }}</span> lượt.
    </div>

    <!-- Nội dung chi tiết -->
    <div v-if="!article.isLocked"
      class="news-content-body text-gray-800 text-lg leading-relaxed space-y-6"
      v-html="article.content" 
    />

    <!-- Giao diện khóa Premium Paywall -->
    <div v-else class="my-10 p-8 rounded-xl border border-amber-200 bg-amber-50/50 text-center shadow-lg max-w-xl mx-auto">
      <div class="text-5xl mb-4">🔒</div>
      <h3 class="text-xl font-black text-gray-900 mb-3 uppercase tracking-wide">Nội Dung Độc Quyền Dành Cho Hội Viên</h3>
      <p class="text-gray-600 text-sm mb-6 leading-relaxed">
        Bài viết này thuộc danh mục nội dung chất lượng cao giới hạn. 
        <span v-if="article.freeTrialsLeft === 0">Bạn đã sử dụng hết 3 lượt đọc thử miễn phí trong tháng này.</span>
        <span v-else>Vui lòng đăng nhập tài khoản để nhận 3 lượt đọc thử miễn phí, hoặc nâng cấp Hội viên để xem không giới hạn.</span>
      </p>
      <div class="flex flex-wrap justify-center gap-4">
        <router-link to="/account-management" class="bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-bold px-6 py-2.5 rounded-full text-xs md:text-sm shadow-md transition-all hover:-translate-y-0.5">
          ⭐ Đăng ký Hội Viên ngay
        </router-link>
        <router-link to="/login" v-if="!isLoggedIn" class="bg-white border border-gray-300 text-gray-700 font-bold px-6 py-2.5 rounded-full text-xs md:text-sm hover:bg-gray-50 transition">
          Đăng nhập
        </router-link>
      </div>
    </div>

    <!-- Khu vực Tranh luận 2 chiều (Chỉ xuất hiện khi có bài viết và không bị khóa) -->
    <div v-if="article && !article.isLocked" class="mt-16 pt-10 border-t border-gray-100">
      <div class="text-center mb-8">
        <h2 class="text-2xl font-black text-gray-800 uppercase tracking-tight">🗣️ Tranh Luận Độc Giả</h2>
        <p class="text-gray-500 text-xs mt-1">Cộng đồng hội viên bình luận đa chiều và tôn trọng quan điểm trái chiều</p>
        
        <!-- Biểu đồ khảo sát tỷ lệ -->
        <div class="mt-6 max-w-md mx-auto bg-gray-100 h-6 rounded-full overflow-hidden flex text-white font-bold text-[10px] shadow-inner">
          <div 
            :style="{ width: agreePercentage + '%' }" 
            class="bg-emerald-600 flex items-center justify-center transition-all duration-500"
          >
            {{ agreePercentage > 0 ? `Đồng ý ${agreePercentage}%` : '' }}
          </div>
          <div 
            :style="{ width: disagreePercentage + '%' }" 
            class="bg-rose-600 flex items-center justify-center transition-all duration-500"
          >
            {{ disagreePercentage > 0 ? `Phản đối ${disagreePercentage}%` : '' }}
          </div>
        </div>
      </div>

      <!-- Chia 2 cột bình luận -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <!-- Cột Trái: Đồng ý -->
        <div class="space-y-4">
          <div class="bg-emerald-50 border-l-4 border-emerald-600 p-3 rounded-r-lg font-bold text-emerald-800 text-sm flex justify-between">
            <span>🟢 ĐỒNG Ý / ỦNG HỘ</span>
            <span>{{ commentsAgree.length }} bình luận</span>
          </div>
          <div v-if="commentsAgree.length === 0" class="text-center py-6 text-gray-400 text-xs italic bg-gray-50 rounded-lg">
            Chưa có bình luận đồng ý nào.
          </div>
          <div 
            v-for="comment in commentsAgree" 
            :key="comment.id" 
            class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm relative group hover:border-emerald-200 transition"
          >
            <div class="flex justify-between items-start gap-2 mb-2">
              <div>
                <span class="font-bold text-gray-800 text-sm">{{ comment.username }}</span>
                <span class="ml-1.5 text-[9px] px-1.5 py-0.5 rounded font-black bg-purple-100 text-purple-800 uppercase" v-if="comment.role === 'MEMBER'">Hội viên</span>
                <span class="ml-1.5 text-[9px] px-1.5 py-0.5 rounded font-black bg-red-100 text-red-800 uppercase" v-if="comment.role === 'ADMIN'">Admin</span>
              </div>
              <span class="text-[10px] text-gray-400 font-mono">{{ formatCommentDate(comment.createdDate) }}</span>
            </div>
            <p class="text-gray-700 text-sm leading-relaxed mb-3 whitespace-pre-wrap">{{ comment.content }}</p>
            <div class="flex items-center gap-1.5">
              <button 
                @click="voteComment(comment.id)"
                class="bg-gray-100 hover:bg-emerald-50 text-gray-500 hover:text-emerald-700 text-[11px] px-2.5 py-1 rounded-full font-bold border border-gray-200 hover:border-emerald-200 transition flex items-center gap-1 cursor-pointer"
              >
                👍 Ủng hộ ({{ comment.upvotes }})
              </button>
            </div>
          </div>
        </div>

        <!-- Cột Phải: Phản đối -->
        <div class="space-y-4">
          <div class="bg-rose-50 border-l-4 border-rose-600 p-3 rounded-r-lg font-bold text-rose-800 text-sm flex justify-between">
            <span>🔴 PHẢN ĐỐI / Ý KIẾN KHÁC</span>
            <span>{{ commentsDisagree.length }} bình luận</span>
          </div>
          <div v-if="commentsDisagree.length === 0" class="text-center py-6 text-gray-400 text-xs italic bg-gray-50 rounded-lg">
            Chưa có bình luận phản đối nào.
          </div>
          <div 
            v-for="comment in commentsDisagree" 
            :key="comment.id" 
            class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm relative group hover:border-rose-200 transition"
          >
            <div class="flex justify-between items-start gap-2 mb-2">
              <div>
                <span class="font-bold text-gray-800 text-sm">{{ comment.username }}</span>
                <span class="ml-1.5 text-[9px] px-1.5 py-0.5 rounded font-black bg-purple-100 text-purple-800 uppercase" v-if="comment.role === 'MEMBER'">Hội viên</span>
                <span class="ml-1.5 text-[9px] px-1.5 py-0.5 rounded font-black bg-red-100 text-red-800 uppercase" v-if="comment.role === 'ADMIN'">Admin</span>
              </div>
              <span class="text-[10px] text-gray-400 font-mono">{{ formatCommentDate(comment.createdDate) }}</span>
            </div>
            <p class="text-gray-700 text-sm leading-relaxed mb-3 whitespace-pre-wrap">{{ comment.content }}</p>
            <div class="flex items-center gap-1.5">
              <button 
                @click="voteComment(comment.id)"
                class="bg-gray-100 hover:bg-rose-50 text-gray-500 hover:text-rose-700 text-[11px] px-2.5 py-1 rounded-full font-bold border border-gray-200 hover:border-rose-200 transition flex items-center gap-1 cursor-pointer"
              >
                👍 Ủng hộ ({{ comment.upvotes }})
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Khung đăng bình luận mới -->
      <div class="bg-gray-50 p-6 rounded-2xl border border-gray-200 shadow-sm mb-10">
        <h3 class="text-base font-bold text-gray-800 mb-4 flex items-center gap-1.5">
          <span>✍️</span> Tham gia tranh luận luận điểm
        </h3>

        <div v-if="!isLoggedIn" class="text-center py-4">
          <p class="text-gray-500 text-sm mb-3">Bạn cần đăng nhập để tham gia tranh luận.</p>
          <router-link to="/login" class="bg-red-700 hover:bg-red-800 text-white font-bold text-xs px-5 py-2 rounded-full shadow-sm transition inline-block">ĐĂNG NHẬP NGAY</router-link>
        </div>
        <div v-else-if="userRole !== 'MEMBER' && userRole !== 'ADMIN'" class="text-center py-4">
          <p class="text-gray-500 text-sm mb-3">Tính năng gửi tranh luận chỉ dành cho tài khoản Hội viên.</p>
          <router-link to="/account-management" class="bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-bold text-xs px-6 py-2 rounded-full shadow-sm transition-all inline-block hover:-translate-y-0.5">⭐ NÂNG CẤP HỘI VIÊN</router-link>
        </div>
        <form v-else @submit.prevent="submitComment" class="space-y-4">
          <textarea 
            v-model="newCommentContent"
            required
            rows="3"
            placeholder="Nêu luận điểm hoặc quan điểm tranh luận của bạn một cách văn minh..."
            class="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition bg-white text-sm"
          ></textarea>
          
          <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <!-- Chọn vị trí lập luận -->
            <div class="flex items-center gap-3">
              <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">Lập trường:</span>
              <button 
                type="button" 
                @click="newCommentIsAgree = true"
                :class="[
                  'px-4 py-1.5 rounded-full text-xs font-bold transition-all border shadow-sm cursor-pointer',
                  newCommentIsAgree ? 'bg-emerald-600 text-white border-emerald-600 shadow-md scale-105' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'
                ]"
              >
                🟢 Đồng ý với tác giả
              </button>
              <button 
                type="button" 
                @click="newCommentIsAgree = false"
                :class="[
                  'px-4 py-1.5 rounded-full text-xs font-bold transition-all border shadow-sm cursor-pointer',
                  !newCommentIsAgree ? 'bg-rose-600 text-white border-rose-600 shadow-md scale-105' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'
                ]"
              >
                🔴 Phản đối / Ý kiến khác
              </button>
            </div>
            
            <button 
              type="submit" 
              :disabled="commentLoading"
              class="w-full sm:w-auto bg-red-700 hover:bg-red-800 text-white font-bold px-6 py-2 rounded-full shadow transition disabled:opacity-50 text-xs md:text-sm cursor-pointer"
            >
              {{ commentLoading ? 'Đang gửi...' : 'GỬI BÌNH LUẬN' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <footer class="mt-12 pt-8 border-t border-gray-100 flex justify-between items-center text-gray-400 italic text-sm">
      <span>Nguồn: {{ getHostname(targetUrl || article.link) }}</span>
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
import { useRoute, useRouter } from 'vue-router'
import { toast } from '../utils/toast'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)

const isBookmarked = ref(false)
const dbArticleId = ref(null)

const targetUrl = computed(() => route.query.url || (article.value ? article.value.link : ""))
const articleDate = computed(() => route.query.date)

const fetchDetail = (url) => {
  if (!url) return
  loading.value = true
  const token = localStorage.getItem("token")
  const headers = {}
  if (token) {
    headers["Authorization"] = `Bearer ${token}`
  }
  
  fetch(`http://localhost:5000/api/news-detail?url=${encodeURIComponent(url)}`, {
    headers: headers
  })
    .then(res => {
      if (res.status === 404) {
        router.push({ name: 'NotFound' })
        throw new Error("Không tìm thấy bài viết")
      }
      if (!res.ok) throw new Error("Lỗi máy chủ")
      return res.json()
    })
    .then(data => {
      article.value = data
      loading.value = false
      if (data.id) {
        fetchComments(data.id)
      }
    })
    .catch(err => {
      console.error("Lỗi:", err)
      loading.value = false
    })
    .finally(() => {
      loading.value = false
    })
}

const checkBookmarkStatus = (url) => {
  const token = localStorage.getItem("token")
  if (!token || !url) {
    isBookmarked.value = false
    return
  }

  fetch(`http://localhost:5000/api/bookmarks/check?link=${encodeURIComponent(url)}`, {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => res.json())
    .then(data => {
      isBookmarked.value = data.bookmarked
      if (data.articleId) {
        dbArticleId.value = data.articleId
      }
    })
    .catch(err => console.error("Lỗi check bookmark:", err))
}

const toggleBookmark = () => {
  const token = localStorage.getItem("token")
  if (!token) {
    toast.info("Vui lòng đăng nhập để sử dụng tính năng lưu tin tức!")
    router.push("/login")
    return
  }

  const url = targetUrl.value || (article.value ? article.value.link : "")
  if (!url) {
    toast.error("Không tìm thấy đường dẫn bài viết để lưu!")
    return
  }

  if (isBookmarked.value) {
    // Xoá lưu tin
    if (!dbArticleId.value) return
    fetch(`http://localhost:5000/api/bookmarks/${dbArticleId.value}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    })
      .then(res => res.json())
      .then(data => {
        isBookmarked.value = false
        dbArticleId.value = null
        toast.success("Đã xóa khỏi danh sách lưu tin!")
      })
      .catch(err => console.error("Lỗi xoá lưu tin:", err))
  } else {
    // Thêm lưu tin
    fetch("http://localhost:5000/api/bookmarks", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({
        link: url
      })
    })
      .then(res => res.json())
      .then(data => {
        isBookmarked.value = true
        toast.success("Lưu tin tức thành công!")
        checkBookmarkStatus(url)
      })
      .catch(err => console.error("Lỗi lưu tin:", err))
  }
}

watch(targetUrl, (newUrl) => {
  if (newUrl) {
    fetchDetail(newUrl)
    checkBookmarkStatus(newUrl)
  }
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

// Tranh luận & Bình luận
const commentsAgree = ref([])
const commentsDisagree = ref([])
const newCommentContent = ref("")
const newCommentIsAgree = ref(true)
const commentLoading = ref(false)
const userRole = ref(localStorage.getItem("user_role") || "USER")
const isLoggedIn = computed(() => !!localStorage.getItem("token"))

const fetchComments = (articleId) => {
  if (!articleId) return
  fetch(`http://localhost:5000/api/news/${articleId}/comments`)
    .then(res => res.json())
    .then(data => {
      commentsAgree.value = data.agree || []
      commentsDisagree.value = data.disagree || []
    })
    .catch(err => console.error("Lỗi tải bình luận:", err))
}

const submitComment = () => {
  if (!newCommentContent.value.trim()) return
  commentLoading.value = true
  const token = localStorage.getItem("token")
  
  fetch(`http://localhost:5000/api/news/${article.value.id}/comments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      content: newCommentContent.value.trim(),
      isAgree: newCommentIsAgree.value
    })
  })
    .then(async res => {
      const data = await res.json()
      if (!res.ok) throw new Error(data.message || "Lỗi gửi bình luận")
      return data
    })
    .then(data => {
      newCommentContent.value = ""
      toast.success(data.message)
      fetchComments(article.value.id)
    })
    .catch(err => {
      toast.error(err.message)
    })
    .finally(() => {
      commentLoading.value = false
    })
}

const voteComment = (commentId) => {
  const token = localStorage.getItem("token")
  if (!token) {
    toast.info("Vui lòng đăng nhập để ủng hộ luận điểm này!")
    return
  }
  
  fetch(`http://localhost:5000/api/comments/${commentId}/vote`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(async res => {
      const data = await res.json()
      if (!res.ok) throw new Error(data.message || "Lỗi ủng hộ")
      return data
    })
    .then(data => {
      toast.success(data.message)
      fetchComments(article.value.id)
    })
    .catch(err => {
      toast.error(err.message)
    })
}

const agreePercentage = computed(() => {
  const total = commentsAgree.value.length + commentsDisagree.value.length
  if (total === 0) return 50
  return Math.round((commentsAgree.value.length / total) * 100)
})

const disagreePercentage = computed(() => {
  const total = commentsAgree.value.length + commentsDisagree.value.length
  if (total === 0) return 50
  return Math.round((commentsDisagree.value.length / total) * 100)
})

const formatCommentDate = (dateStr) => {
  if (!dateStr) return ""
  try {
    const d = new Date(dateStr)
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })
  } catch (e) {
    return dateStr
  }
}
</script>
