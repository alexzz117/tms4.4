<template>
  <div>
    <el-dialog title="规则名称" :visible.sync="visibleFlag" width="450px" :close-on-click-modal="false" @close="submitFunc">
      <el-tree :data="treeData" node-key="id" ref="tree"
               show-checkbox
               :props="defaultTreeProps"
               :highlight-current=true
               :default-expanded-keys="defaultExpendKey"
               :default-checked-keys="defaultCheckKey"
               :expand-on-click-node="false"
               :render-content="renderContent"
               style="overflow-y: auto; max-height: 500px;">
      </el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleFlag = false" size="large">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  export default {
    name: 'txnDetail',
    computed: {},
    data () {
      return {
        visibleFlag: false,
        defaultTreeProps: {
          children: 'children',
          label: 'tab_desc'
        },
        defaultExpendKey: ['T'],
        defaultCheckKey: [],
        treeData: []
      }
    },
    props: [],
    methods: {
      open (params) {
        this.visibleFlag = true
        let defaultCheckKey = []
        if (params && util.isArray(params) && params.length > 0) {
          defaultCheckKey = params
        }
        this.defaultCheckKey = defaultCheckKey
        this.init(defaultCheckKey)
      },
      init () {
        let self = this
        ajax.post({
          url: '/trandef/queryRuleTree',
          loading: true,
          success: function (data) {
            self.treeData = self.formatTreeData(data.list)
          }
        })
      },
      formatTreeData (list, rootNodes) {
        var tree = []
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = []
          for (var i in list) {
            list[i].text = `${list[i].code_value}(${list[i].code_key})`
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        // 根节点不存在判断
        if (rootNodes.length === 0) {
          console.error('根节点不存在，请确认树结构是否正确')
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
        }
        // 根据根节点遍历组装数据
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
      // 功能树渲染方法
      renderContent (h, { node, data, store }) {
        if (node.data.enable === 0) { // 功能节点状态禁用
          return (<span class="el-tree-node__label disabledFlag">{node.label}</span>)
        } else { // 功能节点状态正常
          return (<span class="el-tree-node__label">{node.label}</span>)
        }
      },
      submitFunc () {
        let checkedArr = this.$refs['tree'].getCheckedNodes()
        let checkedStrArr = []
        let checkedStrKeyArr = []
        for (let item of checkedArr) {
          if (item.children && item.children.length > 0) {
            continue
          }
          checkedStrArr.push(item.tab_desc)
          checkedStrKeyArr.push(item.tab_name + '')
        }
        this.$emit('listenToSubmit', {name: checkedStrArr, value: checkedStrKeyArr})
      }
    }
  }
</script>
<style scoped>
</style>
