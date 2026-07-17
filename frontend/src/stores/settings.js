import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useSettingsStore = defineStore('settings', () => {
  const providers = ref([])
  const activeProviderId = ref(null)

  async function fetchProviders() {
    const res = await api.getProviders()
    providers.value = res.data
    if (!activeProviderId.value && providers.value.length > 0) {
      const active = providers.value.find(p => p.isActive)
      if (active) activeProviderId.value = active.id
    }
  }

  async function saveProvider(provider) {
    if (provider.id) {
      await api.updateProvider(provider.id, provider)
    } else {
      await api.createProvider(provider)
    }
    await fetchProviders()
  }

  async function removeProvider(id) {
    await api.deleteProvider(id)
    await fetchProviders()
  }

  return { providers, activeProviderId, fetchProviders, saveProvider, removeProvider }
})
