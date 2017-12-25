<template>
  <div>
    <h1>预警事件查询</h1>
    <h2>选择时间： {{ value10 }}</h2>
    <el-date-picker v-model="value10" type="datetime"
                    value-format="yyyy-MM-dd HH:mm:ss"
                    :default-value="defaultValue"></el-date-picker>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        value10: new Date().format('yyyy-MM-dd hh:mm:ss'),
        defaultValue: new Date()
      };
    }
  };
</script>

<style>

</style>
