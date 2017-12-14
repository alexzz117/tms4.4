<template>
  <div>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button plain class="el-icon-plus" @click="">新建</el-button>
      <el-button plain class="el-icon-edit" @click="">编辑</el-button>
      <el-button plain class="el-icon-delete" @click="">删除</el-button>
    </div>
    <el-container style="height: 580px; border: 1px solid #eee">
      <el-aside width="250px">
        <el-tree :data="treeData" node-key="id" :default-expanded-keys="expendKey" :props="defaultProps" @node-click="handleNodeClick"></el-tree>
      </el-aside>

      <el-container>

        <el-main>

        </el-main>
      </el-container>
    </el-container>
  </div>
</template>


<script>
  import ajax from '@/common/ajax'

  export default {
    created () {
      this.selTree()
    },
    data() {
      return {
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
        expendKey: ['-1']
      };
    },
    methods: {
      selTree() {
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
              var treeList = [rootNodes];
              treeList = treeList.concat(data.list);
              self.treeData = self.formatTreeData(treeList);
              self.expendNodesByLevel(2);
            }
          }
        }
        ajax.post(option)
      },
      formatTreeData (list, rootNodes) {
        var tree = [];
        // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
        if (rootNodes === undefined || rootNodes.length === 0) {
          rootNodes = [];
          for (var i in list) {
            if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '') {
              rootNodes.push(list[i])
            }
          }
        }
        if (rootNodes.length === 0){
          console.error('根节点不存在，请确认树结构是否正确');
          console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）');
        }
        for (var r in rootNodes) {
          var node = rootNodes[r];
          node.children = getChildren(list, node.id);
          tree.push(node);
        }
        // 递归查询节点的子节点
        function getChildren (list, id) {
          var childs = [];
          for (var i in list) {
            var node = list[i];
            if (node.fid === id) {
              node.children  = getChildren(list, node.id);
              childs.push(node)
            }
          }
          return childs;
        }
        return tree;  // 返回树结构Json
      },
      expendNodesByLevel(deep) {
        var expendNodeIds = [];
        var treeNode = this.treeData[0];
        var level = 1;
        while (level <= deep) {
          expendNodeIds.push(treeNode.id);
          treeNode = treeNode.children[0];
          level++;
        }
        this.expendKey =expendNodeIds;
      },
      handleNodeClick(data) {
        console.log(data);
      }
    }
  };
</script>

<style>

  .el-aside {
    color: #333;
  }
</style>
