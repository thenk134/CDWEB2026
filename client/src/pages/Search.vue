<template>
  <main class="min-h-screen bg-gray-50">
    <!-- Header search bar -->
    <div class="bg-white border-b shadow-sm sticky top-0 z-10">
      <div class="container mx-auto px-4 py-4 max-w-6xl">
        <div class="flex flex-col sm:flex-row gap-3 items-stretch sm:items-center">
          <!-- Ô tìm kiếm -->
          <div class="relative flex-grow">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              v-model="localQuery"
              @keyup.enter="doSearch"
              type="text"
              placeholder="Nhập từ khóa tìm kiếm..."
              class="w-full pl-9 pr-4 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition"
            />
          </div>

          <!-- Lọc chuyên mục -->
          <select
            v-model="selectedCategory"
            @change="doSearch"
            class="border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 bg-white min-w-[160px]"
          >
            <option value="">📂 Tất cả chuyên mục</option>
            <option v-for="cat in categories" :key="cat.slug" :value="cat.slug">
              {{ cat.label }}
            </option>
          </select>

          <!-- Nút tìm -->
          <button
            @click="doSearch"
            class="bg-red-700 hover:bg-red-800 text-white font-bold text-sm px-6 py-2.5 rounded-xl transition flex items-center gap-2 justify-center"
          >
            <Search class="w-4 h-4" />
            Tìm kiếm
          </button>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-6 max-w-6xl">
      <!-- Tiêu đề kết quả -->
      <div class="flex items-center justify-between mb-5" v-if="query">
        <div>
          <h1 class="text-xl font-black text-gray-800">
            Kết quả cho: <span class="text-red-700">"{{ query }}"</span>
            <span v-if="selectedCategory" class="text-gray-400 font-normal text-base ml-2">
              trong <em>{{ getCategoryLabel(selectedCategory) }}</em>
            </span>
          </h1>
          <p class="text-sm text-gray-500 mt-1" v-if="!loading">
            Tìm thấy <strong>{{ totalElements }}</strong> bài viết
            <span v-if="totalPages > 1"> · Trang {{ currentPage + 1 }}/{{ totalPages }}</span>
          </p>
        </div>
        <!-- Nút xóa filter -->
        <button
          v-if="selectedCategory"
          @click="selectedCategory = ''; doSearch()"
          class="text-xs text-gray-500 hover:text-red-600 flex items-center gap-1 transition"
        >
          <X class="w-3.5 h-3.5" /> Xóa lọc
        </button>
      </div>

      <!-- Loading skeleton -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="i in 9" :key="i" class="bg-white rounded-2xl overflow-hidden shadow-sm animate-pulse">
          <div class="bg-gray-200 h-44"></div>
          <div class="p-4 space-y-2">
            <div class="bg-gray-200 h-4 rounded w-3/4"></div>
            <div class="bg-gray-200 h-3 rounded w-full"></div>
            <div class="bg-gray-200 h-3 rounded w-2/3"></div>
          </div>
        </div>
      </div>

      <!-- Danh sách kết quả -->
      <div v-else-if="results.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <NewsCard v-for="(item, idx) in results" :key="idx" :news="item" />
      </div>

      <!-- Rỗng -->
      <div v-else-if="query" class="text-center py-24">
        <div class="text-6xl mb-4">🔍</div>
        <h3 class="text-lg font-bold text-gray-700 mb-2">Không tìm thấy kết quả</h3>
        <p class="text-sm text-gray-500 mb-6">
          Không có bài viết nào khớp với "<strong>{{ query }}</strong>"
          <span v-if="selectedCategory"> trong chuyên mục này</span>.
        </p>
        <button
          v-if="selectedCategory"
          @click="selectedCategory = ''; doSearch()"
          class="text-sm text-red-600 underline hover:no-underline"
        >
          Tìm trong tất cả chuyên mục
        </button>
      </div>

      <!-- Màn hình ban đầu -->
      <div v-else class="text-center py-24 text-gray-400">
        <Search class="w-14 h-14 mx-auto mb-4 opacity-30" />
        <p class="text-base">Nhập từ khóa để bắt đầu tìm kiếm</p>
      </div>

      <!-- Phân trang -->
      <div v-if="totalPages > 1 && !loading" class="flex items-center justify-center gap-2 mt-10">
        <!-- Trang đầu -->
        <button
          @click="goToPage(0)"
          :disabled="currentPage === 0"
          class="px-3 py-2 text-sm rounded-lg border transition"
          :class="currentPage === 0 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
        >
          «
        </button>

        <!-- Trang trước -->
        <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="px-3 py-2 text-sm rounded-lg border transition flex items-center gap-1"
          :class="currentPage === 0 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
        >
          <ChevronLeft class="w-4 h-4" /> Trước
        </button>

        <!-- Số trang -->
        <template v-for="p in pageNumbers" :key="p">
          <span v-if="p === '...'" class="px-2 text-gray-400 text-sm">…</span>
          <button
            v-else
            @click="goToPage(p)"
            class="w-9 h-9 text-sm rounded-lg border font-bold transition"
            :class="p === currentPage
              ? 'bg-red-700 text-white border-red-700 shadow-sm'
              : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
          >
            {{ p + 1 }}
          </button>
        </template>

        <!-- Trang sau -->
        <button
          @click="goToPage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="px-3 py-2 text-sm rounded-lg border transition flex items-center gap-1"
          :class="currentPage >= totalPages - 1 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
        >
          Sau <ChevronRight class="w-4 h-4" />
        </button>

        <!-- Trang cuối -->
        <button
          @click="goToPage(totalPages - 1)"
          :disabled="currentPage >= totalPages - 1"
          class="px-3 py-2 text-sm rounded-lg border transition"
          :class="currentPage >= totalPages - 1 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-red-50 hover:border-red-300 hover:text-red-700'"
        >
          »
        </button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import NewsCard from '../components/NewsCard.vue'

