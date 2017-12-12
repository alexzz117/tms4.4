//UTF-8 encoding
function tree_init(page){
	var toolbar = page.toolbar;
	var tree = page.tree;
	var tabPanel = page.tabPanel;
	
//	toolbar.onClick('#tree-newtransation', addTraffic);
//	toolbar.onClick('#tree-deltransation', delTxn);
	
	$('#tree-box').on('click', '[type=radio]', function(){
		toolbar.enable('#tree-newtransation');
		toolbar.enable('#tree-deltransation');
	});

//	$('#tree-newtransation').click(addTraffic);
//	$('#tree-deltransation').click(delTxn);
//	$('#tree-edittransation').click(editTxn);
//	$('#tree-conftransation').click(configTxn);
	
	function showTreeManagerMenu(){
//		var id = $(this).val();
		tree.enable('tree-newtransation');
		tree.enable('tree-deltransation');
		
//		$('#tree-newtransation').show();
//		$('#tree-edittransation').show();
//		$('#tree-conftransation').show();
//		$('#tree-deltransation').show();

//		$('#tree-newgroup').hide();
//		$('#tree-editgroup').hide();
//		$('#tree-delgroup').hide();
		
		//li.children('.text').append($('#tree-manager-menu'));
		//$('#tree-manager-menu').show();
	}
	
	//配置交易
	function configTxn(){
		var txnId = $(':radio[name=funcId]:checked').val();
		var txnIdStr = 'txnId='+txnId;
		var contextPath = data.contextPath;
		window.open(contextPath+"/tms/dp/txn/config?"+txnIdStr,'交易特征配置','height=700,width=1200,top=50,left=100,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开
//		var iframe = document.getElementById("iframId");
		$('#form-box').hide();
		$('#formbox-txn_def').hide();
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
		var txnIdStr = 'txnId='+txnId+'&optype=copy';
		
		var contextPath = data.contextPath;
//		window.open(contextPath+"/tms/dp/txn/edit?"+txnIdStr,'复制交易信息','height=600,width=900,top=50,left=200,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;//新页面打开
		var iframe = document.getElementById("iframId");
		iframe.src  = contextPath+"/tms/dp/txn/edit?"+txnIdStr;
		
		$('#form-box').hide();
		$('#formbox-txn_def').hide();
		//reSetIframe("iframId");
		$('#ifram-box').show();
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
	

	//$('input[name=funcId][value=T]').attr('checked', true);
}

function cancel(evnet){
	$('#form-box').slideUp('fast');
	return false;
}



function txncancel(evnet){
	$('#formbox-txn_def').slideUp('fast');
	return false;
}



