/**
 * @description 工具函数库
 * @author wuruiqi
 * @createtime 2017-12-06
 */

import crypt from '@/common/crypt'
var toString = Object.prototype.toString,
  hasOwn = Object.prototype.hasOwnProperty,
  push = Array.prototype.push,
  slice = Array.prototype.slice,
  trim = String.prototype.trim,
  indexOf = Array.prototype.indexOf;

// [[Class]] -> type pairs
var class2type = {};

function isFunction( obj ) {
  return type(obj) === "function";
}

function isArray( obj ) {
  return type(obj) === "array";
}

function isWindow( obj ) {
  return obj != null && obj == obj.window;
}

function isNumeric( obj ) {
  return !isNaN( parseFloat(obj) ) && isFinite( obj );
}

/*

 IsNumber(string,string,string):

 功能：判断是否为浮点数、整数

 add by caiqian 2012-2-16修改校验浮点数，只需判断是否是数字，小数点后保留几位
 sign:null表示没有正负号，"+"：正；"-"：负
 deci:小数点的精度，如果此参数没有，默认是无限浮点数，如果为0，表示整数，如果不为0，表示具体的精度
 */

function isNumber(objStr,sign,deci)
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

function type( obj ) {
  return obj == null ?
    String( obj ) :
    class2type[ toString.call(obj) ] || "object";
}

function isPlainObject( obj ) {
  // Must be an Object.
  // Because of IE, we also have to check the presence of the constructor property.
  // Make sure that DOM nodes and window objects don't pass through, as well
  if ( !obj || type(obj) !== "object" || isWindow( obj ) ) {
    return false;
  }

  try {
    // Not own constructor property must be Object
    if ( obj.constructor &&
      !hasOwn.call(obj, "constructor") &&
      !hasOwn.call(obj.constructor.prototype, "isPrototypeOf") ) {
      return false;
    }
  } catch ( e ) {
    // IE8,9 Will throw exceptions on certain host objects #9897
    return false;
  }

  // Own properties are enumerated firstly, so to speed up,
  // if last one is own, then all properties are own.

  var key;
  for ( key in obj ) {}

  return key === undefined || hasOwn.call( obj, key );
}

function isEmptyObject( obj ) {
  for ( var name in obj ) {
    return false;
  }
  return true;
}

function error( msg ) {
  throw new Error( msg );
}

function parseJSON( data ) {
  if ( typeof data !== "string" || !data ) {
    return null;
  }

  // Make sure leading/trailing whitespace is removed (IE can't handle it)
  data = util.trim( data );

  // Attempt to parse using the native JSON parser first
  if ( window.JSON && window.JSON.parse ) {
    return window.JSON.parse( data );
  }

  // Make sure the incoming data is actual JSON
  // Logic borrowed from http://json.org/json2.js
  if ( rvalidchars.test( data.replace( rvalidescape, "@" )
      .replace( rvalidtokens, "]" )
      .replace( rvalidbraces, "")) ) {

    return ( new Function( "return " + data ) )();

  }
  error( "Invalid JSON: " + data );
}

function noop() {}

function toggleCase(oldObj, newObj, flag){
  if (flag === 'lower') {
    for (var key in oldObj) {
      if (util.isPlainObject(oldObj[key]) || util.isArray(oldObj[key])) {
        newObj[key.toLowerCase()] = toggleObjKey(oldObj[key], flag)
      } else {
        newObj[key.toLowerCase()] = oldObj[key];
      }
    }
  } else if (flag === 'upper') {
    for (var key in oldObj) {
      if (util.isPlainObject(oldObj[key]) || util.isArray(oldObj[key])) {
        newObj[key.toUpperCase()] = toggleObjKey(oldObj[key], flag)
      } else {
        newObj[key.toUpperCase()] = oldObj[key];
      }
    }
  }

  return newObj;
}

/**
 * @description 把对象的key值进行大小写转换
 * @param obj Object or Array
 * @param flag String lower or upper, 默认值lower
 */
function toggleObjKey(obj, flag){
  var newObj, caseFlag;
  caseFlag = flag ? flag : 'lower';

  if (isPlainObject(obj)){
    newObj = {};
    return toggleCase(obj, newObj, caseFlag)
  } else if(util.isArray(obj)){
    newObj = [];
    return toggleCase(obj, newObj, caseFlag)
  } else {
    return obj;
  }
}

/**
 * @description 格式化金额
 * @param s 传入数值
 * @param n 小数点保留位数，默认2位
 * @param f 千位分隔符，默认无
 */
