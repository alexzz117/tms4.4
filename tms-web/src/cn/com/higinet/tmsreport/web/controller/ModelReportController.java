package cn.com.higinet.tmsreport.web.controller;

import java.io.IOException;
import java.io.OutputStream;
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

import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.web.model.Model;
import cn.com.higinet.tms35.manage.common.util.CalendarUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tmsreport.web.service.ModelReportService;

@Controller("dateModelController")
@RequestMapping("/report/model")
public class ModelReportController {
	@Autowired
	private ModelReportService modelReportService ;

	/**
	 * 转向模型评价信息报表页面
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listReportView(){
		return "report/report_modelList";
	}
	
	/**
	 * 通过查询条件，查询相应的模型评价信息
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/list", method=RequestMethod.POST)
	public Model listReportAction(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		Page<Map<String, Object>>  page = modelReportService.listModelReport(reqs);
		model.setPage(page);
		return model;
	}
	/**
	 * 通过前台图形展示的设置信息，查询相关数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/showChart", method=RequestMethod.POST)
	public Model showChartAction(@RequestParam Map<String, String> reqs,HttpServletRequest request){
		Model model = new Model();
		model.set("CHARTDATA", modelReportService.getChartData(reqs));
		return model;
	}

	/**
	 * 导出列表数据
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value="/export", method=RequestMethod.GET)
	public void exportListAction(@RequestParam Map<String, String> reqs,HttpServletRequest request,HttpServletResponse response){
		List<Map<String, Object>>  dateList = modelReportService.exportList(reqs);
		String titles [] = {"模型名称","日期","训练精度最大值","训练精度最小值","训练精度平均值",
				"运行精度最大值","运行精度最小值","运行精度平均值"};
		String colum [] = {"TAB_DESC","STARTDATE","M_FLSCORE_MAX","M_FLSCORE_MIN","M_FLSCORE_AVG","R_FLSCORE_MAX",
				"R_FLSCORE_MIN","R_FLSCORE_AVG"};
		try{
			String dateStr = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			String name = "模型评价信息"+dateStr;
			OutputStream os = response.getOutputStream();// 取得输出流   
			response.reset();// 清空输出流   
			response.setHeader("Content-disposition", "attachment; filename="
			                                    + new String(name.getBytes("GB2312"),
			                                        "iso8859_1") + ".xls");// 设定输出文件头   
			response.setContentType("application/msexcel");// 定义输出类型 
			
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			if (workbook != null) {
			
				WritableSheet sheet = workbook.createSheet("模型评价信息", 0);
				
				// 设置标题 sheet.addCell(new jxl.write.Label(列(从0开始), 行(从0开始), 内容.)); 
				try {
					
					WritableFont titleFont = new WritableFont(WritableFont.createFont("宋体"), 10, WritableFont.BOLD);     
					WritableCellFormat titleFormat = new WritableCellFormat(titleFont);    
				  for(int i=0;i<titles.length;i++){
					  // 设置标题
					  sheet.addCell(new Label(i, 0, titles[i],titleFormat));
					  // 设置单元格的宽度 
					  sheet.setColumnView(i, 20);
				  }
				  for(int i=0;i<dateList.size();i++){
					  Map<String,Object> txn = dateList.get(i);
					  for(int j=0;j<colum.length;j++){
						  // 每列的值
						  sheet.addCell(new Label(j, i+1, MapUtil.getString(txn, colum[j])));
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
