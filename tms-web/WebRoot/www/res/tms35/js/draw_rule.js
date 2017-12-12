/**
 * @author Administrator
 */

// 画线
var Line = function(lineName) {   
    var color = "#000000"; // 连线的默认颜色
    var idocument = document; // 页面对象  
  	var g = lineName;// 连线名称
    var draw = { 
        // 画出最基本的图像，点，和垂直直线或水平直线   
        drawBase : function(x1, y1, w, h) {   
            try {   
				var span = "<span name="+g+" style="+"z-index:2;position:absolute;left:"+x1+"px;top:"+y1+"px;height:"+h+"px;width:"+w+"px;background:"+color+"; ></span>";
				$('#drowDiv').append(span);
            } catch (e) {   
            }   
        },   
        drawLine : function(x1, y1, x2, y2,m) {   
            var w = (((x2 - x1) == 0 ? 3 : (x2 - x1)));   
            var h = (((y2 - y1) == 0 ? 3 : (y2 - y1)));   
            var oldX = -1;   
            var oldY = -1;   
            var newX = 0;
            var newY = 0;   
            if (Math.abs(w) > Math.abs(h)) {   
            	for (var i = 0; i < Math.abs(w); i=i+parseInt(m)) {   
            		newX = (x1 + (w > 0 ? 1 : -1) * i);   
            		newY = (y1 + (h > 0 ? 1 : -1) * Math.abs(i * h / w));   
                    if (oldX != newX && oldY != newY) {   
                    	oldX = newX;   
                        oldY = newY;   
                        draw.drawBase(oldX, oldY, 2, 2); 
                    }   
                }   
            } else {   
            	for (var i = 0; i < Math.abs(h); i=i+parseInt(m)) {   
                	newX = (x1 + (w > 0 ? 1 : -1) * Math.abs(i * w / h));   
                	newY = (y1 + (h > 0 ? 1 : -1) * i);   
                	if (oldX != newX && oldY != newY) {   
                		oldX = newX;   
                		oldY = newY;   
                		draw.drawBase(oldX, oldY, 2, 2);
                	}   
                }   
            }   
        },   
        drawArrowheaded : function(x0, y0, x1, y1) {// 箭头   
            var w = (((x1 - x0) == 0 ? 1 : (x1 - x0)));   
            var h = (((y1 - y0) == 0 ? 1 : (y1 - y0)));   
  
            var d = Math.sqrt((y1 - y0) * (y1 - y0) + (x1 - x0) * (x1 - x0));   
            var Xa = x1 + 10 * ((x0 - x1) + (y0 - y1) / 2) / d;   
            var Ya = y1 + 10 * ((y0 - y1) - (x0 - x1) / 2) / d;   
            var Xb = x1 + 10 * ((x0 - x1) - (y0 - y1) / 2) / d;   
            var Yb = y1 + 10 * ((y0 - y1) + (x0 - x1) / 2) / d;   
  
            draw.drawLine(x1, y1, Xa, Ya,4);   
            draw.drawLine(x1, y1, Xb, Yb,4);   
        },   
        drawArrowheadedLine : function(x1, y1, x2, y2) {  
			
			 // 虚直线   
            draw.drawLine(x1+15, y1+15, x2+15, y2+15,5);      
            // 箭头   
            draw.drawArrowheaded(x1+15, y1+15, x2+15, y2+15);     
        }   
    }; 
  
  	
    Line.prototype.drawBaseLine = function(x1, y1) {// 画点   
        draw.drawBase(x1, y1, 3, 3);  
    };  
  
    Line.prototype.drawArrowLine = function(x1, y1, x2, y2) {// 画带箭头的直线   
    	//指向坐标偏移
		var z = Math.sqrt(Math.abs(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2)));
		x1 = x1 + 10/z*(x2 - x1);
		y1 = y1 + 10/z*(y2 - y1);
		x2 = x2 - 10/z*(x2 - x1);
		y2 = y2 - 10/z*(y2 - y1);
		
        draw.drawArrowheadedLine(x1, y1, x2, y2);   
		this.s_x = x1;
		this.s_y = y1;
		this.e_x = x2;
		this.e_y = y2;
    };  
  
    Line.prototype.remove = function() {// 删除画出的线   
    	var spans = $('span[name='+g+']');
        var len = spans.length;   
        for (var i = 0; i < len; i++) {   
            idocument.body.removeChild(spans[i]);   
        }   
    };  
  
    Line.prototype.setColor = function(newColor) {// 设置线条颜色   
        color = newColor;   
    };  
  
    Line.prototype.getLinePost = function() {// 获取线条的坐标   
        return   this.s_x+"a"+this.s_y+"|"+this.e_x+"a"+this.e_y;
    };  
  
	Line.prototype.getColor = function() {// 获取线条的颜色   
        return color;   
    };  
	
	Line.prototype.getLineName = function() {// 获取线条的颜色   
        return g;   
    };  
	
	Line.prototype.setPoint = function(p_ruleid,c_ruleid){// 设置连线的开始节点和结束节点
		var h = "<input type='hidden' value='"+p_ruleid+"|"+c_ruleid+"' id='"+g+"' name='line' />";
		$('#drowDiv').append(h);
	};
}; 

