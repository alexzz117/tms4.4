/*

       名字：commoncheck.js  by changzhu

       功能：通用javascript脚本函数库

       包括：

            1.Trim(str)－－去除字符串两边的空格

            2.XMLEncode(str)－－对字符串进行XML编码

            3.ShowLabel(str,str)－－鼠标提示功能（显示字符，提示字符）

            4.IsEmpty(obj)－－验证输入框是否为空

            5.IsInt(objStr,sign,zero)－－验证是否为整数

            6.IsFloat(objStr,sign,zero)－－验证是否为浮点数

			7.isIP(strIP) -- 校验ip地址的格式

*/

/*字符串操作

Trim(string):去除字符串两边的空格

*/

/*
　　１．LTrim(string):去除左边的空格
*/

function LTrim(str)

{
    var whitespace = new String(" \t\n\r");
    var s = new String(str);

    if (whitespace.indexOf(s.charAt(0)) != -1)
    {
        var j=0, i = s.length;

        while (j < i && whitespace.indexOf(s.charAt(j)) != -1)
        {
            j++;
        }

        s = s.substring(j, i);
    }

    return s;
}

 

/*
　　２．RTrim(string):去除右边的空格
*/

function RTrim(str)

{

    var whitespace = new String(" \t\n\r");

    var s = new String(str);

 

    if (whitespace.indexOf(s.charAt(s.length-1)) != -1)

    {

        var i = s.length - 1;

        while (i >= 0 && whitespace.indexOf(s.charAt(i)) != -1)

        {

            i--;

        }

        s = s.substring(0, i+1);

    }

    return s;

}

 

/*
　　３．Trim(string):去除前后空格
*/

function Trim(str)

{

    return RTrim(LTrim(str));

}

/*

　　XMLEncode(string):对字符串进行XML编码

*/

function XMLEncode(str)

{

       str=Trim(str);

       str=str.replace("&","&amp;");

       str=str.replace("<","&lt;");

       str=str.replace(">","&gt;");

       str=str.replace("'","&apos;");

       str=str.replace("\"","&quot;");

       return str;

}

/*
验证类函数
*/

function IsEmpty(objStr)

{

	if(objStr==null||objStr=='null'||objStr==''||objStr==undefined||objStr=='undefined')
    {
        return false;
    }
    else
    {        
        return true;     

    }

}



/*
验证类综合函数
*/

function IsCheck(obj,zero)

{
	 var objStrvalue=obj.value;
	 var checkvnull=IsEmpty(objStrvalue);
	 var strmessage='';
		if(zero==1)
		{
			//strmessage='计分开始次数';
			strmessage='命中次数';
		}
		if(zero==2)
		{
			strmessage='计分停止次数';
		}
		if(zero==3)
		{
			strmessage='分值';
		}
		if(zero==4)
		{
			strmessage='权重';
		}
		if(zero==5)
		{
			strmessage='统计范围 ';
		}
	 if(checkvnull==false )
     {
    	alert(strmessage+"不能为空");
        return obj.focus();
     }
	var checkvBool=IsInt(objStrvalue,"+",0);
	 
	 //范围为1-100
 if(zero==1 || zero==2 || zero==5)
 {
	var checkvlimit=IsCheckLimitone(objStrvalue,"+");
		 if(checkvBool == false || checkvlimit==false )
	     {
	    	alert(strmessage+"请填写1-100的整数");
	        return obj.focus();
	     }
 }else
 {
		var checkvlimit=IsCheckLimit(objStrvalue,"+");
		 if(checkvBool == false || checkvlimit==false )
	     {
	    	alert(strmessage+"请填写0-100的整数");
	        return obj.focus();
	     }
 }

}



/*
验证类综合函数
*/

function IsCheckselect(obj,i,j,vv,zero)

{

	 var objStrvalue=obj;
	 var checkvnull=true;
	 var strmessage='';
	 var number=i+1;
	 if(zero==1)
		{
			strmessage='对象类型 ';
			checkvnull=IsEmpty(objStrvalue);
		}
	 if(zero==2)
		{
			strmessage='条件域内交易函数 ';
			var feature =objStrvalue.split(",");
			//如果两者都为空，弹出错误，有一方不为空。为正常值
			checkvnull=IsEmpty(feature[0]);
			if(checkvnull==false)
			{
			checkvnull=IsEmpty(feature[1]);
			}
		}
	 if(zero==3)
		{
			strmessage='条件域内表达式 ';
			checkvnull=IsEmpty(objStrvalue);
		}
	 if(zero==4)
		{
			strmessage='条件域内交易函数 ';
			if(objStrvalue==-1)
			{
			checkvnull=false;
			}
		}
	 
	 if(checkvnull==false)
     {
    	alert("第"+number+"行"+strmessage+"请选择下拉列表的值");
        return false;
     }
}




/*
行内验证类综合函数
*/

function IsRowCheckselect(obj,i,j,vv,zero)

