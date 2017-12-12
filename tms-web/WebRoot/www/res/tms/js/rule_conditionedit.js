//UTF-8 encoding

var rulecondmaplist ; // 条件列表
var relationList;// 关联关系
var rangeoper;// 关联关系
var txnList;// 交易对象下拉列表
var dataTypeList;//对象类型列表
var featureList;//属性列表
var funcList;//条件表达域列表
var isPatter;// 是否行为模式
var secondcount=0;//动态添加运算符标识数组
var threecount =0;
var secondfun = new Array();;//标识符参数js对象组
var rulecountunitlist;//统计单位

var funInvokeedit = new jcl_tms.util.FuncInvoke({
	id:'funInvokeedit'
});

var dialogmapedit =""
var editloading = "";
$(document).ready(function(){
	
	/*dialogmapedit = new jcl.ui.Dialog({
		id:'dialogmapedit',
		title:'修改条件配置',
		closeable: false,
		width:1150
	});
	dialogmapedit.addDom('deployeditcond');*/
	dialogmapedit = new jcl.ui.Dialog({
		id:'dialogmapedit',
		title:'条件配置',
		width:1150,
		closeable: false
	});
	dialogmapedit.addDom('addcond');

	editloading = new jcl.ui.Loading({
	         id:'rulemapedit-loading',
	         zindex : 800
	         });

	$('#upeditcondmapBtn').click(function(){
		editloading.show();

		//规则主键
		var ruleId = $('#ruleId').val();
		//大行行数
		var secondcount = $('#secondcount').val();
		//小列行数
		var threecount = $('#threecount').val();

		var msg = false;
		var secondchecknum=0;// 用于校验提示的大行号
		for(var i=0;i<secondcount;i++)
		{
			var threechecknum=1;// 用于校验提示的小行号
			//为存放初始化参数xml的隐藏于循环赋值
			for(var j=0;j<threecount;j++){	
			
				//明细序号
				var shownumblenum=''+i+''+"0000"+''+j+'';

				var featurnObj = $('#condmapfeature'+shownumblenum);

				// 如果交易属性对象存在，则需要检验交易属性、交易函数以及函数参数是否有值和是否合法
				if(featurnObj.html() != null){
					// 校验交易属性下拉列表不能为空
					var featurnconfig=featurnObj.val();// 交易属性值
					var strfeature=IsRowCheckselect(featurnconfig,secondchecknum,threechecknum,shownumblenum,2);
					if(strfeature == false)
					{
						editloading.hide();
						return $('#condmapfeature'+shownumblenum).focus();
					}

					// 校验函数下拉列表不能为空
					var condmapoperator = $('#condmapoperator'+shownumblenum).val();// 函数值
					var stroperator=IsRowCheckselect(condmapoperator,secondchecknum,threechecknum,shownumblenum,3);
					if(stroperator == false)
					{
					   editloading.hide();
					   return $('#condmapoperator'+shownumblenum).focus();
					}

					// 校验函数参数的值，并给函数参数值的隐藏域赋值
					msg  = secondfun[j][0].save('secondrulecondsdiv'+shownumblenum);	
					if(msg!=''){
						editloading.hide();
						alert("第"+(secondchecknum+1)+"大行"+"第"+(threechecknum)+"小行条件域表达式值有误："+msg);
						return;
						break;
					} 
					var tconds = secondfun[j][0].getJson();   
					$('#tmsrulexmlsecond'+shownumblenum).val(tconds);
					threechecknum ++ ;
				}					
			}
			if(threechecknum > 1){
				secondchecknum++;
			}
		}
		var params = $('#condeditForm').serialize();
		jcl.postJSON('/tms/rule/editRuleMap', params, function(data){
			var errorMsg = data.errorMsg;
			

			if(errorMsg != null && errorMsg.length > 0){
				alert("保存规则条件出错");
				jcl.go('/tms/rule/edit?ruleId=' + ruleId);
			}else{
				dialogsuccess.show();
			}

			// 关闭窗口
			dialogmapedit.hide();
			editloading.hide();

		});
		
	});

}); 






