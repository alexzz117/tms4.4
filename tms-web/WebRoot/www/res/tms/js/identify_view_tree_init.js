//UTF-8 encoding

$(document).ready(function(){
	
	var box = new jcl.ui.Box({
		title: jcl.message.get('ui.tms.discern.viewtree'),
		minWidth:770
	});
	box.addDom('func-box');
	
	jcl.postJSON('/tms/dp/identify/tree', 'oper=view', tree_init);
	
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
	list.push({id:'-1', ftype:'-1', text: jcl.message.get('ui.tms.discern.viewtree'), icon:'rootIcon', onum: 0});
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
	
	$('#tree-editgroup').click(modGroup);
	$('#tree-box :radio').click(showTreeManagerMenu);
	$('#tree-edittransation').click(editTxn);
	$('#tree-conftransation').click(configTxn);
	
	
	function showTreeManagerMenu(){
		var id = $(this).val();
		var li = $(this).parent().parent();
		var ftype = t.map[id].ftype;
		
		if(t.map[id].ftype == '0'){
			$('#tree-editgroup').hide();
			$('#tree-edittransation').hide();
			$('#tree-conftransation').hide();
		}else if(t.map[id].ftype == '1'){
			$('#tree-editgroup').show();
			$('#tree-edittransation').hide();
			$('#tree-conftransation').hide();
		}else{
			$('#tree-edittransation').show();
			$('#tree-conftransation').show();
			$('#tree-editgroup').hide();
		}
		
		//li.children('.text').append($('#tree-manager-menu'));
		//$('#tree-manager-menu').show();
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
			$('#ifram-box').hide();
		}
		return false;
	}
	//编辑交易
	function editTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId;
		var contextPath = data.contextPath;
		//window.open(contextPath+"/tms/dp/txnView/edit?"+txnIdStr,'编辑交易信息','height=600,width=900,top=50,left=200,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开

		document.getElementById("iframId").src=contextPath+"/tms/dp/txnView/edit?"+txnIdStr;
		$('#form-box').hide();
		$('#form-txn-box').hide();
//		reSetIframe("iframId");
		$('#ifram-box').show();
	}
	
	//配置交易
	function configTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId;
		var contextPath = data.contextPath;
		window.open(contextPath+"/tms/dp/txnView/config?"+txnIdStr,'交易特征配置','height=700,width=1200,top=50,left=100,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开
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
function cancel(evnet){
	$('#form-box').slideUp('fast');
	return false;
}


function txncancel(evnet){
	$('#form-txn-box').slideUp('fast');
	return false;
}



