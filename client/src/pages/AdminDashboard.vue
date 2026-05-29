<template>
  <main class="container mx-auto px-4 py-8 min-h-screen">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight">Quản Lý Tin Tức</h1>
        <p class="text-gray-500 text-sm">Quản trị toàn bộ bài viết trong hệ thống cơ sở dữ liệu</p>
      </div>
      <router-link 
        to="/admin/create" 
        class="bg-green-600 hover:bg-green-700 text-white font-bold px-5 py-2.5 rounded-lg shadow transition flex items-center gap-2"
      >
        <span>➕</span> Đăng bài viết mới
      </router-link>
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

const articles = ref([])
const loading = ref(true)
const searchTerm = ref("")

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
      alert("Lỗi tải tin tức: " + err.message)
    })
    .finally(() => {
      loading.value = false
    })
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
      alert(data.message || "Đã xóa bài viết thành công!")
      fetchArticles()
    })
    .catch(err => {
      console.error(err)
      alert("Lỗi xóa bài viết!")
    })
}

const filteredArticles = computed(() => {
  const term = searchTerm.value.toLowerCase().trim()
  if (!term) return articles.value
  return articles.value.filter(a => a.title.toLowerCase().includes(term))
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
