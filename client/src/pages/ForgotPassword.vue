<template>
  <main class="min-h-[80vh] flex items-center justify-center px-4 py-12 bg-gray-50">
    <div class="bg-white rounded-xl shadow-xl p-8 max-w-md w-full border border-gray-100">
      
      <div class="text-center mb-8">
        <h2 class="text-3xl font-black text-red-700 uppercase tracking-tight">Khôi Phục Mật Khẩu</h2>
        <p class="text-gray-500 mt-2 text-sm">Đặt lại mật khẩu cho tài khoản của bạn</p>
      </div>

      <!-- Bước 1: Nhập email để lấy token -->
      <div v-if="step === 1" class="space-y-5">
        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Địa chỉ Email</label>
          <input 
            type="email" 
            v-model="email" 
            required 
            placeholder="example@domain.com"
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          />
        </div>

        <div v-if="errorMessage" class="bg-red-50 text-red-700 text-xs px-4 py-3 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </div>

        <button 
          @click="requestResetToken"
          :disabled="loading"
          class="w-full bg-red-700 text-white font-bold py-3 rounded-lg hover:bg-red-800 transition shadow-md disabled:opacity-50"
        >
          {{ loading ? 'Đang xử lý...' : 'GỬI MÃ KHÔI PHỤC' }}
        </button>
      </div>

      <!-- Bước 2: Nhập token và mật khẩu mới -->
      <div v-else class="space-y-5">
        
        <div class="bg-blue-50 text-blue-800 text-xs px-4 py-3 rounded-lg border border-blue-200 mb-4">
          ℹ️ <strong>Hệ thống giả lập:</strong> Mã khôi phục mật khẩu đã được tự động lấy từ phản hồi của máy chủ:<br/>
          <code class="block mt-1 font-mono font-bold bg-white p-1 rounded border text-red-700 text-center select-all">{{ resetToken }}</code>
        </div>

        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Mã khôi phục (Reset Token)</label>
          <input 
            type="text" 
            v-model="resetToken" 
            required 
            placeholder="Nhập mã khôi phục"
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition font-mono"
          />
        </div>

        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Mật khẩu mới</label>
          <input 
            type="password" 
            v-model="newPassword" 
            required 
            placeholder="Nhập mật khẩu mới"
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          />
        </div>

        <div v-if="errorMessage" class="bg-red-50 text-red-700 text-xs px-4 py-3 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </div>

        <button 
          @click="submitNewPassword"
          :disabled="loading"
          class="w-full bg-red-700 text-white font-bold py-3 rounded-lg hover:bg-red-800 transition shadow-md disabled:opacity-50"
        >
          {{ loading ? 'Đang cập nhật...' : 'ĐẶT LẠI MẬT KHẨU' }}
        </button>

        <button 
          @click="step = 1"
          class="w-full text-gray-500 text-sm hover:underline py-1"
        >
          Quay lại nhập email
        </button>
      </div>

      <div class="text-center mt-6 pt-6 border-t border-gray-100 text-sm text-gray-600">
        Quay lại trang 
        <router-link to="/login" class="text-red-700 font-bold hover:underline">
          Đăng nhập
        </router-link>
      </div>

    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email = ref("")
const resetToken = ref("")
const newPassword = ref("")
const step = ref(1)
const loading = ref(false)
const errorMessage = ref("")
const router = useRouter()

const requestResetToken = () => {
  if (!email.value.trim()) {
    errorMessage.value = "Vui lòng nhập địa chỉ email!"
    return
  }

  loading.value = true
  errorMessage.value = ""

  fetch("http://localhost:5000/api/auth/forgot-password", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email: email.value })
  })
    .then(async (res) => {
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data.message || "Email không hợp lệ!")
      }
      return data
    })
    .then((data) => {
      resetToken.value = data.resetToken
      step.value = 2
    })
    .catch((err) => {
      errorMessage.value = err.message
    })
    .finally(() => {
      loading.value = false
    })
}

const submitNewPassword = () => {
  if (!resetToken.value.trim() || !newPassword.value.trim()) {
    errorMessage.value = "Vui lòng nhập đầy đủ thông tin!"
    return
  }

  loading.value = true
  errorMessage.value = ""

  fetch("http://localhost:5000/api/auth/reset-password", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      token: resetToken.value,
      password: newPassword.value
    })
  })
    .then(async (res) => {
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data.message || "Mã khôi phục không hợp lệ!")
      }
      return data
    })
    .then((data) => {
      alert(data.message)
      router.push("/login")
    })
    .catch((err) => {
      errorMessage.value = err.message
    })
    .finally(() => {
      loading.value = false
    })
}
</script>
