import util from '@/common/util'

/**
 * 运行报表打印
 * @author wsy 2018-2-6
 * @param options
 */
function reportPrint (options) {
  var opts = util.extend({
    queryBox: {
      queryTitle: '查询标题', // 查询标题
      queryForm: [{         // 查询条件
        label: '',
        value: ''
      }]
    },
    chartBox: {
      chartTitle: '图表标题', // 图表标题
      chartForm: [{         // 图表筛选条件
        label: '',
        value: ''
      }],
      chartDiv: 'chartdiv', // 图表主键
      chartUrl: null        // 图表地址
    },
    tablebBox: {
      tableTitle: '表格标题', // 表格标题
      columns: [{            // 表格表头
        name: null, dataIndex: null, items: [{name: null, dataIndex: null}]
      }],
      tableData: []        // 表格数据
    },
    minWidth: 960,
    width: window.innerWidth,
    height: window.innerHeight
  }, options);

  // var noPrintType = ['empty', 'hidden'];
  var printHtml = [
    '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">',
    '<html><head><meta http-equiv="X-UA-Compatible" content="IE=edge" /><title>打印视图</title><style type="text/css" media="screen,print">',
    'body,p,span,div,li,td,input,textarea,select{font-family:sans-serif; font-size:9pt;}',
    '.title{text-align: left; font-weight: bold; line-height: 22px; padding:3px;}',
    '.table{ border:1px solid #999; border-collapse:collapse; width:100%;}',
    '.table th,.table td{ border:1px solid #999; border-collapse:collapse; padding:3px;}',
    '.table th{line-height:20px;}</style></head><body>',
    '<div><input type="button" value="打印" onclick="this.style.display=\'none\';window.print();this.style.display=\'block\';" /></div>',
    _getPrintHtml(),
    '</body></html>'
  ];

  function _getPrintHtml () {
    var tmp = [];
    if (opts.queryBox) {
      tmp.push('<table class="table"><caption class="title">' + opts.queryBox.queryTitle + '</caption>');
      tmp.push('<colgroup><col width="15%" /><col width="35%" /><col width="15%" /><col width="35%" /></colgroup>');
      tmp.push('<tbody>');
      if (opts.queryBox.queryForm) {
        _printFormForTBody(opts.queryBox.queryForm, tmp, true, 2);
      }
      tmp.push('</tbody></table>');
    }
    if (opts.chartBox) {
      tmp.push('<table class="table"><caption class="title">' + opts.chartBox.chartTitle + '</caption><tbody>');
      if (opts.chartBox.chartForm) {
        _printFormForTBody(opts.chartBox.chartForm, tmp, false, '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ');
      }
      if (opts.chartBox.chartUrl) {
        var chartHtml = '<img src=\"' + opts.chartBox.chartUrl + '\" >';
        chartHtml = chartHtml.replace('&amp;', '&').replace(/<param name=\"play\"[^<]+>/i, '');
        tmp.push('<tr><td align="center">' + chartHtml + '</td></tr>');
        // modChartWidth(chartBox.container.width());
      }
    }
    if (opts.tablebBox) {
      var columns = opts.tablebBox.columns;
      tmp.push('<table class="table"><caption class="title">' + opts.tablebBox.tableTitle + '</caption><thead><tr>');
      var tr_items = '<tr>';
      for (var r = 0; r < columns.length; r++) {
        var column = columns[r];
        var items = column.items;
        if (items) {
          tmp.push('<th colspan="' + items.length + '">' + column.name + '</th>');
          for (var j = 0; j < items.length; j++) {
            tr_items += '<th>' + items[j].name + '</th>';
          }
        } else {
          tmp.push('<th rowspan="2">' + column.name + '</th>');
        }
      }
      tmp.push('</tr>');
      tmp.push(tr_items);
      tmp.push('</tr>');
      var list = opts.tablebBox.tableData;
      for (var l = 0; l < list.length; l++) {
        tmp.push('<tr>');
        for (var r = 0; r < columns.length; r++) {
          var column = columns[r];
          var items = column.items;
          if (items) {
            for (var j = 0; j < items.length; j++) {
              tmp.push('<td>' + list[l][items[j].dataIndex] + '</td>');
            }
          } else {
            tmp.push('<td>' + list[l][column.dataIndex] + '</td>');
          }
        }
        tmp.push('</tr>');
      }
      tmp.push('</tbody></table>');
    }
    return tmp.join('');
  }

  /**
   *
   * @param _printForm
   * @param _tmp
   * @param isWrap    是否在换行
   * @param separator    不换行的分隔符，换行的td数
   */
  function _printFormForTBody (items, _tmp, isWrap, separator) {
    var i = 0;
    if (!isWrap) {
      _tmp.push('<tr><td>');
    }
    for (var q = 0; q < items.length; q++) {
      var item = items[q];
      if (isWrap) { // 换行
        if (i % separator === 0) {
          _tmp.push('<tr>');
        }
        _tmp.push('<td>' + item.label + '： </td>');
        _tmp.push('<td>' + item.value + '</td>');
        if (i % separator === (separator - 1)) {
          _tmp.push('</tr>');
        }
        i++;
      } else {
        _tmp.push(item.label + '： ' + item.value + separator);
      }
    }
    if (!isWrap) {
      _tmp.push('</td></tr>');
    }
  }

  /**
   * 修改报表图形宽度，适应打印页面宽度
   * @param width
   */
  function modChartWidth(width) {
  // var phbchart = opts.phbchart;
  // phbchart.setAttribute('width', width);
  // phbchart.addVariable('chartWidth', width);
  // phbchart.render(opts.chartDiv);
  }

  function _print () {
    console.info(window)
    if (opts.width < opts.minWidth) {
      opts.width = opts.minWidth;
    }
    var win = window.open('', 'win1', 'width=' + opts.width + ',height=' + opts.height + ',location=no,menubar=yes,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no');
    var out = win.document;
    out.writeln(printHtml.join(''));
    out.close();
    win.focus();
  }

  _print();
}

export default {
  ReportPrint: reportPrint
}
