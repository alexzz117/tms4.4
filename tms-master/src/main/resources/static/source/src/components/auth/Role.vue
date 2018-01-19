<template>
  <div>
    <div class="toolbar">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      <el-form label-position="right" label-width="80px" :model="roleForm"
               :inline="inline" class="toolbar-form">
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.role_name" :clearable="clearable"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roleForm.flag" @focus="selectFocus()" placeholder="请选择" :clearable="clearable">
            <el-option
              v-for="item in flagOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="">
          <el-button class="el-icon-search" type="primary" @click="sel">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!--<div style="margin-bottom: 10px;text-align: left ">-->
      <!--<el-button plain class="el-icon-edit" @click="openDialog('edit')" :disabled="btnStatus">编辑</el-button>-->
      <!--<el-button plain class="el-icon-delete" @click="del" :disabled="btnStatus">删除</el-button>-->
      <!--<el-button plain class="el-icon-setting" @click="grant" :disabled="btnStatus">功能授权</el-button>-->
    <!--</div>-->
    <section class="table">
      <el-table
        :data="roleData"
        @selection-change="handleSelectionChange">
        <!--<el-table-column type="selection" width="55" align="left"></el-table-column>-->
        <el-table-column label="操作" width="100">
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
              icon="el-icon-setting"
              type="text"
              @click="handleRow(scope.$index, scope.row, 'grant')"
              title="功能授权"></el-button>
          </template>
        </el-table-column>
        <el-table-column prop="role_name" label="角色名称" align="left" width="180"></el-table-column>
        <el-table-column prop="flag" label="状态" align="left" width="180" :formatter="formatter"></el-table-column>
        <el-table-column prop="info" label="描述信息" align="left" :show-overflow-tooltip="showOverflow"></el-table-column>
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

    <el-dialog :title="dialogTitle" :visible.sync="roleDialogVisible" width="35%" :close-on-click-modal="closeOnClickModal">
      <el-form :model="roleDialogform" :rules="rules" ref="roleDialogform">
        <el-form-item label="角色名称" :label-width="formLabelWidth" prop="role_name">
          <el-input v-model="roleDialogform.role_name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="状态" :label-width="formLabelWidth" style="text-align: left;">
          <el-radio v-model="roleDialogform.flag" label="1">正常</el-radio>
          <el-radio v-model="roleDialogform.flag" label="0">停用</el-radio>
        </el-form-item>
        <el-form-item label="描述信息" :label-width="formLabelWidth" prop="info">
          <el-input type="textarea" v-model="roleDialogform.info"></el-input>
        </el-form-item>
        <el-form-item label="" :label-width="formLabelWidth">
          <el-button type="primary" @click="submitForm('roleDialogform')">保 存</el-button>
          <el-button @click="roleDialogVisible = false">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog title="功能授权" :visible.sync="grantDialogVisible" width="35%" :close-on-click-modal="closeOnClickModal">
      <div style="overflow: auto; max-height: 500px">
        <el-tree
          :data="grantData"
          :default-expanded-keys="expendKey"
          :default-checked-keys="checkedKeys"
          show-checkbox
          node-key="id"
          ref="grantTree"
          highlight-current
          :props="defaultProps">
        </el-tree>
        <div style="text-align: left; margin: 20px">
          <el-button type="primary" @click="saveGrant">保 存</el-button>
          <el-button @click="grantDialogVisible = false">取 消</el-button>
        </div>
      </div>

    </el-dialog>
  </div>
</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import check from '@/common/check'
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
          this.dialogTitle = '编辑角色'
