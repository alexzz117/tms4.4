<template>
  <div>
    <div class="toolbar">
      <el-form label-position="right" label-width="100px" :model="listForm"
               :inline="inline" class="toolbar-form">
        <el-form-item label="名单名称" prop="listFormFosterdesc">
          <el-input v-model="listForm.rosterdesc"></el-input>
        </el-form-item>
        <!--<el-form-item label="名单数据类型">-->
          <!--<el-select v-model="listForm.datatype" @focus="selectFocus('datatype')" placeholder="请选择" :clearable="clearable">-->
            <!--<el-option-->
              <!--v-for="item in datatypeOptions"-->
              <!--:key="item.value"-->
              <!--:label="item.label"-->
              <!--:value="item.value">-->
            <!--</el-option>-->
          <!--</el-select>-->
        <!--</el-form-item>-->
        <el-form-item label="名单类型">
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
    </div>
    <section class="table">
      <el-table
        :data="gridData"
        @selection-change="handleSelectionChange">
        <el-table-column
          label="操作"
          width="160">
          <template slot-scope="scope" >
            <el-button type="text"  @click="openDialog(scope.$index, scope.row)"  icon="el-icon-view"  v-show="typeof(scope.row.rosterid)=='number'">查看</el-button>
            <el-button type="text"  @click="showValueList(scope.$index, scope.row)" icon="el-icon-document" v-show="typeof(scope.row.rosterid)=='number'">名单值</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="rostername" label="名单英文名" align="left" width="160"></el-table-column>
        <el-table-column prop="rosterdesc" label="名单名称" align="left" width="120"></el-table-column>
        <el-table-column prop="datatype" label="名单数据类型" align="left" width="120" :formatter="formatter"></el-table-column>
        <el-table-column prop="rostertype" label="名单类型" align="left" width="120" :formatter="formatter"></el-table-column>
        <el-table-column prop="valuecount" label="值数量" align="left" width="90"></el-table-column>
        <el-table-column prop="createtime" label="创建时间" align="left" width="160" :formatter="formatter"></el-table-column>
        <el-table-column prop="iscache" label="是否缓存" align="left" :formatter="formatter"></el-table-column>
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
    </section>
    
  <!-- 名单查看弹出窗 -->
    <el-dialog title="查看名单" :visible.sync="listDialogVisible" append-to-body  >
      <el-form :model="listDialogform" ref="listDialogform" :label-width="formLabelWidth"
               style="text-align: left" >
        <el-form-item label="名单英文名" prop="rostername" data="rostername">
          <el-input v-model="listDialogform.rostername" auto-complete="off" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="名单名称" prop="rosterdesc" data="rosterdesc">
          <el-input v-model="listDialogform.rosterdesc" auto-complete="off" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="名单数据类型" prop="datatype" data="datatype">
          <el-select v-model="listDialogform.datatype" disabled :clearable="clearable">
            <el-option
              v-for="item in datatypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名单类型" prop="rostertype" data="rostertype">
          <el-select v-model="listDialogform.rostertype" disabled :clearable="clearable">
            <el-option
              v-for="item in rostertypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否缓存" prop="iscache" data="iscache" disabled>
          <el-radio v-model="listDialogform.iscache" label="1" disabled>是</el-radio>
          <el-radio v-model="listDialogform.iscache" label="0" disabled>否</el-radio>
        </el-form-item>
        <el-form-item label="备注" prop="remark" data="remark">
          <el-input type="textarea" v-model="listDialogform.remark" :disabled="true"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" >
      <el-button @click="listDialogVisible = false" data="cancelBtn" icon="el-icon-arrow-left" type="primary" >返 回</el-button>
    </div>
    </el-dialog>
  </div>
</template>

<script>
  import util from '@/common/util'
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
      openDialog(index, row) {
        this.listDialogVisible=true
        Object.assign(this.listDialogform, this.selectRowNum2Str(row))

        setTimeout(function () {
          self.listDialogform.rostertype = row.rostertype
        }, 300)
      },
      // 下拉获取的码值时字符串，真实数据是数字，要转一下才好用
      selectRowNum2Str (row) {
        let tempObj = {}
        Object.assign(tempObj, row)
        for (let field in tempObj) {
          if (typeof (tempObj[field]) === 'number') {
            tempObj[field] = tempObj[field].toString()
          }
        }
        return tempObj
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
          url: '/mgrView/list',
          param: param,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      showValueList(index, row) {
        var rosterid = row.rosterid.toString()
        var datatype = 'view'
        this.$router.push({ name: 'showValueList', params: { rosterid: rosterid, datatype: datatype}})
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
        dialogTitle: '',
        formLabelWidth: '150px',
        datatypeOptions: [],
        rostertypeOptions: [],
        multipleSelection: []
      }

    },
    props: ['rosterid']
  }
</script>
