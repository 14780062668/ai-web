<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  mode: { type: String, default: '' }, // '' 关闭，create / edit / view
  record: { type: Object, default: null }, // 编辑、查看时的初始数据
  saving: { type: Boolean, default: false },
  error: { type: String, default: '' }
})
const emit = defineEmits(['close', 'submit'])

// 空表单默认值：复选是数组，数值给 0，其余空串
const emptyForm = () => ({
  id: null,
  title: '',
  content: '',
  priority: '中',
  status: '进行中',
  tags: [],
  owner: '',
  email: '',
  progress: 0,
  remark: ''
})

const form = ref(emptyForm())

// 弹窗打开时用初始数据填充表单，新增则重置为空
watch(
  () => props.mode,
  (val) => {
    if (val) {
      // 合并默认值，防止老数据缺字段导致复选绑定到 undefined
      form.value = props.record ? { ...emptyForm(), ...props.record } : emptyForm()
    }
  }
)

const open = computed(() => !!props.mode)
const title = computed(() => ({ create: '新增', edit: '编辑', view: '查看' })[props.mode] || '')
const isView = computed(() => props.mode === 'view')

// 单选、复选选项
const priorityOptions = ['低', '中', '高']
const statusOptions = ['待处理', '进行中', '已完成']
const tagOptions = ['重要', '紧急', '待办', '归档']
</script>

<template>
  <Modal :open="open" :title="title" :footer="null" @cancel="emit('close')">
    <Alert v-if="error" style="margin-bottom: 16px" type="error" :message="error" show-icon />
    <Form layout="vertical">
      <FormItem label="标题">
        <Input v-model:value="form.title" :disabled="isView" :maxlength="100" placeholder="请输入标题" />
      </FormItem>
      <FormItem label="内容">
        <Textarea v-model:value="form.content" :disabled="isView" :maxlength="2000" :rows="4" placeholder="请输入内容" />
      </FormItem>
      <FormItem label="优先级">
        <RadioGroup v-model:value="form.priority" :disabled="isView" :options="priorityOptions" />
      </FormItem>
      <FormItem label="状态">
        <RadioGroup v-model:value="form.status" :disabled="isView" :options="statusOptions" />
      </FormItem>
      <FormItem label="标签">
        <CheckboxGroup v-model:value="form.tags" :disabled="isView" :options="tagOptions" />
      </FormItem>
      <FormItem label="负责人">
        <Input v-model:value="form.owner" :disabled="isView" :maxlength="50" placeholder="请输入负责人" />
      </FormItem>
      <FormItem label="联系邮箱">
        <Input v-model:value="form.email" :disabled="isView" :maxlength="100" placeholder="请输入联系邮箱" />
      </FormItem>
      <FormItem label="进度（%）">
        <InputNumber v-model:value="form.progress" :disabled="isView" :min="0" :max="100" style="width: 100%" />
      </FormItem>
      <FormItem label="备注">
        <Textarea v-model:value="form.remark" :disabled="isView" :maxlength="500" :rows="2" placeholder="请输入备注" />
      </FormItem>
    </Form>
    <Flex justify="flex-end" gap="small">
      <Button @click="emit('close')">关闭</Button>
      <!-- 提交时浅拷贝一份表单，避免外部继续持有引用被改动 -->
      <Button v-if="!isView" type="primary" :loading="saving" @click="emit('submit', { ...form })">保存</Button>
    </Flex>
  </Modal>
</template>
