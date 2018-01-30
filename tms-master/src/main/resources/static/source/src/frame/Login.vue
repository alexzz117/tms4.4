<template>
  <div class="login-bg">
    <!--<header class="sso-header">-->
    <!--<a href="http://www.lagou.com" class="logo ">-->
    <!--</a>-->
    <!--</header>-->

    <section class="content-box">
      <div class="form-head">登录</div>
      <el-form ref="loginForm" :model="loginForm" :rules="rules" size="large">
        <el-form-item label="" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="" prop="password">
          <el-input type="password" v-model="loginForm.password" auto-complete="off"
                    placeholder="请输入密码" @keyup.enter.native="pressKey"></el-input>
        </el-form-item>
        <el-form-item label="">
          <el-checkbox v-model="checked">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm('loginForm')" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  //  import '../assets/css/login.css';

  export default {
    data() {
      return {
        loginForm: {
          username: '',
          password: ''
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' }
          ]
        },
        checked: true
      }
    },
    methods: {
      submitForm(formName) {
        var self = this
        this.$refs[formName].validate((valid) => {
          if (valid) {
            self.login()
          } else {
            return false;
          }
        })
      },
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      login () {
        var self = this
        var param = {
          username: this.loginForm.username,
          password: util.crypt.md5(this.loginForm.password)
        }
        ajax.post({
          url: '/login',
          param: param,
          success: function (data) {
            sessionStorage.setItem('userName', self.loginForm.username)
            self.$router.push({ name: 'main'})
          }
        })
      },
      pressKey (e) {
        if (e.keyCode === 13) {
          this.submitForm('loginForm')
        }
      }
    }
  }
</script>

<style>
  .content-box {
    width: 400px;
    height: 330px;
    padding: 62px 70px 38px 78px;
    background-color: #fff;
    border-radius: 2px;
    border: 1px solid #dce1e6;
    color: #519CE0;
    border-radius: .25rem;
  }

  .form-head{
    height: 33px;
  }

  .login-bg{
    width: 100%;
    height: 100%;
    background-color: #FAFAFA;
    display: flex;
    justify-content: center;
    align-items: center;
    /*background-repeat: no-repeat;*/
    /*background-image: url("../assets/images/login_bg.jpg");*/
    /*background-position: center;*/
  }
</style>
