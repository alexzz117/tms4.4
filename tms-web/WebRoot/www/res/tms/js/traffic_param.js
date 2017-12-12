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
	
	var txnId = "txnId="+jQuery.url.param("txnId");
	var param = txnId+"&oper=temp";
	
	jcl.postJSON('/tms/dp/txn/config', param, function(data){

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
					if(objgrpList[i]['ISEXTEND']=='0'){
						tabHtml+='<tr style="font-weight: bold;"  bgcolor="#EDEDED" ><td  valign="top"  align="left" colspan="9" >';
						tabHtml+='<span id="span'+grp+'" style="cursor: pointer;" onclick="displayInitTr(\''+grp+'\')"> - </span>'+grpName[i]+'</td></tr>';
					}
					else{
						tabHtml+='<tr style="font-weight: bold;"  bgcolor="#EDEDED" ><td  valign="top"  align="left" colspan="6" >';
						tabHtml+='<span id="span'+grp+'" style="cursor: pointer;" onclick="displayInitTr(\''+grp+'\')"> - </span>'+grpName[i]+'</td>';
						tabHtml+='<td  valign="top"  align="right" colspan="3">';
						
						tabHtml+='<input type="button" value="添加行" align="left" class="btn" id="addRow" onclick="addCusRow(\''+grp+'\',\''+grpName[i]+'\')" />  ';
						tabHtml+='<input type="button" value="删除行" align="right" class="btn" id="delRow" onclick="delCusRow(\''+grp+'\')"/>';
						tabHtml+='</tr>';
					}
					
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
								if(featurelist[j]['RWCONTROL']=='2'){//只读
									tabHtml+='<input  type="text" size="10" disabled="disabled" value="'+featurelist[j]['TXNPARAMSOURCE']+'"  />';
									tabHtml+='<input name="OBJTXNPARAMSOURCE'+objcount+'" type="hidden" value="'+featurelist[j]['TXNPARAMSOURCE']+'" id="OBJTXNPARAMSOURCE'+objcount+'" />';
									tabHtml+='</td><td  valign="top"  align="center">';
									tabHtml+='<input  type="text" size="10" disabled="disabled" value="'+featurelist[j]['TXNPARAMCODE']+'"  />';
									tabHtml+='<input name="OBJTXNPARAMCODE'+objcount+'" type="hidden" value="'+featurelist[j]['TXNPARAMCODE']+'" id="OBJTXNPARAMCODE'+objcount+'" />';
								}
								else if(featurelist[j]['RWCONTROL']=='3'){//读写
									tabHtml+='<input name="OBJTXNPARAMSOURCE'+objcount+'" type="text" size="10" value="'+featurelist[j]['TXNPARAMSOURCE']+'" id="OBJTXNPARAMSOURCE'+objcount+'" />';
									tabHtml+='</td><td  valign="top"  align="center">';
									tabHtml+='<input name="OBJTXNPARAMCODE'+objcount+'" type="text" size="10" value="'+featurelist[j]['TXNPARAMCODE']+'" id="OBJTXNPARAMCODE'+objcount+'" />';
								}	
									
								tabHtml+='<input name="OBJRWCONTROL'+objcount+'" type="hidden" value="'+featurelist[j]['RWCONTROL']+'" id="OBJRWCONTROL'+objcount+'" />';
								
								tabHtml+='<input name="OBJTXNFEATUREID'+objcount+'" type="hidden" value="'+featurelist[j]['TXNFEATUREID']+'" id="OBJTXNFEATUREID'+objcount+'" /></td>';
								
								tabHtml+='<td  valign="top"  align="center">';
								var paramtype='';//标记与函数类型做匹配
								var columntype='';//标记与存储字段类型做匹配
								// 数据类型
								if(featurelist[j]['RWCONTROL']=='2'){//只读
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
								}
								else if(featurelist[j]['RWCONTROL']=='3'){//读写 
									tabHtml+='<select name="OBJTXNPARAMTYPE'+objcount+'" id="OBJTXNPARAMTYPE'+objcount+'" onchange="selectFuncByType('+objcount+',\'edit\')" >'
									tabHtml+='<option value="">--请选择--</option>';
									for(var k=0;k<dataTypeList.length;k++){
										//循环显示数据类型的选择项
										if(usedDateType.indexOf(dataTypeList[k]['DATATYPE'])<0){			//数据类型不在已经配置的五大数据类型中
											if(featurelist[j]['TXNPARAMTYPE']==dataTypeList[k]['DATATYPE']){
												paramtype=dataTypeList[k]['DATATYPE'];
												if(dataTypeList[k]['PARENTDATATYPE']=="0"){
													columntype=dataTypeList[k]['DATATYPE'];
												}else{
													columntype=dataTypeList[k]['PARENTDATATYPE'];
												}
												tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'" selected="selected">'+dataTypeList[k]['DATATYPENAME']+'</option>';
											}else{
												tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'">'+dataTypeList[k]['DATATYPENAME']+'</option>';
											}
										}else if(featurelist[j]['TXNPARAMTYPE']==dataTypeList[k]['DATATYPE']){
											//如果当前交易特征的数据类型属于五大数据类型，也会被标记为已使用，那么在第一步中会被过滤掉，所以这一判断相当于将自己除外
											paramtype=dataTypeList[k]['DATATYPE'];
											if(dataTypeList[k]['PARENTDATATYPE']=="0"){
												columntype=dataTypeList[k]['DATATYPE'];
											}else{
												columntype=dataTypeList[k]['PARENTDATATYPE'];
											}
											tabHtml+='<option value="'+dataTypeList[k]['DATATYPE']+'" selected="selected">'+dataTypeList[k]['DATATYPENAME']+'</option>';
										}
										
									}	
									tabHtml+='</select>';
								}
								
								tabHtml+='</td>';
								tabHtml+='<td  valign="top"  align="left" >';
								
								// 过滤条件
								tabHtml+='<select name="OBJFEATUREFUNCID'+objcount+'" id="OBJFEATUREFUNCID'+objcount+'" style="width: 85px" ';
								tabHtml+=' onchange="funObjChange(this,'+objcount+',0)">';
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
								tabHtml+='<select name="OBJTRANSFUNCID'+objcount+'" id="OBJTRANSFUNCID'+objcount+'" style="width: 85px" ';
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
								if(featurelist[j]['RWCONTROL']=='2'){//只读
									for(var m=0;m<storeColumList.length;m++){
										if(storeColumList[m]['PROPCODE']==featurelist[j]['STORECOLUMN']){
											tabHtml+='<input  type="text" size="10"  disabled="disabled" value="'+storeColumList[m]['PROPNAME']+'" />';
											tabHtml+='<input name="OBJSTORECOLUMN'+objcount+'" type="hidden" size="10" id="OBJSTORECOLUMN'+objcount+'"  value="'+storeColumList[m]['PROPCODE']+'" />'
											break;
										}					
									}		
								}
								else if(featurelist[j]['RWCONTROL']=='3'){//读写
									var temp='';
									tabHtml+='<select name="OBJSTORECOLUMN'+objcount+'" id="OBJSTORECOLUMN'+objcount+'" style="width: 85px" >'
									tabHtml+='<option value="">--请选择--</option>';
									for(var m=0;m<storeColumList.length;m++){
										if(columntype==storeColumList[m]['DATATYPE']){
											if(usedStoreColumn.indexOf(storeColumList[m]['PROPCODE'])<0){
												if(storeColumList[m]['PROPCODE']==featurelist[j]['STORECOLUMN']){
													tabHtml+='<option value="'+storeColumList[m]['PROPCODE']+'"  selected="selected" >'+storeColumList[m]['PROPNAME']+'</option>';
													temp = storeColumList[m]['PROPCODE'];
												}else{
													tabHtml+='<option value="'+storeColumList[m]['PROPCODE']+'" >'+storeColumList[m]['PROPNAME']+'</option>';
												}
												
											}else if(storeColumList[m]['PROPCODE']==featurelist[j]['STORECOLUMN']){
												tabHtml+='<option value="'+storeColumList[m]['PROPCODE']+'"  selected="selected" >'+storeColumList[m]['PROPNAME']+'</option>';
												temp = storeColumList[m]['PROPCODE'];
											}
										}
									}	
									tabHtml+='</select>';
									tabHtml+='<input type="hidden" id="tobjcolumn'+objcount+'" value="'+temp+'" name="tobjcolumn'+objcount+'" />';
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
								tabHtml+='<input name="TXNPARAMNAME'+count+'" type="text" size="10"  id="TXNPARAMNAME'+count+'" value="'+featurelist[j]['TXNPARAMNAME']+'" /></td>';
								// 数据来源 maxiao 2011-10-12
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input name="TXNPARAMSOURCE'+count+'" type="text" size="10"  id="TXNPARAMSOURCE'+count+'" value="'+featurelist[j]['TXNPARAMSOURCE']+'"/></td>';	
								// 数据映射
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input name="TXNPARAMCODE'+count+'" type="text" size="10"  id="TXNPARAMCODE'+count+'" value="'+featurelist[j]['TXNPARAMCODE']+'"/></td>';
								var paramtype='';//标记与函数类型做匹配
								var columntype='';//标记与存储字段类型做匹配
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<select name="cusparamtype'+count+'" id="cusparamtype'+count+'" style="width: 85px" onchange="selectCondsByType('+count+',\'edit\')">';
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
								tabHtml+='<select name="cusfeature'+count+'" id="cusfeature'+count+'" style="width: 85px" ';
								tabHtml+=' onchange="funcusChange(this,'+count+',0)">';
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
								tabHtml+='<select name="storefeature'+count+'" id="storefeature'+count+'" style="width: 85px" ';
								tabHtml+='onchange="funcusChange(this,'+count+',1)" >';
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
									tabHtml+='&nbsp;<input type="checkbox" name="ISSTAT'+count+'"size="10" id="ISSTAT'+count+'" value="1" checked="checked" />&nbsp;';
								}else{
									tabHtml+='&nbsp;<input type="checkbox" name="ISSTAT'+count+'"size="10" id="ISSTAT'+count+'" value="1" />&nbsp;';
								}
								
								tabHtml+='<input type="hidden" name="FEATUREPARAM'+count+'" size="10"  id="FEATUREPARAM'+count+'"  />';
								tabHtml+='<input type="hidden" name="TRANSPARAM'+count+'" size="10"   id="TRANSPARAM'+count+'"  />';
								
								// 增加排序字段  maxiao 2011-10-17 
								tabHtml+='<td  valign="top"  align="center">';
								tabHtml+='<input type="text" name="ORDERBY'+count+'" style="width:25px" size="3"  id="ORDERBY'+count+'" value="'+featurelist[j]['ORDERBY']+'" />';
								
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

	
	var dialog = jcl.ui.Dialog({
		title:'提示'
	});
	dialog.addDom('succesInfo');
	
	$("#txnAddTxnSubmitButton").click(function(){	
		var count = $('#count').val();
		var objcount  = $('#objcount').val();
		var msg = '';
		
		var existSpecialType='';	//记录已经使用的五大对象数据类型
		var existcolumn='';			//记录已经使用的存储字段
		var existname='';			//记录已经使用的属性名称
		var existcode='';			//记录已经使用的属性代码
		var existorder='';			//记录已经使用的排序号 maxiao 2011-10-11
		
		//为存放初始化属性xml的隐藏域循环赋值
		for(var i=0;i<objcount;i++){

			var ocode = $('#OBJTXNPARAMCODE'+i).val();
			var osource = $('#OBJTXNPARAMSOURCE'+i).val();
			var oname = $('#OBJTXNPARAMNAME'+i).val();
			var orderby = $('#OBJORDERBY'+i).val();
			if(!checkRepeat(existname,oname)){
				alert('属性名称有重复：'+oname);
				return ;
			}
			else{
				existname+=','+oname+',';
			}
			
			// 数据来源的检查 maxiao 2011-10-12
			if(osource.trim()==''){
				alert("第"+(i+1)+"个初始化属性的数据来源值不能为空！");
				return ;
				break;
			}
			// 不校验数据来源的重复 maxiao 2011-10-18
			/*else if(!checkRepeat(existcode,osource)){
				alert('数据来源值有重复：'+osource);
				return ;
			}*/
			else{
				var flag = checkeLength(osource);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的数据来源值超长！");
					return;
				}
				flag = checkeSpecialCode(osource);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的数据来源值含有特殊字符！");
					return;
				}
				// 比较完映射后再放入 maxiao 2011-10-11
				//existcode+=','+osource+',';
			}
			
			if(ocode.trim()==''){
				alert("第"+(i+1)+"个初始化属性的数据映射值不能为空！");
				return ;
				break;
			}
			else if(!checkRepeat(existcode,ocode)){
				alert('映射值有重复：'+ocode);
				return ;
			}
			else{
				var flag = checkeLength(ocode);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的数据映射值超长！");
					return;
				}
				flag = checkeSpecialCode(ocode);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的数据映射值含有特殊字符！");
					return;
				}
				// 比较完一组,映射再放入 maxiao 2011-10-11
				//existcode+=','+osource+',';
				existcode+=','+ocode+',';
			}
			var paramtype = $('#OBJTXNPARAMTYPE'+i).val();
			if(paramtype!=null && typeof(paramtype)!='undefind'){
				if(paramtype==''){
					alert("第"+(i+1)+"个初始化属性的数据类型不能为空！");
					return ;
					break;
				}else if(specialDateType.indexOf(paramtype)>-1 && existSpecialType.indexOf(paramtype)>-1){
					alert('五大对象数据类型有重复！');
					return ;
					break;
				}else if(specialDateType.indexOf(paramtype)>-1 && existSpecialType.indexOf(paramtype)<0){
					existSpecialType+=','+paramtype+',';
				}
			}
			   
			msg = objtmsfun[i][0].save('condsdiv'+i);
			//msg  = objtmsfun[i][0].check();		
			if(msg!=''){
				alert("第"+(i+1)+"个初始化属性的条件函数值有误："+msg);
				return;
				break;
			} 
			msg  = objtmsfun[i][1].save('storediv'+i);
			//msg  = objtmsfun[i][1].check();
			if(msg!=''){
				alert("第"+(i+1)+"个初始化属性的表达式函数值有误："+msg);
				return;
				break;
			} 
			var column = $('#OBJSTORECOLUMN'+i).val();
			if(column==''){
				alert("第"+(i+1)+"个初始化属性的存储字段不能为空！");
				return ;
				break;
			}else if(existcolumn.indexOf(column)<0){
				existcolumn+=','+column+',';
			}else if(!checkRepeat(existcolumn,column)){
				alert('存储字段有重复：'+column);
				return ;
				break;
			}
			
			if(orderby==''){
				alert("识别顺序不能为空！");
				return ;
				break;
			}else if(!checkRepeat(existorder,orderby)){
				alert('识别顺序有重复：'+orderby);
				return ;
			}
			else{
				var flag = checkeLength(orderby,3);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的识别顺序超长！");
					return;
				}
				flag = checkeSpecialCode(orderby);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的识别顺序含有特殊字符！");
					return;
				}
				flag = IsCheckLimit(orderby);
				if(!flag){
					alert("第"+(i+1)+"个初始化属性的识别顺序应在0-100之间的整数！");
					return;
				}
				// 比较完一组,排序再放入 maxiao 2011-10-18
				existorder+=','+orderby+',';
			}
			
			var tconds = objtmsfun[i][0].getJson();
			var tstore = objtmsfun[i][1].getJson();
			$('#OBJFEATUREPARAM'+i).val(tconds);
			$('#OBJTRANSPARAM'+i).val(tstore);
		} 
		
		
		var m=0;//记录删除后当前扩展属性的行号
		//为存放自定义属性xml的隐藏域循环赋值
		for(var i=0;i<count;i++){ 
			
//			var delcount = $('#delcount').val();		//已被标记为删除的属性的行号 
//			var delStr = delcount.split(',');			
//			var delStrlength= delStr.length-1;			//标记删除的属性的个数
			
			// 属性名称
			var tname = $('#TXNPARAMNAME'+i).val();
			if(tname!=undefined){				//如果没有被标记为删除的行属性进行校验，如果被标记为删除的行的属性，不参与校验
				m++;
				if(tname.trim()==''){
					alert("第"+m+"个扩展属性的名称不能为空！");
					return ;
				}
				else if(!checkRepeat(existname,tname) ){
					alert('属性名称有重复：'+tname);
					return ;
				}
				else{
					var flag = checkeLength(tname);
					if(!flag){
						alert("第"+m+"个扩展属性的名称超长！");
						return;
					}
					flag = checkeSpecialCode(tname);
					if(!flag){
						alert("第"+m+"个扩展属性的名称含有特殊字符！");
						return;
					}
					existname+=','+tname+',';
				}
				
				var tsource = $('#TXNPARAMSOURCE'+i).val();
				var tcode = $('#TXNPARAMCODE'+i).val();
				
				// 判断来源
				if(tsource.trim()==''){
					alert("第"+m+"个扩展属性的数据来源值不能为空！");
					return ;
					break;
				}
				/*else if(!checkRepeat(existcode,tsource)){
					alert('数据来源值有重复：'+tsource);
					return ;
				}*/
				else{
					var flag = checkeLength(tsource);
					if(!flag){
						alert("第"+m+"个扩展属性的数据来源值超长！");
						return;
					}
					flag = checkeSpecialCode(tsource);
					if(!flag){
						alert("第"+m+"个扩展属性的数据来源值含有特殊字符！");
						return;
					}
				}
				
				// 判断映射
				var flag2 = true;
				for(var z=0;z<hideList.length;z++){
					if(tcode.trim() == hideList[z]["PROPCODE"].trim()){
						flag2 = false;
						break;
					}
				}
				if(flag2 == false){
					alert('映射值与交易模版中隐藏属性有重复：'+tcode.trim());
					return ;
				}
				if(tcode.trim()==''){
					alert("第"+m+"个扩展属性的数据映射值不能为空！");
					return ;
					break;
				}
				else if(!checkRepeat(existcode,tcode)){
					alert('映射值有重复：'+tcode);
					return ;
				}
				else{
					
					var flag = checkeLength(tcode,255);
					if(!flag){
						alert("第"+m+"个扩展属性的数据映射值超长！");
						return;
					}
					flag = checkeSpecialCode(tcode);
					if(!flag){
						alert("第"+m+"个扩展属性的数据映射值含有特殊字符！");
						return;
					}
					//existcode+=','+tsource+',';
					existcode+=','+tcode+',';
				}
		 
				var tparamtype = $('#cusparamtype'+i).val();
				if(tparamtype==''){
					alert("第"+m+"个扩展属性的数据类型不能为空！");
					return ;
					break;
				}else if(specialDateType.indexOf(','+tparamtype+',')>-1 && existSpecialType.indexOf(','+tparamtype+',')>-1){
					alert('五大对象数据类型有重复！');
					return ;
					break;
				}else if(specialDateType.indexOf(','+tparamtype+',')>-1 && existSpecialType.indexOf(','+tparamtype+',')<0){
					existSpecialType+=','+tparamtype+',';
				}
				
				msg  = custmsfun[i][0].save('cuscondsdiv'+i);
				//msg  = custmsfun[i][0].check();
				if(msg!=''){
					alert("第"+m+"个扩展属性的条件函数值有误："+msg);
					return;
					break;
				} 
				msg  = custmsfun[i][1].save('cusstorediv'+i);
				//msg  = custmsfun[i][1].check();
				if(msg!=''){
					alert("第"+m+"个扩展属性的表达式函数值有误："+msg);
					return;
					break;
				} 
				
				var tcolumn = $('#STORECOLUMN'+i).val();
				if(tcolumn==''){
					alert("第"+m+"个扩展属性的存储字段不能为空！");
					return ;
					break;
				}else if(existcolumn.indexOf(tcolumn)<0){
					existcolumn+=','+tcolumn+',';
				}else if(!checkRepeat(existcolumn,tcolumn) ){
					alert('存储字段有重复：'+tcolumn);
					return ;
					break;
				}
				
				var torderby = $('#ORDERBY'+i).val();
				if(torderby==''){
					alert("第"+m+"个扩展属性的识别顺序不能为空！");
					return ;
					break;
				}else if(!checkRepeat(existorder,torderby)){
					alert('识别顺序有重复：'+torderby);
					return ;
				}
				else{
					var flag = checkeLength(torderby,3);
					if(!flag){
						alert("第"+m+"个扩展属性的识别顺序超长！");
						return;
					}
					flag = checkeSpecialCode(torderby);
					if(!flag){
						alert("第"+m+"个扩展属性的识别顺序含有特殊字符！");
						return;
					}
					flag = IsCheckLimit(torderby);
					if(!flag){
						alert("第"+m+"个扩展属性的识别顺序应在0-100之间的整数！");
						return;
					}
					// 比较完一组,排序再放入 maxiao 2011-10-18
					existorder+=','+torderby+',';
				}
				
				$('#FEATUREPARAM'+i).val(custmsfun[i][0].getJson());
				$('#TRANSPARAM'+i).val(custmsfun[i][1].getJson());
			}
			
		}
		var params = $('#txn-paramForm').serialize();
		
		jcl.postJSON('/tms/dp/feature/addTxnParams', params, function(data){
			if(data.success){
				$("#succesInfo").show();
				dialog.show();
			}
		});		
	})
	
	$('#updatePropButton').click(function(){
		var versionId = $('#versionId').val();
		var params = txnId+"&versionId="+versionId;
		jcl.postJSON('/tms/dp/feature/refreshParams', params, function(data){
			if(data.success){
				jcl.go('/tms/dp/txn/configTemp?'+params);
			}
		});
	});
	
	$('#txncancelButton').click(function(){
		window.close();
		//window.parent.document.getElementById("ifram-box").style.display="none";
	});
	$('#backBtn').click(function(){
		window.location.href=window.location.href;
	});
	
	
});


