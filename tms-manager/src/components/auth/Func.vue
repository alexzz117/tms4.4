<template>
  <div>

    <el-row style="height: 100%;border: 1px solid #eee">
      <el-col :span="6" style="border-right: 1px solid #eee">
        <div style="height:38px;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee;">
          <el-button plain class="el-icon-plus" @click="addFunc" :disabled="toolBtn.addBtn" style="margin-left: 5px;">新建
          </el-button>
          <el-button plain class="el-icon-edit" @click="editFunc" :disabled="toolBtn.editBtn" style="margin-left: 0px;">编辑
          </el-button>
          <el-button plain class="el-icon-delete" @click="delFunc" :disabled="toolBtn.delBtn" style="margin-left: 0px;">删除
          </el-button>
        </div>
        <el-tree :data="treeData" node-key="id"
                 :default-expanded-keys="expendKey"
                 :props="defaultProps"
                 :highlight-current=true
                 @node-click="handleNodeClick"
                 :render-content="renderContent"
                 style="height: 76vh;overflow-y: auto;">
        </el-tree>
      </el-col>

      <el-col :span="18">
        <div style="height:38px;width:100%;margin-top: 5px;text-align: left;border-bottom: 1px solid #eee;">
          <el-breadcrumb id="funcPath" separator=">" style="padding: 10px 15px;">
            <el-breadcrumb-item v-for="item in breadcrumbData">{{ item.text }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-main style="max-height: 76vh;overflow-y: auto;">
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
            <el-form-item label="是否菜单" style="text-align: left;" v-bind:class="{hidden:funcFormVisible.menu}">
              <el-radio v-model="funcForm.menu" label="1" :disabled="funcFormReadonly">是</el-radio>
              <el-radio v-model="funcForm.menu" label="0" :disabled="funcFormReadonly">否</el-radio>
            </el-form-item>
            <el-form-item label="状态" style="text-align: left;" v-bind:class="{hidden:funcFormVisible.flag}">
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
            <el-form-item style="text-align: center" v-bind:class="{hidden:funcFormReadonly}">
              <el-button type="primary" @click="">保 存</el-button>
              <el-button @click="">取 消</el-button>
            </el-form-item>
          </el-form>
        </el-main>
      </el-col>
    </el-row>
  </div>
</template>


<script>
  import ajax from '@/common/ajax'

  export default {
    created () {
      this.selTree()
    },
    data () {
      return {
        toolBtn: {
          addBtn: true,
          editBtn: true,
          delBtn: true
        },
        treeData: [{
          id: '-1',
          func_type: '-1',
          flag: '1',
          ftype_name: '',
          text: '功能树',
          icon: 'ticon-root',
          onum: 0
        }],
        defaultProps: {
          children: 'children',
          label: 'text'
        },
        expendKey: ['-1'],
        formLabelWidth: '120px',
        funcFormReadonly: true,
        funcFormVisible: {
          form: true,
          func_id: true,
          parent_id: true,
          func_type: true,
          func_name: true,
          conf: true,
          ftype_name: true,
          isgrant: true,
          menu: true,
          flag: true,
          onum: true,
          info: true,
          islog: true,
          logconf: true
        },
        funcForm: {
          func_id: '',
          parent_id: '',
          func_type: '',
          func_name: '',
          conf: '',
          ftype_name: '',
          isgrant: '',
          menu: '1',
          flag: '1',
          onum: '',
          info: '',
          islog: '',
          logconf: ''
        },
        breadcrumbData: [],
        rules: {}
      }
    },
    methods: {
      renderContent (h, { node, data, store }) {
        return (<span class="el-tree-node__label">运行监控</span>)
      },
      selTree () {
        var self = this
        var option = {
          url: '/cmc/func/tree',
          success: function (data) {
            if (data.list) {
              var rootNodes = {
                id: '-1',
                func_type: '-1',
                flag: '1',
                ftype_name: '',
                text: '功能树',
                icon: 'ticon-root',
                onum: 0
              }
              var treeList = [rootNodes]
              treeList = treeList.concat(data.list)
              self.treeData = self.formatTreeData(treeList)
              self.expendNodesByLevel(2)
            }
          }
        }
        ajax.post(option)
      },
      formatTreeData (list, rootNodes) {
        var tree = []
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = []
          for (var i in list) {
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        if (rootNodes.length === 0) {
          console.error('根节点不存在，请确认树结构是否正确')
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
        }
        for (var r in rootNodes) {
          var node = rootNodes[r]
          node.children = getChildren(list, node.id)
          tree.push(node)
        }

        // 递归查询节点的子节点
        function getChildren (list, id) {
          var childs = []
          for (var i in list) {
            var node = list[i]
            if (node.fid === id) {
              node.children = getChildren(list, node.id)
              // node.icon = 'el-icon-message'
              childs.push(node)
            }
          }
          return childs
        }

        return tree  // 返回树结构Json
      },
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
      handleNodeClick (data, node) {
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
        self.showNodeAttr(node.data.func_type)
        self.showNodeValue(data)
        console.info(node)
      },
      syncBreadcrumb (node) {
        var self = this
        var nodePath = []
        while (node.parent !== null) {
          nodePath.unshift({text: node.data.text})
          node = node.parent
        }
        self.breadcrumbData = nodePath
      },
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
      showNodeValue (data) {
        var nodeTypes = ['子系统', '模块', '功能', '子功能']
        var self = this
        var formData = {
          func_id: data.func_id,
          parent_id: data.parent_id,
          func_type: data.func_type,
          func_name: data.text,
          conf: data.conf,
          ftype_name: nodeTypes[data.func_type],
          isgrant: (data.isgrant === '1' ? true : false),
          menu: (data.menu ? data.menu : '1'),
          flag: (data.flag ? data.flag : '1'),
          onum: data.onum,
          info: data.info,
          islog: (data.islog === '1' ? true : false),
          logconf: data.logconf
        }
        self.funcForm = formData
      },
      addFunc () {
        var self = this
        console.info(self.$refs)
        var selectNode = self.$refs.tree.getCurrentNode()
        // self.showNodeAttr(node.data.func_type)
      },
      editFunc () {
      },
      delFunc () {
      }
    }
  }
</script>

<style>
  .hidden {
    display: none;
  }
</style>
