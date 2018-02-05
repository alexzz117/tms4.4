<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" style="min-height: 400px;max-height: 700px;">
        <section class="toolbar">
          <el-button plain class="el-icon-plus" @click="addNode" style="margin-left: 5px;" :disabled="!selectOneNode">新建
          </el-button>
          <el-button plain class="el-icon-delete" @click="delNode" style="margin-left: 0px;" :disabled="canNotDel">删除
          </el-button>
        </section>
        <section class="section scroll">
          <el-tree
            ref="tree"
            :props="treeProps"
            :load="loadNode1"
            :check-strictly="true"
            node-key="path"
            @node-click="nodeClick"
            @check-change="treeCheckChange"
            show-checkbox
            lazy>
          </el-tree>
        </section>
      </el-col>
      <el-col :span="18" style="min-height: 400px;max-height: 700px;">
        <section class="table">
          <el-table
            :data="tableData"
            style="width: 100%"
            @selection-change="tableSelectionChange">

            <el-table-column
              align="left"
              type="selection"
              width="55">
            </el-table-column>

            <el-table-column prop="label" label="Name" align="left"></el-table-column>
            <el-table-column prop="value" label="Value" align="left"></el-table-column>

          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" :close-on-click-modal="false" width="330px">
      <el-form :model="dialogForm" :rules="rules" ref="dialogForm" style="text-align: left" :inline="true">
        <el-form-item label="Name:" prop="label">
          <el-input v-model="dialogForm.label" auto-complete="off" :maxlength="50"></el-input>
        </el-form-item>
        <el-form-item v-show="dialogType === 'prop'" label="Value:" :label-width="formLabelWidth" prop="value">
          <el-input v-model="dialogForm.value" auto-complete="off"></el-input>
        </el-form-item>
        <div>
          <el-form-item label=" " :label-width="formLabelWidth">
            <el-button type="primary" @click="submitForm('dialogForm')" size="large">保 存</el-button>
            <el-button @click="dialogVisible = false" size="large">取 消</el-button>
          </el-form-item>
        </div>

      </el-form>
    </el-dialog>

  </div>
</template>
<script>
  import ajax from "../../common/ajax";

  export default {
    computed: {
      selectOneNode(){
        return this.selectedTreeData.length === 1
      },
      canNotDel(){
        return this.selectedTreeData.length !== 1 || this.selectedTreeData[0].root
      }
    },
    data () {
      return {
        formLabelWidth: '130px',
        dialogTitle:'',
        dialogVisible:false,
        dialogType:'',
        dialogForm:{
          label:'',
          value:''
        },
        rules:{
          label:[
            { required: true, message: '请输入Name', trigger: 'blur' }
          ]
        },
        treeProps: {
          label: 'label',
          children: 'children',
          isLeaf: 'leaf'
        },
        tableData:[],
        selectedTableData:[],
        selectedTreeData:[],
        treeParentMap : new Map()
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        // this.reloadData()
        // this.loadNodesData()
      })
    },
    methods: {
      initDialogForm(){
        return {
          label:'',
          value:''
        }
      },
      addNode(){
        this.dialogForm = this.initDialogForm()
        this.dialogVisible = true
        this.dialogType = 'node'
      },
      delNode(){
        if(this.selectedTreeData[0].isRoot) {
          return
        }
        let self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/zookeeper/delete',
            loading: true,
            param: {
              path: this.selectedTreeData[0].path
            },
            success: function (data) {
              self.$message.success('删除成功')

              let subList = self.treeParentMap.get(self.selectedTreeData[0].path).subList
              let subListTemp = []
              for(let item of subList) {
                if(item.path !== self.selectedTreeData[0].path) {
                  subListTemp.push(item)
                }
              }
              self.treeParentMap.get(self.selectedTreeData[0].path).subList = subListTemp
              self.$refs['tree'].updateKeyChildren(self.treeParentMap.get(self.selectedTreeData[0].path).path, subListTemp)
              self.selectedTreeData = []
            }
          })
        })
      },
      submitForm(formName){
        let self = this
        if(this.dialogType === 'node') {
          this.$refs[formName].validate((valid) => {
            if (valid) {
              let path = this.selectedTreeData[0].path + "/" + this.dialogForm.label
              let params = {
                label: this.dialogForm.label,
                path: path,
                type: 'node'
              }
              ajax.post({
                url: '/zookeeper/set',
                loading:true,
                param: params,
                success: function (data) {
                  self.dialogVisible = false
                  let subList = self.selectedTreeData[0].subList
                  if(subList) {
                    subList.push({
                      label: self.dialogForm.label,
                      path: path
                    })
                    self.$refs['tree'].updateKeyChildren(self.selectedTreeData[0].path, subList)
                  }
                  self.treeParentMap.set(path, self.selectedTreeData[0])
                  self.$message.success('新建成功')
                }
              })
            }
          })
        } else {

        }
      },
      nodeClick(data){
        this.tableData = data.valueList
      },
      treeCheckChange(data, checked, indeterminate) {
        this.selectedTreeData = this.$refs.tree.getCheckedNodes()
      },
      tableSelectionChange (rows) {
        this.selectedTableData = rows
      },

      loadNode1(node, resolve) {
        let self = this
        let params = {}
        if(node.level !== 0) {
          params = node.data
        }

        ajax.post({
          url: '/zookeeper/list',
          param: params,
          success: function (data) {
            if(node.level === 0) {
              resolve(data.row)
            } else {
              let dataRow = data.row
              let valueList = []
              let treeList = []
              for(let item of dataRow) {
                self.treeParentMap.set(item.path, node.data)
                if(item.leaf) {
                  valueList.push(item)
                } else {
                  treeList.push(item)
                }
              }
              node.data.valueList = valueList
              node.data.subList = treeList
              self.tableData = valueList
              resolve(treeList)
            }
          }
        })

        // if (node.level > 1) return resolve([]);
        //
        // setTimeout(() => {
        //   const data = [{
        //     label: 'leaf',
        //     leaf: true
        //   }, {
        //     label: 'zone'
        //   }];
        //
        //   resolve(data);
        // }, 500);
      }
    }
  }
</script>
<style>

</style>
