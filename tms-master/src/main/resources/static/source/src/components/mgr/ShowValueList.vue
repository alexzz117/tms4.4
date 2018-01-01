<template>
  <div>
    <h1>名单值管理 -> {{ title }}</h1>
    <el-form label-position="right" label-width="100px" :model="valueListForm"
             :inline="inline" style="text-align: left">
      <el-form-item label="名单值">
        <el-input v-model="valueListForm.rostervalue"></el-input>
      </el-form-item>
    </el-form>
    <el-table
      :data="gridData"
      stripe
      border
      style="width: 100%">
      <el-table-column type="selection" width="55" align="left"></el-table-column>
      <el-table-column prop="rostervalue" label="名单值" align="left" width="200"></el-table-column>
      <el-table-column prop="enabletime" label="开始时间" align="left" width="200"></el-table-column>
      <el-table-column prop="disabletime" label="结束时间" align="left" width="200"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="200"></el-table-column>
      <el-table-column prop="remark" label="备注" align="left" width="300"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="pageindex"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>
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
      bindValueGridData(data) {
        this.changeValueTableData = data.row
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
      },
      update() {
        var self = this;
        var param = util.extend({
          rosterid: this.listData.rosterid,
          datatype: this.listData.datatype,
          rosterdesc: this.listData.rosterdesc,
        }, this.valueListDialogform)

        ajax.post({
          url: '/mgr/valuemod',
          param: param,
          success: function (data) {
            self.$message('更新成功。')
            self.valueListDialogVisible = false
            self.sel()
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
        formLabelWidth: '150px',
        rules: {
          rostervalue: [
            { required: true, message: '请输入名单英文名', trigger: 'blur' }
//            ,
//            { max: 32, message: '长度在50个字符以内', trigger: 'blur' },
//            { validator: check.checkFormEnSpecialCharacter, trigger: 'blur' }
          ],
          enabletime: [
//            { required: true, message: '请输入名单名称', trigger: 'blur' },
//            { max: 64, message: '长度在50个字符以内', trigger: 'blur' },
//            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          disabletime: [
//            { required: true, message: '请输入名单数据类型', trigger: 'blur' }
          ],
          remark: [
            { max: 512, message: '长度在512个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        multipleSelection: [],
        flag: '',
        valuecurrentRow: {},
        btnStatus: true,
        delBtnStatus: true
      }
    }
  }
</script>

