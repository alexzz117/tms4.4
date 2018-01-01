<template>
  <div>
    <h1>名单值管理 -> {{ title }}</h1>
    <el-table
      :data="gridData"
      style="width: 100%">
      <el-table-column prop="rostervalue" label="名单值" align="left" width="200"></el-table-column>
      <el-table-column prop="enabletime" label="开始时间" align="left" width="200"></el-table-column>
      <el-table-column prop="disabletime" label="结束时间" align="left" width="200"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="200"></el-table-column>
      <el-table-column prop="remark" label="备注" align="left" width="300"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;" background  label="left"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="pageindex"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>
    <div slot="footer" class="dialog-footer" >
      <el-button @click="backList" data="cancelBtn" icon="el-icon-arrow-left" type="primary" >返 回</el-button>
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
      this.getList(this.sel)
    },
    methods: {
      handleSizeChange(val) {
        this.sel({
          pageindex:this.pageindex,
          pagesize:val
        })
      },
      handleCurrentChange(val) {
        this.sel({
          pageindex:val,
          pagesize:this.pagesize
        })
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      getList(cb) {
        self = this
        var param = {
          rosterid: this.$router.history.current.params.rosterid
        }
        ajax.post({
          url: '/mgr/get',
          param: param,
          success: function (data) {
            self.listData = data.row
            self.title = data.row.rosterdesc
            if (cb) {
              cb()
            }
          }
        })
      },
      backList() {
        this.$router.push({ name: 'namelist' })
//        this.$router.push('list');
      },
      sel(pageinfo) {
        var self = this;
        var param;
        var comParam = {
          pageindex:this.pageindex,
          pagesize:this.pagesize,
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype
        }
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend(comParam, this.valueListForm, pageinfo)
        } else {
          param = util.extend(comParam, this.valueListForm)
        }
        ajax.post({
          url: '/mgr/valuelist',
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
        valueListForm: {
          rostervalue: ""
        },
        listData: {
          rosterdesc: ""
        },
        gridData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        valueListDialogVisible: false,
        valueListDialogform: {
          rostervalue: "",
          enabletime: new Date().format('yyyy-MM-dd hh:mm:ss'),
          disabletime: "",
          remark: ""
        },
        dialogTitle: "",
        title: "testdddd",
        multipleSelection: [],
        flag: '',
        valuecurrentRow: {},
        changeValueDialogVisible: false,
        btnStatus: true,
        delBtnStatus: true
      }
    }
  }
</script>

