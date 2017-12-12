
//根据类型选择存储字段
function selectFuncByType(obj,operat){
	var type = obj.value;
	var parentid = $('#parent'+type).val();
	var versionId = $('#versionId').val();
	
	if(type!=''){
		var storeColumList=null;
		var datatype= "";
		if(parentid=="0"){	//数据类型没有父节点
			datatype="datatype="+type+"&versionId="+versionId;
		}else{				//数据类型有父节点，按父节点的类型查询
			datatype="datatype="+parentid+"&versionId="+versionId;
		}
		jcl.postJSON('/tms/dp/txn/getColum', datatype, function(coldata){
			storeColumList=coldata.row;
			var tabHtml='';//<option value="" >--请选择--</option>
			if(operat=="add"){		//如果是新建参数，存储字段将已经配置的过滤掉，使其不能再次选择
				var objproplist  =coldata.objproplist;
				var f = true;
				for(var k=0;storeColumList!=null && k<storeColumList.length;k++){
					for(var m=0;objproplist != null && m<objproplist.length;m++){
						if(storeColumList[k]['PROPCODE']==objproplist[m]['STORECOLUMN']){
							f=false;
							break;
						}else{
							f=true;
						}
					}
					if(f){
						tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'">'+storeColumList[k]['PROPNAME']+'</option>';
					}
				}
			}else{
				var objproplist  =coldata.objproplist;
				var editType = $('#STORECOLUMN').val();
				var f = true;
				for(var k=0;k<storeColumList.length;k++){
					for(var m=0;objproplist != null && m<objproplist.length;m++){
						if(storeColumList[k]['PROPCODE']==objproplist[m]['STORECOLUMN'] && editType!=objproplist[m]['STORECOLUMN']){
							f=false;
							break;
						}else{
							f=true;
						}
					}
					if(f){
						tabHtml+='<option value="'+storeColumList[k]['PROPCODE']+'">'+storeColumList[k]['PROPNAME']+'</option>';
					}
				}
			}
			$('select[name=STORECOLUMN]').html(tabHtml);
		});
	}else{
		$('select[name=STORECOLUMN]').html('');
		return;
	}
	
}
//根据数据来源类型标识查询数据来源类型Id列表
function selectSource(obj){
	var sourceType =obj.value;
	if(sourceType!=''){
		sourceHtml(sourceType);
	}else{
		$('select[name=SOURCEID]').html('');
	}
}
function sourceHtml(sourceType,sourceId){
	if(sourceType=='DICT'){
		jcl.postJSON('/tms/mgr/meta/getSource', 'sourceType='+sourceType, function(data){
			var sourceIdList=data.row;		
			var tabHtml='<option value="" >--请选择--</option>';
			for(var k=0;k<sourceIdList.length;k++){
				if(sourceId!=null && sourceIdList[k]['CATEGORY_ID']==sourceId){
					tabHtml+='<option value="'+sourceIdList[k]['CATEGORY_ID']+'" selected="selected">'+sourceIdList[k]['CATEGORY_NAME']+'</option>';
				}else{
					tabHtml+='<option value="'+sourceIdList[k]['CATEGORY_ID']+'">'+sourceIdList[k]['CATEGORY_NAME']+'</option>';
				}
			}
			$('select[name=SOURCEID]').removeAttr("disabled");
			$('select[name=SOURCEID]').html(tabHtml);
			$('#trSOURCEID').show();
		});	
	}else{
		$('select[name=SOURCEID]').html('');
		$('#trSOURCEID').hide();
	}
}