const route  = useRoute()
const router = useRouter()

// State
const localQuery       = ref(route.query.q || '')
const selectedCategory = ref(route.query.cat || '')
const results          = ref([])
const loading          = ref(false)
const totalElements    = ref(0)
const totalPages       = ref(0)
const currentPage      = ref(0)

const query = computed(() => route.query.q || '')

const categories = [
  { slug: 'thoi-su',          label: '🗞 Thời sự' },
  { slug: 'viec-lam',         label: '💼 Việc làm' },
  { slug: 'phap-luat',        label: '⚖️ Pháp luật' },
  { slug: 'bao-hiem',         label: '🛡 Bảo hiểm' },
  { slug: 'cong-doan',        label: '🤝 Công đoàn' },
  { slug: 'suc-khoe',         label: '❤️ Sức khỏe' },
  { slug: 'quandiem-tranhluan', label: '✍️ Quan điểm' },
]

const getCategoryLabel = (slug) => {
  const found = categories.find(c => c.slug === slug)
  return found ? found.label : slug
}

// Tính danh sách trang hiển thị (ellipsis)
const pageNumbers = computed(() => {
  const total  = totalPages.value
  const cur    = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const pages = []
  pages.push(0)
  if (cur > 3)       pages.push('...')
  for (let i = Math.max(1, cur - 1); i <= Math.min(total - 2, cur + 1); i++) pages.push(i)
  if (cur < total - 4) pages.push('...')
  pages.push(total - 1)
  return pages
})

// Fetch tìm kiếm
const fetchSearch = async (pg = 0) => {
  const q = query.value
  if (!q.trim()) {
    results.value = []
    totalElements.value = 0
    totalPages.value = 0
    return
  }
  loading.value = true
  currentPage.value = pg
  try {
    let url = `http://localhost:5000/api/news/search?query=${encodeURIComponent(q.trim())}&page=${pg}&size=12`
    if (selectedCategory.value) {
      url += `&category=${encodeURIComponent(selectedCategory.value)}`
    }
    const res  = await fetch(url)
    const data = await res.json()
    results.value      = Array.isArray(data.content)      ? data.content      : []
    totalElements.value = typeof data.totalElements === 'number' ? data.totalElements : 0
    totalPages.value    = typeof data.totalPages    === 'number' ? data.totalPages    : 0
    currentPage.value   = typeof data.currentPage   === 'number' ? data.currentPage   : 0
  } catch (err) {
    console.error('Search error:', err)
    results.value = []
  } finally {
    loading.value = false
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// Khi nhấn nút / Enter — đẩy lên URL rồi fetch trang 0
const doSearch = () => {
  const q = localQuery.value.trim()
  if (!q) return
  router.push({ name: 'Search', query: { q, ...(selectedCategory.value ? { cat: selectedCategory.value } : {}) } })
}

const goToPage = (pg) => {
  if (pg < 0 || pg >= totalPages.value) return
  fetchSearch(pg)
}

// Theo dõi URL thay đổi (khi gõ từ navbar)
watch(
  () => route.query,
  (newQ) => {
    localQuery.value       = newQ.q   || ''
    selectedCategory.value = newQ.cat || ''
    fetchSearch(0)
  },
  { immediate: true }
)
</script>
