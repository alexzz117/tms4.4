<template>
  <el-container style="height: 100%; border: 1px solid #eee" class="wrapper">
    <el-container>
      <el-aside width="220px" style="overflow:hidden;height: 100%; padding-top:60px; box-sizing: border-box;">
        <div class="title">TMS</div>
        <div class="scroll" style="overflow: auto; height: 100%; margin: 60px 0px 10px 0px">
          <el-menu :default-openeds="['1', '3', '4']" >
            <!--<el-submenu v-for="(menu, index) in menus" :key="menu.id" :index= "index.toString()">-->
              <!--<template slot="title"><i class="el-icon-message"></i>{{ menu.text }}</template>-->

              <!--&lt;!&ndash;<el-menu-item v-for="submenu in menu.children" :key="submenu.id">{{ submenu.text }}</el-menu-item>&ndash;&gt;-->

              <!--<div v-for="(submenu, subindex) in menu.children" :key="submenu.id" :index= "subindex.toString()">-->
                <!--<router-link :to="{ name: submenu.conf }" tag="li" replace >-->
                  <!--<el-menu-item :index="index.toString() + '-' + subindex.toString">{{ submenu.text }}</el-menu-item>-->
                <!--</router-link>-->
              <!--</div>-->

            <!--</el-submenu>-->

            <el-submenu index="1">
              <template slot="title"><i class="el-icon-message"></i>运行管理</template>
              <div>
                <router-link :to="{ name:'txn'}" tag="li" replace>
                  <el-menu-item index="1-1">交易模型</el-menu-item>
                </router-link>
                <router-link :to="{ name:'list'}" tag="li" replace>
                  <el-menu-item index="1-2">名单管理</el-menu-item>
                </router-link>
                <router-link :to="{ name:'namelist'}" tag="li" replace>
                  <el-menu-item index="1-3">名单查看</el-menu-item>
                </router-link>
              </div>
            </el-submenu>
            <el-submenu index="3">
              <template slot="title"><i class="el-icon-setting"></i>权限管理</template>
              <router-link :to="{ name:'user'}" tag="li" replace>
                <el-menu-item index="3-1">操作员管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'role'}" tag="li" replace>
                <el-menu-item index="3-2">角色管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'func'}" tag="li" replace>
                <el-menu-item index="3-3">功能管理</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="4">
              <template slot="title"><i class="el-icon-setting"></i>系统管理</template>
              <router-link :to="{ name:'codedict'}" tag="li" replace>
                <el-menu-item index="4-1">数据字典管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'ipaddr'}" tag="li" replace>
                <el-menu-item index="4-2">IP地址维护</el-menu-item>
              </router-link>
              <router-link :to="{ name:'authCenter'}" tag="li" replace>
                <el-menu-item index="4-3">授权中心</el-menu-item>
              </router-link>
              <router-link :to="{ name:'log'}" tag="li" replace>
                <el-menu-item index="4-4">操作日志管理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'time'}" tag="li" replace>
                <el-menu-item index="4-5">定时任务管理</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="5">
              <template slot="title"><i class="el-icon-setting"></i>综合查询</template>
              <router-link :to="{ name:'txnEventQuery'}" tag="li" replace>
                <el-menu-item index="5-1">交易事件查询</el-menu-item>
              </router-link>
              <router-link :to="{ name:'alarmEventQuery'}" tag="li" replace>
                <el-menu-item index="5-2">报警事件查询</el-menu-item>
              </router-link>
              <!--<router-link :to="{ name:'dashboard'}" tag="li" replace>-->
              <!--<el-menu-item index="5-3">交易监控</el-menu-item>-->
              <!--</router-link>-->
            </el-submenu>
            <el-submenu index="6">
              <template slot="title"><i class="el-icon-setting"></i>风险处置</template>
              <router-link :to="{ name:'alarmEventSend'}" tag="li" replace>
                <el-menu-item index="6-1">报警事件分派</el-menu-item>
              </router-link>
              <router-link :to="{ name:'alarmeventExecute'}" tag="li" replace>
                <el-menu-item index="6-2">报警事件处理</el-menu-item>
              </router-link>
              <router-link :to="{ name:'alarmeventAudit'}" tag="li" replace>
                <el-menu-item index="6-3">报警事件审核</el-menu-item>
              </router-link>
            </el-submenu>
            <el-submenu index="7">
              <template slot="title"><i class="el-icon-setting"></i>运行报表</template>
              <router-link :to="{ name:'txnListReport'}" tag="li" replace>
                <el-menu-item index="7-1">操作报警信息</el-menu-item>
              </router-link>
              <router-link :to="{ name:'ruleListReport'}" tag="li" replace>
                <el-menu-item index="7-2">规则报警信息</el-menu-item>
              </router-link>
              <router-link :to="{ name:'areaListReport'}" tag="li" replace>
                <el-menu-item index="7-3">地区报警信息</el-menu-item>
              </router-link>
              <router-link :to="{ name:'dateListReport'}" tag="li" replace>
                <el-menu-item index="7-3">日期报警信息</el-menu-item>
              </router-link>
            </el-submenu>
          </el-menu>
        </div>
      </el-aside>
      <el-main class="scroll">
        <div class="toolbar">
          <div class="page-title">交易监控</div>
          <!--<el-dropdown-menu slot="dropdown">-->
          <!--<el-dropdown-item><i class="el-icon-setting" style="margin-right: 5px"></i>修改密码</el-dropdown-item>-->
          <!--<el-dropdown-item><i class="el-icon-circle-close" style="margin-right: 5px"></i>退出</el-dropdown-item>-->
          <!--</el-dropdown-menu>-->
        </div>
        <!--用router-view渲染视图-->
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import util from '@/common/util'

  export default {
    name: 'items',
    created () {
//      ajax.post({
//        url: '/mgr/menu',
//        param: {},
//        success: function (data) {
//          console.log(data)
//        }
//      })

      var data = {
        "list": [{
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 0,
          "conf": "/dualaudit/tms35/trandef/init?maxpanel=1",
          "id": "90D488B26F6C49C5ADB328B5BEA8469P",
          "text": "交易模型"
        }, {
          "fid": "90D488B26F6C49C5ADB328B554A8469P",
          "ftype": "2",
          "onum": 0,
          "conf": "/tms/ip/protect",
          "id": "1E857C9799874CE6A982B2C02DF13762",
          "text": "IP地址维护"
        }, {
          "fid": "A45BDE6E8EBB481194BEA0D76FBB8C45",
          "ftype": "2",
          "onum": 0,
          "conf": "/cmc/func/tree",
          "id": "435E450A16C947318D8C76B277866F3B",
          "text": "功能管理"
        }, {
          "fid": "CF56A270A2D34CD99FD4AE162A7DE37D",
          "ftype": "2",
          "onum": 0,
          "conf": "/cmc/log/list",
          "id": "37B2F671436A4D07BC6A0F666DB3ACD5",
          "text": "操作日志管理"
        }, {
          "fid": "CDA3CA5A764145F29C405A7A177DBA1E",
          "ftype": "2",
          "onum": 0,
          "conf": "/tms/monitor/listY15?maxpanel=1",
          "id": "315A3EF5674D4671B7641A0042749516",
          "text": "实时运行监控"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 0,
          "conf": "/report/txn/list",
          "id": "EEC7424A873E449D8D3615B440AF9B7F",
          "text": "交易报警信息"
        }, {
          "fid": "8F59A7A2F2F244ACB09F5857ACC06F32",
          "ftype": "2",
          "onum": 0,
          "conf": "/cmc/role/list",
          "id": "189D2BBE40FD4DB78779FC8C325FB49C",
          "text": "角色管理"
        }, {
          "fid": "8F59A7A2F2F244ACB09F5857ACC06F32",
          "ftype": "2",
          "onum": 0,
          "conf": "/cmc/operator/list",
          "id": "97F4DF1ED4134841B9EA7344F34FFAC6",
          "text": "操作员管理"
        }, {
          "fid": "51446C4AD17B4866A238D0AD59D86166",
          "ftype": "2",
          "onum": 0,
          "conf": "/tms/aml/config",
          "id": "052E0175ED024DC7B9A68D4C0ABB17CD",
          "text": "反洗钱配置定义"
        }, {
          "fid": "2E89681CA6834CA1BBB8C303878FEFBE",
          "ftype": "2",
          "onum": 0,
          "conf": "/tms35/query/show?queryId=258",
          "id": "2F3DB37B474B400FAFB1B13DDF572D8B",
          "text": "交易事件查询"
        }, {
          "fid": "3740C399A7F84440BF5EBA3A65C5A920",
          "ftype": "2",
          "onum": 0,
          "conf": "/tms35/rate/list",
          "id": "0AB31B19A4A44A92853E8092D8950230",
          "text": "评级分类"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 1,
          "conf": "/report/rule/list",
          "id": "EEC7424A873E449D8D3615B440AF9B7C",
          "text": "规则报警信息"
        }, {
          "fid": "2E89681CA6834CA1BBB8C303878FEFBE",
          "ftype": "2",
          "onum": 1,
          "conf": "/dualaudit/tms35/query/show?queryId=4004",
          "id": "F54A2A201DE84805B9CA575C38487974",
          "text": "报警事件查询"
        }, {
          "fid": "3740C399A7F84440BF5EBA3A65C5A920",
          "ftype": "2",
          "onum": 1,
          "conf": "/tms35/query/show?queryId=4009&RS_ID=1",
          "id": "0AB31B19A4A44A92853E8092D8950231",
          "text": "用户评级"
        }, {
          "fid": "3740C399A7F84440BF5EBA3A65C53928",
          "ftype": "2",
          "onum": 1,
          "conf": "/dualaudit/tms35/query/show?queryId=4007",
          "id": "EE0B2A8EAABE4402AA0116D91748855B",
          "text": "报警事件分派"
        }, {
          "fid": "51446C4AD17B4866A238D0AD59D86166",
          "ftype": "2",
          "onum": 1,
          "conf": "/tms/aml/list",
          "id": "F8E2490AA9754CCD8A3E9B85C00B8FF9",
          "text": "反洗钱数据管理"
        }, {
          "fid": "90D488B26F6C49C5ADB328B554A8469P",
          "ftype": "2",
          "onum": 1,
          "conf": "/tms/auth/modelList",
          "id": "1E857C9799874CE6A982B2C02Da13a62",
          "text": "授权中心"
        }, {
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 1,
          "conf": "/tms35/trandef/init?maxpanel=1",
          "id": "90D488B26F6C4PPADB328B5BEA8469P",
          "text": "交易模型查看"
        }, {
          "fid": "A45BDE6E8EBB481194BEA0D76FBB8C45",
          "ftype": "2",
          "onum": 2,
          "conf": "/cmc/codedict/category/list",
          "id": "CAD22DC8A2274003BABB2866798D625C",
          "text": "数据字典管理"
        }, {
          "fid": "3740C399A7F84440BF5EBA3A65C53928",
          "ftype": "2",
          "onum": 2,
          "conf": "/dualaudit/tms35/query/show?queryId=4005",
          "id": "BE30559DDCB646FF94F2FFADC14EFA35",
          "text": "报警事件处理"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 2,
          "conf": "/report/area/list",
          "id": "EEC7424A873E449D8D3615B440AF9B7E",
          "text": "地区报警信息"
        }, {
          "fid": "90D488B26F6C49C5ADB328B554A8469P",
          "ftype": "2",
          "onum": 2,
          "conf": "/tms/auth/authQuery",
          "id": "1E857C9799874CE6A982d2C02DF13a62",
          "text": "授权查看"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 3,
          "conf": "/report/date/list",
          "id": "EEC7424A873E449D8D3615B440AF9B7D",
          "text": "日期报警信息"
        }, {
          "fid": "3740C399A7F84440BF5EBA3A65C53928",
          "ftype": "2",
          "onum": 3,
          "conf": "/dualaudit/tms35/query/show?queryId=4006",
          "id": "99E2140B3A8E49348ACF3925D9241982",
          "text": "报警事件审核"
        }, {
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 4,
          "conf": "/tms/mgr/list",
          "id": "E0BD05ECCABB43AD8732855FA3EBB46A",
          "text": "名单管理"
        }, {
          "fid": "90D488B26F6C49C5ADB328B554A8469P",
          "ftype": "2",
          "onum": 4,
          "conf": "/tms35/dfp/dfpTab",
          "id": "1E857C9799874CE6A982B2C02DF13a62",
          "text": "设备指纹"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 4,
          "conf": "/report/model/list",
          "id": "EEC7424A873E449D8D3615B440AFAB7D",
          "text": "模型评价信息"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 5,
          "conf": "/report/fraud/list",
          "id": "A89D655739FF4E90ABA56B3CA1A07AB0",
          "text": "欺诈报警信息"
        }, {
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 5,
          "conf": "/tms/mgrView/list",
          "id": "9E4458FD6B144349B6548D6338D9ACC5",
          "text": "名单查看"
        }, {
          "fid": "7BC577D925064E918C64351BFD4A921C",
          "ftype": "2",
          "onum": 6,
          "conf": "/report/txnTime/list",
          "id": "FD6A8067B6114D468D731AF47F30144D",
          "text": "交易运行效率"
        }, {
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 8,
          "conf": "/tms35/query/tree",
          "id": "1A3FB348A8904C1880CDE355612A9908",
          "text": "自定义查询"
        }, {
          "fid": "641581B7A93F47FCB845E4A0E87624F4",
          "ftype": "2",
          "onum": 11,
          "conf": "/tms/modelmgr/list",
          "id": "1A3FB34418904C1880CDE35561549909",
          "text": "模型管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 0,
          "conf": null,
          "id": "641581B7A93F47FCB845E4A0E87624F4",
          "text": "运行管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 1,
          "conf": null,
          "id": "CDA3CA5A764145F29C405A7A177DBA1E",
          "text": "运行监控"
        }, {
          "fid": "cmc",
          "ftype": "1",
          "onum": 2,
          "conf": "menu_node_icon3",
          "id": "8F59A7A2F2F244ACB09F5857ACC06F32",
          "text": "操作员管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 2,
          "conf": null,
          "id": "2E89681CA6834CA1BBB8C303878FEFBE",
          "text": "综合查询"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 3,
          "conf": null,
          "id": "3740C399A7F84440BF5EBA3A65C53928",
          "text": "风险处置"
        }, {
          "fid": "cmc",
          "ftype": "1",
          "onum": 3,
          "conf": "menu_node_icon4",
          "id": "A45BDE6E8EBB481194BEA0D76FBB8C45",
          "text": "系统配置"
        }, {
          "fid": "cmc",
          "ftype": "1",
          "onum": 4,
          "conf": "menu_node_icon5",
          "id": "CF56A270A2D34CD99FD4AE162A7DE37D",
          "text": "日志管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 4,
          "conf": null,
          "id": "7BC577D925064E918C64351BFD4A921C",
          "text": "运行报表"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 5,
          "conf": null,
          "id": "90D488B26F6C49C5ADB328B554A8469P",
          "text": "系统管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 6,
          "conf": null,
          "id": "51446C4AD17B4866A238D0AD59D86166",
          "text": "反洗钱管理"
        }, {
          "fid": "tms",
          "ftype": "1",
          "onum": 7,
          "conf": null,
          "id": "3740C399A7F84440BF5EBA3A65C5A920",
          "text": "评级管理"
        }, {
          "fid": "",
          "ftype": "0",
          "onum": 0,
          "conf": null,
          "id": "tms",
          "text": "风险监控系统"
        }, {
          "fid": "",
          "ftype": "0",
          "onum": 1,
          "conf": null,
          "id": "cmc",
          "text": "公用子系统"
        }],
        "success": true
      }
      this.menus = util.formatTreeData(data.list)
    },
    data() {
      return {
        menus: [],
        testmenu: [
          {
            "fid": "641581B7A93F47FCB845E4A0E87624F4",
            "ftype": "2",
            "onum": 0,
            "conf": "/dualaudit/tms35/trandef/init?maxpanel=1",
            "id": "90D488B26F6C49C5ADB328B5BEA8469P",
            "text": "交易模型"
          },
          {
            "fid": "90D488B26F6C49C5ADB328B554A8469P",
            "ftype": "2",
            "onum": 0,
            "conf": "/tms/ip/protect",
            "id": "1E857C9799874CE6A982B2C02DF13762",
            "text": "IP地址维护"
          },
          {
            "fid": "A45BDE6E8EBB481194BEA0D76FBB8C45",
            "ftype": "2",
            "onum": 0,
            "conf": "/cmc/func/tree",
            "id": "435E450A16C947318D8C76B277866F3B",
            "text": "功能管理"
          },
          {
            "fid": "CF56A270A2D34CD99FD4AE162A7DE37D",
            "ftype": "2",
            "onum": 0,
            "conf": "/cmc/log/list",
            "id": "37B2F671436A4D07BC6A0F666DB3ACD5",
            "text": "操作日志管理"
          },
          {
            "fid": "7BC577D925064E918C64351BFD4A921C",
            "ftype": "2",
            "onum": 0,
            "conf": "/report/txn/list",
            "id": "EEC7424A873E449D8D3615B440AF9B7F",
            "text": "交易报警信息"
          },
          {
            "fid": "8F59A7A2F2F244ACB09F5857ACC06F32",
            "ftype": "2",
            "onum": 0,
            "conf": "/cmc/role/list",
            "id": "189D2BBE40FD4DB78779FC8C325FB49C",
            "text": "角色管理"
          },
          {
            "fid": "8F59A7A2F2F244ACB09F5857ACC06F32",
            "ftype": "2",
            "onum": 0,
            "conf": "/cmc/operator/list",
            "id": "97F4DF1ED4134841B9EA7344F34FFAC6",
            "text": "操作员管理"
          },
          {
            "fid": "51446C4AD17B4866A238D0AD59D86166",
            "ftype": "2",
            "onum": 0,
            "conf": "/tms/aml/config",
            "id": "052E0175ED024DC7B9A68D4C0ABB17CD",
            "text": "反洗钱配置定义"
          },
          {
            "fid": "2E89681CA6834CA1BBB8C303878FEFBE",
            "ftype": "2",
            "onum": 0,
            "conf": "/tms35/query/show?queryId=258",
            "id": "2F3DB37B474B400FAFB1B13DDF572D8B",
            "text": "交易事件查询"
          },
          {
            "fid": "3740C399A7F84440BF5EBA3A65C5A920",
            "ftype": "2",
            "onum": 0,
            "conf": "/tms35/rate/list",
            "id": "0AB31B19A4A44A92853E8092D8950230",
            "text": "评级分类"
          },
          {
            "fid": "51446C4AD17B4866A238D0AD59D86166",
            "ftype": "2",
            "onum": 1,
            "conf": "/tms/aml/list",
            "id": "F8E2490AA9754CCD8A3E9B85C00B8FF9",
            "text": "反洗钱数据管理"
          },
          {
            "fid": "90D488B26F6C49C5ADB328B554A8469P",
            "ftype": "2",
            "onum": 1,
            "conf": "/tms/auth/modelList",
            "id": "1E857C9799874CE6A982B2C02Da13a62",
            "text": "授权中心"
          },
          {
            "fid": "641581B7A93F47FCB845E4A0E87624F4",
            "ftype": "2",
            "onum": 1,
            "conf": "/tms35/trandef/init?maxpanel=1",
            "id": "90D488B26F6C4PPADB328B5BEA8469P",
            "text": "交易模型查看"
          },
          {
            "fid": "A45BDE6E8EBB481194BEA0D76FBB8C45",
            "ftype": "2",
            "onum": 2,
            "conf": "/cmc/codedict/category/list",
            "id": "CAD22DC8A2274003BABB2866798D625C",
            "text": "数据字典管理"
          },
          {
            "fid": "641581B7A93F47FCB845E4A0E87624F4",
            "ftype": "2",
            "onum": 4,
            "conf": "/tms/mgr/list",
            "id": "E0BD05ECCABB43AD8732855FA3EBB46A",
            "text": "名单管理"
          },
          {
            "fid": "tms",
            "ftype": "1",
            "onum": 0,
            "conf": null,
            "id": "641581B7A93F47FCB845E4A0E87624F4",
            "text": "运行管理"
          },
          {
            "fid": "tms",
            "ftype": "1",
            "onum": 1,
            "conf": null,
            "id": "CDA3CA5A764145F29C405A7A177DBA1E",
            "text": "运行监控"
          },
          {
            "fid": "cmc",
            "ftype": "1",
            "onum": 2,
            "conf": "menu_node_icon3",
            "id": "8F59A7A2F2F244ACB09F5857ACC06F32",
            "text": "操作员管理"
          },
          {
            "fid": "tms",
            "ftype": "1",
            "onum": 2,
            "conf": null,
            "id": "2E89681CA6834CA1BBB8C303878FEFBE",
            "text": "综合查询"
          },
          {
            "fid": "tms",
            "ftype": "1",
            "onum": 3,
            "conf": null,
            "id": "3740C399A7F84440BF5EBA3A65C53928",
            "text": "风险处置"
          },
          {
            "fid": "cmc",
            "ftype": "1",
            "onum": 3,
            "conf": "menu_node_icon4",
            "id": "A45BDE6E8EBB481194BEA0D76FBB8C45",
            "text": "系统配置"
          },
          {
            "fid": "cmc",
            "ftype": "1",
            "onum": 4,
            "conf": "menu_node_icon5",
            "id": "CF56A270A2D34CD99FD4AE162A7DE37D",
            "text": "日志管理"
          },
          {
            "fid": "-1",
            "ftype": "0",
            "onum": 1,
            "conf": null,
            "id": "cmc",
            "text": "公用子系统"
          }
        ]
      };
    },
    methods: {
    }
  };
