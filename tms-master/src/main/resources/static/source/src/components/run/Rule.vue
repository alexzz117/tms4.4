<template>
  <div>

    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow" >
        <el-form-item label="规则名称:" prop="stat_name">
          <el-input v-model="queryShowForm.rule_shortdesc" class="query-form-item" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="评估类型:" prop="eval_type">
          <el-select v-model="queryShowForm.eval_type" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in evalTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>

        </el-form-item>
        <el-form-item label="处置方式:" prop="disposal">
          <el-select v-model="queryShowForm.disposal" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in disposalList"
              :key="item.dp_code"
              :label="item.dp_name"
              :value="item.dp_code">
            </el-option>

          </el-select>
        </el-form-item>
        <el-form-item label="有效性:" prop="rule_enable">
          <el-select v-model="queryShowForm.rule_enable" class="query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in ruleEnableList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>
      </el-form>

    </transition>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-view" type="primary" @click="queryFormShow = !queryFormShow">查询</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
    </div>
    <el-table
      ref="dataTable"
      :data="tableDataShow"
      stripe
      border
      style="width: 100%">

      <el-table-column
        fixed="left"
        label="操作"
        width="250">
        <template slot-scope="scope">

          <el-button type="text" size="small" @click="openDialog('edit', scope.row)">编辑</el-button>
          <el-button type="text" size="small" @click="copy(scope.row)">复制</el-button>
          <el-button type="text" size="small" @click="delData(scope.row)">删除</el-button>
          <el-button type="text" size="small" @click="openRefsDialog(scope.row)">引用点</el-button>
          <el-button type="text" size="small" @click="subList(scope.row)">动作列表</el-button>

        </template>
      </el-table-column>
      <el-table-column prop="rule_shortdesc" label="规则名称" align="left" ></el-table-column>
      <el-table-column label="评估类型" align="left" width="80">
        <template slot-scope="scope">
          <span>{{scope.row.eval_type | evalTypeFilter}}</span>
        </template>
      </el-table-column>
      <el-table-column label="处置方式" align="left" width="80">
        <template slot-scope="scope">
          <span>{{scope.row.disposal | disposalFilter}}</span>
        </template>
      </el-table-column>

      <el-table-column prop="action_count" label="动作数量" align="left" width="80"></el-table-column>

      <el-table-column label="是否测试" align="left" width="80">
        <template slot-scope="scope">
          <span>{{scope.row.rule_istest | ruleIsTestFilter}}</span>
        </template>
      </el-table-column>

      <el-table-column label="有效性" align="left" width="80">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.rule_enable"
            active-color="#13ce66"
            inactive-color="#ff4949"
            :active-value=1
            :inactive-value=0
            @change="ruleEnableChange(scope.row)">
          </el-switch>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px">
      <el-form :model="dialogForm" ref="dialogForm" style="text-align: left" :inline="true" :rules="dialogFormRules">
        <el-form-item label="规则名称:" :label-width="formLabelWidth" prop="rule_shortdesc" :style="formItemStyle">
          <el-input v-model="dialogForm.rule_shortdesc" auto-complete="off" :style="formItemContentStyle" :maxlength="128"></el-input>
        </el-form-item>

        <el-form-item label="规则条件:" class="is-required" :label-width="formLabelWidth" prop="rule_cond_in" :style="formItemStyle">
          <div @dblclick="ruleCondInPopup">
            <el-input v-model="dialogForm.rule_cond_in" auto-complete="off" :style="formItemContentStyle" readonly></el-input>
            <el-input v-model="dialogForm.rule_cond" v-show="false" auto-complete="off" :style="formItemContentStyle" readonly></el-input>
          </div>
        </el-form-item>

        <el-form-item label="评估类型:" :label-width="formLabelWidth" prop="eval_type" :style="formItemStyle">
          <el-select v-model="dialogForm.eval_type" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in evalTypeList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="处置方式:" :label-width="formLabelWidth" prop="disposal" :style="formItemStyle">
          <el-select v-model="dialogForm.disposal" placeholder="请选择" :style="formItemContentStyle"
                     clearable>
            <el-option
              v-for="item in disposalList"
              :key="item.dp_code"
              :label="item.dp_name"
              :value="item.dp_code">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="分值:" :label-width="formLabelWidth" prop="rule_score" :style="formItemStyle">
          <el-input v-model="dialogForm.rule_score" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>

        <el-form-item label="是否测试:" class="is-required" :label-width="formLabelWidth" prop="rule_istest" :style="formItemStyle">
          <el-switch
            v-model="dialogForm.rule_istest"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-value="1"
            inactive-value="0"
          >
          </el-switch>
        </el-form-item>

        <el-form-item label="有效性:" class="is-required" :label-width="formLabelWidth" prop="rule_enable" :style="formItemStyle" >
          <el-switch
            v-model="dialogForm.rule_enable"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-value="1"
            inactive-value="0"
          >
          </el-switch>
        </el-form-item>
        <div>
          <el-form-item label="规则描述:" :label-width="formLabelWidth" prop="rule_desc">
            <el-input type="textarea" v-model="dialogForm.rule_desc" :style="textareaContentStyle"  :maxlength="1024"></el-input>
          </el-form-item>
        </div>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('dialogForm')" :disabled="dialogFormSureBtnDisabled">保 存</el-button>
      </div>
    </el-dialog>

    <StatCondPicker ref="RuleCondDialog" @valueCallback="ruleCondInValueCallBack"
                    :statCond="dialogForm.rule_cond" :statCondIn="dialogForm.rule_cond_in" :txnId="txnIdParent"
                    :hideItems="['rule_func']" >

    </StatCondPicker>
  </div>
