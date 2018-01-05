/**
 * Created by Administrator on 2017/12/18.
 */
import ajax from '@/common/ajax'
import util from '@/common/util'

var dictCode = {}

function getCodeData(cb) {
  if (util.isEmptyObject(dictCode)){
    ajax.get({
      url: '/common/dict',
      success: function (data) {
        dictCode = data.codes ? data.codes : {}
        cb()
      }
    })
  }
}

function rendCode(codeName, codeVal) {
  var items = dictCode[codeName] ? dictCode[codeName].items:[]
  codeVal = codeVal + ''
  for (var i = 0 ; i < items.length; i++){
    if(items[i].value === codeVal){
      return items[i].label
    }
  }
  return codeVal
}

function getCodeItems(codeName) {
  var [...items] = dictCode[codeName] ? dictCode[codeName].items:[]
  return items
}

function getCodeItemsCb(codeName, cb) {
  if (util.isEmptyObject(dictCode)){
    ajax.get({
      url: '/common/dict',
      success: function (data) {
        dictCode = data.codes ? data.codes : {}
        var [...items] = dictCode[codeName] ? dictCode[codeName].items:[]
        cb(items)
      }
    })
  } else {
    var [...items] = dictCode[codeName] ? dictCode[codeName].items:[]
    cb(items)
  }
}

function getCode(codeName) {
  return dictCode[codeName]
}

Date.prototype.format =function(format) {
  var o = {
    "M+" : this.getMonth()+1, //month
    "d+" : this.getDate(), //day
    "h+" : this.getHours(), //hour
    "m+" : this.getMinutes(), //minute
    "s+" : this.getSeconds(), //second
    "q+" : Math.floor((this.getMonth()+3)/3), //quarter
    "S" : this.getMilliseconds() //millisecond
  }
  if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
    (this.getFullYear()+"").substr(4- RegExp.$1.length));
  for(var k in o)if(new RegExp("("+ k +")").test(format))
    format = format.replace(RegExp.$1,
      RegExp.$1.length==1? o[k] :
        ("00"+ o[k]).substr((""+ o[k]).length));
  return format;
}

function rendDatetime(val) {
  val = parseInt(val)
  return new Date(val).format('yyyy-MM-dd hh:mm:ss');
}

export default {
  getCodeData: getCodeData,
  rendCode: rendCode,
  getCodeItems: getCodeItems,
  getCodeItemsCb: getCodeItemsCb,
  getCode: getCode,
  rendDatetime: rendDatetime
}