</script>

<style>
  .el-header {
    background-color: #F0F1F4;
    color: #333;
    line-height: 70px;
    margin-bottom: 20px;
    height: 70px;
    text-align: right;
    font-size: 12px;
  }

  .el-aside {
    color: #333;
    border: 1px solid rgb(238, 241, 246);
    background-color: #f0f1f4
  }

  .el-menu {
    border-right: none;
    background-color: #f0f1f4;
  }

  .el-main {
    background-color: #FAFAFA;
  }

  .page-title {
    font-size: 20px;
    float: left;
  }

  .title {
    font-size: 20px;
    height: 60px;
    line-height: 60px;
    margin: 20px auto;
    width: 220px;
    position:absolute;
    top:0;
    left:0;
  }

  /*工具栏及表单样式设置*/
  .toolbar {
    margin-bottom: 20px;
    text-align: left;
    background-color: #F0F1F4;
    height: 60px;
    line-height: 60px;
    padding-left: 20px;
    border-radius: 7px;
  }

  .toolbar .toolbar-form {
    text-align: left;
    float: right;
    display: inline-block;
  }

  .toolbar .toolbar-form .el-form-item__content {
    line-height: 60px;
  }

  .table {
    padding: 20px;
    background-color: #FFFFFF;
    border: 1px solid rgba(112, 112, 112, 0.12);
    border-radius: 7px;
  }

  .el-dialog__body {
    padding: 30px 30px 30px 20px;
  }

  .el-form textarea {
    resize: none;
  }

  .section {
    padding: 20px;
    background-color: #FFFFFF;
    border: 1px solid rgba(112, 112, 112, 0.12);
    border-radius: 7px;
    height:100%;
    overflow: auto
  }
</style>
