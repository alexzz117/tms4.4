<template>
  <div>
    <el-form label-position="right" label-width="80px" :model="roleForm"
             :inline="inline" style="text-align: left">
      <el-form-item label="角色名称">
        <el-input v-model="roleForm.role_name"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="roleForm.flag" @focus="selectFocus()" placeholder="请选择" :clearable="clearable">
          <el-option
            v-for="item in flagOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-search" type="primary" @click="selRole">查询</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="delRole">删除</el-button>
      <el-button plain class="el-icon-setting" @click="grant">功能授权</el-button>
    </div>

    <el-table
      :data="roleData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="role_name" label="角色名称" align="left" width="180"></el-table-column>
      <el-table-column prop="flag" label="状态" align="left" width="180"></el-table-column>
      <el-table-column prop="info" label="描述信息" align="left"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="currentPage"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>

    <el-dialog :title="dialogTitle" :visible.sync="roleDialogVisible">
      <el-form :model="roleDialogform" :rules="rules" ref="roleDialogform">
        <el-form-item label="角色名称" :label-width="formLabelWidth" prop="role_name">
          <el-input v-model="roleDialogform.role_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="状态" :label-width="formLabelWidth" style="text-align: left;">
          <el-radio v-model="roleDialogform.flag" label="1">正常</el-radio>
          <el-radio v-model="roleDialogform.flag" label="2">停用</el-radio>
        </el-form-item>
        <el-form-item label="描述信息" :label-width="formLabelWidth" prop="info">
          <el-input type="textarea" v-model="roleDialogform.info"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('roleDialogform')">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import axios from 'axios'
  import util from '@/common/util'
  import ajax from '@/common/ajax'

  export default {
    created () {
      this.selRole()
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
          this.dialogTitle = '编辑角色'
          var length = this.multipleSelection.length
          if (length !== 1) {
            this.$message('请选择一行角色信息。')
            return
          }
          this.roleDialogform = util.extend({}, this.multipleSelection[0])
        } else if (flag === 'add') {
          this.dialogTitle = '新建角色'
          if (this.$refs['roleDialogform']) {
            this.$refs['roleDialogform'].resetFields();
          } else {
            this.roleDialogform = {
              role_name: '',
              rosterdesc: "",
              datatype: "",
              rostertype: "",
              iscache: "1",
              remark: "",
            }
          }
        }

        } else if (flag === 'add') {
          this.dialogTitle = '新建角色'
          this.roleDialogform = {
            ROLE_NAME: '',
            FLAG: '1',
            INFO: ''
          }
        }
        this.roleDialogVisible = true
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      submitForm(formName) {
        if (this.flag === 'add') {
          this.addRole()
        } else if (this.flag === 'edit') {
          this.updateRole()
        }
//        this.$refs[formName].validate((valid) => {
//          console.log(valid)
//          this.addRole()
//          if (valid) {
//            if (this.flag === 'add') {
//              this.addRole()
//            } else if (this.flag === 'edit') {
//              this.updateRole()
//            }
//          } else {
//            console.log('error submit!!');
//            return false;
//          }
//        })
      },
      bindGridData(data) {
        this.roleData = data.page.list
        this.currentPage = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      addRole() {
        var self = this
        ajax.post('/cmc/role/add', this.roleDialogform, function (data) {
          self.$message('创建成功。')
          self.roleDialogVisible = false
          self.selRole()
        })
      },
      delRole() {
        var self = this
        var data = this.multipleSelection[0]
        ajax.post('/cmc/role/del', {
          roleId: data.ROLE_ID
        }, function (data) {
          self.$message('删除成功。')
          self.selRole()
        })
      },
      selRole() {
        var self = this;
        ajax.post('/cmc/role/list', {
          pageindex:1,
          pagesize:10
        }, function (data) {
          if (data.page) {
            self.bindGridData(data)
          }
        })
      },
      updateRole() {
        var self = this;
        ajax.post('/cmc/role/mod', this.roleDialogform, function (data) {
          self.$message('更新成功。')
          self.roleDialogVisible = false
          self.selRole()
        })
      },
      grant() {

      },
      selectFocus () {
        this.flagOptions = dictCode.getCodeItems('cmc.cmc_role.flag')
      }
    },
    data() {
      var checkSpecialCode = (rule, value, callback) => {
        if (!this.checkSpecialCode(value)){
          return callback(new Error('请不要输入特殊字符！如($,%)'));
        }
      };

      var roleNameExist = () => {
        axios.post('/cmc/role/checkUserName', this.roleDialogform)
          .then(function (data) {
            if('false'==data.checke_result){
              return callback(new Error('请不要输入特殊字符！如($,%)'));
            }else{
              this.$message({
                message: '角色名称可以使用',
                type: 'success'
              });
            }
          })
          .catch(function (error) {
            console.log(error);
          });
      }
      return {
        inline: true,
        roleForm: {
          ROLE_NAME:''
        },
        roleData: [
          {
            "SPLIT_ROWS_NUM": 1,
            "ROLE_ID": "26D20B7B9533435098FAC7B4B0DE8DF4",
            "ROLE_NAME": "tms系统管理员",
            "FLAG": "1",
            "INFO": null
          }
        ],
        currentPage: 4,
        pagesize: 10,
        total: 100,
        roleDialogVisible: false,
        roleDialogform: {
          ROLE_NAME: '',
          FLAG: '1',
          INFO: ''
        },
        dialogTitle: '',
        formLabelWidth: '100px',
        rules: {
          ROLE_NAME: [
            { required: true, message: '请输入角色名称', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: util.checkSpecialCode, trigger: 'blur' }
//            ,
//            { validator: roleNameExist, trigger: 'blur' }
          ],
          INFO: [
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: util.checkSpecialCode, trigger: 'blur' }
          ]
        },
        multipleSelection: [],
        flag: '',
        flagOptions: []
      }
    }
  }
</script>
