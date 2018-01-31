<template>
  <div id="app">
    <transition name="slide-fade">
      <router-view/>
    </transition>
  </div>
</template>

<script>
  export default {
    name: 'app'
  }
</script>

<style>
  html, body{
    width: 100%;
    height: 100%;
    overflow: hidden;
    margin: 0;
    padding: 0;
  }

  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    width: 100%;
    height: 100%;
    /*display: flex;*/
    /*align-items: center;*/
  }

  /*************element 样式调整 start************/
  .el-menu-item, .el-submenu__title {
    text-align: left;
  }

  /*************element 样式调整 end************/

  .slide-fade-enter-active{
    transition: all 0.3s ease;
  }

  .slide-fade-leave-active{
    transition: all 0.3s cubic-bezier(1.0,0.5,0.8,1.0);
  }

  .slide-fade-enter, .slide-fade-leave-active{
    transform: translateX(-430px);
    opacity: 0;
  }

  /*************统一设置样式************/
  .el-form {
    text-align: left
  }
  .el-select{
    width: 100%;
  }
  .el-date-editor.el-input, .el-date-editor.el-input__inner {
    width: 100%;
  }
  /*.el-table {*/
    /*border: 1px solid rgb(238, 241, 246)*/
  /*}*/

  /********定制webkit内核滚动条**********/
  .scroll::-webkit-scrollbar {
    width: 12px;
    height: 10px;
  }
  .scroll::-webkit-scrollbar-button       {
    display:none
  }
  .scroll::-webkit-scrollbar-button    {
    background-color:#FF7677;
  }
  .scroll::-webkit-scrollbar-track        {
    display:none
  }
  .scroll::-webkit-scrollbar-track-piece  {
    display:none
  }
  .scroll::-webkit-scrollbar-thumb {
    /*background: rgb(158, 165, 171);*/
    background: #e9ecf2;
    display: none;
    -webkit-border-radius: 10px;
    border-radius: 10px;
    -webkit-box-shadow: inset 0 0 4px rgba(0,0,0,0.5);
  }
  .scroll::-webkit-scrollbar-corner       { /* 6 */ }
  .scroll::-webkit-resizer                { /* 7 */ }
  .scroll:hover::-webkit-scrollbar-thumb{
    display: block;
    opacity: 0.4;
  }
</style>
