//UTF-8 encoding
var dataTypeList;//数据类型列表
var specialDateType=',UserIdType,AccoutFromType,AccoutToType,DeviceIdType,IpType,';//五大数据类型
var usedDateType='';//本交易已经配置过的五大类型 
var usedStoreColumn = '';

var storeColumList;//存储字段列表
var count =0;//自定义属性行数标识
var objcount=0;//初始化属性行数标识
var objtmsfun;//初始化属性js对象组
var custmsfun;//自定义属性js对象组
var hideList;

var objfeatureParm=new Array();//初始化交易识别函数属性组
var objtransParm=new Array();//初始化交易转换函数属性组

var cusfeatureParm=new Array();//自定义交易识别函数属性组
var custransParm=new Array();//自定义交易转换函数属性组

var cusstorecolumn='';//自定义属性已经配置的存储字段


var objfeature=new Array() ;//标记需要隐藏的元数据过滤函数下拉框的行号
var objstore=new Array();//标记需要隐藏的元数据转换函数下拉框的行号
var cousfeature=new Array() ;//标记需要隐藏的自定义过滤函数下拉框的行号
var cousstore=new Array();//标记需要隐藏的自定义转换函数下拉框的行号

$(document).ready(function(){
	
	var txnId = jQuery.url.param("txnId");
	var param = "txnId="+txnId;
	var qtype = jQuery.url.param("qtype");
	var configurl = '/tms/dp/txnView/config';
	if(qtype=='new'){
		configurl = '/tms/dp/txn/config';
	}
	
	jcl.postJSON(configurl, param, function(data){

		$('#txnfid').val(data.row.TXNID);
		var parentname = data.row.PARENTNAME;
		var txnname = data.row.TXNNAME;
		var channelName = data.row.CHANNELNAME;
		$('#txnfname').text(txnname);
		$('#txnffname').text(parentname);
		$('#versionId').val(data.row.VERSIONID);


		$('#txn-paramForm').show();
		var tabBox = new jcl.ui.Box({
			title:channelName+'-'+parentname+'-'+txnname+' '+jcl.message.get('ui.tms.feature.configtitle'),
			id:'confinfo'
		});
		tabBox.addDom('txn-paramForm');
		
		jcl.code.selector('txntype','tms.common.txntype',{text:'--请选择--',value:''});
		
			
			if (data.row.STATUS == '0'){
				$('#status').text(jcl.message.get('ui.tms.traffic.false'));
			}else{
				$('#status').text(jcl.message.get('ui.tms.traffic.true'));
			}
			
			var featurelist = data.featurelist;
			var condsList = data.condsList;
			var storList = data.storList;
			var objgrpList = data.objgrpList;
			hideList = data.hideList;//交易模版中隐藏属性数据
			dataTypeList = data.dataTypeList;
			storeColumList = data.storeColumList;
			usedDateType=data.usedDateType;		//配置过的数据类型
			usedStoreColumn = data.usedStoreColumn; //配置过的存储字段
			//var objMap = data.objMap;
			//如果交易属性版本下有交易类别BEGIN
			if(objgrpList!=null && objgrpList.length>0){
				var tabHtml=$('#conftab').html();
				tabHtml=tabHtml.substring(0,tabHtml.length-8);
				
				for(var m=0;m<dataTypeList.length;m++){
					tabHtml+='<input id="parent'+dataTypeList[m]["DATATYPE"]+'" value="'+dataTypeList[m]["PARENTDATATYPE"]+'" type="hidden" />';
				}
				
				var grpIdStr="";
				var grpNameStr="";
				for(var i=0;i<objgrpList.length;i++){
					grpIdStr+=objgrpList[i]['METAOBJID']+",";
					grpNameStr+=objgrpList[i]['OBJNAME']+",";
				}
				var grpId = grpIdStr.substring(0,grpIdStr.length-1).split(",");
				var grpName = grpNameStr.substring(0,grpNameStr.length-1).split(",");
				var customflag=true;
				for(var i=0;i<grpId.length;i++){
					grp=grpId[i];
					if(objgrpList[i]['ISDISPLAY']=='0'){//如果交易组设置为不显示，忽略此组信息，继续下一组循环
						continue;
					}
					// 生成每个分组的标题行
					tabHtml+='<tr style="font-weight: bold;"  bgcolor="#EDEDED" ><td  valign="top"  align="left" colspan="9" >';
					tabHtml+='<span id="span'+grp+'" style="cursor: pointer;" onclick="displayInitTr(\''+grp+'\')"> - </span>'+grpName[i]+'</td></tr>';
				
					
					tabHtml+='<tbody id="tbody'+grp+'">';
					//如果交易类别下有属性
					if(featurelist!=null && featurelist.length>0){
						for(var j=0;j<featurelist.length;j++){
						if(grp==featurelist[j]['TXNPARAMGRPID']){//属于组
							//元数据并且不隐藏
							if((featurelist[j]['PARAMSOURCE']=='0' || featurelist[j]['TXNPARAMID']!=null)  
								&& (featurelist[j]['RWCONTROL']=='2' ||featurelist[j]['RWCONTROL']=='3' )){	
								
								tabHtml+='<tr ><td  valign="top"  align="right">'+featurelist[j]['TXNPARAMNAME']+':</td>';
								
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input name="OBJTXNPARAMNAME'+objcount+'" type="hidden" value="'+featurelist[j]['TXNPARAMNAME']+'" id="OBJTXNPARAMNAME'+objcount+'" />';
								// 生成数据来源 数据映射的输入框
								tabHtml+='<input  type="text" size="10" disabled="disabled" value="'+featurelist[j]['TXNPARAMSOURCE']+'"  />';
								tabHtml+='<input name="OBJTXNPARAMSOURCE'+objcount+'" type="hidden" value="'+featurelist[j]['TXNPARAMSOURCE']+'" id="OBJTXNPARAMSOURCE'+objcount+'" />';
								tabHtml+='</td>';
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input  type="text" size="10" disabled="disabled" value="'+featurelist[j]['TXNPARAMCODE']+'"  />';
								tabHtml+='<input name="OBJTXNPARAMCODE'+objcount+'" type="hidden" value="'+featurelist[j]['TXNPARAMCODE']+'" id="OBJTXNPARAMCODE'+objcount+'" />';
								tabHtml+='<input name="OBJRWCONTROL'+objcount+'" type="hidden" value="'+featurelist[j]['RWCONTROL']+'" id="OBJRWCONTROL'+objcount+'" />';
								tabHtml+='<input name="OBJTXNFEATUREID'+objcount+'" type="hidden" value="'+featurelist[j]['TXNFEATUREID']+'" id="OBJTXNFEATUREID'+objcount+'" />';
								tabHtml+='</td>';
								
								tabHtml+='<td  valign="top"  align="center">';
								var paramtype='';//标记与函数类型做匹配
								var columntype='';//标记与存储字段类型做匹配
								// 数据类型
									for(var k=0;k<dataTypeList.length;k++){
										if(featurelist[j]['TXNPARAMTYPE']==dataTypeList[k]['DATATYPE']){
											paramtype=dataTypeList[k]['DATATYPE'];
											if(dataTypeList[k]['PARENTDATATYPE']=="0"){
												columntype=dataTypeList[k]['DATATYPE'];
											}else{
												columntype=dataTypeList[k]['PARENTDATATYPE'];
											}
											// 只读则显示数据类型名称
											tabHtml+='<input  type="text" size="10" disabled="disabled" value="'+dataTypeList[k]['DATATYPENAME']+'"  />';
											break;
										}
									}	
								
								tabHtml+='</td>';
								tabHtml+='<td  valign="top"  align="left" >';
								
								// 过滤条件
								tabHtml+='<select name="OBJFEATUREFUNCID'+objcount+'" id="OBJFEATUREFUNCID'+objcount+'" style="width: 85px" disabled="disabled" >';
								tabHtml+='<option value="" >--请选择--</option>';
								var objff=false;
								for(var k=0;k<condsList.length;k++){
									if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(paramtype.toUpperCase())>=0){
										objff=true;
										if(condsList[k]['FUNCID']==featurelist[j]['FEATUREFUNCID']){
											tabHtml+='<option value="'+condsList[k]['FUNCID']+'" selected="selected">'+condsList[k]['FUNCNAME']+'</option>';
										}else{
											tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
										}
									}						
								}
								tabHtml+='</select>';
								tabHtml+='<div style="width: 5px" id="condsdiv'+objcount+'"></div></td>';
								if(!objff){
									objfeature[objfeature.length]=objcount;
								}
								objff=false;
								
								// 表达式
								tabHtml+='<td  valign="top"  align="left">';
								tabHtml+='<select name="OBJTRANSFUNCID'+objcount+'" id="OBJTRANSFUNCID'+objcount+'" style="width: 85px" disabled="disabled" ';
								tabHtml+='onchange="funObjChange(this,'+objcount+',1)">';
								tabHtml+='<option value="" >--请选择--</option>';
								var objsf=false;
								for(var k=0;k<storList.length;k++){	
									if(storList[k]['FUNCTYPE']=='all' || storList[k]['FUNCTYPE'].toUpperCase().indexOf(paramtype.toUpperCase())>=0){
										objsf=true;
										if(storList[k]['FUNCID']==featurelist[j]['TRANSFUNCID']){
											tabHtml+='<option value="'+storList[k]['FUNCID']+'" selected="selected">'+storList[k]['FUNCNAME']+'</option>';
										}else{
											tabHtml+='<option value="'+storList[k]['FUNCID']+'">'+storList[k]['FUNCNAME']+'</option>';
										}
									
									}						
								}
								tabHtml+='</select><div style="width: 5px" id="storediv'+objcount+'"></div></td>';
								if(!objsf){
									objstore[objstore.length]=objcount;
								}
								objsf=false;
								
								tabHtml+='<td  valign="top"  align="center">';
								
								// 存储字段
									for(var m=0;m<storeColumList.length;m++){
										if(storeColumList[m]['PROPCODE']==featurelist[j]['STORECOLUMN']){
											tabHtml+='<input  type="text" size="10"  disabled="disabled" value="'+storeColumList[m]['PROPNAME']+'" />';
											tabHtml+='<input name="OBJSTORECOLUMN'+objcount+'" type="hidden" size="10" id="OBJSTORECOLUMN'+objcount+'"  value="'+storeColumList[m]['PROPCODE']+'" />'
											break;
										}					
								}
								
								tabHtml+='</td>';
								tabHtml+='<td  valign="top"  align="center">';
								
								// 是否统计
								if(featurelist[j]['ISSTAT']=="0"){
									tabHtml+='&nbsp;<input type="checkbox" name="OBJISSTAT'+objcount+'" size="10" value="1" id="OBJISSTAT'+objcount+'"  />&nbsp;';
								}
								else{
									tabHtml+='&nbsp;<input type="checkbox" name="OBJISSTAT'+objcount+'" size="10" value="1" id="OBJISSTAT'+objcount+'" checked="checked" />&nbsp;';
								}
								
								
								tabHtml+='<input type="hidden" name="OBJFEATUREPARAM'+objcount+'" size="10" value=""  id="OBJFEATUREPARAM'+objcount+'"  />';
								tabHtml+='<input type="hidden" name="OBJTRANSPARAM'+objcount+'" size="10"   value=""  id="OBJTRANSPARAM'+objcount+'"  />';
								
								tabHtml+='</td>';
								
								// 增加排序字段 maxiao 2011-10-17
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input type="text" name="OBJORDERBY'+objcount+'" style="width:25px" size="3"   value="'+featurelist[j]['ORDERBY']+'"  id="OBJORDERBY'+objcount+'"  />';
								
								var tempconds = {};
								var t = 'condsdiv'+objcount+'#'+featurelist[j]['FEATUREPARAM'];
								tempconds["conds"]=t;
								objfeatureParm[objcount]=tempconds;

								var tempstore = {};
								var st = 'storediv'+objcount+'#'+featurelist[j]['TRANSPARAM'];
								tempstore["store"]=st;
								objtransParm[objcount]= tempstore;
								
								tabHtml+='</td></tr>';
								objcount++;
							}
							//自定义属性
							else if(featurelist[j]['PARAMSOURCE']=='1' && featurelist[j]['TXNPARAMID']==null){	
								tabHtml+='<tr ><td  valign="top"  align="center">';
								
								tabHtml+='<input name="TXNPARAMGRPID'+count+'" type="hidden" size="10"  id="TXNFEATUREID'+count+'" value="'+grp+'"/>';
								tabHtml+='<input name="TXNPARAMGRPNAME'+count+'" type="hidden" size="10"  id="TXNFEATUREID'+count+'" value="'+grpName[i]+'"/>';
								
								tabHtml+='<input name="TXNFEATUREID'+count+'" type="hidden" size="10"  id="TXNFEATUREID'+count+'" value="'+featurelist[j]['TXNFEATUREID']+'"/>';
								tabHtml+='<input type="checkbox" name="ck'+grp+'" size="10" id=""  value="'+featurelist[j]['TXNFEATUREID']+'" />';
								tabHtml+='<input name="TXNPARAMNAME'+count+'" type="text" size="10"  id="TXNPARAMNAME'+count+'" value="'+featurelist[j]['TXNPARAMNAME']+'" disabled="disabled" /></td>';
								// 数据来源 maxiao 2011-10-12
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input name="TXNPARAMSOURCE'+count+'" type="text" size="10"  id="TXNPARAMSOURCE'+count+'" value="'+featurelist[j]['TXNPARAMSOURCE']+'" disabled="disabled" /></td>';	
								// 数据映射
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input name="TXNPARAMCODE'+count+'" type="text" size="10"  id="TXNPARAMCODE'+count+'" value="'+featurelist[j]['TXNPARAMCODE']+'" disabled="disabled" /></td>';
								var paramtype='';//标记与函数类型做匹配
								var columntype='';//标记与存储字段类型做匹配
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<select name="cusparamtype'+count+'" id="cusparamtype'+count+'" style="width: 85px" disabled="disabled" >';
								tabHtml+='<option value="" >--请选择--</option>';
								//循环显示数据类型下拉框
								for(var k=0;k<dataTypeList.length;k++){
									if(usedDateType.indexOf(dataTypeList[k]['DATATYPE'])<0){			//数据类型不在已经配置的五大数据类型中
										if(dataTypeList[k]['DATATYPE']==featurelist[j]['TXNPARAMTYPE']){//当前交易特征已经配置的数据类型
											tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'" selected="selected" >'+dataTypeList[k]['DATATYPENAME']+'</option>';
											paramtype=dataTypeList[k]['DATATYPE'];
											if(dataTypeList[k]['PARENTDATATYPE']=="0"){
												columntype=dataTypeList[k]['DATATYPE'];
											}
											else{
												columntype=dataTypeList[k]['PARENTDATATYPE'];
											}
										}else{
											tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'">'+dataTypeList[k]['DATATYPENAME']+'</option>';
										}
									}else if(dataTypeList[k]['DATATYPE']==featurelist[j]['TXNPARAMTYPE']){
										//如果当前交易特征的数据类型属于五大数据类型，也会被标记为已使用，那么在第一步中会被过滤掉，所以这一判断相当于将自己除外
										tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'" selected="selected" >'+dataTypeList[k]['DATATYPENAME']+'</option>';

										paramtype=dataTypeList[k]['DATATYPE'];
										if(dataTypeList[k]['PARENTDATATYPE']=="0"){
											columntype=dataTypeList[k]['DATATYPE'];
										}else{
											columntype=dataTypeList[k]['PARENTDATATYPE'];
										}
									}
								}	
								tabHtml+='</select></td>';			
								tabHtml+='<td  valign="top"  align="left">';
								tabHtml+='<select name="cusfeature'+count+'" id="cusfeature'+count+'" style="width: 85px" disabled="disabled" >';
								tabHtml+='<option value="" >--请选择--</option>';
								var cusf=false;//标记条件函数是否需要隐藏
								for(var k=0;k<condsList.length;k++){
									if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(paramtype.toUpperCase())>=0){
										cusf=true;
										if(condsList[k]['FUNCID']==featurelist[j]['FEATUREFUNCID']){
											tabHtml+='<option value="'+condsList[k]['FUNCID']+'" selected="selected">'+condsList[k]['FUNCNAME']+'</option>';
										}else{
											tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
										}
									}					
								}
								tabHtml+='</select>';
								if(!cusf){
									cousfeature[cousfeature.length]=count;
								}
								
								tabHtml+='<div id="cuscondsdiv'+count+'"></div></td>'

								tabHtml+='<td  valign="top"  align="left">';
								tabHtml+='<select name="storefeature'+count+'" id="storefeature'+count+'" style="width: 85px" disabled="disabled" >';
								tabHtml+='<option value="" >--请选择--</option>';
								var storef=false;//标记转换函数是否需要隐藏
								for(var k=0;k<storList.length;k++){	
									if(storList[k]['FUNCTYPE']=='all' || storList[k]['FUNCTYPE'].toUpperCase().indexOf(paramtype.toUpperCase())>=0){
										storef=true;
										if(storList[k]['FUNCID']==featurelist[j]['TRANSFUNCID']){
											tabHtml+='<option value="'+storList[k]['FUNCID']+'" selected="selected">'+storList[k]['FUNCNAME']+'</option>';
										}else{
											tabHtml+='<option value="'+storList[k]['FUNCID']+'">'+storList[k]['FUNCNAME']+'</option>';
										}
									} 					
								}
								tabHtml+='</select>';
								if(!storef){
									cousstore[cousstore.length]=count;
								}
								tabHtml+='<div id="cusstorediv'+count+'"></div></td>';
													
								tabHtml+='<td  valign="top"  align="center">';
								var tcolumn='';
								tabHtml+='<select name="STORECOLUMN'+count+'" id="STORECOLUMN'+count+'" style="width: 85px" >';
								tabHtml+='<option value="" >--请选择--</option>';
								//循环显示存储字段
								for(var k=0;k<storeColumList.length;k++){
									if(columntype==storeColumList[k]['DATATYPE']){//首先属于当前交易特征的数据类型
										//已经配置过的存储字段，不再显示
										if(usedStoreColumn.indexOf(storeColumList[k]['PROPCODE'])<0){
											if(storeColumList[k]['PROPCODE']==featurelist[j]['STORECOLUMN']){
												cusstorecolumn+=storeColumList[k]['PROPCODE']+",";
												tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'" selected="selected">'+storeColumList[k]['PROPNAME']+'</option>';
												tcolumn=storeColumList[k]['PROPCODE'];
											}else{
												tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'"  >'+storeColumList[k]['PROPNAME']+'</option>';
											}
										}else if(storeColumList[k]['PROPCODE']==featurelist[j]['STORECOLUMN']){
											//当前交易特征配置的存储字段也会被标记为已经配置，所以第二步的过滤中会被过滤掉
											cusstorecolumn+=storeColumList[k]['PROPCODE']+",";
											tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'" selected="selected">'+storeColumList[k]['PROPNAME']+'</option>';
											tcolumn=storeColumList[k]['PROPCODE'];
										}
									}			
								}
								tabHtml+='</select>';
								tabHtml+='<input type="hidden" value="'+tcolumn+'" id="tcolumn'+count+'" name="tcolumn'+count+'" />';
								tabHtml+='</td>';
								
								tabHtml+='<td  valign="top"  align="center">';
								if(featurelist[j]['ISSTAT']=='1'){
									tabHtml+='&nbsp;<input type="checkbox" name="ISSTAT'+count+'"size="10" id="ISSTAT'+count+'" value="1" checked="checked"  disabled="disabled"  />&nbsp;';
								}else{
									tabHtml+='&nbsp;<input type="checkbox" name="ISSTAT'+count+'"size="10" id="ISSTAT'+count+'" value="1"  disabled="disabled"  />&nbsp;';
								}
								
								tabHtml+='<input type="hidden" name="FEATUREPARAM'+count+'" size="10"  id="FEATUREPARAM'+count+'"  />';
								tabHtml+='<input type="hidden" name="TRANSPARAM'+count+'" size="10"   id="TRANSPARAM'+count+'"  />';
								
								// 增加排序字段  maxiao 2011-10-17 
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input type="text" name="ORDERBY'+count+'" style="width:25px" size="3"  id="ORDERBY'+count+'" value="'+featurelist[j]['ORDERBY']+'" disabled="disabled"/>';
								
								var tempconds = {};
								var t = 'cuscondsdiv'+count+'#'+featurelist[j]['FEATUREPARAM'];
								tempconds["conds"]=t;
								cusfeatureParm[count]=tempconds;
								
								var tempstore = {};
								var st = 'cusstorediv'+count+'#'+featurelist[j]['TRANSPARAM'];
								tempstore["store"]=st;
								custransParm[count]= tempstore;
								
								tabHtml+='</td>';
								tabHtml+='</tr>';
								count++;
								customflag=false;
							}
						}
					}
					}
					$('#objcount').val(objcount);

					$('#count').val(count);
					tabHtml+="</tbody>";
				}
				
				
				$('#conftab').html(tabHtml); 
											
				//根据初始化属性的个数(行数)new 公用插件对象，用户函数选择时，对函数属性的设置
				objtmsfun=new Array(objcount);
				for(var i=0;i<objcount;i++){
					var temp = new jcl_tms.util.FuncInvoke({
						id:'temp'+i
					});
					var temp1 = new jcl_tms.util.FuncInvoke({
						id:'temp1'+i
					});

					//条件函数属性数组
					if(objfeatureParm[i]!=null && objfeatureParm[i]!=''){
						var featureParm = objfeatureParm[i]["conds"].toString().split('#');
						if(featureParm[1]!=null && featureParm[1]!='null'  && featureParm[1]!=''){
							var divname = featureParm[0];
							var json = featureParm[1];
							temp.setJson(json);
							temp.show(divname,'edit'); 
							// 表格宽度不够用，直接采用写html的方式生成input maxiao 2011-10-12
							
							
						}
					}
					//转换函数属性数组
					if(objtransParm[i]!=null && objtransParm[i]!=''){
						var transParm = objtransParm[i]["store"].toString().split('#');
						if(transParm[1]!=null && transParm[1]!='null' && transParm[1]!=''){
							objtransParm[i]["store"]
							var divname = transParm[0];
							var json = transParm[1];
							temp1.setJson(json);
							temp1.show(divname,'edit'); 
						}
					}
					
					objtmsfun[i] = new Array(temp,temp1);
					
				}
				//根据自定义属性的个数(行数)new 公用插件对象，用户函数选择时，对函数属性的设置
				custmsfun=new Array();
				for(var i=0;i<count;i++){
					var custemp = new jcl_tms.util.FuncInvoke({
						id:'tms'+i
					});
					var custemp1 = new jcl_tms.util.FuncInvoke({
						id:'tms'+i
					});
					
					if(!customflag){		//如果存在自定义属性，则循环赋值
						//条件函数属性数组	
						if(cusfeatureParm[i]!=null && cusfeatureParm[i]!=''){
							var featureParm = cusfeatureParm[i]["conds"].toString().split('#');
							if(featureParm[1]!=null && featureParm[1]!='null' && featureParm[1]!=''){
								var divname = featureParm[0];
								var json = featureParm[1];
								custemp.setJson(json);
								custemp.show(divname,'edit'); 
							}
						}
						//转换函数属性数组
						if(custransParm[i]!=null && custransParm[i]!=''){
							var transParm = custransParm[i]["store"].toString().split('#');
							if(transParm[1]!=null && transParm[1]!='null' && transParm[1]!=''){
								var divname = transParm[0];
								var json = transParm[1];
								custemp1.setJson(json); 
								custemp1.show(divname,'edit'); 
							}
						}
					}
					
					custmsfun[i] = new Array(custemp,custemp1);
					
				}
				
			}//交易属性版本下是否有交易属性类别END		

			//隐藏元数据属性中需要隐藏的过滤函数  
			for(var m=0;m<objfeature.length;m++){
				funObjChange($('#OBJFEATUREFUNCID'+objfeature[m]),''+objfeature[m],'0');//将条件函数值设置为空
				$('#OBJFEATUREFUNCID'+objfeature[m]).hide();
			} 
			//隐藏元数据属性中需要隐藏的转换函数  
			for(var m=0;m<objstore.length;m++){
				funObjChange($('#OBJTRANSFUNCID'+objstore[m]),''+objstore[m],'1');//将转换函数值设置为空
				$('#OBJTRANSFUNCID'+objstore[m]).hide();
			} 

			//隐藏自定义属性中需要隐藏的过滤函数  
			for(var m=0;m<cousfeature.length;m++){
				funcusChange($('#cusfeature'+cousfeature[m]),''+cousfeature[m],'0');//将条件函数值设置为空
				$('#cusfeature'+cousfeature[m]).hide();
			}
			//隐藏自定义属性中需要隐藏的转换函数  
			for(var m=0;m<cousstore.length;m++){
				funcusChange($('#storefeature'+cousstore[m]),''+cousstore[m],'1');//将转换函数值设置为空
				$('#storefeature'+cousstore[m]).hide();
			}
			
			
	});

	
	$('#txncancelButton').click(function(){
		window.close();
		//window.parent.document.getElementById("ifram-box").style.display="none";
	});
	
	
});

