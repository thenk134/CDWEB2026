<template>
  <main class="container mx-auto px-4 py-8 min-h-screen">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight">Duyệt Bài Viết Thành Viên</h1>
        <p class="text-gray-500 text-sm">Quản lý và phê duyệt các bài viết từ cộng đồng độc giả gửi về</p>
      </div>
      <router-link
          to="/admin" 
          class="bg-gray-600 hover:bg-gray-700 text-white font-bold px-5 py-2.5 rounded-lg shadow transition flex items-center gap-2"
      >
        <ArrowLeft class="w-4 h-4 text-white" />
        <span>Quay lại Dashboard</span>
      </router-link>
    </div>

    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
      <div class="w-full md:w-96 relative">
        <Search class="w-4 h-4 text-gray-400 absolute left-3 top-3" />
        <input
            type="text"
            v-model="searchTerm"
            placeholder="Lọc nhanh tiêu đề bài viết..."
            class="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 text-sm"
        />
      </div>
      <div class="text-sm text-gray-500 font-medium">
        Hiển thị: <span class="text-gray-800 font-bold">{{ filteredPosts.length }}</span> / {{ posts.length }} bài viết chờ duyệt
      </div>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-500">
      Đang tải danh sách bài viết chờ duyệt...
    </div>

    <div v-else-if="filteredPosts.length > 0" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
      <table class="w-full text-left text-sm">
        <thead class="bg-gray-50 uppercase text-xs font-bold text-gray-700">
        <tr>
          <th class="p-4">ID</th>
          <th class="p-4">Tiêu đề bài viết</th>
          <th class="p-4">Tác giả (Nguồn)</th>
          <th class="p-4">Ngày gửi</th>
          <th class="p-4 text-center">Thao tác</th>
        </tr>
        </thead>
        <tbody class="divide-y">
        <tr v-for="post in filteredPosts" :key="post.id" class="hover:bg-orange-50/30 transition">
          <td class="p-4 font-mono">#{{ post.id }}</td>
          <td class="p-4 font-bold text-gray-800 max-w-md truncate">{{ post.title }}</td>
          <td class="p-4 text-blue-700 font-semibold">{{ post.source }}</td>
          <td class="p-4 text-gray-500 text-xs">{{ post.pubDate }}</td>
          <td class="p-4 text-center">
            <router-link
                :to="`/admin/user-posts/${post.id}`"
                class="bg-orange-500 text-white px-4 py-1.5 rounded hover:bg-orange-600 transition font-bold text-xs inline-block"
            >
              XEM & DUYỆT
            </router-link>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="text-center py-20 bg-gray-50 rounded-xl border border-dashed border-gray-200 text-gray-400">
      <p class="font-medium text-base mb-1">Không tìm thấy bài viết chờ duyệt nào khớp từ khóa!</p>
      <p class="text-xs">Vui lòng kiểm tra lại cụm từ tìm kiếm của bạn.</p>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Search } from 'lucide-vue-next'

const posts = ref([])
const loading = ref(true)
const searchTerm = ref("")

// Hàm gọi API lấy danh sách bài viết đang chờ duyệt (status = 0)
const fetchPendingPosts = async () => {
  loading.value = true
  const token = localStorage.getItem("token")
  try {
    // CHÍNH XÁC: Gọi trúng API lấy danh sách bài viết chờ duyệt (/pending)
    const res = await fetch(`http://localhost:5000/api/admin/news/pending`, {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Không thể tải danh sách bài viết.")
    const data = await res.json()
    posts.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error("Lỗi tải tin chờ duyệt:", err)
  } finally {
    loading.value = false
  }
}

// LOGIC BỘ LỌC TÌM KIẾM TIÊU ĐỀ THEO BẢNG MỚI
const filteredPosts = computed(() => {
  if (!searchTerm.value.trim()) return posts.value
  return posts.value.filter(post =>
      post.title && post.title.toLowerCase().includes(searchTerm.value.toLowerCase().trim())
  )
})

onMounted(() => {
  fetchPendingPosts()
})
</script>