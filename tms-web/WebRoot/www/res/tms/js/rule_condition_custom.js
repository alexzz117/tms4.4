//UTF-8 encoding
// JSON字符串到Object
// 自定义条件页面初始化

var ftype = "";// 树的节点类型
var funInvoke1 = new jcl_tms.util.FuncInvoke({ 
					id:'funInvoke'
				});
var rulecustomid = jQuery.url.param("ruleId");
// 显示自定义条件的增加DIV
function customConditionView(blockCondDiv){
	

	$('#customcond').html('');// 把自定义DIV清空

	var t = '';
	//var contree = $('#customcond').html();
	//if(contree == ""){
		// 显示自定义条件右侧DIV
		addCondView(blockCondDiv);
		//var contree = $('#tree-box').html();
	
		// 如果树未初始化则执行下面代码
	
		// 查询左侧树并显示
		jcl.postJSON('/tms/rule/conditioncustom/conditionTree','',function(data){
			var list = data.list;
			var icons = ['moduleIcon', 'nodeIcon','fnIcon','subfnIcon'];
			for(var i = 0 ; i < list.length; i++){
				list[i]['icon'] = icons[list[i]['ftype']];
			} 
			list.push({id:'-1', ftype:'-1', text: '条件', icon:'rootIcon', onum: 0});
			t = new jcl.ui.Tree(list, {
			id: 'tree-box',
			sm:{
				type: 'radio',
				name: 'conId'
			}
			});
		});
		// 定义树的选择事件
		$('#tree-box :radio').live('click',function(){
			// 获取选中节点的ID
			var id = $(this).val();
			// 获取选中节点的类型
			ftype = t.map[id].ftype;
			
			// 叶子节点
			if(ftype == 1){
				// 刷新右侧窗口
				document.getElementById('right-box').style.display='block';
	
				// 初始化右侧节点的数据
				jcl.postJSON('/tms/rule/conditioncustom/edit','CONDID='+id,function(data){
					// 给出默认值				
					$('#score').val(data.row.SCORE);	
					$('#weight').val(data.row.WEIGHT);
					$('#condrange').val(data.row.CONDRANGE);	
					$('#countround').val(data.row.COUNTROUND);
					$('#condlimit').val(data.row.CONDLIMIT);	
					$('#countunit').val(data.row.COUNTUNIT);
					var featureisparam = data.row.FEATUREISPARAM;
					var featureList = data.featureList;
					var target = data.row.TARGET;
					//添加操作
					funInvoke1.setJson(data.row.JSONSTR);
					
					var jsonobj = eval("({root:"+_jsonCharFilter(data.row.JSONSTR)+"})");
					$('#condnamelable').html(data.row.CONDNAME);
					$('#CONDNAME').val(data.row.CONDNAME);
					$('#TARGET').val(target);
					$('#condid').text(data.row.EXPERTCONDID);	
					$('#customruleid').val(jQuery.url.param("ruleId"));
					var feature = jsonobj.root.FEATURE;
					var txnid = document.dataForm.TXNID.value;
					// 把参数的<tr>重写一下
					$('#param_0').parent().parent().html('<td><div id="param_0" style="display: inline"></div></td>');
					var atd0;
					if(feature == 'hit'){
						atd0 = "<input name='FEATUREID' type='hidden' id='FEATUREID' value='HITID'/>";
					}else if(feature == 'custom'){
						atd0 = "<input name='FEATUREID' type='hidden' id='FEATUREID' value='CUSTOMID'/>";
					}else{
						if(featureisparam == 1){
							atd0 = '<td>';
							atd0+='交易属性：<select name="FEATUREID" id="FEATUREID" style="width: 120px"';
							atd0+=' >';
							atd0+='<option value="" >--请选择--</option>';
							atd0+='</select>';	
							atd0+='</td>';
						}else{
							atd0='<input name="FEATUREID" id="FEATUREID" type="hidden" value=""/>';
						}
					}
					$('#param_0').parent().parent().prepend(atd0);

					var ruletxnid = $('#TXNID').val();
					
					// 显示规则下的属性列表
					var txnFeature = featureList[ruletxnid];
					var featureObj = document.getElementById("FEATUREID");
					if(featureObj.type != 'hidden'){
						$(featureObj).empty();// 删除下拉列表所有选项
						featureObj.add(new Option("--请选择--",""));
						if(isPatter[txnid] && target != null && target == 'UserIdType'){
							featureObj.add(new Option("--行为模式","PATTERNID"));
						}
						for(var f=0;txnFeature != null && f<txnFeature.length;f++){
							if (txnFeature[f]['G2'] == -1) {//如果是组或渠道，添加为optgroup属性，不能选择     
								var group = document.createElement('OPTGROUP');
								group.label = txnFeature[f]['PROPNAME'];
								group.innerText = " ";
								featureObj.appendChild(group);
							}
							else {
								featureObj.add(new Option(txnFeature[f]['PROPNAME'], txnFeature[f]['TXNFEATUREID']));
							}
						}
					}
					
					funInvoke1.show('param_0', "add");
				});
			}else{
				document.getElementById('right-box').style.display='none';
			}
		});
	//}
} 
function _jsonCharFilter(str) {
			str = str.replace(/\n/ig,"\\n");
			str = str.replace(/\\/ig,"\\\\");
			str = str.replace(/\t/ig,"\\t");
			str = str.replace(/\r/ig,"\\r");
			return str;
		}
		
