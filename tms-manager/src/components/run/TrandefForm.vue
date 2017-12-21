<template>
  <div>
    <el-form :model="tranDefForm"
             :rules="rules"
             label-suffix="："
             :label-width="formLabelWidth"
             ref="tranDefForm"
             style="text-align: left;padding-right: 5em;">
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
        <el-input v-model.number="tranDefForm.show_order" auto-complete="off" :readonly="formReadonly"></el-input>
      </el-form-item>
      <el-form-item label="交易类型">
        <el-radio v-model="tranDefForm.txn_type" label="0" :disabled="formReadonly">交易组</el-radio>
        <el-radio v-model="tranDefForm.txn_type" label="1" :disabled="formReadonly">交易</el-radio>
      </el-form-item>
      <el-form-item label="交易识别标识" prop="txnid" v-bind:class="{hidden:txnIdVisible}">
        <el-input v-model="tranDefForm.txnid" auto-complete="off" :readonly="formReadonly"></el-input>
      </el-form-item>
      <el-form-item label="处置策略:" :label-width="formLabelWidth" prop="tab_disposal">
        <AllPickSelect :dataList="tabDisposalList" @dataChange="addStatParamDataChange" :selectedList="selectedList" :disabled="formReadonly"></AllPickSelect>
      </el-form-item>
      <el-form-item label="渠道名称" prop="chann">
        <el-select v-model="tranDefForm.chann" placeholder="请选择" :disabled="formReadonly||channVisible">
          <el-option
            v-for="item in channelList"
            :key="item.channelid"
            :label="item.channelname"
            :value="item.channelid">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="上级交易" prop="txnffname">
        <el-input v-model="tranDefForm.txnffname" auto-complete="off" readonly="readonly"></el-input>
      </el-form-item>
      <el-form-item label="模型状态" prop="modelused" v-bind:class="{hidden:modelusedVisible}">
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
  import check from '@/common/check'

  export default {
    create () {
    },
    computed: {
      selectedList: function () { // 交易识别标识是否可见（组：不可见；交易：可见）
        var list = this.tranDefForm.tab_disposal.split(',')
        console.info(list)
        return list
      },
      txnIdVisible: function () { // 交易识别标识是否可见（组：不可见；交易：可见）
        return this.tranDefForm.txn_type !== '1'
      },
      channVisible: function () { // 渠道名称是否可选择(上级的渠道不为空，则不可以选择渠道)
        var parChann = this.tranDefForm.par_chann
        return !(parChann === null || parChann === '' || undefined === parChann)
      },
      modelusedVisible: function () { // 模型状态是否可见（添加:不可见，默认值为0；编辑：可见）
        return this.tranDefForm.op === 'add'
      },
      formReadonly () { // 表单只读控制
        return this.tranDefForm.tab_name === 'T' || (this.tranDefForm.tab_name === '' && this.tranDefForm.op === '')
      }
    },
    data () {
      var txnIdCheck = (rule, value, callback) => {
        var self = this
        if (value.trim() === '' && self.tranDefForm.txn_type === '1') {
          return callback(new Error('当交易类型为交易时,交易识别标识不能为空'))
        } else {
          callback()
        }
      }
      var channCheck = (rule, value, callback) => {
        var self = this
        if (value.trim() === '' && self.tranDefForm.txn_type === '1') {
          return callback(new Error('当交易类型为交易时,渠道不能为空'))
        } else {
          callback()
        }
      }
      return {
        txnIdParent: this.txnId,
        isVisibilityParent: this.isVisibility,
        tabDisposalList: [],
        tabDisposalSelectedCached: [],
        modelUsedList: [{label: '不使用模型', value: '0'}, {label: '模型学习期', value: '1'}],
        channelList: [{channelname: '全渠道', channelid: '0'}],
        tranDefForm: {
          parent_tab: '',
          tab_name: '',
          modelused_o: '',
          traindate: '',
          op: '', // 操作的类型：add：添加；mod：编辑
          tab_desc: '', // 名称
          show_order: '', // 顺序
          txn_type: '0', // 交易类型
          txnid: '', // 交易识别标识
          tab_disposal: '', // 处置策略
          chann: '', // 渠道名称
          par_chann: '', // 父渠道名称
          txnffname: '', // 上级交易
          modelused: '', // 模型状态
          is_enable: '1' // 有效性
        },
        formLabelWidth: '120px',
        rules: {
          txnid: [
            {max: 20, message: '交易识别标识不能超过20个字符', trigger: 'blur'},
            {validator: check.checkFormEnSpecialCharacter, trigger: 'blur'}, // 输入格式校验：包含字母,数字和下划线
            {validator: txnIdCheck, trigger: 'blur'} // 交易识别标识空校验;交易类型为交易的时候不允许为空
          ],
          chann: [
            {validator: channCheck, trigger: 'blur'} // 渠道空校验;交易类型为交易的时候不允许为空
          ],
          disposal: [
            {required: true, message: '请选择处置策略', trigger: 'blur'}
          ],
          tab_desc: [
            {required: true, message: '请输入名称', trigger: 'blur'},
            {max: 32, message: '交易识别标识不能超过32个字符', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'} // 输入格式校验：只能包含汉字,字母,数字和下划线
          ],
          show_order: [
            {required: true, message: '请输入顺序', trigger: 'blur'},
            {type: 'number', max: 9999, min: 0, message: '顺序必须为小于5位数的正整数', trigger: 'blur'}
          ]
        }
      }
    },
    methods: {
      addStatParamDataChange (value) {
        this.tranDefForm.tab_disposal = value.join(',')
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
