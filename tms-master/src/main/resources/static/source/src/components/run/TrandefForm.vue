<template>
  <div>
    <el-form :model="tranDefForm"
             :rules="rules"
             label-suffix="："
             :label-width="formLabelWidth"
             ref="tranDefForm"
             style="text-align: left;">
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
      <el-row>
        <el-col :span="12" class="form-item">
          <el-form-item label="名称" prop="tab_desc">
            <el-input v-model="tranDefForm.tab_desc" auto-complete="off" :readonly="formReadonly" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="form-item">
          <el-form-item label="顺序" prop="show_order">
            <el-input v-model="tranDefForm.show_order" auto-complete="off" :readonly="formReadonly" :maxlength="4" clearable></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12" class="form-item">
          <el-form-item label="交易类型">
            <el-radio v-model="tranDefForm.txn_type" label="0" :disabled="formReadonly">交易组</el-radio>
            <el-radio v-model="tranDefForm.txn_type" label="1" :disabled="formReadonly">交易</el-radio>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="form-item">
          <el-form-item label="交易识别标识" prop="txnid" v-bind:class="{hidden:txnIdVisible}">
            <el-input v-model="tranDefForm.txnid" auto-complete="off" :readonly="formReadonly" clearable></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12" class="form-item">
          <el-form-item label="处置策略" :label-width="formLabelWidth" prop="tab_disposal">
            <AllPickSelect :dataList="tabDisposalList" @dataChange="addStatParamDataChange" :selectedList="selectedList" :disabled="formReadonly" clearable></AllPickSelect>
          </el-form-item>
        </el-col>
        <el-col :span="12" class="form-item">
          <el-form-item label="渠道名称" prop="chann">
            <el-select v-model="tranDefForm.chann" placeholder="请选择" :disabled="formReadonly||channVisible" clearable>
              <el-option
                v-for="item in channelList"
                :key="item.channelid"
                :label="item.channelname"
                :value="item.channelid">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12"class="form-item">
          <el-form-item label="上级交易" prop="txnffname">
            <el-input v-model="tranDefForm.txnffname" auto-complete="off" readonly="readonly"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12"class="form-item">
          <el-form-item label="模型状态" prop="modelused" v-bind:class="{hidden:modelusedVisible}">
            <el-select v-model="tranDefForm.modelused" placeholder="请选择" :disabled="formReadonly" clearable>
              <el-option
                v-for="item in modelUsedList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12"class="form-item">
          <el-form-item label="有效性">
            <el-switch
              v-model="tranDefForm.is_enable"
              active-text="启用"
              inactive-text="停用"
              active-value="1"
              inactive-value="0"
              :disabled="formReadonly">
            </el-switch>
            <!--<el-radio v-model="tranDefForm.is_enable" label="1" :disabled="formReadonly">启用</el-radio>
            <el-radio v-model="tranDefForm.is_enable" label="0" :disabled="formReadonly">停用</el-radio>-->
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item>
            <el-button type="primary" @click="submitForm" size="large" :disabled="saveVisible">保存</el-button>
            <el-button v-if="addFlag" @click="closeAddDialog" size="large">取消</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
  import AllPickSelect from '@/components/common/AllPickSelect'
  import check from '@/common/check'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'

  export default {
    computed: {
      selectedList: function () { // 交易识别标识是否可见（组：不可见；交易：可见）
        let tabDisposal = this.tranDefForm.tab_disposal
        if (!tabDisposal || tabDisposal.trim() === '') {
          return []
        } else {
          return tabDisposal.split(',')
        }
      },
      saveVisible () {
        return this.readonly || this.editVisible
      },
      addFlag () {
        return this.tranDefForm.op === 'add'
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
        return this.readonly || this.tranDefForm.tab_name === 'T' || (this.tranDefForm.tab_name === '' && this.tranDefForm.op === '')
      }
    },
    data () {
      var txnIdCheck = (rule, value, callback) => {
        var self = this
        if (self.tranDefForm.txn_type === '1') {
          if (value.trim() === '') {
            return callback(new Error('当交易类型为交易时,交易识别标识不能为空'))
          } else if (!(/^[a-zA-Z][\w]*$/.test(value))) {
            return callback(new Error('请以字母开头，包括字母、数字和下划线'))
          }
        }
        callback()
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
            {max: 20, message: '长度不能超过20个字符', trigger: 'blur'},
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
            {max: 32, message: '长度不能超过32个字符', trigger: 'blur'},
            {validator: check.checkFormZhSpecialCharacter, trigger: 'blur'} // 输入格式校验：只能包含汉字,字母,数字和下划线
          ],
          show_order: [
            {required: true, message: '请输入顺序', trigger: 'blur'},
            {pattern: /^(0|[1-9]\d{0,3})$/, message: '顺序必须为小于5位数的正整数', trigger: 'blur'}
          ]
        }
      }
    },
    props: ['editVisible', 'readonly'],
    watch: {
      tranDefForm: {
        handler (curVal) {
          // 当交易类型选择为【交易组】的时候，清空交易识别标识的值
          if (curVal.txn_type === '0') {
            curVal.txnid = ''
          }
        },
        deep: true
      }
    },
    methods: {
      addStatParamDataChange (value) {
        this.tranDefForm.tab_disposal = value.join(',')
      },
      closeAddDialog () {
        this.$emit('listenToCloseDialog', false)
      },
      reloadPage (op) {
        this.$emit('listenToReloadPage', op)
      },
      resetForm () {
        this.$refs['tranDefForm'].resetFields()
      },
      submitForm () { // 提交表单；op：操作方式；
        var self = this
        var params = self.tranDefForm
        var op = params.op
        this.$refs['tranDefForm'].validate((valid) => {
          if (valid) {
            ajax.post({
              url: '/trandef/save',
              loading: true,
              param: params,
              success: function (data) {
                if (data.success) {
                  if (op === 'add') {
                    self.$message.success('创建成功')
                    self.closeAddDialog()
                  } else {
                    self.$message.success('编辑成功')
                  }
                  self.reloadPage(op)
                }
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          }
        })
      },
      getModelUsedList (val) {
        let self = this
        let list = dictCode.getCodeItems('tms.model.modelused')
        let modelUsed = val || self.tranDefForm.modelused
        if (modelUsed === null || modelUsed === '0') {
          list.pop()
        }
        self.modelUsedList = list
      }
    },
    components: {
      AllPickSelect
    }
  }
</script>

<style>
.form-item{
  padding-right: 20px;
}
</style>
