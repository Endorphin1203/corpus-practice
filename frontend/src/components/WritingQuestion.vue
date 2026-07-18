<template>
  <div class="question-card">
    <div class="question-header">
      <el-tag>{{ index + 1 }} / {{ total }}</el-tag>
      <el-tag type="danger">写作题</el-tag>
      <span class="timer">⏱ {{ elapsed }}s</span>
    </div>

    <div class="scene-text">{{ question.sceneDescription }}</div>

    <div v-if="question.requiredCorpusIds && question.requiredCorpusIds.length > 0" class="corpus-hint">
      <el-icon><InfoFilled /></el-icon>
      请尽量运用已学语料完成写作
    </div>

    <el-input
      v-model="answer"
      type="textarea"
      :rows="10"
      placeholder="请在此写作..."
    />

    <div style="margin-top: 16px; text-align: right">
      <el-button type="primary" @click="submit" :loading="submitting" :disabled="!answer.trim()">
        提交（Ctrl+Enter）
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  question: Object,
  index: Number,
  total: Number,
  initialElapsed: { type: Number, default: 0 }
})

const emit = defineEmits(['submit', 'elapsed'])

const answer = ref('')
const submitting = ref(false)
const elapsed = ref(props.initialElapsed)
let timer = null

onMounted(() => {
  timer = setInterval(() => { elapsed.value++; emit('elapsed', elapsed.value) }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

async function submit() {
  if (!answer.value.trim()) return
  submitting.value = true
  clearInterval(timer)
  emit('submit', { answer: answer.value, duration: elapsed.value })
}
</script>

<style scoped>
.question-card {
  max-width: 700px;
  margin: 60px auto;
  padding: 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.question-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}
.timer { margin-left: auto; color: #909399; font-size: 14px; }
.scene-text {
  font-size: 17px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}
.corpus-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  margin-bottom: 16px;
  background: #fdf6ec;
  color: #e6a23c;
  border-radius: 6px;
  font-size: 14px;
}
</style>
