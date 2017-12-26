<template>
  <div>
    <el-form label-position="right" label-width="100px" :model="listForm" class="demo-form-inline" label="left"
             :inline="inline" style="text-align: left">
      <el-form-item label="名单名称:" prop="listFormFosterdesc">
        <el-input v-model="listForm.rosterdesc"></el-input>
      </el-form-item>
      <el-form-item label="名单数据类型:">
        <el-select v-model="listForm.datatype" @focus="selectFocus('datatype')" placeholder="请选择" :clearable="clearable">
          <el-option
            v-for="item in datatypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="名单类型:">
        <el-select v-model="listForm.rostertype" @focus="selectFocus('rostertype')" placeholder="请选择" :clearable="clearable">
          <el-option
            v-for="item in rostertypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button class="el-icon-search" type="primary" @click="sel" ref="selBtn" id="selBtn">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="gridData"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column label="序号"
        type="index"
        width="50">
      </el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        width="100">
        <template slot-scope="scope">
          <el-button plain class="el-icon-edit" @click="openDialog()" size="mini">查看</el-button>
          <el-button plain class="el-icon-edit" @click="showValueList" size="mini">名单值</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="rostername" label="名单英文名" align="left" width="120"></el-table-column>
      <el-table-column prop="rosterdesc" label="名单名称" align="left" width="120"></el-table-column>
      <el-table-column prop="datatype" label="名单数据类型" align="left" width="120" :formatter="formatter"></el-table-column>
      <el-table-column prop="rostertype" label="名单类型" align="left" width="120" :formatter="formatter"></el-table-column>
      <el-table-column prop="valuecount" label="值数量" align="left" width="120"></el-table-column>
      <el-table-column prop="createtime" label="创建时间" align="left" width="150" :formatter="formatter"></el-table-column>
      <el-table-column prop="iscache" label="是否缓存" align="left" width="120" :formatter="formatter"></el-table-column>
      <el-table-column prop="remark" label="备注" align="left" width="120"></el-table-column>
    </el-table>
    <el-pagination  class="block" label="center" label-width="100px"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :current-page="pageindex"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pagesize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>

  <!-- 名单查看弹出窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="listDialogVisible">
      <el-form :model="listDialogform" :rules="rules" ref="listDialogform" :label-width="formLabelWidth"
               style="text-align: left">
        <el-form-item label="名单英文名" prop="rostername" data="rostername">
          <el-input v-model="listDialogform.rostername" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单名称" prop="rosterdesc" data="rosterdesc">
          <el-input v-model="listDialogform.rosterdesc" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单数据类型" prop="datatype" data="datatype">
          <el-select v-model="listDialogform.datatype" :disabled="status" :clearable="clearable">
            <el-option
              v-for="item in datatypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名单类型" prop="rostertype" data="rostertype">
          <el-select v-model="listDialogform.rostertype" :disabled="status" :clearable="clearable">
            <el-option
              v-for="item in rostertypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否缓存" prop="iscache" data="iscache">
          <el-radio v-model="listDialogform.iscache" label="1">是</el-radio>
          <el-radio v-model="listDialogform.iscache" label="0">否</el-radio>
        </el-form-item>
        <el-form-item label="备注" prop="remark" data="remark">
          <el-input type="textarea" v-model="listDialogform.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="listDialogVisible = false" data="cancelBtn">返 回</el-button>
      </div>
    </el-dialog>
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
      openDialog() {
          this.dialogTitle = '查看名单'
          this.status = true
          var length = this.multipleSelection.length
          if (length !== 1) {
            this.$message('请选择一行名单信息。')
            return
          }
          this.listDialogform = util.extend({}, this.multipleSelection[0])
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      sel(pageinfo) {
        var self = this;
        var param;
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm, pageinfo)
        } else {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm)
        }

        ajax.post({
          url: '/mgr/Namelist',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      showValueList() {
        var rosterid = this.multipleSelection[0].rosterid
        var datatype = this.multipleSelection[0].datatype
        this.$router.push({ name: 'valuelist', params: { rosterid: rosterid, datatype: datatype}})
      },
      selectFocus (name) {
        if (name === 'datatype' &&　this.datatypeOptions.length === 0) {
          this.datatypeOptions = dictCode.getCodeItems('tms.model.datatype.roster')
        }

        if (name === 'rostertype' &&　this.rostertypeOptions.length === 0) {
          this.rostertypeOptions = dictCode.getCodeItems('tms.mgr.rostertype')
        }
      },
      formatter(row, column, cellValue) {
        switch(column.property )
        {
          case 'datatype':
            return dictCode.rendCode('tms.model.datatype.roster', cellValue)
            break;
          case 'rostertype':
            return dictCode.rendCode('tms.mgr.rostertype', cellValue)
            break;
          case 'createtime':
            return dictCode.rendDatetime(cellValue)
            break;
          case 'iscache':
            return dictCode.rendCode('tms.mgr.iscache', cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
    },
    data() {
      return {
        inline: true,
        clearable: true,
        listForm: {
          rosterdesc: "",
          datatype: "",
          rostertype: ""
        },
        gridData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        listDialogVisible: false,
        listDialogform: {
          rostername: "",
          rosterdesc: "",
          datatype: "",
          rostertype: "",
          iscache: "1",
          remark: "",
        },
        status: true,
        btnStatus: true,
        dialogTitle: '',
        formLabelWidth: '150px',
        datatypeOptions: [],
        rostertypeOptions: [],
        multipleSelection: []
      }
    }
  }
</script>
