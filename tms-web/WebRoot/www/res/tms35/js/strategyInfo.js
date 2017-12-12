var model = window.model || {};
var txn_id = null; //存储交易树节点
// 处置策略
(function() {
	var grid = null;
	var eval_form_list = [];
	var dis;
	model.strategyInfo = {
		init: function(txnId, page) {
			txn_id = txnId;
			// 处置分值的DIALOG
			var ps_value_dialog;
			
			// 当前的处置策略表单
			var t_form;
			$('#strategy_list').empty();

			var evaltype_code_list = jcl.code.getCodes('tms.rule.evaltype');
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
					{id:"st-btn-view", icon:"icon-tb-find", text:"查看", action:'viewRow', enable:'oneRowSelected'},
					{id:"st-btn-add", icon:"icon-tb-add", text:"新建", action:'addRow'},
					{id:"st-btn-edit", icon:"icon-tb-edit", text:"编辑", enable:'oneRowSelected', action:'editRow'},
					{id:"st-btn-copy", icon:"icon-tb-edit", text:"复制", enable:'oneRowSelected'},
					{id:"st-btn-del", icon:"icon-tb-del", text:"删除", enable:'rowSelected'},
					{id:"st-btn-valid-y", icon:"icon-tb-edit", text:"启用", enable:'rowSelected'},
					{id:"st-btn-valid-n", icon:"icon-tb-edit", text:"停用", enable:'rowSelected'}
				],
				qform:{display:false,items:[
	              {name:'ST_NAME', label:'策略名称'},
	              {name:'EVAL_MODE', label:'评估方式', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.strategy.evalmode'}, selectedTopItem:true},
	              {name:'RULE_EXEC_MODE', label:'规则执行方式', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.strategy.rule_exec_mode'}, selectedTopItem:true},
	              {name:'ST_ENABLE', label:'有效性', type:'selector', title:'--请选择--', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.mgr.rulestatus'}, selectedTopItem:true}
       			 ], action:'localQuery'},
				table:{
					sortType: 'local',
					columns:[
						{name:"策略名称", width: 50, dataIndex:'ST_NAME'},
						{name:"评估方式", width: 20, dataIndex:'EVAL_MODE', render:'tms.strategy.evalmode'},
						{name:"规则执行方式", width: 20, dataIndex:'RULE_EXEC_MODE', render:'tms.strategy.rule_exec_mode'},
						{name:"规则数量", width: 20, dataIndex:'RULE_COUNT'},
						{name:"创建时间", width: 30, dataIndex:'CREATETIME',render:'datetime'},
						{name:"修改时间", width: 30, dataIndex:'MODIFYTIME',render:'datetime'},
						{name:"有效性", width: 15, dataIndex:'ST_ENABLE', render:'tms.mgr.rulestatus'}
					],
					rowForm:{
		                items:[
	                       {label:"策略名称", name: 'ST_NAME', type:'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"评估条件", name: 'EVAL_COND_IN', type: 'text', required:true, entireRow:true, cls:'text-diy'},
	                       {label:"表达式", name: 'EVAL_COND', type: 'hidden'},
	                       {label:"评估方式", name: 'EVAL_MODE', type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.strategy.evalmode', selectedTopItem:true}, required:true},
	                       {label:"规则执行方式", name: 'RULE_EXEC_MODE', type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.strategy.rule_exec_mode', selectedTopItem:true}, required:true},
	                       {label:"有效性", name: 'ST_ENABLE', value:'0', type:'radio', items:status_list, required:true}
	                   ],
	                   btns:[
	                       {text:'确定', id:'st-row-edit-sure', action:'submit'},
	                       {text:'取消', id:'st-row-edit-cancel', action:'cancel'}
	                   ]
	                },
					pagebar: false
				}
			}, $('#strategy_list'));
			
			
			// 处置分值确定按钮事件
			$('#process_ps_value').on('click', '#ps_value_sav_btn', function(){
				var ps_and_value_obj_arr = [];
				// 收集所有处置状态和处置值
				$('#ps_value_tb [name=PS_POV]').each(function(idx, v_obj){
					var ps_and_value_obj = {};
					ps_and_value_obj.ps_key = $(this).val();
					ps_and_value_obj.ps_value_s = $('#ps_value_tb [name=start_score]').eq(idx).val();
					ps_and_value_obj.ps_value_e = $('#ps_value_tb [name=end_score]').eq(idx).val();
					ps_and_value_obj_arr.push(ps_and_value_obj);
				});
				
				// 按key排序
				ps_and_value_obj_arr.sort(function(a,b) {
					return parseInt(b.ps_key.substring(2))-parseInt(a.ps_key.substring(2));
					//return a.ps_key < b.ps_key;
				});
				
				// 遍历收集到的数据,前闭后闭区间,做校验
				var key_arr = [], same = false, not_null = false,
				start_val_null = false, // has 0
				end_val_null = false, // has 100
				error_type = "", last_obj = {},
				compare_good = false, disp_val = '', ps_key_mix = '';
				$.each(ps_and_value_obj_arr, function(idx, ps_and_value_obj){
					var ps_key = ps_and_value_obj.ps_key;
					var ps_value_s = ps_and_value_obj.ps_value_s;
					var ps_value_e = ps_and_value_obj.ps_value_e; 
					
					// 非空
					if(ps_value_s == null || ps_value_s == '' || ps_value_e == null || ps_value_e == ''){
						not_null = true;
					}
					// 第一行区间开始值不能小于-100
					/*if (idx == 0 && ps_value_s != -100){
						start_val_null = true;
						return false;
					}
					// 最后一行区间结束值不能大于100					
					if (idx == ps_and_value_obj_arr.length - 1 && ps_value_e != "100"){
						end_val_null = true;
						return false;
					}*/
					
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
					disp_val+= '~';
					disp_val+= ps_value_e;
					disp_val+= ')';

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
					var reg = /^-?[0-9]\d*$/;
					if(!reg.test(current.ps_value_s) ||
							!reg.test(current.ps_value_e) ||
							!reg.test(last.ps_value_s) ||
							!reg.test(last.ps_value_e)){
						alert("处置值必须为整数");
						return true;
					}
					var currentSS = parseInt(current.ps_value_s),
					currentES = parseInt(current.ps_value_e),
					lastSS = parseInt(last.ps_value_s),
					lastES = parseInt(last.ps_value_e);
					if(lastES != currentSS ||
						lastSS > 100 || lastSS < -100 ||
						lastES > 100 || lastES < -100 ||
						currentSS > 100 || currentSS < -100 ||
						currentES > 100 || currentES < -100 ||
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
				if(not_null){
					alert('处置值不能为空');
					return false;
				}
				/*if(start_val_null){
					alert('处置值必须以-100开始');
					return false;
				}
				if(end_val_null){
					alert('处置值必须以100结束');
					return false;
				}*/
				t_form.find("input[name='PS_SCORE_DESC']").val(disp_val)
				t_form.find("input[name='PS_SCORE']").val(ps_key_mix)
				ps_value_dialog.hide();
			});
			
			// 表单打开事件
			grid.table.onRowFormShow(function(isAdd){
				eval_form_list = [];
				grid.table.rowForm.getItem('EVAL_COND_IN').component.attr("readonly","readonly");
				var row = null
				if(!isAdd){
					row = grid.table.selectedOneRow();
				}
				
				// 找到行编辑的表单
				var fo = grid.table.rowForm.jqDom.find('.form-items:first');
				
				// 删除已经有的li.form-item-box，多次点新建/编辑会增加多个
				fo.find('li.form-item-box').remove();
				
				// 根据数据字典“评估机制”生成BOX
				$.each(evaltype_code_list,function(i,code){
					//fo.append(evalStrategy(isAdd,row,i,code).jqDom);
					var dom = $('<li class="form-item form-item-box" style="width: 100%;"></li>').insertAfter(fo.children('li.form-item:last'));
					evalStrategy(dom,isAdd,row,i,code);
				});
			});
			
			// 策略条件
			var _ce = new condEdit();
			grid.table.rowForm.jqDom.on('dblclick', '[name=EVAL_COND_IN]', function(){
				_ce.init_cond(grid.table.rowForm.getItem('EVAL_COND').component,$(this),txn_id, ['RULE_FUNC','AC_FUNC'],'条件');
			});
			
			
			// 新增/修改的保存
			grid.table.onRowFormSubmit(function(rowFormJqDom, data, isAdd){
				if(check_stratedy_data(data)){

					var _r = data;
					if(!isAdd) {
						_r = grid.table.selectedOneRow();
						$.extend(_r,data);
					}
					data.ST_TXN = txn_id;
					_r.ST_TXN = txn_id;
					
					var eval = [];
					$.each(eval_form_list, function(idx, eval_form){
						eval.push(eval_form.data());
					});
					
					_r.evalSt = eval;

					var json_data = isAdd ? JSON.stringify({'add':[_r]}):JSON.stringify({'mod':[_r]});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;;
					
					jcl.postJSON('/tms35/strategy/save', url_para, function(data1){
						// 初始化查询列表
						jcl.postJSON("/tms35/strategy/list", "ST_TXN="+txn_id, function(data){
							grid.renderPage({list: data.row});
						});
						alert("保存成功");
					});
					return false;
				}else{
					return false;
				}
		    });
			
			// 删除按钮点击事件
			grid.toolbar.onClick('#st-btn-del', function(){
				if(confirm('确定删除？')){
					var json_data = JSON.stringify({'del':grid.selectedRows()});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/strategy/save', url_para, function(callback){
						grid.table.deleteSelectedRow();
						alert("删除成功");
					});
				}else{
					return false;
				}
			});
			
			// 启用按钮事件
			grid.toolbar.onClick('#st-btn-valid-y', function(){
				if(confirm('确定启用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {ST_ENABLE: '1'})
						);
					});

					var json_data = JSON.stringify({'valid-y':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/strategy/save', url_para, function(callback){
						grid.table.updateSelectedRow({ST_ENABLE: 1});
						alert("启用成功");
					});

					return;
				}else{
					return;
				}
			});

			// 停用按钮事件
			grid.toolbar.onClick('#st-btn-valid-n', function(){
				if(confirm('确定停用？')){
					var selectRows = grid.selectedRows();
					var list = [];
					$.each(selectRows, function(i, row){
						list.push(
							$.extend({}, row, {ST_ENABLE: '0'})
						);
					});

					var json_data = JSON.stringify({'valid-n':list});
					var json_data_encode = encodeURIComponent(json_data);

					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;

					jcl.postJSON('/tms35/strategy/save', url_para, function(callback){
						grid.table.updateSelectedRow({ST_ENABLE: 0});
						alert("停用成功");
					});

					return;
				}else{
					return;
				}
			});
			
			grid.table.onRowSelectChange(function(){
				resetEffectivenessBtn();
			});

			grid.table.onModelChange(function(){
				resetEffectivenessBtn();
			});

			// 查看按钮事件
			grid.toolbar.onClick('#st-btn-view', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").hide();
			});
			// 新增按钮事件
			grid.toolbar.onClick('#st-btn-add', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").show();
			});
			// 修改按钮事件
			grid.toolbar.onClick('#st-btn-edit', function(){
				grid.table.rowForm.jqDom.find("#st-row-edit-sure").show();
			});
			// 复制按钮事件
			grid.toolbar.onClick('#st-btn-copy', function(){
				if (confirm('确定复制？')) {
					var _r = grid.table.selectedOneRow();
					_r.ST_TXN = txn_id;
					var st_name = _r.ST_NAME + "_复制";
					if (!checkeLength(st_name, 128)) {
						alert('策略复制后策略名称超过128个字符，复制失败！');
						return false;
					}
					else {
						_r.ST_NAME = st_name;
					}
					
					var json_data = JSON.stringify({'copy': [_r]});
					var json_data_encode = encodeURIComponent(json_data);
					
					var url_para = "postData=" + json_data_encode + "&txnId=" + txn_id;
					
					jcl.postJSON('/tms35/strategy/save', url_para, function(data1){
						alert("复制成功");
						// 查询列表
						jcl.postJSON("/tms35/strategy/list", "ST_TXN=" + txn_id, function(data){
							grid.renderPage({list: data.row});
						});
					});
				}else{
					return;
				}
			});
			// 加载数据
			this.load(txn_id); 

			txn_id = txnId;
			
			function evalStrategy(container,isAdd,row,evalcode,evalvalue){
				var eval_list = [];
				if(row!=null){
					eval_list = row.EVALST;
				}
				
				// 评估机制BOX
				var box = new jcl.ui.Box({
					id: 'strate_box'+evalcode,
					title: evalvalue,
					width: 900,
					minWidth:500
				}, container);
				// 评估机制FORM
				var st_form = new jcl.ui.Form({
					id: 'st_form'+evalcode,
					items:[
					   {name:'EVAL_MECH', label:'评估机制',type:'selector', ds:{type:'code', category:'tms.statregy.eval_mech', selectedTopItem:true}, required:true},
					   {name:'DIS_STRATEGY', label:'中断策略',type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.statregy.dis_strategy', selectedTopItem:true}, required:true},
					   {name:'STAT_FUNC', label:'统计函数',type:'selector', items:[{text:'--请选择--',value:''}], ds:{type:'code', category:'tms.strategy.statfunc', selectedTopItem:true}, required:true},
					   {name:'PS_SCORE_DESC', label:"处置分值", type:'text', required:true, entireRow:true, cls:'text-diy'},
					   {name:'PS_SCORE', label:"处置分值", type:'hidden'},
					   {name:'SRE_ID', label:"处置分值", type:'hidden'},
					   {name:'ST_ID', label:"处置分值", type:'hidden'},
					   {name:'EVAL_TYPE', label:"评估类型", type:'hidden',value:evalcode}
					]
	    		}, box.container);
				
				//box.addDom(st_form.jqDom);
				
				ps_value_dialog = new jcl.ui.Dialog({
					"title" : '处置值-100到100,前闭后开', 
					"zindex" : '701', 
					marginTop: 0,
					draggable : true
				});
		
				st_form.getItem('PS_SCORE_DESC').component.attr("readonly","readonly");// 处置分值只读

				if(eval_list.length==0){
					st_form.getItem('EVAL_MECH').val('1');// 评估机制，默认”处置“
					st_form.disable('PS_SCORE_DESC');// 处置分值，默认隐藏
					st_form.disable('STAT_FUNC');// 统计函数，默认隐藏
				}else{
					var isFind = false;
					for(var i=0;i< eval_list.length;i++){
						var eval = eval_list[i];
						if(eval.EVAL_TYPE==evalcode){
							evalChange(st_form,eval.EVAL_MECH,eval);
							isFind = true;
						}
					}
					if(!isFind){
						evalChange(st_form,'1');
					}
				}
				
				if(isAdd==undefined){
					st_form.showViewMode();
				}
				
				eval_form_list.push(st_form);
				
				// 评估机制选择事件
				st_form.getItem('EVAL_MECH').component.onChange(function(v){
					evalChange(st_form,v.value);
				});
				
				// 处置分值双击事件
				st_form.getItem('PS_SCORE_DESC').component.dblclick(function(v){
					t_form = $(this).closest("form");
					var ps_value = st_form.jqDom.find('[name=PS_SCORE]').val() ;
					var ps_value_arr = parsePsValue(ps_value);
					
					$('#table_ps_value tbody[id=ps_value_tb] tr').remove();
					$.each(ps_value_arr, function (idx, ps_value_obj){
						insertNewPs(ps_value_obj, null);
					});
					ps_value_dialog.addDom('process_ps_value');
					ps_value_dialog.show();			
				});
				
				return box;
			}
			
			function evalChange(st_form1,eval_mech,eval){
				// 0分值
				if(eval_mech == '0'){
					st_form1.enable('STAT_FUNC');
					st_form1.enable('PS_SCORE_DESC');
					st_form1.disable('DIS_STRATEGY');
					
					var ps_value = eval?eval.PS_SCORE:'';// st_form.jqDom.find('[name=PS_SCORE]').val() ;
					// 处置分值给出默认值
					if(ps_value == ''){
						ps_value = psValueDefault();
					}
					
					if(ps_value == ''){
						alert("系统没有处置方式");
						return;
					}
					
					st_form1.getItem('PS_SCORE_DESC').val(parsePsValueToDesc(parsePsValue(ps_value)));
					st_form1.getItem('PS_SCORE').val(ps_value);
					st_form1.getItem('DIS_STRATEGY').val("");
					st_form1.getItem('EVAL_MECH').val('0');
					st_form1.getItem('STAT_FUNC').val(eval?eval.STAT_FUNC:'');
				}
				// 默认选中1处置
				if(eval_mech == '1'){
					st_form1.disable('STAT_FUNC');
					st_form1.disable('PS_SCORE_DESC');
					st_form1.enable('DIS_STRATEGY');
					st_form1.getItem('STAT_FUNC').val("");
					st_form1.getItem('PS_SCORE_DESC').val("");
					st_form1.getItem('PS_SCORE').val("");
					st_form1.getItem('EVAL_MECH').val('1');
					st_form1.getItem('DIS_STRATEGY').val(eval?eval.DIS_STRATEGY:'');
				}
				
				if(eval){
					st_form1.getItem('SRE_ID').val(eval.SRE_ID);
					st_form1.getItem('ST_ID').val(eval.ST_ID);
				}
			}
				
			function psValueDefault(){
				var ps_value = '';
				var codes = dis.row;
				for(var i in codes){
					if(ps_value.length == 0){
						ps_value = codes[i]['DEFAULT_SCORE'] +','+codes[i]['DP_CODE'];						
					}else{
						ps_value = codes[i]['DEFAULT_SCORE'] +','+codes[i]['DP_CODE']+ "|" + ps_value;
					}
				}
				return ps_value;
			}
			
			function parsePsValueToDesc(list){
				var disp_val = '';
				var length = list.length;
				$.each(list, function(idx, ps_value_obj){
					disp_val+= getPsCodeValue(ps_value_obj.ps_value_code);
					disp_val+= '[';
					disp_val+= ps_value_obj.start_score;
					disp_val+= '~';
					disp_val+= ps_value_obj.end_score;
					disp_val+= ')';
					idx == length - 1 ? '' :disp_val+= ',';
				});
				return disp_val;
			}
			
			function insertNewPs(ps_value_obj, idx){
				var $tr = $('<TR>');
				var $ps_select_td = $('<TD>', {width : '20%',align:'right'});
				var $start_score_td = $('<TD>', {width : '35%',align:'center'});
				var $zhi_td = $('<TD>', {width : '10%',align:'center'});
				var $end_score_td = $('<TD>', {width : '35%',align:'center'});
				var $ps_select = $('<input>', {"type":"hidden", "name":"PS_POV", "value":ps_value_obj.ps_value_code});
				var $start_score_text = $('<input>', {"type":"text", "name":"start_score", "class":"itext", "value":ps_value_obj.start_score});
				var $end_score_text = $('<input>', {"type":"text", "name":"end_score", "class":"itext", "value":ps_value_obj.end_score});
				
				$tr.append($ps_select_td.append($ps_select).append(getPsCodeValue(ps_value_obj.ps_value_code)+"： "));
				$tr.append($start_score_td.append($start_score_text));
				$tr.append($zhi_td.append(">=&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<"));
				$tr.append($end_score_td.append($end_score_text));
				
				$('#table_ps_value tbody[id=ps_value_tb]').append($tr);
			}
	
			function getPsCodeValue(ps_code){
				var codes = dis.row;;
				for(var i in codes){
					if(ps_code == codes[i]['DP_CODE']){
						return codes[i]['DP_NAME'];
					}
				}
			}

			function parsePsValue(value){
				
				// 0,PS01|30,PS02|50,PS04|80,PS05
				var ps_value_arr = [];

				if(value == null || value.length==0) return ps_value_arr;
				
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
			
			function resetEffectivenessBtn(){

				if(dualAudit){ // readonly mode
					grid.toolbar.disable($('#st-btn-add'), false);
					grid.toolbar.disable($('#st-btn-edit'), false);
					grid.toolbar.disable($('#st-btn-del'), false);
					grid.toolbar.disable($('#st-btn-copy'), false);
					grid.toolbar.disable($('#st-btn-valid-c'), false);
					grid.toolbar.disable($('#st-btn-valid-y'), false);
					grid.toolbar.disable($('#st-btn-valid-n'), false);
					grid.table.rowForm.jqDom.find("#st-row-edit-sure").hide();
					return;
				}

				grid.toolbar.enable('#st-btn-valid-y');
				grid.toolbar.enable('#st-btn-valid-n');

				if (grid.selectedRows().length == 0) {
					grid.toolbar.disable('#st-btn-valid-y'); // 启用按钮 disable
					grid.toolbar.disable('#st-btn-valid-n'); 
				} else {
					$.each(grid.selectedRows(), function(idx, row){
						if(row.ST_ENABLE == '1'){ // 有启用状态的记录
							grid.toolbar.disable('#st-btn-valid-y'); // 启用按钮 disable
						} else if(row.ST_ENABLE == '0'){
							grid.toolbar.disable('#st-btn-valid-n');
						}
					});
				}
			}
		},

		load: function(txnId) {
			txn_id = txnId;
			
			if(grid.qform.jqDom.is(":visible")){
				grid.qformDiv.slideUp('fast');// 查询表单隐藏
				grid.toolbar.toggle(0, false);// 查询按钮恢复选中
			}
			// 查询条件重置
			grid.qform.reset();
			
			if(dualAudit){ // readonly mode
				grid.toolbar.disable($('#st-btn-add'), false);
				grid.toolbar.disable($('#st-btn-edit'), false);
				grid.toolbar.disable($('#st-btn-del'), false);
				grid.toolbar.disable($('#st-btn-copy'), false);
				grid.toolbar.disable($('#st-btn-valid-c'), false);
				grid.toolbar.disable($('#st-btn-valid-y'), false);
				grid.toolbar.disable($('#st-btn-valid-n'), false);
			}
			
			jcl.postJSON('/tms35/rule/disposal', "txn_id="+txn_id, function(data1){
				dis = data1;
				// 初始化查询列表
				jcl.postJSON("/tms35/strategy/list", "ST_TXN="+txn_id, function(data){
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
		}
	};
	
	// 校验输入域有效性
	function check_stratedy_data(data){
		var st_name=data.ST_NAME;
		var eval_cond=data.EVAL_COND;
		var eval_mode=data.EVAL_MODE;
		var rule_exec_mode=data.RULE_EXEC_MODE;

		if (Trim(st_name) == "") {
			alert('策略名称不能为空');
			return false;
		}
		if (!checkeLength(st_name, 128)) {
			alert('策略名称不能超过128个字符');
			return false;
		}
		if (!checkSpecialCharacter(st_name, "2")) {
			alert('策略名称只能包含汉字,字母,数字和下划线');
			return false;
		}
		if(Trim(eval_cond) == "") {
			alert('评估条件不能为空');
			return false;
		}
		if (!checkeLength(eval_cond, 512)) {
			alert('评估条件不能超过512个字符');
			return false;
		}
		if(eval_mode == "") {
			alert('评估方式不能为空');
			return false;
		}
		if(rule_exec_mode == "") {
			alert('规则执行方式不能为空');
			return false;
		}
		var evaltype_code_list1 = jcl.code.getCodes('tms.rule.evaltype');
		var v_mess = "";
		$.each(eval_form_list, function(idx, eval_form){
			var form_data = eval_form.data();
			var eval_type=form_data.EVAL_TYPE;
			var eval_mech=form_data.EVAL_MECH;
			var dis_strategy=form_data.DIS_STRATEGY;
			var stat_func=form_data.STAT_FUNC;
			var ps_score=form_data.PS_SCORE;
			var eval_val = "";
			$.each(evaltype_code_list1,function(i,code){
				if(eval_type==i){
					eval_val = code;
					return false;
				}
			});
				
			// 处置
			if(eval_mech == 1){
				if(dis_strategy.length == 0){
					v_mess = eval_val+'的中断策略不能为空';
					return false;
				}
				 
			}
			// 分值
			if(eval_mech == 0){
				if(stat_func.length == 0){
					v_mess = eval_val+'的统计函数不能为空';
					return false;
				}
				if(ps_score.length == 0){
					v_mess = eval_val+'的处置分值不能为空';
					return false;
				}
			}
		});		
		if(v_mess.length > 0){
			alert(v_mess);
			return false;
		}
		/*jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acCond+"&TXNID="+txnId,function(data){
			jcl.postJSON("/tms/stat/checkCond","STAT_COND_VALUE="+acSrc+"&TXNID="+txnId,function(data){
			});
		});*/
		return true;
	}
})();