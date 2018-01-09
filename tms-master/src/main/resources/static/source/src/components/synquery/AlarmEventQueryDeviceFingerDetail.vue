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

      <el-pagination style="margin-top: 10px; text-align: right;"
                     :current-page="currentPage"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :page-sizes="[5]"
                     :page-size="pageSize"
                     layout="total, prev, next, jumper"
                     :total="total">
      </el-pagination>
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
        tableData: [],
        currentPage: 1,
        pageSize: 5,
        total: 0
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
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      handleSizeChange (val) {
        // console.log(`每页 ${val} 条`)
        this.currentPage = 1
        this.pageSize = val
        this.loadData(this.showItemParent)
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.loadData(this.showItemParent)
      },
      loadData (row) {
        let self = this
        ajax.post({
          url: '/query/alarmEvent/deviceFingerDetail',
          modal: ajax.model.dualaudit,
          param: {
            device_id: row.deviceid,
            pageindex: this.currentPage,
            pagesize: this.pageSize
          },
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
              // self.tableData = data.row
            }
          }
        })
      }
    }
  }
</script>
<style>

</style>
