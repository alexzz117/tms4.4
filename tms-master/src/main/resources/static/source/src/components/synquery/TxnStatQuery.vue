<template>
  <div>
    <el-table
      :data="txnStatTableData"
      style="width: 100%">
      <el-table-column prop="stat_desc" label="统计名称" align="left"></el-table-column>
      <el-table-column prop="storecolumn" label="存储字段" align="left"></el-table-column>
      <el-table-column prop="storevalue" label="当前统计值" align="left"></el-table-column>
    </el-table>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'

  export default {
    props: ['txnCode'],
    created () {
      this.initTable()
    },
    data () {
      return {
        pTnxCode: this.txnCode,
        txnStatTableData: [],
        formLabelWidth: '120px'
      }
    },
    watch: {
      txnCode: function (val) {
        this.pTnxCode = val
        this.initTable(val)
      }
    },
    methods: {
      initTable (txnCode) {
        let self = this
        txnCode = txnCode || self.pTnxCode
        ajax.post({
          url: '/query/tnxEvent/txnStatQuery',
          param: {'txnCode': txnCode},
          success: function (data) {
            if (data && data.success === true) {
              self.txnStatTableData = data.list
            }
          }
        })
      }
    }
  }
</script>

<style scoped>

</style>