// 画节点,构造函数 ruleName:规则名称 rootpath:根目录
var Node = function(rulejson, txnId, rootpath){
	 var i = rulejson.RULE_ID;   
  	 var g = rulejson.RULE_SHORTDESC;
  	 var enable = rulejson.RULE_ENABLE;
  	 var rule_txn = rulejson.RULE_TXN;
  	 var rule_desc = rulejson.RULE_DESC;
	 var s = null;
	
	 if(enable == '1') {
		 if(rule_txn != txnId) {
			 s = rootpath + "/s/tms35/images/node_status_3.bmp";
		 }else {
			 s = rootpath + "/s/tms35/images/node_status_1.bmp";
		 }
	 }
	 if(enable == '0') {
		 if(rule_txn != txnId) {
			 s = rootpath + "/s/tms35/images/node_status_4.bmp";
		 }else {
			 s = rootpath + "/s/tms35/images/node_status_0.bmp";
		 }
	 }
	 var img;
	 var draw = {
			drawNode: function(x1, y1){
				try {
					var table = "<table id="+i+" style="+"z-index:1;position:absolute;left:"+parseInt(x1)+"PX;"+"top:"+parseInt(y1)+"PX;"+">";
					table += "<tr><td valign=top><img id='"+i+"' style='left:"+parseInt(x1)+"PX;"+"top:"+parseInt(y1)+"PX;' alt='"+g+"' src='"+s+"' title=''></img></td></tr>";
					table += "<tr><td style='white-space:nowrap;' valign=top ><input type='checkbox' name='r' value='"+i+"'/>" + "<FONT>"+g+"</FONT></td></tr>";
					table += "</table>";
					$("#drowDiv").append(table);
					
					$("#drowDiv img[id='"+i+"']").tooltip({content:rule_desc, track:true, opacity:1, tooltipClass:'ui-tooltip1'});
				}
				catch (e) {
				}
			}
	 };
		
	    Node.prototype.drawImg = function(x1,y1) {// 画节点   
	        draw.drawNode(x1,y1);
    	};
		Node.prototype.getImg = function(){
			return img;
		};
		Node.prototype.getId = function(){
			return i;
		};
		Node.prototype.setImgSrc = function(src){
			s = src;
		};
};
// json对象转字符串
function Obj2str(o) {             
	if (o == undefined) {                    
		return "";                
	}                
	var r = [];                
	if (typeof o == "string") 
		return "\"" + o.replace(/([\"\\])/g, "\\$1").replace(/(\n)/g, "\\n").replace(/(\r)/g, "\\r").replace(/(\t)/g, "\\t") + "\"";                
	if (typeof o == "object") {                    
		if (!o.sort) {                        
			for (var i in o)                            
				r.push("\"" + i + "\":" + Obj2str(o[i]));                        
				if (!!document.all && !/^\n?function\s*toString\(\)\s*\{\n?\s*\[native code\]\n?\s*\}\n?\s*$/.test(o.toString)) 
				{                            
					r.push("toString:" + o.toString.toString());                        
				}                        
				r = "{" + r.join() + "}";                  
		} else {                        
			for (var i = 0; i < o.length; i++)                            
				r.push(Obj2str(o[i]));                    
				r = "[" + r.join() + "]";                    
		}                    
		return r;                
	}                
	return o.toString().replace(/\"\:/g, '":""');            
}

// json字符串转对象
function json2Object(j){
	if (j == undefined) {                    
		return "";                
	}
	return eval("("+j.replace(/(\n)/g, "\\n").replace(/(\r)/g, "\\r")+")");
}
