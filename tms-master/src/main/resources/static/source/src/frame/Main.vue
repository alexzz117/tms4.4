<template>
  <el-container style="height: 100%; border: 1px solid #eee" class="wrapper">
    <el-container>
      <el-aside width="220px" style="overflow:hidden;height: 100%; padding-top:60px; box-sizing: border-box;">
        <div class="title">TMS</div>
        <div class="scroll" style="overflow: auto; height: 100%; margin: 60px 0px 10px 0px">
          <el-menu  @select="handleSelect">
            <el-submenu v-for="(menu, index) in menus" :key="menu.id" :index= "index.toString()">
              <template slot="title"><i class="el-icon-message"></i>{{ menu.text }}</template>

              <!--<el-menu-item v-for="submenu in menu.children" :key="submenu.id">{{ submenu.text }}</el-menu-item>-->

              <!--<div v-for="(submenu, subindex) in menu.children" :key="submenu.id" :index= "subindex.toString()">-->
              <div v-for="(submenu, subindex) in menu.children">
                <el-submenu v-if="submenu.children && submenu.children.length > 0" :key="submenu.id" :index= "subindex.toString()">
                  <template slot="title">{{ submenu.text }}</template>
                  <el-menu-item :index="index.toString() + '-' + subindex.toString()">{{ submenu.text }}</el-menu-item>
                </el-submenu>

                <router-link v-else :to="{ name: submenu.conf }" tag="li" replace>
                  <el-menu-item :index="index.toString() + '-' + subindex.toString()">{{ submenu.text }}</el-menu-item>
                </router-link>
              </div>
            </el-submenu>
          </el-menu>
        </div>
      </el-aside>
      <el-main class="scroll">
        <div class="toolbar">
          <div class="page-title">{{ modeName }}</div>
          <el-dropdown style="float: right; margin-right: 20px" @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userName }}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown" style="float: none">
              <el-dropdown-item command="openDialog"><span class="el-icon-setting"></span> 修改密码</el-dropdown-item>
              <el-dropdown-item command="logout"><span class="el-icon-circle-close"></span> 退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <!--用router-view渲染视图-->
        <router-view/>
      </el-main>
      <el-dialog title="修改密码" :visible.sync="updPassDialogVisible" width="35%" :close-on-click-modal="closeOnClickModal">
        <el-form :model="passwordDialogform" :rules="rules" ref="passwordDialogform">
          <el-form-item label="旧密码" :label-width="formLabelWidth" prop="old_pwd">
            <el-input type="password" v-model="passwordDialogform.old_pwd" auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="新密码" :label-width="formLabelWidth" prop="new_pwd">
            <el-input type="password" v-model="passwordDialogform.new_pwd" auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" :label-width="formLabelWidth" prop="r_new_pwd">
            <el-input type="password" v-model="passwordDialogform.r_new_pwd" auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="" :label-width="formLabelWidth">
            <el-button type="primary" @click="submitForm('passwordDialogform')">保 存</el-button>
            <el-button @click="updPassDialogVisible = false">取 消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

    </el-container>
  </el-container>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import check from '@/common/check'

  export default {
    name: 'items',
    created () {
      this.Hub.$on('changeModeName', (modeName) => { //Hub接收事件
        this.modeName = modeName;
      });
      this.getMeuns()
      this.userName = sessionStorage.getItem('userName')
    },
    data() {
      var self = this
      var checkSame = function (rule, value, callback) {
        if (self.passwordDialogform.new_pwd !== self.passwordDialogform.r_new_pwd) {
          return callback(new Error('两次输入的新密码不一致'));
        }
        callback()
      }

      var checkOldNewNotSame = function (rule, value, callback) {
        if (self.passwordDialogform.old_pwd === self.passwordDialogform.new_pwd) {
          return callback(new Error('新密码与旧密码不能相同'));
        }
        callback()
      }

      var checkStrength = function (rule, value, callback) {
        if (check.checkStrength(value) < 2) {
          return callback(new Error('密码要同时包括数字和字母'));
        }
        callback()
      }

      return {
        funData: [],
        menus: [],
        modeName: "",
        userName: "",
        closeOnClickModal: false,
        formLabelWidth: '100px',
        updPassDialogVisible: false,
        passwordDialogform: {
          old_pwd: "",
          new_pwd: "",
          r_new_pwd: "",
        },
        rules: {
          old_pwd: [
            { required: true, message: '请输入旧密码', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' }
          ],
          new_pwd: [
            { required: true, message: '请输入新密码', trigger: 'blur' },
            { min: 6, message: '密码不能少于6位', trigger: 'blur' },
            { max: 20, message: '密码不能大于20位', trigger: 'blur' },
            { validator: checkOldNewNotSame, trigger: 'blur' },
            { validator: checkSame, trigger: 'blur' },
            { validator: checkStrength, trigger: 'blur' }

          ],
          r_new_pwd: [
            { required: true, message: '请输入确认新密码', trigger: 'blur' },
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: checkSame, trigger: 'blur' }
          ]
        },
      };
    },
    methods: {
      handleSelect(key, keyPath) {
        let pathArr = key.split('-')
        let menu = {}
        for (let i = 0 ; i < pathArr.length; i++) {
          if (i === 0) {
            menu = this.menus[pathArr[i]]
          } else {
            menu = menu.children[pathArr[i]]
          }
        }
        this.modeName = menu.text
      },
      getMeuns () {
        let self = this
        ajax.post({
          url: '/menu',
          param: {},
          success: function (data) {
            if (util.isArray(data.list) && data.list.length > 0) {
              self.menus = util.formatTreeData(data.list)
            }
          }
        })
      },
      handleCommand (command) {
        switch (command)
        {
          case 'openDialog':
            return this.updPassDialogVisible = true
            break;
          case 'logout':
            return this.logout()
            break;
          default:
            break;
        }
      },
      updatePassword () {
        let param = Object.assign({}, this.passwordDialogform, {
          old_password: util.crypt.md5(this.passwordDialogform.old_pwd),
          new_password: util.crypt.md5(this.passwordDialogform.new_pwd)
        })
        ajax.post({
          url: '/profile/pwd/update',
          param: param,
          success: function (data) {
            self.$message({
              type: 'success',
              message: '修改成功'
            })
          }
        })
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.updatePassword()
          } else {
            return false;
          }
        })
      },
      logout () {
        debugger
        this.$router.push({ name: 'login'})
        ajax.post({
          url: '/logout'
        })

      }
    }
  };
