<template>
<div>
  <el-form  label-position="right" :model="listForm"  label-width="100px"  class="demo-form-inline"  :inline="inline"  >
  <el-row>
    <el-col :span="2"><div><el-form-item label="流水号"/></div> </el-col>
    <el-col :span="5"><div>
      <el-form-item prop="listFormFosterdesc">
        <el-input v-model="listForm.txncode" ></el-input>
      </el-form-item></div>
    </el-col>
    <el-col :span="2"><div ><el-form-item label="交易时间起"/></div></el-col>
    <el-col :span="5"><div>
          <el-form-item >
            <el-date-picker type="datetime" placeholder="选择时间" v-model="listForm.operate_time" ></el-date-picker>
          </el-form-item></div>
    </el-col>
    <el-col :span="2"><div ><el-form-item label="交易时间止"/></div></el-col>
    <el-col :span="5"><div>
        <el-form-item >
          <el-date-picker type="datetime" placeholder="选择时间" v-model="listForm.end_time" ></el-date-picker>
        </el-form-item>
    </div>
    </el-col>
    <el-col :span="3"><div > <el-button class="el-icon-more" type="primary" @click="showMore"/></div></el-col>
  </el-row>

    <el-row>
      <el-col :span="2"><div ><el-form-item label="客户号"/></div></el-col>
      <el-col :span="5"><div ><el-form-item prop="listFormFosterdesc" >
        <el-input v-model="listForm.userid" /></el-form-item>
      </div>
      </el-col>

      <el-col :span="2"> <div ><el-form-item label="处理状态"  prop="PSSTATUS"/></div> </el-col>
      <el-col :span="5"><div >
        <el-select v-model="listForm.passtatus" placeholder="请选择" @focus="selectFocus('passtatus')" :clearable="clearable">
          <el-option
            v-for="item in datatypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select></div>
      </el-col>

      <el-col :span="9"><div align="right" ><el-button class="el-icon-search" type="primary" @click="sel">查询</el-button></div></el-col>
    </el-row>
  <el-row>
    <el-col :span="1"><div >  <el-button type="primary" :align="left"  icon="el-icon-success" @click="openDialog" :disabled="sendBtnStatus">分派</el-button></div></el-col>
  </el-row>
  </el-form>

  <!--   数据列表  -->
  <el-table
    :data="gridData"
    style="width: 100%" tooltip-effect="dark" @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" align="left" />
    <el-table-column fixed="left" label="操 作" width="60" alert="center" >
      <template slot-scope="scope"  >
        <el-button type="text"  @click="" size="mini"  icon="el-icon-search" />
      </template>
    </el-table-column>
    <el-table-column  prop="txncode" label="流水号" width="210" align="left" />
    <el-table-column  prop="userid" label="客户号" width="150" align="left" />
    <el-table-column  prop="username" label="客户名称" width="120" align="left" />
    <el-table-column  prop="txntime" label="操作时间" width="150" align="left" :formatter="formatter"/>
    <el-table-column  prop="txnname" label="监控操作" width="100" align="left" />
    <el-table-column  prop="disposal" label="处置结果" width="140" align="left" />
    <el-table-column  prop="psstatus" label="处理状态" width="80" :formatter="formatter"/>
    <el-table-column  prop="oper_name" label="处理人" width="80"/>
  </el-table>
  <el-pagination background class="block" label="left" label-width="100px"
                            @size-change="handleSizeChange"
                            @current-change="handleCurrentChange"
                            :current-page="pageindex"
                            :page-sizes="[10, 25, 50, 100]"
                            :page-size="pagesize"
                            layout="total, prev, pager, next"
                            :total="total">
 </el-pagination>

  <!-- 选择人员弹窗 -->
  <el-dialog title="报警事件分派" :visible.sync="listDialogVisible" append-to-body  >
    <div>
      <el-table
        :data="userData" highlight-current-row height="280"
        tooltip-effect="dark" style="width: 90%" @selection-change="handleCurrentRow">
        <el-table-column type="selection" width="55" align="left"></el-table-column>

        <el-table-column  prop="login_name" label="登录名" width="180" align="left" />
        <el-table-column  prop="assign_number" label="总量" width="70" align="left" />
        <el-table-column  prop="process_number" label="已处理" width="70" align="left" />
        <el-table-column  prop="unprocess_number" label="未处理" width="70" align="left" />
      </el-table>
      <div class="dialog-footer"  align="center"  slot="footer">
        <el-button data="cancelBtn" icon="el-icon-success" type="primary"  @click="onSaveAssign" :disabled="btnStatus">确 定</el-button>
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
        if (val.length === 1){
          this.btnStatus = false
        }
        else {
          this.btnStatus = true
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
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
        if (name === 'passtatus' && this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.alarm.process.status')
        }
      },
      formatter(row, column, cellValue) {
        //GRID数据列表值转换
        switch(column.property)
        {
          case 'psstatus':
            return dictCode.rendCode('tms.alarm.process.status', cellValue)
            break;
          case 'txntime':
            return dictCode.rendDatetime(cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
      sel(pageinfo) {
        //界面初始化和查询按钮事件
        var self = this;
        var param;
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm, pageinfo)
        } else {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm)
        }
        ajax.post({
          url: '/alarmevent/list',
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
      openDialog(){
        //分派按钮弹出报警事件人员窗体
        var self=this
        this.listDialogVisible=true
        this.txncodeData = this.multipleSelection //保存选中的流水信息
        var param = {
          TXNCODES: this.multipleSelection
        }
        debugger
        ajax.post({
          url: '/alarmevent/assign',
          param: param,
          success: function (data) {
            if (data.list) {
              self.userData =  data.list
            }
            if(data.rosterIds){
              self.rosterIds  = data.rosterIds
            }
          }
        })
      },
      onSaveAssign(){
        var self = this
        var param = [{
          TXNCODES: this.txncodeData, //需要分派的流水交易
          OPERATER: this.multipleSelection //选中操作员
        }]
        debugger
        ajax.post({
          url: '/alarmevent/onsaveAssign',
          param: param,
          success: function (data) {
            self.$message({
              message: '分派成功。',
              type: 'success'
            })
            self.listDialogVisible = false
            self.sel()
          }
        })
      }
      },
    data() {
      return {
        inline: true,
        clearable: true,
        sendBtnStatus: true,
        listDialogVisible: false,
        btnStatus: true,
        listForm: {
          txncode: '',
          passtatus: '',
          starttime: '',
          endtime: '',
          userid: ''
        },
        gridData: [],
        userData: [],
        txncodeData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        datatypeOptions: []
      }
    }, props: ['operator_id']
  }
</script>


