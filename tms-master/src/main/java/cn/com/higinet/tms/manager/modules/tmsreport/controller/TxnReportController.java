package cn.com.higinet.tms.manager.modules.tmsreport.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.TxnReportService;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


/**
 * 交易告警信息报表控制类
 * @author zhangfg
 * @version 1.0.0,
 * @date 2011-12-20
 * 
 * @author zhang.lei
 */

@RestController("txnReportController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/report/txn")
public class TxnReportController {
	
	@Autowired
	private TxnReportService txnReportService ;
	@Autowired
	private DisposalService disposalService;

	/**
	 * 转向交易告警信息汇总报表页面
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_txnList";
	}
	
	/**
	 * 通过查询条件和排序条件，查询相应的交易告警信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listReportAction(@RequestBody Map<String, String> reqs){
//		Page<Map<String, Object>>  page = txnReportService.listTxnReport(reqs);
		Model model = new Model();
		model.setPage(txnReportService.listTxnReport(reqs));
//		model.setList();
		return model;
	}
	/**
	 * 获取处置方式
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/getPS", method=RequestMethod.POST)
	public Model getPSAction(){
		Model model = new Model();
		model.setList(disposalService.queryList());
		return model;
	}
	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/showChart", method=RequestMethod.POST)
	public Model showChartAction(@RequestBody Map<String, String> reqs){
		Model model = new Model();
		String chartStr = txnReportService.getChartData(null,reqs);
		model.set("CHARTDATA", chartStr);
		//根据设置信息查询数据集，返回相应字符串
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestBody Map<String, String> reqs, HttpServletResponse response){
		List<Map<String, Object>>  txnList = txnReportService.exportList(reqs);
		String titles [] = {"交易名称","交易总数"};
		List<String> colum = new ArrayList<String>();
		colum.add("TXNNAME");
		colum.add("TXNNUMBER");
		try{
			String dateStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "交易报警信息"+dateStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
			
				WritableSheet sheet = workbook.createSheet("交易报警信息", 0);
				// 设置标题 sheet.addCell(new jxl.write.Label(列(从0开始), 行(从0开始), 内容.)); 
				try {
					
					WritableFont titleFont = new WritableFont(WritableFont.createFont("宋体"), 10, WritableFont.BOLD);     
					WritableCellFormat titleFormat = new WritableCellFormat(titleFont); 
					titleFormat.setAlignment(Alignment.CENTRE);// 设置居中 
				  for(int i=0;i<titles.length;i++){
					  // 设置标题
					  sheet.mergeCells(i, 0, i, 1); 
					  sheet.addCell(new Label(i, 0, titles[i],titleFormat));
					  // 设置单元格的宽度 
					  sheet.setColumnView(i, 20);
				  }
				  List<Map<String, Object>> pslist = disposalService.queryList();
				  for (int i = titles.length,j = 0; i < (pslist.size()*3)+titles.length; i+=3,j++) {
					  // 设置标题
					  Map<String, Object> code = pslist.get(j);
					  sheet.mergeCells(i, 0, i+2, 0); 
					  sheet.addCell(new Label(i, 0, MapUtil.getString(code, "DP_NAME"),titleFormat));
					  //总数,欺诈数、非欺诈数					  
					  sheet.addCell(new Label(i, 1, "总数",titleFormat));
					  sheet.addCell(new Label(i+1, 1, "欺诈数",titleFormat));
					  sheet.addCell(new Label(i+2, 1, "非欺诈数",titleFormat));
					  String key = MapUtil.getString(code, "DP_CODE");
					  colum.add(key+ReportConstant.REPORT_PS_RATE);
					  colum.add(key+ReportConstant.REPORT_PS_RATE_FD);
					  colum.add(key+ReportConstant.REPORT_PS_RATE_NFD);
					  // 设置单元格的宽度 
					  sheet.setColumnView(i, 20);
				  }
				  for(int i=0;i<txnList.size();i++){
					  Map<String,Object> txn = txnList.get(i);
					  for(int j=0;j<colum.size();j++){
						  // 每列的值
						  sheet.addCell(new Label(j, i+2, MapUtil.getString(txn, colum.get(j))));
					  }
				  }
				  //从内存中写入文件中   
				  workbook.write();
				  //关闭资源，释放内存    
				  workbook.close();
				  os.flush();
				}catch (RowsExceededException e) {
					e.printStackTrace();
				} catch (WriteException e) {
					e.printStackTrace();
				}
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 获取所有渠道信息
	 * @return
	 */
	@RequestMapping(value="/getChannel", method=RequestMethod.POST)
	public Model getChannelList(){
		Model model = new Model();
		model.set("channelList", txnReportService.getChannelList(null));
		return model;
	}
	
	/**
	 * 获取所有渠道信息
	 * @return
	 */
	@RequestMapping(value="/getCountry", method=RequestMethod.POST)
	public Model getCountryList(){
		Model model = new Model();
		model.set("countryList", txnReportService.getAllCountry());
		return model;
	}
	/**
	 * 根据国家代码获取所有省份列表
	 * @param countryCode	国家代码
	 * @return
	 */
	@RequestMapping(value="/getRegion", method=RequestMethod.POST)
	public Model getAllRegion(@RequestBody String countryCode){
		Model model = new Model();
		model.set("regList", txnReportService.getAllRegion(countryCode));
		//根据国家代码获取所有省份列表
		return model;
	}
	/**
	 * 根据省份代码获取所有地市列表
	 * @param regionCode	省份代码
	 * @return
	 */
	@RequestMapping(value="/getCity", method=RequestMethod.POST)
	public Model getAllCity(@RequestBody String regionCode ){
		Model model = new Model();
		if(regionCode.length() == 1){
			regionCode = "0" + regionCode;
		}
		model.set("cityList", txnReportService.getAllCity(regionCode));
		//根据省份代码获取所有地市列表
		return model;
	}
	
}
