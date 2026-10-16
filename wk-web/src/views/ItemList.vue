<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { createItem, deleteItem, fetchItems, updateItem } from '@/api/items'
import DataTable from '@/components/DataTable.vue'
import ItemFormModal from '@/components/ItemFormModal.vue'
import { formatTime } from '@/utils/format'

const route = useRoute()
// 当前分类由路由 meta 驱动，切换菜单时 watch 它重新加载（见文件末尾）
const category = computed(() => route.meta.category)

const list = ref([])
const loading = ref(false)
const error = ref('')
const mode = ref('')
const record = ref(null)
const saving = ref(false)
const keyword = ref('') // 搜索关键字，模糊匹配标题/内容

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 列配置：操作列固定 key: 'action'，由 DataTable 按 actions 配置渲染
// tags、priority、status、progress 的展示格式在 bodyCell 插槽里处理
const columns = [
  { title: '标题', dataIndex: 'title', width: 160, ellipsis: true },
  { title: '内容', dataIndex: 'content', width: 200, ellipsis: true },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '标签', dataIndex: 'tags', width: 160, ellipsis: true },
  { title: '负责人', dataIndex: 'owner', width: 100, ellipsis: true },
  { title: '联系邮箱', dataIndex: 'email', width: 180, ellipsis: true },
  { title: '进度', dataIndex: 'progress', width: 80 },
  { title: '备注', dataIndex: 'remark', width: 160, ellipsis: true },
  { title: '更新时间', dataIndex: 'updatedAt', width: 170 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

// 加载当前分类的分页列表；失败文案展示在表格上方 Alert
async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchItems({
      category: category.value,
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value.trim()
    })
    list.value = data.records
    pagination.total = data.total
    pagination.current = data.page
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// 操作列按钮：文案 + 回调可配置，confirm 带二次确认
const actions = [
  { label: '查看', onClick: (row) => openDetail(row, 'view') },
  { label: '编辑', onClick: (row) => openDetail(row, 'edit') },
  { label: '删除', danger: true, confirm: '确认删除这条记录？', onClick: (row) => remove(row.id) }
]

// 搜索/清空时都回到第 1 页重新查
function onSearch() {
  pagination.current = 1
  loadList()
}

function openCreate() {
  error.value = ''
  record.value = null
  mode.value = 'create'
}

// 弹窗所需字段列表行里已全有，直接用行数据填充，省一次详情请求
function openDetail(row, nextMode) {
  error.value = ''
  record.value = { ...row }
  mode.value = nextMode
}

function closePanel() {
  mode.value = ''
}

async function save(values) {
  saving.value = true
  error.value = ''
  try {
    if (mode.value === 'create') {
      await createItem({ ...values, category: category.value })
      pagination.current = 1 // 新增记录排在第 1 页顶部，跳回去让用户看到
      keyword.value = '' // 清空搜索，避免新记录被关键字过滤看不到
    } else {
      await updateItem(values.id, values)
    }
    mode.value = ''
    await loadList()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  error.value = ''
  try {
    await deleteItem(id)
    // 删空当前页后回退一页，避免停留在空页
    if (list.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadList()
  } catch (e) {
    error.value = e.message
  }
}

// 路由复用组件切换分类时：重置弹窗、搜索与分页状态，按新分类重新加载
watch(category, () => {
  mode.value = ''
  record.value = null
  error.value = ''
  keyword.value = ''
  pagination.current = 1
  loadList()
})

onMounted(loadList)
</script>

<template>
  <div class="panel list-panel">
    <!-- 搜索与操作按钮分两行排列，按钮 margin-top 控制行间距 -->
    <Flex vertical align="start">
      <InputSearch
        v-model:value="keyword"
        placeholder="搜索标题/内容"
        allow-clear
        style="width: 260px"
        @search="onSearch"
      />
      <Button type="primary" style="margin-top: 16px" @click="openCreate">新增</Button>
    </Flex>
    <Alert v-if="error && !mode" style="margin-top: 16px" type="error" :message="error" show-icon />
    <DataTable
      v-model:current="pagination.current"
      v-model:page-size="pagination.pageSize"
      :columns="columns"
      :data-source="list"
      :loading="loading"
      :total="pagination.total"
      :actions="actions"
      :scroll-x="1500"
      @change="loadList"
    >
      <template #bodyCell="{ column, record: row }">
        <template v-if="column.dataIndex === 'updatedAt'">
          {{ formatTime(row.updatedAt) }}
        </template>
        <!-- tags 是数组，顿号拼接展示；空数组显示 - -->
        <template v-else-if="column.dataIndex === 'tags'">
          {{ (row.tags || []).length ? row.tags.join('、') : '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'progress'">
          {{ row.progress ?? 0 }}%
        </template>
        <!-- 其余列：null/undefined/空字符串统一显示 - -->
        <template v-else>
          {{ row[column.dataIndex] === null || row[column.dataIndex] === undefined || row[column.dataIndex] === '' ? '-' : row[column.dataIndex] }}
        </template>
      </template>
    </DataTable>

    <ItemFormModal
      :mode="mode"
      :record="record"
      :saving="saving"
      :error="error"
      @close="closePanel"
      @submit="save"
    />
  </div>
</template>

<style scoped lang="scss">
/* 面板占满可视高度，纵向 flex 布局 */
.list-panel {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 136px); // 100vh - 头部64 - 页签栏40 - 内容区上下padding 32
  min-height: 0;

  /* 小屏适配：高度按缩小后的留白重新计算 */
  @media (max-width: 768px) {
    height: calc(100vh - 112px); // 100vh - 头部56 - 页签栏40 - 内容区上下padding 16
  }
}
</style>
