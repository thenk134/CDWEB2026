<template>
  <div class="container mx-auto px-4 py-8 min-h-screen max-w-6xl">
    <!-- Tiêu đề chuyên mục -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-black border-l-4 border-red-700 pl-4 uppercase text-red-700 tracking-tight">
        {{ getCategoryLabel(slug) }}
      </h1>
      <span v-if="!loading && totalElements > 0" class="text-sm text-gray-500">
        {{ totalElements }} bài viết
      </span>
    </div>

    <!-- Skeleton loading -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <div v-for="i in 12" :key="i" class="bg-white rounded-2xl overflow-hidden shadow-sm animate-pulse">
        <div class="bg-gray-200 h-44"></div>
        <div class="p-4 space-y-2">
          <div class="bg-gray-200 h-4 rounded w-3/4"></div>
          <div class="bg-gray-200 h-3 rounded w-full"></div>
          <div class="bg-gray-200 h-3 rounded w-2/3"></div>
        </div>
      </div>
    </div>

    <!-- Danh sách tin -->
    <div v-else-if="news.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <NewsCard v-for="(item, index) in news" :key="index" :news="item" />
    </div>

    <!-- Không có tin -->
    <div v-else class="text-center py-24 text-gray-500">
      <p class="text-xl font-bold mb-2">Chưa có tin trong chuyên mục này</p>
      <p class="text-sm italic">Vui lòng quay lại sau.</p>
    </div>

    <!-- Phân trang -->
    <div v-if="totalPages > 1 && !loading" class="flex items-center justify-center gap-2 mt-10">
      <button
        @click="goToPage(0)"
        :disabled="currentPage === 0"
        class="px-3 py-2 text-sm rounded-lg border transition"
        :class="currentPage === 0 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
      >«</button>

      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-3 py-2 text-sm rounded-lg border transition flex items-center gap-1"
        :class="currentPage === 0 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
      >
        <ChevronLeft class="w-4 h-4" /> Trước
      </button>

      <template v-for="p in pageNumbers" :key="p">
        <span v-if="p === '...'" class="px-2 text-gray-400 text-sm">…</span>
        <button
          v-else
          @click="goToPage(p)"
          class="w-9 h-9 text-sm rounded-lg border font-bold transition"
          :class="p === currentPage
            ? 'bg-red-700 text-white border-red-700 shadow-sm'
            : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
        >{{ p + 1 }}</button>
      </template>

      <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-3 py-2 text-sm rounded-lg border transition flex items-center gap-1"
        :class="currentPage >= totalPages - 1 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
      >
        Sau <ChevronRight class="w-4 h-4" />
      </button>

      <button
        @click="goToPage(totalPages - 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-3 py-2 text-sm rounded-lg border transition"
        :class="currentPage >= totalPages - 1 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
      >»</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import NewsCard from '../components/NewsCard.vue'

const route = useRoute()
const news         = ref([])
const loading      = ref(true)
const totalElements = ref(0)
const totalPages    = ref(0)
const currentPage   = ref(0)

const slug = computed(() => route.params.slug)

const categoryLabels = {
  'thoi-su':           '🗞 Thời sự',
  'viec-lam':          '💼 Việc làm',
  'phap-luat':         '⚖️ Pháp luật',
  'bao-hiem':          '🛡 Bảo hiểm',
  'cong-doan':         '🤝 Công đoàn',
  'suc-khoe':          '❤️ Sức khỏe',
  'quandiem-tranhluan':'✍️ Quan điểm - Tranh luận',
  'tin-moi-nhat':      '🔥 Tin mới nhất',
}
const getCategoryLabel = (s) => categoryLabels[s] || (s ? s.replace(/-/g, ' ') : '')

// Ellipsis pagination
const pageNumbers = computed(() => {
  const total = totalPages.value
  const cur   = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const pages = [0]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(1, cur - 1); i <= Math.min(total - 2, cur + 1); i++) pages.push(i)
  if (cur < total - 4) pages.push('...')
  pages.push(total - 1)
  return pages
})

const fetchNews = async (categorySlug, pg = 0) => {
  if (!categorySlug) return
  loading.value = true
  currentPage.value = pg
  try {
    const res  = await fetch(`http://localhost:5000/api/news/paged/${categorySlug}?page=${pg}&size=12`)
    const data = await res.json()
    news.value          = Array.isArray(data.content)       ? data.content       : []
    totalElements.value = typeof data.totalElements === 'number' ? data.totalElements : 0
    totalPages.value    = typeof data.totalPages    === 'number' ? data.totalPages    : 0
    currentPage.value   = typeof data.currentPage   === 'number' ? data.currentPage   : 0
  } catch (err) {
    console.error('Category fetch error:', err)
    news.value = []
  } finally {
    loading.value = false
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const goToPage = (pg) => {
  if (pg < 0 || pg >= totalPages.value) return
  fetchNews(slug.value, pg)
}

watch(slug, (newSlug) => {
  currentPage.value = 0
  fetchNews(newSlug, 0)
}, { immediate: true })
</script>
