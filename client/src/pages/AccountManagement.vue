<template>
  <main class="container mx-auto px-4 py-12 max-w-xl min-h-screen">
    <div class="bg-white rounded-2xl border border-gray-200 shadow-xl p-8 mb-8 text-center">
      <div class="w-20 h-20 bg-gradient-to-tr from-red-600 to-amber-500 rounded-full flex items-center justify-center text-white text-3xl font-black mx-auto mb-4 shadow-md">
        {{ username.charAt(0).toUpperCase() }}
      </div>
      <h2 class="text-2xl font-black text-gray-800 mb-1 tracking-tight">{{ username }}</h2>
      <p class="inline-block bg-gray-50 border border-gray-200 text-gray-650 px-4 py-1 rounded-full text-xs font-bold uppercase tracking-wider mb-2">
        Quyền hạn: <span :class="roleColorClass">{{ role }}</span>
      </p>
      <div v-if="role === 'MEMBER'" class="mt-2 text-sm text-gray-500 font-bold">
        ✍️ Điểm nhuận bút: <span class="text-amber-500 text-base font-black">{{ points }}</span> điểm
      </div>
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

    <!-- Modal Thanh Toán PayPal -->
    <div v-if="showPaymentModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fade-in">
      <div class="bg-white rounded-3xl border border-gray-100 shadow-2xl p-6 md:p-8 max-w-md w-full relative transform scale-100 transition-all">
        <!-- Nút Đóng -->
        <button 
          @click="closePaymentModal" 
          class="absolute top-4 right-4 text-gray-400 hover:text-gray-650 bg-gray-100 hover:bg-gray-200 w-8 h-8 rounded-full flex items-center justify-center font-bold text-lg cursor-pointer border-none"
        >
          ✕
        </button>

        <div class="text-center">
          <span class="inline-block bg-amber-50 text-amber-600 font-black text-[10px] px-3 py-1 rounded-full uppercase shadow-sm tracking-wider mb-2">
            ⭐ Nâng Cấp Hội Viên
          </span>
          <h3 class="text-xl font-black text-gray-900 mb-2">Thanh Toán Qua PayPal</h3>
          <p class="text-xs text-gray-500 mb-6">Mức phí nâng cấp gói: <span class="font-bold text-amber-600 text-sm">$2.00 USD</span> (~50.000đ)</p>
          
          <!-- PayPal Button Container -->
          <div class="my-6 min-h-[150px] flex items-center justify-center bg-gray-50 p-4 rounded-2xl border border-dashed border-gray-200">
            <div id="paypal-button-container" class="w-full">
              <!-- Nút PayPal sẽ tự động được render ở đây bởi SDK -->
              <div class="text-sm text-gray-400 animate-pulse">Đang tải cổng thanh toán PayPal...</div>
            </div>
          </div>

          <!-- Hẹn giờ / Loading -->
          <div class="flex items-center justify-center gap-2 mb-6">
            <div class="w-2 h-2 bg-amber-500 rounded-full animate-ping"></div>
            <span class="text-xs font-bold text-gray-500">Đang chờ thanh toán... Hết hạn sau: <span class="font-mono text-red-650 font-black">{{ formatTimer }}</span></span>
          </div>

          <div class="space-y-3">
            <!-- Nút giả lập thanh toán -->
            <button 
              @click="simulatePaymentSuccess"
              class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm py-3 rounded-xl shadow-md transition cursor-pointer uppercase tracking-wider border-none"
            >
              🚀 Giả lập thanh toán PayPal thành công
            </button>
            <button 
              @click="closePaymentModal" 
              class="w-full bg-white hover:bg-gray-50 text-gray-500 hover:text-gray-700 border border-gray-250 py-2.5 rounded-xl font-bold text-xs transition cursor-pointer"
            >
              Hủy bỏ thanh toán
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from '../utils/toast'

const router = useRouter()

// Lấy thông tin user đã lưu trong localStorage từ lúc Đăng nhập thành công
const username = ref(localStorage.getItem('username') || 'Người dùng')
const role = ref(localStorage.getItem('user_role') || 'USER')
const points = ref(0)