{

	 var objStrvalue=obj;
	 var checkvnull=true;
	 var strmessage='';
	 var number=i+1;
	 if(zero==1)
		{
			strmessage='对象类型 ';
			checkvnull=IsEmpty(objStrvalue);
		}
	 if(zero==2)
		{
			strmessage='交易函数 ';
			var feature =objStrvalue.split(",");
			//如果两者都为空，弹出错误，有一方不为空。为正常值
			checkvnull=IsEmpty(feature[0]);
			if(checkvnull==false)
			{
			checkvnull=IsEmpty(feature[1]);
			}
		}
	 if(zero==3)
		{
			strmessage='表达式 ';
			checkvnull=IsEmpty(objStrvalue);
		}
	 if(checkvnull==false)
     {
    	alert("第"+number+"大行"+"第"+j+"小行"+strmessage+"请选择下拉列表的值");
        return false;
     }
}

/*
　　IsInt(string,string,int or string):(测试字符串,+ or - or empty,empty or 0)

　　功能：判断是否为整数、正整数、负整数、正整数+0、负整数+0
*/

function IsInt(objStr,sign,zero)

{

    var reg;    

    var bolzero;    

    

    if(Trim(objStr)=="")

    {

        return false;

    }

    else

    {

        objStr=objStr.toString();

    }    

    

    if((sign==null)||(Trim(sign)==""))
    {
        sign="+-";
    }

    if((zero==null)||(Trim(zero)==""))
    {
        bolzero=false;
    }
    else
    {
        zero=zero.toString();
        if(zero=="0")
        {
            bolzero=true;
        }
        else
        {
            alert("检查是否包含0参数，只可为(空、0)");
        }
    }

    switch(sign)

    {

        case "+-":

            //整数

            reg=/(^-?|^\+?)\d+$/;            

            break;

        case "+": 

            if(!bolzero)           

            {

                //正整数

                reg=/^\+?[0-9]*[1-9][0-9]*$/;

            }

            else

            {

                //正整数+0

                //reg=/^\+?\d+$/;

                reg=/^\+?[0-9]*[0-9][0-9]*$/;

            }

            break;

        case "-":

            if(!bolzero)

            {

                //负整数

                reg=/^-[0-9]*[1-9][0-9]*$/;

            }

            else

            {

                //负整数+0

                //reg=/^-\d+$/;

                reg=/^-[0-9]*[0-9][0-9]*$/;

            }            

            break;

        default:

            alert("检查符号参数，只可为(空、+、-)");

            return false;

            break;

    }

    var r=objStr.match(reg);

    if(r==null)

    {

        return false;

    }

    else

    {        

        return true;     

    }

}
/*

IsNumber(string,string,string):

功能：判断是否为浮点数、整数

add by caiqian 2012-2-16修改校验浮点数，只需判断是否是数字，小数点后保留几位
sign:null表示没有正负号，"+"：正；"-"：负
deci:小数点的精度，如果此参数没有，默认是无限浮点数，如果为0，表示整数，如果不为0，表示具体的精度
*/

function IsNumber(objStr,sign,deci)
{
  var regStr="^";
  if(!sign){
  	regStr+="-?";
  }else if(sign=="-"){
  	regStr+="-";
  }
  if(!deci){       	
	  regStr += "\\d+(\\.\\d+)?$";
  }else if(deci=="0"){
	  regStr += "(0|([1-9]+\\d*))$";
  }else{
	  regStr += "\\d+(\\.\\d{1,"+deci+"})?$";
  }
  var regu = new RegExp(regStr,"g"); 
  return regu.test(objStr);
}

/*
IsCheckLimit(int):(1--100)

功能：判断是否为为制定区间的整数、正整数、负整数、正整数+0、负整数+0
*/

function IsCheckLimitone(objStr)

{
	if (objStr.length<=3 && parseInt(objStr)>=1 && parseInt(objStr)<=100 ) 
	{
		return true;
	}else
	{
		return false;
	}

}



/*
IsCheckLimit(int):(0--100)

功能：判断是否为为制定区间的整数、正整数、负整数、正整数+0、负整数+0
*/

function IsCheckLimit(objStr)

{
	var reg=/^\d+$/;
	var flag = reg.test(objStr);
	if (flag && objStr.length<=3 && parseInt(objStr)>=0 && parseInt(objStr)<=100 ) 
	{
		return true;
	}else
	{
		return false;
	}

}



//名字是否重复新增
function checksomenameadd(obj) {
	//校验是否有特殊字符
	var ruleNamecheck=checkeSpecialCode(obj.value);
     if(ruleNamecheck==false )
       {
      	alert('规则名称不允许有特殊字符!');
      	return obj.focus();
       }
//          var ruleName = "ruleName="+obj.value+","+"0";
//		  jcl.postJSON('/tms/rule/getRuleByName', ruleName, function(data){
//          if(data.row.isexit=='YES')
//          {
//        	  alert('规则名称不允许重名!');
//  	          return obj.focus();
//          }else
//          { 
//      		  return true;
//          }   
//		  });
}


