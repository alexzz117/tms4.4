<template>
  <div>
    <el-container style="border: 1px solid #eee;">
      <el-header height="30px" class="table-header">
          <div>交易模型</div>
      </el-header>
      <el-main style="padding: 0px;">
        <div style="padding-left: 10px;text-align: left;border-bottom: 1px solid #eee;">
          <el-button type="text" class="el-icon-plus" @click="tmAddFunc">新建</el-button>
          <el-button type="text" class="el-icon-edit" @click="tmEditFunc">编辑</el-button>
          <el-button type="text" class="el-icon-delete" @click="tmDelFunc">删除</el-button>
          <el-button type="text" class="el-icon-search" @click="tmInfoFunc">查看</el-button>
        </div>
        <el-table :data="tableData" ref="formList"
                  :span-method="groupHandle"
                  :row-class-name="groupClassName"
                  @row-dblclick="toggleListHandle"
                  max-height="430"
                  style="width: 100%">
          <el-table-column type="selection" width="35" align="left"></el-table-column>
          <el-table-column label="属性名称" prop="name">
            <template slot-scope="scope">
              <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row)></i>
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
      </el-main>
    </el-container>
    <el-dialog :title="tmTitle" :visible.sync="tmDialogVisible">
      <el-form :model="tmForm">
        <el-form-item label="活动名称" :label-width="formLabelWidth">
          <el-input v-model="tmForm.name" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="dialogFormVisible = false">确 定</el-button>
      </div>
    </el-dialog>
    <el-container style="border: 1px solid #eee;">
      <el-header height="30px" class="table-header">
        <div>交易模型引用</div>
      </el-header>
      <el-main style="padding: 0px;">
        <div style="padding-left: 10px;text-align: left;border-bottom: 1px solid #eee;">
          <el-button type="text" class="el-icon-plus" @click="">新建</el-button>
          <el-button type="text" class="el-icon-edit" @click="">编辑</el-button>
          <el-button type="text" class="el-icon-delete" @click="">删除</el-button>
          <el-button type="text" class="el-icon-search" @click="">查看</el-button>
        </div>
        <el-table :data="tableData" ref="formList"
                  :span-method="groupHandle"
                  :row-class-name="groupClassName"
                  @row-dblclick="toggleListHandle"
                  max-height="430"
                  style="width: 100%">
          <el-table-column type="selection" width="35" align="left"></el-table-column>
          <el-table-column label="属性名称" prop="name">
            <template slot-scope="scope">
              <i v-if="scope.row.group_type==='group'" :class=groupIcon(scope.row)></i>
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
      </el-main>
    </el-container>
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
      isVisibilityParent () { return this.isVisibility },
      tableData () {
        var expendNodeKey = this.expendNodeKey
        var dataList = this.dataList
        var tableData = []
        for (var i in dataList) { // 取数据列表
          for (var key in dataList[i]) { // 取对象的Key-Value
            if (dataList[i][key].length > 0) { // 取Value的值List
              let keys = key.split('____') // 按格式获取名称
              if (keys && keys.length === 2) { // 格式正确
                tableData.push({
                  tab_name: keys[0],
                  name: keys[1],
                  group_type: 'group'
                })
                if (expendNodeKey.indexOf(key) === -1) {
                  break
                }
                for (var j in dataList[i][key]) {
                  var item = dataList[i][key][j]
                  item.group_type = 'item'
                  tableData.push(item)
                }
              }
            }
          }
        }
        return tableData
      }
    },
    props: ['txnId', 'isVisibility'],
    data () {
      return {
        toggleIcon: ['el-icon-arrow-up', 'el-icon-arrow-down'],
        dataList: [],
        expendNodeKey: [],
        tmTitle: '',
        tmDialogVisible: false,
        tmForm: [],
        formLabelWidth: '120px'
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
            var dataList = data.txnfds
            if (dataList) {
              self.dataList = dataList
              let expendNodeKey = []
              for (let i in dataList) {
                for (let key in dataList[i]) {
                  expendNodeKey.push(key)
                }
              }
              self.expendNodeKey = expendNodeKey
            }
          }
        }
        ajax.post(option)
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
          return 'groupStyle'
        } else {
          return ''
        }
      },
      groupIcon (row) {
        if (row.group_type !== 'group') {
          return ''
        }
        let expendNodeKey = this.expendNodeKey
        let key = row.tab_name + '____' + row.name
        if (expendNodeKey.indexOf(key) > -1) {
          return this.toggleIcon[0]
        } else {
          return this.toggleIcon[1]
        }
      },
      toggleListHandle (row, e) {
        let self = this
        if (row.group_type !== 'group') {
          return
        }
        let path = e.path
        let cellItem = null
        for (let i in path) {
          if (path[i].className === 'cell') {
            cellItem = path[i]
          }
        }
        let key = row.tab_name + '____' + row.name
        let expendNodeKey = self.expendNodeKey
        let iconItem = cellItem.getElementsByTagName('i')
        if (iconItem[0].getAttribute('class') === 'el-icon-arrow-up') {
          if (expendNodeKey.indexOf(key) > -1) {
            expendNodeKey.splice(expendNodeKey.indexOf(key), 1)
          }
        } else if (iconItem[0].getAttribute('class') === 'el-icon-arrow-down') {
          expendNodeKey.push(key)
        }
        self.expendNodeKey = expendNodeKey
      },
      tmAddFunc () {
        let self = this
        self.tmTitle = '新建交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
      },
      tmEditFunc () {
        let self = this
        self.tmTitle = '编辑交易模型'
        self.tmDialogVisible = true
        self.tmFormReadOnly = false
      },
      tmDelFunc () {
        let self = this
        console.info('删除交易模型')
      },
      tmInfoFunc () {
        let self = this
        self.tmTitle = '交易模型信息'
        self.tmDialogVisible = true
        self.tmFormReadOnly = true
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
    text-align: left;
  }
  tbody .groupStyle .cell {
    padding-left: 15px;
  }

  .table-header {
    text-align: left;
    line-height: 2em;
    background-color: aquamarine;
  }
</style>
