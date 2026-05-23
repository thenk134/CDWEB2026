<template>
  <footer class="bg-gray-900 text-gray-300 pt-10 pb-6 border-t-4 border-red-700">
    <div class="container mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
        
        <!-- Cột 1: Thông tin chung -->
        <div>
          <h2 class="text-white text-xl font-bold mb-4 uppercase">
            Báo Người Lao Động
          </h2>
          <p class="text-sm leading-relaxed">
            Cổng thông tin hỗ trợ người lao động, cập nhật tin tức pháp luật, 
            việc làm và bảo hiểm xã hội nhanh nhất, chính xác nhất.
          </p>
        </div>

        <!-- Cột 2: Chuyên mục nhanh -->
        <div>
          <h3 class="text-white font-semibold mb-4">Chuyên mục</h3>
          <ul class="text-sm space-y-2">
            <li><router-link to="/category/thoi-su" class="hover:text-red-500 cursor-pointer">➜ Tin tức thời sự</router-link></li>
            <li><router-link to="/category/phap-luat" class="hover:text-red-500 cursor-pointer">➜ Tư vấn pháp luật</router-link></li>
            <li><router-link to="/category/viec-lam" class="hover:text-red-500 cursor-pointer">➜ Tuyển dụng việc làm</router-link></li>
            <li><router-link to="/category/bao-hiem" class="hover:text-red-500 cursor-pointer">➜ Bảo hiểm xã hội</router-link></li>
          </ul>
        </div>

        <!-- Cột 3: Liên hệ -->
        <div>
          <h3 class="text-white font-semibold mb-4">Liên hệ</h3>
          <ul class="text-sm space-y-2">
            <li>Địa chỉ: 123 Đường Lao Động, TP. Hồ Chí Minh</li>
            <li>Email: hotro@baolaodong.vn</li>
            <li>Hotline: 1900 1234</li>
          </ul>
        </div>

      </div>

      <!-- Dòng bản quyền dưới cùng -->
      <div class="border-t border-gray-800 pt-6 text-center text-xs text-gray-500">
        <p>© 2026 Dự án Đồ án Nhóm 17 - LTFE. Thiết kế bởi CyanTakeOff.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
// Không cần xử lý logic phức tạp ở Footer
</script>
