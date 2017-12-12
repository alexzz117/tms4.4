var model = window.model || {};
var txn_id = null; //存储交易树节点
(function() {
	var st_id = '';
	var st_name = '';
	var addDialog = null;
	var grid = null;
	var dis;
	model.strategyRule = {
		init: function(txnId, page) {
			txn_id = txnId;
			
			var srows = model.strategyInfo.grid().table.selectedRows();
			if(srows.length!=1){
				alert("请选择一个策略");
				model.strategyMain.tabPanel().activeTab(0);
				return;
			}
			st_id = srows[0].ST_ID;
			st_name = srows[0].ST_NAME;
			
			$('#strategy_rule_list').empty();
			
			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"sr-btn-add", icon:"icon-tb-add", text:"选择"},
					{id:"sr-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'}
				],
				qform:{display:false,items:[
	              {name:'RULE_SHORTDESC', label:'规则名称'},
	              {name:'EVAL_TYPE', label:'评估类型', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.rule.evaltype'}, selectedTopItem:true},
	              {name:'DISPOSAL', label:'处置方式', type:'selector', title:'--请选择--',  items:[{text:'--请选择--',value:''}], ds:{type:'remote', url:'/tms35/rule/disposal', parser:{key:'row', text:'DP_NAME', value:'DP_CODE'}, lazyLoad:true}, selectedTopItem:true},
	              {name:'RULE_ENABLE', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"规则名称", width: 60, dataIndex:'RULE_SHORTDESC'},
						{name:"评估类型", width: 20, dataIndex:'EVAL_TYPE', render:'tms.rule.evaltype'},
						{name:"处置方式", width: 20, dataIndex:'DISPOSAL', render:function(v){
							var return_text = v;
							$.each(dis.row, function(i,row){
								if(v != '' && v == row['DP_CODE'])
								{
									return_text = row['DP_NAME'];
									return false;
								}
							});
							return return_text;
						}},
						{name:"规则分值", width: 20, dataIndex:'RULE_SCORE'},
						//{name:"执行顺序", width: 20, dataIndex:'RULE_ORDER_VIEW'},
						{name:"有效性", width: 15, dataIndex:'RULE_ENABLE', render:'tms.mgr.rulestatus'}
					],
					pagebar: false
				}
			}, $('#strategy_rule_list'));
			
			addDialog = new jcl.ui.Dialog({title:'规则列表', draggable: true,width:1050});
			
			var isFirst = true;
			// 选择按钮点击事件
			grid.toolbar.onClick('#sr-btn-add', function(){
				addDialog.show();
				addDialog.container.attr('style','height:300px;position:relative;overflow-y:auto');// 设置滚动条
				//addDialog.addDom('formbox-rule_list1');
				if(isFirst){
					model.rule_c.initFlag(addDialog,txn_id, '1',st_id);
					isFirst = false;
				}else{
					model.rule_c.load(addDialog,txn_id, '1',st_id);
				}
				
			});
			// 删除按钮点击事件
			grid.toolbar.onClick('#sr-btn-del', function(){
				if(confirm('确定删除？')){
					var dataList = grid.selectedRows();
					var list = [];
					$.each(dataList, function(i, row){
						list.push($.extend({}, row, {ST_ID: st_id,ST_NAME: st_name,ST_TXN: txn_id}));
					});
					var json_data = JSON.stringify({'del':list});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/strategy/saveStrategyRule', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			
			function resetEffectivenessBtn(){
				if(dualAudit){ // readonly mode
					grid.toolbar.disable($('#sr-btn-add'), false);
					grid.toolbar.disable($('#sr-btn-del'), false);
					return;
				}
			}

			grid.table.onRowSelectChange(function(){
				resetEffectivenessBtn();
			});

			grid.table.onModelChange(function(){
				resetEffectivenessBtn();
			});

			// 加载数据
			this.load(txn_id); 

		},

		load: function(txnId) {
			var srows = model.strategyInfo.grid().table.selectedRows();
			if(srows.length!=1){
				alert("请选择一个策略");
				model.ruleMain.tabPanel().activeTab(0);
				return;
			}
			st_id = srows[0].ST_ID;
			st_name = srows[0].ST_NAME;
			
			txn_id = txnId;
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			if(dualAudit){ // readonly mode
				grid.toolbar.disable($('#sr-btn-add'), false);
				grid.toolbar.disable($('#sr-btn-del'), false);
			}
			
			jcl.postJSON('/tms35/rule/disposal', "txn_id="+txn_id, function(data1){
				dis = data1;
				dis.row.unshift({DP_NAME:'--请选择--',DP_CODE:''});
				// 初始化查询列表
				jcl.postJSON("/tms35/strategy/rulelist", "ST_ID="+st_id, function(data){
					grid.renderPage({list: data.row});
				});
				});
		},

		isDirty: function() {
			return true;
		},

		save: function() {

		},
		
		grid: function(){
			return grid;
		},
		
		rccallback: function(dataList){
			
			var list = [];
			$.each(dataList, function(i, row){
				list.push($.extend({}, row, {ST_ID: st_id,ST_NAME: st_name,ST_TXN: txn_id}));
			});
					
			var json_data = JSON.stringify({'add':list});
			var json_data_encode = encodeURIComponent(json_data);
			
			
			var url_para = "postData=" + json_data_encode + "&ST_ID=" + st_id;
			jcl.postJSON('/tms35/strategy/saveStrategyRule', url_para, function(callback){
				addDialog.hide();
				// 初始化查询列表
				jcl.postJSON("/tms35/strategy/rulelist", "ST_ID="+st_id, function(data){
					grid.renderPage({list: data.row});
				});
			});
		}
	};
	
})();