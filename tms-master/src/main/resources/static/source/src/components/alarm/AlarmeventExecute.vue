<template>
<div>
  <el-table
    :data="userData" highlight-current-rows
    tooltip-effect="dark" style="width: 100%" @selection-change="handleSelectionChange">
    <el-table-column  prop="login_name" label="操作员ID" width="120" align="left" />
    <el-table-column  prop="ASSIGN_NUMBER" label="总量" width="70" align="left" />
    <el-table-column  prop="PROCESS_NUMBER" label="已处理" width="70" align="left" />
    <el-table-column  prop="UNPROCESS_NUMBER" label="未处理" width="70" align="left" />
  </el-table>
  <div class="dialog-footer"  align="center"  slot="footer">
    <el-button data="cancelBtn" icon="el-icon-success" type="primary" >确 定</el-button>
    <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" >返 回</el-button>
  </div>

</div>
</template>

<script>
  import util from '@/common/util'
  import check from '@/common/check'
  import ajax from '@/common/ajax'
  import dictCode from '@/common/dictCode'
  export default {
    created () {
      this.sel()
    },
    methods: {
      handleSizeChange(val) {
        this.sel({
        })
      },
      handleCurrentChange(val) {
        this.sel({
        })
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      bindGridData(data) {
        this.gridData = data.list
      },
      backList() {
        this.$router.push({ name: 'alarmEventSend' })
      },
      sel(pageinfo) {
        var self = this;
        c
        alert(param)
        ajax.post({
          url: '/alarmevent/assign',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      }
    },
    data() {
      return {
        inline: true,
        gridData: [],
        dialogTitle: "",
        title: "testdddd",
        multipleSelection: [],
        flag: '',
        valuecurrentRow: {}
      }
    }
  }
</script>


