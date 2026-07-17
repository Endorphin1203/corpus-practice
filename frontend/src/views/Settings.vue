<template>
  <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px">
    <h2 style="margin-bottom: 30px">AI 后端配置</h2>

    <el-card v-for="p in providers" :key="p.id" style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <strong>{{ p.name }}</strong>
          <span style="margin-left: 12px; color: #909399">{{ p.modelName }}</span>
          <el-tag v-if="p.isActive" type="success" size="small" style="margin-left: 8px">启用</el-tag>
        </div>
        <div>
          <el-button size="small" @click="editProvider(p)">编辑</el-button>
          <el-button size="small" @click="testConnection(p.id)">测试</el-button>
          <el-button size="small" type="danger" @click="deleteProvider(p.id)">删除</el-button>
        </div>
      </div>
    </el-card>

    <el-button type="primary" @click="showAdd">添加 AI 后端</el-button>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '添加'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如 DeepSeek" /></el-form-item>
        <el-form-item label="API 地址"><el-input v-model="form.baseUrl" placeholder="https://api.deepseek.com/v1" /></el-form-item>
        <el-form-item label="API Key"><el-input v-model="form.apiKey" type="password" placeholder="sk-..." /></el-form-item>
        <el-form-item label="模型名"><el-input v-model="form.modelName" placeholder="deepseek-chat" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSettingsStore } from '../stores/settings'
import api from '../api'

const store = useSettingsStore()
const providers = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', baseUrl: '', apiKey: '', modelName: '', isActive: 1 })

onMounted(async () => {
  await store.fetchProviders()
  providers.value = store.providers
})

function showAdd() {
  Object.assign(form, { id: null, name: '', baseUrl: '', apiKey: '', modelName: '', isActive: 1 })
  dialogVisible.value = true
}

function editProvider(p) {
  Object.assign(form, { ...p, apiKey: '' })
  dialogVisible.value = true
}

async function save() {
  await store.saveProvider({ ...form })
  providers.value = store.providers
  dialogVisible.value = false
  ElMessage.success('保存成功')
}

async function deleteProvider(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await store.removeProvider(id)
  providers.value = store.providers
}

async function testConnection(id) {
  try {
    const res = await api.testProvider(id)
    ElMessage.success(res.data || '连接成功')
  } catch {
    ElMessage.error('连接失败')
  }
}
</script>
