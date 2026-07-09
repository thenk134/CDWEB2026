<template>
  <main class="container mx-auto px-4 py-12 max-w-xl min-h-screen">
    <!-- Thẻ Thông tin người dùng -->
    <div class="bg-white rounded-2xl border border-gray-200 shadow-xl p-8 mb-6 text-center">
      <div class="w-20 h-20 bg-gradient-to-tr from-red-600 to-amber-500 rounded-full flex items-center justify-center text-white text-3xl font-black mx-auto mb-4 shadow-md">
        {{ username.charAt(0).toUpperCase() }}
      </div>
      <h2 class="text-2xl font-black text-gray-800 mb-1 tracking-tight">{{ username }}</h2>
      <div class="flex flex-col items-center gap-1.5 mb-2">
        <p class="inline-block bg-gray-50 border border-gray-200 text-gray-650 px-4 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
          Quyền hạn: <span :class="roleColorClass">{{ role }}</span>
        </p>
        <span v-if="vipExpireDate" class="text-[11px] font-bold text-amber-600 bg-amber-50 border border-amber-200 px-3 py-0.5 rounded-full shadow-sm">
          Hạn dùng VIP: {{ formatDate(vipExpireDate) }}
        </span>
      </div>
      <div v-if="role === 'MEMBER' || role === 'ADMIN'" class="mt-2 text-sm text-gray-500 font-bold flex items-center justify-center gap-1">
        <Edit3 class="w-4 h-4 text-amber-500" />
        <span>Điểm nhuận bút: <span class="text-amber-500 text-base font-black">{{ Number(points).toFixed(1) }}</span> điểm</span>
      </div>
    </div>

    <!-- Tabs chuyển đổi nội dung -->
    <div class="flex border border-gray-200 mb-6 bg-white rounded-xl shadow-sm p-1">
      <button 
        @click="activeTab = 'general'"
        :class="[
          'flex-1 text-center py-2 rounded-lg text-[11px] font-bold uppercase transition cursor-pointer',
          activeTab === 'general' ? 'bg-red-700 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        Tài khoản
      </button>
      <button 
        @click="switchTab('history')"
        :class="[
          'flex-1 text-center py-2 rounded-lg text-[11px] font-bold uppercase transition cursor-pointer',
          activeTab === 'history' ? 'bg-red-700 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        Nhật ký điểm
      </button>
      <button 
        @click="switchTab('payouts')"
        :class="[
          'flex-1 text-center py-2 rounded-lg text-[11px] font-bold uppercase transition cursor-pointer',
          activeTab === 'payouts' ? 'bg-red-700 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        Lịch sử rút
      </button>
    </div>

    <!-- TAB 1: TÀI KHOẢN -->
    <div v-if="activeTab === 'general'" class="flex flex-col gap-4">
      <template v-if="role === 'ADMIN'">
        <router-link to="/admin" class="flex items-center justify-center gap-2.5 p-5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          <Briefcase class="w-5 h-5 text-white" />
          <span>Quản Lý Tin Tức</span>
        </router-link>
        <router-link to="/admin/revenue" class="flex items-center justify-center gap-2.5 p-5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          <TrendingUp class="w-5 h-5 text-white" />
          <span>Quản Lý Doanh Thu</span>
        </router-link>
        <router-link to="/admin/user-posts" class="flex items-center justify-center gap-2.5 p-5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          <FileText class="w-5 h-5 text-white" />
          <span>Quản lý bài viết cá nhân</span>
        </router-link>
      </template>

      <template v-else-if="role === 'MEMBER'">
        <!-- Quy đổi điểm sang VIP -->
        <div class="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm space-y-3">
          <h3 class="text-sm font-bold text-gray-800 uppercase tracking-wide flex items-center gap-1.5">
            <Star class="w-4 h-4 text-amber-500 fill-amber-500" />
            <span>Quy đổi điểm sang VIP</span>
          </h3>
          <p class="text-xs text-gray-500">Mức quy đổi cố định: <span class="font-bold text-gray-800">10.0 điểm = 7 ngày</span> Hội Viên VIP miễn phí.</p>
          <button 
            @click="redeemVipDays" 
            :disabled="points < 10.0 || vipRedeemSubmitting"
            class="w-full bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-bold text-xs py-2.5 rounded-lg shadow-sm transition disabled:opacity-50 cursor-pointer"
          >
            {{ vipRedeemSubmitting ? 'Đang thực hiện quy đổi...' : 'Xác nhận đổi 10 điểm nhuận bút' }}
          </button>
        </div>

        <!-- Yêu cầu rút tiền nhuận bút -->
        <div class="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm space-y-4">
          <h3 class="text-sm font-bold text-gray-800 uppercase tracking-wide flex items-center gap-1.5">
            <Edit3 class="w-4 h-4 text-purple-600" />
            <span>Rút tiền nhuận bút về tài khoản</span>
          </h3>
          <p class="text-xs text-gray-500">Tỷ lệ quy đổi: <span class="font-bold text-gray-800">1 điểm = 10,000 VND</span>. Rút tối thiểu <span class="font-bold text-red-700">10 điểm</span>.</p>
          
          <form @submit.prevent="submitPayoutRequest" class="space-y-3">
            <div>
              <label class="block text-[10px] font-bold text-gray-700 uppercase mb-1">Số điểm muốn rút</label>
              <input 
                type="number" 
                v-model="payoutPoints" 
                min="10" 
                :max="points"
                step="0.1" 
                required
                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
              />
            </div>
            <div>
              <label class="block text-[10px] font-bold text-gray-700 uppercase mb-1">Hình thức nhận tiền</label>
              <select v-model="payoutMethod" class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs bg-white">
                <option value="MOMO">Ví MoMo</option>
                <option value="BANK">Chuyển khoản Ngân hàng</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] font-bold text-gray-700 uppercase mb-1">Thông tin thanh toán (Số tài khoản, Tên chủ thẻ, Tên ngân hàng...)</label>
              <textarea 
                v-model="payoutInfo" 
                required 
                placeholder="Ví dụ: 0987654321 - Nguyen Van A - Vietcombank"
                class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
                rows="2"
              ></textarea>
            </div>
            <button 
              type="submit" 
              :disabled="payoutSubmitting || points < 10.0"
              class="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs py-2.5 rounded-lg shadow-sm transition disabled:opacity-50 cursor-pointer"
            >
              {{ payoutSubmitting ? 'Đang gửi yêu cầu...' : 'Gửi yêu cầu rút ' + Number(payoutPoints * 10000).toLocaleString() + 'đ' }}
            </button>
          </form>
        </div>

        <button @click="goToMemberPosts" class="flex items-center justify-center gap-2.5 p-5 bg-purple-600 hover:bg-purple-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5 cursor-pointer">
          <Edit3 class="w-5 h-5 text-white" />
          <span>Bài viết của tôi</span>
        </button>
        <router-link to="/" class="flex items-center justify-center gap-2.5 p-5 bg-gray-600 hover:bg-gray-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          <Newspaper class="w-5 h-5 text-white" />
          <span>Trở về trang tin</span>
        </router-link>
      </template>

      <template v-else>
        <button @click="upgradeAccount" class="flex items-center justify-center gap-2.5 p-5 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5 font-black cursor-pointer">
          <Star class="w-5 h-5 text-white fill-white animate-bounce" />
          <span>Nâng cấp hội viên</span>
        </button>
        <router-link to="/" class="flex items-center justify-center gap-2.5 p-5 bg-gray-600 hover:bg-gray-700 text-white font-bold text-lg rounded-xl shadow-md transition-all hover:-translate-y-0.5">
          <Newspaper class="w-5 h-5 text-white" />
          <span>Trở về trang tin</span>
        </router-link>
      </template>

      <button @click="handleLogout" class="flex items-center justify-center gap-2.5 p-5 bg-red-50 hover:bg-red-100 text-red-700 font-bold text-lg rounded-xl shadow-sm border border-red-200 transition-all mt-4 cursor-pointer">
        <LogOut class="w-5 h-5 text-red-750" />
        <span>Đăng xuất tài khoản</span>
      </button>
    </div>

    <!-- TAB 2: NHẬT KÝ ĐIỂM -->
    <div v-else-if="activeTab === 'history'" class="space-y-3 bg-white p-6 rounded-2xl border border-gray-250 shadow-md">
      <h3 class="text-sm font-bold text-gray-800 uppercase tracking-wide border-b pb-3 mb-4">Nhật ký biến động điểm</h3>
      <div v-if="txLoading" class="text-center py-10 text-gray-400 text-xs">Đang tải lịch sử...</div>
      <div v-else-if="txHistory.length === 0" class="text-center py-10 text-gray-400 text-xs italic">Chưa có biến động điểm nào.</div>
      <div v-else class="space-y-4 max-h-[400px] overflow-y-auto pr-1">
        <div v-for="tx in txHistory" :key="tx.id" class="border-b last:border-b-0 pb-3 flex justify-between items-start gap-4">
          <div class="min-w-0 flex-1">
            <p class="text-xs text-gray-800 font-bold leading-relaxed break-words">{{ tx.description }}</p>
            <span class="text-[9px] text-gray-400 mt-1 block font-mono">{{ formatDateTime(tx.createdAt) }}</span>
          </div>
          <span 
            :class="[
              'text-xs font-black px-2.5 py-0.5 rounded-full flex-shrink-0',
              tx.amount > 0 ? 'bg-emerald-50 text-emerald-700' : tx.amount < 0 ? 'bg-rose-50 text-rose-700' : 'bg-gray-100 text-gray-500'
            ]"
          >
            {{ tx.amount > 0 ? '+' : '' }}{{ tx.amount }}
          </span>
        </div>
      </div>
    </div>

    <!-- TAB 3: LỊCH SỬ RÚT TIỀN -->
    <div v-else-if="activeTab === 'payouts'" class="space-y-3 bg-white p-6 rounded-2xl border border-gray-250 shadow-md">
      <h3 class="text-sm font-bold text-gray-800 uppercase tracking-wide border-b pb-3 mb-4">Danh sách yêu cầu rút tiền</h3>
      <div v-if="payoutsLoading" class="text-center py-10 text-gray-400 text-xs">Đang tải yêu cầu...</div>
      <div v-else-if="payoutRequests.length === 0" class="text-center py-10 text-gray-400 text-xs italic">Chưa có yêu cầu rút tiền nào.</div>
      <div v-else class="space-y-4 max-h-[400px] overflow-y-auto pr-1">
        <div v-for="req in payoutRequests" :key="req.id" class="border-b last:border-b-0 pb-3 flex justify-between items-center gap-4">
          <div>
            <p class="text-xs text-gray-800 font-bold leading-relaxed">Rút {{ req.points }} điểm → <span class="text-red-700">{{ Number(req.amountMoney).toLocaleString() }}đ</span></p>
            <p class="text-[10px] text-gray-500 font-medium">Cổng: {{ req.payoutMethod }} • {{ req.payoutInfo }}</p>
            <span class="text-[9px] text-gray-400 mt-1 block font-mono">{{ formatDateTime(req.createdAt) }}</span>
          </div>
          <span 
            :class="[
              'text-[9px] font-black px-2.5 py-1 rounded-lg uppercase tracking-wider',
              req.status === 1 ? 'bg-green-100 text-green-800' : req.status === 2 ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'
            ]"
          >
            {{ req.status === 1 ? 'Đã duyệt' : req.status === 2 ? 'Bị từ chối' : 'Chờ duyệt' }}
          </span>
        </div>
      </div>
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
          <p class="text-xs text-gray-500 mb-6">Mức phí nâng cấp gói: <span class="font-bold text-amber-600 text-sm">$9.99 USD</span> (~250.000đ)</p>
          
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
import { Star, Briefcase, FileText, Newspaper, LogOut, Edit3, TrendingUp } from 'lucide-vue-next'

