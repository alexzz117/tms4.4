<template>
  <el-container style="height: 100%; border: 1px solid #eee">
    <el-aside width="60%" >
      <div style="margin: 10px;text-align: left ">
        <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
      </div>
      <el-table
        :data="timeData"
        @selection-change="handleSelectionChange">
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button
              icon="el-icon-edit"
              size="mini"
              type="text"
              @click="handleEdit(scope.$index, scope.row)"></el-button>
            <el-button
              icon="el-icon-delete"
              size="mini"
              type="text"
              @click="handleDelete(scope.$index, scope.row)"></el-button>
          </template>
        </el-table-column>
        <el-table-column label="任务启停" width="80">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              active-value= "NORMAL"
              inactive-value= "PAUSED"
              @change="statusChange(scope.row)">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="任务名称" align="left"></el-table-column>
        <el-table-column prop="cron" label="时间表达式" align="left"></el-table-column>
        <el-table-column prop="group" label="任务组" align="left" :formatter="formatter"></el-table-column>
        <el-table-column prop="className" label="类名" align="left" :formatter="formatter"></el-table-column>
      </el-table>
    </el-aside>
    <el-main>
      <el-form :model="timerForm" :rules="rules" ref="timerForm">
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
        <el-form-item label="描述信息" :label-width="formLabelWidth" prop="description">
          <el-input type="textarea" v-model="timerForm.description"></el-input>
        </el-form-item>
        <el-form-item label=" " :label-width="formLabelWidth" size="large">
          <el-button type="primary" @click="submitForm('timerDialogForm')">保 存</el-button>
        </el-form-item>
      </el-form>
    </el-main>
    <el-dialog title="新建定时任务" :visible.sync="timerDialogVisible">
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
        <el-form-item label="描述信息" :label-width="formLabelWidth" prop="description">
          <el-input type="textarea" v-model="timerDialogForm.description"></el-input>
        </el-form-item>
        <el-form-item label=" " :label-width="formLabelWidth" size="large">
          <el-button type="primary" @click="submitForm('timerDialogForm')">保 存</el-button>
          <el-button @click="timerDialogVisible = false">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-container>
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
      var nameExist = function (rule, value, callback) {
        if (this.flag === 'add') {
          return self.checkName(self.timerDialogForm.name, callback)
        } else if (this.flag === 'edit') {
          if (value !== self.selectionRow.name) {
            return self.checkName(self.timerDialogForm.name, callback)
          }
        }
        return callback()
      }
      return {
        timeData: [],
        formLabelWidth: '100px',
        timerForm: {
          "name": "",
          "cron": "",
          "className": "",
          "group": "",
          "description": ""
        },
        timerDialogForm: {
          "name": "",
          "cron": "",
          "className": "",
          "group": "",
          "description": ""
        },
        flag: '',
        timerDialogVisible: false,
        rules: {
          name: [
            { required: true, message: '请输入任务名称', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: nameExist, trigger: 'blur' }
          ],
          cron: [
            { required: true, message: '请输入cron', trigger: 'blur' }
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
        selectionRow: {},
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
          success: function (data) {
            if (data.list) {
              self.timeData = data.list
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
          url: '/timer/reschedule',
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
      handleEdit(index, row) {
        this.flag = 'edit'
        this.timerForm = row
      },
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
      handleSelectionChange(val) {
        this.selectionRow = val;
      },
      checkName(name, callback) {
        ajax.post({
          url: '/timer/check-name',
          model: ajax.model.timer,
          param: {
            name: name
          },
          success: function (data) {
            if('false' === data){
              callback(new Error('任务名称已被占用'));
            }else{
              callback();
            }
          }
        })
      },
      statusChange(row) {
        var self = this
        if (row.status === 'NORMAL') {
          ajax.post({
            url: '/timer/resume',
            model: ajax.model.timer,
            param: {
              taskId: row.taskid
            },
            success: function (data) {
              self.$message({
                message: '启动成功',
                type: 'success'
              })
            }
          })
        } else if (row.status === 'PAUSED') {
          ajax.post({
            url: '/timer/pause',
            model: ajax.model.timer,
            param: {
              taskId: row.taskid
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
      }
    }
  }
</script>
<style>
</style>
