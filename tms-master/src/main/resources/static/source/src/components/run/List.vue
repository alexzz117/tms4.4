<template>
  <div>
    <div class="toolbar">
      <el-button plain class="el-icon-plus" @click="openDialog('add')" id="addBtn">新建</el-button>
      <!--<el-button plain class="el-icon-edit" @click="openDialog('edit')" :disabled="btnStatus">编辑</el-button>-->
      <!--<el-button plain class="el-icon-delete" @click="del" :disabled="delBtnStatus">删除</el-button>-->
      <!--<el-button plain class="el-icon-edit" @click="showValueList" :disabled="btnStatus">名单值</el-button>-->
      <!--<el-button plain class="el-icon-upload" @click="importList" :disabled="btnStatus">导入</el-button>-->
      <!--<el-button plain class="el-icon-download" @click="exportList" :disabled="btnStatus">导出</el-button>-->

      <el-form label-position="right" label-width="100px" :model="listForm"
               :inline="inline" class="toolbar-form">
        <el-form-item label="名单名称" prop="listFormFosterdesc">
          <el-input v-model="listForm.rosterdesc" :clearable="clearable"></el-input>
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
        <el-form-item label="">
          <el-button class="el-icon-search" type="primary" @click="sel" ref="selBtn" id="selBtn">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <section class="section">
      <el-table
        :data="gridData"
        @selection-change="handleSelectionChange"
        class="scroll">
        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
            <el-button
              icon="el-icon-edit"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'edit')"
              title="编辑"></el-button>
            <el-button
              icon="el-icon-delete"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'del')"
              title="删除"></el-button>
            <el-button
              icon="el-icon-document"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'valueList')"
              title="名单值"></el-button>
            <el-button
              icon="el-icon-upload"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'upload')"
              title="导入"></el-button>
            <el-button
              icon="el-icon-download"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'download')"
              title="导出"></el-button>
          </template>
        </el-table-column>
        <el-table-column prop="rostername" label="名单英文名" align="left" width="160"></el-table-column>
        <el-table-column prop="rosterdesc" label="名单名称" align="left" width="120"></el-table-column>
        <el-table-column prop="datatype" label="名单数据类型" align="left" width="120" :formatter="formatter"></el-table-column>
        <el-table-column prop="rostertype" label="名单类型" align="left" width="120" :formatter="formatter"></el-table-column>
        <el-table-column prop="valuecount" label="值数量" align="left" ></el-table-column>
        <el-table-column prop="createtime" label="创建时间" align="left" width="160" :formatter="formatter"></el-table-column>
        <el-table-column prop="iscache" label="是否缓存" align="left" :formatter="formatter"></el-table-column>
        <el-table-column prop="remark" label="备注" align="left" :show-overflow-tooltip="showOverflow"></el-table-column>
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

    <el-dialog :title="dialogTitle" :visible.sync="listDialogVisible" width="40%" :close-on-click-modal="closeOnClickModal">
      <el-form :model="listDialogform" :rules="rules" ref="listDialogform" :label-width="formLabelWidth"
               style="text-align: left">
        <el-form-item label="名单英文名" prop="rostername" data="rostername">
          <el-input v-model="listDialogform.rostername" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单名称" prop="rosterdesc" data="rosterdesc">
          <el-input v-model="listDialogform.rosterdesc" auto-complete="off" :disabled="status"></el-input>
        </el-form-item>
        <el-form-item label="名单数据类型" prop="datatype" data="datatype">
          <el-select v-model="listDialogform.datatype" :disabled="status" :clearable="clearable" @focus="selectFocus('datatype')">
            <el-option
              v-for="item in datatypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名单类型" prop="rostertype" data="rostertype">
          <el-select v-model="listDialogform.rostertype" :disabled="status" :clearable="clearable" @focus="selectFocus('rostertype')">
            <el-option
              v-for="item in rostertypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否缓存" prop="iscache" data="iscache">
          <el-switch v-model="listDialogform.iscache" active-value= "1"  inactive-value= "0"></el-switch>
        </el-form-item>
        <el-form-item label="备注" prop="remark" data="remark">
          <el-input type="textarea" v-model="listDialogform.remark"></el-input>
        </el-form-item>
        <el-form-item label="">
          <el-button type="primary" @click="submitForm('listDialogform')" data="saveBtn">保 存</el-button>
          <el-button @click="listDialogVisible = false" data="cancelBtn">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog title="名单管理导入" :visible.sync="importDialogVisible" :close-on-click-modal="closeOnClickModal">
      <el-upload
        class="upload-demo"
        ref="upload"
        action="/context/manager/mgr/import"
        :on-change="handleChange"
        :file-list="fileList"
        :multiple = "multiple"
        :auto-upload="false">
        <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
        <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
        <div slot="tip" class="el-upload__tip">请上传XLS、XLSX、TXT或CSV格式的文件</div>
      </el-upload>
    </el-dialog>

    <el-dialog title="选择导出名单格式" :visible.sync="exportDialogVisible" :close-on-click-modal="closeOnClickModal">
      <el-button type="text"  @click="exportAction('txt')">TXT</el-button>
      <el-button type="text" @click="exportAction('csv')">CSV</el-button>
      <el-button type="text" @click="exportAction('xls')">Excel2003及以下版本</el-button>
      <el-button type="text" @click="exportAction('xlsx')">Excel2007及以上版本</el-button>
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
      openDialog(flag) {
        this.flag = flag
        if (flag === 'edit') {
          this.dialogTitle = '编辑名单'
          this.status = true
//          var length = this.multipleSelection.length
//          if (length !== 1) {
//            this.$message('请选择一行名单信息。')
//            return
//          }
//          this.listDialogform = util.extend({}, this.multipleSelection[0])
          this.listDialogform =  Object.assign({}, this.selectedRow)
        } else if (flag === 'add') {
          this.dialogTitle = '新建名单'
          this.status = false
          if (this.$refs['listDialogform']) {
            this.$refs['listDialogform'].resetFields();
          } else {
            this.listDialogform = {
              rostername: "",
              rosterdesc: "",
              datatype: "",
              rostertype: "",
              iscache: "1",
              remark: "",
            }
          }
        }
        this.listDialogVisible = true
        if (this.$refs['listDialogform']) {
          this.$refs['listDialogform'].clearValidate()
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
        if (val.length === 1){
          this.btnStatus = false
          this.delBtnStatus = false
        } else if (val.length > 1){
          this.btnStatus = true
          this.delBtnStatus = false
        } else {
          this.btnStatus = true
          this.delBtnStatus = true
        }
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            if (this.flag === 'add') {
              this.add()
            } else if (this.flag === 'edit') {
              this.update()
            }
          } else {
            return false;
          }
        })
      },
      bindGridData(data) {
        this.gridData = data.page.list
        this.pageindex = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      add() {
        var self = this
        ajax.post({
          url: '/mgr/add',
          param: this.listDialogform,
          success: function (data) {
            self.$message({
              type: 'success',
              message: '创建成功'
            })
            self.listDialogVisible = false
            self.sel()
          }
        })
      },
      del() {
        var self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/mgr/del',
            param: {
//              del: this.multipleSelection
              del: [this.selectedRow]
            },
            success: function (data) {
              self.$message({
                type: 'success',
                message: '删除成功'
              })
              self.sel()
            }
          })
        }).catch(() => {
        })
      },
      sel(pageinfo) {
        var self = this;
        var param;
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = Object.assign({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm, pageinfo)
        } else {
          param = Object.assign({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.listForm)
        }

        ajax.post({
          url: '/mgr/list',
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
        ajax.post({
          url: '/mgr/mod',
          param: this.listDialogform,
          success: function (data) {
            self.$message({
              type: 'success',
              message: '编辑成功'
            })
            self.listDialogVisible = false
            self.sel()
          }
        })
      },
      showValueList() {
//        var rosterid = this.multipleSelection[0].rosterid
//        var datatype = this.multipleSelection[0].datatype
        var rosterid = this.selectedRow.rosterid
        var datatype = this.selectedRow.datatype
        this.$router.push({ name: 'valuelist', query: { rosterid: rosterid, datatype: datatype}})
      },
      importList() {
        this.fileList = []
        this.importDialogVisible = true
      },
      exportList() {
//        var row = this.multipleSelection[0]
        if(this.selectedRow.valuecount === 0){
          this.$message('名单中没有名单值');
          return;
        }
        this.exportDialogVisible = true
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
      submitUpload() {
        let self = this
        let format = `.${this.file.name.split('.').pop()}`
        let row = this.selectedRow
        this.uploadForm = new FormData()
        this.uploadForm.append('importFile', this.file.raw)
        this.uploadForm.append('rosterid',  row.rosterid)
        this.uploadForm.append('rosterdesc',  row.rosterdesc)
        this.uploadForm.append('format',  format)
        ajax.post({
          url: '/mgr/import',
          param: self.uploadForm,
          success: function (data) {
            self.importDialogVisible = false
            self.$message({
              type: 'success',
              message: '导入成功'
            })
          }
        })
      },
      handleChange(file, fileList) {
        this.fileList = [file]
        this.file = file
      },
      exportAction(flag) {
//        let row = this.multipleSelection[0]
        let row = this.selectedRow
        let rosterId = `&rosterid=${row.rosterid}&rosterdesc=${row.rosterdesc}`
        let params = util.serializeObj(this.listForm) + rosterId + "&flag="+flag;
        let url = '/context/manager/mgr/export?' + params;
        window.open(url);
        this.exportDialogVisible = false
      },
      handleRow(index, row, oper) {
        this.selectedRow = row
        switch(oper)
        {
          case 'edit':
            return this.openDialog('edit')
            break;
          case 'del':
            return this.del()
            break;
          case 'valueList':
            return this.showValueList()
            break;
          case 'upload':
            return this.importList()
            break;
          case 'download':
            return this.exportList()
            break;
          default:
            return
            break;
        }
      }
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
        total: 0,
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
        delBtnStatus: true,
        dialogTitle: '',
        formLabelWidth: '120px',
        datatypeOptions: [],
        rostertypeOptions: [],
        rules: {
          rostername: [
            { required: true, message: '请输入名单英文名', trigger: 'blur' },
            { max: 32, message: '长度在32个字符以内', trigger: 'blur' },
            { validator: check.checkFormEnSpecialCharacter, trigger: 'blur' }
          ],
          listFormFosterdesc: [
            { max: 64, message: '长度在64个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          rosterdesc: [
            { required: true, message: '请输入名单名称', trigger: 'blur' },
            { max: 64, message: '长度在64个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          datatype: [
            { required: true, message: '请输入名单数据类型', trigger: 'blur' }
          ],
          rostertype: [
            { required: true, message: '请输入名单类型', trigger: 'blur' }
          ],
          remark: [
            { max: 512, message: '长度在512个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        multipleSelection: [],
        flag: '',
        importDialogVisible: false,
        exportDialogVisible: false,
        fileList: [],
        multiple: false,
        selectedRow: {},
        closeOnClickModal: false,
        showOverflow: true
      }
    }
  }
</script>

<style>
</style>
