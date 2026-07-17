<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>语料管理</h2>
      <div style="display: flex; gap: 12px">
        <el-button type="primary" @click="$router.push('/corpus/import')">导入 Excel</el-button>
        <el-button type="success" @click="showAddDialog">添加语料</el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-select v-model="filter.category" placeholder="大类" clearable @change="fetchData">
          <el-option label="描写续写" value="描写续写" />
          <el-option label="议论文" value="议论文" />
        </el-select>
      </el-col>
      <el-col :span="6">
        <el-select v-model="filter.subcategory" placeholder="小类" clearable @change="fetchData">
          <el-option v-for="sub in allSubs" :key="sub" :label="sub" :value="sub" />
        </el-select>
      </el-col>
      <el-col :span="8">
        <el-input v-model="filter.keyword" placeholder="搜索中文/英文..." clearable @clear="fetchData" @keyup.enter="fetchData">
          <template #append><el-button @click="fetchData">搜索</el-button></template>
        </el-input>
      </el-col>
    </el-row>

    <el-table :data="tableData" stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="category" label="大类" width="100" />
      <el-table-column prop="subcategory" label="小类" width="100" />
      <el-table-column prop="chinese" label="中文" show-overflow-tooltip />
      <el-table-column prop="english" label="英文" show-overflow-tooltip />
      <el-table-column prop="notes" label="备注" width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="editCorpus(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteCorpus(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next, total" @current-change="fetchData"
      style="margin-top: 20px; justify-content: center"
    />

    <div v-if="selectedIds.length > 0" style="margin-top: 12px">
      <el-button type="danger" @click="batchDelete">批量删除（{{ selectedIds.length }}）</el-button>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingCorpus.id ? '编辑语料' : '添加语料'" width="600px">
      <el-form :model="editingCorpus" label-width="80px">
        <el-form-item label="大类">
          <el-select v-model="editingCorpus.category">
            <el-option label="描写续写" value="描写续写" />
            <el-option label="议论文" value="议论文" />
          </el-select>
        </el-form-item>
        <el-form-item label="小类">
          <el-select v-model="editingCorpus.subcategory">
            <el-option v-for="sub in allSubs" :key="sub" :label="sub" :value="sub" />
          </el-select>
        </el-form-item>
        <el-form-item label="中文">
          <el-input v-model="editingCorpus.chinese" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="英文">
          <el-input v-model="editingCorpus.english" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editingCorpus.notes" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCorpus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const allSubs = ['优秀表达','动作','情绪','环境','外貌','好句段落','开头','主体','观点库','高级表达','主题词']

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const editingCorpus = reactive({ category: '描写续写', subcategory: '优秀表达', chinese: '', english: '', notes: '' })

const filter = reactive({ category: '', subcategory: '', keyword: '' })

async function fetchData() {
  loading.value = true
  const res = await api.getCorpus({ ...filter, page: page.value, size: size.value })
  tableData.value = res.data.records
  total.value = res.data.total
  loading.value = false
}

function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function showAddDialog() {
  Object.assign(editingCorpus, { id: null, category: '描写续写', subcategory: '优秀表达', chinese: '', english: '', notes: '' })
  dialogVisible.value = true
}

function editCorpus(row) {
  Object.assign(editingCorpus, { ...row })
  dialogVisible.value = true
}

async function saveCorpus() {
  if (editingCorpus.id) {
    await api.updateCorpus(editingCorpus.id, editingCorpus)
  } else {
    await api.createCorpus(editingCorpus)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

async function deleteCorpus(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await api.deleteCorpus(id)
  ElMessage.success('已删除')
  fetchData()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条？`, '提示', { type: 'warning' })
  await api.batchDeleteCorpus(selectedIds.value)
  ElMessage.success('已删除')
  selectedIds.value = []
  fetchData()
}

onMounted(fetchData)
</script>