//          var length = this.multipleSelection.length
//          if (length !== 1) {
//            this.$message('请选择一行角色信息。')
//            return
//          }
//          this.roleDialogform = util.extend({}, this.multipleSelection[0])
          this.roleDialogform =  Object.assign({}, this.selectedRow)
        } else if (flag === 'add') {
          this.dialogTitle = '新建角色'
          if (this.$refs['roleDialogform']) {
            this.$refs['roleDialogform'].resetFields();
          } else {
            this.roleDialogform = {
              role_name: '',
              flag: '1',
              info: ''
            }
          }
        }
        this.roleDialogVisible = true
        if (this.$refs['roleDialogform']) {
          this.$refs['roleDialogform'].clearValidate()
        }
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
        if (val.length === 1){
          this.btnStatus = false
        } else if (val.length > 1){
          this.btnStatus = true
        } else {
          this.btnStatus = true
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
        this.roleData = data.page.list
        this.currentPage = data.page.index
        this.pagesize = data.page.size
        this.total = data.page.total
      },
      add() {
        var self = this
        ajax.post({
          url: '/role/add',
          param: this.roleDialogform,
          success: function (data) {
            self.$message({
              type: 'success',
              message: '创建成功'
            })
            self.roleDialogVisible = false
            self.sel()
          }
        })
      },
      del() {
        var self = this
//        var data = this.multipleSelection[0]
        var data = this.selectedRow
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
          url: '/role/del',
          param: {
            roleid: data.role_id
          },
          success: function (data) {
            self.$message({
              type: 'success',
              message: '删除成功'
            })
            self.sel()
          }
        })
      }).catch(() => {})
      },
      sel(pageinfo) {
        var self = this;
        var param;
        if (pageinfo && (pageinfo.pageindex || pageinfo.pagesize)) {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.roleForm, pageinfo)
        } else {
          param = util.extend({
            pageindex:this.pageindex,
            pagesize:this.pagesize
          }, this.roleForm)
        }

        ajax.post({
          url: '/role/list',
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
          url: '/role/mod',
          param: this.roleDialogform,
          success: function (data) {
            self.$message({
              type: 'success',
              message: '编辑成功'
            })
            self.roleDialogVisible = false
            self.sel()
          }
        })
      },
      grant() {
        var self = this
        var data = this.selectedRow
        ajax.post({
          url: '/role/grant/list',
          param: {
            roleid: data.role_id
          },
          success: function (data) {
            if (data.list) {
              var rootNodes = { // 根节点
                id: '-1',
                fid: '',
                func_type: '-1',
                flag: '1',
                ftype_name: '',
                text: '功能树',
                icon: 'ticon-root',
                onum: 0
              }
              var treeList = [rootNodes]
              treeList = treeList.concat(data.list)
              self.treeList = treeList
              self.grantData = util.formatTreeData(treeList)
              self.expendKey = util.expendNodesByLevel(self.grantData, 2)
              self.checkedKeys = util.checkKeys(self.grantData, 'grant', '1')
              self.grantDialogVisible = true
            }
          }
        })
      },
      saveGrant () {
        var self = this
        ajax.post({
          url: '/role/grant/mod',
          param: {
            roleid: this.selectedRow.role_id,
            funcs: this.$refs.grantTree.getCheckedNodes(true)
          },
          success: function (data) {
            self.$message({
              type: 'success',
              message: '编辑成功'
            })
            self.grantDialogVisible = false
            self.sel()
          }
        })
      },
      selectFocus () {
        this.flagOptions = dictCode.getCodeItems('cmc.cmc_role.flag')
      },
      formatter(row, column, cellValue) {
        switch(column.property )
        {
          case 'flag':
            return dictCode.rendCode('cmc.cmc_role.flag', cellValue)
            break;
          default:
            return cellValue
            break;
        }
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
          case 'grant':
            return this.grant()
            break;
          default:
            return
            break;
        }
      }
    },
    data() {
      var self = this
      var roleNameExist = function (rule, value, callback) {
        if (value === self.selectedRow.role_name) {
          return callback();
        }
        ajax.post({
          url: '/role/checkUserName',
          param: {
            role_name: self.roleDialogform.role_name
          },
          success: function (data) {
            if('false' === data.check_result){
              callback(new Error('角色名称已被占用'));
            }else{
              callback();
            }
          }
        })
      }
      return {
        inline: true,
        roleForm: {
          role_name:''
        },
        roleData: [],
        pageindex: 1,
        pagesize: 10,
        total: 100,
        roleDialogVisible: false,
        grantDialogVisible: false,
        roleDialogform: {
          role_name: '',
          flag: '1',
          info: ''
        },
        dialogTitle: '',
        formLabelWidth: '100px',
        btnStatus: true,
        rules: {
          role_name: [
            { required: true, message: '请输入角色名称', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' },
            { validator: roleNameExist, trigger: 'blur' }
          ],
          info: [
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        },
        multipleSelection: [],
        flag: '',
        flagOptions: [],
        clearable: true,
        grantData: [],
        expendKey: ['-1'], // 默认展开的功能节点id
        checkedKeys: [],   // 默认选中功能
        defaultProps: {
          children: 'children',
          label: 'text'
        },
        selectedRow: {},
        closeOnClickModal: false,
        showOverflow: true
      }
    }
  }
</script>
