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

      <!-- Phải: Form tìm kiếm + Auth status -->
      <div class="flex items-center space-x-4">
        
        <!-- Form tìm kiếm chứa trong container relative phục vụ Live Search -->
        <div class="relative hidden sm:block">
          <form 
            @submit.prevent="handleSearch" 
            class="flex items-center bg-gray-100 border border-gray-200 rounded-full px-4 py-1.5 w-64"
          >
            <input 
              type="text" 
              placeholder="Tìm kiếm tin tức..." 
              class="bg-transparent focus:outline-none text-sm w-full"
              v-model="keyword"
              @input="onSearchInput"
              @focus="showSuggestions = true"
              @blur="onBlur"
            />
            <button type="submit" class="text-gray-400 hover:text-red-700">
              🔎
            </button>
          </form>

          <!-- Dropdown Gợi ý tìm kiếm trực tiếp bằng AJAX -->
          <div 
            v-if="showSuggestions && suggestions.length > 0" 
            class="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-xl z-50 max-h-80 overflow-y-auto w-72"
          >
            <div class="p-2.5 text-[10px] font-bold text-gray-400 border-b uppercase tracking-wider">Tin tức gợi ý</div>
            <ul>
              <li 
                v-for="item in suggestions" 
                :key="item.id"
                class="border-b last:border-b-0 border-gray-100"
              >
                <router-link 
                  :to="{
                    path: '/news-detail',
                    query: {
                      url: item.link,
                      date: item.pubDate
                    }
                  }"
                  class="flex items-center p-2.5 hover:bg-red-50/50 transition gap-3"
                  @click="selectSuggestion"
                >
                  <img :src="item.image" class="w-10 h-10 object-cover rounded shadow-sm flex-shrink-0" />
                  <div class="text-left min-w-0">
                    <p class="text-xs font-bold text-gray-800 truncate hover:text-red-700">{{ item.title }}</p>
                    <p class="text-[10px] text-gray-400 mt-0.5 capitalize">{{ item.category }} • {{ item.source }}</p>
                  </div>
                </router-link>
              </li>
            </ul>
          </div>
          
          <!-- Trạng thái đang tải gợi ý -->
          <div 
            v-else-if="showSuggestions && keyword.trim().length >= 2 && loadingSuggestions"
            class="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-xl z-50 p-4 text-center text-xs text-gray-400 w-72"
          >
            Đang tìm kiếm nhanh...
          </div>
        </div>

        <!-- Auth Section -->
        <div class="flex items-center space-x-2 text-sm font-semibold">
          <template v-if="isLoggedIn">
            <span class="text-gray-700 hidden md:inline">
              Chào, <span class="text-red-700 font-bold">{{ username }}</span>
            </span>
            <router-link
                to="/account-management"
                class="bg-amber-500 text-white px-3 py-1.5 rounded-full hover:bg-amber-600 transition text-xs shadow-sm font-bold"
            >
              ⚙️ Quản lý tài khoản
            </router-link>
            <button 
              @click="handleLogout" 
              class="bg-gray-100 text-gray-700 border border-gray-200 px-3 py-1.5 rounded-full hover:bg-gray-200 transition text-xs"
            >
              Đăng xuất
            </button>
          </template>
          <template v-else>
            <router-link 
              to="/login" 
              class="text-red-700 hover:text-red-800 px-3 py-1.5 transition text-xs md:text-sm"
            >
              Đăng nhập
            </router-link>
            <router-link 
              to="/register" 
              class="bg-red-700 text-white px-4 py-1.5 rounded-full hover:bg-red-800 transition text-xs md:text-sm shadow-sm"
            >
              Đăng ký
            </router-link>
          </template>
        </div>
      </div>
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
          <li><router-link to="/category/quandiem-tranhluan" class="hover:text-yellow-400 transition">Quan điểm - Tranh luận</router-link></li>
        </ul>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const keyword = ref("")
const weather = ref(null)
const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const username = ref("")
const isAdmin = ref(false)

const showSuggestions = ref(false)
const suggestions = ref([])
const loadingSuggestions = ref(false)
let debounceTimeout = null

const checkAuth = () => {
  const token = localStorage.getItem("token")
  isLoggedIn.value = !!token
  username.value = localStorage.getItem("username") || ""
  isAdmin.value = localStorage.getItem("user_role") === "ADMIN"
}

// Cập nhật trạng thái đăng nhập khi đổi route
watch(() => route.path, () => {
  checkAuth()
})

const onSearchInput = () => {
  if (debounceTimeout) clearTimeout(debounceTimeout)
  
  if (keyword.value.trim().length < 2) {
    suggestions.value = []
    return
  }

  loadingSuggestions.value = true
  debounceTimeout = setTimeout(() => {
    fetch(`http://localhost:5000/api/news/search?query=${encodeURIComponent(keyword.value.trim())}`)
      .then(res => res.json())
      .then(data => {
        // Lấy tối đa 5 bài báo làm gợi ý
        suggestions.value = Array.isArray(data) ? data.slice(0, 5) : []
      })
      .catch(err => {
        console.error("Lỗi gợi ý tìm kiếm:", err)
        suggestions.value = []
      })
      .finally(() => {
        loadingSuggestions.value = false
      })
  }, 300)
}

const onBlur = () => {
  // Trì hoãn đóng dropdown một chút để kịp kích hoạt sự kiện click của router-link
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const selectSuggestion = () => {
  showSuggestions.value = false
  keyword.value = ""
}

const apiKey = "127d989b114b9d3db5aef385245818b9"
const city = "Ho Chi Minh"

onMounted(() => {
  checkAuth()
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
    showSuggestions.value = false
  }
}

const handleLogout = () => {
  const token = localStorage.getItem("token")
  fetch("http://localhost:5000/api/auth/logout", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  }).finally(() => {
    localStorage.removeItem("token")
    localStorage.removeItem("username")
    localStorage.removeItem("user_role")
    checkAuth()
    router.push("/")
  })
}
</script>