//配置条件函数
function editdeploycondConfig()
{

	  var	editloading = new jcl.ui.Loading({
	         id:'rulemapedit-loading',
	         zindex : 800
	         });
	        editloading.show();
	        
	document.getElementById('deployeditcond').style.display='block';	
	var ruleId = "ruleId="+jQuery.url.param("ruleId");
	$('#ruleid').val(jQuery.url.param("ruleId"));
	
	jcl.postJSON('/tms/rule/editconfig', ruleId, function(data){
		
		 rulecondmaplist = data.rulecondmaplist;// 规则对应的所有条件信息

		 dataTypeList = data.dataTypeList;//对象类型

		 featureList=data.featureList;//  交易属性

		 isPatter=data.isPatter;//  是否行为模式

		 funcList=data.funcList;// 所有函数列表

		 rulecountunitlist=data.rulecountunitlist;//统计单位

		 relationList=data.relationList; // 关联关系下拉列表
		 rangeoper=data.rangeoperList; // 关联关系下拉列表

		 txnList = data.txnList;// 交易对象下拉列表
	     //默认给出保存后的规则条件映射
		 for(var u=0;u<rulecondmaplist.length;u++){
				var condMap = rulecondmaplist[u];
				var oTbody =document.getElementById("tbody");
				var rowlength = oTbody.rows.length;
				var newTr = oTbody.insertRow(rowlength);
				newTr.setAttribute("id","tr" + secondcount.toString());
				newTr.setAttribute("valign","top");
				var newTd0 = newTr.insertCell(0); 
				newTd0.setAttribute("valign","top");
				var newTd1 = newTr.insertCell(1); 
				newTd1.setAttribute("valign","top");
				var newTd2 = newTr.insertCell(2); 
				newTd2.setAttribute("valign","top");
				var newTd3 = newTr.insertCell(3); 
				newTd3.setAttribute("valign","top");
				var newTd4 = newTr.insertCell(4); 
				newTd4.setAttribute("valign","top");
				var newTd5 = newTr.insertCell(5); 
				newTd5.setAttribute("valign","top");
				var newTd6 = newTr.insertCell(6); 
				newTd6.setAttribute("valign","top");
				var newTd7 = newTr.insertCell(7); 
				newTd7.setAttribute("valign","top");
				var newTd8 = newTr.insertCell(8); 
				newTd8.setAttribute("valign","top");
				var newTd9 = newTr.insertCell(9); 
				newTd9.setAttribute("valign","top");

				var shownumble=''+secondcount+''+"0000"+''+threecount+'';

				// 第一行不显示关联关系
				if(u > 0){
					// 关联关系
					var td0='';
					td0+='<select name="relation'+secondcount+'" id="relation'+secondcount+'" style="width: 60px"';
					td0+=' >';
					for(var k=0;k<relationList.length;k++){
						if(relationList[k]['CODE_KEY']==condMap['RELATION']){
							td0+='<option value="'+relationList[k]['CODE_KEY']+'" selected="true">'+relationList[k]['CODE_VALUE']+'</option>';
						}else{
							td0+='<option value="'+relationList[k]['CODE_KEY']+'">'+relationList[k]['CODE_VALUE']+'</option>';
						}
					}	
					td0+='</select>';	
					newTd0.innerHTML = td0 ;
				}

				// 交易对象
				var td1='';
				td1+='<select name="txnid'+secondcount+'" id="txnid'+secondcount+'" style="width: 80px"';
				td1+=' onchange="funseDatypeChoiceAll(this,\''+secondcount+'\')"';
				td1+=' >';
				for(var k=0;k<txnList.length;k++){
					if(txnList[k]['K'].indexOf('G')>-1){//如果是组或渠道，添加为optgroup属性，不能选择               
						td1+="<optgroup label='"+txnList[k]['V']+"'> "+ txnList[k]['V'] + "</optgroup>";
					}else{
						if(txnList[k]['K']==condMap['TXNID']){
							//td1+='<option value="'+txnList[k]['TXNID']+'" selected="true">'+txnList[k]['TXNNAME']+'</option>';
							td1+="<option value='" + txnList[k]['K'] + "' selected='true'>" + txnList[k]['V'] + "</option>";
						}else{
							//td1+='<option value="'+txnList[k]['TXNID']+'">'+txnList[k]['TXNNAME']+'</option>';
							td1+="<option value='" + txnList[k]['K'] + "'>" + txnList[k]['V'] + "</option>";
						}
					} 
				}	
				td1+='</select>';	
				newTd1.innerHTML = td1 ;

				// 条件对象
				var td2='';
				td2+='<input name="condmapId'+secondcount+'" type="hidden" size="10" id="condmapId'+secondcount+'" value=\''+condMap['RULECONDMAPID']+'\'/>';
				td2+='<select name="condmaptarget'+secondcount+'" id="condmaptarget'+secondcount+'" style="width: 60px"';
				td2+=' onchange="funseDatypeChoiceAll(this,\''+secondcount+'\')">';
				td2+='<option value="-1" >交易</option>';
				for(var k=0;k<dataTypeList.length;k++){
					if(condMap['TARGET'] != null && condMap['TARGET'].length > 0 && condMap['TARGET']==dataTypeList[k]['DATATYPE']){
						td2+='<option value="'+dataTypeList[k]['DATATYPE']+'" selected="selected">'+dataTypeList[k]['DATATYPENAME']+'</option>';
					}else{
						td2+='<option value="'+dataTypeList[k]['DATATYPE']+'">'+dataTypeList[k]['DATATYPENAME']+'</option>';
					}
				}	
				td2+='</select>';	
				newTd2.innerHTML = td2 ;
				// 拼条件域
				td3 = '<table width="100%" id="deployconftab" border="1">';
				td3 += '<tbody id="smalltbody'+secondcount+'">';
				td3+='</tbody>';
				td3+='</table>';
				newTd3.innerHTML = td3;

				var jsonObj = _json2Obj(condMap['METADATA']);
				var datarulemapmeta=jsonObj.root;
				$.each(datarulemapmeta, function(idx,item){
					shownumble=''+secondcount+''+"0000"+''+threecount+'';
					var datarulestr = _json2str(item.RULEfeature);
					var ruleandoroperator = item.ruleandoroperator;// 操作符
					var rulemodeltype = item.modeltype;// 交易类型
					var rulecondsid = item.RULEfeature.ID;
					var featureid = item.mapfeature;// 交易ID 
					var _threecount = threecount;

					var a = -1;
					if(ruleandoroperator=='and'){
						a = 0
					}
					if(ruleandoroperator=='or'){
						a = 1;
					}

					// 增加一行
					andSecondRow(a,secondcount);
					
					// 交易属性下拉列表
					funseDatypeChoice(document.getElementById('condmaptarget'+secondcount),shownumble,featureList[condMap['TXNID']],isPatter[condMap['TXNID']]);
					$('#condmapfeature'+shownumble).val(featureid);// 设置选中项

					// 函数下拉列表
					rulecondfeatureChange(document.getElementById('condmapfeature'+shownumble),shownumble,secondcount);
					$('#condmapoperator'+shownumble).val(rulecondsid);// 设置选中项

					//根据初始化参数的个数(行数)new 公用插件对象，用户函数选择时，对函数参数的设置
					var temp = new jcl_tms.util.FuncInvoke({
							id:'tms'+_threecount
					});
					secondfun[_threecount] = new Array(temp);
					secondfun[_threecount][0].setJson(datarulestr);
					secondfun[_threecount][0].show('secondrulecondsdiv'+shownumble,'edit');
								
				}); 
				
				var td4 ='<select name="rangeoper'+secondcount+'" id="rangeoper'+secondcount+'">';
				for(var m=0;m<rangeoper.length;m++){
					if(condMap['RANGEOPER'] != null && condMap['RANGEOPER'].length > 0 && condMap['RANGEOPER']==rangeoper[m]['CODE_KEY']){
						td4+='<option value="'+rangeoper[m]['CODE_KEY']+'" selected="selected">'+rangeoper[m]['CODE_VALUE']+'</option>';
					}else{
						td4+='<option value="'+rangeoper[m]['CODE_KEY']+'">'+rangeoper[m]['CODE_VALUE']+'</option>';
					}
				}	
				td4+='</select>';
				// 命中次数
				td4 +='<input name="condmaprange'+secondcount+'" type="text" size="1" value="'+condMap['CONDRANGE']+'" onblur="IsCheck(this,1)" id="condmaprange'+secondcount+'" /></td>';
				newTd4.innerHTML = td4;
					
				// 计分上限
				var td5 ='<input name="condmaplimit'+secondcount+'" type="text" size="3"  value="'+condMap['CONDLIMIT']+'" onblur="IsCheck(this,2)" id="condmaplimit'+secondcount+'" /></td>';
				newTd5.innerHTML = td5;
				
				// 分值
				var td6 ='<input name="condmapscore'+secondcount+'" type="text" size="3"  value="'+condMap['SCORE']+'" onblur="IsCheck(this,3)" id="condmapscore'+secondcount+'" /></td>';
				newTd6.innerHTML = td6;

				// 权重
				var td7 ='<input name="condmapweight'+secondcount+'" type="text" size="2"   value="'+condMap['WEIGHT']+'" onblur="IsCheck(this,4)" id="condmapweight'+secondcount+'"/>%</td>';
				newTd7.innerHTML = td7;

				// 统计范围
				var td8='<input name="condmapround'+secondcount+'" type="text" size="1"   value="'+condMap['COUNTROUND']+'" onblur="IsCheck(this,5)" id="condmapround'+secondcount+'"/>';
				td8 +='<select name="condmapunit'+secondcount+'" id="condmapunit'+secondcount+'" style="width:55px" >';
				for(var m=0;m<rulecountunitlist.length;m++){
					
					if(condMap['COUNTUNIT'] != null && condMap['COUNTUNIT'].length > 0 && condMap['COUNTUNIT']==rulecountunitlist[m]['COUNTUNIT']){
						td8+='<option value="'+rulecountunitlist[m]['COUNTUNIT']+'" selected="selected">'+rulecountunitlist[m]['COUNTUNITNAME']+'</option>';
					}else{
						td8+='<option value="'+rulecountunitlist[m]['COUNTUNIT']+'">'+rulecountunitlist[m]['COUNTUNITNAME']+'</option>';
					}
				}	
				td8+='</select>';
				if(condMap['CONSECUTIVE'] == 'on'){
					td8+='<input name="consecutive'+secondcount+'" type="checkbox" id="consecutive'+secondcount+'" value="'+condMap['CONSECUTIVE']+'" checked="true"/>';
				}else{
					td8+='<input name="consecutive'+secondcount+'" type="checkbox" id="consecutive'+secondcount+'"/>';
				}
				td8+='<input type="hidden" name="tmsfeateretype'+shownumble+'"  id="tmsfeateretype'+shownumble+'" value="'+featuretype+'"/>';
				td8+='<input type="hidden" name="tmsrulexmlsecond'+shownumble+'"  id="tmsrulexmlsecond'+shownumble+'"/>';
				td8+='<input type="hidden" name="delcondmapaddid'+secondcount+'"  id="delcondmapaddid'+secondcount+'"/></td>';
				newTd8.innerHTML = td8;

				// 删除按钮
				var td9='<input type="button" value="删除" align="left" class="btn" onclick="deleteCurrentRow(\''+secondcount+'\')"/>';
				newTd9.innerHTML = td9;

				secondcount++;
				$('#secondcount').val(secondcount);
				$('#threecount').val(threecount);
				count++;
				$('#count').val(count);
		
				changeRowColor();
		 }
			
		 editloading.hide();
	});

}

