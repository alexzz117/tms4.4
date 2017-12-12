jcl.ui=jcl.ui||{};

jcl.ui.Tree =function(nodelist, options){
	this.opts = $.extend({
		id: null,
		sm:null,
		//{type:'checkbox', name:'funcId'} //'radiobox', 'none'
		iconable: false,
		//iconable: true,
		//modify by yangk
		order: true,
		defIcon: 'icon',
		fields:{
			id: 'id',
			fid: 'fid',
			onum: 'onum',
			icon: 'icon',
			url: 'url',
			type: 'type',
			text: 'text',
			font: 'font'
		}
	}, options);
	var CHILD_NODES = 'childNodes';
	var TOP = 'top';
	var $tree = this;
	
	this.top = [];
	this.list = nodelist || [];
	this.map = {};
	
	function _init(){
		var opts = $tree.opts;
		var list = $tree.list;
		var top = $tree.top;
		var map = $tree.map;
		
		if(opts.order){
			list.sort(function(x, y){
				return (x[opts.fields.onum]||0) - (y[opts.fields.onum]||0);
			});
		}
		for(var i = 0; i < list.length; i++){
			map[list[i][opts.fields.id]] = list[i];
		}
		for(var i = 0; i < list.length; i++){
			var node = list[i];
			if(node[CHILD_NODES] == undefined){
				node[CHILD_NODES] = [];
			}
			var fid = node[opts.fields.fid];
			if(map[fid]){
				var fnode = map[fid];
				if(fnode[CHILD_NODES] == undefined){
	                fnode[CHILD_NODES] = [];
	            }
				fnode[CHILD_NODES].push(i);
			}else{
				node[TOP] = true;
				top.push(i);
			}
		}
		var htmlstr = '<ul class="menu">';
		htmlstr += _printNodeList(top);
		htmlstr += '</ul>';
		$('#' + opts.id).html(htmlstr);
	}
	
	function _printNodeList(nids){
		var opts = $tree.opts;
		var list = $tree.list;
		var htmlstr = '';
	    for(var i=0; i< nids.length; i++){
	        var node = list[nids[i]];
	        htmlstr += _printNode(node, i==(nids.length-1), nids.length > 1);
	    }
	    return htmlstr;
	}
	
	function _printNode(node, isEnd, hasBrother){
		var opts = $tree.opts;
		var list = $tree.list;
		var isTop = !!node.top;
		var htmlstr = '';
		var hasChild = (node[CHILD_NODES].length > 0);
		var icon = '';
		if(opts.iconable){
			icon = node[opts.fields.icon] ? node[opts.fields.icon]: opts.defIcon;
			icon = '<span class="'+icon+'"></span>';
		}
		var textCls = 'text'+(node[opts.fields.font] ? ' '+node[opts.fields.font]:'');
		var a = (url ? '<a href="'+url+'" class="link">'+node[opts.fields.text]+'</a>' : node[opts.fields.text]);
		var text = '<span class="'+textCls+'">'+a+'</span>';
		var url = node[opts.fields.url];
		
		var line = (isTop ? '':'<span class="'+(isEnd?'lineEnd':'line')+(hasChild ? 'Close" onclick="collapse()"':'"')+'></span>');
		var input = '';
		if(node[opts.fields.id]!='-1'){
			if(opts.sm){
				input = '<span class="noline"><input type="'+opts.sm.type+'" name="'+opts.sm.name+'" value="'+node[opts.fields.id]+'"/></span>';
			}
		}
		htmlstr += ('<li class="'+(hasChild ? 'node':'leaf')+'">'+line+icon+input+text);
		if(hasChild){
			htmlstr += ('<ul'+((hasBrother && !isEnd) ? ' class="mult"':'')+ (isTop ? 'style="padding:0px;"':' style="display:none;"')+'>');
			htmlstr += _printNodeList(node[CHILD_NODES]);
			htmlstr += ('</ul>');
		}
		htmlstr += ('</li>');
		
		return htmlstr;
	}
	
	_init();
};

function collapse(){
    var theEvent = window.event || arguments.callee.caller.arguments[0];
    var obj = theEvent.srcElement;
    if (!obj) {
        obj = theEvent.target;
    }
    var childs = obj.parentNode.childNodes;
    var swaps = document.getElementsByTagName("UL");
    var clickNode;
    var clickNodeDisplay;
    for(var i = 0; i < childs.length; i ++){
        if(childs[i].tagName){
            if(childs[i].tagName.toUpperCase() == "UL"){
                clickNode = childs[i];
                clickNodeDisplay = childs[i].style.display;
            }
        }
    }
    if(clickNodeDisplay == "none"){
        clickNode.style.display = "";
        obj.className = (obj.className.indexOf("End") != -1)?"lineEnd":"line";
    }else{
        clickNode.style.display = "none";
        obj.className = (obj.className.indexOf("End") != -1)?"lineEndClose":"lineClose";
    }
}