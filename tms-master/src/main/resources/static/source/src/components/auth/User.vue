<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div class="toolbar">
          <div style="float: left ">
            <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
          </div>
          <div style="float: right;">
            <el-form label-position="right" label-width="80px" :model="userForm"
                     :inline="inline" class="toolbar-form">
              <el-form-item label="用户名" prop="login_name">
                <el-input v-model="userForm.login_name"></el-input>
              </el-form-item>
              <!--<el-form-item label="姓名" prop="real_name">
                <el-input v-model="userForm.real_name"></el-input>
              </el-form-item>-->
              <el-form-item label="状态">
                <el-select v-model="userForm.flag" clearable placeholder="状态">
                  <el-option label="全部" value=""></el-option>
                  <el-option
                    v-for="item in flagList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </el-form-item>
              <!--<el-form-item label="角色">
                <el-select v-model="userForm.role" clearable placeholder="角色">
                  <el-option label="全部" value=""></el-option>
                  <el-option
                    v-for="item in roles"
                    :key="item.role_id"
                    :label="item.role_name"
                    :value="item.role_id">
                  </el-option>
                </el-select>
              </el-form-item>-->
              <el-form-item>
                <el-button class="el-icon-search" type="primary" @click="selUser">查询</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
    <section class="table">
      <el-table
        :data="roleData"
        @selection-change="handleSelectionChange">
        <el-table-column label="操作" width="120px">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" title="编辑" @click="openDialog('edit',scope.$index, scope.row)"></el-button>
            <el-button type="text" icon="el-icon-delete"  title="删除" @click="delUser(scope.$index, scope.row)"></el-button>
            <el-button type="text" icon="el-icon-refresh" title="重置密码" @click="resetPwd(scope.$index, scope.row)"></el-button>
            <el-button type="text" icon="el-icon-circle-check-outline" title="解锁" @click="resetFlag(scope.$index, scope.row)" :disabled="scope.row.flag !== '0'"></el-button>
          </template>
        </el-table-column>
        <el-table-column prop="login_name" label="用户名" align="left"></el-table-column>
        <el-table-column prop="real_name" label="姓名" align="left"></el-table-column>
        <el-table-column prop="role_name" label="角色" align="left"></el-table-column>
        <el-table-column prop="flag" label="状态" align="left" :formatter=renderFlag></el-table-column>
        <el-table-column prop="failed_login_attempts" label="登录失败次数" align="left" :formatter="emptyCheck"></el-table-column>
        <el-table-column prop="lockout" label="锁定次数" align="left" :formatter="emptyCheck"></el-table-column>
        <el-table-column prop="lockout_date" label="锁定时间" align="left"></el-table-column>
        <el-table-column prop="email" label="电子邮件" align="left"></el-table-column>
        <el-table-column prop="mobile" label="移动电话" align="left"></el-table-column>
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
    </section>
    <el-dialog :title="dialogTitle" :visible.sync="roleDialogVisible" width="900px">
      <el-form :model="userDialogForm" :rules="rules" ref="userDialogForm" :inline="true">
        <el-form-item label="用户名" :label-width="formLabelWidth" prop="login_name" :style="formItemStyle">
          <el-input v-model="userDialogForm.login_name" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="姓名" :label-width="formLabelWidth" prop="real_name" :style="formItemStyle">
          <el-input v-model="userDialogForm.real_name" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="角色" :label-width="formLabelWidth" prop="role" style="text-align: left;" :style="formItemStyle">
          <el-select v-model="userDialogForm.role" clearable placeholder="角色" :style="formItemContentStyle">
            <el-option
              v-for="item in roles"
              :key="item.role_id"
              :label="item.role_name"
              :value="item.role_id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" :label-width="formLabelWidth" style="text-align: left;" :style="formItemStyle">
          <el-radio v-model="userDialogForm.flag" label="1">正常</el-radio>
          <el-radio v-model="userDialogForm.flag" label="2">停用</el-radio>
        </el-form-item>
        <el-form-item label="证件类型" :label-width="formLabelWidth" prop="credentialtype" style="text-align: left;" :style="formItemStyle">
          <el-select v-model="userDialogForm.credentialtype" clearable placeholder="证件类型" :style="formItemContentStyle">
            <el-option
              v-for="item in credentialList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="证件号码" :label-width="formLabelWidth" prop="credentialnum" :style="formItemStyle">
          <el-input v-model="userDialogForm.credentialnum" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="固定电话" :label-width="formLabelWidth" prop="phone" :style="formItemStyle">
          <el-input v-model="userDialogForm.phone" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="移动电话" :label-width="formLabelWidth" prop="mobile" :style="formItemStyle">
          <el-input v-model="userDialogForm.mobile" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="电子邮件" :label-width="formLabelWidth" prop="email" :style="formItemStyle">
          <el-input v-model="userDialogForm.email" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="通讯地址" :label-width="formLabelWidth" prop="address" :style="formItemStyle">
          <el-input v-model="userDialogForm.address" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="备注信息" :label-width="formLabelWidth" prop="memo" :style="formItemStyle">
          <el-input type="textarea" v-model="userDialogForm.memo" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="密码1" :label-width="formLabelWidth" prop="repwd" style="display: none" :style="formItemStyle">
          <el-input v-model="userDialogForm.repwd" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>
        <el-form-item label="密码2" :label-width="formLabelWidth" prop="password" style="display: none" :style="formItemStyle">
          <el-input v-model="userDialogForm.password" auto-complete="off" :style="formItemContentStyle"></el-input>
        </el-form-item>

        <div>
          <el-form-item label=" " :label-width="formLabelWidth" :style="formItemStyle">
            <el-button type="primary" @click="submitForm('userDialogForm')" size="large">保 存</el-button>
            <el-button @click="roleDialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
    </el-dialog>
  </div>
