<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="container mx-auto px-4 py-3 flex flex-wrap justify-between items-center">
      
      <!-- Nhóm Logo và Thời tiết -->
      <div class="flex items-center space-x-6">
        <router-link to="/" class="flex items-center space-x-2">
          <div class="bg-red-700 text-white font-black px-3 py-1 rounded-sm text-2xl">NLĐ</div>
          <div class="leading-tight">
            <h1 class="text-xl font-extrabold text-red-700 tracking-tighter">NGƯỜI LAO ĐỘNG</h1>
            <p class="text-[10px] text-gray-500 font-medium uppercase tracking-widest">Tin tức & Việc làm</p>
          </div>
        </router-link>

        <!-- Widget Thời tiết hiển thị ở đây -->
        <div v-if="weather" class="hidden lg:flex items-center bg-gray-50 px-3 py-1 rounded-full border border-gray-100 space-x-1">
          <img 
            :src="`https://openweathermap.org/img/wn/${weather.icon}.png`" 
            alt="weather-icon" 
            class="w-8 h-8"
          />
          <div class="text-[11px] leading-tight">
            <p class="font-bold text-red-700">{{ weather.temp }}°C</p>
            <p class="text-gray-500 capitalize">{{ weather.description }}</p>
          </div>
        </div>
        <div v-else class="hidden lg:block w-20 h-8 bg-gray-100 animate-pulse rounded-full"></div>
      </div>

      <!-- Form tìm kiếm -->
      <form 
        @submit.prevent="handleSearch" 
        class="hidden sm:flex items-center bg-gray-100 border border-gray-200 rounded-full px-4 py-1.5 w-72"
      >
        <input 
          type="text" 
          placeholder="Tìm kiếm tin tức..." 
          class="bg-transparent focus:outline-none text-sm w-full"
          v-model="keyword"
        />
        <button type="submit" class="text-gray-400 hover:text-red-700">
          👌
        </button>
      </form>
    </div>

    <!-- Navigation Menu -->
    <nav class="bg-red-700">
      <div class="container mx-auto px-4">
        <ul class="flex items-center space-x-6 overflow-x-auto no-scrollbar py-2.5 text-white text-sm font-bold uppercase whitespace-nowrap">
          <li><router-link to="/" class="hover:text-yellow-400 transition">Trang chủ</router-link></li>
          <li><router-link to="/category/thoi-su" class="hover:text-yellow-400 transition">Thời sự</router-link></li>
          <li><router-link to="/category/viec-lam" class="hover:text-yellow-400 transition">Việc làm</router-link></li>
          <li><router-link to="/category/phap-luat" class="hover:text-yellow-400 transition">Pháp luật</router-link></li>
          <li><router-link to="/category/bao-hiem" class="hover:text-yellow-400 transition">Bảo hiểm</router-link></li>
          <li><router-link to="/category/cong-doan" class="hover:text-yellow-400 transition">Công đoàn</router-link></li>
          <li><router-link to="/category/suc-khoe" class="hover:text-yellow-400 transition">Sức khỏe</router-link></li>
          <li><router-link to="/bookmarks" class="hover:text-yellow-400 transition">📌 TIN ĐÃ LƯU</router-link></li>
        </ul>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const keyword = ref("")
const weather = ref(null)
const router = useRouter()

const apiKey = "127d989b114b9d3db5aef385245818b9"
const city = "Ho Chi Minh"

onMounted(() => {
  fetch(`https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${apiKey}&lang=vi`)
    .then(res => res.json())
    .then(data => {
      if (data.main) {
        weather.value = {
          temp: Math.round(data.main.temp),
          icon: data.weather[0].icon,
          description: data.weather[0].description
        }
      }
    })
    .catch(err => console.error("Không tải được thời tiết!", err))
})

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { q: keyword.value.trim() } })
  }
}
</script>
