//UTF-8 encoding

$(document).ready(function(){
	
	var box = new jcl.ui.Box({
		title: jcl.message.get('ui.tms.discern.tree'),
		minWidth:770
	});
	box.addDom('func-box');
	
	jcl.postJSON('/tms/dp/identify/tree', 'oper=temp', tree_init);
	
	//对交易组的操作
	$('#addGroupSubmitButton').click(addGroupSubmit);
	$('#modGroupSubmitButton').click(modGroupSubmit);
	$('#cancelButton').click(cancel);

	//对交易的操作
	$('#txnaddSubmitButton').click(addTxnSubmit);
	$('#txnmodSubmitButton').click(modTxnSubmit);
	$('#txncancelButton').click(txncancel);
	
	jcl.code.selector('txntype','tms.common.txntype',{text:'--请选择--',value:''});
});


function tree_init(data){
	var nodeType = jcl.message.get('ui.cmc.func.nodetype').split(','); 
	var list = data.list;
	var icons = ['moduleIcon', 'nodeIcon','fnIcon','subfnIcon'];
	for(var i = 0 ; i < list.length; i++){
		list[i]['icon'] = icons[list[i]['ftype']];
		//if(list[i]['flag'] == '0'){list[i]['font'] = 'grayfont';} 
	}
	list.push({id:'-1', ftype:'-1', text: jcl.message.get('ui.tms.discern.tree'), icon:'rootIcon', onum: 0});
	var t = new jcl.ui.Tree(list, {
		id: 'tree-box',
		sm:{
			type: 'radio',
			name: 'funcId'
		}
	});

	$('#tree-box span[onclick]:first').click();
	$('#tree-box a').each(function(){
		$(this).replaceWith($(this).text());
	});
	
	//var htmlstr = '<span class="tree-menu" id="tree-manager-menu" style="display:none; margin-left:5px;">';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-newgroup">'+jcl.message.get('ui.tms.discern.newgroup')+'</a> ';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-editgroup">'+jcl.message.get('ui.tms.discern.editgroup')+'</a> ';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-delgroup">'+jcl.message.get('ui.tms.discern.delgroup')+'</a>';

	//htmlstr += '<a href="javascript:;" class="operate" id="tree-newtransation">'+jcl.message.get('ui.tms.discern.newtransation')+'</a>';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-edittransation">'+jcl.message.get('ui.tms.discern.edittransation')+'</a>';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-conftransation">'+jcl.message.get('ui.tms.discern.conftransation')+'</a>';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-deltransation">'+jcl.message.get('ui.tms.discern.deltransation')+'</a>';
	//htmlstr += '<a href="javascript:;" class="operate" id="tree-copytransation">'+jcl.message.get('ui.tms.discern.copytransation')+'</a>';
	//htmlstr += '</span>';
	
	//$('#tree-box').append(htmlstr);
	//$('#button-box').html(htmlstr);
	
	$('#tree-newgroup').click(addGroup);	
	$('#tree-editgroup').click(modGroup);
	$('#tree-delgroup').click(delGroup);
	$('#tree-box :radio').click(showTreeManagerMenu);

	$('#tree-newtransation').click(addTraffic);
	$('#tree-deltransation').click(delTxn);
	$('#tree-edittransation').click(editTxn);
	$('#tree-copytransation').click(copyTxn);
	$('#tree-conftransation').click(configTxn);
	
	
	
	function showTreeManagerMenu(){
		var id = $(this).val();
		var li = $(this).parent().parent();
		var ftype = t.map[id].ftype;
		
		if(t.map[id].ftype == '0'){
			$('#tree-newgroup').show();
			$('#tree-newtransation').hide();
			
			$('#tree-editgroup').hide();
			$('#tree-delgroup').hide();
			$('#tree-edittransation').hide();
			$('#tree-conftransation').hide();
			$('#tree-deltransation').hide();
			$('#tree-copytransation').hide();
			
		}else if(t.map[id].ftype == '1'){
			$('#tree-newgroup').show();
			$('#tree-newtransation').show();
			$('#tree-editgroup').show();
			$('#tree-delgroup').show();
			
			$('#tree-edittransation').hide();
			$('#tree-conftransation').hide();
			$('#tree-deltransation').hide();
			$('#tree-copytransation').hide();
		}else{
			$('#tree-edittransation').show();
			$('#tree-conftransation').show();
			$('#tree-deltransation').show();
			$('#tree-copytransation').show();

			$('#tree-newgroup').hide();
			$('#tree-newtransation').hide();
			$('#tree-editgroup').hide();
			$('#tree-delgroup').hide();
		}
		
		//li.children('.text').append($('#tree-manager-menu'));
		//$('#tree-manager-menu').show();
	}
	//添加交易组视图
	function addGroup(){
		$('#modGroupSubmitButton').hide();
		$('#addGroupSubmitButton').show();
		var groupId = $(':radio[name=funcId]:checked').val();
		var node = t.map[groupId];
		var ffid = groupId;
		$('#fid').val('');
		$('#ffid').val(ffid);
		$('#fname').val('');
		$('#ffname').val(node.text);
		$('#channelid').val(node.channelid);		
		$('#onum').val('0');
		$('#info').val('');
		
		$('#item-ffname').show();
		var ftype = parseInt(node.ftype) + 1;

		$('#form-box').show();
		$('#form-txn-box').hide();
		$('#ifram-box').hide();
		return false;
	}
	//修改交易组视图
	function modGroup(){
		$('#addGroupSubmitButton').hide();
		$('#modGroupSubmitButton').show();
		var groupId = $(':radio[name=funcId]:checked').val();
		if(groupId){
			var node = t.map[groupId];
			$('#fid').val(groupId);
			$('#ffid').val(node.fid);
			$('#fname').val(node.text);
			$('#channelid').val(node.channelid);	
			$('#ffname').val(node.parentname);	
			$('#onum').val(node.onum);	
			$('#info').val(node.info);
			

			$('#form-box').show();
			$('#form-txn-box').hide();
			$('#ifram-box').hide();
		}
		return false;
	}
	//删除交易组
	function delGroup(){
		if(confirm(jcl.message.get("ui.tms.pub.del.confirm"))){
			var groupId = $(':radio[name=funcId]:checked').val();
			
			jcl.postJSON('/tms/dp/identify/del', 'groupId='+groupId, function(data){
				var info = '';
				if(data.success){
					info += jcl.message.get("ui.tms.pub.del.success");
					alert(info);
				}
				jcl.go("/tms/dp/identify/treeTemp");
			});
		}
		return false;
	}
	

	
	//新建交易
	function addTraffic(){
		var versionlist = data.versionlist;
		$('#txnmodSubmitButton').hide();
		$('#txnaddSubmitButton').show();
		var txnId = $(':radio[name=funcId]:checked').val();
		var node = t.map[txnId];
		var ffid = txnId;
		$('#txnffid').val(ffid);
		$('#txnfname').val('');
		$('#txnffname').val(node.text);
		$('#txnchannelid').val(node.channelid);		
		$('#ORDERBY').val('0');
		var htmlStr = "<option value=''>--请选择--</option>";
		for(var i=0;i<versionlist.length;i++){
			htmlStr+="<option value='"+versionlist[i]['VERSIONID']+"'>"+versionlist[i]['VERSIONNAME']+"</option>";
		}
		$('#txnversion').html(htmlStr);

		$('#form-box').hide();
		$('#form-txn-box').show();
		$('#ifram-box').hide();
		return false;
	}
	//编辑交易
	function editTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId+'&optype=edit&oper=temp';
		var contextPath = data.contextPath;
		//window.open(contextPath+"/tms/dp/txn/edit?"+txnIdStr,'编辑交易信息','height=600,width=900,top=50,left=200,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开

		document.getElementById("iframId").src=contextPath+"/tms/dp/txn/editTemp?"+txnIdStr;
		$('#form-box').hide();
		$('#form-txn-box').hide();
//		reSetIframe("iframId");
		$('#ifram-box').show();
	}
	
	//配置交易
	function configTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId+"&oper=temp";
		var contextPath = data.contextPath;
		window.open(contextPath+"/tms/dp/txn/configTemp?"+txnIdStr,'交易特征配置','height=700,width=1200,top=50,left=100,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开
//		var iframe = document.getElementById("iframId");
		$('#form-box').hide();
		$('#form-txn-box').hide();
		//reSetIframe("iframId");
		//$('#ifram-box').show();
	}
	//使Iframe的高度自动变化
	function reSetIframe(iframId){
		var iframe = document.getElementById(iframId);
		try{
			var bHeight = iframe.contentWindow.document.body.scrollHeight;
			var dHeight = iframe.contentWindow.document.documentElement.scrollHeight;
			var height = Math.max(bHeight, dHeight);
			iframe.height =  height;
		}catch (ex){
		}
	}
	//复制交易
	function copyTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId+'&optype=copy&oper=temp';
		
		var contextPath = data.contextPath;
