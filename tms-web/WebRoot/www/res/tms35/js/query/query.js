Array.prototype.S=String.fromCharCode(2);
Array.prototype.in_array=function(e){
    var r=new RegExp(this.S+e+this.S);
    return (r.test(this.S+this.join(this.S)+this.S));
};

function isArray(obj) {
	return Object.prototype.toString.call(obj) === '[object Array]';
}

function requestsSplit(params, split) {
	var paraString = params.substring(params.indexOf("?")+1,params.length).split(split);
	var paraObj = {};
	for (var i=0; j=paraString[i]; i++){
		paraObj[j.substring(0,j.indexOf("="))] = j.substring(j.indexOf("=")+1,j.length);
	}
	return paraObj;
}

function requests() {
	var url = window.location.href;
	var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
	var paraObj = {};
	for (var i=0; j=paraString[i]; i++){
		var key = j.substring(0,j.indexOf("="));
		var value = j.substring(j.indexOf("=")+1,j.length);
		if(paraObj[key]) {
			var values = [];
			if(isArray(paraObj[key])) {
				values = paraObj[key];
				values.push(value);
			} else {
				values.push(paraObj[key]);
				values.push(value);
			}
			paraObj[key] = values;
		} else {
			paraObj[key] = value;
		}
	}
	return paraObj;
}

function request(paras) {
	var paraObj = requests();
	var returnValue = paraObj[paras];
	if(typeof(returnValue)=="undefined"){
		return "";
	}else{
		return returnValue;
	}
}

/**
 * 自适应iframe页面的高度
 * @param query_id
 */
function autoIframeHeight(query_id) {
	var iframeId = query_id + 'Iframe';
	var iframe = $(window.parent.document).find('#'+iframeId);
	if(iframe.length > 0) {
		var htmlheight = $('html').height();
		var bodyheight = $('body').height();
		var thisheight = htmlheight > bodyheight ? htmlheight : (bodyheight + 10);
		iframe.height(thisheight);
	}
}

/**
 * 格式化url中获取的参数信息
 * @param queryId		查询id，替换原url中的queryId值
 * @param delimiter		分割符，参数值之间的分割符
 * @returns
 */
function formatParamsUrl(queryId, delimiter) {
	var urlParams = requests();
	var params = [];
	$.each(urlParams, function(key, value) {
		if(key == 'queryId') {
			if(IsEmpty(queryId)) {
				params.push(key + "=" + queryId);
			}
		} else {
			if(isArray(value)) {
				for(v in value) {
					params.push(key + "=" + v);
				}
			} else {
				params.push(key + "=" + value);
			}
		}
	});
	var param = params.join(delimiter);
	return param;
}

/**
 * 获取当前URL地址中参数部分的字符串
 * @return {}
 */
function getUrlParamsString() {
	var url = window.location.href;
	return getParamsStringForUrl(url);
}

/**
 * 获取URL地址中的参数部分的字符串
 * @param {} url
 */
function getParamsStringForUrl(url) {
	var params = url.substring(url.indexOf("?")+1,url.length);
	return params;
}

/**
 * 替换当前URL参数字符串中参数的值
 * @param {} name	参数名
 * @param {} value	参数值
 */
function replaceUrlParamValue(name, value) {
	var paramsStr = getUrlParamsString();
	return replaceValueIndexForParamsString(name, 1, value, paramsStr);
}


function replaceValueForParamsString(name, value, paramsString) {
	return replaceValueIndexForParamsString(name, 1, value, paramsString);
}

/**
 * 替换参数字符串中的参数的值
 * @param name			参数名
 * @param index			参数名的索引，当存在多个同名参数时，指定替换第几个，索引值从1开始
 * @param value			参数值
 * @param paramsString	参数字符串
 */
function replaceValueIndexForParamsString(name, index, value, paramsString) {
	var params = paramsString.split('&');
	var paramObjNum = {};
	var paramsArray = [];
	for (var i=0;param = params[i];i++) {
		var size = 0;
		var _key = param.substring(0,param.indexOf("="));
		var _value = param.substring(param.indexOf("=")+1, param.length);
		if(paramObjNum[_key]) {
			size = paramObjNum[_key]*1 + 1;
		} else {
			size = 1;
		}
		if(name == _key && size*1 == index*1) {
			paramsArray.push(_key + "=" + value);
		} else {
			paramsArray.push(_key + "=" + _value);
		}
		paramObjNum[_key] = size;
	}
	return paramsArray.join('&');
}

function formatParamsObj(paramsObj, delimiter) {
	var params = [];
	$.each(paramsObj, function(key, value) {
		params.push(key + "=" + value);
	});
	var param = params.join(delimiter);
	return param;
}

/**
 * 格式化url，url中的参数不变，把params中的参数兼并到url中
 * @param url		url地址
 * @param params	要合并到url中的参数
 * @param split	params参数见的分割符
 */
function formatUrl(url, params, split) {
	var urlParams = requestsSplit(url, '&');
	url = url.substring(0,url.indexOf('?'));
	var _params = IsEmpty(params) ? params.split(split) : '';
	if(IsEmpty(_params)) {
		for(var i=0;i<_params.length;i++) {
			var param = _params[i];
			if(!IsEmpty(urlParams[param.substring(0, param.indexOf('='))])) {
				urlParams[param.substring(0, param.indexOf('='))] = param.substring(param.indexOf('=')+1, param.length);
			}
		}
	}
	url += urlParams.join('&');
}

/**
 * 两个参数串合并，param1兼并param2
 * @param param1
 * @param param2
 */
function annexParams(param1, param2) {
	$.each(param2, function(key, value) {
		if(!IsEmpty(param1[key])) {
			param1[key] = value;
		}
	});
	return param1;
}

/**
 * 动态加载js代码
 * @param sId
 * @param source
 */
function includeJS(sId, source) {
	if ((source !== null) && (!document.getElementById(sId))) {
        var oHead = document.getElementsByTagName('HEAD').item(0);
        var oScript = document.createElement("script");
        oScript.language = "javascript";
        oScript.type = "text/javascript";
        oScript.id = sId;
        oScript.defer = true;
        oScript.text = source;
        oHead.appendChild(oScript);
    }
}

/**
 * 通过当前页面查找父窗口中的元素
 * @param self当前窗口的jquery对象window|self
 **/
function findParentWindowObj(self) {
	//var _self = $(self);
	var _parent = $(self.parent);//获取父窗口
	if(self.location.href == self.parent.location.href) {
		return false;
	}
	if(_parent.length > 0) {//是否存在父窗口
		var back = $(_parent[0].document).find('#queryBackDiv :visible');//在父窗口的dom中查找返回按钮
		if(back.length > 0) {
			return true;
		} else {
			return findParentWindowObj(_parent[0]);
		}
	} else {
		return false;
	}
}