function formatMoney(s, n, f) {
  if (IsNumber(s)){
    n = n > 0 && n <= 20 ? n : 2;
    f = typeof (f) == 'undefined' ? "" : f;
    s = parseFloat((s + "").replace(/[^\d\.-]/g, ""));

    var fixNum = new Number(s + 1).toFixed(n);
    var fixedNum = new Number(fixNum - 1).toFixed(n);
    s = fixedNum + "";

    var l = s.split(".")[0].split("").reverse();
    var r = s.split(".")[1];
    var t = "";
    for( var i = 0; i < l.length; i++ ) {
      t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? f : "");
    }
    return t.split("").reverse().join("") + "." + r.substring(0, n);
  }
  return '';
}

/**
 * @description 判断字符串是否为'',null,undefined
 * @param str  String
 */
function isEmpty(str) {
  if (str === '' || str === null || str === undefined){
    return true
  } else {
    return false
  }
}

function renderDate(v){
  if (v == 0 || v =='undefined' || v==null || v=='') {
    return ''
  }
  try{
    var d = new Date(parseInt(v, 10));
    var month = (d.getMonth() + 1).toString();
    var year = (d.getFullYear()).toString();
    var day = (d.getDate()).toString();
    return [year,
      (month.length < 2 ? "-0":"-"), month,
      (day.length < 2 ? "-0":"-"), day
    ].join("");
  }catch(e){
    return v;
  }
}
function renderDateTime(v){
  if (v == 0 || v =='undefined' || v==null || v=='') {
    return ''
  }
  try{
    var d = new Date(parseInt(v, 10));
    var month = (d.getMonth() + 1).toString();
    var year = (d.getFullYear()).toString();
    var day = (d.getDate()).toString();
    var hour = (d.getHours()).toString();
    var min = (d.getMinutes()).toString();
    var sec = (d.getSeconds()).toString();
    return [year,
      (month.length < 2 ? "-0":"-"), month,
      (day.length < 2 ? "-0":"-"), day,
      (hour.length < 2 ? " 0":" "), hour,
      (min.length < 2 ? ":0":":"), min,
      (sec.length < 2 ? ":0":":"), sec
    ].join("");
  }catch(e){
    return v;
  }
}

function getWebRootPath () {
  var pathName = window.document.location.pathname
  var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1)
  return projectName
}