//		window.open(contextPath+"/tms/dp/txn/edit?"+txnIdStr,'复制交易信息','height=600,width=900,top=50,left=200,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开
		var iframe = document.getElementById("iframId");
		iframe.src  = contextPath+"/tms/dp/txn/editTemp?"+txnIdStr;
		
		$('#form-box').hide();
		$('#form-txn-box').hide();
		//reSetIframe("iframId");
		$('#ifram-box').show();
	}
	
	//删除交易
	function delTxn(){
		if(confirm(jcl.message.get("ui.tms.pub.del.confirm"))){
			var txnId = $(':radio[name=funcId]:checked').val();
			var param = 'txnId='+txnId+'&oper=temp';
			jcl.postJSON('/tms/dp/txn/del', param, function(data){
				var info = '';
				if(data.success){
					info += jcl.message.get("ui.tms.pub.del.success");
				}
				alert(info);
				jcl.go("/tms/dp/identify/treeTemp");
			});
		}
		return false;
	}
	
	function setMenu(v){
		$('input:radio[name=menu]').each(function(){
			if (v == $(this).val()){
				$(this).attr('checked', true);
			}else{
				$(this).attr('checked', false);
			}
		});
	}
	function setIslog(v){
		$('input:checkbox[name=islog]').each(function(){
			if (v == $(this).val()){
				$(this).attr('checked', true);
			}else{
				$(this).attr('checked', false);
			}
		});
	}
	function setIsgrant(v){
		$('input:checkbox[name=grant]').each(function(){
			if (v == $(this).val()){
				$(this).attr('checked', true);
			}else{
				$(this).attr('checked', false);
			}
		});
	}
}
//添加交易组
function addGroupSubmit(){
	var fname = $('#fname').val();
	var onum = $('#onum').val();
	var info = $('#info').val();
	if(fname.trim()==''){		
		alert(jcl.message.get('ui.tms.traffic.nameEmpty'));
		return;
	}else {
		var flag = checkeLength(fname);
		if(!flag){
			alert('交易组名称长度超长');
			return;
		}
		flag = checkeSpecialCode(fname);
		if(!flag){
			alert('交易组名称含有特殊字符');
			return;
		}
	}
	if(info.trim()!=''){
		var flag = checkeSpecialCode(info);
		if(!flag){
			alert('描述信息含有特殊字符');
			return;
		}
		flag = checkeLength(info,'256');
		if(!flag){
			alert('交易组描述信息超长');
			return;
		}
	}
	
	
	if(onum!=''){
		var r   =    /^\d*$/;　　//非负整数   
		if(!r.test(onum)){
			alert('顺序请填写数字');
			return;
		}
		var flag = checkeLength(onum,'4');
		if(!flag){
			alert('交易组顺序不能超过4位数');
			return;
		}
	}
	$('#fname').val(fname.trim());
	var params = $('#data-form-group').serializeArray();
	jcl.postJSON('/tms/dp/identify/create', params, function(data){
		if(data.success){
			alert(jcl.message.get('ui.tms.pub.add.success'));
			jcl.go("/tms/dp/identify/treeTemp");
		}
	});
}
//修改交易组
function modGroupSubmit(){
	var fname = $('#fname').val();
	var onum = $('#onum').val();
	var info = $('#info').val();
	if(fname.trim()==''){
		alert(jcl.message.get('ui.tms.traffic.nameEmpty'));
		return;
	}else {
		var flag = checkeLength(fname);
		if(!flag){
			alert('交易组名称长度超长');
			return;
		}
		flag = checkeSpecialCode(fname);
		if(!flag){
			alert('交易组名称含有特殊字符');
			return;
		}
	}
	if(info.trim()!=''){
		var flag = checkeSpecialCode(info);
		if(!flag){
			alert('描述信息含有特殊字符');
			return;
		}
		flag = checkeLength(info,'256');
		if(!flag){
			alert('交易组描述信息超长');
			return;
		}
	}
	if(onum!=''){
		var r   =    /^\d*$/;　　//非负整数   
		if(!r.test(onum)){
			alert('顺序请填写数字');
			return;
		}
		var flag = checkeLength(onum,'4');
		if(!flag){
			alert('交易组顺序不能超过4位数');
			return;
		}
	}
	$('#fname').val(fname.trim());
	var params = $('#data-form-group').serializeArray();
	jcl.postJSON('/tms/dp/identify/mod', params, function(data){
		if(data.success){
			alert(jcl.message.get('ui.tms.pub.edit.success'));
			jcl.go("/tms/dp/identify/treeTemp");
		}
	});
}
function cancel(evnet){
	$('#form-box').slideUp('fast');
	return false;
}


