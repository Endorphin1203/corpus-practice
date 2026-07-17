<template>
  <el-card>
    <template #header>🔴 薄弱语料 Top 10</template>
    <el-table :data="data" stripe>
      <el-table-column prop="chinese" label="中文" show-overflow-tooltip />
      <el-table-column prop="english" label="英文" show-overflow-tooltip />
      <el-table-column prop="subcategory" label="分类" width="100" />
      <el-table-column prop="error_count" label="错误次数" width="100" sortable />
    </el-table>
  </el-card>
</template>

<script setup>
defineProps({ data: Array })
</script>
