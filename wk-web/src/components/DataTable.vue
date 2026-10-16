<script setup>
// 通用数据表格：内置操作列与底部分页，其余列的自定义渲染通过 bodyCell 插槽透传
defineProps({
  columns: { type: Array, required: true }, // antd Table 列配置，操作列约定 key: 'action'
  dataSource: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  current: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  rowKey: { type: String, default: 'id' },
  scrollX: { type: Number, default: 720 },
  // 操作列按钮配置：{ label 文案, danger 红色, confirm 有值则带二次确认, onClick(row) 回调 }
  actions: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:current', 'update:pageSize', 'change'])

// 分页变化：同步 v-model 后通知使用方重新加载
function onPageChange(page, size) {
  emit('update:current', page)
  emit('update:pageSize', size)
  emit('change', page, size)
}
</script>

<template>
  <div class="table-wrap">
    <Table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: scrollX }"
      :row-key="rowKey"
    >
      <template #bodyCell="scope">
        <!-- 操作列：按 actions 配置渲染按钮 -->
        <template v-if="scope.column.key === 'action'">
          <template v-for="(a, i) in actions" :key="i">
            <Popconfirm v-if="a.confirm" :title="a.confirm" @confirm="a.onClick(scope.record)">
              <Button type="link" size="small" :danger="a.danger">{{ a.label }}</Button>
            </Popconfirm>
            <Button v-else type="link" size="small" :danger="a.danger" @click="a.onClick(scope.record)">
              {{ a.label }}
            </Button>
          </template>
        </template>
        <!-- 其他列透传给使用方的 bodyCell 插槽 -->
        <slot v-else name="bodyCell" v-bind="scope" />
      </template>
    </Table>
  </div>
  <div class="pager">
    <Pagination
      :current="current"
      :page-size="pageSize"
      :total="total"
      show-size-changer
      :page-size-options="['10', '20', '50']"
      @change="onPageChange"
    />
  </div>
</template>

<style scoped lang="scss">
/* 表格区域占满剩余空间，内容过多时内部滚动 */
.table-wrap {
  flex: 1;
  min-height: 0;
  margin-top: 16px;
  overflow: auto;
}

/* 分页固定在面板底部 */
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;

  // 小屏允许换行
  @media (max-width: 768px) {
    flex-wrap: wrap;
  }
}
</style>
