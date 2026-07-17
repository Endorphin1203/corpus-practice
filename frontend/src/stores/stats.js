import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useStatsStore = defineStore('stats', () => {
  const overview = ref({})
  const trend = ref([])
  const byCategory = ref([])
  const weakCorpus = ref([])

  async function fetchOverview() {
    const res = await api.getStatsOverview()
    overview.value = res.data
  }

  async function fetchTrend() {
    const res = await api.getStatsTrend()
    trend.value = res.data
  }

  async function fetchByCategory() {
    const res = await api.getStatsByCategory()
    byCategory.value = res.data
  }

  async function fetchWeakCorpus() {
    const res = await api.getWeakCorpus()
    weakCorpus.value = res.data
  }

  async function fetchAll() {
    await Promise.all([fetchOverview(), fetchTrend(), fetchByCategory(), fetchWeakCorpus()])
  }

  return { overview, trend, byCategory, weakCorpus, fetchOverview, fetchTrend, fetchByCategory, fetchWeakCorpus, fetchAll }
})
