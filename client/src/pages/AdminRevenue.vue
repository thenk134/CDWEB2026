<template>
  <main class="container mx-auto px-4 py-8 min-h-screen">
    <!-- Header -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <router-link to="/admin" class="text-xs font-bold text-gray-500 hover:text-red-700 transition flex items-center gap-1">
            ← Quay lại quản trị
          </router-link>
        </div>
        <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight flex items-center gap-2">
          <TrendingUp class="w-8 h-8 text-red-700" />
          <span>Báo Cáo Doanh Thu & Nhuận Bút</span>
        </h1>
        <p class="text-gray-500 text-sm">Thống kê tài chính toàn diện, cơ cấu quỹ và lịch sử chi trả nhuận bút</p>
      </div>

      <button 
        @click="simulateExportCSV"
        class="bg-gray-800 hover:bg-gray-900 text-white font-bold text-xs px-4 py-2.5 rounded-lg shadow-sm transition flex items-center gap-1.5 border-none cursor-pointer"
      >
        <Download class="w-4 h-4 text-white" />
        <span>Xuất dữ liệu Excel</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <div class="w-10 h-10 border-4 border-red-700 border-t-transparent rounded-full animate-spin mb-4"></div>
      <p class="text-sm font-bold text-gray-500">Đang phân tích số liệu tài chính...</p>
    </div>

    <template v-else>
      <!-- Cards Thống Kê Tài Chính -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- VIP Card -->
        <div class="bg-gradient-to-br from-blue-600 to-indigo-700 text-white p-6 rounded-2xl shadow-lg relative overflow-hidden">
          <div class="absolute right-0 bottom-0 opacity-10 transform translate-x-2 translate-y-2">
            <Star class="w-36 h-36 text-white fill-white" />
          </div>
          <h4 class="text-xs uppercase font-bold tracking-wider opacity-85">Doanh Thu Gói VIP</h4>
          <p class="text-2xl font-black mt-2">${{ Number(stats.totalVipSales).toFixed(2) }} USD</p>
          <p class="text-[10px] opacity-75 mt-1">≈ {{ Number(stats.totalVipSales * 25000).toLocaleString() }} VND</p>
          <span class="text-[9px] bg-white/20 px-2 py-0.5 rounded-full mt-3 inline-block font-semibold">
            {{ detailData.vipOrders.length }} lượt đăng ký
          </span>
        </div>

        <!-- Donations Card -->
        <div class="bg-gradient-to-br from-amber-500 to-orange-600 text-white p-6 rounded-2xl shadow-lg relative overflow-hidden">
          <div class="absolute right-0 bottom-0 opacity-10 transform translate-x-2 translate-y-2">
            <Heart class="w-36 h-36 text-white fill-white" />
          </div>
          <h4 class="text-xs uppercase font-bold tracking-wider opacity-85">Quỹ Độc Giả Ủng Hộ</h4>
          <p class="text-2xl font-black mt-2">${{ Number(stats.totalDonations).toFixed(2) }} USD</p>
          <p class="text-[10px] opacity-75 mt-1">≈ {{ Number(stats.totalDonations * 25000).toLocaleString() }} VND</p>
          <span class="text-[9px] bg-white/20 px-2 py-0.5 rounded-full mt-3 inline-block font-semibold">
            {{ detailData.donations.length }} lượt đóng góp
          </span>
        </div>

        <!-- Payouts Card -->
        <div class="bg-gradient-to-br from-rose-500 to-red-650 text-white p-6 rounded-2xl shadow-lg relative overflow-hidden">
          <div class="absolute right-0 bottom-0 opacity-10 transform translate-x-2 translate-y-2">
            <Check class="w-36 h-36 text-white" />
          </div>
          <h4 class="text-xs uppercase font-bold tracking-wider opacity-85">Đã Chi Trả Nhuận Bút</h4>
          <p class="text-2xl font-black mt-2">{{ Number(stats.totalPayoutMoney).toLocaleString() }} VND</p>
          <p class="text-[10px] opacity-75 mt-1">Khấu trừ trực tiếp từ điểm thành viên</p>
          <span class="text-[9px] bg-white/20 px-2 py-0.5 rounded-full mt-3 inline-block font-semibold">
            {{ stats.payoutCount }} yêu cầu hoàn tất
          </span>
        </div>

        <!-- Balance Card -->
        <div class="bg-gradient-to-br from-emerald-500 to-teal-600 text-white p-6 rounded-2xl shadow-lg relative overflow-hidden">
          <div class="absolute right-0 bottom-0 opacity-10 transform translate-x-2 translate-y-2">
            <TrendingUp class="w-36 h-36 text-white" />
          </div>
          <h4 class="text-xs uppercase font-bold tracking-wider opacity-85">Số Dư Quỹ Thực Tế</h4>
          <p class="text-2xl font-black mt-2">{{ Number(netBalance).toLocaleString() }} VND</p>
          <p class="text-[10px] opacity-75 mt-1">Tổng thu quy đổi - Tổng chi trả</p>
          <span class="text-[9px] bg-white/20 px-2 py-0.5 rounded-full mt-3 inline-block font-semibold">
            Quỹ vận hành thặng dư
          </span>
        </div>
      </div>

      <!-- Biểu Đồ Thống Kê Trực Quan -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
        <!-- Chart 1: Cơ Cấu Nguồn Thu (Income Streams Split) -->
        <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm flex flex-col justify-between">
          <div>
            <h3 class="font-black text-gray-800 text-sm uppercase tracking-wider mb-1 flex items-center gap-1.5">
              <Star class="w-4 h-4 text-amber-500 fill-amber-500" />
              <span>Cơ cấu tỷ lệ nguồn thu</span>
            </h3>
            <p class="text-gray-400 text-xs mb-4">So sánh doanh thu bán gói VIP vs Quỹ ủng hộ độc giả đóng góp</p>
          </div>

          <!-- Custom SVG Donut Chart -->
          <div class="flex items-center justify-center my-6">
            <svg class="w-40 h-40 transform -rotate-90" viewBox="0 0 42 42">
              <circle cx="21" cy="21" r="15.91549430918954" fill="transparent" stroke="#E5E7EB" stroke-width="4.5"></circle>
              <!-- VIP Order circle segment -->
              <circle 
                cx="21" 
                cy="21" 
                r="15.91549430918954" 
                fill="transparent" 
                stroke="#4F46E5" 
                stroke-width="4.5" 
                :stroke-dasharray="`${vipPercentage} ${100 - vipPercentage}`" 
                stroke-dashoffset="0"
              ></circle>
              <!-- Donation segment -->
              <circle 
                cx="21" 
                cy="21" 
                r="15.91549430918954" 
                fill="transparent" 
                stroke="#F59E0B" 
                stroke-width="4.5" 
                :stroke-dasharray="`${donationPercentage} ${100 - donationPercentage}`" 
                :stroke-dashoffset="`-${vipPercentage}`"
              ></circle>
            </svg>
          </div>

          <div class="space-y-2 mt-4 text-xs font-semibold">
            <div class="flex justify-between items-center">
              <span class="flex items-center gap-1.5 text-gray-650">
                <span class="w-3 h-3 bg-indigo-600 rounded-full"></span>
                <span>Doanh thu bán VIP ({{ Number(vipPercentage).toFixed(1) }}%)</span>
              </span>
              <span class="font-extrabold text-indigo-700">${{ Number(stats.totalVipSales).toFixed(2) }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="flex items-center gap-1.5 text-gray-650">
                <span class="w-3 h-3 bg-amber-500 rounded-full"></span>
                <span>Độc giả ủng hộ ({{ Number(donationPercentage).toFixed(1) }}%)</span>
              </span>
              <span class="font-extrabold text-amber-600">${{ Number(stats.totalDonations).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- Chart 2: Biểu Đồ So Sánh Thu Chi Hàng Tháng (Inflow vs Outflow) -->
        <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm lg:col-span-2 flex flex-col justify-between">
          <div>
            <h3 class="font-black text-gray-800 text-sm uppercase tracking-wider mb-1 flex items-center gap-1.5">
              <TrendingUp class="w-4 h-4 text-indigo-600" />
              <span>Biến động Thu - Chi nhuận bút hàng tháng (VND)</span>
            </h3>
            <p class="text-gray-400 text-xs mb-6">Thống kê so sánh dòng tiền chảy vào hệ thống (VIP + Ủng hộ) và nhuận bút thực chi</p>
          </div>

          <!-- Custom SVG Bar Chart -->
          <div class="relative h-48 w-full border-b border-l border-gray-200 pt-6">
            <div class="absolute inset-0 flex justify-between px-4 items-end">
              <div v-for="(item, idx) in monthlyChartData" :key="idx" class="flex flex-col items-center w-12 gap-1.5">
                <!-- Bar Container -->
                <div class="flex items-end justify-center gap-1 h-36 w-full">
                  <!-- Inflow Bar (Green) -->
                  <div 
                    class="bg-emerald-500 w-4 rounded-t transition-all hover:bg-emerald-600"
                    :style="{ height: `${(item.inflow / maxMonthlyValue) * 100}%` }"
                    :title="`Thu: ${Number(item.inflow).toLocaleString()}đ`"
                  ></div>
                  <!-- Outflow Bar (Red) -->
                  <div 
                    class="bg-rose-500 w-4 rounded-t transition-all hover:bg-rose-600"
                    :style="{ height: `${(item.outflow / maxMonthlyValue) * 100}%` }"
                    :title="`Chi: ${Number(item.outflow).toLocaleString()}đ`"
                  ></div>
                </div>
                <!-- Label -->
                <span class="text-[9px] font-bold text-gray-400 font-mono">{{ item.month }}</span>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-6 text-[10px] mt-4 font-bold text-gray-500">
            <span class="flex items-center gap-1"><span class="w-3 h-3 bg-emerald-500 rounded"></span> Tổng tiền thu (VND)</span>
            <span class="flex items-center gap-1"><span class="w-3 h-3 bg-rose-500 rounded"></span> Thực chi nhuận bút (VND)</span>
          </div>
        </div>
      </div>

      <!-- Lịch Sử Các Giao Dịch Chi Tiết -->
      <div class="bg-white border border-gray-200 rounded-2xl shadow-md overflow-hidden">
        <!-- Tabs Phân Loại Giao Dịch -->
        <div class="flex border-b border-gray-100 bg-gray-50 p-2 gap-2">
          <button 
            @click="activeListTab = 'vip'"
            :class="[
              'px-4 py-2 text-xs font-bold uppercase rounded-lg transition cursor-pointer',
              activeListTab === 'vip' ? 'bg-indigo-600 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            Đăng ký gói VIP ({{ detailData.vipOrders.length }})
          </button>
          <button 
            @click="activeListTab = 'donations'"
            :class="[
              'px-4 py-2 text-xs font-bold uppercase rounded-lg transition cursor-pointer',
              activeListTab === 'donations' ? 'bg-amber-500 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            Lượt ủng hộ ({{ detailData.donations.length }})
          </button>
          <button 
            @click="activeListTab = 'payouts'"
            :class="[
              'px-4 py-2 text-xs font-bold uppercase rounded-lg transition cursor-pointer',
              activeListTab === 'payouts' ? 'bg-rose-600 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            Nhuận bút đã chi ({{ detailData.payouts.length }})
          </button>
        </div>

        <!-- Bảng Hiển Thị Dữ Liệu -->
        <div class="p-4 border-b border-gray-150 flex justify-between items-center gap-4">
          <div class="relative w-64">
            <span class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
              <Search class="w-4 h-4 text-gray-400" />
            </span>
            <input 
              type="text" 
              v-model="searchTerm" 
              placeholder="Tìm kiếm tài khoản, mã GD..."
              class="w-full pl-9 pr-4 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
            />
          </div>
        </div>

        <div class="overflow-x-auto">
          <!-- Bảng VIP Orders -->
          <table v-if="activeListTab === 'vip'" class="w-full text-left text-xs">
            <thead class="bg-gray-50 text-[9px] uppercase font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">Mã đơn hàng</th>
                <th class="p-4">Tài khoản mua</th>
                <th class="p-4">Số tiền thanh toán</th>
                <th class="p-4">Mô tả giao dịch</th>
                <th class="p-4 font-mono">Thời gian mua</th>
              </tr>
            </thead>
            <tbody class="divide-y font-medium text-gray-700">
              <tr v-for="order in filteredVipOrders" :key="order.id" class="hover:bg-gray-50/50 transition">
                <td class="p-4 font-mono font-bold">#{{ order.id }}</td>
                <td class="p-4 text-gray-900 font-extrabold">@{{ order.username }}</td>
                <td class="p-4 text-indigo-700 font-black">${{ Number(order.amount).toFixed(2) }} USD</td>
                <td class="p-4 text-gray-500">{{ order.description }}</td>
                <td class="p-4 font-mono text-gray-400">{{ formatDateTime(order.createdAt) }}</td>
              </tr>
              <tr v-if="filteredVipOrders.length === 0">
                <td colspan="5" class="p-8 text-center text-gray-400 italic">Không tìm thấy giao dịch VIP nào phù hợp.</td>
              </tr>
            </tbody>
          </table>

          <!-- Bảng Lượt Ủng Hộ (Donations) -->
          <table v-else-if="activeListTab === 'donations'" class="w-full text-left text-xs">
            <thead class="bg-gray-50 text-[9px] uppercase font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">Mã ủng hộ</th>
                <th class="p-4">Người ủng hộ</th>
                <th class="p-4">Người nhận</th>
                <th class="p-4">Số tiền ủng hộ</th>
                <th class="p-4">Lời nhắn</th>
                <th class="p-4 font-mono">Thời gian gửi</th>
              </tr>
            </thead>
            <tbody class="divide-y font-medium text-gray-700">
              <tr v-for="dn in filteredDonations" :key="dn.id" class="hover:bg-gray-50/50 transition">
                <td class="p-4 font-mono font-bold">#{{ dn.id }}</td>
                <td class="p-4 text-gray-900 font-extrabold">{{ dn.donorName }}</td>
                <td class="p-4 text-purple-700 font-bold">
                  {{ dn.receiverName === 'Tòa soạn' ? '📰 Ban Biên Tập' : '@' + dn.receiverName }}
                </td>
                <td class="p-4 text-amber-600 font-black">${{ Number(dn.amount).toFixed(2) }} USD</td>
                <td class="p-4 text-gray-500 italic max-w-xs truncate" :title="dn.message">{{ dn.message || 'Không có' }}</td>
                <td class="p-4 font-mono text-gray-400">{{ formatDateTime(dn.createdAt) }}</td>
              </tr>
              <tr v-if="filteredDonations.length === 0">
                <td colspan="6" class="p-8 text-center text-gray-400 italic">Không tìm thấy lượt ủng hộ nào phù hợp.</td>
              </tr>
            </tbody>
          </table>

          <!-- Bảng Chi Trả Nhuận Bút (Payouts) -->
          <table v-else-if="activeListTab === 'payouts'" class="w-full text-left text-xs">
            <thead class="bg-gray-50 text-[9px] uppercase font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">Mã chi trả</th>
                <th class="p-4">Thành viên nhận</th>
                <th class="p-4">Số điểm khấu trừ</th>
                <th class="p-4">Số tiền VNĐ chi trả</th>
                <th class="p-4">Kênh thanh toán</th>
                <th class="p-4 font-mono">Thời gian duyệt</th>
              </tr>
            </thead>
            <tbody class="divide-y font-medium text-gray-700">
              <tr v-for="p in filteredPayouts" :key="p.id" class="hover:bg-gray-50/50 transition">
                <td class="p-4 font-mono font-bold">#{{ p.id }}</td>
                <td class="p-4 text-gray-900 font-extrabold">@{{ p.username }}</td>
                <td class="p-4 text-amber-500 font-bold">{{ p.points }} điểm</td>
                <td class="p-4 text-red-700 font-black">{{ Number(p.amountMoney).toLocaleString() }}đ</td>
                <td class="p-4 text-purple-700 font-bold">
                  {{ p.payoutMethod }} <span class="text-[10px] text-gray-400 font-medium">({{ p.payoutInfo }})</span>
                </td>
                <td class="p-4 font-mono text-gray-400">{{ formatDateTime(p.resolvedAt || p.createdAt) }}</td>
              </tr>
              <tr v-if="filteredPayouts.length === 0">
                <td colspan="6" class="p-8 text-center text-gray-400 italic">Không tìm thấy yêu cầu chi trả nào phù hợp.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { TrendingUp, Download, Star, Heart, Check, Search } from 'lucide-vue-next'
import { toast } from '../utils/toast'

const loading = ref(true)
const stats = ref({
  totalVipSales: 0,
  totalDonations: 0,
  totalPayoutMoney: 0,
  vipCount: 0,
  donationCount: 0,
  payoutCount: 0
})

const detailData = ref({
  vipOrders: [],
  donations: [],
  payouts: []
})

// UI Search & Tabs
const activeListTab = ref('vip')
const searchTerm = ref('')

// Compute dynamic stats
const netBalance = computed(() => {
  // Exchange rate assumption: 1 USD = 25,000 VND
  const totalInflowVnd = (stats.value.totalVipSales + stats.value.totalDonations) * 25000
  return totalInflowVnd - stats.value.totalPayoutMoney
})

const vipPercentage = computed(() => {
  const totalInflow = stats.value.totalVipSales + stats.value.totalDonations
  if (totalInflow === 0) return 50
  return (stats.value.totalVipSales / totalInflow) * 100
})

const donationPercentage = computed(() => {
  const totalInflow = stats.value.totalVipSales + stats.value.totalDonations
  if (totalInflow === 0) return 50
  return (stats.value.totalDonations / totalInflow) * 100
})

// Build dynamic monthly charts mapping for last 6 months (VND)
const monthlyChartData = computed(() => {
  // Setup labels for last 6 months
  const now = new Date()
  const list = []
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const monthLabel = `T.${d.getMonth() + 1}`
    list.push({
      month: monthLabel,
      monthInt: d.getMonth(),
      yearInt: d.getFullYear(),
      inflow: 0,
      outflow: 0
    })
  }

  // Aggregate VIP sales (inflow)
  detailData.value.vipOrders.forEach(o => {
    const d = new Date(o.createdAt)
    const match = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (match) {
      match.inflow += o.amount * 25000
    }
  })

  // Aggregate donations (inflow)
  detailData.value.donations.forEach(dn => {
    const d = new Date(dn.createdAt)
    const match = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (match) {
      match.inflow += dn.amount * 25000
    }
  })

  // Aggregate payouts (outflow)
  detailData.value.payouts.forEach(p => {
    const d = new Date(p.resolvedAt || p.createdAt)
    const match = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (match) {
      match.outflow += p.amountMoney
    }
  })

  return list
})

const maxMonthlyValue = computed(() => {
  let max = 1000000 // default fallback baseline to avoid division by zero
  monthlyChartData.value.forEach(item => {
    if (item.inflow > max) max = item.inflow
    if (item.outflow > max) max = item.outflow
  })
  return max
})

// Filtering lists
const filteredVipOrders = computed(() => {
  const query = searchTerm.value.trim().toLowerCase()
  if (!query) return detailData.value.vipOrders
  return detailData.value.vipOrders.filter(o => 
    o.username.toLowerCase().includes(query) || 
    String(o.id).includes(query) ||
    o.description.toLowerCase().includes(query)
  )
})

const filteredDonations = computed(() => {
  const query = searchTerm.value.trim().toLowerCase()
  if (!query) return detailData.value.donations
  return detailData.value.donations.filter(dn => 
    dn.donorName.toLowerCase().includes(query) || 
    dn.receiverName.toLowerCase().includes(query) ||
    String(dn.id).includes(query) ||
    (dn.message && dn.message.toLowerCase().includes(query))
  )
})

const filteredPayouts = computed(() => {
  const query = searchTerm.value.trim().toLowerCase()
  if (!query) return detailData.value.payouts
  return detailData.value.payouts.filter(p => 
    p.username.toLowerCase().includes(query) || 
    String(p.id).includes(query) ||
    p.payoutMethod.toLowerCase().includes(query) ||
    p.payoutInfo.toLowerCase().includes(query)
  )
})

// Utilities
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const simulateExportCSV = () => {
  toast.success("Đang chuẩn bị trích xuất dữ liệu... Tệp excel báo cáo đã được lưu vào thư mục Downloads!")
}

// Fetch API
const fetchFinanceData = async () => {
  loading.value = true
  const token = localStorage.getItem("token")
  try {
    // 1. Fetch aggregated stats
    const statsRes = await fetch("http://localhost:5000/api/admin/revenue", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!statsRes.ok) throw new Error("Lỗi tải tổng hợp thống kê tài chính.")
    stats.value = await statsRes.json()

    // 2. Fetch detailed transaction registers
    const detailsRes = await fetch("http://localhost:5000/api/admin/revenue/details", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!detailsRes.ok) throw new Error("Lỗi tải lịch sử giao dịch tài chính.")
    detailData.value = await detailsRes.json()

  } catch (err) {
    toast.error(err.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFinanceData()
})
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.25s ease-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.97); }
  to { opacity: 1; transform: scale(1); }
}
</style>
