<template>
  <div>
    <!-- 处理信息 -->
    <section class="table">
      <el-table
        :data="tableData"
        style="width: 100%">
        <el-table-column prop="device_id" label="设备标识" align="left">

        </el-table-column>

        <el-table-column prop="prop_name" label="属性名称" align="left">
        </el-table-column>

        <el-table-column prop="prop_type" label="属性类型" align="left">
        </el-table-column>

        <el-table-column prop="prop_value" label="属性值" align="left">
        </el-table-column>

        <el-table-column prop="prop_comment" label="属性声明" align="left">
        </el-table-column>

      </el-table>
    </section>

  </div>
</template>
<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";

  export default {
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        tableData: []
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderPsResult (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.result', value)
      },
      renderIs (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderPsType (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.alarm.process.type', value)
      }
    },
    props: ['showItem'],
    mounted: function () {
      this.$nextTick(function () {
      })
    },
    methods: {

      loadData (row) {
        let self = this
        self.transHitRuleList = []
        ajax.post({
          url: '/query/alarmEvent/deviceFingerDetail',
          modal: ajax.model.dualaudit,
          param: {
            device_id: row.deviceid
          },
          success: function (data) {
            if (data.row) {
              self.tableData = data.row
            }
          }
        })
      }
    }
  }
</script>
<style>

</style>
