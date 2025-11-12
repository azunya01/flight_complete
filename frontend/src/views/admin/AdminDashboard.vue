<template>
  <el-row :gutter="12">
    <el-col :sm="12" :lg="8">
      <el-card shadow="hover">
        <template #header>快速入口</template>
        <div class="quick">
          <el-button @click="$router.push('/admin/airline/list')">新增航线</el-button>
          <el-button @click="$router.push('/admin/flight/list')">航班列表</el-button>
          <el-button @click="$router.push('/admin/flight/list')">航班管理</el-button>
        </div>
      </el-card>
    </el-col>

    <el-col :sm="12" :lg="8">
      <el-card shadow="hover">
        <template #header>系统概览</template>
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="用户">管理员</el-descriptions-item>
          <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
          <el-descriptions-item label="状态">运行中</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>

    <el-col :sm="12" :lg="8">
      <el-card shadow="hover">
        <template #header>公告 / 提示</template>
        <el-empty description="暂无公告" />
      </el-card>
    </el-col>
  </el-row>
</template>

<style scoped>
.quick { display: flex; flex-wrap: wrap; gap: 8px; }
</style>
