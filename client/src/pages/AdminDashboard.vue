<template>
  <div class="min-h-screen bg-slate-50 flex">
    
    <!-- LEFT SIDEBAR -->
    <aside class="w-64 bg-slate-900 text-slate-100 flex flex-col fixed top-0 bottom-0 left-0 z-20 border-r border-slate-800 shadow-xl">
      <!-- Sidebar Brand Header -->
      <div class="p-6 border-b border-slate-800 flex items-center gap-3">
        <div class="w-8 h-8 bg-red-700 rounded-lg flex items-center justify-center font-black text-white text-lg">
          L
        </div>
        <div>
          <h2 class="font-black text-sm tracking-tight text-white uppercase">Lao Động Admin</h2>
          <p class="text-[9px] text-slate-400 font-bold uppercase tracking-wider">Hệ thống quản trị</p>
        </div>
      </div>

      <!-- Navigation Links -->
      <nav class="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
        <!-- Overview Link -->
        <button 
          @click="switchMenu('dashboard')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'dashboard' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <LayoutDashboard class="w-4 h-4" />
            <span>Bảng Điều Khiển</span>
          </span>
        </button>

        <!-- News Link -->
        <button 
          @click="switchMenu('news')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'news' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <FolderOpen class="w-4 h-4" />
            <span>Quản Lý Tin Tức</span>
          </span>
          <span class="text-[10px] bg-slate-800 text-slate-300 px-2 py-0.5 rounded-full font-bold">
            {{ articles.length }}
          </span>
        </button>

        <!-- Member Posts Link -->
        <button 
          @click="switchMenu('member-posts')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'member-posts' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <FileText class="w-4 h-4" />
            <span>Duyệt Bài Thành Viên</span>
          </span>
          <span 
            v-if="pendingPostsCount > 0"
            class="text-[9px] bg-amber-500 text-slate-900 px-2 py-0.5 rounded-full font-black animate-pulse"
          >
            {{ pendingPostsCount }}
          </span>
        </button>

        <!-- Users Link -->
        <button 
          @click="switchMenu('users')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'users' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <Users class="w-4 h-4" />
            <span>Quản Lý Thành Viên</span>
          </span>
        </button>

        <!-- Payout Requests Link -->
        <button 
          @click="switchMenu('payouts')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'payouts' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <DollarSign class="w-4 h-4" />
            <span>Yêu Cầu Rút Tiền</span>
          </span>
          <span 
            v-if="pendingPayoutsCount > 0"
            class="text-[9px] bg-rose-600 text-white px-2 py-0.5 rounded-full font-black"
          >
            {{ pendingPayoutsCount }}
          </span>
        </button>

        <!-- Revenue Link -->
        <button 
          @click="switchMenu('revenue')"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-xl text-xs font-bold transition-all duration-200 border-none cursor-pointer',
            activeMenu === 'revenue' ? 'bg-red-700 text-white shadow-md' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
          ]"
        >
          <span class="flex items-center gap-2.5">
            <TrendingUp class="w-4 h-4" />
            <span>Báo Cáo Doanh Thu</span>
          </span>
        </button>
      </nav>

      <!-- Sidebar Footer Profile -->
      <div class="p-4 border-t border-slate-800 bg-slate-950 flex flex-col gap-2">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-slate-300">
            A
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-xs font-bold text-slate-100 truncate">Administrator</p>
            <p class="text-[9px] text-emerald-500 font-bold">ONLINE</p>
          </div>
        </div>
        <router-link 
          to="/" 
          class="text-center text-[10px] bg-slate-800 hover:bg-slate-750 text-slate-300 py-1.5 rounded-lg font-bold transition mt-2"
        >
          Quay lại trang tin tức
        </router-link>
      </div>
    </aside>

    <!-- MAIN RIGHT CONTENT CONTAINER -->
    <main class="ml-64 flex-1 min-h-screen p-8 lg:p-10">
      
      <!-- VIEW 1: OVERVIEW DASHBOARD -->
      <section v-if="activeMenu === 'dashboard'" class="space-y-8 animate-fade-in">
        <div>
          <h1 class="text-3xl font-black text-gray-800 uppercase tracking-tight">Hệ Thống Chỉ Huy</h1>
          <p class="text-gray-500 text-xs mt-0.5">Tổng hợp hiện trạng vận hành và các yêu cầu cấp bách chờ phê duyệt</p>
        </div>

        <!-- Metrics cards row -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6">
          <div class="bg-white p-5 rounded-2xl border border-gray-250/70 shadow-sm flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600 flex-shrink-0">
              <FolderOpen class="w-5 h-5" />
            </div>
            <div>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Tổng tin bài</p>
              <h3 class="text-xl font-black text-gray-800 mt-0.5">{{ articles.length }} bài</h3>
            </div>
          </div>
          <div class="bg-white p-5 rounded-2xl border border-gray-250/70 shadow-sm flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-600 flex-shrink-0">
              <FileText class="w-5 h-5" />
            </div>
            <div>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Bài viết chờ duyệt</p>
              <h3 class="text-xl font-black text-amber-600 mt-0.5">{{ pendingPostsCount }} bài</h3>
            </div>
          </div>
          <div class="bg-white p-5 rounded-2xl border border-gray-250/70 shadow-sm flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-600 flex-shrink-0">
              <Users class="w-5 h-5" />
            </div>
            <div>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Thành Viên VIP</p>
              <h3 class="text-xl font-black text-gray-800 mt-0.5">{{ vipCount }} người</h3>
            </div>
          </div>
          <div class="bg-white p-5 rounded-2xl border border-gray-250/70 shadow-sm flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-rose-50 flex items-center justify-center text-rose-600 flex-shrink-0">
              <DollarSign class="w-5 h-5" />
            </div>
            <div>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Đơn rút chờ duyệt</p>
              <h3 class="text-xl font-black text-rose-600 mt-0.5">{{ pendingPayoutsCount }} đơn</h3>
            </div>
          </div>
          <div class="bg-white p-5 rounded-2xl border border-gray-250/70 shadow-sm flex items-center gap-4 sm:col-span-2 lg:col-span-1">
            <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center text-purple-600 flex-shrink-0">
              <TrendingUp class="w-5 h-5" />
            </div>
            <div>
              <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Thặng dư quỹ thực</p>
              <h3 class="text-base font-black text-emerald-700 mt-0.5">{{ Number(netBalance).toLocaleString() }}đ</h3>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Pending Member Articles -->
          <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm flex flex-col">
            <h3 class="font-black text-gray-800 text-sm uppercase tracking-wide border-b pb-3 mb-4 flex justify-between items-center">
              <span>Bài viết chờ duyệt gần đây</span>
              <span class="text-[10px] bg-amber-50 text-amber-600 border border-amber-200 px-2 py-0.5 rounded font-black">
                {{ pendingPosts.length }} bài chờ duyệt
              </span>
            </h3>
            <div v-if="pendingPosts.length === 0" class="text-center py-12 text-gray-400 text-xs italic">
              Tuyệt vời! Không có bài viết nào đang chờ duyệt.
            </div>
            <div v-else class="space-y-3 max-h-[300px] overflow-y-auto pr-1">
              <div v-for="post in pendingPosts.slice(0, 5)" :key="post.id" class="border-b last:border-b-0 pb-3 flex justify-between items-center gap-3">
                <div class="min-w-0 flex-1">
                  <p class="text-xs font-bold text-gray-800 truncate" :title="post.title">{{ post.title }}</p>
                  <p class="text-[10px] text-gray-500 mt-0.5">Người gửi: <span class="text-blue-700 font-bold">@{{ post.source }}</span> • {{ post.pubDate }}</p>
                </div>
                <router-link 
                  :to="`/admin/user-posts/${post.id}`"
                  class="bg-orange-500 hover:bg-orange-600 text-white font-bold text-[10px] px-3 py-1.5 rounded transition shadow-sm text-center"
                >
                  Duyệt bài
                </router-link>
              </div>
            </div>
          </div>

          <!-- Pending Payout Requests -->
          <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm flex flex-col">
            <h3 class="font-black text-gray-800 text-sm uppercase tracking-wide border-b pb-3 mb-4 flex justify-between items-center">
              <span>Yêu cầu rút tiền chờ duyệt</span>
              <span class="text-[10px] bg-rose-50 text-rose-600 border border-rose-200 px-2 py-0.5 rounded font-black">
                {{ pendingPayouts.length }} đơn chờ duyệt
              </span>
            </h3>
            <div v-if="pendingPayouts.length === 0" class="text-center py-12 text-gray-400 text-xs italic">
              Không có yêu cầu rút tiền nào đang chờ duyệt.
            </div>
            <div v-else class="space-y-3 max-h-[300px] overflow-y-auto pr-1">
              <div v-for="req in pendingPayouts.slice(0, 5)" :key="req.id" class="border-b last:border-b-0 pb-3 flex justify-between items-center gap-3">
                <div class="min-w-0 flex-1">
                  <p class="text-xs font-bold text-gray-800">
                    @{{ req.username }} rút <span class="text-amber-500">{{ req.points }}đ</span> → <span class="text-red-700 font-extrabold">{{ Number(req.amountMoney).toLocaleString() }}đ</span>
                  </p>
                  <p class="text-[10px] text-gray-400 mt-0.5">Kênh: {{ req.payoutMethod }} • {{ req.payoutInfo }}</p>
                </div>
                <div class="flex gap-1.5">
                  <button 
                    @click="handleApprovePayout(req.id)"
                    class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[9px] px-2 py-1 rounded shadow-sm transition border-none cursor-pointer"
                  >
                    Duyệt
                  </button>
                  <button 
                    @click="handleRejectPayout(req.id)"
                    class="bg-red-650 hover:bg-red-700 text-white font-bold text-[9px] px-2 py-1 rounded shadow-sm transition border-none cursor-pointer"
                  >
                    Từ chối
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- VIEW 2: QUẢN LÝ TIN TỨC HỆ THỐNG -->
      <section v-else-if="activeMenu === 'news'" class="space-y-6 animate-fade-in">
        <div class="flex justify-between items-center">
          <div>
            <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">Quản Lý Tin Tức Hệ Thống</h1>
            <p class="text-gray-500 text-xs">Thêm bài viết mới, sửa đổi thông tin hoặc xóa bài viết hệ thống</p>
          </div>
          <router-link 
            to="/admin/create"
            class="bg-green-600 hover:bg-green-700 text-white font-bold text-xs px-4 py-2.5 rounded-lg shadow-md transition flex items-center gap-1.5"
          >
            <Plus class="w-4 h-4 text-white" />
            <span>Đăng bài viết mới</span>
          </router-link>
        </div>

        <div class="bg-white p-4 rounded-xl border border-gray-250 shadow-sm flex flex-col sm:flex-row gap-4 items-center justify-between">
          <div class="w-full sm:w-80 relative">
            <Search class="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input 
              type="text" 
              v-model="searchTerm" 
              placeholder="Tìm tên bài viết hệ thống..."
              class="w-full pl-9 pr-4 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
            />
          </div>
          <select 
            v-model="selectedCategory" 
            class="w-full sm:w-48 px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs bg-white"
          >
            <option value="">Tất cả danh mục</option>
            <option v-for="cat in categories" :key="cat.slug" :value="cat.slug">
              {{ cat.name }}
            </option>
          </select>
        </div>

        <div v-if="loading" class="text-center py-20 text-gray-500">Đang tải danh sách bài báo...</div>
        <div v-else class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
          <table class="w-full text-left text-xs">
            <thead class="bg-gray-100 uppercase text-[9px] font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">ID</th>
                <th class="p-4">Tiêu đề bài viết</th>
                <th class="p-4">Danh mục</th>
                <th class="p-4 text-center">Lượt xem</th>
                <th class="p-4 text-center">Hành động</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="a in filteredArticles" :key="a.id" class="hover:bg-slate-50 transition">
                <td class="p-4 font-mono">#{{ a.id }}</td>
                <td class="p-4">
                  <div class="max-w-md truncate font-extrabold text-gray-800" :title="a.title">{{ a.title }}</div>
                  <div class="text-[9px] text-gray-400 mt-0.5">Nguồn: {{ a.source }} • {{ formatDate(a.pubDate) }}</div>
                </td>
                <td class="p-4 text-gray-500 capitalize">{{ a.category }}</td>
                <td class="p-4 text-center text-gray-800 font-bold">{{ a.views }}</td>
                <td class="p-4">
                  <div class="flex justify-center gap-2">
                    <router-link 
                      :to="`/admin/edit/${a.id}`" 
                      class="bg-blue-600 hover:bg-blue-700 text-white font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm"
                    >
                      Sửa
                    </router-link>
                    <button 
                      @click="deleteArticle(a.id)" 
                      class="bg-red-600 hover:bg-red-700 text-white font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm border-none cursor-pointer"
                    >
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredArticles.length === 0">
                <td colspan="5" class="p-8 text-center text-gray-500 italic bg-gray-50">Không tìm thấy bài viết nào khớp từ khóa.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW 3: DUYỆT BÀI VIẾT THÀNH VIÊN (MEMBER ARTICLES) -->
      <section v-else-if="activeMenu === 'member-posts'" class="space-y-6 animate-fade-in">
        <div>
          <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">Duyệt Bài Viết Thành Viên</h1>
          <p class="text-gray-500 text-xs">Xem xét nội dung bài thảo luận/quan điểm được độc giả gửi về và quyết định phê duyệt xuất bản</p>
        </div>

        <div class="bg-white p-4 rounded-xl border border-gray-250 shadow-sm flex items-center justify-between">
          <div class="w-full sm:w-80 relative">
            <Search class="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input 
              type="text" 
              v-model="searchTerm" 
              placeholder="Lọc tiêu đề bài viết thành viên..."
              class="w-full pl-9 pr-4 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
            />
          </div>
          <span class="text-xs text-gray-500 font-bold">Bài đang chờ duyệt: <span class="text-amber-500">{{ pendingPosts.length }}</span></span>
        </div>

        <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
          <table class="w-full text-left text-xs">
            <thead class="bg-gray-100 uppercase text-[9px] font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">ID</th>
                <th class="p-4">Tiêu đề bài viết</th>
                <th class="p-4">Tác giả (Thành viên)</th>
                <th class="p-4">Ngày gửi</th>
                <th class="p-4 text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="post in filteredPendingPosts" :key="post.id" class="hover:bg-slate-50 transition">
                <td class="p-4 font-mono">#{{ post.id }}</td>
                <td class="p-4 text-gray-800 font-extrabold max-w-sm truncate">{{ post.title }}</td>
                <td class="p-4 text-purple-700 font-black">@{{ post.source }}</td>
                <td class="p-4 text-gray-400 font-mono">{{ post.pubDate }}</td>
                <td class="p-4 text-center">
                  <router-link 
                    :to="`/admin/user-posts/${post.id}`"
                    class="bg-orange-500 hover:bg-orange-600 text-white font-bold text-[9px] px-3.5 py-1.5 rounded transition shadow-sm inline-block"
                  >
                    XEM & PHÊ DUYỆT
                  </router-link>
                </td>
              </tr>
              <tr v-if="filteredPendingPosts.length === 0">
                <td colspan="5" class="p-8 text-center text-gray-400 italic bg-gray-50">Không có bài viết thành viên nào chờ duyệt.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW 4: QUẢN LÝ THÀNH VIÊN (USERS DIRECTORY) -->
      <section v-else-if="activeMenu === 'users'" class="space-y-6 animate-fade-in">
        <div>
          <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">Quản Lý Thành Viên & Phân Quyền</h1>
          <p class="text-gray-500 text-xs">Cấp quyền hội viên (MEMBER), theo dõi số dư nhuận bút và lịch sử hoạt động</p>
        </div>

        <div class="bg-white p-4 rounded-xl border border-gray-250 shadow-sm flex items-center justify-between">
          <div class="w-full sm:w-80 relative">
            <Search class="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input 
              type="text" 
              v-model="userSearchTerm" 
              placeholder="Tìm tên đăng nhập hoặc email..."
              class="w-full pl-9 pr-4 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
            />
          </div>
        </div>

        <div v-if="usersLoading" class="text-center py-20 text-gray-500">Đang tải danh sách tài khoản...</div>
        <div v-else class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
          <table class="w-full text-left text-xs">
            <thead class="bg-gray-100 uppercase text-[9px] font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">ID</th>
                <th class="p-4">Tên đăng nhập</th>
                <th class="p-4">Địa chỉ Email</th>
                <th class="p-4 text-center">Nhuận bút (Điểm)</th>
                <th class="p-4 text-center">Vai trò tài khoản</th>
                <th class="p-4 text-center">Hành động</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-slate-50 transition">
                <td class="p-4 font-mono">#{{ user.id }}</td>
                <td class="p-4 text-gray-900 font-extrabold">@{{ user.username }}</td>
                <td class="p-4 text-gray-500">{{ user.email }}</td>
                <td class="p-4 text-center text-amber-600 font-black">{{ Number(user.points || 0).toFixed(1) }} điểm</td>
                <td class="p-4 text-center">
                  <span 
                    :class="[
                      'text-[9px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider',
                      user.role === 'ADMIN' ? 'bg-red-50 text-red-650' : user.role === 'MEMBER' ? 'bg-purple-50 text-purple-600' : 'bg-blue-50 text-blue-650'
                    ]"
                  >
                    {{ user.role }}
                  </span>
                </td>
                <td class="p-4">
                  <div class="flex justify-center gap-2">
                    <button 
                      v-if="user.role === 'USER'"
                      @click="updateUserRole(user.id, 'MEMBER')"
                      class="bg-purple-600 hover:bg-purple-700 text-white font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm border-none cursor-pointer"
                    >
                      Nâng cấp Member
                    </button>
                    <button 
                      v-else-if="user.role === 'MEMBER'"
                      @click="updateUserRole(user.id, 'USER')"
                      class="bg-white hover:bg-red-50 text-red-650 hover:text-red-705 border border-red-200 font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm cursor-pointer"
                    >
                      Thu hồi Member
                    </button>
                    <span v-else class="text-[10px] text-gray-400 italic">Bảo vệ quyền Admin</span>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredUsers.length === 0">
                <td colspan="6" class="p-8 text-center text-gray-400 italic bg-gray-50">Không tìm thấy tài khoản nào khớp từ khóa.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW 5: YÊU CẦU RÚT TIỀN (CASHOUT REQUESTS) -->
      <section v-else-if="activeMenu === 'payouts'" class="space-y-6 animate-fade-in">
        <div>
          <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">Duyệt Yêu Cầu Rút Tiền</h1>
          <p class="text-gray-500 text-xs">Xem xét và giải ngân nhuận bút mặt cho các nhà báo thành viên</p>
        </div>

        <div class="bg-white p-4 rounded-xl border border-gray-250 shadow-sm flex items-center justify-between">
          <div class="w-full sm:w-80 relative">
            <Search class="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input 
              type="text" 
              v-model="searchTerm" 
              placeholder="Lọc tài khoản rút tiền..."
              class="w-full pl-9 pr-4 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-xs"
            />
          </div>
          <span class="text-xs text-gray-500 font-bold">Chờ duyệt: <span class="text-rose-600 font-extrabold">{{ pendingPayoutsCount }} đơn</span></span>
        </div>

        <div v-if="payoutsLoading" class="text-center py-20 text-gray-500">Đang tải yêu cầu rút tiền...</div>
        <div v-else class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
          <table class="w-full text-left text-xs">
            <thead class="bg-gray-100 uppercase text-[9px] font-black text-gray-650 border-b">
              <tr>
                <th class="p-4">Mã rút</th>
                <th class="p-4">Thành viên</th>
                <th class="p-4">Số điểm rút</th>
                <th class="p-4">Số tiền quy đổi</th>
                <th class="p-4">Thông tin chuyển tiền</th>
                <th class="p-4 text-center">Trạng thái</th>
                <th class="p-4 text-center">Hành động</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="req in filteredPayoutRequests" :key="req.id" class="hover:bg-slate-50 transition">
                <td class="p-4 font-mono">#{{ req.id }}</td>
                <td class="p-4 text-gray-900 font-extrabold">@{{ req.username }}</td>
                <td class="p-4 text-amber-500 font-black">{{ req.points }} điểm</td>
                <td class="p-4 text-red-700 font-black">{{ Number(req.amountMoney).toLocaleString() }}đ</td>
                <td class="p-4 font-mono">
                  <span class="block font-black text-purple-700 text-[10px]">{{ req.payoutMethod }}</span>
                  <span class="text-[10px] text-gray-500">{{ req.payoutInfo }}</span>
                </td>
                <td class="p-4 text-center">
                  <span 
                    :class="[
                      'text-[9px] font-black px-2.5 py-1 rounded-full uppercase tracking-wider',
                      req.status === 1 ? 'bg-green-100 text-green-800' : req.status === 2 ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'
                    ]"
                  >
                    {{ req.status === 1 ? 'Đã duyệt' : req.status === 2 ? 'Bị từ chối' : 'Chờ duyệt' }}
                  </span>
                </td>
                <td class="p-4">
                  <div v-if="req.status === 0" class="flex justify-center gap-2">
                    <button 
                      @click="handleApprovePayout(req.id)"
                      class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm border-none cursor-pointer flex items-center gap-1"
                    >
                      <Check class="w-3.5 h-3.5" />
                      <span>Duyệt</span>
                    </button>
                    <button 
                      @click="handleRejectPayout(req.id)"
                      class="bg-red-650 hover:bg-red-700 text-white font-bold text-[9px] px-2.5 py-1.5 rounded transition shadow-sm border-none cursor-pointer flex items-center gap-1"
                    >
                      <X class="w-3.5 h-3.5" />
                      <span>Từ chối</span>
                    </button>
                  </div>
                  <div v-else class="text-center text-[10px] text-gray-400 font-bold italic">
                    Đã xử lý ({{ formatDateTime(req.resolvedAt) }})
                  </div>
                </td>
              </tr>
              <tr v-if="filteredPayoutRequests.length === 0">
                <td colspan="7" class="p-8 text-center text-gray-500 italic bg-gray-50">Không tìm thấy yêu cầu rút tiền nào khớp từ khóa.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- VIEW 6: BÁO CÁO DOANH THU & SVG CHARTS -->
      <section v-else-if="activeMenu === 'revenue'" class="space-y-6 animate-fade-in">
        <!-- Merged gorgeous custom SVG revenue dashboards! -->
        <div class="flex justify-between items-center">
          <div>
            <h1 class="text-2xl font-black text-gray-800 uppercase tracking-tight">Thống Kê Tài Chính</h1>
            <p class="text-gray-500 text-xs">Phân tích cơ cấu nguồn thu và so sánh dòng tiền nạp vào vs chi nhuận bút</p>
          </div>
          <button 
            @click="simulateExportCSV"
            class="bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs px-4 py-2.5 rounded-lg shadow-sm transition flex items-center gap-1.5 border-none cursor-pointer"
          >
            <Download class="w-4 h-4 text-white" />
            <span>Xuất báo cáo CSV</span>
          </button>
        </div>

        <!-- Metric widgets row -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="bg-gradient-to-br from-blue-600 to-indigo-700 text-white p-6 rounded-2xl shadow-sm">
            <h4 class="text-[10px] uppercase font-bold tracking-wider opacity-85">Doanh Thu Gói VIP</h4>
            <p class="text-2xl font-black mt-2">${{ Number(revenueStats.totalVipSales).toFixed(2) }} USD</p>
            <p class="text-[9px] opacity-75 mt-1">≈ {{ Number(revenueStats.totalVipSales * 25000).toLocaleString() }}đ</p>
          </div>
          <div class="bg-gradient-to-br from-amber-500 to-orange-600 text-white p-6 rounded-2xl shadow-sm">
            <h4 class="text-[10px] uppercase font-bold tracking-wider opacity-85">Ủng hộ từ độc giả</h4>
            <p class="text-2xl font-black mt-2">${{ Number(revenueStats.totalDonations).toFixed(2) }} USD</p>
            <p class="text-[9px] opacity-75 mt-1">≈ {{ Number(revenueStats.totalDonations * 25000).toLocaleString() }}đ</p>
          </div>
          <div class="bg-gradient-to-br from-rose-500 to-red-650 text-white p-6 rounded-2xl shadow-sm">
            <h4 class="text-[10px] uppercase font-bold tracking-wider opacity-85">Nhuận bút đã chi</h4>
            <p class="text-2xl font-black mt-2">{{ Number(revenueStats.totalPayoutMoney).toLocaleString() }}đ</p>
            <p class="text-[9px] opacity-75 mt-1">Hoàn tất {{ revenueStats.payoutCount }} yêu cầu</p>
          </div>
          <div class="bg-gradient-to-br from-emerald-500 to-teal-600 text-white p-6 rounded-2xl shadow-sm">
            <h4 class="text-[10px] uppercase font-bold tracking-wider opacity-85">Số dư quỹ vận hành</h4>
            <p class="text-2xl font-black mt-2">{{ Number(netBalance).toLocaleString() }}đ</p>
            <p class="text-[9px] opacity-75 mt-1">Thặng dư sau chi trả nhuận bút</p>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Chart 1: Donut -->
          <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm flex flex-col justify-between">
            <div>
              <h3 class="font-black text-gray-800 text-xs uppercase tracking-wider mb-1">Cơ cấu nguồn quỹ nạp</h3>
              <p class="text-gray-400 text-[10px]">Tỉ lệ đóng góp giữa bán VIP và quỹ ủng hộ</p>
            </div>
            <div class="flex items-center justify-center my-6">
              <svg class="w-36 h-36 transform -rotate-90" viewBox="0 0 42 42">
                <circle cx="21" cy="21" r="15.91549430918954" fill="transparent" stroke="#E5E7EB" stroke-width="4.5"></circle>
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
            <div class="space-y-1.5 text-[11px] font-semibold">
              <div class="flex justify-between">
                <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 bg-indigo-600 rounded-full"></span> VIP ({{ Number(vipPercentage).toFixed(0) }}%)</span>
                <span>${{ Number(revenueStats.totalVipSales).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 bg-amber-500 rounded-full"></span> Ủng hộ ({{ Number(donationPercentage).toFixed(0) }}%)</span>
                <span>${{ Number(revenueStats.totalDonations).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- Chart 2: Custom SVG monthly Bar chart -->
          <div class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm lg:col-span-2 flex flex-col justify-between">
            <div>
              <h3 class="font-black text-gray-800 text-xs uppercase tracking-wider mb-1">So sánh dòng tiền Thu - Chi theo tháng</h3>
              <p class="text-gray-400 text-[10px] mb-4">Biến động dòng tiền chảy vào so với tiền nhuận bút đã rút (VND)</p>
            </div>
            <div class="relative h-44 border-b border-l border-gray-200 pt-4">
              <div class="absolute inset-0 flex justify-between px-3 items-end">
                <div v-for="(item, idx) in monthlyChartData" :key="idx" class="flex flex-col items-center w-11 gap-1">
                  <div class="flex items-end justify-center gap-0.5 h-32 w-full">
                    <div 
                      class="bg-emerald-500 w-3.5 rounded-t hover:bg-emerald-600 transition-all"
                      :style="{ height: `${(item.inflow / maxMonthlyValue) * 100}%` }"
                    ></div>
                    <div 
                      class="bg-rose-500 w-3.5 rounded-t hover:bg-rose-600 transition-all"
                      :style="{ height: `${(item.outflow / maxMonthlyValue) * 100}%` }"
                    ></div>
                  </div>
                  <span class="text-[9px] font-bold text-gray-400 font-mono">{{ item.month }}</span>
                </div>
              </div>
            </div>
            <div class="flex justify-end gap-4 text-[9px] font-bold text-gray-400 mt-3">
              <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 bg-emerald-500 rounded-sm"></span> Thu</span>
              <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 bg-rose-500 rounded-sm"></span> Chi</span>
            </div>
          </div>
        </div>

        <!-- Ledger details list under tabs -->
        <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
          <div class="flex border-b bg-gray-50 p-2 gap-2">
            <button 
              @click="revenueTab = 'vip'" 
              :class="['px-3 py-1.5 text-[10px] font-bold uppercase rounded transition cursor-pointer', revenueTab === 'vip' ? 'bg-slate-900 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700']"
            >
              Lượt đăng ký VIP
            </button>
            <button 
              @click="revenueTab = 'donations'" 
              :class="['px-3 py-1.5 text-[10px] font-bold uppercase rounded transition cursor-pointer', revenueTab === 'donations' ? 'bg-slate-900 text-white shadow-sm' : 'text-gray-500 hover:text-gray-700']"
            >
              Quỹ ủng hộ
            </button>
          </div>
          <div class="p-3 border-b flex justify-between items-center gap-2">
            <div class="relative w-64">
              <Search class="w-3.5 h-3.5 text-gray-400 absolute left-2.5 top-2" />
              <input 
                type="text" 
                v-model="searchTerm" 
                placeholder="Tìm kiếm giao dịch tài chính..."
                class="w-full pl-8 pr-3 py-1 border rounded-lg focus:outline-none focus:ring-1 focus:ring-red-700 text-[10px]"
              />
            </div>
          </div>

          <!-- VIP list -->
          <table v-if="revenueTab === 'vip'" class="w-full text-left text-xs">
            <thead class="bg-gray-50 text-[9px] uppercase font-black text-gray-650 border-b">
              <tr>
                <th class="p-3">Mã GD</th>
                <th class="p-3">Thành viên mua</th>
                <th class="p-3">Số tiền</th>
                <th class="p-3">Thời gian</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="order in filteredVipOrders" :key="order.id" class="hover:bg-slate-50 transition">
                <td class="p-3 font-mono">#{{ order.id }}</td>
                <td class="p-3 font-bold">@{{ order.username }}</td>
                <td class="p-3 text-indigo-600 font-extrabold">${{ Number(order.amount).toFixed(2) }} USD</td>
                <td class="p-3 text-gray-400 font-mono">{{ formatDateTime(order.createdAt) }}</td>
              </tr>
            </tbody>
          </table>

          <!-- Donations list -->
          <table v-else class="w-full text-left text-xs">
            <thead class="bg-gray-50 text-[9px] uppercase font-black text-gray-650 border-b">
              <tr>
                <th class="p-3">Mã GD</th>
                <th class="p-3">Người ủng hộ</th>
                <th class="p-3">Người nhận</th>
                <th class="p-3 font-black">Số tiền</th>
                <th class="p-3">Thời gian</th>
              </tr>
            </thead>
            <tbody class="divide-y font-semibold text-gray-700">
              <tr v-for="dn in filteredDonations" :key="dn.id" class="hover:bg-slate-50 transition">
                <td class="p-3 font-mono">#{{ dn.id }}</td>
                <td class="p-3 font-bold">@{{ dn.donorName }}</td>
                <td class="p-3 text-purple-700">{{ dn.receiverName === 'Tòa soạn' ? 'Tòa soạn' : '@' + dn.receiverName }}</td>
                <td class="p-3 text-amber-600 font-extrabold">${{ Number(dn.amount).toFixed(2) }} USD</td>
                <td class="p-3 text-gray-400 font-mono">{{ formatDateTime(dn.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { toast } from '../utils/toast'
import { 
  LayoutDashboard, 
  FolderOpen, 
  FileText, 
  Users, 
  DollarSign, 
  TrendingUp, 
  Search, 
  Check, 
  X, 
  Plus, 
  Download, 
  Star, 
  Heart 
} from 'lucide-vue-next'

// Active panel selection
const activeMenu = ref('dashboard')
const loading = ref(true)

// Tab inner selection for ledger
const revenueTab = ref('vip')

// Common search parameters
const searchTerm = ref("")
const selectedCategory = ref("")
const userSearchTerm = ref("")

// Entities state
const articles = ref([])
const pendingPosts = ref([])
const users = ref([])
const allPayoutRequests = ref([])
const usersLoading = ref(true)
const payoutsLoading = ref(false)

const revenueStats = ref({
  totalVipSales: 0,
  totalDonations: 0,
  totalPayoutMoney: 0,
  vipCount: 0,
  donationCount: 0,
  payoutCount: 0
})

const revenueDetails = ref({
  vipOrders: [],
  donations: [],
  payouts: []
})

// Counts
const pendingPostsCount = computed(() => pendingPosts.value.length)
const pendingPayoutsCount = computed(() => allPayoutRequests.value.filter(r => r.status === 0).length)
const pendingPayouts = computed(() => allPayoutRequests.value.filter(r => r.status === 0))
const vipCount = computed(() => users.value.filter(u => u.role === 'MEMBER').length)

// Financial Calculations
const netBalance = computed(() => {
  const totalInflowVnd = (revenueStats.value.totalVipSales + revenueStats.value.totalDonations) * 25000
  return totalInflowVnd - revenueStats.value.totalPayoutMoney
})

const vipPercentage = computed(() => {
  const total = revenueStats.value.totalVipSales + revenueStats.value.totalDonations
  if (total === 0) return 50
  return (revenueStats.value.totalVipSales / total) * 100
})

const donationPercentage = computed(() => {
  const total = revenueStats.value.totalVipSales + revenueStats.value.totalDonations
  if (total === 0) return 50
  return (revenueStats.value.totalDonations / total) * 100
})

// Charts dataset mapping
const monthlyChartData = computed(() => {
  const now = new Date()
  const list = []
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    list.push({
      month: `T.${d.getMonth() + 1}`,
      monthInt: d.getMonth(),
      yearInt: d.getFullYear(),
      inflow: 0,
      outflow: 0
    })
  }

  revenueDetails.value.vipOrders.forEach(o => {
    const d = new Date(o.createdAt)
    const m = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (m) m.inflow += o.amount * 25000
  })

  revenueDetails.value.donations.forEach(dn => {
    const d = new Date(dn.createdAt)
    const m = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (m) m.inflow += dn.amount * 25000
  })

  revenueDetails.value.payouts.forEach(p => {
    const d = new Date(p.resolvedAt || p.createdAt)
    const m = list.find(l => l.monthInt === d.getMonth() && l.yearInt === d.getFullYear())
    if (m) m.outflow += p.amountMoney
  })

  return list
})

const maxMonthlyValue = computed(() => {
  let max = 1000000
  monthlyChartData.value.forEach(item => {
    if (item.inflow > max) max = item.inflow
    if (item.outflow > max) max = item.outflow
  })
  return max
})

// Side switching load trigger
const switchMenu = (menuName) => {
  activeMenu.value = menuName
  searchTerm.value = "" // clear filters
  if (menuName === 'revenue') {
    fetchRevenueStats()
    fetchRevenueDetails()
  } else if (menuName === 'payouts') {
    fetchAllPayoutRequests()
  } else if (menuName === 'member-posts') {
    fetchPendingPosts()
  } else if (menuName === 'news') {
    fetchArticles()
  } else if (menuName === 'users') {
    fetchUsers()
  }
}

// APIs
const fetchArticles = async () => {
  loading.value = true
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/news", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    const data = await res.json()
    articles.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const fetchPendingPosts = async () => {
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/news/pending", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    const data = await res.json()
    pendingPosts.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error(err)
  }
}

const fetchUsers = async () => {
  usersLoading.value = true
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/users", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    const data = await res.json()
    users.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error(err)
  } finally {
    usersLoading.value = false
  }
}

const fetchAllPayoutRequests = async () => {
  payoutsLoading.value = true
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/payout-requests", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    allPayoutRequests.value = await res.json()
  } catch (err) {
    console.error(err)
  } finally {
    payoutsLoading.value = false
  }
}

const fetchRevenueStats = async () => {
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/revenue", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    revenueStats.value = await res.json()
  } catch (err) {
    console.error(err)
  }
}

const fetchRevenueDetails = async () => {
  const token = localStorage.getItem("token")
  try {
    const res = await fetch("http://localhost:5000/api/admin/revenue/details", {
      headers: { "Authorization": `Bearer ${token}` }
    })
    revenueDetails.value = await res.json()
  } catch (err) {
    console.error(err)
  }
}

const handleApprovePayout = async (id) => {
  if (!confirm("Xác nhận phê duyệt và chuyển tiền nhuận bút cho yêu cầu này?")) return
  const token = localStorage.getItem("token")
  try {
    const res = await fetch(`http://localhost:5000/api/admin/payout-requests/${id}/approve`, {
      method: "POST",
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Duyệt thất bại.")
    toast.success("Đã duyệt yêu cầu rút tiền thành công!")
    fetchAllPayoutRequests()
    fetchRevenueStats()
  } catch (err) {
    toast.error(err.message)
  }
}

const handleRejectPayout = async (id) => {
  if (!confirm("Xác nhận từ chối yêu cầu và trả lại điểm cho thành viên?")) return
  const token = localStorage.getItem("token")
  try {
    const res = await fetch(`http://localhost:5000/api/admin/payout-requests/${id}/reject`, {
      method: "POST",
      headers: { "Authorization": `Bearer ${token}` }
    })
    if (!res.ok) throw new Error("Từ chối thất bại.")
    toast.success("Đã từ chối yêu cầu và hoàn lại điểm!")
    fetchAllPayoutRequests()
    fetchRevenueStats()
  } catch (err) {
    toast.error(err.message)
  }
}

const updateUserRole = (id, newRole) => {
  const act = newRole === 'MEMBER' ? 'nâng cấp hội viên (MEMBER)' : 'thu hồi về tài khoản thường (USER)'
  if (!confirm(`Bạn có chắc chắn muốn ${act}?`)) return

  const token = localStorage.getItem("token")
  fetch(`http://localhost:5000/api/admin/users/${id}/role`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({ role: newRole })
  })
    .then(res => res.json())
    .then(data => {
      toast.success(data.message)
      fetchUsers()
    })
    .catch(err => toast.error(err.message))
}

const deleteArticle = (id) => {
  if (!confirm("Bạn có chắc chắn muốn xóa bài viết này?")) return

  const token = localStorage.getItem("token")
  fetch(`http://localhost:5000/api/admin/news/${id}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` }
  })
    .then(res => res.json())
    .then(data => {
      toast.success(data.message)
      fetchArticles()
    })
    .catch(err => toast.error(err.message))
}

const simulateExportCSV = () => {
  toast.success("Báo cáo tài chính đã xuất thành công!")
}

// Filters lists
const filteredArticles = computed(() => {
  const q = searchTerm.value.toLowerCase().trim()
  const cat = selectedCategory.value
  return articles.value.filter(a => {
    return (!q || a.title.toLowerCase().includes(q)) &&
           (!cat || a.category === cat)
  })
})

const filteredPendingPosts = computed(() => {
  const q = searchTerm.value.toLowerCase().trim()
  return pendingPosts.value.filter(p => !q || p.title.toLowerCase().includes(q))
})

const filteredUsers = computed(() => {
  const q = userSearchTerm.value.toLowerCase().trim()
  return users.value.filter(u => {
    return !q || u.username.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
  })
})

const filteredPayoutRequests = computed(() => {
  const q = searchTerm.value.toLowerCase().trim()
  return allPayoutRequests.value.filter(p => {
    return !q || p.username.toLowerCase().includes(q) || p.payoutMethod.toLowerCase().includes(q)
  })
})

const filteredVipOrders = computed(() => {
  const q = searchTerm.value.toLowerCase().trim()
  return revenueDetails.value.vipOrders.filter(o => 
    !q || o.username.toLowerCase().includes(q) || o.description.toLowerCase().includes(q)
  )
})

const filteredDonations = computed(() => {
  const q = searchTerm.value.toLowerCase().trim()
  return revenueDetails.value.donations.filter(d => 
    !q || d.donorName.toLowerCase().includes(q) || d.receiverName.toLowerCase().includes(q)
  )
})

const formatDate = (dateStr) => {
  if (!dateStr) return "N/A"
  return dateStr.length > 25 ? dateStr.substring(0, 25) + "..." : dateStr
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('vi-VN')
}

const categories = [
  { slug: "tin-moi-nhat", name: "Trang chủ" },
  { slug: "thoi-su", name: "Thời sự" },
  { slug: "viec-lam", name: "Việc làm" },
  { slug: "phap-luat", name: "Pháp luật" },
  { slug: "bao-hiem", name: "Bảo hiểm" },
  { slug: "cong-doan", name: "Công đoàn" },
  { slug: "suc-khoe", name: "Sức khỏe" },
  { slug: "quandiem-tranhluan", name: "Quan điểm - Tranh luận" }
]

onMounted(() => {
  fetchArticles()
  fetchPendingPosts()
  fetchUsers()
  fetchAllPayoutRequests()
  fetchRevenueStats()
  fetchRevenueDetails()
})
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.2s ease-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
