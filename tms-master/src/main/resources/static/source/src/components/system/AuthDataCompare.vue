<template>
  <div>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-back" type="primary" @click="back()">返回</el-button>
    </div>

    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%">
      <el-table-column prop="col_num" label="序号" align="left" width="80"></el-table-column>
      <el-table-column prop="field_name" label="属性名称" align="left"></el-table-column>
      <el-table-column label="新值" align="left">
        <template slot-scope="scope">
          <span :class="{'red-span': (scope.row.new_value !== scope.row.old_value) }">
            <span v-if="scope.row.type === 'date'">{{scope.row.new_value | renderDate}}</span>
            <span v-else-if="scope.row.type === 'datetime'">{{scope.row.new_value | renderDateTime}}</span>
            <span v-else>{{scope.row.new_value}}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="旧值" align="left">
        <template slot-scope="scope">
          <span :class="{'green-span': (scope.row.new_value !== scope.row.old_value) }">
            <span v-if="scope.row.type === 'date'">{{scope.row.old_value | renderDate}}</span>
            <span v-else-if="scope.row.type === 'datetime'">{{scope.row.old_value | renderDateTime}}</span>
            <span v-else>{{scope.row.old_value}}</span>
          </span>
        </template>

      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  export default {
    mounted: function () {
      this.$nextTick(function () {
        this.modelName = this.$route.query.modelName
        this.tableName = this.$route.query.table_name
        this.tablePk = this.$route.query.table_pk
        this.tablePkvalue = this.$route.query.table_pkvalue

        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      renderDate: function (value) {
        return util.renderDate(value)
      }
    },
    data () {
      return {
        modelName: '',
        tableName: '',
        tablePk: '',
        tablePkvalue: '',
        tableData: []
      }
    },
    methods: {
      getData () {
        let self = this
        let params = {
          table_name: this.tableName,
          table_pk: this.tablePk,
          table_pkvalue: this.tablePkvalue
        }
        ajax.post({
          url: '/auth/dataCompare',
          param: params,
          success: function (data) {
            if (data.page) {
              self.tableData = data.page.list
            }
          }
        })
      },
      back () {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>
.red-span{
  color: red;
}
.green-span{
  color: green;
}
</style>
