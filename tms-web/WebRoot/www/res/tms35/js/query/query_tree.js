var json_div = '<div id="jsonlinediv"><textarea cols="4" rows="10" id="jsonline" disabled></textarea></div>';
json_div += '<div id="jsontextdiv"><textarea id="jsontext" name="queryData" cols="60" rows="10" wrap="off" oncontextmenu="return false" class="ttext-view grey"></textarea></div>';
json_div += '<div class="clear"></div><pre id="results"></pre>';

function tree_init(page) {
	//var frame = page.frame;
	var toolbar = page.toolbar;
	var tree = page.tree;

	page.queryform.jqDom.find('textarea').parent().html(json_div);

	page.queryform.jqDom.on({
		'scroll':function(){
			orgLine($('#jsonline'), $(this));
		},
		'keyup':function(){
			orgLine($('#jsonline'), $(this));
		},
		'click':function(){
			$(document).on('click.json', function(e){
				if(e.target != $('#jsontext').get(0)
					&& e.target != page.queryform.jqDom.find('.saveBtn').get(0)
					&& e.target != page.queryform.jqDom.find('.cancelBtn').get(0)){
					var qdata = page.queryform.jqDom.find('textarea[name=queryData]');
					if(IsEmpty(jQuery.trim(qdata.val()))){//不为空
						jsonValidate(qdata);
					} else {
						emptyResultInfo(page.queryform);
					}
					$(document).off('click.json');
				}else if(e.target == page.queryform.jqDom.find('.saveBtn').get(0)
					|| e.target == page.queryform.jqDom.find('.cancelBtn').get(0)){
					$(document).off('click.json');
				}
			});
		}
	}, '#jsontext');

	tree.onSelectChange(function(){
		var list = tree.select();
		if(list.length > 0){
			var node = tree.getNode(list[0]);
			_selectTree(node);
		}
	});

	function _selectTree(node){
        if(node.qtype == '-1'){//顶节点
        	toolbar.enable($('#tree-newgroup'));
			toolbar.enable($('#tree-newquery'));
            toolbar.disable($('#tree-delquery'));
            toolbar.disable($('#tree-viewquery'));
        }else if(node.qtype == '0'){//分组
        	toolbar.disable($('#tree-newgroup'));
			toolbar.enable($('#tree-newquery'));
            toolbar.enable($('#tree-delquery'));
            toolbar.disable($('#tree-viewquery'));
        }else if(node.qtype == '1'){//查询
        	toolbar.disable($('#tree-newgroup'));
            toolbar.disable($('#tree-newquery'));
            toolbar.enable($('#tree-delquery'));
        	toolbar.enable($('#tree-viewquery'));
        }
		var form = null;
		if(node.qtype == '-1'){
			page.groupform.jqDom.hide();
			page.queryform.jqDom.hide();
		} else if(node.qtype == '0') {
			form = page.groupform;
			page.queryform.jqDom.hide();
			form.jqDom.show();
			form.jqDom.find('.form-items > .form-item').show();
			form.set(node, {
				nodeId:'id',
				parentNodeId:'fid',
	        	tNodeName: 'text',
				orderBy: 'onum'
	        });
        } else if(node.qtype == '1') {
        	form = page.queryform;
			page.groupform.jqDom.hide();
			form.jqDom.show();
			form.jqDom.find('.form-items > .form-item').show();
			form.jqDom.find('.btn:button').show();
			form.set(node, {
				nodeId:'id',
				parentNodeId:'fid',
	        	tNodeName: 'text',
				_create_time: 'ctime'
	        });
			form.jqDom.find('#jsonlinediv').parent().html(json_div);
	        form.jqDom.find('textarea[name=queryData]').val(node.qdata);
	        orgLine($('#jsonline'), $('#jsontext'));
	        emptyResultInfo(form);
        }

        //显示节点路径
        var text = node.text;
        var count = 0;
        while(node.id != '-1' && count < 10){
        	node = tree.getNode(node.fid);
        	text = node.text + ' > ' + text;
        	count ++;
        }
        $('#node-info').text(text);
    }
}

/**
 * json校验
 * @returns {Boolean}
 */
function jsonValidate(jqObj) {
	var formatJson = jsl.interactions.validate(jqObj);
	orgLine($('#jsonline'), $('#jsontext'));
    return formatJson;
}

/**
 * 清空json校验结果
 * @param form
 */
function emptyResultInfo(form){
	var result = form.jqDom.find('#results');
	result.removeClass('error').empty().hide();
}

/**
* 计算json的行号
* @param {} line	行textarea,jquery对象
* @param {} text	内容textarea,jquery对象
*/
function orgLine(line, text) {
	writeLine(line, text);
	textHeight(line, text);
}

function textHeight(line, text) {
	var domTextArea	= $(text)[0];
	var scrollTop = domTextArea.scrollTop;
	$(line)[0].scrollTop = scrollTop;
}

function writeLine(line, text){
	var str = text.val();
	str = str.replace(/\r/gi,"");
	str = str.split("\n");
	n = str.length;
	siteLine(n, line);
}

function siteLine(n, line){
	var num = '';
	for(var i=1;i<=n;i++){
	   if(document.all){
	    num += (i + "\r\n");
	   }else{
	    num += (i + "\n");
	   }
	}
	line.val(num);
	num="";
}

function submitCheck(form) {
	var nodeName = form.jqDom.find('[name=tNodeName]').val();
	if(!(/^.+$/.test(jQuery.trim(nodeName)))){
		jcl.msg.error('[节点名称]不能为空！');
		return false;
	}
	if(!checkSpecialCharacter(nodeName, '2')){
		jcl.msg.error('[节点名称]只能包含汉字,字母,数字和下划线！');
		return false;
	}
	if(nodeName.lengths() > 30){
		//alert('[节点名称]最多只能输入30个字符！');
		jcl.msg.error('[节点名称]最多只能输入30个字符！');
		return false;
	}
	var nodeType= form.jqDom.find('[name=tNodeType]').val();
	if(nodeType=='0'){//分组
		var onum = form.jqDom.find('[name=orderBy]').val();
		if(!(/^\d{1,2}$/.test(onum)) || (onum+'' != onum*1+'')){
			//alert('[排序]请输入小于99的正整数！');
			jcl.msg.error('[排序]请输入小于99的正整数！');
			return false;
		}
	} else if(nodeType=='1'){//查询
		var qdata = form.jqDom.find('textarea[name=queryData]');
		if(!IsEmpty(jQuery.trim(qdata.val()))){
			//alert('[JSON配置]不能为空！');
			jcl.msg.error('[JSON配置]不能为空！');
			return false;
		}
		jsonValidate(qdata);
		if($('#results').attr("class") == 'error') {
			//alert('[JSON配置]格式错误！');
			jcl.msg.error('[JSON配置]格式错误！');
			return false;
		}
	}
	return true;
}