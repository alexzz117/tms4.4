<template>
  <div>
    <div class="toolbar">
      <el-button plain class="el-icon-plus" @click="addFunc" :disabled="toolBtn.addBtn">新建
      </el-button>
      <el-button plain class="el-icon-edit" @click="editFunc" :disabled="toolBtn.editBtn">编辑
      </el-button>
      <el-button plain class="el-icon-delete" @click="delFunc" :disabled="toolBtn.delBtn">删除
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" style="height: 650px;">
        <!--<div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee;">-->
          <!--<el-button plain class="el-icon-plus" @click="addFunc" :disabled="toolBtn.addBtn" style="margin-left: 5px;">新建-->
          <!--</el-button>-->
          <!--<el-button plain class="el-icon-edit" @click="editFunc" :disabled="toolBtn.editBtn" style="margin-left: 0px;">编辑-->
          <!--</el-button>-->
          <!--<el-button plain class="el-icon-delete" @click="delFunc" :disabled="toolBtn.delBtn" style="margin-left: 0px;">删除-->
          <!--</el-button>-->
        <!--</div>-->
        <section class="section">
          <el-tree :data="treeData" node-key="id" ref="tree"
                   :default-expanded-keys="expendKey"
                   :props="defaultProps"
                   :highlight-current=true
                   :expand-on-click-node="false"
                   @node-click="handleNodeClick"
                   :render-content="renderContent">
          </el-tree>
        </section>

      </el-col>
      <el-col :span="18" style="height: 650px;">
        <section class="section">
          <div style="height:38px;width:100%;text-align: left;">
            <el-breadcrumb id="funcPath" separator=">" style="padding: 10px 15px;">
              <el-breadcrumb-item v-for="item in breadcrumbData" :key="item.text">
                {{ item.text }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <el-form :model="funcForm"
                   :rules="rules"
                   ref="funcForm"
                   label-suffix="："
                   :label-width="formLabelWidth"
                   style="text-align: left;"
                   v-bind:class="{hidden:funcFormVisible.form}">
            <el-form-item label="功能编号" prop="func_id" v-bind:class="{hidden:funcFormVisible.func_id}">
              <el-input v-model="funcForm.func_id" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="父节点编号" prop="parent_id" v-bind:class="{hidden:funcFormVisible.parent_id}">
              <el-input v-model="funcForm.parent_id" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="功能类型" prop="func_type" v-bind:class="{hidden:funcFormVisible.func_type}">
              <el-input v-model="funcForm.func_type" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="节点名称" prop="func_name" v-bind:class="{hidden:funcFormVisible.func_name}">
              <el-input v-model="funcForm.func_name" auto-complete="off" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item label="节点配置" prop="conf" v-bind:class="{hidden:funcFormVisible.conf}">
              <el-input type="textarea" v-model="funcForm.conf" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item label="节点类型" prop="ftype_name" v-bind:class="{hidden:funcFormVisible.ftype_name}">
              <el-input v-model="funcForm.ftype_name" auto-complete="off" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item label="访问授权" prop="isgrant" v-bind:class="{hidden:funcFormVisible.isgrant}">
              <el-checkbox label="需要授权" name="type" v-model="funcForm.isgrant"
                           :disabled="funcFormReadonly"></el-checkbox>
            </el-form-item>
            <el-form-item label="是否菜单" v-bind:class="{hidden:funcFormVisible.menu}">
              <el-radio v-model="funcForm.menu" label="1" :disabled="funcFormReadonly">是</el-radio>
              <el-radio v-model="funcForm.menu" label="0" :disabled="funcFormReadonly">否</el-radio>
            </el-form-item>
            <el-form-item label="状态" v-bind:class="{hidden:funcFormVisible.flag}">
              <el-radio v-model="funcForm.flag" label="1" :disabled="funcFormReadonly">正常</el-radio>
              <el-radio v-model="funcForm.flag" label="0" :disabled="funcFormReadonly">停用</el-radio>
            </el-form-item>
            <el-form-item label="顺序" prop="onum" v-bind:class="{hidden:funcFormVisible.onum}">
              <el-input v-model="funcForm.onum" auto-complete="off" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item label="描述信息" prop="info" v-bind:class="{hidden:funcFormVisible.info}">
              <el-input type="textarea" v-model="funcForm.info" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item label="记录日志" prop="islog" v-bind:class="{hidden:funcFormVisible.islog}">
              <el-checkbox label="记录" name="type" v-model="funcForm.islog" :disabled="funcFormReadonly"></el-checkbox>
            </el-form-item>
            <el-form-item label="日志记录配置" prop="logconf" v-bind:class="{hidden:funcFormVisible.logconf}">
              <el-input v-model="funcForm.logconf" auto-complete="off" :readonly="funcFormReadonly"></el-input>
            </el-form-item>
            <el-form-item v-bind:class="{hidden:funcFormReadonly}">
              <el-button type="primary" @click="submitFunc" size="large">保 存</el-button>
              <el-button @click="closeFuncForm" size="large">取 消</el-button>
            </el-form-item>
          </el-form>
        </section>
      </el-col>
    </el-row>
  </div>
</template>


<script>
  import check from '@/common/check'
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  export default {
    created () {
      this.selTree()
    },
    data () {
      // 功能名称重复校验
      var checkFunctionName = (rule, value, callback) => {
        var funcInfo = this.funcForm
        var treeList = this.treeList
        for (var i in treeList) {
          var funcItem = treeList[i]
          if (funcItem.text === value && funcItem.id !== funcInfo.func_id) {
            return callback(new Error('[节点名称]不能重复'))
          }
        }
        return callback()
      }
      // 功能顺序重复校验
      var checkFunctionOnum = (rule, value, callback) => {
        var self = this
        if (!(/^[1-9]\d{0,1}$/.test(value))) {
          return callback(new Error('[顺序]必须为小于100的正整数'))
        }
        var currentNode = self.$refs.tree.currentNode.node
        var funcInfo = self.funcForm
        if (funcInfo.func_id === '') { // 添加功能节点
          var childs = currentNode.childNodes
          for (var i in childs) {
            if (childs[i].data.onum === value) {
              return callback(new Error('[顺序]不能重复'))
            }
          }
        } else { // 编辑功能节点
          var siblings = currentNode.parent.childNodes
          for (var j in siblings) {
            if (siblings[j].data.onum === Number(value) && siblings[j].data.id !== funcInfo.func_id) {
              return callback(new Error('[顺序]不能重复'))
            }
          }
        }
        return callback()
      }
      // 日志配置校验
      var checkFunctionLogConf = (rule, value, callback) => {
        var self = this
        if (util.trim(value) === '') {
          return callback()
        }
        if (value.indexOf('GET:') === 0) {
          self.funcForm.logurlMethod = '0'
          self.funcForm.loguri = value.substring(4)
        } else if (value.indexOf('POST:') === 0) {
          self.funcForm.logurlMethod = '1'
          self.funcForm.loguri = value.substring(5)
        } else {
          return callback(new Error('[日志记录配置]只支持GET: 或POST:开头'))
        }
      }
      return {
        toolBtn: { // 功能树操作按钮
          addBtn: true, // 添加按钮
          editBtn: true, // 编辑按钮
          delBtn: true // 删除按钮
        },
        treeList: [], // 树节点数据List
        treeData: [{ // 树节点Json数据
          id: '-1',
          func_type: '-1',
          flag: '1',
          ftype_name: '',
          text: '功能树',
          icon: 'ticon-root',
          onum: 0
        }],
        nodeTypes: ['子系统', '模块', '功能', '子功能'],
        defaultProps: {
          children: 'children',
          label: 'text'
        },
        expendKey: ['-1'], // 默认展开的功能节点id
        formLabelWidth: '120px',
        funcFormReadonly: true, // 功能信息表单只读标识
        funcFormVisible: { // 功能表单中条目显隐标志配置
          form: true, // 表单整体显示
          func_id: true, // 功能id
          parent_id: true, // 父功能id
          func_type: true, // 功能类型
          func_name: true, // 功能名称
          conf: true, // 配置
          ftype_name: true, // 功能类型名称
          isgrant: true, // 授权
          menu: true, // 菜单
          flag: true, // 状态
          onum: true, // 排序
          info: true, // 备注
          islog: true, // 记录日志
          logconf: true // 日志配置
        },
        funcForm: {
          func_id: '', // 功能id
          parent_id: '', // 父功能id
          func_type: '', // 功能类型
          func_name: '', // 功能名称
          conf: '', // 配置
          ftype_name: '', // 功能类型名称
          isgrant: '', // 授权
          menu: '1', // 菜单
          flag: '1', // 状态
          onum: '', // 排序
          info: '', // 备注
          islog: '', // 记录日志
          logconf: '', // 日志配置,
          logurlMethod: '', // 日志请求方式
          loguri: '' // 日子请求地址
        },
        breadcrumbData: [], // 地址路径数组
        rules: { // 表单校验
          func_name: [
            {required: true, message: '请输入节点名称', trigger: 'blur'},
            {max: 32, message: '长度在32个字符以内', trigger: 'blur'},
            {validator: check.checkFormSpecialCode, trigger: 'blur'}, // 特殊字符校验
            {validator: checkFunctionName, trigger: 'blur'} // 功能名称重复校验
          ],
          conf: [
            {max: 500, message: '长度在500个字符以内', trigger: 'blur'}
          ],
          onum: [
            {required: true, message: '请输入节点顺序', trigger: 'blur'},
            {validator: checkFunctionOnum, trigger: 'blur'} // 功能顺序重复校验
          ],
          info: [
            {max: 300, message: '长度在300个字符以内', trigger: 'blur'}
          ],
          logconf: [
            {validator: checkFunctionLogConf, trigger: 'blur'} // 日志配置校验
          ]
        }
      }
    },
    methods: {
      // 功能树渲染方法
      renderContent (h, { node, data, store }) {
        if (node.data.flag === '0') { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      // 查询树结构
      selTree () {
        var self = this
        ajax.post({
          url: '/func/tree',
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
              self.treeData = util.formatTreeData(treeList)
              self.expendNodesByLevel(2)
            }
          },
          fail: function (e) {
            if (e.response.data.message) {
              self.$message.error(e.response.data.message)
            }
          }
        })
      },
      // 展开前几层功能树
      expendNodesByLevel (deep) {
        var expendNodeIds = []
        var treeNode = this.treeData[0]
        var level = 1
        while (level <= deep) {
          expendNodeIds.push(treeNode.id)
          treeNode = treeNode.children[0]
          level++
        }
        this.expendKey = expendNodeIds
      },
      // 功能树节点点击事件
      handleNodeClick (data, node) {
        var selectNode = this.funcForm
        // 重复点击同一节点判断
        if (selectNode.func_id === data.id) {
          return
        }
        this.$refs['funcForm'].clearValidate()
        var self = this
        var ftype = data.func_type // 功能类型（节点类型）
        // 设置可操作按键
        if (ftype < '3' && data.flag === '1') {
          self.toolBtn.addBtn = false
        } else {
          self.toolBtn.addBtn = true
        }
        if (ftype === '-1') { // 顶层
          self.toolBtn.editBtn = true
          self.toolBtn.delBtn = true
        } else {
          self.toolBtn.editBtn = false
          self.toolBtn.delBtn = false
        }
        // 同步面包屑导航地址
        self.syncBreadcrumb(node)
        // 功能表单为只读
        self.funcFormReadonly = true
        self.showNodeAttr(node.data.func_type)
        self.showNodeValue(data)
      },
      // 同步地址栏显示
      syncBreadcrumb (node) {
        var self = this
        var nodePath = []
        while (node.parent !== null) {
          nodePath.unshift({text: node.data.text})
          node = node.parent
        }
        self.breadcrumbData = nodePath
      },
      // 功能信息表单条目显示处理（根据功能类型不同）
      showNodeAttr (ftype) {
        var self = this
        var visible = {
          form: false,
          func_id: true,
          parent_id: true,
          func_type: true,
          func_name: false,
          conf: false,
          ftype_name: false,
          isgrant: false,
          menu: false,
          flag: false,
          onum: false,
          info: false,
          islog: false,
          logconf: false
        }
        if (ftype === '-1') { // 顶层
          visible.ftype_name = true
          visible.isgrant = true
          visible.menu = true
          visible.flag = true
          visible.onum = true
          visible.islog = true
          visible.logconf = true
          self.funcFormVisible = visible
        } else {
          // visible.ftype_name = false
          // visible.flag = false
          // visible.onum = false
          if (ftype === '0' || ftype === '1') {
            // 子系统，模块
            // 名称，配置，类型，是否菜单，状态，顺序，描述
            visible.isgrant = true
            // visible.menu = false
            visible.islog = true
            visible.logconf = true
            self.funcFormVisible = visible
          } else if (ftype === '2') {
            // 功能
            // 名称，配置，类型，访问授权, 是否菜单，状态，顺序，描述
            // 操作日志，记录日志URL，请求方式
            // visible.isgrant = false
            // visible.menu = false
            // visible.islog = false
            // visible.logconf = false
            self.funcFormVisible = visible
          } else if (ftype === '3') {
            // 子功能
            // 名称，配置，类型，状态，顺序，描述
            // 操作日志，记录日志URL，请求方式
            visible.isgrant = true
            visible.menu = true
            // visible.islog = false
            // visible.logconf = false
            self.funcFormVisible = visible
          } else {
            // visible.ftype_name = false
            // visible.isgrant = false
            // visible.flag = false
            // visible.onum = false
            self.funcFormVisible = visible
          }
        }
      },
      //  功能信息表单条目数据展示
      showNodeValue (data) {
        var self = this
        var formData = {
          func_id: data.id,
          parent_id: data.fid,
          func_type: data.func_type,
          func_name: data.text,
          conf: data.conf,
          ftype_name: self.nodeTypes[data.func_type],
          isgrant: (data.isgrant === '1'),
          menu: (data.menu ? data.menu : '1'),
          flag: (data.flag ? data.flag : '1'),
          onum: data.onum,
          info: data.info,
          islog: (data.islog === '1'),
          logconf: '', // 日志配置,
          logurlMethod: data.logurlmethod, // 日志请求方式
          loguri: data.loguri // 日子请求地址
        }
        switch (data.logurlmethod) {
          case '0' :
            formData.logconf = 'GET:' + data.loguri
            break
          case '1' :
            formData.logconf = 'POST:' + data.loguri
            break
          default :
            break
        }
        self.funcForm = formData
      },
      resetPage () {
        this.funcFormVisible.form = true
        this.breadcrumbData = []
        this.toolBtn = { // 功能树操作按钮
          addBtn: true, // 添加按钮
          editBtn: true, // 编辑按钮
          delBtn: true // 删除按钮
        }
      },
      // 添加功能事件处理
      addFunc () {
        this.$refs['funcForm'].resetFields()
        var self = this
        var selectNode = self.$refs.tree.getCurrentNode()
        var funcType = Number(selectNode.func_type) + 1
        self.funcForm.parent_id = selectNode.id
        self.funcForm.func_type = funcType
        self.funcForm.ftype_name = self.nodeTypes[funcType]
          // 功能表单为编辑状态
        self.funcFormReadonly = false
        self.showNodeAttr(funcType + '')
      },
      // 编辑功能事件处理
      editFunc () {
        // 功能表单为编辑状态
        this.funcFormReadonly = false
      },
      // 删除功能事件处理
      delFunc () {
        var self = this
        var selectNode = self.$refs.tree.currentNode.node
        this.$confirm('确定要删除功能节点吗？其下级节点将都会被删除！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/func/del',
            param: {
              funcId: selectNode.data.id,
              rf: 'json'
            },
            success: function (data) {
              selectNode.visible = false
              self.resetPage()
              self.$message.success('删除功能成功')
            },
            fail: function (e) {
              if (e.response.data.message) {
                self.$message.error(e.response.data.message)
              }
            }
          })
        }).catch(() => {})
      },
      closeFuncForm () {
        var self = this
        if (self.funcForm.func_id === '') {
          self.resetPage()
        } else {
          self.funcFormReadonly = true
        }
      },
      submitFunc () {
        var self = this
        this.$refs['funcForm'].validate((valid) => {
          if (valid) {
            var url = ''
            if (self.funcForm.func_id === '') {
              url = '/func/add'
            } else {
              url = '/func/mod'
            }
            ajax.post({
              url: url,
              param: self.funcForm,
              success: function (data) {
                self.$message.success('操作成功。')
                self.selTree()
                self.resetPage()
              },
              fail: function (e) {
                if (e.response.data.message) {
                  self.$message.error(e.response.data.message)
                }
              }
            })
          }
        })
      }
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }
  .disabledFlag {
    color: #dcdfe6;
  }
</style>
