<template>
  <div style="min-height: 85%">
    <div class="toolbar">
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="9" style="height: 400px;">
        <section class="section">
          <el-table
            :data="timeData"
            highlight-current-row
            @current-change="handleCurrentChange"
            ref="timeTable">
            <!--@selection-change="handleSelectionChange"-->

            <el-table-column label="操作" width="50">
              <template slot-scope="scope">
                <!--<el-button-->
                <!--icon="el-icon-edit"-->
                <!--type="text"-->
                <!--@click="handleEdit(scope.$index, scope.row)"-->
                <!--title="编辑"></el-button>-->
                <el-button
                  icon="el-icon-delete"
                  type="text"
                  @click="handleDelete(scope.$index, scope.row)"
                  title="删除"></el-button>
              </template>
            </el-table-column>
            <!--<el-table-column label="任务启停" width="80">-->
            <!--<template slot-scope="scope">-->
            <!--<el-switch-->
            <!--v-model="scope.row.status"-->
            <!--active-value= "NORMAL"-->
            <!--inactive-value= "PAUSED"-->
            <!--@change="statusChange(scope.row)">-->
            <!--</el-switch>-->
            <!--</template>-->
            <!--</el-table-column>-->
            <el-table-column prop="name" label="任务名称" align="left"></el-table-column>
            <!--<el-table-column prop="cron" label="时间表达式" align="left"></el-table-column>-->
            <el-table-column prop="group" label="任务组" align="left" :formatter="formatter"></el-table-column>
            <!--<el-table-column prop="className" label="类名" align="left" :formatter="formatter"></el-table-column>-->
          </el-table>
        </section>
      </el-col>
      <el-col :span="15" style="height: 400px;">
        <section class="section">
          <el-form :model="timerForm" :rules="rules" ref="timerForm" style="margin: 20px 20px 0px 10px;">
            <el-form-item label="任务名称" :label-width="formLabelWidth" prop="name">
              <el-input v-model="timerForm.name" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="时间表达式" :label-width="formLabelWidth" prop="cron">
              <el-input v-model="timerForm.cron" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="类名" :label-width="formLabelWidth" prop="className">
              <el-select v-model="timerForm.className" placeholder="请选择"
                         :clearable="clearable">
                <el-option
                  v-for="item in classOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="任务组" :label-width="formLabelWidth" prop="group">
              <el-select v-model="timerForm.group" placeholder="请选择"
                         :clearable="clearable">
                <el-option
                  v-for="item in groupOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="是否启动" :label-width="formLabelWidth" prop="status">
              <el-switch v-model="timerForm.status"
                         active-value= "NORMAL"
                         inactive-value= "PAUSED"
                         @change="statusChange()">
              </el-switch>
            </el-form-item>
            <el-form-item label="描述信息" :label-width="formLabelWidth" prop="description">
              <el-input type="textarea" v-model="timerForm.description"></el-input>
            </el-form-item>
            <el-form-item label=" " :label-width="formLabelWidth">
              <el-button type="primary" @click="submitForm('timerForm')">保 存</el-button>
            </el-form-item>
          </el-form>
        </section>
      </el-col>
    </el-row>
    <el-dialog title="新建定时任务" :visible.sync="timerDialogVisible" width="30%">
      <el-form :model="timerDialogForm" :rules="rules" ref="timerDialogForm">
        <el-form-item label="任务名称" :label-width="formLabelWidth" prop="name">
          <el-input v-model="timerDialogForm.name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="时间表达式" :label-width="formLabelWidth" prop="cron">
          <el-input v-model="timerDialogForm.cron" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="类名" :label-width="formLabelWidth" prop="className">
          <el-select v-model="timerDialogForm.className" placeholder="请选择"
                     :clearable="clearable">
            <el-option
              v-for="item in classOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务组" :label-width="formLabelWidth" prop="group">
          <el-select v-model="timerDialogForm.group" placeholder="请选择"
                     :clearable="clearable">
            <el-option
              v-for="item in groupOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否启动" :label-width="formLabelWidth" prop="status">
          <el-switch v-model="timerDialogForm.status"
                     active-value= "NORMAL"
                     inactive-value= "PAUSED">
          </el-switch>
        </el-form-item>
        <el-form-item label="描述信息" :label-width="formLabelWidth" prop="description">
          <el-input type="textarea" v-model="timerDialogForm.description"></el-input>
        </el-form-item>
        <el-form-item label=" " :label-width="formLabelWidth">
          <el-button type="primary" @click="submitForm('timerDialogForm')">保 存</el-button>
          <el-button @click="timerDialogVisible = false">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>

</template>

