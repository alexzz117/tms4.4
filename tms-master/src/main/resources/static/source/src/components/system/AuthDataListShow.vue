<template>
  <div>
    <transition name="fade">

      <el-form label-position="right" label-width="120px" :model="queryShowForm" ref="queryShowForm"
               :inline="true" style="text-align: left" v-show="queryFormShow" >

        <el-form-item label="所属交易:" prop="txn_id">
          <div @click="openTxnDialog" >
            <el-input v-model="queryShowForm.txnIdShow" class="alarm-event-query-form-item" auto-complete="off" readonly clearable></el-input>
          </div>
        </el-form-item>

        <el-form-item label="授权状态:" prop="auth_status">
          <el-select v-model="queryShowForm.auth_status" class="alarm-event-query-form-item" placeholder="请选择"
                     clearable>
            <el-option
              v-for="item in authStatusList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>

          </el-select>
        </el-form-item>

        <el-form-item label="提交授权时间:" prop="queryDate">
          <el-date-picker
            v-model="queryShowForm.queryDate"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
          </el-date-picker>
        </el-form-item>

      </el-form>

    </transition>

    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-back" type="primary" @click="back()">返回</el-button>
      <div style="float:right">
        <el-button type="primary" @click="queryFormShow = !queryFormShow">更多</el-button>
        <el-button type="primary" @click="searchData">搜索</el-button>
      </div>
      <div style="float:right; padding-right: 15px;">

        <el-date-picker
          v-model="queryShowForm.queryDate"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </div>
    </div>
    <section class="table">
      <el-table
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange">

        <el-table-column
          fixed="left"
          label="操作"
          width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showLog(scope.row)">查看日志</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作数据" align="left">
          <template slot-scope="scope">
            <span>{{scope.row.operatedata_value}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orig_operatename" label="操作名称" align="left"></el-table-column>
        <el-table-column v-if="txnnameRowShow" prop="txnname" label="所属交易" align="left"></el-table-column>
        <el-table-column label="授权状态" align="left" width="80">
          <template slot-scope="scope">
            <span>{{scope.row.auth_status|renderAuthStatus}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="auth_msg" label="授权说明" align="left"></el-table-column>
        <el-table-column label="提交授权时间" align="left"  width="150">
          <template slot-scope="scope">
            <span>{{scope.row.proposer_time | renderDateTime}}</span>
          </template>
        </el-table-column>
        <el-table-column label="缓存刷新结果" align="left" width="100">
          <template slot-scope="scope">
            <span>{{scope.row.refresh_status|renderOperateResult}}</span>
          </template>

        </el-table-column>
        <el-table-column prop="refresh_info" label="缓存刷新信息" align="left" width="100"></el-table-column>

      </el-table>
      <el-pagination style="margin-top: 10px; text-align: right;"
                     :current-page="currentPage"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :page-sizes="[10, 25, 50, 100]"
                     :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total">
      </el-pagination>
    </section>

    <el-dialog title="所属交易" :visible.sync="txntypeDialogVisible" width="400px" :close-on-click-modal="false">
      <el-tree :data="treeData" node-key="id" ref="tree"
               show-checkbox
               :props="defaultTreeProps"
               :highlight-current=true
               :default-expanded-keys="expendKey"
               :expand-on-click-node="false"
               :render-content="renderContent"
               @check-change="handleCheckChange"
               style="overflow-y: auto;">
      </el-tree>

      <div slot="footer" class="dialog-footer">
        <el-button @click="txntypeDialogVisible = false" size="large">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'
  import check from '@/common/check'
  import dictCode from "../../common/dictCode";

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      },
      notSelect () {
        return this.selectedRows.length === 0
      },
      txnnameRowShow () {
        return this.modelName !== '名单管理'
      }
    },
    data () {
      return {
        queryFormShow: false,
        queryShowForm: {
          txnIdShow: '',
          txn_id: '',
          auth_status: '',
          queryDate: this.initDateRange()
        },
        queryForm: {
          txn_id: '',
          auth_status: '',
          queryDate: this.initDateRange()
        },
        txntypeDialogVisible: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        expendKey: ['T'], // 默认展开的功能节点id
        treeData: [],
        modelName: '',
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectRow: {},
        selectedRows: [],
        authStatusList: []
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.modelName = this.$route.query.modelname
        this.authStatusList = dictCode.getCodeItems('tms.auth.authstatus')
        this.selTree()
        this.getData()
      })
    },
    filters: {
      renderDateTime: function (value) {
        return util.renderDateTime(value)
      },
      renderAuthStatus: function (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('tms.auth.authstatus', value.toString())
      },
      renderOperateResult: function (value) {
        if (value === undefined || value === '') {
          return ''
        }
        return dictCode.rendCode('common.operateresult', value.toString())
      }
    },
    methods: {
      initDateRange () {
        return null
      },
      openTxnDialog () {
        this.txntypeDialogVisible = true
      },
      // 功能树渲染方法
      renderContent (h, { node, data, store }) {
        if (node.data.enable === 0) { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      // 查询树结构
      selTree () {
        var self = this
        var option = {
          url: '/trandef/query',
          success: function (data) {
            if (data.list) {
              self.treeList = (data.list)
              self.treeData = self.formatTreeData(data.list)
            }
          }
        }
        ajax.post(option)
      },
      // 把功能节点列表格式化为树形Json结构
      formatTreeData (list, rootNodes) {
        var tree = []
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = []
          for (var i in list) {
            list[i].text = `${list[i].code_value}(${list[i].code_key})`
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        // 根节点不存在判断
        if (rootNodes.length === 0) {
          console.error('根节点不存在，请确认树结构是否正确')
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
        }
        // 根据根节点遍历组装数据
        for (var r in rootNodes) {
          var node = rootNodes[r]
          node.children = getChildren(list, node.id)
          tree.push(node)
        }

        // 递归查询节点的子节点
        function getChildren (list, id) {
          var childs = []
          for (var i in list) {
            var node = list[i]
            if (node.fid === id) {
              node.children = getChildren(list, node.id)
              // node.icon = 'el-icon-message'
              childs.push(node)
            }
          }
          return childs
        }
        return tree  // 返回树结构Json
      },
      handleCheckChange (data, checked, indeterminate) {
        // console.log(data, checked, indeterminate)
        let checkedArr = this.$refs.tree.getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name)
        }
        this.queryShowForm.txnIdShow = checkedStrArr.join(',')
        this.queryShowForm.txn_id = checkedStrKeyArr.join(',')
      },
      handleSizeChange (val) {
        this.currentPage = 1
        this.pageSize = val
        this.getData()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.getData()
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      getData () {
        let self = this
        let paramsObj = {
          modelName: this.modelName,
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        Object.assign(paramsObj, this.queryForm)
        if (this.queryForm.queryDate && this.queryForm.queryDate.length > 1) {
          if (this.queryForm.queryDate[0] && this.queryForm.queryDate[1]) {
            paramsObj.startDate = this.queryForm.queryDate[0].getTime()
            paramsObj.endDate = this.queryForm.queryDate[1].getTime()
          }
        }
        ajax.post({
          url: '/auth/dataquary',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      showLog (row) {
        let params = {
          authId: row.auth_id,
          modelName: this.modelName
        }
        this.$router.push({name: 'authLogList', query: params})
      },
      searchData () {
        Object.assign(this.queryForm, this.queryShowForm)
        this.getData()
      },
      back () {
        this.$router.go(-1)
      }
    }
  }
</script>

<style>
  .auth-data-list-show-form-item{
    width: 200px;
  }
</style>
