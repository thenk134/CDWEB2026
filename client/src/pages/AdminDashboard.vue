<template>
  <main class="container mx-auto px-4 py-8 min-h-screen">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight">Quản Lý Tin Tức</h1>
        <p class="text-gray-500 text-sm">Quản trị toàn bộ bài viết trong hệ thống cơ sở dữ liệu</p>
      </div>
      <div class="flex flex-wrap gap-3">
        <router-link
            to="/admin/user-posts"
            class="bg-orange-500 hover:bg-orange-600 text-white font-bold px-5 py-2.5 rounded-lg shadow transition flex items-center gap-2"
        >
          <span>📝</span> Quản lý bài viết cá nhân
        </router-link>

        <router-link
            to="/admin/create"
            class="bg-green-600 hover:bg-green-700 text-white font-bold px-5 py-2.5 rounded-lg shadow transition flex items-center gap-2"
        >
          <span>➕</span> Đăng bài viết mới
        </router-link>
      </div>
    </div>

    <!-- Thanh công cụ tìm kiếm và lọc nội bộ -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
      <div class="w-full md:w-96 relative">
        <span class="absolute left-3 top-3 text-gray-400">🔍</span>
        <input
          type="text"
          v-model="searchTerm"
          placeholder="Lọc nhanh tiêu đề bài viết..."
          class="w-full pl-9 pr-4 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition text-sm"
        />
      </div>
      <div class="w-full sm:w-56">
        <select
            v-model="selectedCategory"
            class="w-full px-3 py-2.5 rounded-lg border border-gray-300 bg-white focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition text-sm font-medium text-gray-700 cursor-pointer"
        >
          <option value="">📂 Tất cả danh mục</option>
          <option v-for="cat in categories" :key="cat.slug" :value="cat.slug">
            {{ cat.name }}
          </option>
        </select>
      </div>
      <div class="text-sm text-gray-500 font-medium">
        Tổng số: <span class="text-red-700 font-bold">{{ filteredArticles.length }}</span> bài viết
      </div>
    </div>

    <!-- Bảng danh sách bài viết -->
    <div v-if="loading" class="text-center py-20 animate-pulse text-gray-500">
      Đang tải dữ liệu từ PostgreSQL...
    </div>
    
    <div v-else class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse text-sm">
          <thead>
            <tr class="bg-gray-50 text-gray-700 font-bold border-b border-gray-200 uppercase text-xs">
              <th class="p-4 w-12">ID</th>
              <th class="p-4">Tiêu đề bài viết</th>
              <th class="p-4 w-32">Danh mục</th>
              <th class="p-4 w-32">Nguồn</th>
              <th class="p-4 w-44">Ngày đăng</th>
              <th class="p-4 w-36 text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="article in filteredArticles" :key="article.id" class="hover:bg-gray-50 transition">
              <td class="p-4 text-gray-500 font-mono font-medium">{{ article.id }}</td>
              <td class="p-4 font-semibold text-gray-800">
                <a :href="article.link" target="_blank" class="hover:text-red-700 transition">
                  {{ article.title }}
                </a>
                <span 
                  v-if="article.local" 
                  class="ml-2 bg-green-100 text-green-800 text-[10px] px-2 py-0.5 rounded font-bold uppercase tracking-wider"
                >
                  Nội bộ
                </span>
              </td>
              <td class="p-4">
                <span class="bg-gray-100 text-gray-800 text-xs px-2.5 py-1 rounded-full capitalize">
                  {{ article.category }}
                </span>
              </td>
              <td class="p-4 text-xs font-semibold uppercase tracking-wider text-gray-600">
                {{ article.source }}
              </td>
              <td class="p-4 text-xs text-gray-500">
                {{ formatDate(article.pubDate) }}
              </td>
              <td class="p-4 text-center">
                <div class="flex justify-center gap-2">
                  <router-link 
                    :to="`/admin/edit/${article.id}`"
                    class="bg-blue-50 text-blue-700 border border-blue-200 px-3 py-1 rounded hover:bg-blue-100 transition text-xs font-bold"
                  >
                    Sửa
                  </router-link>
                  <button 
                    @click="deleteArticle(article.id)"
                    class="bg-red-50 text-red-700 border border-red-200 px-3 py-1 rounded hover:bg-red-100 transition text-xs font-bold"
                  >
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredArticles.length === 0">
              <td colspan="6" class="p-8 text-center text-gray-500 italic bg-gray-50">
                Không tìm thấy bài viết nào phù hợp.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { toast } from '../utils/toast'

const articles = ref([])
const loading = ref(true)
const searchTerm = ref("")
const selectedCategory = ref("")

const fetchArticles = () => {
  loading.value = true
  const token = localStorage.getItem("token")

  fetch("http://localhost:5000/api/admin/news", {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Không thể tải danh sách bài viết.")
      return res.json()
    })
    .then(data => {
      articles.value = Array.isArray(data) ? data : []
    })
    .catch(err => {
      console.error(err)
      toast.error("Lỗi tải tin tức: " + err.message)
    })
    .finally(() => {
      loading.value = false
    })
}
const categories = [
  { slug: "tin-moi-nhat", name: "Trang chủ (Tin mới nhất)" },
  { slug: "thoi-su", name: "Thời sự" },
  { slug: "viec-lam", name: "Việc làm" },
  { slug: "phap-luat", name: "Pháp luật" },
  { slug: "bao-hiem", name: "Bảo hiểm" },
  { slug: "cong-doan", name: "Công đoàn" },
  { slug: "suc-khoe", name: "Sức khỏe" },
  { slug: "quandiem-tranhluan", name: "Quan điểm - Tranh luận" }
]
// Hàm bổ sung: Giúp hiển thị tên Tiếng Việt đẹp hơn
const getCategoryName = (slug) => {
  const found = categories.find(c => c.slug === slug)
  return found ? found.name : slug
}
const deleteArticle = (id) => {
  if (!confirm("Bạn có chắc chắn muốn xóa bài viết này không? Hành động này không thể hoàn tác.")) {
    return
  }

  const token = localStorage.getItem("token")
  fetch(`http://localhost:5000/api/admin/news/${id}`, {
    method: "DELETE",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => res.json())
    .then(data => {
      toast.success(data.message || "Đã xóa bài viết thành công!")
      fetchArticles()
    })
    .catch(err => {
      console.error(err)
      toast.error("Lỗi xóa bài viết!")
    })
}

const filteredArticles = computed(() => {
  const term = searchTerm.value.toLowerCase().trim()
  const cat = selectedCategory.value // Lấy giá trị danh mục đang được chọn (ref(""))

  return articles.value.filter(a => {
    // Nếu ô tìm kiếm trống (!term) thì coi như Luôn Đúng, ngược lại thì tìm trong tiêu đề
    const matchesSearch = !term || (a.title && a.title.toLowerCase().includes(term))

    // Nếu danh mục chọn "Tất cả" (!cat) thì coi như Luôn Đúng, ngược lại thì thuộc tính category phải trùng với danh mục được chọn
    const matchesCategory = !cat || a.category === cat

    // Bài viết hợp lệ là bài viết phải thỏa mãn ĐỒNG THỜI cả 2 bộ lọc trên
    return matchesSearch && matchesCategory
  })
})

const formatDate = (dateStr) => {
  if (!dateStr) return "N/A"
  // Rút gọn ngày cho dễ đọc trên table
  return dateStr.length > 25 ? dateStr.substring(0, 25) + "..." : dateStr
}

onMounted(() => {
  fetchArticles()
})
</script>
