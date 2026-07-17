import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useExerciseStore = defineStore('exercise', () => {
  const questions = ref([])
  const currentIndex = ref(0)
  const sessionId = ref(null)
  const mode = ref('daily_review')
  const loading = ref(false)

  async function generate(config) {
    loading.value = true
    try {
      // 生成翻译题
      const transRes = await api.generateQuestions({
        questionType: 'translation',
        subcategories: config.subcategories,
        count: config.translationCount || 5
      })

      questions.value = [...transRes.data]

      // 如果有选择题，也生成
      if (config.choiceCount > 0) {
        const choiceRes = await api.generateQuestions({
          questionType: 'choice',
          subcategories: config.subcategories,
          count: config.choiceCount
        })
        questions.value = [...questions.value, ...choiceRes.data]
      }

      // 创建会话
      const sessionRes = await api.startSession({
        mode: config.mode,
        questionType: [
          config.translationCount > 0 ? 'translation' : '',
          config.choiceCount > 0 ? 'choice' : '',
          config.writingCount > 0 ? 'writing' : ''
        ].filter(Boolean).join(','),
        categoriesFilter: JSON.stringify(config.subcategories)
      })
      sessionId.value = sessionRes.data.id
    } finally {
      loading.value = false
    }
  }

  function currentQuestion() {
    return questions.value[currentIndex.value] || null
  }

  function next() {
    currentIndex.value++
  }

  function isFinished() {
    return currentIndex.value >= questions.value.length
  }

  function reset() {
    questions.value = []
    currentIndex.value = 0
    sessionId.value = null
  }

  return { questions, currentIndex, sessionId, mode, loading, generate, currentQuestion, next, isFinished, reset }
})
