var model = window.model || {};
var txn_id = null; //存储交易树节点
(function() {
	var grid = null;
	var dis;
	model.rule = {
		init: function(txnId, page,st_id) {
			txn_id = txnId;
			this.initFlag(txn_id, '0',st_id, page);
		},

		load: function(txnId,type,st_id) {
			txn_id = txnId;
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			if(dualAudit){ // readonly mode
				grid.toolbar.disable($('#rule-btn-add'), false);
				grid.toolbar.disable($('#rule-btn-edit'), false);
				grid.toolbar.disable($('#rule-btn-del'), false);
				grid.toolbar.disable($('#rule-btn-copy'), false);
				grid.toolbar.disable($('#rule-btn-valid-c'), false);
				grid.toolbar.disable($('#rule-btn-valid-y'), false);
				grid.toolbar.disable($('#rule-btn-valid-n'), false);
				grid.toolbar.disable($('#rule-btn-ref'), false);
			}
			if(type==0){
				grid.toolbar.disable($('#rule-btn-valid-c'), false);
			}
			
			jcl.postJSON('/tms35/rule/disposal', "txn_id="+txn_id, function(data1){
				dis = data1;
				dis.row.unshift({DP_NAME:'--请选择--',DP_CODE:''});
				// 初始化查询列表
				jcl.postJSON("/tms35/rule/list", "RULE_TXN="+txn_id+"&type="+type+"&st_id="+st_id, function(data){
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
		
		initFlag: function(txnId, type,st_id, page){// type 0:规则，1：策略规则
			txn_id = txnId;
			$('#formbox-rule_list'+type).empty();
			
			var status = jcl.code.getCodes('tms.mgr.rulestatus');
			var status_list = new Array();
			$.each(status,function(i,code){
				var o ;
				if(i==0){
					o = {text:code, value:i, checked:true};
				}else{
					o = {text:code, value:i};
				}
				status_list.push(o);
			});
			var istest = jcl.code.getCodes('common.is');
			var istest_list = new Array();
			$.each(istest,function(i,code){
				var o ;
				if(i==0){
					o = {text:code, value:i, checked:true};
				}else{
					o = {text:code, value:i};
				}
				istest_list.push(o);
			});
			
			// 初始化表格
			grid = new jcl.ui.Grid({
				id:'formbox-rule_list'+type,
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"rule-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"rule-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"rule-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"rule-btn-copy", icon:"icon-tb-edit", text:"复制", enable:'oneRowSelected'},
					{id:"rule-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"rule-btn-valid-y", icon:"icon-tb-edit", text:"启用", enable:'rowSelected'},
					{id:"rule-btn-valid-n", icon:"icon-tb-edit", text:"停用", enable:'rowSelected'},
					{id:"rule-btn-ref", icon:"icon-tb-edit", text:"引用点", enable:'oneRowSelected'},
					{id:"rule-btn-valid-c", icon:"icon-tb-edit", text:"确定", enable:'rowSelected'}
				],
				qform:{display:false,items:[
	              {name:'RULE_SHORTDESC', label:'规则名称'},
	              {name:'EVAL_TYPE', label:'评估类型', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.rule.evaltype'}, selectedTopItem:true},
	              {name:'DISPOSAL', label:'处置方式', type:'selector',  title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'remote', url:'/tms35/rule/disposal', parser:{key:'row', text:'DP_NAME', value:'DP_CODE'}, lazyLoad:true}, selectedTopItem:true},
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
						//{name:"执行顺序", width: 20, dataIndex:'RULE_ORDER_VIEW'},
						{name:"动作数量", width: 20, dataIndex:'ACTION_COUNT'},
						{name:"是否测试", width: 15, dataIndex:'RULE_ISTEST', render:'common.is'},
						{name:"有效性", width: 15, dataIndex:'RULE_ENABLE', render:'tms.mgr.rulestatus'}
					],
					rowForm:{
		                items:[
	                       {label:"规则名称", name: 'RULE_SHORTDESC', type:'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"规则条件", name: 'RULE_COND_IN', type: 'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"表达式", name: 'RULE_COND', type: 'hidden'},
	                       {label:"评估类型", name: 'EVAL_TYPE', type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.rule.evaltype', selectedTopItem:true}, required:true},
	                       {label:"处置方式", name: 'DISPOSAL', type:'selector',  title:'--请选择--', items:[{text:'--请选择--',value:''}], required:true},
						   {label:"分值", name: 'RULE_SCORE', type: 'text', required:true},
						   // {label:"执行顺序", name: 'RULE_ORDER', type: 'text', required:true},
	                       {label:"是否测试", name: 'RULE_ISTEST', value:'0', type:'radio', items:istest_list, required:true},
	                       {label:"有效性", name: 'RULE_ENABLE', value:'0', type:'radio', items:status_list, required:true},
	                       {label:"规则描述", name: 'RULE_DESC', type:'textarea', entireRow:true, cls:'textarea-diy'}
	                   ],
	                   btns:[
	                       {text:'确定', id:'rule-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'rule-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			});
			
			
			if(type==0){
				grid.toolbar.disable($('#rule-btn-valid-c'), false);
			}
			
			// 表单打开事件
			grid.table.onRowFormShow(function(isAdd){
				grid.table.rowForm.getItem('RULE_COND_IN').component.attr("readonly","readonly");
				
				var disposal = '';
				if(!isAdd){
					disposal = grid.selectedOneRow().DISPOSAL;
				}
				
				grid.table.rowForm.getItem('DISPOSAL').component.reload(dis.row, {text:'DP_NAME', value:'DP_CODE'});// 根据函数类型过滤存储字段
				
				grid.table.rowForm.getItem('DISPOSAL').val(disposal);
			});
			
			// 表单处置方式选择事件
			 grid.table.rowForm.getItem('DISPOSAL').component.onChange(function(item){
			 	$.each(dis.row, function(i,row){
					if(item.value == '')
					{
						grid.table.rowForm.getItem('RULE_SCORE').val("");// 默认分值
						return false;
					}
					if(item.value==row['DP_CODE']){
						grid.table.rowForm.getItem('RULE_SCORE').val(row['DEFAULT_SCORE']);// 默认分值
						return false;
					}
				});
		    });
			
			// 新增/修改的保存
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_rule_data(data)){
					data.RULE_TXN = txn_id;

					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}

					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;;

					jcl.postJSON('/tms35/rule/save', url_para, function(data1){
						alert("保存成功");
						// 查询列表
						jcl.postJSON("/tms35/rule/list", "RULE_TXN=" + txn_id+"&type="+type+"&st_id="+st_id, function(data){
							grid.renderPage({
								list: data.row
							});
						});
					});
					return false;
				}else{
					return false;
				}
		    });
			
			// 删除按钮点击事件
			grid.toolbar.onClick('#rule-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/rule/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			// 确定按钮点击事件
			grid.toolbar.onClick('#rule-btn-valid-c', function(){
				model.strategyRule.rccallback(grid.selectedRows());
			});
			
			// 规则条件
			var _ce = new condEdit();
			grid.table.rowForm.jqDom.on('dblclick', '[name=RULE_COND_IN]', function(){
				_ce.init_cond(grid.table.rowForm.getItem('RULE_COND').component,$(this),txn_id, ['RULE_FUNC'],'条件');
			});
			
			// 启用按钮事件
			grid.toolbar.onClick('#rule-btn-valid-y', function(){
				if(confirm('确定启用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {RULE_ENABLE: '1'})
						);
					});

					var json_data = JSON.stringify({'valid-y':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/rule/save', url_para, function(callback){
						grid.table.updateSelectedRow({RULE_ENABLE: 1});
						alert("启用成功");
					});

					return;
				}else{
					return;
				}
			});

			// 停用按钮事件
			grid.toolbar.onClick('#rule-btn-valid-n', function(){
				if(confirm('确定停用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {RULE_ENABLE: '0'})
						);
					});

					var json_data = JSON.stringify({'valid-n':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/rule/save', url_para, function(callback){
						grid.table.updateSelectedRow({RULE_ENABLE: 0});
						alert("停用成功");
					});

					return;
				}else{
					return;
				}
			});
			
			function resetEffectivenessBtn(){

				if(dualAudit){ // readonly mode
					grid.toolbar.disable($('#rule-btn-add'), false);
					grid.toolbar.disable($('#rule-btn-edit'), false);
					grid.toolbar.disable($('#rule-btn-del'), false);
					grid.toolbar.disable($('#rule-btn-copy'), false);
					grid.toolbar.disable($('#rule-btn-ref'), false);
					grid.toolbar.disable($('#rule-btn-valid-c'), false);
					grid.toolbar.disable($('#rule-btn-valid-y'), false);
					grid.toolbar.disable($('#rule-btn-valid-n'), false);
					grid.table.rowForm.jqDom.find("#rule-row-edit-sure").hide();
					return;
				}
				if(type==0){
					grid.toolbar.disable('#rule-btn-valid-c', false);
				}

				grid.toolbar.enable('#rule-btn-valid-y');
				grid.toolbar.enable('#rule-btn-valid-n');

				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#rule-btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#rule-btn-valid-n'); 
				} else {
					$.each(grid.selectedRows(), function(idx, row){
						if(row.RULE_ENABLE == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#rule-btn-valid-y'); // 启用按钮 disable
						} else if(row.RULE_ENABLE == '0'){
							grid.toolbar.disable('#rule-btn-valid-n');
						}
					});
				}
			}

			grid.table.onRowSelectChange(function(){
				resetEffectivenessBtn();
			});

			grid.table.onModelChange(function(){
				resetEffectivenessBtn();
			});

			// 查看按钮事件
			grid.toolbar.onClick('#rule-btn-view', function(){
				grid.table.rowForm.jqDom.find("#rule-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#rule-btn-add', function(){
				grid.table.rowForm.jqDom.find("#rule-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#rule-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#rule-row-edit-sure").show();
			});
			// 复制按钮事件
			grid.toolbar.onClick('#rule-btn-copy', function(){
				
				if (confirm('确定复制？')) {
					var _r = grid.table.selectedOneRow();
					_r.RULE_TXN = txn_id;
					var rule_shortdesc = _r.RULE_SHORTDESC + "_复制";
					if (!checkeLength(rule_shortdesc, 128)) {
						alert('规则复制后规则名称超过128个字符，复制失败！');
						return false;
					}
					else {
						_r.RULE_SHORTDESC = rule_shortdesc;
					}
					
					var json_data = JSON.stringify({
						'copy': [_r]
					});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;
					
					jcl.postJSON('/tms35/rule/save', url_para, function(data1){
						alert("复制成功");
						// 查询列表
						jcl.postJSON("/tms35/rule/list", "RULE_TXN=" + txn_id+"&type="+type+"&st_id="+st_id, function(data){
							grid.renderPage({
								list: data.row
							});
						});
					});
				}else{
					return;
				}
			});
			var addDialog = new jcl.ui.Dialog({title:'策略列表', draggable: true,width:920});
			
			// 引用点按钮事件
			grid.toolbar.onClick('#rule-btn-ref', function(){
				//ref.initTree(txn_id, grid.table.selectedOneRow().RULE_NAME, 0);
				$('#rule-ref_list').empty();			
				var ruleRefrid = new jcl.ui.Grid({
					title: null,
					marginTop: 0,
					checkboxEnable:false,
					table:{
						sortType: 'local',
						columns:[
							{name:"策略名称", width: 50, dataIndex:'ST_NAME'},
							{name:"所属交易", width: 50, dataIndex:'TAB_DESC'},
							{name:"评估方式", width: 20, dataIndex:'EVAL_MODE', render:'tms.strategy.evalmode'},
							{name:"规则执行方式", width: 20, dataIndex:'RULE_EXEC_MODE', render:'tms.strategy.rule_exec_mode'},
							{name:"规则数量", width: 20, dataIndex:'RULE_COUNT'},
							{name:"创建时间", width: 30, dataIndex:'CREATETIME',render:'datetime'},
							{name:"修改时间", width: 30, dataIndex:'MODIFYTIME',render:'datetime'},
							{name:"有效性", width: 15, dataIndex:'ST_ENABLE', render:'tms.mgr.rulestatus'}
						],
						pagebar: false
					}
				}, $('#rule-ref_list'));
				
				addDialog.show();
				addDialog.addDom('rule-ref_list');
				var rule_id = grid.selectedOneRow().RULE_ID;
				// 初始化查询列表
				jcl.postJSON("/tms35/strategy/refList", "RULE_ID="+rule_id, function(data){
					ruleRefrid.renderPage({list: data.row});
				});
			});
			// 加载数据
			this.load(txn_id,type,st_id); 

			
		}
		
	};
	
	
		
	// 校验输入域有效性
	function check_rule_data(data){
		var rule_shortdesc=data.RULE_SHORTDESC;
		var rule_desc=data.RULE_DESC;
		var rule_cond=data.RULE_COND;
		var rule_score=data.RULE_SCORE;
		var eval_type=data.EVAL_TYPE;
		//var rule_order=data.RULE_ORDER;
		var disposal=data.DISPOSAL;

		if (Trim(rule_shortdesc) == "") {
			alert('规则名称不能为空');
			return false;
		}
		if (!checkeLength(rule_shortdesc, 128)) {
			alert('规则名称不能超过128个字符');
			return false;
		}
		if (!checkSpecialCharacter(rule_shortdesc, "2")) {
			alert('规则名称只能包含汉字,字母,数字和下划线');
			return false;
		}
		if(Trim(rule_cond) == "") {
			alert('规则条件不能为空');
			return false;
		}
		if (!checkeLength(rule_cond, 1024)) {
			alert('规则条件不能超过1024个字符');
			return false;
		}
		if(Trim(eval_type) == "") {
			alert('评估类型不能为空');
			return false;
		}
		if(Trim(disposal) == "") {
			alert('处置方式不能为空');
			return false;
		}
		if (Trim(rule_desc) != "") {
			if (!checkeLength(rule_desc, 1024)) {
				alert('规则描述不能超过1024个字符');
				return false;
			}
			if (!checkeSpecialCode(rule_desc)) {
				alert('规则描述不能输入特殊字符');
				return false;
			}
		}
		
		if (Trim(rule_score) == "") {
			alert('规则分值不能为空');
			return false;
		}
		if (!IsInt(rule_score)) {
			alert('规则分值不是-100~100之间的整数');
			return false;
		}
		if (rule_score > 100||rule_score < -100) {
			alert('规则分值不是-100~100之间的整数');
			return false;
		}
		/*if (Trim(rule_order) == "") {
			alert('执行顺序不能为空');
			return false;
		}	
		if (!IsInt(rule_order, "+", 0)) {
			alert('执行顺序不是0-99999非负整数');
			return false;
		}	
		if (rule_order > 99999) {
			alert('执行顺序不是0-99999非负整数');
			return false;
		}*/		
		/*jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acCond+"&TXNID="+txnId,function(data){
			jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acSrc+"&TXNID="+txnId,function(data){
			});
		});*/
		return true;
	}
})();