// Tự động đá người dùng chưa đăng nhập về trang Login bảo vệ dữ liệu công khai
onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  fetch("http://localhost:5000/api/auth/profile", {
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => res.json())
    .then(data => {
      role.value = data.role
      points.value = data.points || 0
      localStorage.setItem("user_role", data.role)
      localStorage.setItem("user_points", data.points || 0)
    })
    .catch(err => console.error("Lỗi tải profile:", err))
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
// Trạng thái Thanh Toán PayPal
const showPaymentModal = ref(false)
const currentOrder = ref({
  orderId: null,
  amount: 2.00,
  description: "",
  status: "PENDING"
})
const countdownSeconds = ref(300) // 5 minutes
let timerInterval = null
let statusPollInterval = null

const formatTimer = computed(() => {
  const m = Math.floor(countdownSeconds.value / 60)
  const s = countdownSeconds.value % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

const closePaymentModal = () => {
  showPaymentModal.value = false
  clearInterval(timerInterval)
  clearInterval(statusPollInterval)
}

// Tải script SDK PayPal động
const loadPayPalSdk = () => {
  return new Promise((resolve) => {
    if (window.paypal) {
      resolve()
      return
    }
    const script = document.createElement("script")
    script.src = "https://www.paypal.com/sdk/js?client-id=test"
    script.onload = () => resolve()
    document.head.appendChild(script)
  })
}

// Render nút PayPal Smart Payment
const renderPayPalButtons = (order) => {
  if (!window.paypal) return
  
  const container = document.getElementById("paypal-button-container")
  if (container) {
    container.innerHTML = ""
  }

  window.paypal.Buttons({
    createOrder: function(data, actions) {
      return actions.order.create({
        purchase_units: [{
          amount: {
            value: order.amount.toString()
          },
          description: order.description
        }]
      });
    },
    onApprove: function(data, actions) {
      return actions.order.capture().then(function(details) {
        simulatePaymentSuccess()
      });
    },
    onError: function(err) {
      console.error("Lỗi PayPal:", err)
      toast.error("Giao dịch PayPal gặp lỗi!")
    }
  }).render('#paypal-button-container')
}

const upgradeAccount = () => {
  const token = localStorage.getItem("token")
  if (!token) return

  fetch("http://localhost:5000/api/payment/create", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  })
    .then(res => {
      if (!res.ok) throw new Error("Không thể khởi tạo giao dịch!")
      return res.json()
    })
    .then(async (data) => {
      currentOrder.value = data
      showPaymentModal.value = true
      countdownSeconds.value = 300 // Reset timer
      
      // Khởi động đếm ngược
      clearInterval(timerInterval)
      timerInterval = setInterval(() => {
        if (countdownSeconds.value > 0) {
          countdownSeconds.value--
        } else {
          closePaymentModal()
          toast.warning("Giao dịch hết hạn thanh toán!")
        }
      }, 1000)

      // Bắt đầu Polling kiểm tra trạng thái đơn hàng mỗi 3 giây
      clearInterval(statusPollInterval)
      statusPollInterval = setInterval(() => {
        fetch(`http://localhost:5000/api/payment/status/${data.orderId}`)
          .then(res => res.json())
          .then(statusData => {
            if (statusData.status === "PAID") {
              closePaymentModal()
              toast.success("Thanh toán thành công! Tài khoản đã nâng cấp thành Hội viên.")
              localStorage.setItem("user_role", "MEMBER")
              role.value = "MEMBER"
              points.value = 0
            }
          })
          .catch(err => console.error("Lỗi kiểm tra trạng thái đơn hàng:", err))
      }, 3000)

      // Tải và hiển thị nút PayPal
      await loadPayPalSdk()
      setTimeout(() => {
        renderPayPalButtons(data)
      }, 100)
    })
    .catch(err => {
      toast.error(err.message)
    })
}

const simulatePaymentSuccess = () => {
  if (!currentOrder.value.orderId) return
  
  fetch(`http://localhost:5000/api/payment/simulate-success/${currentOrder.value.orderId}`, {
    method: "POST"
  })
    .then(res => {
      if (!res.ok) throw new Error("Giả lập lỗi!")
      return res.json()
    })
    .then(data => {
      closePaymentModal()
      toast.success("Giả lập thành công! Tài khoản của bạn đã được nâng cấp thành Hội viên.")
      localStorage.setItem("user_role", "MEMBER")
      role.value = "MEMBER"
      points.value = 0
    })
    .catch(err => {
      toast.error("Không thể giả lập thanh toán!")
    })
}
</script>