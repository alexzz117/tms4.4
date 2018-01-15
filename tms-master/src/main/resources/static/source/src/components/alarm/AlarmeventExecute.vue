<template>
  <div >
    <el-form  label-position="right" :model="listForm" ref="listForm"   class="demo-form-inline" label-width="100px"  :inline="inline"  >
      <el-row :gutter="10">
        <el-col :span="2"><div><el-form-item label="流水号"/></div> </el-col>
        <el-col :span="4"><div>
          <el-form-item  prop="txncode" data="txncode">
            <el-input v-model="listForm.txncode" clearable></el-input>
          </el-form-item></div>
        </el-col>
        <el-col :span="2"><div ><el-form-item label="交易时间起"/></div></el-col>
        <el-col :span="4"><div>
          <el-form-item >
            <el-date-picker  type="datetime" placeholder="选择时间" v-model="listForm.operate_time" ></el-date-picker>
          </el-form-item></div>
        </el-col>
        <el-col :span="2"><div ><el-form-item label="交易时间止"/></div></el-col>
        <el-col :span="4"><div>
          <el-form-item >
            <el-date-picker type="datetime" placeholder="选择时间" v-model="listForm.end_time" ></el-date-picker>
          </el-form-item>
        </div>
        </el-col>
        <el-col :span="1"><div align="right" ><el-button class="el-icon-search" type="primary" @click="sel" ref="selBtn" id="selBtn">查询</el-button></div></el-col>
      </el-row>
    </el-form>
    <!--   数据列表  -->
    <el-table
      :data="gridData"
      style="width: 100%" tooltip-effect="dark" @selection-change="handleSelectionChange">
      <el-table-column fixed="left" label="操作" width="65" alert="center" >
        <template slot-scope="scope"  >
          <el-button type="text" @click="openDialog(scope.$index, scope.row)"   icon="el-icon-edit-outline" />
          <el-button type="text"  @click=""  icon="el-icon-search" />
        </template>
      </el-table-column>
      <el-table-column  prop="txncode" label="流水号" width="220" />
      <el-table-column  prop="userid" label="客户号" width="120"/>
      <el-table-column  prop="username" label="客户名称" width="80" />
      <el-table-column  prop="txntime" label="交易时间" width="135" :formatter="formatter"/>
      <el-table-column  prop="txnname" label="监控操作" width="100" />
      <el-table-column  prop="disposal" label="处置结果" width="80"  />
      <el-table-column  prop="assign_name" label="分派人" width="80"/>
      <el-table-column  prop="assigntime" label="分派时间" width="135" :formatter="formatter"/>
      <el-table-column  prop="psstatus" label="处理状态" width="80" :formatter="formatter"/>
      <el-table-column  prop="oper_name" label="处理人" width="80"/>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;" background
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="pageindex"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>

    <!-- 报警事件处理弹窗 -->
    <el-dialog title="报警事件处理" :visible.sync="listDialogVisible" append-to-body  >
      <div>
        <el-form  label-position="right" :model="executeForm"  :rules="executeFormRules" ref="executeForm"    >
          <el-row>
            <el-col :span="4"> <div ><el-form-item label="欺诈类型"  prop="FRAUD_TYPE"  data="fraud_type"/></div> </el-col>
            <el-col :span="10"><div >
              <el-select v-model="executeForm.FRAUD_TYPE" placeholder="请选择" @focus="selectFocus('fraud_type')" :clearable="clearable">
                <el-option
                  v-for="item in datatypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select></div>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="4" :rows="3"><div><el-form-item label="处理信息"  prop="PS_INFO"  data="PS_INFO"/></div></el-col>
            <el-col :span="20"><div><el-input
              type="textarea"resize="none" v-model="executeForm.PS_INFO"
              :rows="3"  auto-complete="off"
              placeholder="请输入内容">
            </el-input></div></el-col>
          </el-row>
        </el-form>

        <div>动作信息</div>
        <div>
            <el-button type="primary" plain icon="el-icon-circle-plus-outline" @click="addPsAct('add')"></el-button>
            <el-button type="primary" plain icon="el-icon-delete" @click="delPsAct()" :disabled="delBtnStatus"></el-button>
        </div>
              <!-- GRID -->
              <el-table
                :data="actionData" highlight-current-row height="200" stripe
                tooltip-effect="dark" style="width: 90%" @selection-change="handleCurrentRow">
                <el-table-column  type="selection" width="45" />
                <el-table-column fixed="left" label="操 作" width="55" alert="center" >
                  <template slot-scope="scope"  >
                    <el-button type="text" @click="editPsAct(scope.$index, scope.row,'edit')"  icon="el-icon-edit" />
                  </template>
                </el-table-column>
                <el-table-column  prop="ac_name" label="动作名称" width="120"  />
                <el-table-column  prop="ac_cond_in" label="条件" width="180"  />
                <el-table-column  prop="ac_expr_in" label="表达式" width="180"  />

              </el-table>

        <!-- 添加动作FROM -->
        <div>
        <transition name="fade">
          <el-form  label-position="right" :model="actionForm"  ref="actionForm" :rules="rules"   :inline="inline" >
            <el-row>
              <el-col :span="3"><div ><el-form-item label="动作名称"/></div></el-col>
              <el-col :span="9"><div ><el-form-item prop="ac_name" clearable>
                <el-input v-model="actionForm.ac_name" /></el-form-item>
              </div>
              </el-col>
              <el-col :span="2"><div ><el-form-item label="条件"/></div></el-col>
              <el-col :span="10">
                <div >
                  <el-form-item prop="ac_cond" clearable>
                    <div @dblclick="statCondInPopup" :disabled="ac_condviewDisabled">
                      <el-input v-model="actionForm.ac_cond" auto-complete="off" :style="formItemContentStyle" v-show="false" readonly />
                      <el-input v-model="actionForm.ac_cond_in" auto-complete="off"  :style="formItemContentStyle" readonly />
                    </div>
                  </el-form-item>
                </div></el-col>
            </el-row>
            <el-row>
              <el-col :span="3"><div ><el-form-item label="表达式"/></div></el-col>
              <el-col :span="10"><div ><el-form-item prop="ac_expr" clearable>
                <div @dblclick="ruleCondInPopup" :disabled="ac_condviewDisabled">
                <el-input v-model="actionForm.ac_expr" :style="formItemContentStyle"  v-show="false" readonly />
                  <el-input v-model="actionForm.ac_expr_in" auto-complete="off" :style="formItemContentStyle" readonly />
                </div>
                </el-form-item>
              </div>
              </el-col>
            </el-row>
          </el-form>
        </transition>
        </div>
        <!-- 添加动作FROM结束 -->
          <div class="dialog-footer"  align="center"  slot="footer">
            <el-button type="primary" icon="el-icon-success" @click="savePsAct('actionForm')">保存动作</el-button>
           <el-button data="cancelBtn" icon="el-icon-success" type="primary"  @click="saveProcess('executeForm')" >提交审核</el-button>
           <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" @click="closeDialog()" >取消</el-button>
        </div>
      </div>
    </el-dialog>


    <!-- 条件弹出引用common目录的StatCondPicker.vue -->
    <StatCondPicker ref="StatCondDialog" @valueCallback="statCondInValueCallBack"
                    :statCond="actionForm.ac_cond" :statCondIn="actionForm.ac_cond_in" :txnId="this.vTxncode"
                    :hideItems="['rule_func', 'ac_func']" >

    </StatCondPicker>
    <!-- 表达式弹出引用common目录的StatCondPicker.vue -->
    <StatCondPicker ref="RuleCondDialog" @valueCallback="ruleCondInValueCallBack"
                    :statCond="actionForm.ac_expr" :statCondIn="actionForm.ac_expr_in" :txnId="this.vTxncode"
                    :hideItems="['rule_func']" >

    </StatCondPicker>
  </div>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'
  import StatCondPicker from '@/components/common/StatCondPicker'

  export default {
    created () {
      this.sel()
    },
    methods: {
      handleSizeChange(val) {
        this.sel({
          pageindex:this.pageindex,
          pagesize:val
        })
      },
      handleCurrentChange(val) {
        this.sel({
          pageindex:val,
          pagesize:this.pagesize
        })
      },
      handleCurrentRow(val){
        this.multipleSelection = val;
        if (val.length >= 1){
          this.delBtnStatus = false
        } else {
          this.delBtnStatus = true
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      //查询条件下拉值转换
      selectFocus (name) {
        if (name === 'passtatus' && this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.alarm.process.status')
        }
        if (name === 'fraud_type' && this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.alarm.fraudtype')
        }
      },
      //GRID数据列表值转换
      formatter(row, column, cellValue) {
        switch(column.property)
        {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break;
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'assigntime':
            return dictCode.rendDatetime(cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
      //界面初始化和查询按钮事件
      sel(pageinfo) {
        var self = this
        var param
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex: this.pageindex,
            pagesize: this.pagesize
          }, this.listForm, pageinfo)
        } else {
          param = util.extend({
            pageindex: this.pageindex,
            pagesize: this.pagesize
          }, this.listForm)
        }
        ajax.post({
          url: '/alarmevent/executeList',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      //报警事件处理事件
      openDialog(index, row) {
        this.listDialogVisible = true
        var self = this
        var TXN_CODE = row.txncode
        this.vTxncode = TXN_CODE
        ajax.post({
          url: '/alarmevent/process',
          param: {TXN_CODE: TXN_CODE},
          success: function (data) {
            if (data.list) {
              self.actionData = data.list
            }
            if (data.row) {
              self.txnMap = data.row
              Object.assign(this.executeForm)
            }
          }
        })
      },
      //新增动作信息
      addPsAct(flag){
        var self = this
        this.flag = flag
        this.actionForm= {
          ac_name: "",
          ac_cond: "",
          ac_cond_in: "",
          ac_expr: "",
          ac_expr_in: ""
        }
      },
      //编辑动作信息
      editPsAct(index, row,flag) {
        var self = this
        this.flag = flag
        this.actionForm = Object.assign({}, row)
      },
      // 统计条件弹窗
      statCondInPopup () {
        this.$refs.StatCondDialog.open()
        this.$refs.StatCondDialog.setValue({
          ac_cond: this.actionForm.ac_cond,
          ac_cond_in: this.actionForm.ac_cond_in
        })
      },
      //表达式弹窗
      ruleCondInPopup () {
        this.$refs.RuleCondDialog.open()
        this.$refs.RuleCondDialog.setValue({
          ac_expr: this.actionForm.ac_expr,
          ac_expr_in: this.actionForm.ac_expr_in
        })
      },
      //条件弹窗回调
      statCondInValueCallBack (value) {
        this.actionForm.ac_cond = value.stat_cond_value
        this.actionForm.ac_cond_in = value.stat_cond_in
      },
      //表达式弹窗回调
      ruleCondInValueCallBack (value) {
        this.actionForm.ac_expr = value.stat_cond_value
        this.actionForm.ac_expr_in = value.stat_cond_in
      },
      //增加修改保存动作信息
      savePsAct(formName) {
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
           if(this.flag === 'add'){
             var param = util.extend({
               TXN_CODE: this.vTxncode
             }, this.actionForm)
             ajax.post({
               url: '/alarmevent/addPsAct',
               param: param,
               success: function (data) {
                 self.$message({
                   type: 'success',
                   message: '新增成功。'
                 })
                 //刷新动作列表
                 self.addPsAct()
               }
             })
           }else{
             //编辑保存
             var param = util.extend({
                 TXN_CODE: this.vTxncode,
                 ac_id: this.ac_id
               }, this.actionForm)
             ajax.post({
               url: '/alarmevent/updPsAct',
               param: param,
               success: function (data) {
                 self.$message({
                   type: 'success',
                   message: '修改成功。'
                 })
                 //刷新动作列表
                 self.addPsAct()
               }
             })
           }
          } else {
            return false
          }
        })
      },
      //批量删除动作信息
      delPsAct() {
        var self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          var param = {
            del: this.multipleSelection
          }
          ajax.post({
            url: '/alarmevent/delPsAct',
            param: param,
            success: function (data) {
              self.$message({
                message: '删除成功。',
                type: 'success'
              })
              self.sel()
            }
          })
        }).catch(() => {
        })
      },
      //提交审核按钮事件 PSSTATUS: '03', //处理状态改为待审核
      saveProcess(formName) {
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
            var param = util.extend({
              TXN_CODE: this.vTxncode,
              TXNCODE: this.vTxncode,
              PS_TYPE: '1',
              PSSTATUS: '03',
              PS_RESULT: '1'
            }, this.executeForm)
            ajax.post({
              url: '/alarmevent/saveProcess',
              param: param,
              success: function (data) {
                self.$message({
                  message: '处理成功。',
                  type: 'success'
                })
                self.listDialogVisible = false
                self.sel()
              }
            })
          } else {
            return false
          }
        })
      },
      //关闭窗体
      closeDialog(){
        var self = this
        this.listDialogVisible = false
        self.sel()
      }
    },
    data() {
      return {
        inline: true,
        clearable: true,
        listDialogVisible: false,
        delBtnStatus: true,
        ac_condviewDisabled: false,
        flag: '',
        listForm: {
          txncode: '',
          passtatus: '',
          starttime: '',
          endtime: '',
          userid: ''
        },
        actionForm: {
          ac_id: '',
          ac_name: '',
          ac_cond: '',
          ac_cond_in: '',
          ac_expr: '',
          ac_expr_in: ''
        },
        rules: {
          ac_name: [{ required: true, message: '动作名称不能为空', trigger: 'blur' },
                    { min: 3, max: 64, message: '长度在3到64个字符', trigger: 'blur' }],
          ac_expr: [{ required: true, message: '表达式不能为空', trigger: 'blur' },
                    { min: 3, max: 255, message: '长度在3到255个字符', trigger: 'blur' }]
        },
        executeForm: {
          FRAUD_TYPE: '',
          PS_INFO: ''
        },
        executeFormRules: {
          FRAUD_TYPE: [{ required: true, message: '欺诈类型不能为空', trigger: 'change' }],
          PS_INFO: [{ required: true, message: '处理信息不能为空', trigger: 'blur' },
            { min: 3, max: 255, message: '长度在3到255个字符', trigger: 'blur' }]
        },
        gridData: [],
        actionData: [],
        txnMap: [],
        vTxncode: '',
        pageindex: 1,
        pagesize: 10,
        total: 100,
        datatypeOptions: []
      }
    },props: ['ac_id'], components: {StatCondPicker}
  }
</script>


