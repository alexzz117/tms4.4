<template>
  <div>
    <el-table :data="tableData"
              :span-method="groupHandle"
              :row-class-name="groupClassName"
              max-height="430"
      style="width: 100%">
      <el-table-column type="selection" width="35" align="left"></el-table-column>
      <el-table-column label="属性名称" prop="name">
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 10px">{{scope.row.name}}</span>
        </template>
      </el-table-column>
      <el-table-column label="属性代码" prop="ref_name"></el-table-column>
      <el-table-column label="数据来源" prop="src_id"></el-table-column>
      <el-table-column label="类型" prop="type"></el-table-column>
      <el-table-column label="存储字段" prop="fd_name"></el-table-column>
      <el-table-column label="关联代码集" prop="code"></el-table-column>
      <el-table-column label="默认值" prop="src_default"></el-table-column>
      <el-table-column label="处理函数" prop="genesisrul"></el-table-column>
    </el-table>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  let vm = null
  export default {
    create () {

    },
    computed: {
      txnIdParent () { return this.txnId },
      isVisibilityParent () { return this.isVisibility }
    },
    props: ['txnId', 'isVisibility'],
    data () {
      return {
        tableData: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        vm = this
      })
    },
    watch: {
      txnId: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.initForm()
          }
        }
      },
      isVisibility: {
        handler: (val, oldVal) => {
          if (vm.isVisibilityParent === true) {
            vm.initForm()
          }
        }
      }
    },
    methods: {
      initForm () {
        var self = this
        var option = {
          url: '/tranmdl/query',
          param: {
            tab_name: self.txnId
          },
          success: function (data) {
            if (data.txnfds) {
              self.setFormList(data.txnfds)
            }
          }
        }
        ajax.post(option)
      },
      setFormList (dataList) {
        var tableData = []
        for (var i in dataList) { // 取数据列表
          for (var key in dataList[i]) { // 取对象的Key-Value
            if (dataList[i][key].length > 0) { // 取Value的值List
              let keys = key.split('____')
              tableData.push({
                tab_name: keys[0],
                name: keys[1],
                group_type: 'group'
              })
              for (var j in dataList[i][key]) {
                var item = dataList[i][key][j]
                item.group_type = 'item'
                tableData.push(item)
              }
            }
          }
        }
        this.tableData = tableData
      },
      groupHandle ({ row, column, rowIndex, columnIndex }) {
        if (row.group_type === 'group') {
          switch (columnIndex) {
            case 1 :
              return [1, 9] // 以当前的单元格位置开始【合并行数，合并的列数】
            default :
              return [0, 0]
          }
        }
      },
      groupClassName ({row, rowIndex}) {
        if (row.group_type === 'group') {
          console.info(row)
          return 'groupStyle'
        } else {
          return ''
        }
      }
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }
  tbody .groupStyle {
    background-color: #f5f7fa !important;
  }
</style>
