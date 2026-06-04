<template>
  <main class="container mx-auto px-4 py-8 max-w-4xl min-h-screen">
    <div class="mb-6">
      <router-link to="/admin/user-posts" class="text-red-700 hover:underline flex items-center gap-1 text-sm font-bold">
        ⬅️ Quay lại danh sách chờ duyệt
      </router-link>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-500">
      Đang tải chi tiết bài viết...
    </div>

    <div v-else-if="article" class="bg-white rounded-xl border border-gray-200 shadow-lg p-6 md:p-8">

      <h1 class="text-3xl font-black text-gray-800 tracking-tight mb-4">
        {{ article.title }}
      </h1>

      <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500 mb-8 border-b pb-4">
        <span class="bg-blue-50 text-blue-700 px-3 py-1 rounded-full font-semibold">Tác giả: {{ article.source }}</span>
        <span class="bg-amber-50 text-amber-700 px-3 py-1 rounded-full font-semibold">Mục: {{ article.category }}</span>
        <span>Ngày gửi: {{ article.pubDate }}</span>
      </div>

      <div class="prose max-w-none mb-10">
        <p v-if="article.description" class="font-bold text-gray-700 text-lg mb-6">
          {{ article.description }}
        </p>

        <img
            v-if="article.image"
            :src="article.image"
            alt="Thumbnail"
            class="w-full max-h-[400px] object-cover rounded-lg mb-8 border border-gray-100"
        />

        <div v-html="article.content" class="text-gray-800 leading-relaxed whitespace-pre-wrap"></div>
      </div>

      <div class="flex justify-between items-center pt-6 border-t border-gray-200">
        <button
            @click="reviewPost(2)"
            class="bg-gray-200 text-gray-700 px-6 py-3 rounded-lg font-bold hover:bg-red-100 hover:text-red-700 transition"
        >
          🚫 ĐÁNH DẤU VI PHẠM
        </button>

        <button
            @click="reviewPost(1)"
            class="bg-green-600 text-white px-10 py-3 rounded-lg font-bold hover:bg-green-700 shadow-lg transition"
        >
          ✅ XÉT DUYỆT & ĐĂNG BÀI
        </button>
      </div>

    </div>

    <div v-else class="text-center py-20 text-red-500 font-bold">
      Không tìm thấy bài viết này hoặc đã bị xóa.
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 1. Nhận biến 'id' từ URL (do router cấu hình props: true)
const props = defineProps({
  id: {
    type: String,
    required: true
  }
})

// 2. Khởi tạo router và các biến trạng thái
const router = useRouter()
const article = ref(null)
const loading = ref(true)

// 3. Hàm tải chi tiết bài viết dựa trên ID
// Hàm tải chi tiết bài viết dựa trên ID dành cho Admin
const fetchArticleDetail = async () => {
  try {
    const token = localStorage.getItem("token")
    const res = await fetch(`http://localhost:5000/api/admin/news/${props.id}`, {
      headers: { "Authorization": `Bearer ${token}` }
    })

    if (res.ok) {
      article.value = await res.json()
    }
  } catch (err) {
    console.error("Lỗi khi lấy chi tiết bài viết:", err)
  } finally {
    loading.value = false
  }
}

// 4. Hàm xử lý Duyệt/Vi phạm đã được sửa lỗi biến postId
const reviewPost = async (status) => {
  const confirmMsg = status === 1 ? "Duyệt bài này hiển thị lên trang báo?" : "Đánh dấu vi phạm bài này?";
  if (!confirm(confirmMsg)) return;

  const token = localStorage.getItem("token")

  // Đã sửa 'postId' thành 'props.id'
  const res = await fetch(`http://localhost:5000/api/admin/news/${props.id}/review?status=${status}`, {
    method: "PUT",
    headers: { "Authorization": `Bearer ${token}` }
  })

  if (res.ok) {
    alert(status === 1 ? "Đã duyệt bài thành công!" : "Đã chuyển bài viết vào mục vi phạm!")
    // Chuyển hướng về lại danh sách sau khi duyệt xong
    router.push("/admin/user-posts")
  } else {
    try {
      const errorData = await res.json();
      alert("Lỗi từ Server: " + (errorData.message || "Không xác định"));
    } catch(e) {
      alert("Lỗi máy chủ! (HTTP " + res.status + ")");
    }
  }
}

// 5. Tự động gọi hàm lấy dữ liệu khi vừa vào trang
onMounted(() => {
  fetchArticleDetail()
})
</script>