package cn.com.higinet.tms.manager.modules.tmsreport.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.FraudReportService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

@RestController("fraudReportController")
@RequestMapping("/report/fraud")
public class FraudReportController {
	
	@Autowired
	private FraudReportService fraudReportService ;
	@Autowired
	private DisposalService disposalService;

	/**
	 * 转向按日期汇总交易信息报表页面
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_fraudList";
	}
	
	/**
	 * 通过查询条件和排序条件，查询相应的交易告警信息
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listReportAction(@RequestBody Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		Page<Map<String, Object>>  page = fraudReportService.listFraudReport(reqs);
		model.setPage(page);
		
		//model.set("CHARTDATA", fraudReportService.getChartData(null,reqs));
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
		model.set("CHARTDATA", fraudReportService.getChartData(null,reqs));
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestBody Map<String, String> reqs,HttpServletRequest request,HttpServletResponse response){
		List<Map<String, Object>>  fraudList = fraudReportService.exportList(reqs);
		List<String> titles = new ArrayList<String>();
		titles.add("欺诈类型");
		List<String> colum = new ArrayList<String>();
		colum.add("FRAUDNAME");
		try{
			String fraudStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "欺诈报警信息"+fraudStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
			
				WritableSheet sheet = workbook.createSheet("欺诈报警信息", 0);
				
				// 设置标题 sheet.addCell(new jxl.write.Label(列(从0开始), 行(从0开始), 内容.)); 
				try {
					
					WritableFont titleFont = new WritableFont(WritableFont.createFont("宋体"), 10, WritableFont.BOLD);     
					WritableCellFormat titleFormat = new WritableCellFormat(titleFont);    
					List<Map<String, Object>> pslist = disposalService.queryList();
					for (int i = 0; i < pslist.size(); i++) {
						 Map<String, Object> code = pslist.get(i);
						 titles.add(MapUtil.getString(code, "DP_NAME"));
						 colum.add(MapUtil.getString(code, "DP_CODE")+ReportConstant.REPORT_PS_NUM_FD);
					}
				  for(int i=0;i<titles.size();i++){
					  // 设置标题
					  sheet.addCell(new Label(i, 0, titles.get(i),titleFormat));
					  // 设置单元格的宽度 
					  sheet.setColumnView(i, 20);
				  }
				  for(int i=0;i<fraudList.size();i++){
					  Map<String,Object> txn = fraudList.get(i);
					  for(int j=0;j<colum.size();j++){
						  // 每列的值
						  sheet.addCell(new Label(j, i+1, MapUtil.getString(txn, colum.get(j))));
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