</template>

<script>
  import check from '@/common/check'
  import ajax from '@/common/ajax'
  import crypt from '@/common/crypt'
  import dictCode from '@/common/dictCode'

  export default {
    created () {
      this.selUser()
      this.selRole()
      this.credentialList = dictCode.getCodeItems('tms.certificate.type')
      this.flagList = dictCode.getCodeItems('cmc.cmc_operate.flag')
    },
    methods: {
      handleSizeChange (val) {
        this.currentPage = 1
        this.pageSize = val
        this.selUser()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.selUser()
      },
      handleSelectionChange (val) {
        this.multipleSelection = val
      },
      renderFlag (row, column, cellValue) {
        return dictCode.rendCode('cmc.cmc_operate.flag', cellValue)
      },
      resetForm (formName) {
        this.$refs[formName].resetFields()
      },
      submitForm (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            if (this.flag === 'add') {
              this.addUser()
            } else if (this.flag === 'edit') {
              this.updateUser()
            }
          } else {
            // this.$message('请正确填写用户信息')
            return false
          }
        })
      },
      bindGridData (data) {
        this.roleData = data.page.list
        this.currentPage = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      selRole () {
        var self = this
        ajax.post({
          url: '/role/listNormalRole',
          success: function (data) {
            self.roles = data.row
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      selUser () {
        var self = this
        ajax.post({
          url: '/operator/list',
          param: {
            login_name: this.userForm.login_name,
            real_name: this.userForm.real_name,
            flag: this.userForm.flag,
            role: this.userForm.role,
            pageindex: self.currentPage,
            pagesize: self.pagesize
          },
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      openDialog (flag, index, row) {
        let self = this
        this.flag = flag
        if (flag === 'edit') {
          this.dialogTitle = '编辑用户'
          ajax.post({
            url: '/operator/get',
            param: {operatorId: row.operator_id},
            success: function (data) {
              var info = data.row
              self.multipleSelection = [info]
              self.userDialogForm = {
                operator_id: info.operator_id,
                login_name: info.login_name,
                flag: info.flag,
                real_name: info.real_name,
                role: info.role,
                credentialtype: info.credentialtype,
                credentialnum: info.credentialnum,
                phone: info.phone,
                mobile: info.mobile,
                email: info.email,
                address: info.address,
                memo: info.memo,
                repwd: info.password,
                password: info.password
              }
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
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
            repwd: crypt.md5('123456'),
            password: crypt.md5('123456')
          }
        }
        this.roleDialogVisible = true
        if (this.$refs['userDialogForm']) {
          this.$refs['userDialogForm'].clearValidate()
        }
      },
      addUser () {
        var self = this
        ajax.post({
          url: '/operator/add',
          param: this.userDialogForm,
          success: function (data) {
            self.$message.success('创建成功。')
            self.roleDialogVisible = false
            self.selUser()
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      updateUser () {
        var self = this
        ajax.post({
          url: '/operator/mod',
          param: this.userDialogForm,
          success: function (data) {
            self.$message.success('编辑成功。')
            self.roleDialogVisible = false
            self.selUser()
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      delUser (index, row) {
        var self = this
        this.$confirm('确定删除', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/operator/del',
            param: [row.operator_id],
            success: function (data) {
              self.$message.success('删除成功。')
              self.selUser()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {})
      },
      resetPwd (index, row) {
        var self = this
        var pwd = crypt.md5('123456') // MD5加密
        this.$confirm('确定重置密码', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/operator/reset',
            param: {
              operatorId: row.operator_id,
              loginName: row.login_name,
              passWord: pwd
            },
            success: function (data) {
              self.$message.success('密码重置成功。')
              self.selUser()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {})
      },
      resetFlag (index, row) {
        var self = this
        this.$confirm('确定解锁？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/operator/resetLoginFailedAttempts',
            param: {
              operatorId: row.operator_id
            },
            success: function (data) {
              if (data.result) {
                self.$message.success('解锁成功。')
              } else {
                self.$message.success('解锁失败。')
              }
              self.selUser()
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {})
      },
      emptyCheck (row, item) {
        let val = row[item.property]
        return val || 0
      }
    },
    data () {
      // 用户名格式校验 只能是长度为6的字母和数字组成
      var UserNameCheck = (rule, value, callback) => {
        var type = /^[0-9a-zA-Z]*$/
        if (type.test(value)) {
          callback()
        } else {
          return callback(new Error('用户名只能由6-50位数字或字母组成'))
        }
      }
      // 用户名重复校验
      var UserNameExist = (rule, value, callback) => {
        let self = this
        if (this.flag === 'edit' && this.multipleSelection[0].login_name === value) {
          callback()
        } else {
          ajax.post({
            url: '/operator/check/username',
            param: {'username': value}, // 用户名
            success: function (data) { // 请求校验用户名
              debugger
              if (data.checke_result === 'false') {
                return callback(new Error('用户名已被占用'))
              } else {
                callback()
              }
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }
      }
      // 证件号码格式校验 证件号码只能有数字和字母组成
      var credentialNumCheck = (rule, value, callback) => {
        var type = /^[0-9a-zA-Z]*$/
        if (type.test(value)) {
          callback()
        } else {
          return callback(new Error('证件号码只能有数字和字母组成'))
        }
      }
      // 证件类型输入校验
      var credentialTypeInputCheck = (rule, value, callback) => {
        var item = this
        if (value.trim() === '' && item.userDialogForm.credentialnum !== '') { // 证件类型为空，证件号不为空的处理
          return callback(new Error('请选择证件类型'))
        } else if (value.trim() !== '' && item.userDialogForm.credentialnum !== '') { // 证件类型和证件号都为空时，校验一次证件号码
          // item.$refs.userDialogForm.validateField('credentialnum')
          callback()
        } else {
          callback()
        }
      }
      // 证件号码输入校验
      var credentialNumInputCheck = (rule, value, callback) => {
        var item = this
        if (value.trim() === '' && item.userDialogForm.credentialtype !== '') { // 证件号为空，证件类型不为空处理
          return callback(new Error('证件号码不能为空'))
        } else if (value.trim() !== '') { // 证件号不为空的处理
          var credentialNum = item.userDialogForm.credentialtype.toString() // 证件号码
          if (credentialNum === '0') { // 证件类型为身份证的处理
            var num = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[A-Z])$/
            if (!num.test(credentialNum)) {
              return callback(new Error('身份证号码格式错误'))
            }
          }
          item.$refs.userDialogForm.validateField('credentialtype')
          callback()
        } else {
          callback()
        }
      }
      var phoneCheck = (rule, value, callback) => {
        var type = /^(([0+]\d{2,3}-)?(0\d{2,3})-)?(\d{7,8})(-(\d{3,}))?$/
        if ((value.trim() === '') || (type.test(value))) {
          callback()
        } else {
          return callback(new Error('固定电话号码不正确'))
        }
      }
      var mobileCheck = (rule, value, callback) => {
        var type = /(^13\d{9}$)|(^15\d{9}$)|(^18\d{9}$)/
        if (type.test(value)) {
          callback()
        } else {
          return callback(new Error('移动电话号码不正确'))
        }
      }

      return {
        formItemStyle: {
          width: '400px'
        },
        formItemContentStyle: {
          width: '250px'
        },
        inline: true,
        roles: [],
        credentialList: [],
        flagList: [],
        userForm: {
          login_name: '',
          real_name: '',
          flag: '',
          role: ''
        },
        roleData: [],
        currentPage: 1,
        pagesize: 10,
        total: 0,
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
            {min: 6, max: 50, message: '用户名只能由6-50位数字或字母组成', trigger: 'blur'},
            {validator: UserNameCheck, trigger: 'blur'}, // 用户名格式校验
            {validator: UserNameExist, trigger: 'blur'} // 用户名重复校验
          ],
          real_name: [
            {required: true, message: '请输入姓名', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'} // 特殊字符校验
          ],
          role: [
            {required: true, message: '请选择角色', trigger: 'blur'}
          ],
          credentialtype: [
            {validator: credentialTypeInputCheck, trigger: 'blur'} // 证件类型输入校验
          ],
          credentialnum: [
            {max: 50, message: '长度在32个字符以内', trigger: 'blur'},
            {validator: credentialNumCheck, trigger: 'blur'}, // 证件号码格式校验
            {validator: credentialNumInputCheck, trigger: 'blur'} // 证件号码输入校验
          ],
          phone: [
            {validator: phoneCheck, trigger: 'blur'} // 校验电话号码格式,,
          ],
          mobile: [
            {required: true, message: '请输入移动电话', trigger: 'blur'},
            {validator: mobileCheck, trigger: 'blur'} // 校验移动电话号码格式
          ],
          email: [
            {required: true, message: '请输入电子邮件', trigger: 'blur'},
            {type: 'email', message: '请输入正确的电子邮件', trigger: 'blur'},
            {max: 50, message: '长度在50个字符以内', trigger: 'blur'}
          ],
          address: [
            {max: 200, message: '长度在200个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'} // 特殊字符校验
          ],
          memo: [
            {max: 300, message: '长度在300个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'} // 特殊字符校验
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