//根据自定义的数据类型选择过滤函数、存储函数和存储字段列表
function selectCondsByType(indexId,operat){
	var type = $('select[name=cusparamtype'+indexId+']').val();
	var column = $('input[name=tcolumn'+indexId+']').val();		//当前交易特征属性配置的存储字段
	
	
	if(type!=''){
		var parent = $('#parent'+type).val();
		var versionId = $('#versionId').val();
		
		var condsStr="";
		var storeStr="";
		var columnStr="";
		var condsList=null;
		if(parent=="0"){	//数据类型没有父节点
//			condsStr="condition=condition&functype="+type+"&model=dp";
//			storeStr="condition=store&functype="+type+"&model=dp";
			columnStr="datatype="+type+"&versionId="+versionId;
		}else{				//数据类型有父节点，按父节点的类型查询
//			condsStr="condition=condition&functype="+parent+"&model=dp";
//			storeStr="condition=store&functype="+parent+"&model=dp";
			columnStr="datatype="+parent+"&versionId="+versionId;
		}
		condsStr="condition=condition&functype="+type+"&model=dp";
		storeStr="condition=store&functype="+type+"&model=dp";
		
		jcl.postJSON('/tms/mgr/fun/funcList', condsStr, function(condsdata){
			condsList=condsdata.row;
			var tabHtml='<option value="" >--请选择--</option>';
			if(condsList==null || condsList.length==0){
				$('select[name=cusfeature'+indexId+']').html(tabHtml);
				funcusChange(document.getElementById('cusfeature'+indexId),indexId,'0');
				$('select[name=cusfeature'+indexId+']').hide();
			}else{
				for(var k=0;k<condsList.length;k++){
//					if(parent=="0"){	//数据类型没有父节点
//						if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
//							tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
//						}	
//					}else{				//数据类型有父节点，按父节点的类型查询
//						if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(parent.toUpperCase())>=0){
//							tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
//						}	
//					}	
					if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
						tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
					}				
				}
				$('select[name=cusfeature'+indexId+']').html(tabHtml);
				funcusChange(document.getElementById('cusfeature'+indexId),indexId,'0');
				$('select[name=cusfeature'+indexId+']').show();
			}
		});
		
		var storeList=null;
		
		jcl.postJSON('/tms/mgr/fun/funcList', storeStr, function(condsdata){
			storeList=condsdata.row;
			var tabHtml='<option value="" >--请选择--</option>';
			if(storeList==null || storeList.length==0){
				$('select[name=storefeature'+indexId+']').html(tabHtml);
				funcusChange(document.getElementById('storefeature'+indexId),indexId,'1');
				$('select[name=storefeature'+indexId+']').hide();
			}else{
				for(var k=0;k<storeList.length;k++){
//					if(parent=="0"){	//数据类型没有父节点
//						if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
//							tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
//						}
//					}else{				//数据类型有父节点，按父节点的类型查询
//						if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(parent.toUpperCase())>=0){
//							tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
//						}	
//					}
					if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
						tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
					}					
				}
				$('select[name=storefeature'+indexId+']').html(tabHtml);
				funcusChange(document.getElementById('storefeature'+indexId),indexId,'1');
				$('select[name=storefeature'+indexId+']').show();
			}
		});
		

		var storeColumList=null;
		jcl.postJSON('/tms/dp/txn/getColum', columnStr, function(coldata){
			storeColumList=coldata.row;
			var objproplist=coldata.objproplist;
			var tabHtml='<option value="" >--请选择--</option>';
			var f = true;
			
			for(var k=0;k<storeColumList.length;k++){
				for(var m=0;m<objproplist.length;m++){
					if(usedStoreColumn.indexOf(storeColumList[k]['PROPCODE'])>-1){//如果存储字段已经配置
						f=false;
						break;
					}else{
						f=true;
					}
				}
				if(f || storeColumList[k]['PROPCODE']==column){		//存储字段没有配置或是当前配置的存储字段，显示
					tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'">'+storeColumList[k]['PROPNAME']+'</option>';
				}
			}
			
			$('select[name=STORECOLUMN'+indexId+']').html(tabHtml);
		});
	}else{
		$('select[name=cusfeature'+indexId+']').html('');
		$('select[name=storefeature'+indexId+']').html('');
		$('select[name=STORECOLUMN'+indexId+']').html('');
		return;
	}
}
//对于可修改的交易属性，根据选择的数据类型选择函数和存储字段
function selectFuncByType(indexId,operat){
	var type = $('select[name=OBJTXNPARAMTYPE'+indexId+']').val();
	var column = $('input[name=tobjcolumn'+indexId+']').val();		//当前交易特征属性配置的存储字段
	if(type!=''){
		var parent = $('#parent'+type).val();
		var versionId = $('#versionId').val();
		
		var condsStr="";
		var storeStr="";
		var columnStr="";
		var condsList=null;
		
		if(parent=="0"){	//数据类型没有父节点
//			condsStr="condition=condition&functype="+type+"&model=dp";
//			storeStr="condition=store&functype="+type+"&model=dp";
			columnStr="datatype="+type+"&versionId="+versionId;
		}else{				//数据类型有父节点，按父节点的类型查询
//			condsStr="condition=condition&functype="+parent+"&model=dp";
//			storeStr="condition=store&functype="+parent+"&model=dp";
			columnStr="datatype="+parent+"&versionId="+versionId;
		}
		condsStr="condition=condition&functype="+type+"&model=dp";
		storeStr="condition=store&functype="+type+"&model=dp";
		
		jcl.postJSON('/tms/mgr/fun/funcList', condsStr, function(condsdata){
			condsList=condsdata.row;
			var tabHtml='<option value="" >--请选择--</option>';
			if(condsList==null || condsList.length==0){
				$('select[name=OBJFEATUREFUNCID'+indexId+']').html(tabHtml);
				funObjChange(document.getElementById('OBJFEATUREFUNCID'+indexId),indexId,'0');//将条件函数值设置为空
				$('select[name=OBJFEATUREFUNCID'+indexId+']').hide();
			}else{
				for(var k=0;k<condsList.length;k++){
//					if(parent=="0"){	//数据类型没有父节点
//						if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
//							tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
//						}
//					}else{
//						if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(parent.toUpperCase())>=0){
//							tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
//						}
//					}	
					if(condsList[k]['FUNCTYPE']=='all' || condsList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
						tabHtml+='<option value="'+condsList[k]['FUNCID']+'">'+condsList[k]['FUNCNAME']+'</option>';
					}					
				}
				$('select[name=OBJFEATUREFUNCID'+indexId+']').html(tabHtml);
				funObjChange(document.getElementById('OBJFEATUREFUNCID'+indexId),indexId,'0');//将条件函数值设置为空
				$('select[name=OBJFEATUREFUNCID'+indexId+']').show();
			}
		});
		
		var storeList=null;
		jcl.postJSON('/tms/mgr/fun/funcList', storeStr, function(condsdata){
			storeList=condsdata.row;
			var tabHtml='<option value="" >--请选择--</option>';
			if(storeList==null || storeList.length==0){
				$('select[name=OBJTRANSFUNCID'+indexId+']').html(tabHtml);
				funObjChange(document.getElementById('OBJTRANSFUNCID'+indexId),indexId,'1');//将转换函数值设置为空
				$('select[name=OBJTRANSFUNCID'+indexId+']').hide();
			}else{
				for(var k=0;k<storeList.length;k++){
//					if(parent=="0"){
//						if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
//							tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
//						}
//					}else{
//						if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(parent.toUpperCase())>=0){
//							tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
//						}
//					}
					if(storeList[k]['FUNCTYPE']=='all' || storeList[k]['FUNCTYPE'].toUpperCase().indexOf(type.toUpperCase())>=0){
						tabHtml+='<option value="'+storeList[k]['FUNCID']+'">'+storeList[k]['FUNCNAME']+'</option>';
					}						
				}
				$('select[name=OBJTRANSFUNCID'+indexId+']').html(tabHtml);
				
				funObjChange(document.getElementById('OBJTRANSFUNCID'+indexId),indexId,'1');//将转换函数值设置为空
				$('select[name=OBJTRANSFUNCID'+indexId+']').show();
			}
		});
		

		var storeColumList=null;
		jcl.postJSON('/tms/dp/txn/getColum', columnStr, function(coldata){
			storeColumList=coldata.row;
			var objproplist=coldata.objproplist;
			var tabHtml='<option value="" >--请选择--</option>';
			for(var k=0;k<storeColumList.length;k++){
				for(var m=0;m<objproplist.length;m++){
					if(usedStoreColumn.indexOf(storeColumList[k]['PROPCODE'])>-1){
						f=false;
						break;
					}else{
						f=true;
					}
				}
				if(f || storeColumList[k]['PROPCODE']==column){		//存储字段没有配置或是当前配置的存储字段，显示
					tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'">'+storeColumList[k]['PROPNAME']+'</option>';
				}
			}
			$('select[name=OBJSTORECOLUMN'+indexId+']').html(tabHtml);
		});
	}else{
		$('select[name=OBJFEATUREFUNCID'+indexId+']').html('');
		$('select[name=OBJTRANSFUNCID'+indexId+']').html('');
		$('select[name=OBJSTORECOLUMN'+indexId+']').html('');
		return;
	}
	
}







