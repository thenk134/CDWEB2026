<template>
  <main class="container mx-auto px-4 py-8 min-h-screen">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight">Bài viết của tôi</h1>
        <p class="text-gray-500 text-sm">Xem danh sách và theo dõi trạng thái các bài viết bạn đã gửi lên hệ thống</p>
      </div>
      <router-link
          to="/my-post/create"
          class="bg-red-700 hover:bg-red-800 text-white font-bold px-5 py-2.5 rounded-lg shadow transition flex items-center gap-2"
      >
        ➕ Đăng bài viết mới
      </router-link>
    </div>

    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-col md:flex-row gap-4 items-center justify-between">
      <div class="w-full md:w-96 relative">
        <span class="absolute left-3 top-2.5 text-gray-400">🔍</span>
        <input
            type="text"
            v-model="searchTerm"
            placeholder="Lọc nhanh tiêu đề bài viết..."
            class="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 text-sm"
        />
      </div>
      <div class="text-sm text-gray-500 font-medium">
        Hiển thị: <span class="text-gray-800 font-bold">{{ filteredPosts.length }}</span> / {{ posts.length }} bài viết cá nhân
      </div>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-500">
      Đang tải danh sách bài viết của bạn...
    </div>

    <div v-else-if="filteredPosts.length > 0" class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
      <table class="w-full text-left text-sm">
        <thead class="bg-gray-50 uppercase text-xs font-bold text-gray-700">
        <tr>
          <th class="p-4">ID</th>
          <th class="p-4">Tiêu đề bài viết</th>
          <th class="p-4">Trạng thái</th>
          <th class="p-4">Ngày gửi</th>
          <th class="p-4 text-center">Thao tác</th>
        </tr>
        </thead>
        <tbody class="divide-y">
        <tr v-for="post in filteredPosts" :key="post.id" class="hover:bg-orange-50/30 transition">
          <td class="p-4 font-mono">#{{ post.id }}</td>
          <td class="p-4 font-bold text-gray-800 max-w-md truncate">{{ post.title }}</td>

          <td class="p-4">
            <span v-if="post.status === 0" class="bg-amber-100 text-amber-800 px-2.5 py-1 rounded-full text-xs font-bold border border-amber-200">
              ⏳ Chờ xét duyệt
            </span>
            <span v-else-if="post.status === 1" class="bg-emerald-100 text-emerald-800 px-2.5 py-1 rounded-full text-xs font-bold border border-emerald-200">
              ✅ Đã xuất bản
            </span>
            <span v-else-if="post.status === 2" class="bg-red-100 text-red-800 px-2.5 py-1 rounded-full text-xs font-bold border border-red-200">
              ❌ Bị từ chối / Vi phạm
            </span>
          </td>

          <td class="p-4 text-gray-500 text-xs">{{ post.pubDate }}</td>

          <td class="p-4 text-center">
            <div class="flex items-center justify-center gap-1.5">
              <button
                  @click="viewPostDetail(post)"
                  class="bg-blue-600 text-white px-3 py-1.5 rounded hover:bg-blue-700 transition font-bold text-xs"
              >
                👁️ Xem
              </button>

              <button
                  @click="handleEdit(post)"
                  class="bg-amber-500 hover:bg-amber-600 text-white px-3 py-1.5 rounded font-bold text-xs transition"
                  title="Chỉnh sửa nội dung"
              >
                ✏️ Sửa
              </button>

              <button
                  @click="handleDelete(post.id)"
                  class="bg-red-600 hover:bg-red-700 text-white px-3 py-1.5 rounded font-bold text-xs transition"
                  title="Xóa bài viết vĩnh viễn"
              >
                🗑️ Xóa
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="text-center py-20 bg-gray-50 rounded-xl border border-dashed border-gray-200 text-gray-400">
      <p class="font-medium text-base mb-1">Bạn chưa đăng bài viết nào hoặc không khớp từ khóa!</p>
      <p class="text-xs">Hãy tích cực gửi bài viết đóng góp cho tòa soạn nhé.</p>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const posts = ref([])
const loading = ref(true)
const searchTerm = ref("")

// Hàm gọi API lấy toàn bộ danh sách bài viết của riêng User đang đăng nhập
const fetchMyPosts = async () => {
  loading.value = true
  const token = localStorage.getItem("token")
  try {
    // LƯU Ý: Hãy chắc chắn Backend Spring Boot của bạn có API này để trả về danh sách dựa vào Token người dùng
    const res = await fetch(`http://localhost:5000/api/news/my-posts`, {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Không thể tải danh sách bài viết của bạn.")
    const data = await res.json()
    posts.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error("Lỗi tải tin cá nhân:", err)
  } finally {
    loading.value = false
  }
}

// Bộ lọc tìm kiếm tiêu đề giống hệt Admin
const filteredPosts = computed(() => {
  if (!searchTerm.value.trim()) return posts.value
  return posts.value.filter(post =>
      post.title && post.title.toLowerCase().includes(searchTerm.value.toLowerCase().trim())
  )
})

// Điều hướng xem bài viết dựa vào trạng thái bài
const viewPostDetail = (post) => {
  if (post.status === 1) {
    // Nếu bài viết đã xuất bản công khai, chuyển hướng thẳng ra trang đọc báo công cộng công khai
    router.push({
      path: '/news-detail',
      query: { url: post.link, date: post.pubDate }
    })
  } else if (post.status === 0) {
    alert("Bài viết đang trong hàng đợi phê duyệt của Ban biên tập, vui lòng quay lại sau!")
  } else {
    alert("Bài viết bị từ chối đăng do vi phạm tiêu chuẩn cộng đồng của báo!")
  }
}

onMounted(() => {
  fetchMyPosts()
})
const handleEdit = (post) => {
  if (post.status === 1) {
    const confirmEdit = confirm("Bài viết này đã được xuất bản công khai. Nếu bạn sửa đổi, bài viết sẽ tạm thời bị ẩn đi để Ban biên tập xét duyệt lại từ đầu. Bạn có đồng ý không?");
    if (!confirmEdit) return;
  }
  router.push({ path: '/my-post/create', query: { id: post.id } })
}

// Gọi API xóa bài viết cá nhân khỏi hệ thống
const handleDelete = async (postId) => {
  if (!confirm("⚠️ Bạn có chắc chắn muốn xóa vĩnh viễn bài viết này không? Hành động này không thể rút lại!")) return;

  const token = localStorage.getItem("token")
  try {
    const res = await fetch(`http://localhost:5000/api/news/my-posts/${postId}`, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${token}` }
    })

    if (res.ok) {
      alert("🗑️ Đã xóa bài viết thành công!")
      // Lọc bỏ bài viết vừa xóa khỏi màn hình hiển thị ngay lập tức
      posts.value = posts.value.filter(p => p.id !== postId)
    } else {
      const err = await res.json()
      alert("Lỗi không thể xóa: " + (err.message || "Yêu cầu thất bại."))
    }
  } catch (error) {
    console.error("Lỗi kết nối xóa bài:", error)
    alert("Không thể kết nối đến máy chủ Backend.")
  }
}
</script>