</template>

<script>
  import dictCode from '@/common/dictCode'
  import util from '@/common/util'
  import ajax from '@/common/ajax'

  import StatCondPicker from '@/components/common/StatCondPicker'
  import check from '@/common/check'

  let vm = null

  export default {
    data () {
      return {
        modelName: '',
        tableData: [],
        allStoreFd: [],
        enableStoreFd: [],
        dialogTitle: '',
        dialogType: '',
        dialogVisible: false,
        formLabelWidth: '120px',
        formItemStyle: {
          width: '400px'
        },
        formItemContentStyle: {
          width: '250px'
        },
        textareaContentStyle: {
          width: '665px'
        },
        queryFormShow: false,
        queryShowForm: {
          rule_shortdesc: '',
          eval_type: '',
          disposal: '',
          rule_enable: ''
        },
        dialogForm: this.initDialogForm(),
        dialogFormSureBtnDisabled: false,
        dialogFormRules: {
          rule_shortdesc: [
            { required: true, message: '请输入规则名称', trigger: 'blur' },
            { max: 128, message: '长度在128个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacter, trigger: 'blur' }
          ],
          rule_cond: [
            { required: true, message: '请输入规则条件', trigger: 'blur' },
            { max: 1024, message: '长度在1024个字符以内', trigger: 'blur' }
          ],
          eval_type: [
            { required: true, message: '请输入评估类型', trigger: 'change' }
          ],
          disposal: [
            { required: true, message: '请输入处置方式', trigger: 'change' }
          ],
          rule_desc: [
            { max: 1024, message: '长度在1024个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacter, trigger: 'blur' }
          ],
          rule_score: [
            { required: true, message: '请输入规则分值', trigger: 'blur' },
            { validator: this.checkRuleScore, trigger: 'blur' }
          ]

        },
        selectedRows: [],
        disposalList: [],
        evalTypeList: [],
        ruleEnableList: []
      }
    },
    computed: {
      txnIdParent () {
        return this.txnId
      },
      isVisibilityParent () { return this.isVisibility },
      // tableDataShow 用于表格数据的前台检索
      tableDataShow () {
        let ruleShortDesc = this.queryShowForm.rule_shortdesc
        let evalType = this.queryShowForm.eval_type
        let disposal = this.queryShowForm.disposal
        let ruleEnable = this.queryShowForm.rule_enable

        let showData = this.tableData.filter((x) => {
          if (ruleShortDesc !== '' && !x.rule_shortdesc.includes(ruleShortDesc)) {
            return false
          }
          if (evalType != null && evalType !== '' && x.eval_type !== evalType) {
            return false
          }
          if (disposal != null && disposal !== '' && x.disposal !== disposal) {
            return false
          }
          // 有效性  下面这个!= 不能改成!==
          if (ruleEnable != null && ruleEnable !== '' && x.rule_enable != ruleEnable) {
            return false
          }
          return true
        })
        return showData
      }
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      evalTypeFilter: function (value) {
        return dictCode.rendCode('tms.rule.evaltype', value)
      },
      ruleIsTestFilter (value) {
        return dictCode.rendCode('common.is', value)
      },
      disposalFilter: function (value) {
        var returnText = value
        for (let loop of vm.disposalList) {
          if (value !== '' && value === loop.dp_code) {
            returnText = loop.dp_name
            break
          }
        }
        return returnText
      }
    },
    props: ['txnId', 'isVisibility'],
    mounted: function () {
      this.$nextTick(function () {
        vm = this
        // this.reloadData()
      })
    },
    watch: {
      txnId: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.reloadData()
          }
        }
      },
      'dialogForm.disposal': {
        handler: (val, oldVal) => {
          for (let loop of vm.disposalList) {
            if (val === '') {
              vm.dialogForm.rule_score = ''
              break
            }
            if (val === loop.dp_code) {
              vm.dialogForm.rule_score = loop.default_score
              break
            }
          }
        }
      }
    },
    methods: {
      // 重建一个新的表单对象
      initDialogForm () {
        return {
          rule_shortdesc: '',
          rule_cond_in: '',
          rule_cond: '',
          eval_type: '',
          disposal: '',
          rule_score: '',
          rule_istest: '0',
          rule_enable: '0',
          rule_desc: ''
        }
      },
      getData () {
        let self = this
        // TODO  type st_id 怎么取值 原来系统看是 undefined
        ajax.post({
          url: '/rule/list',
          param: {
            RULE_TXN: this.txnIdParent,
            type: 0,
            st_id: ''
          },
          success: function (data) {
            self.tableData = data.row
          }
        })
      },
      reloadData () {
        let self = this
        this.evalTypeList = dictCode.getCodeItems('tms.rule.evaltype')
        this.ruleEnableList = dictCode.getCodeItems('tms.mgr.rulestatus')
        ajax.post({
          url: '/rule/disposal',
          param: {
            txn_id: this.txnIdParent
          },
          success: function (data) {
            self.disposalList = data.row
            self.getData()
          }
        })
      },
      openDialog (dialogType, row) {
        this.dialogType = dialogType
        let self = this
        if (dialogType === 'edit') {
          this.allPickCollapse = true // 编辑时多选为隐藏多的标签
          this.dialogTitle = '编辑交易规则'
          this.modDisabled = true
          // 拷贝而不是赋值
          Object.assign(this.dialogForm, this.selectRowNum2Str(row))
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建交易规则'
          this.modDisabled = false
          this.dialogForm = this.initDialogForm()
        }
        this.dialogVisible = true
        // this.dialogOpenHandle()
        setTimeout(function () {
          if (self.$refs['dialogForm']) {
            self.$refs['dialogForm'].clearValidate()
          }
        }, 100)
      },
      copy () {

      },
      delData () {

      },
      openRefsDialog () {

      },
      subList () {

      },
      ruleEnableChange () {

      },
      // 提交表单，新增，编辑都在这
      submitForm (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.dialogFormSureBtnDisabled = true
            let self = this
            let submitParam = {}
            Object.assign(submitParam, this.dialogForm)
            submitParam.rule_txn = this.txnIdParent
            let jsonData = {}
            let message = '提交成功'
            if (this.dialogType === 'add') {
              jsonData.add = [submitParam]
              message = '新建成功'
            } else {
              jsonData.mod = [submitParam]
              message = '编辑成功'
            }
            let finalJsonData = {}
            finalJsonData.postData = jsonData
            finalJsonData.txnId = this.txnIdParent

            ajax.post({
              url: '/rule/save',
              param: finalJsonData,
              success: function (data) {
                self.getData()
                self.$message(message)
                self.dialogVisible = false
                self.dialogFormSureBtnDisabled = false
              },
              error: function (data) {
                if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                  alert(data.error.join('|'))
                } else {
                  alert(data.error)
                }
                self.dialogFormSureBtnDisabled = false
              },
              fail: function () {
                self.dialogFormSureBtnDisabled = false
              }
            })
          }
        })
      },
      ruleCondInPopup () {
        this.$refs.RuleCondDialog.open()
        this.$refs.RuleCondDialog.setValue({
          stat_cond_value: this.dialogForm.stat_cond,
          stat_cond_in: this.dialogForm.stat_cond_in
        })
      },
      // 下拉获取的码值时字符串，真实数据是数字，要转一下才好用
      selectRowNum2Str (row) {
        let tempObj = {}
        Object.assign(tempObj, row)
        for (let field in tempObj) {
          if (typeof (tempObj[field]) === 'number') {
            tempObj[field] = tempObj[field].toString()
          }
        }
        return tempObj
      },
      ruleCondInValueCallBack (value) {
        this.dialogForm.rule_cond = value.stat_cond_value
        this.dialogForm.rule_cond_in = value.stat_cond_in
      },
      checkRuleScore (rule, value, callback) {
        if (!util.isNumber(value, null, '0') || parseInt(value) < -100 || parseInt(value) > 100) {
          return callback(new Error(`周期只能输入-100-100的整数`))
        }
        return callback()
      }
    },
    components: {
      StatCondPicker
    }
  }
</script>

<style>

</style>
