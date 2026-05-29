<template>
  <main class="container mx-auto px-4 py-8 max-w-4xl min-h-screen">
    <div class="mb-6">
      <router-link to="/admin" class="text-red-700 hover:underline flex items-center gap-1 text-sm font-bold">
        ⬅️ Quay lại danh sách quản lý
      </router-link>
    </div>

    <div class="bg-white rounded-xl border border-gray-200 shadow-lg p-6 md:p-8">
      <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight mb-2">
        {{ isEdit ? 'Chỉnh Sửa Bài Viết' : 'Đăng Bài Viết Mới' }}
      </h1>
      <p class="text-gray-500 text-sm mb-8">
        {{ isEdit ? 'Thay đổi nội dung cho bài viết mã số #' + articleId : 'Tạo bài viết nội bộ hiển thị trên hệ thống.' }}
      </p>

      <form @submit.prevent="saveArticle" class="space-y-6">
        <!-- Tiêu đề -->
        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Tiêu đề bài viết *</label>
          <input 
            type="text" 
            v-model="article.title" 
            required 
            placeholder="Nhập tiêu đề hấp dẫn..."
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          />
        </div>

        <!-- Danh mục & Ảnh đại diện -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-gray-700 text-sm font-bold mb-2">Danh mục tin tức</label>
            <select 
              v-model="article.category" 
              class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition bg-white"
            >
              <option value="thoi-su">Thời sự</option>
              <option value="viec-lam">Việc làm</option>
              <option value="phap-luat">Pháp luật</option>
              <option value="bao-hiem">Bảo hiểm</option>
              <option value="cong-doan">Công đoàn</option>
              <option value="suc-khoe">Sức khỏe</option>
            </select>
          </div>

          <div>
            <label class="block text-gray-700 text-sm font-bold mb-2">Đường dẫn ảnh bìa (Image URL)</label>
            <input 
              type="text" 
              v-model="article.image" 
              placeholder="https://example.com/image.jpg"
              class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
            />
          </div>
        </div>

        <!-- Ảnh Demo xem trước -->
        <div v-if="article.image" class="border rounded-lg overflow-hidden bg-gray-50 max-h-60 flex items-center justify-center p-2">
          <img :src="article.image" alt="Preview Image" class="max-h-56 object-contain rounded-md" />
        </div>

        <!-- Mô tả ngắn -->
        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Mô tả ngắn (Sapo) *</label>
          <textarea 
            v-model="article.description" 
            rows="3" 
            required
            placeholder="Tóm tắt ngắn gọn nội dung bài viết..."
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          ></textarea>
        </div>

        <!-- Nội dung chi tiết -->
        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Nội dung chi tiết bài viết (Hỗ trợ HTML cơ bản) *</label>
          <textarea 
            v-model="article.content" 
            rows="12" 
            required
            placeholder="Nhập nội dung bài viết... Bạn có thể dùng các thẻ <p>, <img>, <strong> để định dạng."
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition font-sans text-base leading-relaxed"
          ></textarea>
        </div>

        <div v-if="errorMessage" class="bg-red-50 text-red-700 text-xs px-4 py-3 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </div>

        <!-- Nút Hành động -->
        <div class="flex justify-end gap-4 pt-4 border-t border-gray-100">
          <router-link 
            to="/admin" 
            class="px-5 py-2.5 rounded-lg border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold text-sm transition"
          >
            Hủy bỏ
          </router-link>
          
          <button 
            type="submit" 
            :disabled="loading"
            class="bg-red-700 hover:bg-red-800 text-white font-bold px-6 py-2.5 rounded-lg shadow transition disabled:opacity-50 text-sm"
          >
            {{ loading ? 'Đang lưu bài viết...' : (isEdit ? 'LƯU THAY ĐỔI' : 'ĐĂNG BÀI VIẾT') }}
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from '../utils/toast'

const route = useRoute()
const router = useRouter()

const articleId = computed(() => route.params.id)
const isEdit = computed(() => !!articleId.value)

const article = ref({
  title: "",
  description: "",
  content: "",
  image: "",
  category: "thoi-su"
})

const loading = ref(false)
const errorMessage = ref("")

const fetchArticleDetails = () => {
  if (!isEdit.value) return

  loading.value = true
  const token = localStorage.getItem("token")

  fetch(`http://localhost:5000/api/admin/news/${articleId.value}`, {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Không thể tải thông tin bài viết.")
      return res.json()
    })
    .then(data => {
      article.value = {
        title: data.title,
        description: data.description,
        content: data.content,
        image: data.image,
        category: data.category
      }
    })
    .catch(err => {
      console.error(err)
      errorMessage.value = "Lỗi khi lấy thông tin bài viết để chỉnh sửa: " + err.message
    })
    .finally(() => {
      loading.value = false
    })
}

const saveArticle = () => {
  loading.value = true
  errorMessage.value = ""

  const token = localStorage.getItem("token")
  const url = isEdit.value 
    ? `http://localhost:5000/api/admin/news/${articleId.value}`
    : "http://localhost:5000/api/admin/news"
  
  const method = isEdit.value ? "PUT" : "POST"

  fetch(url, {
    method: method,
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify(article.value)
  })
    .then(async (res) => {
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data.message || "Không thể lưu bài viết!")
      }
      return data
    })
    .then((data) => {
      toast.success(isEdit.value ? "Cập nhật bài viết thành công!" : "Đăng bài viết mới thành công!")
      router.push("/admin")
    })
    .catch((err) => {
      errorMessage.value = err.message
    })
    .finally(() => {
      loading.value = false
    })
}

onMounted(() => {
  fetchArticleDetails()
})
</script>