function _jsonCharRe(str) {
			str = str.replace(/\\n/ig,"\n");
			str = str.replace(/\\\\/ig,"\\");
			str = str.replace(/\\t/ig,"\t");
			str = str.replace(/\\r/ig,"\r");
			return str;
}

// 定义返回按钮的事件
function cancelCustomBtn(blockCondDiv){
	document.getElementById(blockCondDiv).style.display='block';// 显示配置条件DIV
	document.getElementById('customcond').style.display='none';// 隐藏自定义条件DIV

    // constomDialog2.hide();
   // jcl.go('/tms/rule/edit?ruleId=' + rulecustomid);
}

// 增加条件保存事件
function updateCustom(blockCondDiv){
	if(ftype != 1){
		alert('请选择一个条件');
		return;
	}
	var txnid = document.dataForm.TXNID.value;
	var featureid = $('#FEATUREID').val();
	if(featureid == ''){
		alert("交易属性不能为空");
		$('#FEATUREID').focus();
		return;
	}
	document.getElementById(blockCondDiv).style.display='block';// 显示配置条件DIV
	document.getElementById('customcond').style.display='none';// 隐藏自定义条件DIV
	var expertsecond = secondcount; 
	var expertthree = threecount; 
	var expertnumble=''+expertsecond+''+"0000"+''+expertthree+'';

	// 增加一行
	addRow();

	// 给此行赋值
	
	$('#txnid'+expertsecond).val(txnid);
	$('#condmaptarget'+expertsecond).val($('#TARGET').val());
	// 交易属性下拉列表
	funseDatypeChoice(document.getElementById('condmaptarget'+expertsecond),expertnumble,featureList[txnid],isPatter[txnid]);
	$('#condmapfeature'+expertnumble).val(featureid);// 设置选中项
	// 函数下拉列表
	rulecondfeatureChange(document.getElementById('condmapfeature'+expertnumble),expertnumble,expertsecond);
	$('#condmaprange'+expertsecond).val($('#condrange').val());
	$('#condmaplimit'+expertsecond).val($('#condlimit').val());
	$('#condmapscore'+expertsecond).val($('#score').val());
	$('#condmapweight'+expertsecond).val($('#weight').val());
	$('#condmapround'+expertsecond).val($('#countround').val());
	$('#condid'+expertsecond).val($('#condid').val());

	// 校验参数和参数组
	var checkResult = funInvoke1.save("param_0");
	
	if( checkResult != null && checkResult.length > 0){
		alert(checkResult);
		return;
	}
		
	var customXml = funInvoke1.getJson();
	var params = "json="+customXml;
	jcl.postJSON('/tms/rule/conditioncustom/getFuncJson', params, function(data){
		$('#condmapoperator'+expertnumble).val(data.funcid);// 设置选中项

		// json
		var tmsrulexmlsecond = data.funcjson;
		secondfun[expertthree][0].setJson(tmsrulexmlsecond);
		secondfun[expertthree][0].show('secondrulecondsdiv'+expertnumble);
		$('#tmsrulexmlsecond'+expertsecond).val(tmsrulexmlsecond);

		document.getElementById(blockCondDiv).style.display='block';// 显示配置条件DIV
		document.getElementById('customcond').style.display='none';// 隐藏自定义条件DIV

	});
			
}

