// UTF-8 encoding
// 名单管理

function showHintByType(obj,type){
	
	var hint = "";
	if(type == 'IpType'){
		hint = "IP格式，如：192.168.0.1";
	}
	if(type == 'DoubleType'){
		hint = "小数，可以包含正负号，可以包含小数点";
	}
	if(type == 'NumberType'){
		hint = "数字，不能包含正负号，不能包含小数点";
	}
	if(type == 'MoneyType'){
		hint = "金额，不能包含正负号，小数点后保留两位小数";
	}
	
	fz_showFieldHint(obj,hint);
	
}

// 显示提示信息
function fz_showFieldHint(obj,hint)
{
	if(hint == ''){
		return;
	}
	var hl = getHintLayer();
	var w = document.body.clientWidth;
	hl.style.width = (w > 500) ? '300px' : w * 0.6;
	hl.innerHTML = '<span style="height:100%" onmouseover="fz_hiddenHintLayer()">' + hint + '</span>';
	hl.style.display = '';
	
	// 计算宽度
	var ww=hl.childNodes[0].offsetWidth + 10;
	if(hl.clientWidth > ww) hl.style.width = ww;
	
	// 定位
	setHintframePosition(hl, obj);
	
};


// 动态增加或删除域
document.write("<div id='append-div' style='width:100%;height:100%;display:none'></div>");


// 输入域的提示信息
function getHintLayer()
{
	var w = document.getElementById('freezeHintLayer');
	if(w == null){
		w = document.createElement('DIV');
		w.id='freezeHintLayer';
		w.style.cssText = 'filter:Alpha(Opacity=85); border:1px solid #000000; padding:2px; position: absolute; z-index: 999; background-color:#FFFFCC; font-size: 10pt; color:#000000; display: none';
		var pNode=document.body.all['append-div'];
		if(pNode.style.display=='none') pNode=document.body;
		pNode.appendChild(w);
		pNode=null;
	}
	
	try{return w;} finally{w=null;}
};


// 设置层的位置，优先在域的上面
function setHintframePosition(iLayer, obj)
{
	var rect = getObjectPosition(obj);
	var t = rect.top,  h = rect.height, l = rect.left, r = rect.width; 
 	
 	var bo=document.body;
    var cw = iLayer.clientWidth, ch = iLayer.clientHeight;
    var dw = bo.clientWidth, dl = bo.scrollLeft, dt = bo.scrollTop;
    
    // 是否在控件的上方没有足够的空间
	var o = iLayer.style;
    o.top = (t-ch-4 > dt) ? t-ch-4 : t+h+4;
    o.left = (cw < dw - l) ? l+dl : dw-cw+dl;
    
    iLayer=bo=o=obj=null;
};


// 取对象的坐标
function getObjectPosition(obj)
{
	var isGrid=false;
	var t=0, l=0, ol=0, ot=0, w=obj.clientWidth, h=obj.clientHeight;
	
	do{
		var win = obj.document.parentWindow;
	 	while(obj){
		
	 		t+=obj.offsetTop; l+=obj.offsetLeft;

	 		obj = obj.offsetParent;
			
	 		if(obj != null){
		 		if(obj.scrollTop > 0) t -= obj.scrollTop;
		 		if(obj.scrollLeft > 0) l -= obj.scrollLeft;
		 		var bw=parseInt(obj.style.borderLeftWidth); if(!isNaN(bw)) ol+=bw;
		 		var bh=parseInt(obj.style.borderTopWidth); if(!isNaN(bh)) ot+=bh;
		 		var bw=parseInt(obj.style.offsetLeft); if(!isNaN(bw)) ol+=bw;
		 		var bh=parseInt(obj.style.offsetTop); if(!isNaN(bh)) ot+=bh;
		 	}
	 	}
	 	
	 	obj = (win==window) ? null : win.frameElement;
	} while(obj!=null);
	obj=win=null;
 	 	
 	t+=ot; l+=ol;
 	return {left:l, top:t, width:w, height:h, right:l+w, bottom: t+h};
};

// 隐藏提示信息
function fz_hiddenHintLayer()
{
	var l = document.getElementById('freezeHintLayer');
	if(l!=null){l.style.display = "none"; l.innerHTML=''; l=null;}
};