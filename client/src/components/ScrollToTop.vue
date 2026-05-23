<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
watch(
  () => route.path,
  () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
)
</script>

<template>
  <!-- Trống, chỉ dùng để xử lý tác vụ cuộn -->
</template>