<script>
  import util from '@/common/util'
  import ajax from '@/common/ajax'
  import check from '@/common/check'

  export default {
    created () {
      var vm = this
      var getOptions = function(url) {
        var promise = new Promise(function(resolve, reject){
          ajax.get({
            url: url,
            model: ajax.model.timer,
            success: function (data) {
              if (data.list) {
                resolve(data.list)
              } else {
                resolve([])
              }
            },
            error: function (data) {
              reject(new Error(data));
            },
            fail: function (error) {
              reject(new Error(error));
            }
          })
        })
        return promise
      }

      var classP = getOptions("/timer/classes").then(function(list) {
        vm.classOptions = list
      }, function(error) {
        console.error(error);
      });

      var groupsP = getOptions("/timer/groups").then(function(list) {
        vm.groupOptions = list
      }, function(error) {
        console.error(error);
      });

      Promise.all([classP, groupsP]).then(function(data) {
        vm.sel()
      }).catch(function(error){
        console.error(error);
      });
    },
    data () {
      var self = this
      var checkName = function (rule, value, callback) {
        if (self.flag === 'add') {
          return self.checkName(self.timerDialogForm.name, callback)
        } else if (self.flag === 'edit') {
          if (value !== self.selectedRow.name) {
            return self.checkName(self.timerForm.name, callback)
          }
        }
        return callback()
      }
      var checkCron = function (rule, value, callback) {
        ajax.post({
          url: '/timer/check-cron',
          model: ajax.model.timer,
          param: {
            cron: value
          },
          success: function (data) {
            if(false === data.row){
              callback(new Error('请输入合法的时间表达式'));
            }else{
              callback();
            }
          }
        })
      }
      return {
        timeData: [],
        formLabelWidth: '100px',
        timerForm: {
          "name": "",
          "cron": "",
          "className": "",
          "group": "",
          "status": "PAUSED",
          "description": ""
        },
        timerDialogForm: {
          "name": "",
          "cron": "",
          "className": "",
          "group": "",
          "status": "NORMAL",
          "description": ""
        },
        flag: '',
        timerDialogVisible: false,
        rules: {
          name: [
            { required: true, message: '请输入任务名称', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: checkName, trigger: 'blur' }
          ],
          cron: [
            { required: true, message: '请输入时间表达式', trigger: 'blur' },
            { validator: checkCron, trigger: 'blur' }
          ],
          className: [
            { required: true, message: '请选择所属类', trigger: 'blur' }
          ],
          group: [
            { max: 100, message: '长度在100个字符以内', trigger: 'blur' }
          ],
          description: [
            { max: 300, message: '长度在300个字符以内', trigger: 'blur' }
          ]
        },
        selectedRow: {},
        classOptions: [],
        groupOptions: [],
        clearable: true
      }
    },
    methods: {
      sel () {
        let self = this
        ajax.get({
          url: '/timer/list',
          model: ajax.model.timer,
          toLowerCase: false,
          success: function (data) {
            if (data.list) {
              self.timeData = data.list
              if (data.list.length > 0) {
                self.setCurrent(self.timeData[0])
              }
            }
          }
        })
      },
      add () {
        var self = this
        ajax.post({
          url: '/timer/add',
          model: ajax.model.timer,
          param: this.timerDialogForm,
          success: function (data) {
            self.$message({
              message: '创建成功',
              type: 'success'
            })
            self.timerDialogVisible = false
            self.sel()
          }
        })
      },
      update () {
        var self = this;
        ajax.post({
          url: '/timer/update',
          model: ajax.model.timer,
          param: this.timerForm,
          success: function (data) {
            self.$message({
              message: '更新成功',
              type: 'success'
            })
            self.sel()
          }
        })
      },
//      handleEdit(index, row) {
//        this.flag = 'edit'
//        this.timerForm = row
//      },
      handleDelete(index, row) {
        var self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/timer/delete',
            model: ajax.model.timer,
            param: row,
            success: function (data) {
              self.$message({
                type: 'success',
                message: '删除成功!'
              })
              self.sel()
            }
          })
        }).catch(() => {
          self.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      },
      openDialog(flag) {
        this.flag = flag
        if (flag === 'add') {
          if (this.$refs['timerDialogForm']) {
            this.$refs['timerDialogForm'].resetFields();
          } else {
            this.timerDialogForm = {
              "name": "",
              "cron": "",
              "className": "",
              "group": "",
              "status": "NORMAL",
              "description": ""
            }
          }
        }
        this.timerDialogVisible = true
        if (this.$refs['timerDialogForm']) {
          this.$refs['timerDialogForm'].clearValidate()
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
//      handleSelectionChange(val) {
//        this.selectedRow = val;
//      },
      checkName(name, callback) {
        ajax.post({
          url: '/timer/check-name',
          model: ajax.model.timer,
          param: {
            name: name
          },
          success: function (data) {
            if (false === data.row) {
              callback(new Error('任务名称已被占用'));
            } else {
              callback();
            }
          }
        })
      },
      statusChange() {
        var self = this
        if (this.timerForm.status === 'NORMAL') {
          ajax.post({
            url: '/timer/resume',
            model: ajax.model.timer,
            param: {
              taskId: this.timerForm.taskid
            },
            success: function (data) {
              self.$message({
                message: '启动成功',
                type: 'success'
              })
            }
          })
        } else if (this.timerForm.status === 'PAUSED') {
          ajax.post({
            url: '/timer/pause',
            model: ajax.model.timer,
            param: {
              taskId: this.timerForm.taskid
            },
            success: function (data) {
              self.$message({
                message: '停止成功',
                type: 'success'
              })
            }
          })
        }
      },
      formatter(row, column, cellValue) {
        switch(column.property )
        {
          case 'className':
            return util.renderCellValue(this.classOptions, cellValue)
            break;
          case 'group':
            return util.renderCellValue(this.groupOptions, cellValue)
            break;
          default:
            return cellValue
            break;
        }
      },
      handleCurrentChange(val) {
        this.flag = 'edit'
        this.timerForm = Object.assign({}, val)
        this.selectedRow = val
      },
      setCurrent(row) {
        this.$refs.timeTable.setCurrentRow(row);
      }
    }
  }
</script>
<style>
  .section {
    padding: 20px;
    background-color: #FFFFFF;
    border: 1px solid rgba(112, 112, 112, 0.12);
    border-radius: 7px;
    height:100%;
  }

</style>
