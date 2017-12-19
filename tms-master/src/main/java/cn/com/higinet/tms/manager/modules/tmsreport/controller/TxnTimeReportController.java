package cn.com.higinet.tms.manager.modules.tmsreport.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.service.TxnTimeReportService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

@RestController("txnTimeReportController")
@RequestMapping("/report/txnTime")
public class TxnTimeReportController {
	
	@Autowired
	private TxnTimeReportService txnTimeReportService ;

	/**
	 * 转向按日期汇总交易信息报表页面
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_txnTimeList";
	}
	
	/**
	 * 通过查询条件和排序条件，查询相应的交易告警信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listReportAction(@RequestBody Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		Page<Map<String, Object>>  page = txnTimeReportService.listTxnTimeReport(reqs);
		model.setPage(page);
		
		//model.set("CHARTDATA", txnTimeReportService.getChartData(null,reqs));
		return model;
	}
	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/showChart", method=RequestMethod.POST)
	public Model showChartAction(@RequestBody Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		model.set("CHARTDATA", txnTimeReportService.getChartData(null,reqs));
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestBody Map<String, String> reqs,HttpServletRequest request,HttpServletResponse response){
		List<Map<String, Object>>  txnTimeList = txnTimeReportService.exportList(reqs);
		String titles [] = {"交易名称"};
		String colum [] = {"TXNNAME","RISK_EVAL_NUMBER","RISK_EVAL_RUNTIME_AVG","RISK_EVAL_RUNTIME_MAX","RISK_EVAL_RUNTIME_MIN",
						"RISK_EVAL_TPM_AVG","RISK_EVAL_TPM_MAX","RISK_EVAL_TPM_MIN",
						"RISK_CFM_NUMBER","RISK_CFM_RUNTIME_AVG","RISK_CFM_RUNTIME_MAX","RISK_CFM_RUNTIME_MIN",
						"RISK_CFM_TPM_AVG","RISK_CFM_TPM_MAX","RISK_CFM_TPM_MIN"};
		try{
			String txnTimeStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "交易运行效率"+txnTimeStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
			
				WritableSheet sheet = workbook.createSheet("交易运行效率", 0);
				
				// 设置标题 sheet.addCell(new jxl.write.Label(列(从0开始), 行(从0开始), 内容.)); 
				try {
					
					WritableFont titleFont = new WritableFont(WritableFont.createFont("宋体"), 10, WritableFont.BOLD);     
					WritableCellFormat titleFormat = new WritableCellFormat(titleFont); 
				  for(int i=0;i<titles.length;i++){
					  // 设置标题
					  sheet.mergeCells(i, 0, i, 1); 
					  sheet.addCell(new Label(i, 0, titles[i],titleFormat));
					  // 设置单元格的宽度 
					  sheet.setColumnView(i, 20);
				  }
				  sheet.mergeCells(1, 0, 1+6, 0); 
				  sheet.addCell(new Label(1, 0, "风险评估",titleFormat));
				  //总数,欺诈数、非欺诈数					  
				  sheet.addCell(new Label(1, 1, "调用次数",titleFormat));
				  sheet.addCell(new Label(2, 1, "平均时间",titleFormat));
				  sheet.addCell(new Label(3, 1, "最大时间",titleFormat));
				  sheet.addCell(new Label(4, 1, "最小时间",titleFormat));
				  sheet.addCell(new Label(5, 1, "平均TPM",titleFormat));
				  sheet.addCell(new Label(6, 1, "最大TPM",titleFormat));
				  sheet.addCell(new Label(7, 1, "最小TPM",titleFormat));
				  sheet.mergeCells(8, 0, 8+6, 0); 
				  sheet.addCell(new Label(8, 0, "交易确认",titleFormat));
				  //总数,欺诈数、非欺诈数					  
				  sheet.addCell(new Label(8, 1, "调用次数",titleFormat));
				  sheet.addCell(new Label(9, 1, "平均时间",titleFormat));
				  sheet.addCell(new Label(10, 1, "最大时间",titleFormat));
				  sheet.addCell(new Label(11, 1, "最小时间",titleFormat));
				  sheet.addCell(new Label(12, 1, "平均TPM",titleFormat));
				  sheet.addCell(new Label(13, 1, "最大TPM",titleFormat));
				  sheet.addCell(new Label(14, 1, "最小TPM",titleFormat));
				  for(int i=0;i<txnTimeList.size();i++){
					  Map<String,Object> txn = txnTimeList.get(i);
					  for(int j=0;j<colum.length;j++){
						  // 每列的值
						  sheet.addCell(new Label(j, i+2, MapUtil.getString(txn, colum[j])));
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
}
