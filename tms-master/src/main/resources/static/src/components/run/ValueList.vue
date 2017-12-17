<template>
  <div>
    <h1>名单值管理->{{listdata.rosterdesc}}</h1>
    <el-form label-position="right" label-width="100px" :model="valueListForm" :rules="rules"
             :inline="inline" style="text-align: left">
      <el-form-item label="名单值" prop="rostervalue">
        <el-input v-model="valueListForm.rostervalue"></el-input>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-search" type="primary" @click="sel">查询</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="del">删除</el-button>
      <el-button plain class="el-icon-setting" @click="convertValue">值转换</el-button>
      <el-button plain class="el-icon-setting" @click="backList">返回</el-button>
    </div>
    <el-table
      :data="gridData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="rostervalue" label="名单值" align="left" width="120"></el-table-column>
      <el-table-column prop="enabletime" label="开始时间" align="left" width="120"></el-table-column>
      <el-table-column prop="disabletime" label="结束时间" align="left" width="120"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="120"></el-table-column>
      <el-table-column prop="remark" label="备注" align="left" width="120"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="pageindex"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>
    <el-dialog :title="dialogTitle" :visible.sync="valueListDialogVisible">
      <el-form :model="valueListDialogform" :rules="rules" ref="valueListDialogform" :label-width="formLabelWidth"
               style="text-align: left">
        <el-form-item label="名单值" prop="rostervalue">
          <el-input v-model="valueListDialogform.rostervalue" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="开始时间" prop="enabletime">
          <el-date-picker v-model="valueListDialogform.enabletime" type="datetime"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="disabletime">
          <el-date-picker v-model="valueListDialogform.disabletime" type="datetime"></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="valueListDialogform.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="valueListDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('valueListDialogform')">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import axios from 'axios'
  import util from '@/common/util'
  import check from '@/common/check'
  import ajax from '@/common/ajax'

  export default {
    created () {
      this.getList()
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
      openDialog(flag) {
        this.flag = flag
        this.dialogTitle = `名单值管理->${this.listdata.rosterdesc}`
        if (flag === 'edit') {
          var length = this.multipleSelection.length
          if (length !== 1) {
            this.$message('请选择一行名单值信息。')
            return
          }
          this.valueListDialogform = util.extend({}, this.multipleSelection[0])
        } else if (flag === 'add') {
          if (this.$refs['valueListDialogform']) {
            this.$refs['valueListDialogform'].resetFields();
          } else {
            this.valueListDialogform = {
              rostervalue: "",
              enabletime: "",
              disabletime: "",
              remark: ""
            }
          }
        }
        this.valueListDialogVisible = true
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            if (this.flag === 'add') {
              this.add()
            } else if (this.flag === 'edit') {
              this.update()
            }
          } else {
            console.log('error submit!!');
            return false;
          }
        })
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      getList() {
        var parm = {
          rosterid: this.$router.params.rosterid
        }
        ajax.post('/tms/mgr/get', parm, function (data) {
          this.listData = data.row
        })
      },
      add() {
        var self = this
        var parm = util.extend({
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype,
          rosterdesc: this.listData.rosterdesc,
        }, this.valueListDialogform)
        ajax.post('/tms/mgr/valueadd', parm, function (data) {
          self.$message('创建成功。')
          self.valueListDialogVisible = false
          self.sel()
        })
      },
      del() {
        var self = this
        ajax.post('/tms/mgr/valuedel', {
          del: this.multipleSelection
        }, function (data) {
          self.$message('删除成功。')
          self.sel()
        })
      },
      sel(pageinfo) {
        var self = this;
        var param;
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.valueListForm, pageinfo)
        } else {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.valueListForm)
        }
        ajax.post('/tms/mgr/valuelist', param, function (data) {
          if (data.page) {
            self.bindGridData(data)
          }
        })
      },
      update() {
        var self = this;
        var parm = util.extend({
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype,
          rosterdesc: this.listData.rosterdesc,
        }, this.valueListDialogform)
        ajax.post('/tms/mgr/valuemod', parm, function (data) {
          self.$message('更新成功。')
          self.valueListDialogVisible = false
          self.sel()
        })
      },
      convertValue() {
        console.log('值转换')
      },
      backList() {
        this.$router.push('list');
      }
    },
    data() {
      return {
        inline: true,
        valueListForm: {
          rostervalue: ""
        },
        listData: {},
        gridData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        valueListDialogVisible: false,
        valueListDialogform: {
          rostervalue: "",
          enabletime: "",
          disabletime: "",
          remark: ""
        },
        dialogTitle: '',
        formLabelWidth: '150px',
        rules: {
          rostervalue: [
            { required: true, message: '请输入名单英文名', trigger: 'blur' }
//            ,
//            { max: 32, message: '长度在50个字符以内', trigger: 'blur' },
//            { validator: check.checkFormEnSpecialCharacter, trigger: 'blur' }
          ],
          enabletime: [
//            { required: true, message: '请输入名单名称', trigger: 'blur' },
//            { max: 64, message: '长度在50个字符以内', trigger: 'blur' },
//            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          disabletime: [
//            { required: true, message: '请输入名单数据类型', trigger: 'blur' }
          ],
          remark: [
            { max: 512, message: '长度在512个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        multipleSelection: [],
        flag: ''
      }
    }
  }
</script>

