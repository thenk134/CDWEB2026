<template>
  <main class="min-h-[80vh] flex items-center justify-center px-4 py-12 bg-gray-50">
    <div class="bg-white rounded-xl shadow-xl p-8 max-w-md w-full border border-gray-100">
      
      <div class="text-center mb-8">
        <h2 class="text-3xl font-black text-red-700 uppercase tracking-tight">Đăng Nhập</h2>
        <p class="text-gray-500 mt-2 text-sm">Chào mừng bạn trở lại với báo Người Lao Động</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-5">
        
        <div>
          <label class="block text-gray-700 text-sm font-bold mb-2">Tên đăng nhập</label>
          <input 
            type="text" 
            v-model="username" 
            required 
            placeholder="Nhập tên đăng nhập của bạn"
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          />
        </div>

        <div>
          <div class="flex justify-between items-center mb-2">
            <label class="block text-gray-700 text-sm font-bold">Mật khẩu</label>
            <router-link to="/forgot-password" class="text-xs text-red-700 hover:underline">
              Quên mật khẩu?
            </router-link>
          </div>
          <input 
            type="password" 
            v-model="password" 
            required 
            placeholder="Nhập mật khẩu"
            class="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-red-700/20 focus:border-red-700 transition"
          />
        </div>

        <div v-if="errorMessage" class="bg-red-50 text-red-700 text-xs px-4 py-3 rounded-lg border border-red-200">
          ⚠️ {{ errorMessage }}
        </div>

        <button 
          type="submit" 
          :disabled="loading"
          class="w-full bg-red-700 text-white font-bold py-3 rounded-lg hover:bg-red-800 transition shadow-md disabled:opacity-50"
        >
          {{ loading ? 'Đang xử lý...' : 'ĐĂNG NHẬP' }}
        </button>

      </form>

      <div class="text-center mt-6 pt-6 border-t border-gray-100 text-sm text-gray-600">
        Bạn chưa có tài khoản? 
        <router-link to="/register" class="text-red-700 font-bold hover:underline">
          Đăng ký ngay
        </router-link>
      </div>

    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref("")
const password = ref("")
const loading = ref(false)
const errorMessage = ref("")
const router = useRouter()

const handleLogin = () => {
  loading.value = true
  errorMessage.value = ""

  fetch("http://localhost:5000/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: username.value,
      password: password.value
    })
  })
    .then(async (res) => {
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data.message || "Đăng nhập thất bại!")
      }
      return data
    })
    .then((data) => {
      localStorage.setItem("token", data.token)
      localStorage.setItem("username", data.user.username)
      localStorage.setItem("user_role", data.user.role)
      
      alert(data.message)
      if (data.user.role === "ADMIN") {
        router.push("/admin")
      } else {
        router.push("/")
      }
    })
    .catch((err) => {
      errorMessage.value = err.message
    })
    .finally(() => {
      loading.value = false
    })
}
</script>
