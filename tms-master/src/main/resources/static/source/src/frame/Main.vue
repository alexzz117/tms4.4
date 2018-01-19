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
          <!--<el-dropdown-menu slot="dropdown">-->
          <!--<el-dropdown-item><i class="el-icon-setting" style="margin-right: 5px"></i>修改密码</el-dropdown-item>-->
          <!--<el-dropdown-item><i class="el-icon-circle-close" style="margin-right: 5px"></i>退出</el-dropdown-item>-->
          <!--</el-dropdown-menu>-->
        </div>
        <!--用router-view渲染视图-->
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'

  export default {
    name: 'items',
    created () {
      this.Hub.$on('changeModeName', (modeName) => { //Hub接收事件
        this.modeName = modeName;
      });
      this.getMeuns()
    },
    data() {
      return {
        funData: [],
        menus: [],
        modeName: ""
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
          url: '/func/tree',
          param: {},
          success: function (data) {
            if (data.list) {
              self.menus = util.formatTreeData(data.list)
            }
          }
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
