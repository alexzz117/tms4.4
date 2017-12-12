var model = window.model || {};
(function(){
	
	//处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外  
    function forbidBackSpace(e) {  
        var ev = e || window.event; //获取event对象   
        var obj = ev.target || ev.srcElement; //获取事件源   
        var t = obj.type || obj.getAttribute('type'); //获取事件源类型   
        //获取作为判断条件的事件类型   
        var vReadOnly = obj.readOnly;  
        var vDisabled = obj.disabled;  
        //处理undefined值情况   
        vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;  
        vDisabled = (vDisabled == undefined) ? true : vDisabled;  
        //当敲Backspace键时，事件源类型为密码或单行、多行文本的，   
        //并且readOnly属性为true或disabled属性为true的，则退格键失效   
        var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea") && (vReadOnly == true || vDisabled == true);  
        //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效   
        var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea";  
        //判断   
        if (flag2 || flag1) return false;  
    }  
    //禁止后退键 作用于Firefox、Opera  
    document.onkeypress = forbidBackSpace;  
    //禁止后退键  作用于IE、Chrome  
    document.onkeydown = forbidBackSpace;  
	
	var grid = null;
	var ps_value_dialog = {};
	var processIsDirty = false;
	var g_txnId = '';
	
	model.processor = {
		init: function(txnId, page){
			g_txnId = txnId;
			$('#formbox-process_mdl').empty();
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
				id : "process",
				title: null,
				marginTop: 0,
				toolbar:[
				    {icon:"icon-tb-find", text:"查询", action:'toggleQform'},
				    {id:"process_btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"process_btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"process_btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"process_btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"ps-btn-valid-y", icon:"icon-tb-edit", text:"启用"},
					{id:"ps-btn-valid-n", icon:"icon-tb-edit", text:"停用"}
					//{id:"process_btn-save", icon:"icon-tb-gear", text:'保存', enable:'modelDirty'}
				],
				qform:{
					display:false,
					items:[
					       {name:'PS_NAME', label:'处置名称'},
					       {name:'PS_ORDER', label:'处置顺序'},
					       {name:'PS_ENABLE', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
					], 
				action:'localQuery'},
				table:{
					sortType : 'local',
					columns:[
						{name:'处置顺序', width: 25, dataIndex:'PS_ORDER'},
						{name:'处置名称', width: 50, dataIndex:'PS_NAME'},
						{name:'处置值', width: 100, dataIndex:'PS_VALUE_DESC'},
						{name:'处置条件', width: 60, dataIndex:'PS_COND_IN'},
						{name:'处置状态', width: 25, dataIndex:'PS_ENABLE', render:'tms.mgr.rulestatus'}
//						{name:'所在交易', width: 40, dataIndex:'TAB_DESC'}
					],
					rowForm:{

		                items:[
	                       {label:'处置顺序', name: 'PS_ORDER', type:'text', value:'0', required : true},
	                       //{label:'处置条件', name: 'PS_COND',  type:'text', required : true},
	                       {label:'处置条件', name: 'PS_COND_IN',  type:'text'},
	                       {label:'处置条件', name: 'PS_COND',  type:'hidden'},
	                       {label:'处置值',   name: 'PS_VALUE_DESC', type:'text', required : true},
	                       {label:'处置名称', name: 'PS_NAME',  type:'text', required : true},
	                       {label:"有效性", name: 'PS_ENABLE', value:'0', type:'radio', items:status_list, required:true},

	                       {name: 'PS_ID', type:'hidden'},
	                       {name: 'PS_VALUE', type:'hidden'}
	                   ],
	                   btns:[
	                       {text:'确定', id:'ps-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'ps-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#formbox-process_mdl'));

			var _ce = new condEdit();
			// bind cond dialog on dblclick
			grid.table.rowForm.jqDom.on('dblclick', '[name=PS_COND_IN]', function(){
				var needHideSelectArr = ['RULE_FUNC'];
				_ce.init_cond(grid.table.rowForm.getItem('PS_COND').component,$(this),g_txnId, needHideSelectArr,'条件');
			});
			
			function resetEffectivenessBtn(){
				
				if(dualAudit){ // readonly mode
					//console.log("resetEffectivenessBtn=" + grid); 
					grid.toolbar.disable($('#process_btn-add'), false);
					grid.toolbar.disable($('#process_btn-edit'), false);
					grid.toolbar.disable($('#process_btn-del'), false);
					grid.toolbar.disable($('#ps-btn-valid-y'), false);
					grid.toolbar.disable($('#ps-btn-valid-n'), false);
					grid.table.rowForm.jqDom.find("#ps-row-edit-sure").hide();
					return;
				}
				
				grid.toolbar.enable('#ps-btn-valid-y');
				grid.toolbar.enable('#ps-btn-valid-n'); 
				
				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#ps-btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#ps-btn-valid-n'); 
				} else {
					$.each(grid.selectedRows(), function(idx, row){
						if(row.PS_ENABLE == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#ps-btn-valid-y'); // 启用按钮 disable
						} else if(row.PS_ENABLE == '0'){
							grid.toolbar.disable('#ps-btn-valid-n'); 
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
			grid.toolbar.onClick('#process_btn-view', function(){
				grid.table.rowForm.jqDom.find("#ps-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#process_btn-add', function(){
				grid.table.rowForm.jqDom.find("#ps-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#process_btn-edit', function(){
				grid.table.rowForm.jqDom.find("#ps-row-edit-sure").show();
			});
			
			// 启用按钮事件
			grid.toolbar.onClick('#ps-btn-valid-y', function(){
				
				if(confirm('确定启用？')){
					
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
								$.extend({}, row, {PS_ENABLE: '1'})
						);
					});
					
					var json_data = JSON.stringify({'valid-y':list});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;
					
					jcl.postJSON('/tms35/process/save', url_para, function(callback){
						grid.table.updateSelectedRow({PS_ENABLE: 1});
						alert("启用成功");
					});
					
					return;
				} else {
					return;
				}
			});

			// 停用按钮事件
			grid.toolbar.onClick('#ps-btn-valid-n', function(){
				if(confirm('确定停用？')){

					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {PS_ENABLE: '0'})
						);
					});
					
					var json_data = JSON.stringify({'valid-n':list});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;

					jcl.postJSON('/tms35/process/save', url_para, function(callback){
						grid.table.updateSelectedRow({PS_ENABLE: 0});
						alert("停用成功");
					});

					return;
				}else{
					return;
				}
			});
			
			grid.table.rowForm.jqDom.on('dblclick', '[name=PS_VALUE_DESC]', function(){
				var temp = grid.table.rowForm.jqDom.find('[name=PS_VALUE]').val() ;
				var ps_value = temp == '' ? '0,PS01|30,PS02|50,PS04|80,PS05' : temp;
				var ps_value_arr = parsePsValue(temp);
				$('#table_ps_value tr:gt(1)').remove();
				if (!ps_value_arr || ps_value_arr.length == 0) {
					var ps_value_obj = {"ps_value_code" : "PS01", "start_score" : "0", "end_score" : "100"};
					insertNewPs(ps_value_obj, null);
				} else {
					$.each(ps_value_arr, function (idx, ps_value_obj){
						insertNewPs(ps_value_obj, null);
					});
				}
				ps_value_dialog.show();
			});

			grid.toolbar.onClick('#process_btn-save', function(){
				var json_data = JSON.stringify(grid.table.change());
				var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" +g_txnId;;
				jcl.postJSON('/tms35/process/save', url_para, function(callback){
					alert("保存成功");
				});
			});
			
			grid.table.onRowFormShow(function(isAdd){
				grid.table.rowForm.getItem('PS_COND_IN').component.attr("readonly","readonly");
			});
			
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(ps_check_data(ps_format_data(data))){
					var page_rows = grid.table.page.list;
					var ps_name = data.PS_NAME;
					var ps_order = data.PS_ORDER;
					var ps_id = data.PS_ID;
					var is_same = false;
					$.each(page_rows, function(){
					    if(ps_id != this.PS_ID){ // keep self out
					        if(ps_name == this.PS_NAME){
					            alert('重复的处置名称');
					            is_same = true;
					            return false;
					        }
					        if(ps_order == this.PS_ORDER){
					            alert('重复的处置顺序');
					            is_same = true;
					            return false;
					        }
					    }
					});
					if(is_same){
						processIsDirty = false;
						return false;
					}
					var node = page.tree.getNode(g_txnId);
					data.PS_TXN = node.id;
					data.TAB_DESC = node.text;
					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}
					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;;
					jcl.postJSON('/tms35/process/save', url_para, function(callback){
						grid.table.updateEditingRow(callback.row);
						grid.table.localSort('PS_ORDER', 'asc');
						alert("保存成功");
					});
					return false;
				} else {
					return false;
				}
		    });
			grid.toolbar.onClick('#process_btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var url_para = "postData=" + encodeURIComponent(json_data) + "&txnId=" + g_txnId;
					jcl.postJSON('/tms35/process/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			bindDialog(grid.table.rowForm.jqDom);
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
			
			jcl.postJSON('/tms35/process/list', "txnId=" + txnId, function(data){
				$.each(data.process, function(idx, pro_obj){
					pro_obj.PS_VALUE_DESC = parsePsValueToDesc(parsePsValue(pro_obj.PS_VALUE));
				});
				grid.renderPage({list: data.process});
				grid.table.rowForm.jqDom.find('[name=PS_VALUE_DESC]').prop("readonly", true);
			});
		},
		isDirty: function(){
			return false;
		},
		save: function(){

		}
	};
	function insertNewPs(ps_value_obj, idx){
		var $tr = $('<TR>');
		var $checkbox_td = $('<TD>', {width : '10%'});
		var $ps_select_td = $('<TD>', {width : '30%'});
		var $start_score_td = $('<TD>', {width : '30%'});
		var $end_score_td = $('<TD>', {width : '30%'});
		var $checkbox = $('<input>', {"type":"checkbox", "name":"ps_value_checkbox", "align":"center", "size":"10"});
		var $ps_select = $('<select name="PS_POV" align="center">');
//		var $ps_select = $('<select>', {"name":"PS_POV", "align" : "center", "class":"selector-itext"});
		var $start_score_text = $('<input>', {"type":"text", "name":"start_score", "class":"itext", "value":ps_value_obj.start_score});
		var $end_score_text = $('<input>', {"type":"text", "name":"end_score", "class":"itext", "value":ps_value_obj.end_score});
		var codes = jcl.code.getCodes("tms.rule.disposal");
		var htmlstr = '';
		for(var i in codes){
			if (ps_value_obj.ps_value_code == i) {
				htmlstr += '<option selected="selected" value="'+i+'">'+codes[i]+'</option>';
			} else
				htmlstr += '<option value="'+i+'">'+codes[i]+'</option>';
		}
		$ps_select.html(htmlstr);
		$tr.append($checkbox_td.append($checkbox));
		$tr.append($ps_select_td.append($ps_select));
		$tr.append($start_score_td.append($start_score_text));
		$tr.append($end_score_td.append($end_score_text));
		if(idx == null || $('#ps_value_tb tr').length == 0){
			$('#table_ps_value tbody[id=ps_value_tb]').append($tr);
		} else {
			if (idx >= $('#ps_value_tb tr').length){
				$('#ps_value_tb tr:last').after($tr) ;
			} else {
				$('#table_ps_value tbody[id=ps_value_tb] tr:eq('+idx+')').before($tr);
			}
		}
	}
	function bindDialog(jqDom){
		ps_value_dialog = new jcl.ui.Dialog({
			"title" : '处置值-0到100,前闭后开', 
			"zindex" : '701', 
			marginTop: 0,
			//width:650,
			draggable : true
		});
		ps_value_dialog.addDom('process_ps_value');
		$('#process_ps_value').on('click', '#ps_value_add_btn', function(){
			/*
			 * 1. check if over ps count
			 * 2. calculate which ps should be add
			 * 3. insert into a right row
			 */
			var codes = jcl.code.getCodes("tms.rule.disposal");
			var ps_count = 0;
			// calculate exist ps
			var $exist_ps = $('#process_ps_value [name=PS_POV]');
			var exist_ps_count = $('#process_ps_value [name=PS_POV]').length;
			var not_exist_arr = [];
			/*
			 * 1.1 calculate ps count in CODE
			 * 1.2 search which is not in
			 */
			$.each(codes, function(key, value){
				var exist = false;
				$.each($exist_ps, function(){
					if(key == $(this).val()){
					    exist = true;
						return false;
					}
				});
				exist ? null : not_exist_arr.push({key : key, value : value});
				ps_count++;
			});
			if(exist_ps_count == ps_count){
				alert('无可用处置新建');
				return false;
			}
			// search which is the right key in not_exist_arr
			var not_exist_obj = {key : 'PS09'};
			$.each(not_exist_arr, function(idx, not_exist_obj_tmp){
				var not_exist_obj_idx = parseInt(not_exist_obj.key.substring(3, not_exist_obj.key.length));
				var not_exist_obj_tmp_idx = parseInt(not_exist_obj_tmp.key.substring(3, not_exist_obj_tmp.key.length));
				if(not_exist_obj_idx >= not_exist_obj_tmp_idx){
					not_exist_obj = not_exist_obj_tmp;
				}
			});
			var right_key = not_exist_obj.key;
			var idx = parseInt(right_key.substring(3, right_key.length));
			var last_ps_end_score = _getMaxEndScore();
			insertNewPs({start_score : last_ps_end_score, end_score : '', ps_value_code : right_key}, --idx);
		});
		$('#process_ps_value').on('click', '#ps_value_del_btn', function(){
			var $checked_ps = $('#ps_value_tb tr').has('input:checked');
			if($checked_ps.length < 1){
				alert("请选择一条记录操作");
				return false;
			}
			$checked_ps.remove();
		});
		/*
		 * 1. check ps value vaild
		 * 2. parse value
		 * 4. return value to process page
		 */
		$('#process_ps_value').on('click', '#ps_value_sav_btn', function(){
			var ps_and_value_obj_arr = [];
			// 收集所有处置状态和处置值
			/**
			0: {
				ps_key: "PS01"
				ps_value_e: ""
				ps_value_s: "100"
			}
			1: {
				ps_key: "PS02"
				ps_value_e: "20"
				ps_value_s: "0"
			}
			*/
			$('#ps_value_tb [name=PS_POV]').each(function(idx, v_obj){
				var ps_and_value_obj = {};
				ps_and_value_obj.ps_key = $(this).val();
				ps_and_value_obj.ps_value_s = $('#ps_value_tb [name=start_score]').eq(idx).val();
				ps_and_value_obj.ps_value_e = $('#ps_value_tb [name=end_score]').eq(idx).val();
				ps_and_value_obj_arr.push(ps_and_value_obj);
			});
			
			// 按key排序
			ps_and_value_obj_arr.sort(function(a,b) {
				return a.ps_key > b.ps_key;
			});
			
			// 遍历收集到的数据,前闭后闭区间,做校验
			// 1.处置key不重复
			// 2.[0,x]开始,[y,100]结束
			// 3.[ps=01] of end_value == [ps=02] of start_value + 1
			var key_arr = [], same = false, not_null = false,
			start_val_null = false, // has 0
			end_val_null = false, // has 100
			error_type = "", last_obj = {},
			compare_good = false, disp_val = '', ps_key_mix = '';
			$.each(ps_and_value_obj_arr, function(idx, ps_and_value_obj){
				var ps_key = ps_and_value_obj.ps_key;
				var ps_value_s = ps_and_value_obj.ps_value_s;
				var ps_value_e = ps_and_value_obj.ps_value_e; 
				
				if(jQuery.inArray(ps_key, key_arr) > -1){
					same = true;
					return false;
				}
				key_arr.push(ps_key);
				
				if(ps_value_s == null || ps_value_s == '' || ps_value_e == null || ps_value_e == ''){
					not_null = true;
					return false;
				}
				if (idx == 0 && ps_value_s != "0"){
					start_val_null = true;
					return false;
				}
				if (idx == ps_and_value_obj_arr.length - 1 && ps_value_e != "100"){
					end_val_null = true;
					return false;
				}
				if (ps_value_e == "100") {
					if(idx == 0){
						ps_key_mix += ps_value_s;
					    ps_key_mix += ",";
					    ps_key_mix += ps_key;
					} else if (idx == ps_and_value_obj_arr.length - 1){ // last idx
						ps_key_mix += ",";
						ps_key_mix += ps_key;
					} else {
						ps_key_mix += ",";
						ps_key_mix += ps_key;
						ps_key_mix += '|';
						ps_key_mix += ps_value_e;
					}
				} else {
					if(idx == 0){
						ps_key_mix += ps_value_s;
						ps_key_mix += ",";
						ps_key_mix += ps_key;
						ps_key_mix += '|';
						ps_key_mix += ps_value_e;
					} else if (idx == ps_and_value_obj_arr.length - 1){ // last idx
						ps_key_mix += ",";
						ps_key_mix += ps_key;
					} else {
						ps_key_mix += ",";
						ps_key_mix += ps_key;
						ps_key_mix += '|';
						ps_key_mix += ps_value_e;
					}
				}
				disp_val+= getPsCodeValue(ps_key);
				disp_val+= '[';
				disp_val+= ps_value_s;
				disp_val+= '-';
				disp_val+= ps_value_e;
				disp_val+= ')';
				idx == $('#process_ps_value tr').has('select').length-1 ? '' :disp_val+= ',';
				if(idx == 0){
					last_obj = ps_and_value_obj;
					return true;
				}
				compare_good = compareWithLast(ps_and_value_obj, last_obj);
				if(compare_good){
					return false;
				}
				last_obj = ps_and_value_obj;
			});
			function compareWithLast(current, last){
				var flag = false;
				var reg = /^\d+$/;
				if(!reg.test(current.ps_value_s) ||
						!reg.test(current.ps_value_e) ||
						!reg.test(last.ps_value_s) ||
						!reg.test(last.ps_value_e)){
					alert("处置值必须为非负整数");
					return true;
				}
				var currentSS = parseInt(current.ps_value_s),
				currentES = parseInt(current.ps_value_e),
				lastSS = parseInt(last.ps_value_s),
				lastES = parseInt(last.ps_value_e);
				if(lastES != currentSS ||
					lastSS > 100 || lastSS < 0 ||
					lastES > 100 || lastES < 0 ||
					currentSS > 100 || currentSS < 0 ||
					currentES > 100 || currentES < 0 ||
					lastSS >= lastES ||
					currentSS >= currentES){
					alert('处置值值区间错误，必须为连续递增的区间值');
					return true;
				}
				return flag;
			}
			if(compare_good){
				return false;
			}
			if (same){
				alert('处置方式不能重复');
				return false;
			} 
			if(not_null){
				alert('处置值不能为空');
				return false;
			}
			if(start_val_null){
				alert('处置值必须以0开始');
				return false;
			}
			if(end_val_null){
				alert('处置值必须以100结束');
				return false;
			}
			jqDom.find('[name=PS_VALUE_DESC]').val(disp_val);
			jqDom.find('[name=PS_VALUE]').val(ps_key_mix);
			ps_value_dialog.hide();
		});
		/*
		 * select all
		 */
		$('#process_ps_value').on('click', '#ps_value_checkbox_checkall', function(){
			$('#process_ps_value [name=ps_value_checkbox]').prop('checked', $(this).prop("checked"));
		});
		
		/**
		 * 取最大的结束值
		 */
		function _getMaxEndScore() {
			var ps_end_score = $('#ps_value_tb > tr').find('input[name=end_score]'),
			max_end_score = 0;
			$.each(ps_end_score, function(){
				var end_score = $(this).val();
				if(!isNaN(end_score) && end_score*1 > 0  && end_score < 100){
					if(isNaN(max_end_score) || 
							(!isNaN(max_end_score) && end_score*1 > max_end_score)){
						max_end_score = end_score*1;
					}
				}
			});
			return max_end_score > 0 ? max_end_score : '';
		}
	}
	function parsePsValueToDesc(list){
		var disp_val = '';
		var length = list.length;
		$.each(list, function(idx, ps_value_obj){
			disp_val+= getPsCodeValue(ps_value_obj.ps_value_code);
			disp_val+= '[';
			disp_val+= ps_value_obj.start_score;
			disp_val+= '-';
			disp_val+= ps_value_obj.end_score;
			disp_val+= ')';
			idx == length - 1 ? '' :disp_val+= ',';
		});
		return disp_val;
	}
	function getPsCodeValue(ps_code){
		var codes = jcl.code.getCodes("tms.rule.disposal");
		for(var i in codes){
			if(ps_code == i){
				return codes[i];
			}
		}
	}
	function parsePsValue(value){
		// 0,PS01|30,PS02|50,PS04|80,PS05
		var ps_value_arr = [];
		var score_arr = [];
		var ps_code_id_arr = [];
		var val_arr = [];
		if (value.indexOf('|') > -1 || value.indexOf(',') > -1) {
			val_arr = value.split('|');
		} else{
			return null;
		};
		//var val_arr = value.split('|');
		$.each(val_arr, function(idx, arr){
			var firstScore = arr.split(',')[0];
			var ps_code_id = arr.split(',')[1];
			score_arr[idx * 2] = firstScore;
			score_arr[idx * 2 + 1] = firstScore;
			ps_code_id_arr[idx] = ps_code_id;
		});
		score_arr.splice(0, 1);
		score_arr[score_arr.length] = '100';
		$.each(ps_code_id_arr, function(idx, ps_code_id){
			var ps_value_obj = {};
			ps_value_obj['ps_value_code'] = ps_code_id;
			ps_value_obj['start_score'] = parseInt(score_arr[idx * 2]);//(parseInt(score_arr[idx * 2]) + (idx == 0 ? 0 : 1));
			ps_value_obj['end_score'] = score_arr[idx * 2 + 1];
			ps_value_arr.push(ps_value_obj);
		});
		return ps_value_arr;
	}
	function ps_format_data(data){
		var order = data.PS_ORDER;
		data.PS_ORDER = /^\d+$/.test(order) ? parseInt(order*1) : order;
		return data;
	}
	function ps_check_data(data){
		var allow = false;
		var order = data.PS_ORDER + '';
		var cond = data.PS_COND;
		var desc = data.PS_NAME;
		var ps_value = data.PS_VALUE;
		var reg = /^\d+$/;
		if(desc == undefined || Trim(desc)==""){
			alert("处置名称不允许为空");
			return allow;
		} else {
			if(!checkeLength(desc,50)){
				alert('处置名称不能超过50个字符');
				return allow;
			}
			if(!checkSpecialCharacter(desc, "2")){
				alert("处置名称只能包含汉字,字母,数字和下划线");
				return allow;
			}
		}
		if(ps_value == undefined || Trim(ps_value)==""){
			alert("处置值不允许为空");
			return allow;
		} 
		if(order == undefined || Trim(order)==""){
			alert("处置执行顺序不允许为空");
			return allow;
		} else {
			if(!checkeLength(order, 8)){
				alert('处置执行顺序不能超过8个字符');
				return allow;
			}
			if(!reg.test(order)){
				alert("处置执行顺序必须为非负整数");
				return allow;
			}
		}
		if(cond == '' || cond == null){
			//alert("处置条件不允许为空");
			//return allow;
		} else {
			/*if(!checkeSpecialCode(cond)){
				alert('处置条件不能包含特殊字符');
				return allow;
			}*/
			if(!checkeLength(cond, 200)){
				alert('处置条件不能超过200个字符');
				return allow;
			}
		}
		return true;
	}
})();