const router = useRouter()

// Lấy thông tin user
const username = ref(localStorage.getItem('username') || 'Người dùng')
const role = ref(localStorage.getItem('user_role') || 'USER')
const points = ref(0)
const vipExpireDate = ref('')

// Tabs
const activeTab = ref('general')
const txHistory = ref([])
const txLoading = ref(false)
const payoutRequests = ref([])
const payoutsLoading = ref(false)

// Forms
const payoutPoints = ref(10)
const payoutMethod = ref('MOMO')
const payoutInfo = ref('')
const payoutSubmitting = ref(false)
const vipRedeemSubmitting = ref(false)

const switchTab = (tabName) => {
  activeTab.value = tabName
  if (tabName === 'history') {
    fetchHistory()
  } else if (tabName === 'payouts') {
    fetchPayoutRequests()
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('vi-VN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const fetchProfile = () => {
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
      vipExpireDate.value = data.vipExpireDate || ''
      localStorage.setItem("user_role", data.role)
      localStorage.setItem("user_points", data.points || 0)
    })
    .catch(err => console.error("Lỗi tải profile:", err))
}

onMounted(() => {
  fetchProfile()
})

const fetchHistory = async () => {
  txLoading.value = true
  const token = localStorage.getItem('token')
  try {
    const res = await fetch("http://localhost:5000/api/points/history", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Không thể tải lịch sử biến động điểm")
    txHistory.value = await res.json()
  } catch (err) {
    toast.error(err.message)
  } finally {
    txLoading.value = false
  }
}

const fetchPayoutRequests = async () => {
  payoutsLoading.value = true
  const token = localStorage.getItem('token')
  try {
    const res = await fetch("http://localhost:5000/api/points/payout-requests", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Không thể tải danh sách yêu cầu rút tiền")
    payoutRequests.value = await res.json()
  } catch (err) {
    toast.error(err.message)
  } finally {
    payoutsLoading.value = false
  }
}

// Quy đổi điểm lấy ngày gia hạn VIP
const redeemVipDays = async () => {
  if (points.value < 10.0) return
  if (!confirm("Xác nhận quy đổi 10.0 điểm nhuận bút sang 7 ngày Hội Viên VIP?")) return

  vipRedeemSubmitting.value = true
  const token = localStorage.getItem('token')
  try {
    const res = await fetch("http://localhost:5000/api/points/redeem-vip", {
      method: "POST",
      headers: { "Authorization": `Bearer ${token}` }
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.message || "Giao dịch đổi VIP thất bại.")

    toast.success(data.message)
    points.value = data.points
    role.value = data.role
    vipExpireDate.value = data.vipExpireDate
    localStorage.setItem("user_role", data.role)
    localStorage.setItem("user_points", data.points)
  } catch (err) {
    toast.error(err.message)
  } finally {
    vipRedeemSubmitting.value = false
  }
}

// Đăng ký rút tiền nhuận bút
const submitPayoutRequest = async () => {
  if (payoutPoints.value < 10.0) {
    toast.warning("Số điểm quy đổi rút tiền tối thiểu là 10.0 điểm!")
    return
  }
  if (payoutPoints.value > points.value) {
    toast.warning("Số dư điểm nhuận bút của bạn không đủ!")
    return
  }

  payoutSubmitting.value = true
  const token = localStorage.getItem('token')
  try {
    const res = await fetch("http://localhost:5000/api/points/payout-request", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({
        points: payoutPoints.value,
        payoutMethod: payoutMethod.value,
        payoutInfo: payoutInfo.value
      })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.message || "Gửi yêu cầu thất bại.")

    toast.success(data.message)
    points.value = data.points
    localStorage.setItem("user_points", data.points)
    
    // Reset form
    payoutPoints.value = 10
    payoutInfo.value = ""
    
    // Refresh history / status
    fetchPayoutRequests()
  } catch (err) {
    toast.error(err.message)
  } finally {
    payoutSubmitting.value = false
  }
}

const roleColorClass = computed(() => {
  if (role.value === 'ADMIN') return 'text-red-650 font-extrabold'
  if (role.value === 'MEMBER') return 'text-purple-600 font-extrabold'
  return 'text-blue-650 font-extrabold'
})

const handleLogout = () => {
  if (confirm("Bạn có chắc chắn muốn đăng xuất tài khoản?")) {
    localStorage.clear()
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
  amount: 9.99,
  description: "",
  status: "PENDING"
})
const countdownSeconds = ref(300)
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
      countdownSeconds.value = 300
      
      clearInterval(timerInterval)
      timerInterval = setInterval(() => {
        if (countdownSeconds.value > 0) {
          countdownSeconds.value--
        } else {
          closePaymentModal()
          toast.warning("Giao dịch hết hạn thanh toán!")
        }
      }, 1000)

      clearInterval(statusPollInterval)
      statusPollInterval = setInterval(() => {
        fetch(`http://localhost:5000/api/payment/status/${data.orderId}`)
          .then(res => res.json())
          .then(statusData => {
            if (statusData.status === "PAID") {
              closePaymentModal()
              toast.success("Thanh toán thành công! Tài khoản đã nâng cấp thành Hội viên.")
              fetchProfile()
            }
          })
          .catch(err => console.error("Lỗi kiểm tra trạng thái đơn hàng:", err))
      }, 3000)

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
      fetchProfile()
    })
    .catch(err => {
      toast.error("Không thể giả lập thanh toán!")
    })
}
</script>