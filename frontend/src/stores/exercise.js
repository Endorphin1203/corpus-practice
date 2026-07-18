import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const STORAGE_KEY = 'corpus_practice_session'

function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({
    questions: state.questions,
    currentIndex: state.currentIndex,
    sessionId: state.sessionId,
    mode: state.mode,
    savedAt: Date.now()
  }))
}

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    return JSON.parse(raw)
  } catch { return null }
}

function clearState() {
  localStorage.removeItem(STORAGE_KEY)
}

export const useExerciseStore = defineStore('exercise', () => {
  const questions = ref([])
  const currentIndex = ref(0)
  const sessionId = ref(null)
  const mode = ref('daily_review')
  const loading = ref(false)
  const elapsedSeconds = ref(0)  // 当前题已用时间

  async function generate(config) {
    loading.value = true
    try {
      const transRes = await api.generateQuestions({
        questionType: 'translation',
        mode: config.mode,
        subcategories: config.subcategories,
        count: config.translationCount || 5
      })
      questions.value = [...transRes.data]

      if (config.choiceCount > 0) {
        const choiceRes = await api.generateQuestions({
          questionType: 'choice',
          mode: config.mode,
          subcategories: config.subcategories,
          count: config.choiceCount
        })
        questions.value = [...questions.value, ...choiceRes.data]
      }

      if (config.writingCount > 0) {
        const writingRes = await api.generateQuestions({
          questionType: 'writing',
          mode: config.mode,
          subcategories: config.subcategories,
          count: config.writingCount
        })
        questions.value = [...questions.value, ...writingRes.data]
      }

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
      mode.value = config.mode

      // 持久化到 localStorage
      persist()
    } finally {
      loading.value = false
    }
  }

  function currentQuestion() {
    return questions.value[currentIndex.value] || null
  }

  function next() {
    currentIndex.value++
    elapsedSeconds.value = 0
    persist()
  }

  function isFinished() {
    return currentIndex.value >= questions.value.length
  }

  function reset() {
    questions.value = []
    currentIndex.value = 0
    sessionId.value = null
    elapsedSeconds.value = 0
    clearState()
  }

  function persist() {
    saveState({
      questions: questions.value,
      currentIndex: currentIndex.value,
      sessionId: sessionId.value,
      mode: mode.value,
      elapsedSeconds: elapsedSeconds.value
    })
  }

  /** 检查是否有未完成的练习，有则恢复并返回 true */
  function tryRestore() {
    const saved = loadState()
    if (!saved || !saved.sessionId || !saved.questions || saved.questions.length === 0) {
      return false
    }
    if (saved.currentIndex >= saved.questions.length) {
      clearState()
      return false
    }
    questions.value = saved.questions
    currentIndex.value = saved.currentIndex
    sessionId.value = saved.sessionId
    mode.value = saved.mode || 'daily_review'
    elapsedSeconds.value = saved.elapsedSeconds || 0
    return true
  }

  /** 仅检查是否有未完成练习，不恢复 */
  function hasUnfinished() {
    const saved = loadState()
    if (!saved || !saved.sessionId || !saved.questions?.length) return false
    return saved.currentIndex < saved.questions.length
  }

  return {
    questions, currentIndex, sessionId, mode, loading, elapsedSeconds,
    generate, currentQuestion, next, isFinished, reset,
    tryRestore, hasUnfinished
  }
})
