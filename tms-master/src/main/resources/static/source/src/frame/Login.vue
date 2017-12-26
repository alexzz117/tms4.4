<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" label-width="80px" :rules="rules">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="loginForm.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="loginForm.password" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">登录</el-button>
        <el-button @click="resetForm('loginForm')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>

</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'

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
        }
      }
    },
    methods: {
      onSubmit() {
        var param = {
          username: this.loginForm.username,
          password: util.crypt.md5(this.loginForm.password)
        }

        ajax.post({
          url: '/cmc/login',
          param: param,
          success: function (data) {
//            this.$message('登录成功。')
//            console.log(data)
            sessionStorage.setItem("userToken",'1111')
            sessionStorage.setItem("loginUser",'bbb')
            this.$router.push({ name: '/'})
          }
        })
      },
      resetForm(formName) {
        this.$refs[formName].resetFields();
      }
    }
  }
</script>

<style>
.login{
  margin: 0 auto;
  width: 30%;
}
</style>
