<template>
  <div>
    <el-form  label-position="right" :model="listForm" ref="listForm"  label-width="100px" :inline="inline"  >
      <el-row :gutter="10">
        <el-row>
          <el-col :span="2"><div ><el-form-item label="客户号"/></div></el-col>
          <el-col :span="4"><div ><el-form-item prop="userid" >
            <el-input v-model="listForm.userid" /></el-form-item>
          </div>
          </el-col>
        <el-col :span="2"><div ><el-form-item label="交易时间起"/></div></el-col>
        <el-col :span="4"><div>
          <el-form-item >
            <el-date-picker type="datetime" placeholder="选择时间" v-model="listForm.operate_time" ></el-date-picker>
          </el-form-item></div>
        </el-col>
          <el-col :span="2"><div ><el-form-item label="交易时间止"/></div></el-col>
        <el-col :span="4"><div>
          <el-form-item >
            <el-date-picker type="datetime" placeholder="选择时间" v-model="listForm.end_time" ></el-date-picker>
          </el-form-item>
        </div>
        </el-col>
          <el-col :span="1"><el-button class="el-icon-search" type="primary" @click="sel" ref="selBtn" id="selBtn">查询</el-button></el-col>
      </el-row>
      </el-row>
    </el-form>

    <!--   数据列表  -->
    <el-table
      :data="gridData"
      style="width: 100%" tooltip-effect="dark" @selection-change="handleSelectionChange">
      <el-table-column fixed="left" label="操作" width="65" alert="center" >
        <template slot-scope="scope"  >
          <el-button type="text"  @click="openDialog(scope.$index, scope.row)" size="mini"  icon="el-icon-edit-outline" />
          <el-button type="text"  @click="" size="mini"  icon="el-icon-search" />
        </template>
      </el-table-column>
      <el-table-column  prop="txncode" label="流水号" width="235" align="left" />
      <el-table-column  prop="userid" label="客户号" width="110" align="left" />
      <el-table-column  prop="username" label="客户名称" width="100" align="left" />
      <el-table-column  prop="txntime" label="交易时间" width="135" align="left" :formatter="formatter"/>
      <el-table-column  prop="txnname" label="监控操作" width="80" align="left" />
      <el-table-column  prop="disposal" label="处置结果" width="80" align="left" />
      <el-table-column  prop="psstatus" label="处理状态" width="80" :formatter="formatter"/>
      <el-table-column  prop="assign_name" label="分派人" width="60"/>
      <el-table-column  prop="oper_name" label="处理人" width="60"/>
      <el-table-column  prop="opertime" label="处理时间" width="135" :formatter="formatter"/>
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

    <!--报警事件审核ExecuteData -->
    <el-dialog title="报警事件审核" :visible.sync="listDialogVisible" append-to-body  >
      <div>
        <el-form label-position="right"  :model="actionForm" :inline="inline" >
          <el-row>
            <el-col :span="3"><div ><el-form-item label="欺诈类型"/></div></el-col>
            <el-col :span="9"><div ><el-form-item prop="fraud_type" clearable>
              <el-select v-model="actionForm.fraud_type" placeholder="请选择" @focus="selectFocus('fraud_type')" disabled>
                <el-option
                  v-for="item in datatypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            </div>
            </el-col>
          </el-row>
        </el-form>

        <el-collapse v-model="activeNames" @change="handleChange">
          <el-collapse-item title="处理动作信息" name="1">
            <el-table
              :data="actionData" highlight-current-row height="100"
              tooltip-effect="dark" style="width: 90%" @selection-change="handleCurrentRow">
              <el-table-column  prop="ac_name" label="动作名称" width="180"  />
              <el-table-column  prop="ac_cond_in" label="条件" width="180"  />
              <el-table-column  prop="ac_expr_in" label="表达式" width="180"  />
            </el-table>
          </el-collapse-item>
          <el-collapse-item title="报警处理信息" name="2">
            <el-table
              :data="executeData" highlight-current-row height="120"
              tooltip-effect="dark" style="width: 90%" @selection-change="handleCurrentRow">
              <el-table-column  prop="ps_time" label="处理时间" width="140"  />
              <el-table-column  prop="ps_type" label="处理类型" width="90"   :formatter="formatter"/>
              <el-table-column  prop="login_name" label="处理人员" width="90"  />
              <el-table-column  prop="ps_result" label="处理结果" width="90"   :formatter="formatter"/>
              <el-table-column  prop="ps_info" label="处理信息" width="140"  />
            </el-table>
          </el-collapse-item>
        </el-collapse>

        <!--审核信息 -->
        <el-form  label-position="right" :model="auditForm"  ref="auditForm" :rules="rules"    >
          <el-row>
            <el-col :span="3.5"><div ><el-form-item label="审核结果" prop="PS_RESULT"/></div></el-col>
            <el-col :span="9"><div >
              <el-form-item  >
                <el-radio v-model="auditForm.PS_RESULT" label="1">通过</el-radio>
               <el-radio v-model="auditForm.PS_RESULT" label="0">不通过</el-radio>
              </el-form-item>
            </div>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="3.5" :rows="3"><div><el-form-item label="处理信息"  prop="PS_INFO"  /></div></el-col>
            <el-col :span="20"><div><el-input
              type="textarea"resize="none" v-model="auditForm.PS_INFO"
              :rows="3"  auto-complete="off"
              placeholder="请输入内容">
            </el-input></div></el-col>
          </el-row>
        </el-form>
        <div class="dialog-footer"  align="center"  slot="footer">
          <el-button data="cancelBtn" icon="el-icon-success" type="primary"  @click="onSaveAudit('auditForm')" >提 交</el-button>
          <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" @click="listDialogVisible = false" >返 回</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'

  export default {
    created () {
      this.sel()
    },
    methods: {
      handleSizeChange(val) {
        this.sel({
          pageindex: this.pageindex,
          pagesize: val
        })
      },
      handleCurrentChange(val) {
        this.sel({
          pageindex: val,
          pagesize: this.pagesize
        })
      },
      handleCurrentRow(val){
        this.multipleSelection = val
        if (val.length === 1){
          this.btnStatus = false
        }
        else {
          this.btnStatus = true
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val
        if (val.length === 1){
          this.sendBtnStatus = false
        } else if (val.length > 1){
          this.sendBtnStatus = false
        } else {
          this.sendBtnStatus = true
        }
      },
      selectFocus (name) {
        //查询条件下拉值转换
        if (name === 'fraud_type' && this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.alarm.fraudtype')
        }
      },
      formatter(row, column, cellValue) {
        //GRID数据列表值转换
        switch(column.property)
        {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break;
          case 'ps_result':
            return dictCode.rendCode('tms.alarm.process.result', cellValue)
            break;
          case 'ps_type':
            return dictCode.rendCode('tms.alarm.process.type', cellValue)
            break;
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'opertime':
            return dictCode.rendDatetime(cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
      sel(pageinfo) {
        //界面初始化和查询按钮事件
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
          url: '/alarmevent/auditList',
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
      //报警事件审核弹窗
      openDialog(index, row){
        var self = this
        this.listDialogVisible = true
        var TXN_CODE = row.txncode
        this.vTxncode = TXN_CODE
        ajax.post({
          url: '/alarmevent/audit',
          param: {TXN_CODE: TXN_CODE},
          success: function (data) {
            console.log(data)
            debugger
            var aa = data['txnmap']
            var psStatus = aa['fraud_type']

            if (data.actlist) {
              self.actionData = data.actlist
            }
            if (data.pslist) {
              self.executeData = data.pslist
            }
            // if (data.txnmap) {
            //   self.actionForm =  Object.assign({}, txnmap.fraud_type)
            // }
          }
        })
      },
      //报警事件审核提交
      onSaveAudit(formName){
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
            var param = util.extend({
              TXN_CODE: this.vTxncode,
              PS_TYPE: '2'
            }, this.auditForm)
            ajax.post({
              url: '/alarmevent/saveAudit',
              param: param,
              success: function (data) {
                self.$message({
                  message: '审核成功。',
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
      }
    },
    data() {
      return {
        inline: true,
        clearable: true,
        listDialogVisible: false,
        listForm: {
          txncode: '',
          passtatus: '',
          starttime: '',
          endtime: '',
          userid: ''
        },
        actionForm: {
          fraud_type: ''
        },
        auditForm: {
          PS_RESULT: '',
          PS_INFO: ''
        },
        rules: {
          PS_RESULT: [{ required: true, message: '审核结果为空', trigger: 'blur' }],
          PS_INFO: [{ required: true, message: '处理信息为空', trigger: 'blur' },
            { min: 3, max: 255, message: '长度在3到255个字符', trigger: 'blur' }]
        },
        gridData: [],
        actionData: [],
        executeData: [],
        vTxncode: '',
        pageindex: 1,
        pagesize: 10,
        total: 100,
        datatypeOptions: []
      }
    }
  }
</script>


