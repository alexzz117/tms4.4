package cn.com.higinet.tms.manager.modules.tmsreport.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.com.higinet.tms.manager.common.ManagerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.tmsreport.common.ReportConstant;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DateReportService;
import cn.com.higinet.tms.manager.modules.tmsreport.service.DisposalService;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author zhang.lei
 */

@RestController("dateReportController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/report/date")
public class DateReportController {
	
	@Autowired
	private DateReportService dateReportService ;

	@Autowired
	private DisposalService disposalService;

	/**
	 * 转向按日期汇总交易信息报表页面
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_dateList";
	}
	
	/**
	 * 通过查询条件和排序条件，查询相应的交易告警信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listReportAction(@RequestBody Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		Page<Map<String, Object>>  page = dateReportService.listDateReport(reqs);
		model.setPage(page);
		return model;
	}
	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 */
	@RequestMapping(value="/showChart", method=RequestMethod.POST)
	public Model showChartAction(@RequestBody Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		model.set("CHARTDATA", dateReportService.getChartData(null,reqs));
		return model;
	}

	/**
	 * 导出列表数据
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestBody Map<String, String> reqs,HttpServletRequest request,HttpServletResponse response){
		List<Map<String, Object>>  dateList = dateReportService.exportList(reqs);
		String titles [] = {"日 期","交易总数"};
		//String colum [] = {"ALERTDATE","TXNNUMBER"};
		List<String> colum = new ArrayList<String>();
		colum.add("ALERTDATE");
		colum.add("TXNNUMBER");
		try{
			String dateStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "日期报警信息"+dateStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
				WritableSheet sheet = workbook.createSheet("日期报警信息", 0);
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
					  for(int i=0;i<dateList.size();i++){
						  Map<String,Object> txn = dateList.get(i);
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
}
