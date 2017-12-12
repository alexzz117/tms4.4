//// 对Date的扩展，将 Date 转化为指定格式的String 
//// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
//// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
//// 例子： 
//// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
//// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
//Date.prototype.Format = function(fmt) 
//{ //author: meizz 
//  var o = { 
//    "M+" : this.getMonth()+1,                 //月份 
//    "d+" : this.getDate(),                    //日 
//    "h+" : this.getHours(),                   //小时 
//    "m+" : this.getMinutes(),                 //分 
//    "s+" : this.getSeconds(),                 //秒 
//    "q+" : Math.floor((this.getMonth()+3)/3), //季度 
//    "S"  : this.getMilliseconds()             //毫秒 
//  }; 
//  if(/(y+)/.test(fmt)) 
//    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
//  for(var k in o) 
//    if(new RegExp("("+ k +")").test(fmt)) 
//  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
//  return fmt; 
//};

var model = window.model || {};
(function(){
	var grid = null;
	var swtchiIsDirty = false;
	var g_txnId = '';
	
	model.switcher = {
		init: function(txnId, _page){
			g_txnId = txnId;
			$('#formbox-switch_mdl').empty();
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
			grid = new jcl.ui.Grid({
				id : "switch",
				title: null,
				marginTop: 0,
				toolbar:[
				    {icon:"icon-tb-find", text:"查询", action:'toggleQform'},
				    {id:"switch_btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"switch_btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"switch_btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"switch_btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"btn-valid-y", icon:"icon-tb-edit", text:"启用"},
					{id:"btn-valid-n", icon:"icon-tb-edit", text:"停用"}
					//{id:"switch_btn-save", icon:"icon-tb-gear", text:'保存', enable:'modelDirty'}
				],
				qform:{
					display:false,
					items:[
					    {name:'SW_DESC', label:'开关名称'},
					    {name:'SW_ORDER', label:'开关顺序'},
					    {name:'SW_ENABLE', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
					], 
					action:'localQuery'
				},
				table:{
					sortType : 'local',
					columns:[
						{name:'开关顺序', width: 30, dataIndex:'SW_ORDER'},
						{name:'开关名称', width: 60, dataIndex:'SW_DESC'},
						{name:'开关类型', width: 30, dataIndex:'SW_TYPE', render:'tms35.model.switch.type'},
						{name:'开关条件', width: 60, dataIndex:'SW_COND_IN'},
						{name:'开关状态', width: 40, dataIndex:'SW_ENABLE', render:'tms.mgr.rulestatus'},
//						{name:'所在交易', width: 40, dataIndex:'TAB_DESC'},
						{name:'添加时间', width: 60, dataIndex:'SW_CREATETIME'},
						{name:'修改时间', width: 60, dataIndex:'SW_MODIFYTIME'}
					],
					rowForm:{
						
		                items:[
//	                       {name: 'SW_ID', type:'hidden'},
	                       {label:'开关顺序', name: 'SW_ORDER', type:'text', required : true, value:'0'},
	                       {label:'开关条件', name: 'SW_COND_IN',  type:'text'},
	                       {label:'开关条件', name: 'SW_COND',  type:'hidden'},
	                       {label:'开关名称', name: 'SW_DESC',  type:'text', required : true},
	                       {name: 'SW_ID',  type:'hidden'},
	                       {name: 'SW_CREATETIME',  type:'hidden'},
	                       // {label:'添加时间', name: 'SW_DESC', type: 'text'},
	                       {label:'开关类型', name: 'SW_TYPE',  type:'selector', ds:{type:'code', category:'tms35.model.switch.type', selectedTopItem:true}},
	                     //  {label:'开关状态', name: 'SW_ENABLE',type:'radio',  items:[{text:'启用', value:'1', checked:true}, {text:'停用', value:'0'}]},
	                       {label:"有效性", name: 'SW_ENABLE', value:'0', type:'radio', items:status_list, required:true}
//	                       {label:'所在交易', name: 'TAB_DESC', type:'span', value:page.tree.getNode(txnId).text}
//	                       {label:'开关状态', name: 'SW_ENABLE', type:'selector', title:'请选择', ds:{type:'code', category:'tms.mgr.rulestatus'}}
	                   ],
	                   btns:[
	                       {text:'确定', id:'sw-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'sw-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#formbox-switch_mdl'));
			
			var _ce = new condEdit();
			// bind cond dialog on dblclick
			grid.table.rowForm.jqDom.on('dblclick', '[name=SW_COND_IN]', function(){
				var needHideSelectArr = ['RULE_FUNC', 'STAT_FN'];				
				_ce.init_cond(grid.table.rowForm.getItem('SW_COND').component,$(this),g_txnId, needHideSelectArr,'条件');
			});
			
			grid.table.onRowFormShow(function(isAdd){
				grid.table.rowForm.getItem('SW_COND_IN').component.attr("readonly","readonly");
			});
			
			// 查看按钮事件
			grid.toolbar.onClick('#switch_btn-view', function(){
				grid.table.rowForm.jqDom.find("#sw-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#switch_btn-add', function(){
				grid.table.rowForm.jqDom.find("#sw-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#switch_btn-edit', function(){
				grid.table.rowForm.jqDom.find("#sw-row-edit-sure").show();
			});
			
			// 启用按钮事件
			grid.toolbar.onClick('#btn-valid-y', function(){

				if(confirm('确定启用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {SW_ENABLE: '1'})
						);
					});
					
					var json_data = JSON.stringify({'valid-y':list});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;
					
					jcl.postJSON('/tms35/switch/save', url_para, function(callback){
						grid.table.updateSelectedRow({SW_ENABLE: 1});
						alert("启用成功");
					});
					
					return;
				} else {
					return;
				}
			});
			
			// 停用按钮事件
			grid.toolbar.onClick('#btn-valid-n', function(){

				if(confirm('确定停用？')){
					
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {SW_ENABLE: '0'})
						);
					});
					
					var json_data = JSON.stringify({'valid-n':list});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;
					
					jcl.postJSON('/tms35/switch/save', url_para, function(callback){
						grid.table.updateSelectedRow({SW_ENABLE: 0});
						alert("停用成功");
					});
					
					return;
				} else {
					return;
				}
			});

			function resetEffectivenessBtn(){
				
				if(dualAudit){ // readonly mode
					grid.toolbar.disable($('#switch_btn-add'), false);
					grid.toolbar.disable($('#switch_btn-edit'), false);
					grid.toolbar.disable($('#switch_btn-del'), false);
					grid.toolbar.disable($('#btn-valid-y'), false);
					grid.toolbar.disable($('#btn-valid-n'), false);
					grid.table.rowForm.jqDom.find("#sw-row-edit-sure").hide();
					return;
				}
				
				grid.toolbar.enable('#btn-valid-y');
				grid.toolbar.enable('#btn-valid-n'); 
				
				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#btn-valid-n'); 
				} else {
					
					$.each(grid.selectedRows(), function(idx, row){
						if(row.SW_ENABLE == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#btn-valid-y'); // 启用按钮 disable
						} else if(row.SW_ENABLE == '0'){
							grid.toolbar.disable('#btn-valid-n'); 
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

			grid.toolbar.onClick('#switch_btn-save', function(){
				
				var json_data = JSON.stringify(grid.table.change());
				var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;
				jcl.postJSON('/tms35/switch/save', url_para, function(callback){
					alert("保存成功");
				});
			});
			
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				
				if(sw_check_data(sw_format_data(data))){

					var page_rows = grid.table.page.list;
					var sw_desc = data.SW_DESC;
					var sw_order = data.SW_ORDER;
					var sw_id = data.SW_ID;
					var is_same = false;
					
					$.each(page_rows, function(){
					    
					    if(sw_id != this.SW_ID){ // keep self out
					        if(sw_desc == this.SW_DESC){
					            alert('重复的开关名称');
					            is_same = true;
					            return false;
					        }
					        if(sw_order == this.SW_ORDER){
					            alert('重复的开关顺序');
					            is_same = true;
					            return false;
					        }
					    }
					});
					
					if(is_same){
						swtchiIsDirty = false;
						return false;
					}
					
					var node = _page.tree.getNode(g_txnId);
					data.SW_TXN = node.id;
					data.TAB_DESC = node.text;
					swtchiIsDirty = false;

					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}
					
					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					//var cc = rowFormJqDom.serialize();
					//var u1 = encodeURIComponent(json_data);
					//var u2 = encodeURI(json_data);
					//var u3 = escape(json_data);
					
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;
					jcl.postJSON('/tms35/switch/save', url_para, function(callback){
						
						if(!isAdd) {

							callback.row.SW_MODIFYTIME = callback.row.SW_MODIFYTIME ? jcl.util.convert.datetime(callback.row.SW_MODIFYTIME) : "";
						} else {
							
							callback.row.SW_CREATETIME = jcl.util.convert.datetime(callback.row.SW_CREATETIME);
						}
						
						grid.table.updateEditingRow(callback.row);
						grid.table.localSort('SW_ORDER', 'asc');
						alert("保存成功");
					});
					return false;
				} else {
					return false;
				}
		    });
			
			grid.toolbar.onClick('#switch_btn-del', function(){
				if(confirm('确定删除？')){
					
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;
					
					jcl.postJSON('/tms35/switch/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						swtchiIsDirty = false;
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			page = _page;
			
			this.load(g_txnId);
		},
		load: function(txnId){
			g_txnId = txnId;
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			//$.cookie('the_cookie', 'the_value', {path:'/'});
			//jcl.cookie.set('the_cookie', 'the_value');
			jcl.postJSON('/tms35/switch/list', "txnId=" + txnId, function(data){
				$.each(data.switchs, function(idx, pro_obj){
					
					pro_obj.SW_CREATETIME = jcl.util.convert.datetime(pro_obj.SW_CREATETIME);
					pro_obj.SW_MODIFYTIME = pro_obj.SW_MODIFYTIME ? jcl.util.convert.datetime(pro_obj.SW_MODIFYTIME) : "";
				});
				grid.renderPage({list: data.switchs});
			});
		},
		isDirty: function(){
			return false;
		},
		save: function(){
			
		}
	};
	
	/**
	 * 格式转换
	 * 顺序:0004=>4
	 */
	function sw_format_data(data){
		var order = data.SW_ORDER;
		data.SW_ORDER = /^\d+$/.test(order) ? parseInt(order*1) : order;
		return data;
	}
	
	function sw_check_data(data){
		var allow = false;
		var order = data.SW_ORDER + '';
		var cond = data.SW_COND;
		var desc = data.SW_DESC;

		var reg = /^\d+$/;
		
		if(desc == undefined || Trim(desc)==""){
			alert("开关名称不允许为空");
			return allow;
		} else {

			if(!checkeLength(desc,20)){
				alert('开关名称不能超过20个字符');
				return allow;
			}
			
			//if(!checkeSpecialCode(desc)){
			//	alert('开关名称不能包含特殊字符');
			//	return allow;
			//}
			
			if(!checkSpecialCharacter(desc, "2")){
				alert("开关名称只能包含汉字,字母,数字和下划线");
				return allow;
			}
		}
		if(order == undefined || Trim(order)==""){
			alert("开关执行顺序不允许为空");
			return allow;
		} else {

			if(!checkeLength(order, 8)){
				alert('开关执行顺序不能超过8个字符');
				return allow;
			}
			
			if(!reg.test(order)){
				alert("开关执行顺序必须为非负整数");
				return allow;
			}
		}

		if(cond == '' || cond == null){
			//alert("开关条件不允许为空");
			//return allow;
		} else {
			
			/*if(!checkeSpecialCode(cond)){
				alert('开关条件不能包含特殊字符');
				return allow;
			}*/
			if(!checkeLength(cond, 250)){
				alert('开关条件不能超过250个字符');
				return allow;
			}
		}
		
		return true;
	}
})();