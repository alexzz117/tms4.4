/**
 * Created by Administrator on 2017/12/13.
 */
import util from '@/common/util'

var check = {
  /**
   * @description 判断字符串是否含有特殊字符
   * @param str  String
   */
  checkSpecialCode: function (str){
    if (util.isEmpty(str)) {
      return true
    }

    var reg = "~`#$￥%^&*{}=][【】《》><?？/\\'";	//特殊字符（描述信息可能会有中英文分号、叹号）
    for(var j=0;j<reg.length;j++){
      if(str.indexOf(reg.charAt(j))!=-1){
        return false;
        break;
      };
    }

    return true;
  },
  checkFormSpecialCode: function (rule, value, callback) {
    if (!check.checkSpecialCode(value)){
      return callback(new Error('请不要输入特殊字符！如($,%)'));
    }
    callback()
  },
  checkFormEnSpecialCharacter: function (rule, value, callback) {
    if (!check.checkSpecialCharacter(value, 1)){
      return callback(new Error('请以字母开头，包括字母、数字和下划线'));
    }
    callback()
  },
  checkFormZhSpecialCharacter: function (rule, value, callback) {
    if (!check.checkSpecialCharacter(value, 2)){
      // return callback(new Error('请不要输入特殊字符！如($,%)'));
      return callback(new Error('只能包含汉字,字母,数字和下划线'));
    }
    callback()
  },
  checkFormZhSpecialCharacterHasPunctuation: function (rule, value, callback) {
    if (!check.checkSpecialCharacterHasPunctuation(value)){
      // return callback(new Error('请不要输入特殊字符！如($,%)'));
      return callback(new Error('只能包含汉字,字母,数字,下划线,逗号,句号,问号'));
    }
    callback()
  },
  checkFormIp: function (rule, value, callback) {
    if(value == null || value === '') {
      callback()
    }
    if (!/((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)/.test(value)){
      return callback(new Error('请输入正确的IP地址'));
    }
    callback()
  },
  /*
   * 校验特殊字符
   * type 1
   * 代码:字母开头 + 字母,数字,下划线(位置无区别)
   * type 2
   * 名称:汉字,字母,数字,下划线(位置无区别)
   */
  checkSpecialCharacter: function (src, type) {
    return type == '1'
      ? /^[a-zA-Z][\w]*$/.test(src)
      : /^[\w\u4e00-\u9fa5]*$/.test(src);
  },
  /*
   * 校验特殊字符
   * 名称:汉字,字母,数字,下划线(位置无区别)
   */
  checkSpecialCharacterHasPunctuation: function (src) {
    return /^[\w\u4e00-\u9fa5,\.，。!！\?？]*$/.test(src);
  },
  /*
   * 校验特殊字符
   * return 带有交易属性的提示
   */
  checkSpecialCharacterInTxn: function (desc, src, type) {
    var legal = check.checkSpecialCharacter(src, type);
    if (!legal){
      alert(desc + "只能包含汉字,字母,数字和下划线");
    }
    return legal;
  },
  /* 用途：校验ip地址的格式
   输入：strIP：ip地址
   返回：如果通过验证返回true,否则返回false；
   */

  isIP: function (strIP) {
    var re=/^(\d+)\.(\d+)\.(\d+)\.(\d+)$/g //匹配IP地址的正则表达式
    if(re.test(strIP)) {
      if( RegExp.$1 <256 && RegExp.$2<256 && RegExp.$3<256 && RegExp.$4<256) return true;
    }
    return false;
  },
  /**
   * @description 密码检测密码强度
   * @param String pwd 密码
   */
  checkStrength: function(pwd) {
    var modes = 0;
    //正则表达式验证符合要求的
    if (pwd.length < 1)
      return modes;
    if (/\d/.test(pwd))
      modes++; //数字
    if (/[a-z]/.test(pwd))
      modes++; //小写
    if (/[A-Z]/.test(pwd))
      modes++; //大写
    if (/\W/.test(pwd))
      modes++; //特殊字符

    if (modes >= 3 && pwd.length > 10) {
      modes = 4;
    }
    return modes;
  }
}
export default check
