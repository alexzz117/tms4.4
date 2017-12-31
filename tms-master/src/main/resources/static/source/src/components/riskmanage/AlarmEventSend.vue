<template>
<div>
  <el-form  label-position="right" :model="ruleForm"  label-width="100px"  class="demo-form-inline"  :inline="inline"  >
  <el-row>
    <el-col :span="2"> <div><el-form-item label="流水号"/></div> </el-col>
    <el-col :span="5"><div>
      <el-form-item prop="txtid" >
        <el-input v-model="ruleForm.txtid" ></el-input>
      </el-form-item></div>
    </el-col>
    <el-col :span="2"><div ><el-form-item label="交易时间起"/></div></el-col>
    <el-col :span="5"><div>
          <el-form-item prop="date1">
            <el-date-picker type="datetime" placeholder="选择时间" v-model="ruleForm.date1" ></el-date-picker>
          </el-form-item></div>
    </el-col>
    <el-col :span="2"><div ><el-form-item label="交易时间止"/></div></el-col>
    <el-col :span="5"><div>
        <el-form-item prop="date1">
          <el-date-picker type="datetime" placeholder="选择时间" v-model="ruleForm.date1" ></el-date-picker>
        </el-form-item>
    </div>
    </el-col>
    <el-col :span="3"><div > <el-button class="el-icon-more" type="primary" @click="showMore"/></div></el-col>
  </el-row>

    <el-row>
      <el-col :span="2"><div ><el-form-item label="客户号"/></div></el-col>
      <el-col :span="5"><div ><el-form-item prop="txtid" >
        <el-input v-model="ruleForm.txtid" prop="txtid"/></el-form-item>
      </div>
      </el-col>

      <el-col :span="2"> <div ><el-form-item label="处理状态" /></div> </el-col>
      <el-col :span="5"><div >
        <el-select v-model="value" placeholder="请选择" >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select></div>
      </el-col>

      <el-col :span="9"><div align="right" ><el-button class="el-icon-search" type="primary" @click="">查询</el-button></div></el-col>
    </el-row>
  <el-row>
    <el-col :span="1"><div >  <el-button type="primary" :align="left"  icon="el-icon-success" @click="openDialog">分派</el-button></div></el-col>
  </el-row>
  </el-form>

  <!--   数据列表  -->
  <el-table
    :data="gridData"
    style="width: 100%" tooltip-effect="dark">
    <el-table-column type="selection" width="50" align="left" />
    <el-table-column fixed="left" label="操 作" width="80" alert="center" >
      <template slot-scope="scope"  >
        <el-button type="text"  @click="" size="mini"  icon="el-icon-search" />
      </template>
    </el-table-column>
    <el-table-column  prop="txncode" label="流水号" width="180" align="left" />
    <el-table-column  prop="userid" label="客户号" width="150" align="left" />
    <el-table-column  prop="username" label="客户名称" width="120" align="left" />
    <el-table-column  prop="txtime" label="操作时间" width="150" align="left" />
    <el-table-column  prop="txnname" label="监控操作" width="150" align="left" />
    <el-table-column  prop="iscorrect" label="处置结果" width="140" align="left" />
    <el-table-column  prop="iscorrect" label="处理状态" width="140" align="left" />
  </el-table>
  <el-pagination background class="block" label="left" label-width="100px"
                            @size-change="handleSizeChange"
                            @current-change="handleCurrentChange"
                            :current-page="pageindex"
                            :page-sizes="[10, 25, 50, 100]"
                            :page-size="pagesize"
                            layout="total, prev, pager, next"
                            :total="total">
 </el-pagination>


  <!-- 分派人员弹出窗 -->
  <el-dialog title="分派人员" :visible.sync="userDialogVisible" append-to-body  style="width: 90%">
    <el-table
      :data="userData" highlight-current-rows
      tooltip-effect="dark" style="width: 100%">
      <el-table-column  prop="userid" label="操作员ID" width="120" align="left" />
      <el-table-column  prop="username" label="操作员名称" width="100" align="left" />
      <el-table-column  prop="total" label="总量" width="70" align="left" />
      <el-table-column  prop="ycl" label="已处理" width="70" align="left" />
      <el-table-column  prop="wcl" label="未处理" width="70" align="left" />
      <el-table-column  prop="usermail" label="操作员联系邮箱" width="160" align="left" />
    </el-table>
    <div class="dialog-footer"  align="center"  slot="footer">
      <el-button data="cancelBtn" icon="el-icon-success" type="primary" >确 定</el-button>
      <el-button data="cancelBtn" icon="el-icon-arrow-left" type="primary" >返 回</el-button>
    </div>
  </el-dialog>
</div>
</template>

<script>
  export default {
    created () {
      this.sel()
    },
    methods: {
      openDialog()  {
        this.userDialogVisible = true
      }},
    showMore(pageinfo){

    },
    data() {
      return {
        inline: true,
        userDialogVisible: false,
        ruleForm: {
          name: ''
        },
        gridData: [{
          txncode: '2017122700001',
          userid: 'user001',
          username: 'username',
          txtime: '2016-05-02',
          txnname: 'txnname',
          iscorrect: 'iscorrect'
        }, {
          txncode: '2017122700001',
          userid: 'user001',
          username: 'username',
          txtime: '2016-05-02',
          txnname: 'txnname',
          iscorrect: 'iscorrect'
        }, {
          txncode: '2017122700001',
          userid: 'user001',
          username: 'username',
          txtime: '2016-05-02',
          txnname: 'txnname',
          iscorrect: 'iscorrect'
        }
        ],
        userData: [{
          userid: '2017122700001',
          username: 'username',
          usermail: 'vip@qq.com',
          usertel: '139********',
          total: '10',
          wcl: '10',
          ycl: '0'
        }, {
          userid: '2017122700001',
          username: 'username',
          usermail: 'vip@qq.com',
          usertel: '139********',
          total: '10',
          wcl: '10',
          ycl: '0'
        }]
      }
    }
  }
</script>


