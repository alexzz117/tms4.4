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
    display: flex;
    align-items: center;
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

</style>
