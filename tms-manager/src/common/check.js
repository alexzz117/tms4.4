/**
 * Created by Administrator on 2017/12/13.
 */

var check = {
  /**
   * @description 判断字符串是否含有特殊字符
   * @param str  String
   */
  checkSpecialCode: function (str){
    if (isEmpty(str)) {
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
    if (!this.checkSpecialCode(value)){
      return callback(new Error('请不要输入特殊字符！如($,%)'));
    }
  },
  checkFormEnSpecialCharacter: function (rule, value, callback) {
    if (!this.checkSpecialCharacter(value, 1)){
      return callback(new Error('请以字母开头，包括字母、数字和下划线'));
    }
  },
  checkFormZhSpecialCharacter: function (rule, value, callback) {
    if (!this.checkSpecialCharacter(value, 2)){
      return callback(new Error('请不要输入特殊字符！如($,%)'));
    }
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
   * return 带有交易属性的提示
   */
  checkSpecialCharacterInTxn: function (desc, src, type) {
    var legal = checkSpecialCharacter(src, type);
    if (!legal){
      alert(desc + "只能包含汉字,字母,数字和下划线");
    }
    return legal;
  }
}
export default check
