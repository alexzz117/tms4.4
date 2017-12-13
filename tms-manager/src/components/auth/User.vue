<template>
  <div>
    <el-form label-position="right" label-width="80px" :model="userForm"
             :inline="inline" style="text-align: left">
      <el-form-item label="用户名" prop="login_name">
        <el-input v-model="userForm.login_name"></el-input>
      </el-form-item>
      <el-form-item label="姓名" prop="real_name">
        <el-input v-model="userForm.real_name"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="userForm.flag" placeholder="状态">
          <el-option label="全部" value="0"></el-option>
          <el-option label="停用" value="1"></el-option>
          <el-option label="正常" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="userForm.role" placeholder="角色">
          <el-option label="全部" value="0"></el-option>
          <el-option
            v-for="item in roles"
            :key="item.role_id"
            :label="item.role_name"
            :value="item.role_id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-button plain class="el-icon-edit" @click="openDialog('edit')">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="delUser">删除</el-button>
      <el-button plain class="el-icon-refresh" @click="resetPwd">密码重置</el-button>
      <el-button plain class="el-icon-setting" @click="resetFlag">用户解锁</el-button>
      <el-button class="el-icon-search" type="primary" @click="selUser">查询</el-button>
    </div>
    <el-table
      :data="roleData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="35" align="left"></el-table-column>
      <el-table-column prop="login_name" label="用户名" align="left" width="160"></el-table-column>
      <el-table-column prop="real_name" label="姓名" align="left" width="100"></el-table-column>
      <el-table-column prop="role_name" label="角色" align="left" width="120"></el-table-column>
      <el-table-column prop="flag" label="状态" align="left" width="50"></el-table-column>
      <el-table-column prop="failed_login_attempts" label="登录失败次数" align="left" width="95"></el-table-column>
      <el-table-column prop="lockout" label="锁定次数" align="left" width="75"></el-table-column>
      <el-table-column prop="lockout_date" label="锁定时间" align="left" width="120"></el-table-column>
      <el-table-column prop="email" label="电子邮件" align="left"></el-table-column>
      <el-table-column prop="mobile" label="移动电话" align="left" width="160"></el-table-column>
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
      <el-form :model="userDialogForm" :rules="rules" ref="userDialogForm" style=" max-height: 55vh;overflow-y: auto;">
        <el-form-item label="用户名" :label-width="formLabelWidth" prop="login_name">
          <el-input v-model="userDialogForm.login_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="状态" :label-width="formLabelWidth" style="text-align: left;">
          <el-radio v-model="userDialogForm.flag" label="1">正常</el-radio>
          <el-radio v-model="userDialogForm.flag" label="2">停用</el-radio>
        </el-form-item>
        <el-form-item label="姓名" :label-width="formLabelWidth" prop="real_name">
          <el-input v-model="userDialogForm.real_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="角色" :label-width="formLabelWidth" prop="role" style="text-align: left;">
          <el-select v-model="userDialogForm.role" placeholder="角色">
            <el-option label="全部" value="0"></el-option>
            <el-option
              v-for="item in roles"
              :key="item.role_id"
              :label="item.role_name"
              :value="item.role_id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="证件类型" :label-width="formLabelWidth" prop="credentialtype" style="text-align: left;">
          <el-select v-model="userDialogForm.credentialtype" placeholder="证件类型">
            <el-option label="身份证" value="11"></el-option>
            <el-option label="临时居民身份证" value="11"></el-option>
            <el-option label="军人身份证" value="12"></el-option>
            <el-option label="武装警察身份证" value="12"></el-option>
            <el-option label="港澳居民通行证" value="13"></el-option>
            <el-option label="台湾居民通行证" value="13"></el-option>
            <el-option label="护照" value="14"></el-option>
            <el-option label="其他证件" value="19"></el-option>
            <el-option label="港澳台居民往来内地通行证" value="13"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="证件号码" :label-width="formLabelWidth" prop="credentialnum">
          <el-input v-model="userDialogForm.credentialnum" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="固定电话" :label-width="formLabelWidth" prop="phone">
          <el-input v-model="userDialogForm.phone" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="移动电话" :label-width="formLabelWidth" prop="mobile">
          <el-input v-model="userDialogForm.mobile" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="电子邮件" :label-width="formLabelWidth" prop="email">
          <el-input v-model="userDialogForm.email" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="通讯地址" :label-width="formLabelWidth" prop="address">
          <el-input v-model="userDialogForm.address" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="备注信息" :label-width="formLabelWidth" prop="memo">
          <el-input type="textarea" v-model="userDialogForm.memo"></el-input>
        </el-form-item>
        <el-form-item label="密码1" :label-width="formLabelWidth" prop="repwd" style="display: none">
          <el-input v-model="userDialogForm.repwd" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="密码2" :label-width="formLabelWidth" prop="password" style="display: none">
          <el-input v-model="userDialogForm.password" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('userDialogForm')">保 存</el-button>
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
      this.selUser()
      this.selRole()
    },
    methods: {
      handleSizeChange(val) {
        console.log(`每页 ${val} 条`)
      },
      handleCurrentChange(val) {
        console.log(`当前页: ${val}`)
      },
      handleSelectionChange (val) {
        this.multipleSelection = val
      },
      openDialog (flag) {
        this.flag = flag
        if (flag === 'edit') {
          this.dialogTitle = '编辑用户'
          var length = this.multipleSelection.length
          if (length !== 1) {
            this.$message('请选择一行用户信息。')
            return
          }
          this.userDialogForm = this.multipleSelection[0]
        } else if (flag === 'add') {
          this.dialogTitle = '新建用户'
          this.userDialogForm = {
            operator_id: '',
            login_name: '',
            flag: '1',
            real_name: '',
            role: '',
            credentialtype: '',
            credentialnum: '',
            phone: '',
            mobile: '',
            email: '',
            address: '',
            memo: '',
            repwd: '123456',
            password: '123456'
          }
        }
        this.roleDialogVisible = true
      },
      submitForm(formName) {
        if (this.flag === 'add') {
          this.addUser()
        } else if (this.flag === 'edit') {
          this.updateUser()
        }
      },
      bindGridData(data) {
        this.roleData = data.page.list
        this.currentPage = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      selRole () {
        var self = this;
        ajax.post('/cmc/role/listNormalRole', null,function (data) {
          self.roles = data.row;
        })
      },
      selUser() {
        var self = this;
        ajax.post('/cmc/operator/list', {
          login_name: this.userForm.login_name,
          real_name: this.userForm.real_name,
          flag: this.userForm.flag,
          role: this.userForm.role,
          pageindex: 1,
          pagesize: 10
        }, function (data) {
          if (data.page) {
            self.bindGridData(data)
          }
        })
      },
      addUser() {
        var self = this
        ajax.post('/cmc/operator/add', this.userDialogForm, function (data) {
          self.$message('创建成功。')
          self.roleDialogVisible = false
          self.selUser()
        })
      },
      updateUser() {
        var self = this;
        ajax.post('/cmc/operator/mod', this.userDialogForm, function (data) {
          self.$message('更新成功。')
          self.roleDialogVisible = false
          self.selUser()
        })
      },
      delUser() {
        var self = this
        var length = this.multipleSelection.length
        if (length !== 1) {
          this.$message('请选择一行用户信息。')
          return
        }
        var data = this.multipleSelection[0]
        ajax.post('/cmc/operator/del', {
          operatorId: data.operator_id
        }, function (data) {
          self.$message('删除成功。')
          self.selUser()
        })
      },
      resetPwd() {
        var self = this
        var length = this.multipleSelection.length
        if (length !== 1) {
          this.$message('请选择一行用户信息。')
          return
        }
        var password = '123456';
        var pwd = password; // 需要MD5加密
        // var pwd = jcl.util.crypt.md5(password);

        var data = this.multipleSelection[0]
        ajax.post('/cmc/operator/reset', {
          operatorId: data.operator_id,
          passWord: pwd
        }, function (data) {
          self.$message('密码重置成功。')
          self.selUser()
        })
      },
      resetFlag() {
        var self = this
        var length = this.multipleSelection.length
        if (length !== 1) {
          this.$message('请选择一行用户信息。')
          return
        }
        var data = this.multipleSelection[0]
        ajax.post('/cmc/operator/resetLoginFailedAttempts', {
          operatorId: data.operator_id
        }, function (data) {
          self.$message('用户解锁成功。')
          self.selUser()
        })
      }
    },
    data() {
      var UserNameExist = (rule, value, callback) => {
        if (this.flag === 'edit' && this.multipleSelection[0].login_name === value) {
          callback()
        } else {
          var params = {"username": value};
          console.info("value",params)
          ajax.post('/cmc/operator/check/username', params, function (data) {
            if (data.checkresult === false) {
              return callback(new Error('用户名已被占用'))
            } else {
              callback()
            }
          })
        }
      }

      return {
        inline: true,
        roles: [],
        userForm: {
          login_name: '',
          real_name: '',
          flag: '0',
          role: '0'
        },
        roleData: [{
          "operator_id": '123123123123123123',
          "login_name": '测试假用户',
          "real_name": "王某某",
          "role_name": "tms系统管理员",
          "flag": "1",
          "failed_login_attempts": '0',
          "lockout": '0',
          "lockout_date": new Date().toLocaleDateString(),
          "email": '123@qwe.com',
          "mobile": '13366666666'
        }],
        currentPage: 4,
        pagesize: 10,
        total: 100,
        roleDialogVisible: false,
        userDialogForm: {
          operator_id: '',
          login_name: '',
          flag: '1',
          real_name: '',
          role: '',
          credentialtype: '',
          credentialnum: '',
          phone: '',
          mobile: '',
          email: '',
          address: '',
          memo: '',
          repwd: '123456',
          password: '123456'
        },
        dialogTitle: '',
        formLabelWidth: '100px',
        rules: {
          login_name: [
            {required: true, message: '请输入用户名', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'},
            {validator: UserNameExist, trigger: 'blur'}
          ],
          real_name: [
            {required: true, message: '请输入姓名', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'}
          ],
          role: [
            {required: true, message: '请选择角色', trigger: 'blur'}
          ],
          mobile: [
            {required: true, message: '请输入移动电话', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'}
          ],
          email: [
            {required: true, message: '请输入电子邮件', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'}
          ],
          memo: [
            {max: 200, message: '长度在200个字符以内', trigger: 'blur'}
          ]
        },
        multipleSelection: [],
        flag: ''
      }
    }
  }
</script>

<style>

</style>
