<template>
  <div class="form-action-buttons">
    <v-btn
      type="submit"
      :text="t('forms.save')"
      color="primary"
      @click.prevent="save"
      :loading="isSaving"
    />
    <v-btn
      :text="t('forms.cancel')"
      @click="cancel"
      :disabled="isSaving"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
  useScope: 'global'
})

defineProps<{
  isSaving?: boolean
}>()

const emit = defineEmits(['save', 'cancel'])

function save() {
  emit('save')
}

function cancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.form-action-buttons {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}
</style>
