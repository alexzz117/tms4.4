<template>
  <div>
    <el-form label-position="right" label-width="100px" :model="listForm" :rules="rules"
             :inline="inline" style="text-align: left">
      <el-form-item label="名单名称" prop="rosterdesc">
        <el-input v-model="listForm.rosterdesc"></el-input>
      </el-form-item>
      <el-form-item label="名单数据类型">
        <el-select v-model="listForm.datatype" placeholder="名单数据类型">
          <el-option label="字符类型" value="0"></el-option>
          <el-option label="设备标识" value="1"></el-option>
          <el-option label="ip地址" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="名单类型">
        <el-select v-model="listForm.rostertype" placeholder="名单类型">
          <el-option label="白名单" value="0"></el-option>
          <el-option label="灰名单" value="1"></el-option>
          <el-option label="黑名单" value="2"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-search" type="primary" @click="sel">查询</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')" :disabled="btnStatus">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="del" :disabled="delBtnStatus">删除</el-button>
      <el-button plain class="el-icon-setting" @click="showValueList" :disabled="btnStatus">名单值</el-button>
      <el-button plain class="el-icon-setting" @click="importList" :disabled="btnStatus">导入</el-button>
      <el-button plain class="el-icon-setting" @click="exportList" :disabled="btnStatus">导出</el-button>
    </div>
    <el-table
      :data="gridData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="rostername" label="名单英文名" align="left" width="120"></el-table-column>
      <el-table-column prop="rosterdesc" label="名单名称" align="left" width="120"></el-table-column>
      <el-table-column prop="datatype" label="名单数据类型" align="left" width="120"></el-table-column>
      <el-table-column prop="rostertype" label="名单类型" align="left" width="120"></el-table-column>
      <el-table-column prop="valuecount" label="值数量" align="left" width="120"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="150"></el-table-column>
      <el-table-column prop="iscache" label="是否缓存" align="left" width="120"></el-table-column>
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
    <el-dialog :title="dialogTitle" :visible.sync="listDialogVisible">
      <el-form :model="listDialogform" :rules="rules" ref="listDialogform" :label-width="formLabelWidth"
               style="text-align: left">
        <el-form-item label="名单英文名" prop="rostername">
          <el-input v-model="listDialogform.rostername" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单名称" prop="rosterdesc">
          <el-input v-model="listDialogform.rosterdesc" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单数据类型" prop="datatype">
          <el-select v-model="listDialogform.datatype" :disabled="status">
            <el-option label="字符类型" value="0"></el-option>
            <el-option label="设备标识" value="1"></el-option>
            <el-option label="ip地址" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名单类型" prop="rostertype">
          <el-select v-model="listDialogform.rostertype" :disabled="status">
            <el-option label="白名单" value="0"></el-option>
            <el-option label="灰名单" value="1"></el-option>
            <el-option label="黑名单" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否缓存" prop="iscache">
          <el-radio v-model="listDialogform.iscache" label="1">是</el-radio>
          <el-radio v-model="listDialogform.iscache" label="2">否</el-radio>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="listDialogform.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="listDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('listDialogform')">保 存</el-button>
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
        if (flag === 'edit') {
          this.dialogTitle = '编辑名单'
          this.status = true
          var length = this.multipleSelection.length
          if (length !== 1) {
            this.$message('请选择一行名单信息。')
            return
          }
          console.log(this.listDialogform)
          console.log(this.multipleSelection[0])
          this.listDialogform = util.extend({}, this.multipleSelection[0])
        } else if (flag === 'add') {
          this.dialogTitle = '新建名单'
          this.status = false
          if (this.$refs['listDialogform']) {
            this.$refs['listDialogform'].resetFields();
          } else {
            this.listDialogform = {
              rostername: "",
              rosterdesc: "",
              datatype: "",
              rostertype: "",
              iscache: "1",
              remark: "",
            }
          }
        }
        this.listDialogVisible = true
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
          console.log(valid)
          console.log(this)
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
      add() {
        var self = this
        ajax.post('/tms/mgr/add', this.listDialogform, function (data) {
          self.$message('创建成功。')
          self.listDialogVisible = false
          self.sel()
        })
      },
      del() {
        var self = this
        ajax.post('/tms/mgr/del', {
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
          }, this.listForm, pageinfo)
        } else {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm)
        }
        ajax.post('/tms/mgr/list', param, function (data) {
          if (data.page) {
            self.bindGridData(data)
          }
        })
      },
      update() {
        var self = this;
        ajax.post('/tms/mgr/mod', this.listDialogform, function (data) {
          self.$message('更新成功。')
          self.listDialogVisible = false
          self.sel()
        })
      },
      showValueList() {
//        valuelist/:rosterid/:datatype
        var rosterid = this.multipleSelection[0].rosterid
        var datatype = this.multipleSelection[0].datatype
        var url = `valuelist/${rosterid}/${datatype}`
        this.$router.push(url);
      },
      importList() {

      },
      exportList() {

      }
    },
    data() {
      return {
        inline: true,
        listForm: {
          rosterdesc: "",
          datatype: "",
          rostertype: ""
        },
        gridData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        listDialogVisible: false,
        listDialogform: {
          rostername: "",
          rosterdesc: "",
          datatype: "",
          rostertype: "",
          iscache: "1",
          remark: "",
        },
        status: true,
        btnStatus: true,
        delBtnStatus: true,
        dialogTitle: '',
        formLabelWidth: '150px',
        rules: {
          rostername: [
            { required: true, message: '请输入名单英文名', trigger: 'blur' },
            { max: 32, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormEnSpecialCharacter, trigger: 'blur' }
          ],
          rosterdesc: [
            { required: true, message: '请输入名单名称', trigger: 'blur' },
            { max: 64, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          datatype: [
            { required: true, message: '请输入名单数据类型', trigger: 'blur' }
          ],
          rostertype: [
            { required: true, message: '请输入名单类型', trigger: 'blur' }
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
