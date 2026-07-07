<template>
  <main class="container mx-auto px-4 py-8 max-w-3xl min-h-screen">
    <div class="border-b border-gray-200 pb-4 mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">
          {{ isEditMode ? '📝 Chỉnh sửa bài viết' : '🚀 Đăng bài viết mới' }}
        </h1>
        <p class="text-xs text-gray-500 mt-1">
          {{ isEditMode ? 'Chỉnh sửa lại nội dung bài viết cá nhân của bạn' : 'Bài viết sau khi gửi sẽ nằm trong trạng thái Chờ phê duyệt từ Ban biên tập' }}
        </p>
      </div>
      <router-link to="/my-posts" class="text-sm font-bold text-gray-500 hover:text-gray-700">
        ❌ Hủy bỏ
      </router-link>
    </div>

    <div v-if="loadingOldData" class="text-center py-12 text-gray-500 font-medium">
      ⏳ Đang tải dữ liệu bài viết cũ, vui lòng đợi...
    </div>

    <form v-else @submit.prevent="handleSubmit" class="bg-white border border-gray-200 shadow-sm rounded-xl p-6 space-y-5">

      <div>
        <label class="block text-xs font-bold text-gray-700 uppercase mb-2">Tiêu đề bài viết <span class="text-red-600">*</span></label>
        <input
            type="text"
            v-model="form.title"
            required
            placeholder="Nhập tiêu đề thu hút người đọc..."
            class="w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 text-sm font-medium"
        />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-xs font-bold text-gray-700 uppercase mb-2">Chuyên mục đăng ký</label>
          <div class="w-full px-4 py-2.5 bg-gray-100 border border-gray-200 rounded-lg text-sm font-bold text-red-700 flex items-center justify-between">
            <span class="flex items-center gap-1.5">
              <MessageSquare class="w-4 h-4 text-red-700" />
              <span>Quan điểm - Tranh luận</span>
            </span>
            <span class="text-[10px] bg-red-100 text-red-800 px-2 py-0.5 rounded font-black uppercase tracking-wide">Cố định</span>
          </div>
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-700 uppercase mb-2">Nguồn / Tác giả</label>
          <input
              type="text"
              v-model="form.source"
              disabled
              class="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-500 font-medium"
          />
        </div>
      </div>

      <div>
        <label class="block text-xs font-bold text-gray-700 uppercase mb-2">Link ảnh minh họa (URL)</label>
        <input
            type="url"
            v-model="form.image"
            placeholder="Dán link ảnh (https://...) từ internet vào đây nếu có"
            class="w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 text-sm font-medium"
        />
      </div>

      <div>
        <label class="block text-xs font-bold text-gray-700 uppercase mb-2">Nội dung chi tiết hoặc Tóm tắt ý kiến <span class="text-red-600">*</span></label>
        <textarea
            rows="8"
            v-model="form.description"
            required
            placeholder="Hãy trình bày luận điểm, quan điểm cá nhân của bạn một cách rõ ràng và lịch sự..."
            class="w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 text-sm font-medium"
        ></textarea>
      </div>

      <div class="flex justify-end space-x-3 pt-4 border-t border-gray-100">
        <router-link
            to="/my-posts"
            class="px-5 py-2.5 border border-gray-200 rounded-lg text-sm font-bold text-gray-600 hover:bg-gray-50 transition"
        >
          Hủy bỏ
        </router-link>
        <button
            type="submit"
            :disabled="submitting"
            class="px-6 py-2.5 bg-red-700 text-white rounded-lg text-sm font-bold hover:bg-red-800 transition shadow-md disabled:bg-gray-400"
        >
          {{ submitting ? 'Đang xử lý...' : (isEditMode ? '💾 Lưu & Gửi duyệt lại' : '🚀 Gửi bài chờ duyệt') }}
        </button>
      </div>

    </form>
  </main>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { MessageSquare } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const loadingOldData = ref(false)

// Phân tích URL xem có chứa tham số `?id=...` hay không để bật chế độ sửa bài
const articleId = computed(() => route.query.id)
const isEditMode = computed(() => !!articleId.value)

// Biến reactive lưu trữ dữ liệu form
const form = ref({
  title: "",
  description: "",
  category: "quandiem-tranhluan",
  source: localStorage.getItem("username") || "Thành viên",
  image: "https://images.unsplash.com/photo-1506784365847-bbad939e9335?q=80&w=500",
  link: "#",
  status: 0
})

// Chức năng tự động tải dữ liệu cũ đổ ngược lên form nếu đang ở chế độ Sửa bài
const fetchOldArticle = async () => {
  if (!isEditMode.value) return

  loadingOldData.value = true
  const token = localStorage.getItem("token")
  try {
    const res = await fetch(`http://localhost:5000/api/news/my-posts/${articleId.value}`, {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      // Đổ dữ liệu trả về vào form
      form.value.title = data.title
      form.value.description = data.description
      form.value.image = data.image
    } else {
      alert("Không thể tải thông tin bài viết này. Có thể bạn không có quyền sở hữu bài viết!")
      router.push("/my-posts")
    }
  } catch (err) {
    console.error("Lỗi lấy chi tiết bài viết cũ:", err)
  } finally {
    loadingOldData.value = false
  }
}

onMounted(() => {
  fetchOldArticle()
})

// Xử lý gửi dữ liệu lên Backend (Phân nhánh POST hoặc PUT)
const handleSubmit = async () => {
  submitting.value = true
  const token = localStorage.getItem("token")

  // Nếu sửa bài thì gọi link có ID và method PUT, nếu tạo mới thì gọi link thường và method POST
  const url = isEditMode.value
      ? `http://localhost:5000/api/news/my-posts/${articleId.value}`
      : "http://localhost:5000/api/news/my-posts";

  const method = isEditMode.value ? "PUT" : "POST";

  // Luôn đảm bảo reset trạng thái về 0 (Chờ duyệt) bất kể trước đó bài viết mang trạng thái gì
  form.value.status = 0;

  try {
    const res = await fetch(url, {
      method: method,
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(form.value)
    })

    if (res.ok) {
      alert(isEditMode.value ? "🎉 Đã cập nhật bài viết thành công! Vui lòng đợi Ban biên tập duyệt lại." : "🎉 Gửi bài viết lên hệ thống chờ duyệt thành công!")
      router.push("/my-posts")
    } else {
      const errData = await res.json()
      alert("Lỗi hệ thống: " + (errData.message || "Không thể thực hiện."))
    }
  } catch (error) {
    console.error("Lỗi kết nối mạng:", error)
    alert("Không thể kết nối đến Máy chủ!")
  } finally {
    submitting.value = false
  }
}
</script>