// 增加自定义条件页面
function addCondView(blockCondDiv){
	
	var addHtml = '<table width="100%" id="conftab" border="1">'
	+' <form name="condAddCustomForm" id="condAddCustomForm" method="post">'
	+' 	<tr>'
	+' 		<td width="20%"><div id="tree-box" style="height:420px;OVERFLOW-Y: auto;OVERFLOW-X: auto;"></div></td>'
	+' 		<td width="80%" valign="top">'
	+' 			<div id="right-box" style="list-style:none;display: inline;display: none; width:900px; height:420px;OVERFLOW-Y: auto;OVERFLOW-X: auto;">'
	+'				<table><tr><td>'
	+' 				<table> '
	+' 					<tr>'
	+' 						<ul class="list-box">'
	+' 						<td>'
	+' 						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">条件名称：</label> '
	+' 							<span class="list-box-item-content" id="condnamelable"></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">计分开始次数：</label> '
	+' 							<span class="list-box-item-content"><input id="condrange" size="3"  value="1" onblur="IsCheck(this,1)" name="condrange" maxlength="64" type="text" class="itext"/><font color="red">*</font></span>'
	+' 						</li>'
	+' 						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">分值：</label> '
	+' 							<span class="list-box-item-content"><input id="score" name="score" size="3"  value="0" onblur="IsCheck(this,3)" maxlength="10" type="text" class="itext"/><font color="red">*</font></span>'
	+' 						</li>'
	+' 						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">统计范围：</label> '
	+' 							<span class="list-box-item-content">'
	+'							<input id="countround" name="countround"  maxlength="4" size = "1" size="3"  value="1" onblur="IsCheck(this,5)" type="text" style="width:80px" class="itext"/>'
	+'							<select name="countunit" id = "countunit"></select><font color="red">*</font>'
	+'							</span>'
	+' 						</li></td>'
	+'						<td>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label"></label> '
	+' 							<span class="list-box-item-content"></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">计分停止次数：</label> '
	+' 							<span class="list-box-item-content"><input id="condlimit" name="condlimit" size="3"  value="1" onblur="IsCheck(this,2)" maxlength="64" type="text" class="itext"/><font color="red">*</font></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">权重：</label> '
	+' 							<span class="list-box-item-content"><input id="weight" name="weight" size="3"  value="100" onblur="IsCheck(this,4)" type="text" maxlength="10" class="itext"/>%<font color="red">*</font></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							</li>'
	+' 						</td>'
	+' 						</ul>'
	+' 					</tr>'
	+' 				</table></td></tr>'
	+' 					<tr><td><div id="param_0" style="display: inline"></div></td></tr></table>'
	+' 			</div>'
	+' 		</td>'
	+' 	</tr>'
	+' 	<tr>'
	+' 		<td valign="bottom" colspan= "2">'
	+' 			<div class="button-box">'
	+' 				<input type="button" value="确 定" class="btn" id="updateCustomBtn1" onclick="updateCustom(\''+blockCondDiv+'\')"/>'
	+' 				<input type="button" value="返 回" class="btn" id="cancelCustomBtn1" onclick="cancelCustomBtn(\''+blockCondDiv+'\')"/>'
	+' 			</div>'
	+' 		</td>'
	+' 	</tr>'
	+' 						<textarea id="condid" name="condid" style="visibility: hidden"></textarea>'
	+'				        <textarea id="customXml" name="customXml" style="visibility: hidden"></textarea>'
	+'				        <input name="customruleid" id="customruleid" type="hidden" />'
	+'						<input name="txnid" id="txnid" type="hidden" class="itext" />'
	+'						<input name="CONDNAME" id="CONDNAME" type="hidden" class="itext" />'
	+'						<input name="TARGET" id="TARGET" type="hidden" class="itext" />'
	+'						<input name="RELATION" id="RELATION" type="hidden" class="itext" />'
	+' 	</form>'
	+' </table>';

	$('#customcond').html(addHtml);
	// 统计单位下拉列表
	jcl.code.selector('countunit','tms.mgr.rulecmapunit');
}

