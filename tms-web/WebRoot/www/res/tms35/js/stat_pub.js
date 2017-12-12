(function($) {
funcParams = function(){
	var _param_type;
	var _item;
	var _form;
	
	var func_params_dialog = new jcl.ui.Dialog({
		id: 'func_params_dialog',
		draggable: true,
		title: '参数配置'
	});
	
	func_params_dialog.jqDom.find('.box-content').addClass("funcparam-dialog");// 设置DIALOG自动滚动条
	func_params_dialog.jqDom.find('.box-content').removeClass("box-content");// 设置DIALOG自动滚动条
	
	var htmlStr = "";
	htmlStr += "<table border='0' width='90%' id='fun_param_tab'>";
	htmlStr += "<tbody><tr>";
	htmlStr += "<td colspan='3' align='left'>";
	htmlStr += "<input type='button' value='增加' class='btn' id='param_add_btn' />";
	htmlStr += "<input type='button' value='删除' class='btn' id='param_del_btn'  />"
	htmlStr += "<input type='button' value='确定' class='btn' id='param_sav_btn'  /></td>";
	htmlStr += "</tr>";
	htmlStr += "<tr style='font-weight: bold;FONT-SIZE: 11px'>";
	htmlStr += "<td width='5%'><input type=checkbox name=funparam_all /></td>";
	htmlStr += "<td width='45%'>开始值</td>";
	htmlStr += "<td width='45%'>结束值</td>";
	//htmlStr += "<td width='5%'></td>";
	htmlStr += "</tr></tbody>";
	htmlStr += "<tbody id='pa_value_tb'></tbody>";
	htmlStr += "</table>";
	func_params_dialog.addDom($(htmlStr));
		
	// 注册增加按钮	
	$("#fun_param_tab").find('#param_add_btn').click(function(){
		insertCond(null,_param_type);
	});
		
	// 注册删除按钮
	$("#fun_param_tab").find('#param_del_btn').click(function(){
		dellCond();
	});
	// 注册确定按钮
	$("#fun_param_tab").find('#param_sav_btn').click(function(){
		saveCond();
	});
	
	$('#fun_param_tab').on('click','[name=funparam_all]',function(){
		$('#fun_param_tab [name=funparam_one]').prop('checked', $(this).prop("checked"));
	});
	
	this.initParamList = function(fn_param,param_type,form){
		// 先清一下列表
		$('#fun_param_tab tr:gt(1)').remove();
		
		_param_type = param_type;// 参数类型
		_form = form;// 需要更新的form
		
		if (fn_param == '') {
			insertCond(null,param_type);// 插入一行
		}else {
			var ps = fn_param.split('|');
			$(ps).each(function(index){
				var param = ps[index].split(',');
				insertCond(param,param_type);// 插入一行
			});
		}
		
		func_params_dialog.show();
		$('#func_params_dialog').unmiddle();
	}
	
	function insertCond(param_val, param_type){
		if (param_type != undefined && (param_type == 'datetime' || param_type == 'time')) {
			insertTimeCond(param_val);
		}else{
			insertTextCond(param_val);
		}
	}
	
	// 增加一行数值类型函数参数
	function insertTextCond(param){
		var tab = $('#pa_value_tb');
		
		var td0 = "<input type='checkbox' name='funparam_one'/>"
		var td1 = "<input type='text' name='param_start'";
		var td2 = "<input type='text' name='param_end'";
		
		if(param != null && param != 'null' && param != ""){
			td1 += " value='"+param[0]+"'";
			var endval = param.length > 1 ? param[1]:'';
			td2 += " value='"+endval+"'";
		}
		td1 += " />";
		td2 += " />";
		
		$(tab).append("<tr><td>"+td0+"</td><td>"+ td1 + "</td><td>" + td2 +"</td></tr>");
	}

	// 增加一行时间类型函数参数
	function insertTimeCond(param){
		var tab = $('#pa_value_tb');
		
		if(param == undefined || param ==null || param == '') param = ['00:00:00','00:00:00']; 
		
		var start_val = param[0].split(':');
		var end_val = param[1].split(':');
		
		var start_val_h = start_val[0];// 开始值-时
		var start_val_s = start_val[1];// 开始值-分
		var start_val_m = start_val[2];// 开始值-秒
		var end_val_h = end_val[0];// 结束值-时
		var end_val_s = end_val[1];// 结束值-分
		var end_val_m = end_val[2];// 结束值-秒
		
		var td0 = "<input type='checkbox' name='funparam_one'/>"
		var td1 = "<select name='param_start_h'>";
		for(var i = 0; i<24;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td1 += "<option value='"+t+"' "+(start_val_h==t?' selected ':'')+">"+t+"</option>";
		}
		td1 += "</select>"
		td1 += "时<select  name='param_start_s' >";
		for(var i = 0; i<60;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td1 += "<option value='"+t+"' "+(start_val_s==t?' selected ':'')+">"+t+"</option>";
		}
		td1 += "</select>"
		td1 += "分<select name='param_start_m' >";
		for(var i = 0; i<60;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td1 += "<option value='"+t+"' "+(start_val_m==t?' selected ':'')+">"+t+"</option>";
		}
		td1 += "</select>秒"
		var td2 = "<select name='param_end_h' >";
		for(var i = 0; i<24;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td2 += "<option value='"+t+"' "+(end_val_h==t?' selected ':'')+">"+t+"</option>";
		}
		td2 += "</select>"
		td2 += "时<select name='param_end_s' >";
		for(var i = 0; i<60;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td2 += "<option value='"+t+"' "+(end_val_s==t?' selected ':'')+">"+t+"</option>";
		}
		td2 += "</select>"
		td2 += "分<select name='param_end_m' >";
		for(var i = 0; i<60;i++){
			var t = i;
			if (i < 10) t = "0"+i; 
			td2 += "<option value='"+t+"' "+(end_val_m==t?' selected ':'')+">"+t+"</option>";
		}
		td2 += "</select>秒"
			
		$(tab).append("<tr><td>"+td0+"</td><td>"+ td1 + "</td><td>" + td2 +"</td></tr>");
		
	}
	// 删除一行函数参数
	function dellCond(){
		var $checked_ps = $('#pa_value_tb tr').has('input:checked');
		if($checked_ps.length < 1){
			alert("请至少选择一条记录操作");
			return false;
		}
		$checked_ps.remove();
	}
	
	// 保存函数参数
	function saveCond(){
		var param_value = "";
		var tab = document.getElementById('pa_value_tb');
		var isPass = true;
		var pre_row_end_val = "";
		var time_betw = new Array();// 区间对象列表[{开始时间，结束时间，行号},{...}]
		$(tab.rows).each(function(index){
			var cells = $(this).children();
			
			var start_isNull = false;
			
			var start_value;
			var end_value;
			if(_param_type == 'datetime' || _param_type == 'time'){
				start_value = $(cells[1]).find('[name=param_start_h]').val()+':' + $(cells[1]).find('[name=param_start_s]').val()+':'+$(cells[1]).find('[name=param_start_m]').val();
				end_value = $(cells[2]).find('[name=param_end_h]').val()+':' + $(cells[2]).find('[name=param_end_s]').val()+':'+$(cells[2]).find('[name=param_end_m]').val();
			}else{
				var start_obj = $(cells[1]).find('[name=param_start]');
				start_value = start_obj.val();
				var end_obj = $(cells[2]).find('[name=param_end]');
				end_value = end_obj.val();
			}
			
			
			if(tab.rows.length ==1 && start_value.length == 0 && end_value.length == 0) {
				alert('第'+(parseInt(index)+1)+'行：开始值和结束值不能都为空');
				isPass = false;
				return false;
			}
				
			if(index != 0 && start_value.length == 0) {
				alert('第'+(parseInt(index)+1)+'行：开始值不能为空');
				isPass = false;
				return false;
			}
			
			if(index!=tab.rows.length-1 && end_value.length == 0){
				alert('第'+(parseInt(index)+1)+'行：结束值不能为空');
				isPass = false;
				return false;
			}
				
			if(!checkFuncParam(index,1,start_obj)){
				isPass = false;
				return false;
			}
			
			if(!checkFuncParam(index,2,end_obj)){
				isPass = false;
				return false;
			}
			
			// 当前区间的结束值大于开始值，下一行区间的开始值大于上一行的结束值
			if(_param_type == 'double'||_param_type == 'money'||_param_type == 'long'){
				if (start_value.length == 0) {
					start_value = 0;
					start_obj.val(start_value);
				}
				if(parseFloat(start_value) >=  parseFloat(end_value)){
					alert('第'+(parseInt(index)+1)+'行：结束值必须大于开始值');
					isPass = false;
					return false;
				}
				if(parseFloat(start_value) <  parseFloat(pre_row_end_val)){
					alert('第'+(parseInt(index)+1)+'行：开始值不能小于上一行的结束值');
					isPass = false;
					return false;
				}
			}
			if (_param_type == 'datetime' || _param_type == 'time') {
				
				// 校验时间大小
				if(start_value == end_value){
					alert('第'+(parseInt(index)+1)+'行：结束值不能等于开始值');
					isPass = false;
					return false;
				}else if(start_value > end_value){
					var i1 = compareBetween(time_betw,[start_value,'24:00:00']);
					var i2 = compareBetween(time_betw,['00:00:00',end_value]);
					if(time_betw.length > 0 && (i1>=0||i2>=0)){
						alert('第'+(parseInt(index)+1)+'行：与第'+ (parseInt(i1 < 0 ? i2 : i1) + 1)  +'行区间重叠');
						isPass = false;
						return false;
					}
					
					time_betw.push([start_value,'24' + start_value.substr(2,start_value.length,index)]);
					time_betw.push(['00' + end_value.substr(2,end_value.length),end_value,index]);
				}else{
					var i1 = compareBetween(time_betw,[start_value,end_value]);
					if(time_betw.length > 0 && i1 >= 0){
						alert('第'+(parseInt(index)+1)+'行：与第'+(parseInt(i1)+1)+'行区间重叠');
						isPass = false;
						return false;
					}
					time_betw.push([start_value,end_value,index]);
				} 
				
			}
				
			param_value += start_value;
			
			if(end_value.length > 0 ) param_value +="," + end_value
			
			if(index != tab.rows.length-1) param_value += '|';
			
			pre_row_end_val = end_value;
		});
		if(!isPass) return;
		_form.set({FN_PARAM:param_value}, 'FN_PARAM');
		func_params_dialog.hide();
	}
	
	function compareBetween(src,f){
		var isFind = -1;
		$.each(src,function(i,t){
			if((f[0] <= t[0] && f[1] <= t[0]) || (f[0] >= t[1] && f[1] >= t[1])) return true;
			isFind = t[2];
			return false;
		});
		return isFind;
	}
	
	// 函数参数的校验，都为整数
	function checkFuncParam(index,i,_this){
		var check_value = $(_this).val();
		switch(_param_type) {
			case 'datetime':{
				break;
			}
			case 'time':{
				break;
			}
			case 'string':{
				break;
			}
			case 'double':{
				if(check_value != '' && !IsNumber(check_value,'+',2)){
					if(i == 1){
						alert('第'+(parseInt(index)+1)+'行：开始值请输入非负小数（保留两位小数）或非负整数');
					}else{
						alert('第'+(parseInt(index)+1)+'行：结束值请输入非负小数（保留两位小数）或非负整数');
					}
					
					//$(_this).focus();
					return false;
				}
				if(!checkeLength(check_value,15)){
					if(i == 1){
						alert('第'+(parseInt(index)+1)+'行：开始值不能超过15个字符');
					}else{
						alert('第'+(parseInt(index)+1)+'行：结束值不能超过15个字符');
					}
					//$(_this).focus();
					return false;
				}
				break;
			}
			case 'money':{
				if(check_value != '' && !IsNumber(check_value,'+',2)){
					if(i == 1){
						alert('第'+(parseInt(index)+1)+'行：开始值请输入非负小数（保留两位小数）或非负整数');
					}else{
						alert('第'+(parseInt(index)+1)+'行：结束值请输入非负小数（保留两位小数）或非负整数');
					}
					
					//$(_this).focus();
					return false;
				}
				if(!checkeLength(check_value,15)){
					if(i == 1){
						alert('第'+(parseInt(index)+1)+'行：开始值不能超过15个字符');
					}else{
						alert('第'+(parseInt(index)+1)+'行：结束值不能超过15个字符');
					}
					//$(_this).focus();
					return false;
				}
				break;
			}
			case 'long':{
				if(check_value != '' && !IsNumber(check_value,'+','0')){
					if(i == 1){
						alert('第'+(parseInt(index)+1)+'行：开始值请输入非负整数');
					}else{
						alert('第'+(parseInt(index)+1)+'行：结束值请输入非负整数');
					}
					
					//$(_this).focus();
					return false;
				}
				if(!checkeLength(check_value,15)){
					if(i == 1){
						alert('第'+index+'行：开始值不能超过15个字符');
					}else{
						alert('第'+index+'行：结束值不能超过15个字符');
					}
					//$(_this).focus();
					return false;
				}
				break;
			}
			defaut:{
				break;
			} 
			
		} 
		return true;
	}
}
})(jQuery);
(function($) {

ref_tree = function(){

	var ref_tree_dialog = new jcl.ui.Dialog({
		id: 'statboxid',
		draggable: true,
		title: '引用点查看'
	});
	
	ref_tree_dialog.jqDom.find('.box-content').addClass("funcparam-dialog");// 设置DIALOG自动滚动条
	ref_tree_dialog.jqDom.find('.box-content').removeClass("box-content");// 设置DIALOG自动滚动条
	
	var t = "<table id='ref-box'><tr><td valign='top' id='leftTd1'><div id='ref_tree-box'></div></td></tr></table>";
	var grantForm = $(t);
	ref_tree_dialog.addDom(grantForm);
	
	// type=0(规则)
	this.initTree = function(txnId, statName, type){
		$('#ref_tree-box').empty();
		// 查询左侧树并显示
		jcl.postJSON('/tms/stat/refTree', 'txnId=' + txnId + "&statName=" + statName +"&type="+type, function(data){
			var list = data.list;
			if (list.length == 0) {
				$('#ref_tree-box').html('没有被引用');
			}
			else {
				var icons = ['ticon-module', 'ticon-node', 'ticon-fn'];
				for (var i = 0; i < list.length; i++) {
					list[i]['icon'] = icons[list[i]['ftype']];
				}
				
				list.push({
					id: '-1',
					ftype: '-1',
					text: '引用点',
					icon: 'ticon-root',
					onum: 0
				});
	
				var ref_tree = new jcl.ui.Tree({
					sm: {
						type: 'row'
					}
				}, $('#ref_tree-box'));
				
				ref_tree.render(list);
			}
			ref_tree_dialog.show()
		});
	}
}
})(jQuery);