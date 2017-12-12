package cn.com.higinet.tmsreport.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tmsreport.web.common.ReportConstant;
import cn.com.higinet.tmsreport.web.service.AreaReportService;
import cn.com.higinet.tmsreport.web.service.DisposalService;

@Controller("areaReportController")
@RequestMapping("/report/area")
public class AreaReportController {
	@Autowired
	private AreaReportService areaReportService;
	@Autowired
	private DisposalService disposalService;
	public void setAreaReportService(AreaReportService areaReportService) {
		this.areaReportService = areaReportService;
	}
	/**
	 * 转向按地区汇总交易告警列表页面
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_areaList";
	}
	/**
	 * 根据查询条件查询交易告警信息数据集
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.POST)
	public Model listReportAction(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		if(StringUtil.isBlank(reqs.get("regioncode")) || "0".equals(reqs.get("regioncode"))){		//地区报警信息
			Page<Map<String, Object>> page = areaReportService.listAreaReport(reqs);
			model.setPage(page);
//			System.out.println(page.getList());
		}else{		//城市告警信息
			Page<Map<String, Object>> page = areaReportService.listCityReport(reqs);
			model.setPage(page);
		}
		return model;
	}
	
	/**
	 * 转向按地区汇总交易告警列表页面
	 * @return
	 */
	@RequestMapping(value="/citylist",method=RequestMethod.GET)
	public String listCityReportView(){
		return "report/report_areaCityList";
	}

	/**
	 * 通过前台图形设置的指标，查询相关数据，并组装成展示全国地图所需的字符串
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/showChinaChart", method=RequestMethod.POST)
	public Model showChinaChartAction(@RequestParam Map<String, String> reqs, HttpServletRequest request){
		Model model = new Model();
		//根据设置信息查询数据集，返回相应字符串
		String dataStr = areaReportService.getChinaChartData(null, reqs);
		model.set("CHARTDATA", dataStr);
		return model;
	}
	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/showChart", method=RequestMethod.POST)
	public Model showChartAction(@RequestParam Map<String, String> reqs, HttpServletRequest request){
		Model model = new Model();
		//根据设置信息查询数据集，返回相应字符串
		String dataStr = areaReportService.getChartData(null, reqs);
		model.set("CHARTDATA", dataStr);
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestParam Map<String, String> reqs,HttpServletRequest request,HttpServletResponse response){
		List<Map<String, Object>>  dateList = null;
		String titles [] = {"地区名称","交易总数"};
		List<String> colum = new ArrayList<String>();
		
		if(StringUtil.isBlank(reqs.get("regioncode")) || "0".equals(reqs.get("regioncode"))){
			dateList = areaReportService.exportList(reqs);
			colum.add("REGIONNAME");
		}else{
			dateList = areaReportService.exportCityList(reqs);
			colum.add("CITYNAME");
		}
		colum.add("TXNNUMBER");
		
		try{
			String dateStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "地区报警信息"+dateStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
			
				WritableSheet sheet = workbook.createSheet("地区报警信息", 0);
				
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
