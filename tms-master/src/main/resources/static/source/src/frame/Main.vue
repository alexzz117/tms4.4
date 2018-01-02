<template>
  <el-container style="height: 100%; border: 1px solid #eee" class="wrapper">
    <el-container>
      <el-aside width="220px" style="overflow: hidden">
        <div class="title">TMS</div>
        <div class="scroll" style="overflow: auto; height: 550px;">
          <el-menu :default-openeds="['1', '3', '4']" >
            <el-submenu index="1">
              <template slot="title"><i class="el-icon-message"></i>运行管理</template>
              <router-link :to="{ name:'txn'}" tag="li" replace>
                <el-menu-item index="1-1">交易模型</el-menu-item>
              </router-link>
              <router-link :to="{ name:'list'}" tag="li" replace>
                <el-menu-item index="1-2">名单管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'namelist'}" tag="li" replace>
                <el-menu-item index="1-3">名单查看</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="3">
              <template slot="title"><i class="el-icon-setting"></i>权限管理</template>
              <router-link :to="{ name:'user'}" tag="li" replace>
                <el-menu-item index="3-1">操作员管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'role'}" tag="li" replace>
                <el-menu-item index="3-2">角色管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'func'}" tag="li" replace>
                <el-menu-item index="3-3">功能管理</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="4">
              <template slot="title"><i class="el-icon-setting"></i>系统管理</template>
              <router-link :to="{ name:'codedict'}" tag="li" replace>
                <el-menu-item index="4-1">数据字典管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'ipaddr'}" tag="li" replace>
                <el-menu-item index="4-2">IP地址维护</el-menu-item>
              </router-link>
              <router-link :to="{ name:'authCenter'}" tag="li" replace>
                <el-menu-item index="4-3">授权中心</el-menu-item>
              </router-link>
              <router-link :to="{ name:'log'}" tag="li" replace>
                <el-menu-item index="4-4">操作日志管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'time'}" tag="li" replace>
                <el-menu-item index="4-5">定时任务管理</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="5">
              <template slot="title"><i class="el-icon-setting"></i>综合查询</template>
              <router-link :to="{ name:'txnEventQuery'}" tag="li" replace>
                <el-menu-item index="5-1">交易事件查询</el-menu-item>
              </router-link>
              <router-link :to="{ name:'alarmEventQuery'}" tag="li" replace>
                <el-menu-item index="5-2">报警事件查询</el-menu-item>
              </router-link>
            </el-submenu>
          </el-menu>
        </div>
      </el-aside>
      <el-main class="scroll">
        <header class="el-header">
          <span class="page-title">名单管理</span>
          <el-dropdown>
        <span class="el-dropdown-link">
        admin<i class="el-icon-arrow-down el-icon--right"></i>
        </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item><i class="el-icon-setting" style="margin-right: 5px"></i>修改密码</el-dropdown-item>
              <el-dropdown-item><i class="el-icon-circle-close" style="margin-right: 5px"></i>退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </header>
        <!--用router-view渲染视图-->
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  export default {
    data() {
      return {
        defaultProps: {
          children: 'children',
          label: 'label'
        }
      };
    },
    methods: {
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
  }
</style>
