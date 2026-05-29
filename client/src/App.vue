<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <ScrollToTop />
    <Header />
    <main class="flex-grow">
      <router-view />
    </main>
    <Footer />

    <!-- Container Toast thông báo tùy chỉnh -->
    <div class="fixed top-20 right-4 z-[9999] flex flex-col gap-3 max-w-sm w-full pointer-events-none">
      <TransitionGroup name="toast">
        <div 
          v-for="item in toasts" 
          :key="item.id"
          :class="[
            'pointer-events-auto p-4 rounded-xl shadow-2xl border flex items-center gap-3 transition-all duration-300 transform',
            item.type === 'error' ? 'bg-red-50 text-red-800 border-red-200' : 
            item.type === 'info' ? 'bg-blue-50 text-blue-800 border-blue-200' :
            'bg-green-50 text-green-800 border-green-200'
          ]"
        >
          <span class="text-lg flex-shrink-0">
            {{ item.type === 'error' ? '❌' : item.type === 'info' ? 'ℹ️' : '✅' }}
          </span>
          <div class="flex-grow text-xs md:text-sm font-bold leading-snug">
            {{ item.message }}
          </div>
          <button 
            @click="removeToast(item.id)" 
            class="text-gray-400 hover:text-gray-600 font-bold ml-2 text-xs"
          >
            ✕
          </button>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup>
import ScrollToTop from './components/ScrollToTop.vue'
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
import { toasts } from './utils/toast'

const removeToast = (id) => {
  toasts.value = toasts.value.filter(t => t.id !== id)
}
</script>

<style>
/* Hiệu ứng trượt và mờ cho Toast */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(50px) scale(0.9);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(50px) scale(0.9);
}
</style>
