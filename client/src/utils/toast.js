import { ref } from 'vue'

export const toasts = ref([])

export const toast = {
  success(message, duration = 3000) {
    show(message, 'success', duration)
  },
  error(message, duration = 3000) {
    show(message, 'error', duration)
  },
  info(message, duration = 3000) {
    show(message, 'info', duration)
  }
}

function show(message, type, duration) {
  const id = Date.now()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, duration)
}
