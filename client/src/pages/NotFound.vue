<script>

</script>

<template>
  <main class="min-h-[80vh] flex flex-col items-center justify-center px-4 py-12 bg-gray-50">
    <div class="text-center">
      <h1 class="text-9xl font-black text-gray-200 mb-4">404</h1>
      <h2 class="text-3xl font-bold text-gray-800 mb-2">Trang không tồn tại</h2>
      <p class="text-gray-500 mb-8 max-w-md mx-auto">
        Rất tiếc, liên kết bạn vừa truy cập có thể đã bị hỏng, bài viết đã bị xóa, hoặc trang này không tồn tại.
      </p>

      <router-link
          to="/"
          class="bg-red-700 text-white px-8 py-3 rounded-lg hover:bg-red-800 font-bold shadow-lg transition duration-300 inline-flex items-center gap-2"
      >
        <span>🏠</span> Quay về Trang Chủ
      </router-link>
    </div>
  </main>
</template>

<style scoped>

</style>