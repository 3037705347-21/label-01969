import { ref } from 'vue'

const listeners = ref({})

export const eventBus = {
  emit(event, data) {
    if (listeners.value[event]) {
      listeners.value[event].forEach(callback => callback(data))
    }
  },
  
  on(event, callback) {
    if (!listeners.value[event]) {
      listeners.value[event] = []
    }
    listeners.value[event].push(callback)
  },
  
  off(event, callback) {
    if (listeners.value[event]) {
      if (callback) {
        listeners.value[event] = listeners.value[event].filter(cb => cb !== callback)
      } else {
        delete listeners.value[event]
      }
    }
  }
}