let fbsArr = ['\\', '$', '(', ')', '*', '+', '.', '[', ']', '?', '^', '{', '}', '|']
function escapeExprSpecialWord (val) {
  if (!val || val === '') {
    return ''
  }
  for (let key of fbsArr) {
    if (val.includes(key)) {
      val = val.replace(key, '\\' + key)
    }
  }
  return val
}
function orderList (list, key) {
  let resultList = []
  if (list !== undefined && key !== undefined) {
    resultList = resultList.concat(list)
    let len = resultList.length
    while (len > 1) {
      for (let i = 0; i < len - 1; i++) {
        let buff = null
        let item = resultList[i]
        let next = resultList[i + 1]
        if (Number(item[key]) < Number(next[key])) {
          buff = item
          resultList[i] = next
          resultList[i + 1] = buff
        }
      }
      len--
    }
  }
  return resultList
}
var util = {
  escapeExprSpecialWord: escapeExprSpecialWord,
  getWebRootPath: getWebRootPath,
  isFunction: isFunction,
  renderDate: renderDate,
  renderDateTime: renderDateTime,
  isArray: Array.isArray || isArray,
  isWindow: isWindow,
  isNumeric: isNumeric,
  isNumber: isNumber,
  type: type,
  isPlainObject: isPlainObject,
  isEmptyObject: isEmptyObject,
  error: error,
  parseJSON: parseJSON,
  noop: noop,
  orderList: orderList,
  trim: trim ?
    function( text ) {
      return text == null ?
        "" :
        trim.call( text );
    } :

    // Otherwise use our own trimming functionality
    function( text ) {
      return text == null ?
        "" :
        text.toString().replace( trimLeft, "" ).replace( trimRight, "" );
    },
  toggleObjKey: toggleObjKey,
  formatMoney: formatMoney,
  isEmpty: isEmpty,
  extend: function() {
    var options, name, src, copy, copyIsArray, clone,
      target = arguments[0] || {},
      i = 1,
      length = arguments.length,
      deep = false;

    // Handle a deep copy situation
    if ( typeof target === "boolean" ) {
      deep = target;
      target = arguments[1] || {};
      // skip the boolean and the target
      i = 2;
    }

    // Handle case when target is a string or something (possible in deep copy)
    if ( typeof target !== "object" && !util.isFunction(target) ) {
      target = {};
    }

    // extend util itself if only one argument is passed
    if ( length === i ) {
      target = this;
      --i;
    }

    for ( ; i < length; i++ ) {
      // Only deal with non-null/undefined values
      if ( (options = arguments[ i ]) != null ) {
        // Extend the base object
        for ( name in options ) {
          src = target[ name ];
          copy = options[ name ];

          // Prevent never-ending loop
          if ( target === copy ) {
            continue;
          }

          // Recurse if we're merging plain objects or arrays
          if ( deep && copy && ( util.isPlainObject(copy) || (copyIsArray = util.isArray(copy)) ) ) {
            if ( copyIsArray ) {
              copyIsArray = false;
              clone = src && util.isArray(src) ? src : [];

            } else {
              clone = src && util.isPlainObject(src) ? src : {};
            }

            // Never move original objects, clone them
            target[ name ] = util.extend( deep, clone, copy );

            // Don't bring in undefined values
          } else if ( copy !== undefined ) {
            target[ name ] = copy;
          }
        }
      }
    }

    // Return the modified object
    return target;
  },
  serializeObj: function(obj) {
    var str = ''
    if (util.isPlainObject(obj)) {
      for(var key in obj) {
        str += `&${key}=${obj[key]}`
      }
    }
    return str
  },
  crypt: crypt,
  // 把功能节点列表格式化为树形Json结构
  formatTreeData (list, rootNodes) {
    var tree = []
    // 如果根节点数组不存在，则取fid不存在或为空字符的节点为父节点
    if (rootNodes === undefined || rootNodes.length === 0) {
      rootNodes = []
      for (var i in list) {
        if (list[i].fid === undefined || list[i].fid === null || list[i].fid === '' || list[i].fid === '-1') {
          if (list[i].id !== undefined && list[i].id !== null && list[i].id !== '') {
            rootNodes.push(list[i])
          }
        }
      }
    }
    // 根节点不存在判断
    if (rootNodes.length === 0) {
      console.error('根节点不存在，请确认树结构是否正确')
      console.info('树结构的根节点是fid不存在（或为空）的节点，否则需手动添加指定得根节点（参数）')
    }
    // 根据根节点遍历组装数据
    for (var r in rootNodes) {
      var node = rootNodes[r]
      node.children = getChildren(list, node.id)
      tree.push(node)
    }

    // 递归查询节点的子节点
    function getChildren (list, id) {
      var childs = []
      for (var i in list) {
        var node = list[i]
        if (node.fid === id) {
          node.children = getChildren(list, node.id)
          // node.icon = 'el-icon-message'
          childs.push(node)
        }
      }
      return childs
    }

    return tree  // 返回树结构Json
  },
  // 展开前几层功能树
  expendNodesByLevel (node, deep) {
    let nodeIds = []
    if (deep > 0) {
      deep--
      for (let i = 0; i < node.length; i++) {
        nodeIds.push(node[i].id)
        if (deep > 0 && node[i].children && node[i].children.length > 0) {
          nodeIds = nodeIds.concat(util.expendNodesByLevel(node[i].children, deep))
        }
      }
    }
    return nodeIds
  },
  // 获取勾选的树节点
  checkKeys (treedata, name, val) {
    let keys = []
    for (let i = 0; i < treedata.length; i++) {
      let item = treedata[i]
      if (item[name] === val) {
        keys.push(item.id)
      }

      if (item.children) {
        keys = keys.concat(util.checkKeys (item.children, name, val))
      }
    }
    return keys
  },
  // 查找list数组中对应value的label值
  renderCellValue (list, cellValue) {
    cellValue = cellValue + ''
    for (var i = 0 ; i < list.length; i++){
      if(list[i].value === cellValue){
        return list[i].label
      }
    }
    return cellValue
  },
  // 获取cookie
  getCookie (name) {
    var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
    if (arr = document.cookie.match(reg))
      return (arr[2]);
    else
      return null;
  },
  // 设置cookie
  setCookie (c_name, value, expiredays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + expiredays);
    document.cookie = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
  },
  // 删除cookie
  delCookie (name) {
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval = getCookie(name);
    if (cval != null)
      document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
  }
}

export default util