//初始化--函数数据选择事件
function funObjChange(obj,objcount,paratype) {
	var funcId = "funId="+obj.value;
	if(obj.value==""){
		if(paratype==0){
			$('#condsdiv'+objcount).html("");
			objtmsfun[objcount][0].setJson('');
			objtmsfun[objcount][0].show('condsdiv'+objcount,'add');
		}else{
			$('#storediv'+objcount).html("");
			objtmsfun[objcount][1].setJson('');
			objtmsfun[objcount][1].show('storediv'+objcount,'add');
		}
		return;
	}
	
	jcl.postJSON('/tms/mgr/fun/getFunJson',funcId, function(data){
		
		if(paratype==0){
			objtmsfun[objcount][0].setJson(data.row);
			objtmsfun[objcount][0].show('condsdiv'+objcount,'add');
		}else{
			objtmsfun[objcount][1].setJson(data.row);
			objtmsfun[objcount][1].show('storediv'+objcount,'add');
		}
	});
	
}
//自定义--函数选择事件
function funcusChange(obj,count,paratype) {
	var funcId = "funId="+obj.value;
	if(obj.value==''){
		if(paratype==0){
			$('#cuscondsdiv'+count).html("");
			custmsfun[count][0].setJson('');
			custmsfun[count][0].show('cuscondsdiv'+count,'add');
		}else{
			$('#cusstorediv'+count).html("");
			custmsfun[count][1].setJson('');
			custmsfun[count][1].show('cusstorediv'+count,'add');
		}
		return;
	}
	jcl.postJSON('/tms/mgr/fun/getFunJson',funcId, function(data){
		if(paratype==0){
			custmsfun[count][0].setJson(data.row);
			custmsfun[count][0].show('cuscondsdiv'+count,'add');
		}else{
			custmsfun[count][1].setJson(data.row);
			custmsfun[count][1].show('cusstorediv'+count,'add');
		}
	});
}
//判断重复：str是否在existStr中出现
function checkRepeat(existStr,str){
//	var strs = existStr.split(',');
//	for(var i=0;i<strs.length;i++){
//		if(strs[i]==str){
//			return false;
//		}
//	}
	if(existStr.indexOf(','+str+',')>-1)return false;
	return true;
}


//直接生成一个table，含有一个input或者select
function creatSubDiv(temp,divid,oper){

			
}



