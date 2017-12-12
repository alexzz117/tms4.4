// JavaScript Document

//验证是否为空
function checkNull(objId,msg){
	var value = $("#"+objId).val();
	if(value==null || value==""){
		alert(msg);
		return;
	}
}
//比较日期,第一个日期是否比第二个日期晚
function compareDate(start,end){
	try{
		if(start!=null && start!='' && end!=null && end!=''){
			start = start.replace("-","/");
			end = end.replace("-","/");
			var sdate = new Date(Date.parse(start));
			var edate = new Date(Date.parse(end));
			return sdate>edate;
		}
	}catch(e){
		alert('日期格式输入错误！');
		return false;
	}
}


//验证是否含有特殊字符
function checkeSpecialCode(str){
//	var reg = "~`,!#$￥%^&*{}][【】《》><?？/\\;'；！";	//特殊字符
	var reg = "~`!！：:；;#@$￥%^&*{}][【】《》><?？/\\'";	//特殊字符（描述信息可能会有中英文分号、叹号）
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