</script>

<style>
  .el-header {
    background-color: #F0F1F4;
    color: #333;
    line-height: 70px;
    margin-bottom: 20px;
    height: 70px;
    text-align: right;
    font-size: 12px;
  }

  .el-aside {
    color: #333;
    border: 1px solid rgb(238, 241, 246);
    background-color: #f0f1f4
  }

  .el-menu {
    border-right: none;
    background-color: #f0f1f4;
  }

  .el-main {
    background-color: #FAFAFA;
  }

  .page-title {
    font-size: 20px;
    float: left;
  }

  .title {
    font-size: 20px;
    height: 60px;
    line-height: 60px;
    margin: 20px auto;
    width: 220px;
    position:absolute;
    top:0;
    left:0;
  }

  /*工具栏及表单样式设置*/
  .toolbar {
    margin-bottom: 20px;
    text-align: left;
    background-color: #F0F1F4;
    height: 60px;
    line-height: 60px;
    padding-left: 20px;
    border-radius: 7px;
  }

  .toolbar .toolbar-form {
    text-align: left;
    float: right;
    display: inline-block;
  }

  .toolbar .toolbar-form .el-form-item__content {
    line-height: 60px;
  }

  .table {
    padding: 20px;
    background-color: #FFFFFF;
    border: 1px solid rgba(112, 112, 112, 0.12);
    border-radius: 7px;
  }

  .el-dialog__body {
    padding: 30px 30px 30px 20px;
  }

  .el-form textarea {
    resize: none;
  }

  .section {
    padding: 20px;
    background-color: #FFFFFF;
    border: 1px solid rgba(112, 112, 112, 0.12);
    border-radius: 7px;
    height:100%;
    overflow: auto
  }
</style>
