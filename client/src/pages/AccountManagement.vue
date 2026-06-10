<template>
  <main class="container mx-auto px-4 py-12 max-w-xl min-h-screen">
    <div class="bg-white rounded-2xl border border-gray-200 shadow-xl p-8 mb-8 text-center">
      <div class="w-20 h-20 bg-gradient-to-tr from-red-600 to-amber-500 rounded-full flex items-center justify-center text-white text-3xl font-black mx-auto mb-4 shadow-md">
        {{ username.charAt(0).toUpperCase() }}
      </div>
      <h2 class="text-2xl font-black text-gray-800 mb-1 tracking-tight">{{ username }}</h2>
      <p class="inline-block bg-gray-50 border border-gray-200 text-gray-600 px-4 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
        Quyền hạn: <span :class="roleColorClass">{{ role }}</span>
      </p>
    </div>

    <div class="flex flex-col gap-4">

      <template v-if="role === 'ADMIN'">
        <router-link to="/admin" class="flex items-center justify-center gap-2 p-5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          💼 Quản Lý Tin Tức
        </router-link>
        <router-link to="/admin/user-posts" class="flex items-center justify-center gap-2 p-5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          📝 Quản lý bài viết cá nhân
        </router-link>
      </template>

      <template v-else-if="role === 'MEMBER'">
        <button @click="goToMemberPosts" class="flex items-center justify-center gap-2 p-5 bg-purple-600 hover:bg-purple-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          ✍️ Bài viết của tôi
        </button>
        <router-link to="/" class="flex items-center justify-center gap-2 p-5 bg-gray-600 hover:bg-gray-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          📰 Trở về trang tin
        </router-link>
      </template>

      <template v-else>
        <button @click="upgradeAccount" class="flex items-center justify-center gap-2 p-5 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5 font-black">
          ⭐ Nâng cấp hội viên
        </button>
        <router-link to="/" class="flex items-center justify-center gap-2 p-5 bg-gray-600 hover:bg-gray-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          📰 Trở về trang tin
        </router-link>
      </template>

      <button @click="handleLogout" class="flex items-center justify-center gap-2 p-5 bg-red-50 hover:bg-red-100 text-red-700 font-bold text-lg rounded-xl shadow-sm border border-red-200 transition-all mt-4">
        🚪 Đăng xuất tài khoản
      </button>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Lấy thông tin user đã lưu trong localStorage từ lúc Đăng nhập thành công
const username = ref(localStorage.getItem('username') || 'Người dùng')
const role = ref(localStorage.getItem('user_role') || 'USER')

// Tự động đá người dùng chưa đăng nhập về trang Login bảo vệ dữ liệu công khai
onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }
})

// Đổi màu chữ theo Role cho sinh động
const roleColorClass = computed(() => {
  if (role.value === 'ADMIN') return 'text-red-600 font-extrabold'
  if (role.value === 'MEMBER') return 'text-purple-600 font-extrabold'
  return 'text-blue-600 font-extrabold'
})

// Logic Đăng xuất: Xóa sạch dữ liệu và đẩy ra trang Login
const handleLogout = () => {
  if (confirm("Bạn có chắc chắn muốn đăng xuất tài khoản?")) {
    localStorage.clear() // Xóa Token, Role, Username,...
    router.push('/login')
  }
}
const goToMemberPosts = () => {
  router.push('/my-posts')
}
// Hàm giả định yêu cầu nâng cấp
const upgradeAccount = () => {
  alert("Thêm thanh toán sau!")
}
</script>