//UTF-8 encoding
var txnid = jQuery.url.param("txnid");
var strategyid = jQuery.url.param("id");
var ruleList = "";// 规则下拉列表
var disposalList = ""// 处置方式下拉列表
var disposalcount= 0;// 条件计数器
var funccount = 1;// 函数个数计数器，从1开始因为默认会给出一行
var secondfun = new Array();
var funcList = '';// 函数下拉列表

	$(document).ready(function(){

		// 风险处置类型下拉
		jcl.code.selector('RISKDISPOSALTYPE','tms.strategy.disposaltype',{text:'--请选择--',value:''});
		
		// 处置方式下拉
		jcl.code.selector('DISPOSAL','tms.rule.disposal',{text:'--请选择--',value:''});

		
		// 动作下拉列表
		jcl.postJSON('/tms/rule/action/getActionList', "", function(data){
			funcList = data.row;
			$.each(data.row, function(i,row){
				$("select[name=funcid0]").append("<option value='" + row['FUNCID'] + "'>" + row['FUNCNAME'] + "</option>");  
			}); 
		});
		

		// 风险处置选择事件
		$('#RISKDISPOSALTYPE').change(function(){
			var riskdisposaltype = $('#RISKDISPOSALTYPE').val();
			var riskli = $('#RISKDISPOSALTYPE').parent().parent().parent();		
			disposalcount = 0;
			// 规则
			if(riskdisposaltype == '01'){
				// 初始化表头
				var actioncondstr = '<li class="list-box-item">';
				actioncondstr += '<label class="list-box-item-label">条件：</label> ';
				actioncondstr += '<span class="list-box-item-content">';
				actioncondstr+= "<table border='1' id='typetable' style='border-collapse: collapse;width:100%'>";
				actioncondstr+="<tr style='font-weight: bold;'>";
				actioncondstr+="<td width='50%' id='RULETITLE'>规则名称</td>";
				actioncondstr+="<td width='30%' id='CONDVALTITLE'>执行结果</td>";
				actioncondstr+="<td width='20%'>操作</td>";
				actioncondstr+="</tr>";
				actioncondstr+="</table>";
				actioncondstr+="</span>";
				actioncondstr+="</li>";
				$('#disposalconddivid').html(actioncondstr);

				// 插入一行
				insertRuleRow();
			}

			// 处置
			if(riskdisposaltype == '02'){
				// 初始化表头
				var actioncondstr = '<li class="list-box-item">';
				actioncondstr += '<label class="list-box-item-label">条件：</label> ';
				actioncondstr += '<span class="list-box-item-content">';
				actioncondstr+= "<table border='1' id='typetable' style='border-collapse: collapse;width:100%'>";
				actioncondstr+="<tr style='font-weight: bold;'>";
				actioncondstr+="<td width='50%'id='RULETITLE'>处置方式</td>";
				actioncondstr+="<td width='30%'id='CONDVALTITLE'>个数</td>";
				actioncondstr+="<td width='20%'>操作</td>";
				actioncondstr+="</tr>";
				actioncondstr+="</table>";
				actioncondstr+="</span>";
				actioncondstr+="</li>";
				$('#disposalconddivid').html(actioncondstr);

				// 插入一行
				insertDisposalRow();
			}

			// 分值
			if(riskdisposaltype == '03'){
				// 初始化表头
				var actioncondstr = '<li class="list-box-item">';
				actioncondstr += '<label class="list-box-item-label">条件：</label> ';
				actioncondstr += '<span class="list-box-item-content">';
				actioncondstr+= "<table border='1' id='typetable' style='border-collapse: collapse;width:100%'>";
				actioncondstr+="<tr style='font-weight: bold;'>";
				//actioncondstr+="<td width='20%'>风险分值</td>";
				actioncondstr+="<td width='50%'id='FUNCTITLE'>函数</td>";
				actioncondstr+="<td width='30%'id='CONDVALTITLE'>数值</td>";
				actioncondstr+="<td width='20%'>操作</td>";
				actioncondstr+="</tr>";
				actioncondstr+="</table>";
				actioncondstr+="</span>";
				actioncondstr+="</li>";
				$('#disposalconddivid').html(actioncondstr);

				// 插入一行
				insertScoreRow();
			}

		});
	});


	
	// 增加动作一小行
	function insertRuleRow(){
		var actioncondstr="<tr><input type='hidden' name='FUNC"+disposalcount+"' id='FUNC"+disposalcount+"' value='eq'/>";
		actioncondstr+="<td width='50%'><select name='RULEID"+disposalcount+"' id='RULEID"+disposalcount+"'>";
		actioncondstr+="<option value=''>--请选择--</option>";
		for(var k=0;k<ruleList.length;k++){
			actioncondstr+="<option value='"+ruleList[k]['RULEID']+"'>"+ruleList[k]['RULENAME']+"</option>";
		}
		actioncondstr+="</select></td>";
		actioncondstr+="<td width='30%'><select name='CONDVAL"+disposalcount+"' id='CONDVAL"+disposalcount+"'>";
		actioncondstr+="<option value=''>--请选择--</option>";
		actioncondstr+="<option value='true'>true</option>";
		actioncondstr+="<option value='false'>false</option>";
		actioncondstr+="</select></td>";
		actioncondstr+="<td width='20%'><input type='button' value='add' class='btn' id='insertAtom"+disposalcount+"'/>";
		actioncondstr+="<input type='button' value='del' class='btn'onclick='dellAtom(this);'/></td>";
		actioncondstr+="</tr>"
		$('#typetable').append(actioncondstr);
		
		$("#insertAtom"+disposalcount).click(function(){insertRuleRow();});// 定义插入行add按钮事件

		disposalcount ++;
		$('#DISPOSALCOUNT').val(disposalcount);
	}

	// 给新增加的这一行赋值
	function setRowValue(thisRowIndex,item){
		$('#FUNC'+thisRowIndex).val(item.FUNC);
		$('#CONDVAL'+thisRowIndex).val(item.CONDVAL);
		$('#RULEID'+thisRowIndex).val(item.COND);
	}

	// 增加处置一小行
	function insertDisposalRow(){
		var actioncondstr="<tr><input type='hidden' name='FUNC"+disposalcount+"' id='FUNC"+disposalcount+"' value='eq'/>";
		actioncondstr+="<td width='50%'><select name='RULEID"+disposalcount+"' id='RULEID"+disposalcount+"'>";
		actioncondstr+="<option value=''>--请选择--</option>";
		for(var k=0;k<disposalList.length;k++){
			actioncondstr+="<option value='"+disposalList[k]['CODE_KEY']+"'>"+disposalList[k]['CODE_VALUE']+"</option>";
		}
		actioncondstr+="</select></td>";
		actioncondstr+="<td width='30%'><select name='CONDVAL"+disposalcount+"' id='CONDVAL"+disposalcount+"'>";
		actioncondstr+="<option value=''>--请选择--</option>";
		actioncondstr+="</select></td>";
		actioncondstr+="<td width='20%'><input type='button' value='add' class='btn' id='insertDisposalAtom"+disposalcount+"'/>";
		actioncondstr+="<input type='button' value='del' class='btn' onclick='dellAtom(this);'/></td>";
		actioncondstr+="</tr>";
		$('#typetable').append(actioncondstr);
		
		// 定义插入行add按钮事件
		$("#insertDisposalAtom"+disposalcount).click(function(){insertDisposalRow();});

		// 定义条件的处置方式的选择事件
		$('#RULEID'+disposalcount).change(function(){
			var disval = $(this).val();
			var id = $(this).attr('id');
			var rindex = id.substr(6);
			// 计算此处置方式的规则个数
			var valsum = 0;
			for(var i = 0; i< ruleList.length; i++){
				if(ruleList[i]['DISPOSAL'] == disval){
					valsum ++;
				}
			}

			// 初始化处置方式个数下拉列表
			$('#CONDVAL'+rindex).html("<option value=''>--请选择--</option>");// 先清空
			for (var j = 0; j <= valsum ; j++)
			{
				var hm = "<option value='"+j+"'>"+j+"个</option>";
				$('#CONDVAL'+rindex).append(hm);
			}
		});

		disposalcount ++;
		$('#DISPOSALCOUNT').val(disposalcount);

	}

	// 增加分值一小行
	function insertScoreRow(){
		var actioncondstr="<tr><input type='hidden' name='RULEID"+disposalcount+"' id='RULEID"+disposalcount+"' value='SCORE'/>";
		//actioncondstr+="<td width='20%'>风险分值</td>";
		actioncondstr+="<td><select name='FUNC"+disposalcount+"' id='FUNC"+disposalcount+"'>";
		actioncondstr+="<option value=''>--请选择--</option>";
		actioncondstr+="<option value='great'>大于</option>";
		actioncondstr+="<option value='less'>小于</option>";
		actioncondstr+="<option value='eq'>等于</option>";
		actioncondstr+="</select></td>";
		actioncondstr+="<td>";
		actioncondstr+="<input type='text' size='3' maxlength='3' name='CONDVAL"+disposalcount+"' id='CONDVAL"+disposalcount+"' class='itext'/>";
		actioncondstr+="</td>";
		actioncondstr+="<td><input type='button' value='add' class='btn' id='insertScoreAtom"+disposalcount+"'/>";
		actioncondstr+="<input type='button' value='del' class='btn'  onclick='dellAtom(this);'/></td>";
		actioncondstr+="</tr>";

		$('#typetable').append(actioncondstr);
		
		$("#insertScoreAtom"+disposalcount).click(function(){insertScoreRow();});// 定义插入行add按钮事件

		disposalcount ++;
		$('#DISPOSALCOUNT').val(disposalcount);
	}



	//JSON字符串到Object
	function _json2Obj(_json) {
		if (_json == '') {
			//alert("函数初始化失败！");
			return "";
		}
		return eval("({root:"+_json+"})");
	}


	// JSON字符串转字符串
	function _json2str(o) {
	  var arr = [];
	  var fmt = function(s) {       
		   if (typeof s == 'object' && s != null) return _json2str(s);
		  return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s;
	  }
	  for (var i in o) arr.push("'" + i + "':" + fmt(o[i]));
	  return '{' + arr.join(',') + '}';
	}

	// 删除动作一小行
	function dellAtom(obj){
		var rows = $(obj).parent().parent().parent().children('tr').length;
		// 需要保留一行
		if(rows > 2){
			$(obj).parent().parent().remove();
		}else{
			 return;
		}
	}

	// 增加一函数
	function insertFuncRow(obj){
		var actioncondstr="<tr>";
		actioncondstr+="<td>";
		actioncondstr+="<select name='funcid"+funccount+"' id = 'funcid"+funccount+"' onchange='funcChange("+funccount+");'>";
		actioncondstr+="<option value=''>--请选择--</option>";

		for(var i = 0; i<funcList.length; i++){
			actioncondstr+="<option value='"+funcList[i]['FUNCID']+"'>"+funcList[i]['FUNCNAME']+"</option>";
		}
		actioncondstr+="</select>";
		actioncondstr+="</td>";
		actioncondstr+="<td><div id='param_"+funccount+"' style='display: inline'></div></td>";
		actioncondstr+="<td align='left'>";
		actioncondstr+="<input type='button' value='add' class='btn' onclick='insertFuncRow(this);'/>";
		actioncondstr+="<input type='button' value='del' class='btn' onclick='dellAtom(this);'/>";
		actioncondstr+="</td><input name='ACTIONJSON"+funccount+"' id='ACTIONJSON"+funccount+"' type='hidden'/>";
		actioncondstr+="</tr>";
		
		$(obj).parent().parent().parent().append(actioncondstr);

		funccount ++;
		$('#FUNCCOUNT').val(funccount);
	}

	// 函数下拉列表选择事件
	function funcChange(funcindex){
		var funcid=$('#funcid'+funcindex).val();
		if(funcid.length > 0){
			
			var temp = new jcl_tms.util.FuncInvoke({ 
					id:'funcid'+funcindex,
					txnid:txnid
				});
			
			jcl.postJSON('/tms/rule/action/getfuncParamList', "funcid="+funcid, function(data){
				temp.setJson(data.row);
				temp.show('param_'+funcindex, "add");
				$('#param_'+funcindex).show();
			});
			secondfun[funcindex] = new Array(temp);	
		}else{
			$('#param_'+funcindex).hide();
		}
			
	}

	function checks(){
		var disposalname = $('#RISKDISPOSALNAME').val();
		// 校验
		if(Trim(disposalname) == ''){
			alert('风险策略名称不能为空');
			$('#RISKDISPOSALNAME').focus();
			return false;
		}else{
			
			if(!checkeLength(disposalname,64)){
				alert('风险策略名称不能超过64个字符');
				$('#RISKDISPOSALNAME').focus();
				return false;
			}
			if(!checkeSpecialCode(disposalname)){
				alert('风险策略名称不能包含特殊字符');
				$('#RISKDISPOSALNAME').focus();
				return false;
			}
			
		}
		if($('#RISKDISPOSALTYPE').val() == ''){
			alert('风险策略类型不能为空');
			$('#RISKDISPOSALTYPE').focus();
			return false;
		}

		for(var i = 0; i < disposalcount; i++){
			var ruleid = $('#RULEID'+i).val();
			var func = $('#FUNC'+i).val();
			var condval = $('#CONDVAL'+i).val();

			if(ruleid == '' || ruleid == 'undefined'){
				alert($('#RULETITLE').text()+'不能为空');
				$('#RULEID'+i).focus();
				return false;
			}
			if(func == '' || func == 'undefined'){
				alert($('#FUNCTITLE').text()+'不能为空');
				$('#FUNC'+i).focus();
				return false;
			}
			if(condval == '' || condval == 'undefined'){
				alert($('#CONDVALTITLE').text()+'不能为空');
				$('#CONDVAL'+i).focus();
				return false;
			}else if(ruleid == 'SCORE'){
				//condval只能为非负整数
				if(!isNumber(condval)){
					alert('数值只能是非负整数');
					$('#CONDVAL'+i).focus();
					return false;
				}
			}
		}
		
		var isExistsFunc = false;
		for(var j = 0; j<funccount; j++ ){
			var funcid = $('#funcid'+j).val();
			if(funcid == null ||funcid == '' || funcid == 'undefined'){
				continue;
			}
			
			var msg  = secondfun[j][0].save('param_'+j);
			if(msg != ''){
				alert('动作['+(j+1)+']条件域表达式值有误：'+msg);
				return;
			}
			var tconds = secondfun[j][0].getJson();   
			$('#ACTIONJSON'+j).val(tconds);
			isExistsFunc = true;
		}

		if($('#DISPOSAL').val() == '' && !isExistsFunc){
			alert('处置方式和动作函数不能同时为空');
			return false;
		}
		return true;
	}

// 大于等于0
function isNumber( s ){   

	var regu = "^[0-9]+$"; 

	var re = new RegExp(regu); 

	if (s.search(re) != -1) { 

		return true; 

	} else { 

		return false; 

	} 

}