//根据存储函数显示配置对话框，进行配置(暂无使用)
function storeFuncConfig(obj,paramId){
	return;
	if(obj.value!=""){
		var tdialog = jcl.ui.Dialog({
			title:'存储转换配置'
		});
		
		var funcId = 'funcId='+obj.value;		
		jcl.postJSON('/tms/mgr/fun/funcParamList', funcId, function(paramdata){
			paramlist=paramdata.row;
			var tabHtml='';
			if(paramlist!=null && paramlist.length>1){
				tabHtml+='<table width="60%"  border="1" style="border-collapse: collapse;">';
				for(var k=0;k<paramlist.length;k++){
					tabHtml+='<tr><td  valign="top" align="center">';
					tabHtml+=paramlist[k]['FUNCPARAMLABEL'];
					tabHtml+='</td><td valign="top"  align="center">';
					tabHtml+='<input name="'+paramlist[k]['FUNCPARAMNAME']+'" type="text" size="15">';
					tabHtml+='</td></tr>';
				}
				tabHtml+='</table>';
				
				tabHtml+='<div class="button-box">';
				tabHtml+='<input type="button" value="确定" class="btn" id="" /> ';
				tabHtml+='<input type="button" value="取消" class="btn" id="" /> </div> ';
				$('#div'+paramId).html(tabHtml);
				
				tdialog.addDom('div'+paramId);
				tdialog.show();
			}
		});	
	}
}
//显示/隐藏初始化的 某组属性配置行
function displayInitTr(grpId){
	if($('#tbody'+grpId).is( ":hidden ")){
		$('#tbody'+grpId).show();
		$('#span'+grpId).text("-");
	}else{
		$('#tbody'+grpId).hide();
		$('#span'+grpId).text("+");
	}
}
//显示隐藏自定义属性的配置行
function displayCousTr(){
	if($('#coustom').is( ":hidden ")){
		$('#coustom').show();
		$('#spancoustom').text("-");
	}else{
		$('#coustom').hide();
		$('#spancoustom').text("+");
	}
}

