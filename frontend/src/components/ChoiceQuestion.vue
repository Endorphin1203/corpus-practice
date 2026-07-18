<template>
  <div class="question-card">
    <div class="question-header">
      <el-tag>{{ index + 1 }} / {{ total }}</el-tag>
      <el-tag type="warning">选择题</el-tag>
      <span class="timer">⏱ {{ elapsed }}s</span>
    </div>

    <div class="stem-text">{{ question.prompt }}</div>

    <el-radio-group v-model="selected" class="options-group">
      <div
        v-for="(opt, i) in question.options"
        :key="i"
        :class="['option-item', { selected: selected === opt }]"
        @click="selected = opt"
      >
        <el-radio :value="opt" size="large">
          <span class="option-label">{{ ['A', 'B', 'C', 'D'][i] }}.</span>
          {{ opt }}
        </el-radio>
      </div>
    </el-radio-group>

    <div style="margin-top: 20px; text-align: right">
      <el-button type="primary" @click="submit" :loading="submitting" :disabled="!selected">
        提交
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  question: Object,
  index: Number,
  total: Number,
  initialElapsed: { type: Number, default: 0 }
})

const emit = defineEmits(['submit', 'elapsed'])

const selected = ref('')
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
  if (!selected.value) return
  submitting.value = true
  clearInterval(timer)
  emit('submit', { answer: selected.value, duration: elapsed.value })
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
.stem-text {
  font-size: 18px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  word-break: break-word;
  overflow-wrap: break-word;
}
.options-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.option-item {
  padding: 14px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  word-break: break-word;
  overflow-wrap: break-word;
}
.option-item:hover { border-color: #409eff; background: #ecf5ff; }
.option-item.selected { border-color: #409eff; background: #ecf5ff; }
.option-label { font-weight: bold; margin-right: 8px; color: #409eff; }
</style>
