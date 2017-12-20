<template>
  <div>
    <el-form :model="tranDefForm"
             :rules="rules"
             label-suffix="："
             :label-width="formLabelWidth"
             ref="tranDefForm"
             style="text-align: left;padding-right: 5em;overflow-y: auto;height: 430px;">
      <el-form-item label="" prop="parent_tab" class="hidden">
        <el-input v-model="tranDefForm.parent_tab" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="tab_name" class="hidden">
        <el-input v-model="tranDefForm.tab_name" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="modelused_o" class="hidden">
        <el-input v-model="tranDefForm.modelused_o" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="traindate" class="hidden">
        <el-input v-model="tranDefForm.traindate" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="op" class="hidden">
        <el-input v-model="tranDefForm.op" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="名称" prop="tab_desc">
        <el-input v-model="tranDefForm.tab_desc" auto-complete="off" :readonly="formReadonly"></el-input>
      </el-form-item>
      <el-form-item label="顺序" prop="show_order">
        <el-input v-model="tranDefForm.show_order" auto-complete="off" :readonly="formReadonly"></el-input>
      </el-form-item>
      <el-form-item label="交易类型">
        <el-radio v-model="tranDefForm.txn_type" label="0" :disabled="formReadonly">交易组</el-radio>
        <el-radio v-model="tranDefForm.txn_type" label="1" :disabled="formReadonly">交易</el-radio>
      </el-form-item>
      <el-form-item label="交易识别标识" prop="tab_desc" v-bind:class="{hidden:txnIdVisible}">
        <el-input v-model="tranDefForm.txnid" auto-complete="off" :readonly="formReadonly"></el-input>
      </el-form-item>
      <el-form-item label="处置策略:" :label-width="formLabelWidth" prop="tab_disposal">
        <AllPickSelect :dataList="tabDisposalList" @dataChange="addStatParamDataChange" :disabled="formReadonly"></AllPickSelect>
      </el-form-item>
      <el-form-item label="渠道名称" prop="chann">
        <el-select v-model="tranDefForm.chann" placeholder="请选择" :disabled="formReadonly">
          <el-option
            v-for="item in channelList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="上级交易" prop="txnffname">
        <el-input v-model="tranDefForm.txnffname" auto-complete="off" readonly="readonly"></el-input>
      </el-form-item>
      <el-form-item label="模型状态" prop="modelused">
        <el-select v-model="tranDefForm.modelused" placeholder="请选择" :disabled="formReadonly">
          <el-option
            v-for="item in modelUsedList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="有效性">
        <el-radio v-model="tranDefForm.is_enable" label="1" :disabled="formReadonly">启用</el-radio>
        <el-radio v-model="tranDefForm.is_enable" label="0" :disabled="formReadonly">停用</el-radio>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
  import AllPickSelect from '@/components/common/AllPickSelect'

  export default {
    create () {
    },
    computed: {
      txnIdVisible: function () { // 交易识别标识是否可见（组：不可见；交易：可见）
        return this.tranDefForm.txn_type !== '1'
      },
      formReadonly: function () { // 变淡只读控制
        return this.tranDefForm.tab_name === 'T'
      }
    },
    data () {
      return {
        txnIdParent: this.txnId,
        isVisibilityParent: this.isVisibility,
        tabDisposalList: [],
        tabDisposalSelectedCached: [],
        modelUsedList: [{label: '不使用模型', value: '0'}, {label: '模型学习期', value: '1'}],
        channelList: [{label: '全渠道', value: '0'}],
        tranDefForm: {
          parent_tab: '',
          tab_name: '',
          modelused_o: '',
          traindate: '',
          op: '', // 操作的类型：add：添加；mod：编辑
          tab_desc: '', // 名称
          show_order: '', // 顺序
          txn_type: '', // 交易类型
          txnid: '', // 交易识别标识
          tab_disposal: '', // 处置策略
          chann: '', // 渠道名称
          txnffname: '', // 上级交易
          modelused: '', // 模型状态
          is_enable: '1' // 有效性
        },
        formLabelWidth: '120px',
        rules: {
          a: ''
        }
      }
    },
    methods: {
      addStatParamDataChange (value) {
        this.dialogForm.stat_param = value
      }
    },
    components: {
      AllPickSelect
    }
  }
</script>

<style>

</style>
·