//添加自定义属性行
function addCusRow(grp,grpname){ 
	 var oTbody =document.getElementById("tbody"+grp);
	 var rowlength = oTbody.rows.length;
	 
	 var newTr = oTbody.insertRow(rowlength);
	 newTr.setAttribute("id","tr" + count.toString());
	 newTr.setAttribute("align","center");
	 //添加两列 
	 var newTd0 = newTr.insertCell(0); 
	 var newTd1 = newTr.insertCell(1); 
	 var newTd2 = newTr.insertCell(2); 
	 var newTd3 = newTr.insertCell(3); 
	 var newTd4 = newTr.insertCell(4); 
	 var newTd5 = newTr.insertCell(5); 
	 var newTd6 = newTr.insertCell(6); 
	 var newTd7 = newTr.insertCell(7); 
	 var newTd8 = newTr.insertCell(8); 
	 //设置列内容和属性 
	
	 var td1='<input name="TXNPARAMGRPID'+count+'" type="hidden" size="10"  id="TXNFEATUREID'+count+'" value="'+grp+'"/>';
	 td1+='<input name="TXNPARAMGRPNAME'+count+'" type="hidden" size="10"  id="TXNFEATUREID'+count+'" value="'+grpname+'"/>';
	 td1+='<input type="checkbox" name="ck'+grp+'" size="10" id="" value=""  /><input name="TXNPARAMNAME'+count+'" type="text" size="10"  id="TXNPARAMNAME'+count+'" />'; 
	 newTd0.valign='top';
	 newTd0.innerHTML = td1;

	 newTd1.valign='top';
	 newTd1.innerHTML= '<input name="TXNPARAMSOURCE'+count+'" type="text" size="10"  id="TXNPARAMSOURCE'+count+'" />'; 
	 
	 newTd2.valign='top';
	 newTd2.innerHTML= '<input name="TXNPARAMCODE'+count+'" type="text" size="10"  id="TXNPARAMCODE'+count+'" />'; 
	 
	 var selectDataTypehtml='<select name="cusparamtype'+count+'" id="cusparamtype'+count+'" style="width: 85px" onchange="selectCondsByType('+count+',\'add\')">';
	 selectDataTypehtml+='<option value="" >--请选择--</option>';
	 for(var k=0;k<dataTypeList.length;k++){
		 if(usedDateType.indexOf(dataTypeList[k]['DATATYPE'])<0){
			 selectDataTypehtml+='<option value="'+dataTypeList[k]['DATATYPE']+'">'+dataTypeList[k]['DATATYPENAME']+'</option>';
		 }
	 }	
	 selectDataTypehtml+='</select>';
	 newTd3.valign='top';
	 newTd3.innerHTML= selectDataTypehtml; 
	 

	 var td4='<select name="cusfeature'+count+'" id="cusfeature'+count+'" style="width: 85px" ';
	 td4+=' onchange="funcusChange(this,'+count+',0)"></select>';
	 td4+='<div id="cuscondsdiv'+count+'"></div>';
	 newTd4.valign='top';	
	 newTd4.align='left';
	 newTd4.innerHTML= td4; 

	 var td5='<select name="storefeature'+count+'" id="storefeature'+count+'" style="width: 85px" ';
	 td5+='onchange="funcusChange(this,'+count+',1)" ></select>';
	 td5+='<div id="cusstorediv'+count+'"></div>';
	 newTd5.valign='top';
	 newTd5.align='left';
	 newTd5.innerHTML= td5; 
	 
	 newTd6.valign='top';
	 newTd6.innerHTML= '<select name="STORECOLUMN'+count+'" id="STORECOLUMN'+count+'" style="width: 85px" ></select>'; 
	 
	 var td7='&nbsp;<input type="checkbox" name="ISSTAT'+count+'" size="10" id="ISSTAT'+count+'"  value="1" />&nbsp;'
	 td7+='<input type="hidden" name="FEATUREPARAM'+count+'" size="10"   id="FEATUREPARAM'+count+'"  />';
	 td7+='<input type="hidden" name="TRANSPARAM'+count+'" size="10"   id="TRANSPARAM'+count+'"  />';
	 newTd7.valign='top';
	 newTd7.innerHTML=td7; 
	 
	 newTd8.valign='top';
	 newTd8.innerHTML= '<input name="ORDERBY'+count+'" type="text" style="width:25px" size="3" id="ORDERBY'+count+'" />'; 
	 
	 var temp = new jcl_tms.util.FuncInvoke({
			id:'tms'
	});
	var temp1 = new jcl_tms.util.FuncInvoke({
		id:'tms'
	});
	custmsfun[count] = new Array(temp,temp1);
	 
	 count++;
	 $('#count').val(count);
} 
//删除自定义属性行
function delCusRow(grp){
	var txnfetureId='';
	var txnId = $('#txnfid').val();
	var oTbody =document.getElementById("tbody"+grp);
	var rows = oTbody.rows.length ;
	
	var ck=document.getElementsByName("ck"+grp);
//	var delcount='';	//记录被删除的行号
	var j=0;
	for(var i=0;i<ck.length;i++){
		if(ck[i].checked){
			if(ck[i].value!=''){
				//txnfetureId+='txnfetureId='+ck[i].value+"&";
				txnfetureId+=ck[i].value+"&";
			}
//			delcount+=''+i+',';
			j=j+1;
		}
	}
	rows = rows-ck.length;
	
	if(j==0){
		alert(jcl.message.get('ui.tms.pub.selectmsg'));
		return;
	}
	if(confirm(jcl.message.get('ui.tms.pub.del.confirm'))){
		//if(txnfetureId!=''){
		//	txnfetureId=txnfetureId+"txnId="+txnId;
		//	jcl.postJSON('/tms/dp/feature/del',txnfetureId, function(data){
		//		if(!data.success){
		//			alert(data.error);
		//		}
		//	});
		//}
//		alert(txnfetureId);
		txnfetureId = txnfetureId.substring(0, txnfetureId.length-1);
		$('#delparamId').val(txnfetureId);
		for(var k=0;k<ck.length;k++){
			if(ck[k].checked){
				oTbody.deleteRow(k+rows);
				k=k-1;
//				count=count-1;
			}
		}
//		$('#count').val(count);
//		$('#delcount').val(delcount);	//将被删除的行号放到隐藏域中	
	}
	
}

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



