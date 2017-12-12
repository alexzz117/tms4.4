
var model = window.model ||{};
var txn_id = null; //存储交易树节点
(function(){
	var grid = null;
	var rule_id="";
	var rule_shortdesc = "";
	model.ac = {
		init: function(this_txnId, page){
			txn_id = this_txnId;
			var srows = model.rule.grid().table.selectedRows();
			if(srows.length!=1){
				alert("请选择一个规则");
				model.ruleMain.tabPanel().activeTab(0);
				model.ruleMain.tabPanel().load(txn_id);
				return;
			}
			rule_id = srows[0].RULE_ID;
			rule_shortdesc = srows[0].RULE_SHORTDESC;
			
			$('#acGridDiv').empty();
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

			// 初始化表格
			grid = new jcl.ui.Grid({
				title: null,
				marginTop: 0,
				toolbar:[
					{id:"btn-find", icon:"icon-tb-find", text:"查询", action:'toggleQform'},
					{id:"ac-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"ac-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"ac-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"ac-btn-copy", icon:"icon-tb-edit", text:"复制", enable:'oneRowSelected'},
					{id:"ac-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"ac-btn-valid-y", icon:"icon-tb-edit", text:"启用"},
					{id:"ac-btn-valid-n", icon:"icon-tb-edit", text:"停用"}
				],
				qform:{display:false,items:[
	              {name:'AC_DESC', label:'动作名称'},
	              {name:'AC_ENABLE', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"动作名称", width: 30, dataIndex:'AC_DESC'},
						{name:"动作条件", width: 60, dataIndex:'AC_COND_IN'},
						{name:"处理函数", width: 60, dataIndex:'AC_EXPR_IN'},
						{name:"有效性", width: 30, dataIndex:'AC_ENABLE', render:'tms.mgr.rulestatus'}
					],
					rowForm:{
		                items:[
	                       {label:"动作名称", name: 'AC_DESC', type:'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"动作条件", name: 'AC_COND_IN', type: 'text', entireRow:true, cls:'text-diy'},
	                       {label:"表达式", name: 'AC_COND', type: 'hidden'},
	                       {label:"处理函数", name: 'AC_EXPR_IN', type: 'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"表达式", name: 'AC_EXPR', type: 'hidden'},
	                       {label:"有效性", name: 'AC_ENABLE', value:'0', type:'radio', items:status_list, required:true}
	                   ],
	                   btns:[
	                       {text:'确定', id:'ac-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'ac-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#acGridDiv'));


			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_ac_data(data)){
					data.RULE_ID = rule_id;
					data.RULE_SHORTDESC = rule_shortdesc;
					data.RULE_TXN = txn_id;

					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}

					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&rule_id=" + rule_id;;

					jcl.postJSON('/tms35/action/save', url_para, function(data1){
						grid.table.updateEditingRow(data1.row);
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });

			grid.toolbar.onClick('#ac-btn-del', function(){
				if(confirm('确定删除？')){
					var dataList = grid.selectedRows();
					var list = [];
					$.each(dataList, function(i, row){
						list.push($.extend({}, row, {RULE_TXN: txn_id,RULE_SHORTDESC:rule_shortdesc}));
					});
					
					var json_data = JSON.stringify({'del':list});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&rule_id=" + rule_id;

					jcl.postJSON('/tms35/action/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			var _ce = new condEdit();
			grid.table.rowForm.jqDom.on('dblclick', '[name=AC_COND_IN]', function(){
				_ce.init_cond(grid.table.rowForm.getItem('AC_COND').component,$(this),txn_id, ['RULE_FUNC','AC_FUNC'],'条件');
			});
			grid.table.rowForm.jqDom.on('dblclick', '[name=AC_EXPR_IN]', function(){
				_ce.init_cond(grid.table.rowForm.getItem('AC_EXPR').component,$(this),txn_id, ['RULE_FUNC'],'条件');
			});
			
			grid.table.onRowFormShow(function(isAdd){
				grid.table.rowForm.getItem('AC_COND_IN').component.attr("readonly","readonly");
				grid.table.rowForm.getItem('AC_EXPR_IN').component.attr("readonly","readonly");
			});
			
			// 加载数据
			this.load(txn_id);

			// 启用按钮事件
			grid.toolbar.onClick('#ac-btn-valid-y', function(){
				if(confirm('确定启用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {AC_ENABLE: '1',RULE_TXN: txn_id,RULE_SHORTDESC:rule_shortdesc})
						);
					});

					var json_data = JSON.stringify({'valid-y':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&rule_id=" + rule_id;

					jcl.postJSON('/tms35/action/save', url_para, function(callback){
						grid.table.updateSelectedRow({AC_ENABLE: 1});
						alert("启用成功");
					});

					return;
				}else{
					return;
				}
			});

			// 停用按钮事件
			grid.toolbar.onClick('#ac-btn-valid-n', function(){
				if(confirm('确定停用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {AC_ENABLE: '0',RULE_TXN: txn_id,RULE_SHORTDESC:rule_shortdesc})
						);
					});

					var json_data = JSON.stringify({'valid-n':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&rule_id=" + rule_id;

					jcl.postJSON('/tms35/action/save', url_para, function(callback){
						grid.table.updateSelectedRow({AC_ENABLE: 0});
						alert("停用成功");
					});

					return;
				}else{
					return;
				}
			});

			function resetEffectivenessBtn(){

				if(dualAudit){ // readonly mode

					grid.toolbar.disable($('#ac-btn-add'), false);
					grid.toolbar.disable($('#ac-btn-edit'), false);
					grid.toolbar.disable($('#ac-btn-del'), false);
					grid.toolbar.disable($('#ac-btn-copy'), false);
					grid.toolbar.disable($('#ac-btn-valid-y'), false);
					grid.toolbar.disable($('#ac-btn-valid-n'), false);
					grid.table.rowForm.jqDom.find("#ac-row-edit-sure").hide();
					return;
				}

				grid.toolbar.enable('#ac-btn-valid-y');
				grid.toolbar.enable('#ac-btn-valid-n');

				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#ac-btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#ac-btn-valid-n'); 
				} else {
					$.each(grid.selectedRows(), function(idx, row){
						if(row.AC_ENABLE == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#ac-btn-valid-y'); // 启用按钮 disable
						} else if(row.AC_ENABLE == '0'){
							grid.toolbar.disable('#ac-btn-valid-n');
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
			grid.toolbar.onClick('#ac-btn-view', function(){
				grid.table.rowForm.jqDom.find("#ac-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#ac-btn-add', function(){
				grid.table.rowForm.jqDom.find("#ac-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#ac-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#ac-row-edit-sure").show();
			});
			// 复制按钮事件
			grid.toolbar.onClick('#ac-btn-copy', function(){
				if (confirm('确定复制？')) {
					var _r = grid.table.selectedOneRow();
					var ac_desc = _r.AC_DESC + "_复制";
					if (!checkeLength(ac_desc, 128)) {
						alert('动作复制后动作名称超过128个字符，复制失败！');
						return false;
					}
					else {
						_r.AC_DESC = ac_desc;
					}
					
					_r.RULE_TXN=txn_id;
					_r.RULE_SHORTDESC=rule_shortdesc;
					var json_data = JSON.stringify({
						'copy': [_r]
					});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;
					;
					
					jcl.postJSON('/tms35/action/save', url_para, function(data1){
						alert("复制成功");
						// 查询列表
						jcl.postJSON("/tms35/action/list", "rule_id=" + rule_id, function(data){
							grid.renderPage({
								list: data.row
							});
						});
					});
				}else{
					return;
				}
			});
		},
		load: function(txnId){
			
			var srows = model.rule.grid().table.selectedRows();
			if(srows.length!=1){
				alert("请选择一个规则");
				model.ruleMain.tabPanel().activeTab(0);
				return;
			}
			rule_id = srows[0].RULE_ID;
			rule_shortdesc = srows[0].RULE_SHORTDESC;
						
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			if(dualAudit){ // readonly mode
				grid.toolbar.disable($('#ac-btn-add'), false);
				grid.toolbar.disable($('#ac-btn-edit'), false);
				grid.toolbar.disable($('#ac-btn-del'), false);
				grid.toolbar.disable($('#ac-btn-copy'), false);
				grid.toolbar.disable($('#ac-btn-valid-y'), false);
				grid.toolbar.disable($('#ac-btn-valid-n'), false);
			}

			// 初始化查询列表
			jcl.postJSON("/tms35/action/list", "rule_id="+rule_id, function(data){
				grid.renderPage({list: data.row});
			});

		},
		isDirty: function(){
			return false;//ac_isDirty;
		},
		save: function(){

		}
	};

	function check_ac_data(data){
		var acDesc=data.AC_DESC;
		var acExpr=data.AC_EXPR;
		var acCond=data.AC_COND;

		if(acDesc == undefined || Trim(acDesc)==""){
			alert("动作名称不能为空");
			return false;
		}else{
			if(!checkeLength(acDesc,128)){
				alert('动作名称不能超过128个字符');
				return false;
			}
			if (!checkSpecialCharacter(acDesc, "2")) {
				alert('动作名称只能包含汉字,字母,数字和下划线');
				return false;
			}
		}
		if(acCond != undefined && acCond != ""){
			if(!checkeLength(acCond,512)){
				alert('动作条件不能超过512个字符');
				return false;
			}else{
				if(!checkeCondSpecialCode(acCond)){
					alert('动作条件不能包含特殊字符');
					return false;
				}
			}
		}
		if(acExpr != undefined && acExpr != ""){
			if(!checkeLength(acExpr,512)){
				alert('处理函数不能超过512个字符');
				return false;
			}else{
				if(!checkeCondSpecialCode(acExpr)){
					alert('处理函数不能包含特殊字符');
					return false;
				}
			}
		}else{
			alert('处理函数不能为空');
			return false;
		}

		/*jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acCond+"&TXNID="+txnId,function(data){
			jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acSrc+"&TXNID="+txnId,function(data){
			});
		});*/
		return true;
	}

})();
