<template>

</template>
<script>
  import ajax from "../../common/ajax";

  export default {
    data () {
      return {

      }
    },
    mounted: function () {
      this.$nextTick(function () {
        // this.reloadData()
        this.loadNodesData()
      })
    },
    methods: {
      loadNodesData () {
        ajax.post({
          url: '/zookeeper/list',
          model: ajax.model.none,
          param: {},
          success: function (data) {
            console.log(data)
          }
        })
      }
    }
  }
</script>
<style>

</style>
