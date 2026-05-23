<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <ScrollToTop />
    <Header />
    <main class="flex-grow">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import ScrollToTop from './components/ScrollToTop.vue'
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
</script>
