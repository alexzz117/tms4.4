<template>
  <div>
    <h1>名单值管理 -> {{ title }}</h1>
    <el-form label-position="right" label-width="100px" :model="valueListForm"
             :inline="inline" style="text-align: left">
      <el-form-item label="名单值">
        <el-input v-model="valueListForm.rostervalue"></el-input>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-search" type="primary" @click="sel">查询</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')" :disabled="btnStatus">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="del" :disabled="delBtnStatus">删除</el-button>
      <el-button plain class="el-icon-edit" @click="changeValue" :disabled="btnStatus">值转换</el-button>
      <el-button plain class="el-icon-back" @click="backList">返回</el-button>
    </div>
    <el-table
      :data="gridData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="rostervalue" label="名单值" align="left" width="200"></el-table-column>
      <el-table-column prop="enabletime" label="开始时间" align="left" width="200"></el-table-column>
      <el-table-column prop="disabletime" label="结束时间" align="left" width="200"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="200"></el-table-column>
      <el-table-column prop="remark" label="备注" align="left" width="300"></el-table-column>
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
          <el-date-picker v-model="valueListDialogform.enabletime" type="datetime"
                          value-format="yyyy-MM-dd HH:mm:ss"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="disabletime">
          <el-date-picker v-model="valueListDialogform.disabletime" type="datetime"
                          value-format="yyyy-MM-dd HH:mm:ss"></el-date-picker>
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

    <el-dialog title="值转换" :visible.sync="changeValueDialogVisible">
      <el-table
        :data="changeValueTableData"
        highlight-current-row
        @current-change="handleCurrentChangeValueTable"
        style="width: 100%">
        <!--<el-table-column type="selection" width="55" align="left"></el-table-column>-->
        <el-table-column prop="rosterdesc" label="名单名称" align="left" width="200"></el-table-column>
        <el-table-column prop="datatype" label="名单数据类型" align="left" width="200"></el-table-column>
        <el-table-column prop="rostertype" label="名单类型" align="left" width="200"></el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="changeValueDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="constrast">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import util from '@/common/util'
  import check from '@/common/check'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'

  export default {
    created () {
      this.getList(this.sel)
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
        this.dialogTitle = `名单值管理->${this.title}`
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
              enabletime: new Date().format('yyyy-MM-dd hh:mm:ss'),
              disabletime: "",
              remark: ""
            }
          }
        }
        this.valueListDialogVisible = true
        if (this.$refs['valueListDialogform']) {
          this.$refs['valueListDialogform'].clearValidate()
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
        if (val.length === 1){
          this.btnStatus = false
          this.delBtnStatus = false
        } else if (val.length > 1){
          this.btnStatus = true
          this.delBtnStatus = false
        } else {
          this.btnStatus = true
          this.delBtnStatus = true
        }
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
      bindValueGridData(data) {
        this.changeValueTableData = data.row
      },
      getList(cb) {
        self = this
        var param = {
          rosterid: this.$router.history.current.params.rosterid
        }
        ajax.post({
          url: '/mgr/get',
          param: param,
          success: function (data) {
            self.listData = data.row
            self.title = data.row.rosterdesc
            if (cb) {
              cb()
            }
          }
        })
      },
      add() {
        var self = this
        var param = util.extend({
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype,
          rosterdesc: this.listData.rosterdesc,
        }, this.valueListDialogform)

        ajax.post({
          url: '/mgr/valueadd',
          param: param,
          success: function (data) {
            self.$message('创建成功。')
            self.valueListDialogVisible = false
            self.sel()
          }
        })
      },
      del() {
        var self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/mgr/valuedel',
            param: {
              postData: {
                del: this.multipleSelection
              }
            },
            success: function (data) {
              self.$message({
                type: 'success',
                message: '删除成功!'
              })
              self.sel()
            }
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      },
      sel(pageinfo) {
        var self = this;
        var param;
        var comParam = {
          pageindex:this.pageindex,
          pagesize:this.pagesize,
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype
        }
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend(comParam, this.valueListForm, pageinfo)
        } else {
          param = util.extend(comParam, this.valueListForm)
        }
        ajax.post({
          url: '/mgr/valuelist',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      update() {
        var self = this;
        var param = util.extend({
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype,
          rosterdesc: this.listData.rosterdesc,
        }, this.valueListDialogform)

        ajax.post({
          url: '/mgr/valuemod',
          param: param,
          success: function (data) {
            self.$message('更新成功。')
            self.valueListDialogVisible = false
            self.sel()
          }
        })
      },
      changeValue() {
        var self = this
        ajax.post({
          url: '/mgr/changevalueget',
          param: {
            rosterid: this.listData.rosterid,
            datatype: this.listData.datatype,
            rostertype: this.listData.rostertype
          },
          success: function (data) {
            self.bindValueGridData(data)
            self.changeValueDialogVisible= true
          }
        })
      },
      // 单击值转换确定按钮时
      constrast() {
        var rosterValue = this.multipleSelection[0]
        ajax.post({
          url: '/mgr/changevalue',
          param: {
            rostervalueid: rosterValue.rostervalueid,
            rostervalue: rosterValue.rostervalue,
            rosterid: this.valuecurrentRow.rosterid + '||||' + this.valuecurrentRow.rosterdesc,
            rosteridold: this.listData.rosterid,
            rosterdescout: this.listData.rosterdesc
          },
          success: function (data) {
            self.$message('值转换成功。')
            self.changeValueDialogVisible= false
            self.sel()
          }
        })
      },
      handleCurrentChangeValueTable(val) {
        this.valuecurrentRow = val;
      },
      backList() {
        this.$router.push({ name: 'list' })
//        this.$router.push('list');
      }
    },
    data() {
      return {
        inline: true,
        valueListForm: {
          rostervalue: ""
        },
        listData: {
          rosterdesc: ""
        },
        gridData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        valueListDialogVisible: false,
        valueListDialogform: {
          rostervalue: "",
          enabletime: new Date().format('yyyy-MM-dd hh:mm:ss'),
          disabletime: "",
          remark: ""
        },
        dialogTitle: "",
        title: "testdddd",
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
        flag: '',
        changeValueDialogVisible: false,
        changeValueTableData: [],
        valuecurrentRow: {},
        btnStatus: true,
        delBtnStatus: true
      }
    }
  }
</script>