//名字是否重复 编辑
function checksomenameedit(obj) {
	
	   //校验是否有特殊字符
	    var ruleNamecheck=checkeSpecialCode(obj.value);
        if(ruleNamecheck==false )
        {
       	alert('规则名称不允许有特殊字符!');
      	return obj.focus();
        }
     
//          var ruleName = "ruleName="+obj.value+","+"1";
//		  jcl.postJSON('/tms/rule/getRuleByName', ruleName, function(data){
//          if(data.row.isexit=='YES')
//          {
//        	  alert('规则名称不允许重名!');
//  	          return obj.focus();
//          }else
//          { 
//      		  return true;
//          }   
//		  });
}
/*

　　IsFloat(string,string,int or string):(测试字符串,+ or - or empty,empty or 0)

　　功能：判断是否为浮点数、正浮点数、负浮点数、正浮点数+0、负浮点数+0

*/

function IsFloat(objStr,sign,zero)

{

    var reg;    

    var bolzero;    

    

    if(Trim(objStr)=="")

    {

        return false;

    }

    else

    {

        objStr=objStr.toString();

    }    

    

    if((sign==null)||(Trim(sign)==""))

    {

        sign="+-";

    }

    

    if((zero==null)||(Trim(zero)==""))

    {

        bolzero=false;

    }
    else
    {

        zero=zero.toString();

        if(zero=="0")
        {

            bolzero=true;

        }
        else
        {

            alert("检查是否包含0参数，只可为(空、0)");

        }

    }

    switch(sign)

    {

        case "+-":

            //浮点数

            reg=/^((-?|\+?)\d+)(\.\d+)?$/;

            break;

        case "+": 

            if(!bolzero)           

            {

                //正浮点数

                reg=/^\+?(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/;

            }

            else

            {

                //正浮点数+0

                reg=/^\+?\d+(\.\d+)?$/;

            }

            break;

        case "-":

            if(!bolzero)

            {

                //负浮点数

                reg=/^-(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/;

            }

            else

            {

                //负浮点数+0

                reg=/^((-\d+(\.\d+)?)|(0+(\.0+)?))$/;

            }            

            break;

        default:

            alert("检查符号参数，只可为(空、+、-)");

            return false;

            break;

    }

    

    var r=objStr.match(reg);

    if(r==null)
    {
        return false;
    }
    else
    {
        return true;
    }
}


/* 用途：校验ip地址的格式 
	
   输入：strIP：ip地址 
	
   返回：如果通过验证返回true,否则返回false； 
*/ 
	
function isIP(strIP) { 
		
	var re=/^(\d+)\.(\d+)\.(\d+)\.(\d+)$/g //匹配IP地址的正则表达式 
		
	if(re.test(strIP)) 
		
	{ 
		if( RegExp.$1 <256 && RegExp.$2<256 && RegExp.$3<256 && RegExp.$4<256) return true; 
	} 
		
	return false; 
	
} 




//验证是否含有特殊字符
function checkeSpecialCode(str){
//	var reg = "~`,!#$￥%^&*{}][【】《》><?？/\\;'；！";	//特殊字符
	var reg = "~`#$￥%^&*{}][【】《》><?？/\\'";	//特殊字符（描述信息可能会有中英文分号、叹号）
	for(var j=0;j<reg.length;j++){
		if(str.indexOf(reg.charAt(j))!=-1){
			return false;
			break;
		};
	}
	
//	var reg = /^[a-zA-Z0-9_():-（）\u4e00-\u9fa5]+$/				//数字、字母、汉字、下划线、中横线、英文冒号、空格、中英文括号
//	for(var j=0;j<str.length;j++){
//		if(!reg.test(str.charAt(j))){
//			return false;
//			break;
//		};
//	}
	return true;
}
//验证字符串长度
function checkeLength(str,strlength){
//	var reg = /^[\u4e00-\u9fa5]+$/				//验证汉字
//	var hzcount=0;
//	var scount =0;
//	for(var j=0;j<str.length;j++){
//		if(reg.test(str.charAt(j))){
//			hzcount++;
//		}else if(escape(str.charAt(j))=='%uFF1A' || escape(str.charAt(j))=='%uFF1B' 
//			|| escape(str.charAt(j))=='%uFF01' || escape(str.charAt(j))=='%uFF08' 
//			|| escape(str.charAt(j))=='%uFF09'){//中文冒号、分号、叹号及括号
//			hzcount++;
//		}else if(escape(str.charAt(j))=='%0A') {//验证回车
//			scount=scount+2;
//		}else{
//			scount++;
//		}
//	}
//	var length1 = hzcount*3+scount;
	var length = str.lengths();//调用jcl公用组件中的方法计算长度
	if(strlength==null || strlength==''){
		if(length>32){
			return false;
		}
	}else{
		if(length>parseInt(strlength)){
			return false;
		}
	}
	return true;
}
