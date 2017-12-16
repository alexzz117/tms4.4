package cn.com.higinet.tms.manager.modules.aml.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.aml.common.AMLConstant;
import cn.com.higinet.tms.manager.modules.aml.service.AntiMoneyLaunderingService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

/**
 * 反洗钱数据管理
 * @author wuc
 * @date 2015-1-27
 * 
 */
@Controller("antiMoneyLaunderingController")
@RequestMapping("/tms/aml")
public class AntiMoneyLaunderingController {

	@Autowired
	private AntiMoneyLaunderingService antiMoneyLaunderingService;

	@RequestMapping(value = "/config", method = RequestMethod.GET)
	public String amlConfigView() {
		return "tms/aml/aml_config";
	}

	@RequestMapping(value = "/getcfg", method = RequestMethod.POST)
	public Model getAMLConfigAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( antiMoneyLaunderingService.queryAmlConfig( reqs ) );
		return model;
	}

	@RequestMapping(value = "/getct", method = RequestMethod.POST)
	public Model getTxnModelFieldsAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( antiMoneyLaunderingService.queryAmlConfig( reqs ) );
		model.setList( antiMoneyLaunderingService.getTxnModelFields( reqs ) );
		return model;
	}

	@RequestMapping(value = "/savecfg", method = RequestMethod.POST)
	public Model saveAMLConfigAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		antiMoneyLaunderingService.saveAmlConfig( reqs );
		return model;
	}

	/**
	 * 跳转反洗钱数据管理页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String amlListView() {
		return "tms/aml/aml_list";
	}

	/**
	 * 反洗钱数据列表
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model amlListAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setPage( antiMoneyLaunderingService.queryAmlList( reqs ) );
		return model;
	}

	/**
	 * 生成报文数据
	 * @return
	 */
	@RequestMapping(value = "/generateMessage", method = RequestMethod.POST)
	public Model generateMessage( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "groupId", antiMoneyLaunderingService.generateMessage( reqs ) );
		return model;
	}

	/**
	 * 跳转导出报文列表页面
	 * @return
	 */
	@RequestMapping(value = "/exportList", method = RequestMethod.GET)
	public String exportListView() {
		return "tms/aml/export_list";
	}

	/**
	 * 导出报文列表
	 * @return
	 */
	@RequestMapping(value = "/exportList", method = RequestMethod.POST)
	public Model exportListAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setPage( antiMoneyLaunderingService.queryExportList( reqs ) );
		return model;
	}

	/**
	 * 跳转报文信息编辑页面
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String messageEditView() {
		return "tms/aml/aml_edit";
	}

	/**
	 * 获取报文编辑页面信息
	 * @return
	 */
	@RequestMapping(value = "/msgInfo", method = RequestMethod.POST)
	public Model queryMessageInfo( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		Map<String, Object> pstr = antiMoneyLaunderingService.queryMessageInfo( reqs );
		model.set( "RBIF", MapUtil.getMap( pstr, AMLConstant.RBIF ) );
		model.set( "CTIF", MapUtil.getList( pstr, AMLConstant.CTIFs ).get( 0 ) );
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		page.setList( MapUtil.getList( pstr, AMLConstant.STIFs ) );
		model.set( "STIFs", page );
		return model;
	}

	/**
	 * 报文信息编辑
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public Model editMessage( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "isSuccess", antiMoneyLaunderingService.updateAmlMessage( reqs ) );
		return model;
	}

	/**
	 * 跳转可疑交易信息编辑页面
	 * @return
	 */
	@RequestMapping(value = "/editStif", method = RequestMethod.GET)
	public String editStifView() {
		return "tms/aml/stif_edit";
	}

	/**
	 * 获取可疑交易编辑页面信息
	 * @return
	 */
	@RequestMapping(value = "/stifInfo", method = RequestMethod.POST)
	public Model queryStifInfo( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		int index = MapUtil.getInteger( reqs, "index" );
		Map<String, Object> pstr = antiMoneyLaunderingService.queryMessageInfo( reqs );
		model.setRow( MapUtil.getList( pstr, AMLConstant.STIFs ).get( index ) );
		return model;
	}

	/**
	 * 可疑交易信息编辑
	 * @return
	 */
	@RequestMapping(value = "/editStif", method = RequestMethod.POST)
	public Model editStif( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "isSuccess", antiMoneyLaunderingService.updateStifMessage( reqs ) );
		return model;
	}

	/**
	 * 报文信息同步
	 * @return
	 */
	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	public Model messageSync( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "isSuccess", antiMoneyLaunderingService.syncCommonMessage( reqs ) );
		return model;
	}

	/**
	 * 报文信息同步
	 * @return
	 */
	@RequestMapping(value = "/updateGroup", method = RequestMethod.POST)
	public Model updateGroup( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.set( "groupId", antiMoneyLaunderingService.updateGroup( reqs ) );
		return model;
	}

	/**
	 * 导出报文信息文件
	 * @return
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void messageExport( @RequestBody Map<String, Object> reqs, HttpServletResponse response ) {
		antiMoneyLaunderingService.exportFile( reqs, response );
	}
}