//添加交易
function addTxnSubmit(){
	var txnfname = $('#txnfname').val();
	var txnversion = $('#txnversion').val();
	var WEIGHT = $('#WEIGHT').val();
	var ORDERBY = $('#ORDERBY').val();
	var txninfo = $('#txninfo').val();
	var txntype = $('#txntype').val();	
	
	if(txnfname.trim()==''){
		alert(jcl.message.get('ui.tms.traffic.empty'));
		return ;
	}else {
		var flag = checkeLength(txnfname);
		if(!flag){
			alert('交易名称长度超长');
			return;
		}
		flag = checkeSpecialCode(txnfname);
		if(!flag){
			alert('交易名称含有特殊字符');
			return;
		}
	}
	if(txninfo.trim()!=''){
		var flag = checkeSpecialCode(txninfo);
		if(!flag){
			alert('描述信息含有特殊字符');
			return;
		}
		flag = checkeLength(txninfo,'256');
		if(!flag){
			alert('交易描述信息超长');
			return;
		}
	}
	
	if(txnversion==''){
		alert(jcl.message.get('ui.tms.traffic.selectVersion'));
		return ;
	}
	
	if(WEIGHT!=''){
		var r   =    /^\d*$/;　　//非负整数   
		if(!r.test(WEIGHT) || WEIGHT<0 || WEIGHT>100){
			alert('交易风险权重请填写0~100的数字');
			return;
		}
	}else{
		alert('交易风险权重不能为空！');
		return;
	}
	if(ORDERBY!=''){
		var r   =    /^\d*$/;　　//非负整数   
		if(!r.test(ORDERBY)){
			alert('顺序请填写数字');  
			return;
		}
		var flag = checkeLength(ORDERBY,'4');
		if(!flag){
			alert('交易顺序不能大于4位数');
			return;
		}
	}
	if(txntype==''){
		alert('交易类型不能为空');
		return;
	}
	
	
	$('#txnfname').val(txnfname.trim());
	var params = $('#data-form').serializeArray();
	jcl.postJSON('/tms/dp/txn/create', params, function(data){
		if(data.success){
			alert(jcl.message.get('ui.tms.pub.add.success'));
			jcl.go("/tms/dp/identify/treeTemp");
		}
	});
}
//更新交易
function modTxnSubmit(){
	
	var txnfname = $('#txnfname').val();
	var txnversion = $('#txnversion').val();
	var ORDERBY = $('#ORDERBY').val();
	var txninfo = $('#txninfo').val();
	
	if(txnfname.trim()==''){
		alert(jcl.message.get('ui.tms.traffic.empty'));
		return ;
	}else {
		var flag = checkeLength(txnfname);
		if(!flag){
			alert('交易名称长度超长');
			return;
		}
		flag = checkeSpecialCode(txnfname);
		if(!flag){
			alert('交易名称含有特殊字符');
			return;
		}
	}
	if(txninfo.trim()!=''){
		var flag = checkeSpecialCode(txninfo);
		if(!flag){
			alert('描述信息含有特殊字符');
			return;
		}
		flag = checkeLength(txninfo,'256');
		if(!flag){
			alert('交易描述信息超长');
			return;
		}
	}
	if(txnversion==''){
		alert(jcl.message.get('ui.tms.traffic.selectVersion'));
		return ;
	}
	if(ORDERBY!=''){
		var r   =    /^\d*$/;　　//非负整数   
		if(!r.test(ORDERBY)){
			alert('顺序请填写数字');  
			return;
		}
		var flag = checkeLength(ORDERBY,'4');
		if(!flag){
			alert('交易顺序不能大于4位数');
			return;
		}
	}
	$('#txnfname').val(txnfname.trim());
	var params = $('#data-form').serializeArray();
	jcl.postJSON('/tms/dp/txn/mod', params, function(data){
		if(data.success){
			alert(jcl.message.get('ui.tms.pub.edit.success'));
			jcl.go("/tms/dp/identify/treeTemp");
		}
	});
}
function txncancel(evnet){
	$('#form-txn-box').slideUp('fast');
	return false;
}



