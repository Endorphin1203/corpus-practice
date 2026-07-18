<template>
  <div style="max-width: 700px; margin: 0 auto; padding: 40px 20px">
    <h2 style="margin-bottom: 30px">练习配置</h2>

    <el-form label-width="100px">
      <el-form-item label="练习模式">
        <el-radio-group v-model="config.mode">
          <el-radio-button value="daily_review">日常积累</el-radio-button>
          <el-radio-button value="exam_cram">考前突击</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="选择分类">
        <el-checkbox-group v-model="config.subcategories">
          <el-checkbox v-for="sub in allSubcategories" :key="sub" :label="sub" :value="sub" />
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="翻译题数量">
        <el-input-number v-model="config.translationCount" :min="0" :max="30" />
      </el-form-item>

      <el-form-item label="选择题数量">
        <el-input-number v-model="config.choiceCount" :min="0" :max="20" />
      </el-form-item>

      <el-form-item label="写作题数量">
        <el-input-number v-model="config.writingCount" :min="0" :max="5" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="large" @click="startExercise" :loading="store.loading"
          :disabled="config.subcategories.length === 0 || totalCount === 0">
          开始练习（共 {{ totalCount }} 题）
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useExerciseStore } from '../stores/exercise'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const store = useExerciseStore()

onMounted(async () => {
  if (store.hasUnfinished()) {
    try {
      await ElMessageBox.confirm(
        '检测到你有一个未完成的练习，是否继续上次的进度？',
        '恢复练习',
        { confirmButtonText: '继续练习', cancelButtonText: '重新开始', type: 'info' }
      )
      store.tryRestore()
      router.push(`/exercise/session/${store.sessionId}`)
    } catch {
      // 用户选择"重新开始"，清除旧状态
      store.reset()
    }
  }
})

const allSubcategories = [
  '优秀表达', '动作', '情绪', '环境', '外貌',
  '好句段落', '开头', '主体', '观点库', '高级表达', '主题词'
]

const config = reactive({
  mode: 'daily_review',
  subcategories: ['优秀表达', '情绪', '高级表达'],
  translationCount: 5,
  choiceCount: 3,
  writingCount: 1
})

const totalCount = computed(() =>
  config.translationCount + config.choiceCount + config.writingCount
)

async function startExercise() {
  await store.generate(config)
  router.push(`/exercise/session/${store.sessionId}`)
}
</script>
