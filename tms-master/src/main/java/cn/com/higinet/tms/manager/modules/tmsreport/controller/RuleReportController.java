package cn.com.higinet.tms.manager.modules.tmsreport.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.tms.manager.common.ManagerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.service.RuleReportService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 规则告警信息控制类
 * @author zhangfg
 * @version 1.0.0
 * @date 2011-12-20
 * 
 * @author zhang.lei
 */

@RestController("ruleReportController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/report/rule")
public class RuleReportController {

	@Autowired
	private RuleReportService ruleReportService;

	/**
	 * 转向规则告警列表页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listReportView() {
		return "report/report_ruleList";
	}

	/**
	 * 根据查询条件查询规则告警信息数据集
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listReportAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> page = ruleReportService.listRuleReport( reqs );
		model.setPage( page );
		return model;
	}

	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/showChart", method = RequestMethod.POST)
	public Model showChartAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.set( "CHARTDATA", ruleReportService.getChartData( null, reqs ) );
		//根据设置信息查询数据集，返回相应字符串
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void exportListAction( @RequestBody Map<String, String> reqs, HttpServletResponse response ) {
		decodeForRequestParamMap( reqs, new String[] {
				"ruleName"
		}, null );
		List<Map<String, Object>> ruleList = ruleReportService.exportList( reqs );

		String titles[] = {
				"规则名称", "监控交易名称", "交易执行次数", "规则命中数", "规则命中率(%)", "交易执行占比(%)", "命中占比(%)"
		};
		String colum[] = {
				"RULENAME", "TXNNAME", "TRIGGERNUM", "HITNUM", "HITRATE", "TRIGGERRATE", "HITNUMRATE"
		};
		try {
			String dateStr = CalendarUtil.getCalendarByFormat( CalendarUtil.FORMAT9 );
			String name = "规则报警信息" + dateStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader( "Content-disposition", "attachment; filename=" + new String( name.getBytes( "GB2312" ), "iso8859_1" ) + ".xls" );// 设定输出文件头   
			response.setContentType( "application/msexcel" );// 定义输出类型 

			WritableWorkbook workbook = Workbook.createWorkbook( os );
			if( workbook != null ) {

				WritableSheet sheet = workbook.createSheet( "规则报警信息", 0 );

				// 设置标题 sheet.addCell(new jxl.write.Label(列(从0开始), 行(从0开始), 内容.)); 
				try {
					WritableFont titleFont = new WritableFont( WritableFont.createFont( "宋体" ), 10, WritableFont.BOLD );
					WritableCellFormat titleFormat = new WritableCellFormat( titleFont );
					for( int i = 0; i < titles.length; i++ ) {
						// 设置标题
						sheet.addCell( new Label( i, 0, titles[i], titleFormat ) );
						// 设置单元格的宽度 
						sheet.setColumnView( i, 20 );
					}
					for( int i = 0; i < ruleList.size(); i++ ) {
						Map<String, Object> txn = ruleList.get( i );
						for( int j = 0; j < colum.length; j++ ) {
							// 每列的值
							sheet.addCell( new Label( j, i + 1, MapUtil.getString( txn, colum[j] ) ) );
						}
					}
					//从内存中写入文件中   
					workbook.write();
					//关闭资源，释放内存    
					workbook.close();
					os.flush();
				}
				catch( RowsExceededException e ) {
					e.printStackTrace();
				}
				catch( WriteException e ) {
					e.printStackTrace();
				}
			}

		}
		catch( IOException e ) {
			e.printStackTrace();
		}
	}

	private void decodeForRequestParamMap( Map<String, String> map, String[] decodeParamNames, String[] decodes ) {
		if( !MapUtil.isEmptyMap( map ) ) {
			try {
				String from = null, to = "utf-8";
				if( decodes == null || (decodes != null && decodes.length == 0) ) {
					from = "iso-8859-1";
				}
				else if( decodes != null && decodes.length == 1 ) {
					from = decodes[0];
				}
				else if( decodes != null && decodes.length == 2 ) {
					from = decodes[0];
					to = decodes[1];
				}
				else {
					return;
				}
				if( decodeParamNames != null && decodeParamNames.length > 0 ) {
					for( String key : decodeParamNames ) {
						String value = MapUtil.getString( map, key );
						if( !CmcStringUtil.isBlank( value ) ) {
							map.put( key, new String( value.getBytes( from ), to ) );
						}
					}
				}
				else {
					for( Map.Entry<String, String> mapEntry : map.entrySet() ) {
						String key = mapEntry.getKey();
						String value = mapEntry.getValue();
						if( !CmcStringUtil.isBlank( value ) ) {
							map.put( key, new String( value.getBytes( from ), to ) );
						}

					}
				}
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
}
