<template>
  <div>
    <!--<transition name="fade">-->
    <!--<el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"-->
    <!--:inline="true" style="text-align: left" v-show="queryFormShow" >-->
    <!--<el-form-item label="规则名称:" prop="stat_name">-->
    <!--<el-input v-model="queryShowForm.rule_shortdesc" class="rule-query-form-item" auto-complete="off" clearable></el-input>-->
    <!--</el-form-item>-->

    <!--<el-form-item label="评估类型:" prop="eval_type">-->
    <!--<el-select v-model="queryShowForm.eval_type" class="rule-query-form-item" placeholder="请选择"-->
    <!--clearable>-->
    <!--<el-option-->
    <!--v-for="item in evalTypeList"-->
    <!--:key="item.value"-->
    <!--:label="item.label"-->
    <!--:value="item.value">-->
    <!--</el-option>-->

    <!--</el-select>-->

    <!--</el-form-item>-->
    <!--<el-form-item label="处置方式:" prop="disposal">-->
    <!--<el-select v-model="queryShowForm.disposal" class="rule-query-form-item" placeholder="请选择"-->
    <!--clearable>-->
    <!--<el-option-->
    <!--v-for="item in disposalList"-->
    <!--:key="item.dp_code"-->
    <!--:label="item.dp_name"-->
    <!--:value="item.dp_code">-->
    <!--</el-option>-->

    <!--</el-select>-->
    <!--</el-form-item>-->
    <!--<el-form-item label="有效性:" prop="rule_enable">-->
    <!--<el-select v-model="queryShowForm.rule_enable" class="rule-query-form-item" placeholder="请选择"-->
    <!--clearable>-->
    <!--<el-option-->
    <!--v-for="item in ruleEnableList"-->
    <!--:key="item.value"-->
    <!--:label="item.label"-->
    <!--:value="item.value">-->
    <!--</el-option>-->

    <!--</el-select>-->
    <!--</el-form-item>-->
    <!--</el-form>-->

    <!--</transition>-->

    <div style="margin-bottom: 10px;text-align: left ">

      <el-button plain class="el-icon-plus" @click="openDialog('add')" :disabled="readonlyParent">新建</el-button>
      <div style="float:right">
        <el-input v-model="queryShowForm.rule_shortdesc" placeholder="规则名称" class="rule-query-form-item" auto-complete="off" clearable>
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
        <!--<el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>-->
      </div>
    </div>
    <section class="section">
      <el-table
        ref="dataTable"
        :data="tableDataShow"
        style="width: 100%">

        <el-table-column
          label="操作"
          width="120">
          <template slot-scope="scope">

            <el-button v-if="readonlyParent" type="text" size="small" icon="el-icon-view" title="查看"  @click="openDialog('edit', scope.row)"></el-button>
            <el-button v-if="!readonlyParent" type="text" size="small" icon="el-icon-edit" title="编辑"  @click="openDialog('edit', scope.row)"></el-button>

            <el-button type="text" size="small" icon="el-icon-document" title="复制" @click="copy(scope.row)" :disabled="readonlyParent"></el-button>
            <el-button type="text" size="small" icon="el-icon-delete" title="删除" @click="delData(scope.row)" :disabled="readonlyParent"></el-button>
            <!--<el-button type="text" size="small" icon="el-icon-location-outline" title="引用点" @click="openRefsDialog(scope.row)"></el-button>-->

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
              :active-value=1
              :inactive-value=0
              :disabled="ruleEnableBtnDisabled || readonlyParent"
              @change="ruleEnableChange(scope.row)">
            </el-switch>
          </template>
        </el-table-column>
      </el-table>
    </section>
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px" :close-on-click-modal="false">

      <el-tabs ref="dialogTab" v-model="dialogTabActive" type="card">
        <el-tab-pane label="数据编辑" name="dataMod">
          <el-form :model="dialogForm" ref="dialogForm" style="text-align: left" :inline="true" :rules="dialogFormRules">
            <el-form-item label="规则名称:" :label-width="formLabelWidth" prop="rule_shortdesc" :style="formItemStyle">
              <el-input v-model="dialogForm.rule_shortdesc" auto-complete="off" :style="formItemContentStyle" :maxlength="128" :disabled="readonlyParent"></el-input>
            </el-form-item>

            <el-form-item label="规则条件:" class="is-required" :label-width="formLabelWidth" prop="rule_cond_in" :style="formItemStyle">
              <div @dblclick="ruleCondInPopup">
                <el-input v-model="dialogForm.rule_cond_in" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
                <el-input v-model="dialogForm.rule_cond" v-show="false" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
              </div>
            </el-form-item>

            <el-form-item label="评估类型:" :label-width="formLabelWidth" prop="eval_type" :style="formItemStyle">
              <el-select v-model="dialogForm.eval_type" placeholder="请选择" :style="formItemContentStyle" :disabled="readonlyParent"
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
              <el-select v-model="dialogForm.disposal" placeholder="请选择" :style="formItemContentStyle" :disabled="readonlyParent"
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
              <el-input v-model="dialogForm.rule_score" auto-complete="off" :style="formItemContentStyle" :disabled="readonlyParent"></el-input>
            </el-form-item>

            <el-form-item label="是否测试:" :label-width="formLabelWidth" prop="rule_istest" :style="formItemStyle">
              <el-switch
                :disabled="readonlyParent"
                v-model="dialogForm.rule_istest"
                active-value="1"
                inactive-value="0"
              >
              </el-switch>
            </el-form-item>

            <el-form-item label="有效性:" :label-width="formLabelWidth" prop="rule_enable" :style="formItemStyle" >
              <el-switch
                :disabled="readonlyParent"
                v-model="dialogForm.rule_enable"
                active-value="1"
                inactive-value="0"
              >
              </el-switch>
            </el-form-item>
            <div>
              <el-form-item label="规则描述:" :label-width="formLabelWidth" prop="rule_desc">
                <el-input type="textarea" v-model="dialogForm.rule_desc" :style="textareaContentStyle"  :maxlength="1024" :disabled="readonlyParent"></el-input>
              </el-form-item>
            </div>

            <div>
              <el-form-item  label=" " :label-width="formLabelWidth" :style="formItemStyle">
                <el-button v-if="!readonlyParent" type="primary" @click="submitForm('dialogForm')" :disabled="dialogFormSureBtnDisabled" size="large">保 存</el-button>
                <el-button @click="dialogVisible = false" size="large">取 消</el-button>
              </el-form-item>
            </div>


          </el-form>
        </el-tab-pane>
        <el-tab-pane label="动作列表" name="actionList" v-if="dialogType === 'edit'">
          <div style="margin-bottom: 10px;text-align: left ">
            <el-button plain class="el-icon-plus" @click="openActionDialog('add')" :disabled="readonlyParent">新建</el-button>
          </div>

          <el-table
            ref="actionDataTable"
            :data="actionTableDataShow"
            style="width: 100%">

            <el-table-column
              label="操作"
              width="150">
              <template slot-scope="scope">

                <el-button v-if="readonlyParent" type="text" size="small" icon="el-icon-view" title="查看"  @click="openActionDialog('edit', scope.row)"></el-button>
                <el-button v-if="!readonlyParent" type="text" size="small" icon="el-icon-edit" title="编辑"  @click="openActionDialog('edit', scope.row)"></el-button>

                <el-button type="text" size="small" icon="el-icon-document" title="复制" @click="copyAction(scope.row)" :disabled="readonlyParent"></el-button>
                <el-button type="text" size="small" icon="el-icon-delete" title="删除" @click="delActionData(scope.row)" :disabled="readonlyParent"></el-button>

              </template>
            </el-table-column>
            <el-table-column prop="ac_desc" label="动作名称" align="left" width="160"></el-table-column>
            <el-table-column prop="ac_cond_in" label="动作条件" align="left" width="230"></el-table-column>
            <el-table-column prop="ac_expr_in" label="处理函数" align="left" width="230"></el-table-column>
            <el-table-column label="有效性" align="left" width="80">
              <template slot-scope="scope">
                <el-switch
                  v-model="scope.row.ac_enable"
                  :active-value=1
                  :inactive-value=0
                  :disabled="acEnableBtnDisabled || readonlyParent"
                  @change="acEnableChange(scope.row)">
                </el-switch>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-dialog :title="actionDialogTitle" :visible.sync="actionDialogVisible" width="450px" :modal="false" :close-on-click-modal="false">
        <el-form :model="actionDialogForm" ref="actionDialogForm" style="text-align: left" :rules="actionRules" :inline="true">
          <el-form-item label="动作名称:" :label-width="formLabelWidth" prop="ac_desc" :style="formItemStyle">
            <el-input v-model="actionDialogForm.ac_desc" auto-complete="off" :style="formItemContentStyle" :maxlength="128" :disabled="readonlyParent"></el-input>
          </el-form-item>
          <el-form-item label="动作条件:" :label-width="formLabelWidth" prop="ac_cond_in" :style="formItemStyle">
            <div @dblclick="acCondInPopup">
              <el-input v-show="false" v-model="actionDialogForm.ac_cond" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
              <el-input v-model="actionDialogForm.ac_cond_in" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
            </div>
          </el-form-item>
          <el-form-item class="is-required" label="处理函数:" :label-width="formLabelWidth" prop="ac_expr_in" :style="formItemStyle">
            <div @dblclick="acExprInPopup">
              <el-input v-show="false" v-model="actionDialogForm.ac_expr" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
              <el-input v-model="actionDialogForm.ac_expr_in" auto-complete="off" :style="formItemContentStyle" readonly :disabled="readonlyParent"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="有效性:" :label-width="formLabelWidth" prop="ac_enable" :style="formItemStyle">
            <el-switch
              :disabled="readonlyParent"
              v-model="actionDialogForm.ac_enable"
              active-value="1"
              inactive-value="0"
            >
            </el-switch>
          </el-form-item>

          <div>
            <el-form-item  label=" " :label-width="formLabelWidth" :style="formItemStyle">
              <el-button v-if="!readonlyParent" type="primary" @click="submitActionForm('actionDialogForm')" :disabled="actionFormSureBtnDisabled" size="large">保 存</el-button>
              <el-button @click="actionDialogVisible = false" size="large">取 消</el-button>
            </el-form-item>
          </div>

        </el-form>
        <StatCondPicker ref="acCondDialog" @valueCallback="acCondInValueCallBack"
                        :statCond="actionDialogForm.ac_cond" :statCondIn="actionDialogForm.ac_cond_in" :txnId="txnIdParent"
                        :hideItems="['rule_func', 'ac_func']" >

        </StatCondPicker>

        <StatCondPicker ref="acExprDialog" @valueCallback="acExprValueCallBack"
                        :statCond="actionDialogForm.ac_expr" :statCondIn="actionDialogForm.ac_expr_in" :txnId="txnIdParent"
                        :hideItems="['rule_func']" >

        </StatCondPicker>
      </el-dialog>

    </el-dialog>

    <StatCondPicker ref="RuleCondDialog" @valueCallback="ruleCondInValueCallBack"
                    :statCond="dialogForm.rule_cond" :statCondIn="dialogForm.rule_cond_in" :txnId="txnIdParent"
                    :hideItems="['rule_func']" >

    </StatCondPicker>

    <el-dialog :title="refsDialogTitle" :visible.sync="refsDialogVisible" width="1000px" :close-on-click-modal="false">
      <el-table
        :data="refsTableData"
        style="width: 100%">
        <el-table-column prop="st_name" label="策略名称"></el-table-column>
        <el-table-column prop="tab_desc" label="所属交易"></el-table-column>
        <el-table-column label="评估方式">
          <template slot-scope="scope">
            <span>{{scope.row.eval_mode | evalModeFilter}}</span>
          </template>
        </el-table-column>
        <el-table-column label="规则执行方式">
          <template slot-scope="scope">
            <span>{{scope.row.rule_exec_mode | ruleExecModeFilter}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rule_count" label="规则数量"></el-table-column>
        <el-table-column label="创建时间" width="140">
          <template slot-scope="scope">
            <span>{{scope.row.createtime | renderDateTime}}</span>
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="140">
          <template slot-scope="scope">
            <span>{{scope.row.modifytime | renderDateTime}}</span>
          </template>
        </el-table-column>
        <el-table-column label="有效性">
          <template slot-scope="scope">
            <span>{{scope.row.st_enable | stEnableFilter}}</span>
          </template>
        </el-table-column>
      </el-table>

    </el-dialog>

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
        refsTableData: [],
        allStoreFd: [],
        enableStoreFd: [],
        dialogTitle: '',
        dialogTabActive: 'dataMod',
        refsDialogTitle: '策略列表',
        dialogType: '',
        dialogVisible: false,
        refsDialogVisible: false,
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
        actionDialogForm: this.initActionDialogForm(),
        dialogFormSureBtnDisabled: false,
        ruleEnableBtnDisabled: false,
        dialogFormRules: {
          rule_shortdesc: [
            { required: true, message: '请输入规则名称', trigger: 'blur' },
            { max: 128, message: '长度在128个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacterHasPunctuation, trigger: 'blur' }
          ],
          rule_cond: [
            { required: true, message: '请输入规则条件', trigger: 'blur' },
            { max: 1024, message: '长度在1024个字符以内', trigger: 'blur' }
          ],
          rule_cond_in: [
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
        ruleEnableList: [],

        // 下面是动作列表
        actionTableData: [],
        acEnableBtnDisabled: false,
        selectedRule: {},
        actionDialogVisible: false,
        actionDialogTitle: '',
        actionFormSureBtnDisabled: false,
        actionDialogType: '',
        actionRules: {
          ac_desc: [
            { required: true, message: '请输入动作名称', trigger: 'blur' },
            { max: 128, message: '长度在128个字符以内', trigger: 'blur' },
            { validator: check.checkFormZhSpecialCharacter, trigger: 'blur' }
          ],
          ac_cond_in: [
            { validator: this.checkAcCond, trigger: 'blur' }
          ],
          ac_expr_in: [
            { validator: this.checkAcRxpr, trigger: 'blur' }
          ],
          ac_enable: [
            { required: true, message: '请输入有效性', trigger: 'change' }
          ]
        }
      }
    },
    computed: {
      readonlyParent () {
        return this.readonly
      },
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
      },
      actionTableDataShow () {
        return this.actionTableData
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
      evalModeFilter (value) {
        return dictCode.rendCode('tms.strategy.evalmode', value)
      },
      ruleExecModeFilter (value) {
        return dictCode.rendCode('tms.strategy.rule_exec_mode', value)
      },
      stEnableFilter (value) {
        return dictCode.rendCode('tms.mgr.rulestatus', value)
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
    props: ['txnId', 'isVisibility', 'readonly'],
    mounted: function () {
      this.$nextTick(function () {
        vm = this
        // this.reloadData()
      })
    },
    watch: {
      txnId: function (val) {
        if (this.isVisibilityParent === true) {
          this.reloadData()
        }
      },
      isVisibility: function (val) {
        if (this.isVisibilityParent === true) {
          this.reloadData()
        }
      },
      'dialogForm.disposal': function (val) {
        for (let loop of this.disposalList) {
          if (val === '') {
            this.dialogForm.rule_score = ''
            break
          }
          if (val === loop.dp_code) {
            this.dialogForm.rule_score = loop.default_score
            break
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
      initActionDialogForm () {
        return {
          ac_desc: '',
          ac_cond_in: '',
          ac_cond: '',
          ac_expr_in: '',
          ac_expr: '',
          ac_enable: '0'
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
          this.dialogTabActive = 'dataMod'
          this.selectedRule = row
          this.allPickCollapse = true // 编辑时多选为隐藏多的标签
          this.dialogTitle = '编辑交易规则'
          if (this.readonlyParent) {
            this.dialogTitle = '查看交易规则'
          }
          this.modDisabled = true
          // 拷贝而不是赋值
          Object.assign(this.dialogForm, this.selectRowNum2Str(row))

          this.getActionData()
        } else if (dialogType === 'add') {
          this.dialogTabActive = 'dataMod'
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
          try {
            if (dialogType === 'add') {
              self.$refs.dialogTab.$refs.nav.$el.parentNode.style.display = 'none'
            } else if (dialogType === 'edit') {
              self.$refs.dialogTab.$refs.nav.$el.parentNode.style.display = 'block'
            }

          } catch (e) {
            console.error(e)
          }
        }, 100)
      },
      copy (row) {
        if (this.readonlyParent) {
          return
        }
        this.$confirm('确定复制?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          let rowTemp = {}
          Object.assign(rowTemp, row)
          rowTemp.rule_shortdesc = row.rule_shortdesc + '_复制'
          jsonData.copy = [rowTemp]
          let finalJsonData = {}
          finalJsonData.postData = jsonData
          finalJsonData.txnId = this.txnIdParent
          let self = this

          ajax.post({
            url: '/rule/save',
            param: finalJsonData,
            success: function (data) {
              self.getData()
              self.$message.success('复制成功')
            },
            error: function (data) {
              if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                self.$message.error(data.error.join('|'))
              } else {
                self.$message.error(data.error)
              }
              self.ruleEnableBtnDisabled = false
            },
            fail: function () {
              self.ruleEnableBtnDisabled = false
            }
          })
        })
      },
      copyAction (row) {
        if (this.readonlyParent) {
          return
        }
        this.$confirm('确定复制?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          let rowTemp = {}
          Object.assign(rowTemp, row)
          rowTemp.ac_desc = row.ac_desc + '_复制'
          if (rowTemp.ac_desc.length > 128) {
            this.$message({
              type: 'error',
              message: '动作复制后动作名称超过128个字符，复制失败'
            })
            return
          }
          rowTemp.rule_txn = this.txnIdParent
          rowTemp.rule_shortdesc = this.selectedRule.rule_shortdesc
          jsonData.copy = [rowTemp]
          let finalJsonData = {}
          finalJsonData.postData = jsonData
          finalJsonData.txnId = this.txnIdParent
          let self = this

          ajax.post({
            url: '/action/save',
            param: finalJsonData,
            success: function (data) {
              self.getActionData()
              self.$message.success('复制成功')
            },
            error: function (data) {
              if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                self.$message.error(data.error.join('|'))
              } else {
                self.$message.error(data.error)
              }
            }
          })
        })
      },
      delData (row) {
        if (this.readonlyParent) {
          return
        }
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          jsonData.del = [row]
          let finalJsonData = {}
          finalJsonData.postData = jsonData
          finalJsonData.txnId = this.txnIdParent
          let self = this

          ajax.post({
            url: '/rule/save',
            param: finalJsonData,
            success: function (data) {
              self.getData()
              self.$message.success('删除成功')
            }
          })
        })
      },
      delActionData (row) {
        if (this.readonlyParent) {
          return
        }
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let jsonData = {}
          let delObj = {}
          Object.assign(delObj, row)
          delObj.rule_txn = this.txnIdParent
          delObj.rule_shortdesc = this.selectedRule.rule_shortdesc
          jsonData.del = [delObj]
          let finalJsonData = {}
          finalJsonData.postData = jsonData
          finalJsonData.txnId = this.txnIdParent
          let self = this

          ajax.post({
            url: '/action/save',
            param: finalJsonData,
            success: function (data) {
              self.getActionData()
              self.$message.success('删除成功')
            }
          })
        })
      },
      openRefsDialog (row) {
        let self = this
        ajax.post({
          url: '/strategy/refList',
          param: {
            RULE_ID: row.rule_id
          },
          success: function (data) {
            self.refsTableData = data.row
            self.refsDialogVisible = true
          }
        })
      },
      ruleEnableChange (row) {
        if (this.readonlyParent) {
          return
        }
        let jsonData = {}
        this.ruleEnableBtnDisabled = true
        let message = '编辑成功'
        if (row.rule_enable === 1) {
          message = '已启用'
          jsonData['valid-y'] = [row]
        } else {
          message = '已停用'
          jsonData['valid-n'] = [row]
        }
        let finalJsonData = {}
        finalJsonData.postData = jsonData
        finalJsonData.txnId = this.txnIdParent
        let self = this
        ajax.post({
          url: '/rule/save',
          param: finalJsonData,
          success: function (data) {
            self.ruleEnableBtnDisabled = false
            self.$message.success(message)
          },
          error: function (data) {
            if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
              self.$message.error(data.error.join('|'))
            } else {
              self.$message.error(data.error)
            }
            self.ruleEnableBtnDisabled = false
            self.getData()
          },
          fail: function () {
            self.ruleEnableBtnDisabled = false
          }
        })
      },
      acEnableChange (row) {
        if (this.readonlyParent) {
          return
        }
        let jsonData = {}
        this.acEnableBtnDisabled = true
        let dataObj = {}
        Object.assign(dataObj, row)
        dataObj.rule_txn = this.txnIdParent
        dataObj.rule_shortdesc = this.selectedRule.rule_shortdesc
        let message = '编辑成功'
        if (row.ac_enable === 1) {
          message = '已启用'
          jsonData['valid-y'] = [dataObj]
        } else {
          message = '已停用'
          jsonData['valid-n'] = [dataObj]
        }

        let finalJsonData = {}
        finalJsonData.postData = jsonData
        finalJsonData.txnId = this.txnIdParent
        let self = this

        ajax.post({
          url: '/action/save',
          param: finalJsonData,
          success: function (data) {
            // self.getData()
            self.acEnableBtnDisabled = false
            self.$message.success(message)
          },
          error: function (data) {
            if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
              self.$message.error(data.error.join('|'))
            } else {
              self.$message.error(data.error)
            }
            self.acEnableBtnDisabled = false
            self.getActionData()
          },
          fail: function () {
            self.acEnableBtnDisabled = false
          }
        })
      },
      // 提交表单，新增，编辑都在这
      submitForm (formName) {
        if (this.readonlyParent) {
          return
        }
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
              message = '创建成功'
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
                self.$message.success(message)
                self.dialogVisible = false
                self.dialogFormSureBtnDisabled = false
              },
              error: function (data) {
                if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                  self.$message.error(data.error.join('|'))
                } else {
                  self.$message.error(data.error)
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
        if (this.readonlyParent) {
          return
        }
        this.$refs.RuleCondDialog.open()
        this.$refs.RuleCondDialog.setValue({
          stat_cond_value: this.dialogForm.rule_cond,
          stat_cond_in: this.dialogForm.rule_cond_in
        })
      },
      acCondInPopup () {
        if (this.readonlyParent) {
          return
        }
        this.$refs.acCondDialog.open()
        this.$refs.acCondDialog.setValue({
          stat_cond_value: this.actionDialogForm.ac_cond,
          stat_cond_in: this.actionDialogForm.ac_cond_in
        })
      },
      acExprInPopup () {
        if (this.readonlyParent) {
          return
        }
        this.$refs.acExprDialog.open()
        this.$refs.acExprDialog.setValue({
          stat_cond_value: this.actionDialogForm.ac_expr,
          stat_cond_in: this.actionDialogForm.ac_expr_in
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
      acCondInValueCallBack (value) {
        this.actionDialogForm.ac_cond = value.stat_cond_value
        this.actionDialogForm.ac_cond_in = value.stat_cond_in
      },
      acExprValueCallBack (value) {
        this.actionDialogForm.ac_expr = value.stat_cond_value
        this.actionDialogForm.ac_expr_in = value.stat_cond_in
        this.$refs.actionDialogForm.validateField('ac_expr_in', (valid) => {
        })
      },
      checkRuleScore (rule, value, callback) {
        if (!util.isNumber(value, null, '0') || parseInt(value) < -100 || parseInt(value) > 100) {
          return callback(new Error(`周期只能输入-100-100的整数`))
        }
        return callback()
      },
      openActionDialog (dialogType, row) {
        if (dialogType === 'edit') {
          this.actionDialogTitle = '编辑动作'
          if (this.readonlyParent) {
            this.actionDialogTitle = '查看动作'
          }
          // 拷贝而不是赋值
          Object.assign(this.actionDialogForm, this.selectRowNum2Str(row))
        } else if (dialogType === 'add') {
          this.actionDialogTitle = '新建动作'
          this.actionDialogForm = this.initActionDialogForm()
        }
        this.actionDialogType = dialogType
        this.actionDialogVisible = true
      },
      getActionData () {
        let ruleId = this.selectedRule.rule_id
        let self = this
        ajax.post({
          url: '/action/list',
          param: {
            rule_id: ruleId
          },
          success: function (data) {
            if (data.row) {
              self.actionTableData = data.row.map((x) => { x.ac_enable = parseInt(x.ac_enable); return x })
            }
          },
          error: function (data) {
            if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
              self.$message.error(data.error.join('|'))
            } else {
              self.$message.error(data.error)
            }
            self.dialogFormSureBtnDisabled = false
          },
          fail: function () {
            self.dialogFormSureBtnDisabled = false
          }
        })
      },
      submitActionForm (formName) {
        if (this.readonlyParent) {
          return
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.actionFormSureBtnDisabled = true
            let self = this
            let submitParam = {}
            submitParam.rule_id = this.selectedRule.rule_id;
            submitParam.rule_shortdesc = this.selectedRule.rule_shortdesc;
            submitParam.rule_txn = this.txnIdParent;
            Object.assign(submitParam, this.actionDialogForm)
            submitParam.rule_txn = this.txnIdParent
            let jsonData = {}
            let message = '提交成功'
            if (this.actionDialogType === 'add') {
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
              url: '/action/save',
              param: finalJsonData,
              success: function (data) {
                self.getActionData()
                self.$message.success(message)
                self.actionDialogVisible = false
                self.actionFormSureBtnDisabled = false
              },
              error: function (data) {
                if (data && data.error && typeof (data.error) === 'object' && data.error.length > 0) {
                  self.$message.error(data.error.join('|'))
                } else {
                  self.$message.error(data.error)
                }
                self.actionFormSureBtnDisabled = false
              },
              fail: function () {
                self.actionFormSureBtnDisabled = false
              }
            })
          }
        })
      },
      checkAcCond (rule, value, callback) {
        let acCond = this.actionDialogForm.ac_cond
        if (acCond !== undefined && acCond !== '') {
          if (acCond.length > 512) {
            callback(new Error('长度在512个字符以内'))
          }
        }
        callback()
      },
      checkAcRxpr (rule, value, callback) {
        let self = this
        let acExpr = self.actionDialogForm.ac_expr
        if (acExpr !== undefined && acExpr !== '') {
          if (acExpr.length > 512) {
            callback(new Error('长度在512个字符以内'))
          }
        } else {
          callback(new Error('处理函数不能为空'))
        }
        callback()
      }
    },
    components: {
      StatCondPicker
    }
  }
</script>

<style scoped>
  .rule-query-form-item{
    width: 200px;
  }
</style>