var dialog3 = "";

// 修改自定义条件
function editCondView(rulecondmapid,condname){
	var	params = "rulecondmapid=" + rulecondmapid;
	// 获取页面初始化数据
	jcl.postJSON('/tms/rule/conditioncustom/get?', params, function(data){
				$('#condnamelable').html(condname);
				$('#rulecondmapid').val(rulecondmapid);
				$('#score').val(data.row.SCORE);
				$('#weight').val(data.row.WEIGHT);
				$('#condrange').val(data.row.CONDRANGE);
				$('#condlimit').val(data.row.CONDLIMIT);
				$('#countround').val(data.row.COUNTROUND);
				$('#metadata').text(data.row.METADATA);
				$('#customruleid').val(data.row.RULEID);
				$('#countunit').attr('value',data.row.COUNTUNIT);
				funInvoke1.setJson(data.row.METADATA);
				funInvoke1.show('param_0', "edit");
			});

	var updateHtml = '<div id="updateCustomCond" style="height:600px; OVERFLOW-Y: auto;OVERFLOW-X: auto;"><table width="100%" id="conftab" border="1" style="border-collapse: collapse;">'
	+' <form name="condCustomForm" id="condCustomForm" method="post">'
	+' 	<tr>'
	+' 		<td width="100%" valign="top" style="display: inline;height:420px;">'
	+' 			<div id="right-box" style="list-style:none;display: inline;height:420px;">'
	+' 				<table><tr><td><table> '
	+' 					<tr>'
	+' 						<ul class="list-box">'
	+' 						<td>'
	+' 						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">条件名称：</label> '
	+' 							<span class="list-box-item-content" id="condnamelable"></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">计分开始次数：</label> '
	+' 							<span class="list-box-item-content"><input id="condrange" name="condrange" size="3"  value="1" onblur="IsCheck(this,1)" type="text" maxlength="64" class="itext"/><font color="red">*</font></span>'
	+' 						</li>'
	+' 						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">分值：</label> '
	+' 							<span class="list-box-item-content"><input id="score" name="score" type="text" size="3"  value="0" onblur="IsCheck(this,3)" maxlength="10" class="itext"/><font color="red">*</font></span>'
	+' 						</li>'
	+' 						<li class="list-box-item">'
	+' 						<label class="list-box-item-label">统计范围：</label> '
	+' 							<span class="list-box-item-content">'
	+'							<input id="countround" name="countround" maxlength="4" style="width:80px" size="3"  value="1" onblur="IsCheck(this,5)" type="text" class="itext"/>'
	+'							<select name="countunit" id = "countunit"></select><font color="red">*</font>'
	+' 							</span>'
	+' 						</li></td>'
	+'						<td>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label"></label> '
	+' 							<span class="list-box-item-content"></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">计分停止次数：</label> '
	+' 							<span class="list-box-item-content"><input id="condlimit" name="condlimit" size="3"  value="1" onblur="IsCheck(this,2)" maxlength="64" type="text" class="itext"/><font color="red">*</font></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							<label class="list-box-item-label">权重：</label> '
	+' 							<span class="list-box-item-content"><input id="weight" name="weight" size="3"  value="100" type="text" onblur="IsCheck(this,4)" maxlength="10" class="itext"/>%<font color="red">*</font></span>'
	+' 							</li>'
	+'						<li class="list-box-item">'
	+' 							</li>'
	+' 						</td>'
	+' 						</ul>'
	+' 					</tr>'
	+' 				</table></td></tr>'
	+'				<tr><td><div id="param_0" style="display: inline"></div></td></tr></table>'
	+' 			</div>'
	+' 		</td>'
	+' 	</tr>'
	+' 	<tr>'
	+' 		<td valign="bottom" colspan= "2">'
	+' 			<div class="button-box">'
	+' 				<input type="button" value="确 定" class="btn" id="updateCustomBtn"/>'
	+' 				<input type="button" value="返 回" class="btn" id="cancelCustomBtn"/>'
	+'				<input name="rulecondmapid" id="rulecondmapid" type="hidden" class="itext" />'
	+'				<input name="customruleid" id="customruleid" type="hidden" class="itext" />'
	+'				<input name="txnid" id="txnid" type="hidden" class="itext" />'
	+'				<textarea id="metadata" name="metadata" style="visibility: hidden"></textarea>'
	+' 			</div>'
	+' 		</td>'
	+' 	</tr>'
	+' 	</form>'
	+' </table></div>';

	// 弹出窗口
	dialog3 = new jcl.ui.Dialog({
		id: 'dialogEditCustom',
		title:'条件配置',
		closeable: false,
		width:1150
	});
	dialog3.addHtml(updateHtml);
	dialog3.show();
	$('#dialogEditCustom').unmiddle();
	
	// 统计单位下拉列表
	jcl.code.selector('countunit','tms.mgr.rulecmapunit');
	
	// 定义修改页面的确定按钮
	$('#updateCustomBtn').live('click',editCustom);
		
	// 定义修改页面的返回按钮
	$('#cancelCustomBtn').live('click',function(){
		dialog3.hide(); 
		var ruleId=$('#customruleid').val()
		jcl.go('/tms/rule/edit?ruleId=' + ruleId);
	});
	
}

