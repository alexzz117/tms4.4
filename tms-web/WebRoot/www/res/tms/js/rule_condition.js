//UTF-8 encoding

var dataTypeList;//对象类型列表
var relationList;// 关联关系
var txnList;// 交易对象下拉列表
var funcList;// 所有函数列表
var isPatter;// 是否行为模式
var featureList;//属性列表
var condsList; //条件表达域列表
var count =0;//总行动态行数标识
var secondcount=0;//动态添加运算符标识数组
var threecount =0;
var secondfun = new Array();//运算符参数js对象组,json对象
var rulecountunitlist;//统计单位
var rangeoper;//统计单位
var featuretype='';
var delcondmapaddid='';//因为是假删。所以需要记录需要假删的id

var funInvoke = new jcl_tms.util.FuncInvoke({
	id:'funInvoke'
});
var addloading = "";
var dialog2 = "";
$(document).ready(function(){
	
	dialog2 = new jcl.ui.Dialog({
		id:'dialog2',
		title:'条件配置',
		width:1150,
		closeable: false
	});
	dialog2.addDom('addcond');
	
	
	addloading = new jcl.ui.Loading({
	         id:'rulemapadd-loading',
	         zindex : 800
	         });
	  
	
	$('#upcondmapBtn').click(function(){
		addloading.show();
		$('#ruleid').val(jQuery.url.param("ruleId"));
		//规则主键
		var ruleId = $('#ruleId').val();
		//大行行数
		var count = $('#count').val();
		//大行行数
		var secondcount = $('#secondcount').val();
		//小行行数
		var threecount = $('#threecount').val();
		
		//页面删除的id
		var checkdelcondaddmapid = $('#delcondmapaddid').val();
		//检验对应返回页面的行号
    	var checknumber=0;
    		
    	var msg = false;
		var secondchecknum=0;// 用于校验提示的大行号
		for(var i=0;i<secondcount;i++)
		{
			var threechecknum=1;// 用于校验提示的小行号
			//为存放初始化参数xml的隐藏于循环赋值
			for(var j=0;j<threecount;j++){	
				var shownumblenum=''+i+''+"0000"+''+j+'';
				
				var featurnObj = $('#condmapfeature'+shownumblenum);

				// 如果交易属性对象存在，则需要检验交易属性、交易函数以及函数参数是否有值和是否合法
				if(featurnObj.html() != null){
					// 校验交易属性下拉列表不能为空
					var featurnconfig=featurnObj.val();// 交易属性值
					var strfeature=IsRowCheckselect(featurnconfig,secondchecknum,threechecknum,shownumblenum,2);
					if(strfeature == false)
				    {
						addloading.hide();
				        return $('#condmapfeature'+shownumblenum).focus();
				    }

					// 校验函数下拉列表不能为空
					var condmapoperator = $('#condmapoperator'+shownumblenum).val();// 函数值
					var stroperator=IsRowCheckselect(condmapoperator,secondchecknum,threechecknum,shownumblenum,3);
					if(stroperator == false)
				    {
					   addloading.hide();
				       return $('#condmapoperator'+shownumblenum).focus();
				    }


					// 校验函数参数的值，并给函数参数值的隐藏域赋值
					msg  = secondfun[j][0].save('secondrulecondsdiv'+shownumblenum);	
					if(msg!=''){
						addloading.hide();
						alert("第"+(secondchecknum+1)+"大行"+"第"+(threechecknum+1)+"小行条件域表达式值有误："+msg);
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

		var params = $('#condForm').serialize();
		jcl.postJSON('/tms/rule/addRuleMap', params, function(data){
			var errorMsg = data.errorMsg;
			if(errorMsg != null && errorMsg.length > 0){
				alert("保存规则条件出错");
				jcl.go('/tms/rule/edit?ruleId=' + ruleId);
			}else{
				dialogmaps.show();
			}
			// 关闭窗口
			dialog2.hide();	
			addloading.hide();
		});
		
	});


   //jcl.code.selector('CONDTYPE','tms.mgr.rulecmaptype',{text:'--请选择--',value:'-1'});

    
   //初始化数据 
	/*$('#CONDTYPE').change(function(){
		var condtype=$(this).val();
		//请选择
		if(condtype==-1)
		{
			document.getElementById('deploycond').style.display='none';// 隐藏配置条件DIV
			document.getElementById('customcond').style.display='none';// 隐藏自定义条件DIV
			document.getElementById('backButtonDiv').style.display='block';// 显示返回按钮DIV 
			if(document.getElementById('deployeditcond')!=null)
			{
				document.getElementById('deployeditcond').style.display='none';	// 隐藏修改配置条件DIV
			}
		}
		//配置
		if(condtype==0)
		{
			document.getElementById('deploycond').style.display='block';// 显示配置条件DIV
			document.getElementById('customcond').style.display='none';// 隐藏自定义条件DIV
			document.getElementById('backButtonDiv').style.display='none';// 隐藏返回按钮
			if(document.getElementById('deployeditcond')!=null)
			{
				document.getElementById('deployeditcond').style.display='none';	// 隐藏修改配置条件DIV
			}

			deploycondConfig();
		} 
		// 自定义条件
		if(condtype==1)
		{
			document.getElementById('deploycond').style.display='none';// 隐藏配置条件DIV
			document.getElementById('customcond').style.display='block';// 显示自定义条件DIV
			document.getElementById('backButtonDiv').style.display='none';// 隐藏返回按钮
			if(document.getElementById('deployeditcond')!=null)
			{
				document.getElementById('deployeditcond').style.display='none';	// 隐藏修改配置条件DIV
			}
			customConditionView(); 
		}
		
	}); */
});





// 配置条件新增页面初始化
function deploycondConfig()
{
	dialog2 = new jcl.ui.Dialog({
		id:'dialog2',
		title:'条件配置',
		width:1150,
		closeable: false
	});
	dialog2.addDom('addcond');

	addloading.show();
	        
	var ruleId = "ruleId="+jQuery.url.param("ruleId");
	
	jcl.postJSON('/tms/rule/config', ruleId, function(data){
		
		dataTypeList = data.dataTypeList;// 条件对象下拉列表(五大数据类型)
		featureList=data.featureList;// 交易属性下拉列表
		rulecountunitlist=data.rulecountunitlist; // 统计单位下拉列表数据
		rangeoper=data.rangeoperList; // 统计单位下拉列表数据
		funcList=data.funcList; // 所有函数列表
		isPatter = data.isPatter;// 此交易是否行为模式
		relationList=data.relationList; // 关联关系下拉列表
		txnList = data.txnList;// 交易对象下拉列表
		if(count==0)
		{
			// 默认给出一行
			addRow();

			// 第一行的关联关系应该为空
			newTd0.innerHTML = "";
				
		}
		
		  addloading.hide();
	});

}


var newTd0;
//初始化一大行,动态添加一大行

function addRow(){
	
	var oTbody =document.getElementById('tbody');
	var rowlength = oTbody.rows.length;
		 
	var newTr = oTbody.insertRow(rowlength);
	newTr.setAttribute("id","tr" + secondcount.toString());
	newTr.setAttribute("valign","top");
	
	newTd0 = newTr.insertCell(0); 
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

	// 关联关系
	var td0='';
	td0+='<select name="relation'+secondcount+'" id="relation'+secondcount+'"  style="width: 60px"';
	//td0+=' onchange="funseDatypeChoiceAll(this,\''+secondcount+'\')"';
	td0+=' >';
	for(var k=0;k<relationList.length;k++){
		if(relationList[k]['CODE_KEY']=='and'){
			td0+='<option value="'+relationList[k]['CODE_KEY']+'" selected="true">'+relationList[k]['CODE_VALUE']+'</option>';
		}else{
			td0+='<option value="'+relationList[k]['CODE_KEY']+'">'+relationList[k]['CODE_VALUE']+'</option>';
		}
	}	
	td0+='</select>';	
	newTd0.innerHTML = td0 ;

	// 交易对象
	var txnid = $('#TXNID').val();// 规则的交易ID
	var td1='';
	td1+='<select name="txnid'+secondcount+'" id="txnid'+secondcount+'" style="width: 80px"';
	td1+=' onchange="funseDatypeChoiceAll(this,\''+secondcount+'\')"';
	td1+=' >';
	for(var k=0;k<txnList.length;k++){
		
		
			if(txnList[k]['K'].indexOf('G')>-1){//如果是组或渠道，添加为optgroup属性，不能选择               
				td1+="<optgroup label='"+txnList[k]['V']+"'> "+ txnList[k]['V'] + "</optgroup>";
			}else{
				if(txnList[k]['K']==txnid){
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
	td2+='<input name="condmapId'+secondcount+'" type="hidden" size="10" id="condmapId'+secondcount+'" />';
	td2+='<input name="condid'+secondcount+'" type="hidden" size="10" id="condid'+secondcount+'" />';
	td2+='<select name="condmaptarget'+secondcount+'" id="condmaptarget'+secondcount+'" style="width: 60px"';
	td2+=' onchange="funseDatypeChoiceAll(this,\''+secondcount+'\')">';
	td2+='<option value="-1" >交易</option>';
	for(var k=0;k<dataTypeList.length;k++){
		td2+='<option value="'+dataTypeList[k]['DATATYPE']+'">'+dataTypeList[k]['DATATYPENAME']+'</option>';
	}	
	td2+='</select>';	
	newTd2.innerHTML = td2 ;


	// 拼条件域
	td3 = '<table width="100%" id="deployconftab" border="1">';
	td3 += '<tbody id="smalltbody'+secondcount+'" align="center">';
	td3 += '<tr id="smallDataRow1">';
			
	// 交易属性
	td3+='<td id="featureTrafficDiv'+shownumble+'" align="left" valign="top" style="display:block;width: 90px">';
	td3+='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<select name="condmapfeature'+shownumble+'" id="condmapfeature'+shownumble+'"  style="width: 90px"';
	td3+=' onchange="rulecondfeatureChange(this,\''+shownumble+'\',\''+secondcount+'\'); ">';
	td3+='<option value="" >--请选择--</option>';
	var feature = featureList[txnid];
	for(var i=0;i<feature.length;i++){
		if(feature[i]['G2']==-1){//如果是组或渠道，添加为optgroup属性，不能选择               
			td3+='<optgroup label="'+feature[i]['PROPNAME']+'">' + feature[i]['PROPNAME'] + '</optgroup>';
		}else{
			td3+='<option value="'+feature[i]['TXNFEATUREID']+'">'+feature[i]['PROPNAME'] + '</option>';
		} 
	}	
	td3+='</select>';
	td3+='</td>';

	//交易函数
	td3+='<td id="Trafficdiv'+shownumble+'" valign="top" style="display:block;width: 70px">';
	td3+='<select name="condmapoperator'+shownumble+'" id="condmapoperator'+shownumble+'"  style="width: 70px"';
	td3+=' onchange="funsecondcountChange(this,\''+shownumble+'\',\''+threecount+'\')">';
	td3+='<option value="" >--请选择--</option>';
	td3+='</select>';
	td3+='</td>';
	td3+='<td id="Trafficdiv2'+shownumble+'"  align="left" valign="top" style="display:block">';
	td3+='<div id="secondrulecondsdiv'+shownumble+'"></div>'
	td3+='</td>';
		
	td3+='<td id="Trafficdiv3'+shownumble+'" valign="top" style="display:block;width: 130px">';
	td3+='<input type="button" value="and" class="btn" id="andRow'+shownumble+'" onclick="andSecondRow(0,\''+secondcount+'\')"/>';
	td3+='<input type="button" value="or" class="btn" id="orRow'+shownumble+'" onclick="andSecondRow(1,\''+secondcount+'\')"/>';
	td3+='<input type="button" value="del" class="btn" id="dellRow'+shownumble+'" onclick="removeRow(\''+secondcount+'\',\''+threecount+'\')"/>';
	td3+='</td>';
					
	td3+='</tr>';
	td3+='</tbody>';
	td3+='</table>';			
	newTd3.innerHTML = td3 ;		
		
	// 命中次数
	var td4 ='<select name="rangeoper'+secondcount+'" id="rangeoper'+secondcount+'">';
	for (var m = 0; m < rangeoper.length; m++) {
		if (rangeoper[m]['CODE_KEY'] == 'eq') {
			td4 += '<option value="' + rangeoper[m]['CODE_KEY'] + '"  selected="selected">' + rangeoper[m]['CODE_VALUE'] + '</option>';
		}
		else {
			td4 += '<option value="' + rangeoper[m]['CODE_KEY'] + '">' + rangeoper[m]['CODE_VALUE'] + '</option>';
		}
	}
	td4+='</select>';
	td4 +='<input name="condmaprange'+secondcount+'" type="text" size="1" value="1" onblur="IsCheck(this,1)" id="condmaprange'+secondcount+'" /></td>';
	newTd4.innerHTML = td4;
		
	// 计分上限
	var td5 ='<input name="condmaplimit'+secondcount+'" type="text" size="3"  value="1" onblur="IsCheck(this,2)" id="condmaplimit'+secondcount+'" /></td>';
	newTd5.innerHTML = td5;
	
	// 分值
	var td6 ='<input name="condmapscore'+secondcount+'" type="text" size="3"  value="0" onblur="IsCheck(this,3)" id="condmapscore'+secondcount+'" /></td>';
	newTd6.innerHTML = td6;

	// 权重
	var td7 ='<input name="condmapweight'+secondcount+'" type="text" size="2"   value="100" onblur="IsCheck(this,4)" id="condmapweight'+secondcount+'"/>%</td>';
	newTd7.innerHTML = td7;

	// 统计范围
	var td8='<input name="condmapround'+secondcount+'" type="text" size="1"   value="1" onblur="IsCheck(this,5)" id="condmapround'+secondcount+'"/>';
	td8 +='<select name="condmapunit'+secondcount+'" id="condmapunit'+secondcount+'" style="width:55px" >';
	for(var m=0;m<rulecountunitlist.length;m++){
		td8+='<option value="'+rulecountunitlist[m]['COUNTUNIT']+'">'+rulecountunitlist[m]['COUNTUNITNAME']+'</option>';
	}	
	td8+='</select>';
	td8+='<input name="consecutive'+secondcount+'" type="checkbox" id="consecutive'+secondcount+'"/>';
	td8+='<input type="hidden" name="tmsfeateretype'+shownumble+'" size="10" id="tmsfeateretype'+shownumble+'" value="'+featuretype+'"/>';
	td8+='<input type="hidden" name="delcondmapaddid'+secondcount+'" size="10"   id="delcondmapaddid'+secondcount+'"/>';
	td8+='<input type="hidden" name="tmsrulexmlsecond'+shownumble+'" size="10"   id="tmsrulexmlsecond'+shownumble+'"/></td>';
	newTd8.innerHTML = td8;

	// 删除按钮
	var td9='<input type="button" value="删除" align="left" class="btn" onclick="deleteCurrentRow(\''+secondcount+'\')"/>';
	newTd9.innerHTML = td9;

//根据初始化参数的个数(行数)new 公用插件对象，用户函数选择时，对函数参数的设置
	
	var temp = new jcl_tms.util.FuncInvoke({
				id:'tms'+threecount
		});
		secondfun[threecount] = new Array(temp);

	secondcount++;
	$('#secondcount').val(secondcount);
	threecount++;
	$('#threecount').val(threecount);
	count++;
	$('#count').val(count);
	
	changeRowColor();
	
}

// 引入专家条件
function importRow(blockCondDiv){
	document.getElementById(blockCondDiv).style.display='none';// 隐藏配置条件DIV
	document.getElementById('customcond').style.display='block';// 显示自定义条件DIV
	/*if(document.getElementById('deployeditcond')!=null)
	{
		document.getElementById('deployeditcond').style.display='none';	// 隐藏修改配置条件DIV
	}*/
	customConditionView(blockCondDiv); 
}

// 奇偶行不同颜色
function changeRowColor(){
	 $('#tbody').children('tr').each(function(index){
		if(index == 0) return true;// 第一行是标题拦不参与
		$(this).toggleClass("row-bg-gray", index % 2 != 1);
	});
}

//动态添加条件域中的and和or行

function andSecondRow(andorparam,secondcount){
	 var oTbody =document.getElementById("smalltbody"+secondcount);

	 var rowlength = oTbody.rows.length;
	 var newTr = oTbody.insertRow(rowlength);
	 newTr.setAttribute("id","tr" + threecount.toString());
	 newTr.setAttribute("valign","top");
	 //添加列 
	 var newTd0 = newTr.insertCell(0); 
	 var newTd1 = newTr.insertCell(1); 
	 var newTd2 = newTr.insertCell(2); 
	 var newTd3 = newTr.insertCell(3);
	 newTd0.setAttribute("valign","top");newTd0.setAttribute("align","left");
	 newTd2.setAttribute("valign","top");
	 newTd3.setAttribute("valign","top");
	 newTd2.setAttribute("align","left");
	 newTd3.setAttribute("align","center");
	 var ruleand='and';
	 var ruleor='or';
	 var shownumble=''+secondcount+''+"0000"+''+threecount+'';

	 // 交易属性
	 var td0='<select name="condmapfeature'+shownumble+'" id="condmapfeature'+shownumble+'" style="width: 90px"';
	 td0+=' onchange="rulecondfeatureChange(this,\''+shownumble+'\',\''+secondcount+'\')">';
	 td0+='<option value="" >--请选择--</option>';
	 var txnid = $('select[id=txnid'+secondcount+']').val();
	 var feature = featureList[txnid];
	 for(var i=0;i<feature.length;i++){
	 	if(feature[i]['G2']==-1){//如果是组或渠道，添加为optgroup属性，不能选择               
			td0+='<optgroup label="'+feature[i]['PROPNAME']+'">' + feature[i]['PROPNAME'] + '</optgroup>';
		}else{
			td0+='<option value="'+feature[i]['TXNFEATUREID']+'">'+feature[i]['PROPNAME'] + '</option>';
		} 
	 }	
	 td0+='</select>';
	 if(andorparam==0)
	 {
	   td0="and&nbsp;"+td0;
	 }
	 if(andorparam==1)
	 {
	   td0="or&nbsp;&nbsp;&nbsp;&nbsp;"+td0;
	 } 
	 if(andorparam==-1)
	 {
	   td0="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+td0;
	 }
     newTd0.innerHTML = td0;

	 // 交易属性下拉列表
	 funseDatypeChoice(document.getElementById('condmaptarget'+secondcount),shownumble,featureList[txnid],isPatter[txnid]);

     // 交易函数
	 var td1='<div id="Trafficdiv'+shownumble+'" style="display:block">';
	 td1+='<select name="condmapoperator'+shownumble+'" id="condmapoperator'+shownumble+'" style="width: 70px"';
	 td1+=' onchange="funsecondcountChange(this,\''+shownumble+'\',\''+threecount+'\')">';
	 td1+='<option value="" >--请选择--</option>';
	 td1+='</select>';
	 td1+='</div>';
	 newTd1.innerHTML = td1;

	 var td2='<div id="Trafficdiv2'+shownumble+'" style="display:block;">';
	 td2+='<div id="secondrulecondsdiv'+shownumble+'"></div>'
	 td2+='</div>';
     newTd2.innerHTML = td2;

	 var td3='<div id="Trafficdiv3'+shownumble+'" style="display:block;width: 130px">'
	 td3+='<input type="button" value="and" class="btn" id="andRow'+shownumble+'" onclick="andSecondRow(0,\''+secondcount+'\')"/>';
	 td3+='<input type="button" value="or" class="btn" id="orRow'+shownumble+'" onclick="andSecondRow(1,\''+secondcount+'\')"/>';
	 td3+='<input type="button" value="del" class="btn" id="dellRow'+shownumble+'" onclick="removeRow(\''+secondcount+'\',\''+threecount+'\')"/>';
	 if(andorparam==0)
	 {
	   td3+='<input type="hidden" name="tmsruleandor'+shownumble+'" size="10"   id="tmsruleandor'+shownumble+'" value="'+ruleand+'"/>';
	 }else if(andorparam==1)
	 {
	   td3+='<input type="hidden" name="tmsruleandor'+shownumble+'" size="10"   id="tmsruleandor'+shownumble+'" value="'+ruleor+'"/>';      
	 }else{
		td3+='<input type="hidden" name="tmsruleandor'+shownumble+'" size="10"   id="tmsruleandor'+shownumble+'" value=""/>';
	 }
	 td3+='<input type="hidden" name="tmsrulexmlsecond'+shownumble+'" size="10"   id="tmsrulexmlsecond'+shownumble+'"/>';
	 td3+='</div>'
	 newTd3.innerHTML = td3;
	 var temp = new jcl_tms.util.FuncInvoke({
			id:'tms'+threecount
	 });

	 secondfun[threecount] = new Array(temp);
	 threecount++;
	 $('#threecount').val(threecount); 
	
  }



//刪除一大行
function deleteCurrentRow(secondcount1)
{
  var oTbody = document.getElementById('tbody');
  
  var len = oTbody.rows.length;
  var currRowIndex=event.srcElement.parentNode.parentNode.rowIndex;

  if(len > 2) {
	/*规则修改页面，需要记录需要删除的真实数据*/
	var condmapId = $('#condmapId'+secondcount1).val();// 规则条件对应表主键
	if(condmapId != null && condmapId.length > 0){
		if(confirm('此为真实规则条件，请确定是否删除?')){	
			if(delcondmapaddid.length > 0){
				delcondmapaddid+=',';
			}
			delcondmapaddid+=condmapId;
			$('#delcondmapaddid').val(delcondmapaddid);
		}else{
			return ;
		}
	}

	// 如果删除的是第一行，那么下一行的关联关系单元格为空
	if(currRowIndex == 1){
		oTbody.rows[currRowIndex+1].cells[0].innerHTML = "  ";
	}

	// 删除当前行
	oTbody.deleteRow(currRowIndex);
	count--;
	$('#count').val(count);
	changeRowColor();
  } else 
  {
	alert("请至少保留一行");//如果全部删除了，也可以，就是不好看
  }
}




// 删除一小行
function removeRow(secondcount,threecount)
{
	var shownumblenum=''+secondcount+''+"0000"+''+threecount+'';
	var oTbody =document.getElementById("smalltbody"+secondcount);
    var currRowIndex=event.srcElement.parentNode.parentNode.parentNode.rowIndex;
    if(currRowIndex>0) {
	   oTbody.deleteRow(currRowIndex);
    } else 
    {
    	alert("表达式首行不允许删除");//如果全部删除了，不便规则and or 拼装
    }
}

// 条件对象下拉事件
function funseDatypeChoice(obj,shownumble,featureList1,isPatter1) {
    var shownumble2=shownumble.toString();
	var dataTypeId = obj.value; 

	var o = document.getElementById('condmapfeature'+shownumble2+'');

	if(o==null) return;

	var featureValue = o.value;

	$(o).empty();// 删除下拉列表所有选项
	o.add(new Option("--请选择--",""));

	if(dataTypeId=='UserIdType' && isPatter1 == true)
	{
		// 如果条件对象是用户，并且该交易有行为模式，那么交易属性下拉列表包含HIT、行为模式和其他
		o.add(new Option("--HIT（命中）","HITID"));
		o.add(new Option("--行为模式","PATTERNID"));
		o.add(new Option("--自定义","CUSTOMID"));

	}
	else
	{
		if(dataTypeId!='-1')
		{
			// 除了交易以外的其他条件对象都有HIT选择项
			o.add(new Option("--HIT（命中）","HITID"));	
			o.add(new Option("--自定义","CUSTOMID"));
		}

	}
	
	for(var f=0;featureList1 != null && f<featureList1.length;f++){
		if (featureList1[f]['G2'] == -1) {//如果是组或渠道，添加为optgroup属性，不能选择     
			var group = document.createElement('OPTGROUP');
			group.label = featureList1[f]['PROPNAME'];
			group.innerText = " ";
			o.appendChild(group);
		}
		else {
			o.add(new Option(featureList1[f]['PROPNAME'], featureList1[f]['TXNFEATUREID']));
		}
	}	

	o.value = featureValue;

	if(o.value == ""){
		o.options[0].selected = true;
	}

}


// 条件对象下拉事件, 改变与该条件对象对应的条件域的交易属性下拉数据
function funseDatypeChoiceAll(obj,secondcount) {

	var secondcount2 = secondcount;
	var dataTypeId = obj.value;
	var txnid = $('select[id=txnid'+secondcount+']').val();
	var txnFeature = featureList[txnid];
	var patter = isPatter[txnid];
	for(var i = 0; i <= threecount; i++){

		var shownumble2 = ''+secondcount2+''+"0000"+''+i+'';

		funseDatypeChoice(document.getElementById('condmaptarget'+secondcount),shownumble2,txnFeature,patter);

	}
}


//交易属性选择事件
function rulecondfeatureChange(obj,shownumble,secondcount) {
    var shownumble2=shownumble.toString();
	var feateretype = obj.value;
	var txnid = document.getElementById("txnid"+secondcount+'').value;
	var condmaptarget = document.getElementById("condmaptarget"+secondcount+'').value;
	var o = document.getElementById("condmapoperator"+shownumble2+'');
	o.length = 0;// 下拉数据清空
	document.getElementById("secondrulecondsdiv"+shownumble2+'').innerHTML = "";// json清空
	
	var feature = 'condition';
	var datatype = 'all';
	var module = 'rule';
	//hit
	if(feateretype=='HITID')
	{
		feature = "hit";

		// 下拉变灰
		o.disabled = "disabled";
		// 增、删、改按钮变灰
		document.getElementById("andRow"+shownumble2+'').disabled = "disabled";
		document.getElementById("orRow"+shownumble2+'').disabled = "disabled";
		document.getElementById("dellRow"+shownumble2+'').disabled = "disabled";
	
	}
	else{
		if(feateretype=='PATTERNID'){
			feature = 'pattern';
		}else if(feateretype=='CUSTOMID'){
			feature = 'custom';
			datatype = condmaptarget;// 属性的数据类型
		}
		else if (feateretype != null && feateretype!='null' && feateretype!='')
		{
			var txnObj = getTxnObjByTxnId(feateretype,txnid);
			var singlePattern = txnObj['ISSINGLE']; // 是否单个行为模式
			var istxncount = txnObj['ISTXNCOUNT'];// 是否统计
			var datatype = txnObj['DATATYPE'];// 属性的数据类型

			if(singlePattern == 'YES'){
				feature += ',pattern';
			}
			if(istxncount == 'YES'){
				feature += ',store';
			}
		}

		// 下拉恢复正常
		o.disabled = "";
		// 增、删、改按钮恢复正常
		document.getElementById("andRow"+shownumble2+'').disabled = "";
		document.getElementById("orRow"+shownumble2+'').disabled = "";
		document.getElementById("dellRow"+shownumble2+'').disabled = "";

		o.add(new Option("--请选择--",""));

	}

	if(feateretype != null && feateretype!='null' && feateretype!='')
	{
		var funcList1 = getFuncList(feature,datatype,module);
		for(var i = 0; i<funcList1.length; i++){
			o.add(new Option(funcList1[i]['FUNCNAME'],funcList1[i]['FUNCID']));
		}
	}
	
}


//函数下拉事件,调用公共组件的方法显示JSON串
function funsecondcountChange(obj,shownumble,threecount) {
    var shownumble2=shownumble.toString();
	var funcId = "funId="+obj.value;
	jcl.postJSON('/tms/mgr/fun/getFunJson',funcId, function(data){
		secondfun[threecount][0].setJson(data.row);
		secondfun[threecount][0].show('secondrulecondsdiv'+shownumble2);
	});
	

}



// 获取函数下拉列表
function getFuncList(feature,functype,module){
	// funcList
	var result = new Array();
	for(var i = 0; i<funcList.length; i++){
		if(feature.indexOf(funcList[i]['FEATURE']) >= 0 && funcList[i]['MODULE'] == module && (funcList[i]['FUNCTYPE'] == 'all' || funcList[i]['FUNCTYPE'].indexOf(functype) >= 0)){
			result.push(funcList[i]);
		}
	}
	return result;
}

// 通过交易ID查询交易信息
function getTxnObjByTxnId(feateretype,txnid){
	var featureList1 = featureList[txnid];
	for(var i = 0; i < featureList1.length; i++){
		if(feateretype == featureList1[i]['TXNFEATUREID']){
			return featureList1[i];	
		}
	}
	return null;
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
