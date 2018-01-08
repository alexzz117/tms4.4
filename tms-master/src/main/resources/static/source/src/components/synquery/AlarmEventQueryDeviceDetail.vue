<template>
  <div>
    <div v-show="!hasData">
      无信息记录
    </div>
    <div v-show="hasData">
      <el-row>

        <el-col :span="6">设备标识:</el-col>
        <el-col :span="6">{{detailData.device_id}}&nbsp</el-col>
        <el-col :span="6">渠道代码:</el-col>
        <el-col :span="6">{{detailData.app_id}}&nbsp</el-col>
      </el-row>
      <el-row>
        <el-col :span="6">创建时间:</el-col>
        <el-col :span="6">{{detailData.create_time|renderDateTime}}&nbsp</el-col>
        <el-col :span="6">最后使用时间:</el-col>
        <el-col :span="6">{{detailData.lastmodified|renderDateTime}}&nbsp</el-col>
      </el-row>
    </div>
  </div>
</template>
<script>
  import ajax from "../../common/ajax";
  import util from "../../common/util";
  import dictCode from "../../common/dictCode";

  export default {
    name: 'alarmEventDeviceDetail',
    computed: {
      showItemParent () {
        return this.showItem
      }
    },
    data () {
      return {
        detailData: {},
        hasData: false
      }
    },
    filters: {
      renderDateTime (value) {
        return util.renderDateTime(value)
      },
      renderCerttype (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.certificate.type', value.toString())
      },
      renderIs (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.is', value)
      },
      renderSex (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.sex', value)
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
        ajax.post({
          url: '/query/alarmEvent/deviceDetail',
          param: {
            deviceid: row.deviceid
          },
          success: function (data) {
            if (data.row && data.row.length > 0) {
              self.hasData = true
              self.detailData = data.row[0]
            } else {
              self.hasData = false
            }
          }
        })
      }
    }
  }
</script>
<style>

</style>