// 自定义条件修改事件
function editCustom(){
	
	
	// 校验参数和参数组
	var checkResult = funInvoke1.save("param_0");

	if( checkResult != null && checkResult.length > 0){
		alert(checkResult);
		return;
	}
	document.condCustomForm.txnid.value=document.dataForm.TXNID.value;
	$('#metadata').text(funInvoke1.getJson());
	/*var rulecondmapid = $('#rulecondmapid').val();
	var score = $('#score').val();
	var weight = $('#weight').val();
	var condrange = $('#condrange').val();
	var condlimit = $('#condlimit').val();
	var metadata = $('#metadata').val();
	var ruleid = $('#ruleid').val();
	var countround = $('#countround').val();
	var countunit =$('select[name=countunit]').val();*/
	//var params = "rulecondmapid="+rulecondmapid + "&ruleid=" + ruleid + "&score="+score + "&weight=" +weight + "&condrange="+condrange + "&condlimit="+condlimit+"&metadata="+metadata + "&countround="+countround + '&countunit=' + countunit ;
	var params = $('#condCustomForm').serialize();
	// 关闭窗口
	dialog3.hide();
	jcl.postJSON('/tms/rule/conditioncustom/mod?', params, function(data){
		// 页面跳转
		//jcl.go('/tms/rule/edit?ruleId=' + $('#customruleid').val());
		dialogsuccess.show();
	});
}

function checkCustom(){
	
	var condrangeObj = $('#condrange');
	var condrange = condrangeObj.val();
	
	if(condrange==null || condrange.length == 0){
		alert('命中次数不能为空');
		condrangeObj.focus();
		return false;
	}
	
	var condlimitObj = $('#condlimit');
	var condlimit = condlimitObj.val();
	
	if(condlimit==null || condlimit.length == 0){
		alert('计分停止次数不能为空');
		condlimitObj.focus();
		return false;
	}
	
	var scoreObj = $('#score');
	var score = scoreObj.val();
	
	if(score==null || score.length == 0){
		alert('分值不能为空');
		scoreObj.focus();
		return false;
	}
	
	var weightObj = $('#weight');
	var weight = weightObj.val();
	
	if(weight==null || weight.length == 0){
		alert('权重不能为空');
		weightObj.focus();
		return false;
	}
	
	var countroundObj = $('#countround');
	var countround = countroundObj.val();
	
	if(countround==null || countround.length == 0){
		alert('统计范围不能为空');
		countroundObj.focus();
		return false;
	}
	return true;
}
