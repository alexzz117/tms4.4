/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.CodeDict;
import cn.com.higinet.tms.manager.common.service.CodeDictService;

/**
 * 字典管理控制类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Controller("cmcCodeDictController")
@RequestMapping("/cmc/codedict")
public class CodeDictController {

	//TODO: 需要设计

	@Autowired
	@Qualifier("cmcCodeDictService")
	private CodeDictService codeDictService;

	@Autowired
	@Qualifier("codeDict")
	CodeDict codeDict;

	/**
	 * 转向类别列表视图
	 * @return
	 */
	@RequestMapping(value = "/category/list", method = RequestMethod.GET)
	public String categoreyListView() {
		return "cmc/codedict/category_list";
	}

	/**
	 * 获取类别列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/category/list", method = RequestMethod.POST)
	public Model categoreyListAction( @RequestParam Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( codeDictService.listCodeCategory( reqs ) );
		return model;
	}

	/**
	 * 转向添加视图
	 * @return
	 */
	@RequestMapping("/category/add")
	public String categoreyAddView() {
		return "cmc/codedict/category_add";
	}

	/**
	 * 添加代码类别操作
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/category/add", method = RequestMethod.POST)
	public Model categoryAddAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			Map<String, Object> temp = codeDictService.getCodeCategory( reqs.get( "category_id" ) );
			if( temp == null || temp.size() == 0 ) {
				Map<String, Object> category = new HashMap<String, Object>();
				category.put( "CATEGORY_ID", reqs.get( "category_id" ).trim() );
				category.put( "CATEGORY_NAME", reqs.get( "category_name" ).trim() );
				category.put( "CATEGORY_SQL", reqs.get( "category_sql" ).trim() );
				category.put( "INFO", reqs.get( "info" ) );
				model.setRow( codeDictService.createCodeCategory( category ) );
			}
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 删除代码类别操作
	 * @param operatorId
	 * @return
	 */
	@RequestMapping(value = "/category/del")
	public Model delCategoryAction( @RequestParam("categoryId") String[] categoryId ) {
		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			if( categoryId != null && categoryId.length > 0 ) {
				codeDictService.deleteCodeCategory( categoryId[0] );
			}
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 转向修改视图
	 * @return
	 */
	@RequestMapping("/category/mod")
	public String categoreyModView() {
		return "cmc/codedict/category_mod";
	}

	/**
	 * 获取编辑对象
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/category/get", method = RequestMethod.POST)
	public Model getCategoreyAction( @RequestBody RequestModel inData ) {
		String categoryId = inData.getString( "categoryId" );
		if(Stringz.isEmpty( categoryId )) return new Model().addError( "categoryId is empty" );
		
		Model model = new Model();
		model.setRow( codeDictService.getCodeCategory( categoryId ) );
		return model;
	}

	/**
	 * 类别更新
	 * @param regs
	 * @return
	 */
	@RequestMapping(value = "/category/update", method = RequestMethod.POST)
	public Model updateCategoreyAction( @RequestBody Map<String, String> regs ) {
		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			Map<String, Object> codeDict = codeDictService.getCodeCategory( regs.get( "category_id" ) );
			codeDict.put( "CATEGORY_ID", regs.get( "category_id" ).trim() );
			codeDict.put( "CATEGORY_NAME", regs.get( "category_name" ).trim() );
			codeDict.put( "CATEGORY_SQL", regs.get( "category_sql" ).trim() );
			codeDict.put( "INFO", regs.get( "info" ) );
			codeDictService.updateCodeCategory( codeDict );
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 转向字典列表视图
	 * @return
	 */
	@RequestMapping(value = "/category/codelist", method = RequestMethod.GET)
	public String codeListView() {
		return "cmc/codedict/code_list";
	}

	/**
	 * 获取字典列表
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/category/codelist", method = RequestMethod.POST)
	public Model codeListAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( codeDictService.listCode( reqs ) );
		return model;
	}

	/**
	 * 转向添加字典信息视图
	 * @return
	 */
	@RequestMapping("/code/add")
	public String codeAddView() {
		return "cmc/codedict/code_add";
	}

	/**
	 * 添加字典信息操作
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/code/add", method = RequestMethod.POST)
	public Model codeAddAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			Map<String, Object> code = new HashMap<String, Object>();
			code.put( "CATEGORY_ID", reqs.get( "category_id" ).trim() );
			code.put( "CODE_KEY", reqs.get( "code_key" ).trim() );
			code.put( "CODE_VALUE", reqs.get( "code_value" ).trim() );
			code.put( "ONUM", reqs.get( "onum" ) );
			code.put( "INFO", reqs.get( "info" ) );
			model.setRow( codeDictService.createCode( code ) );
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 删除字典信息操作
	 * @param operatorId
	 * @return
	 */
	@RequestMapping(value = "/code/del")
	public Model delCodeAction( @RequestBody RequestModel modeMap ) {
		List<String> codeIds = (List<String>) modeMap.getObject( "codeId" );
		if( codeIds == null || codeIds.size() == 0 ) return new Model().addError( "codeId is null" );

		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			codeDictService.deleteCode( codeIds.get( 0 ) );
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 转向字典信息修改视图
	 * @return
	 */
	@RequestMapping("/code/mod")
	public String codeModView() {
		return "cmc/codedict/code_mod";
	}

	/**
	 * 获取字典信息对象
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/code/get", method = RequestMethod.POST)
	public Model getCodeAction( @RequestBody RequestModel modeMap ) {
		String codeId = modeMap.getString( "codeId" );
		Model model = new Model();
		model.setRow( codeDictService.getCode( codeId ) );
		return model;
	}

	/**
	 * 字典信息更新
	 * @param regs
	 * @return
	 */
	@RequestMapping(value = "/code/update", method = RequestMethod.POST)
	public Model updatecodeAction( @RequestBody RequestModel regs ) {
		Model model = new Model();
		String runmode = codeDict.getCode( "common.model", "runmode" );
		if( runmode != null && "development".equals( runmode ) ) {
			Map<String, Object> code = codeDictService.getCode( regs.getString( "code_id" ) );
			code.put( "CATEGORY_ID", regs.getString( "category_id" ) );
			code.put( "CODE_KEY", regs.getString( "code_key" ) );
			code.put( "CODE_VALUE", regs.getString( "code_value" ) );
			code.put( "ONUM", regs.getString( "onum" ) );
			code.put( "INFO", regs.getString( "info" ) );
			codeDictService.updateCode( code );
		}
		else {
			model.addError( "error.cmc.func.runmode.msg" );
		}
		return model;
	}

	/**
	 * 检查代码类别key是否重复
	 */
	@RequestMapping(value = "/check/categoryId", method = RequestMethod.POST)
	public Model checkCateGoryIdAction( @RequestParam String categoryId ) {
		Model model = new Model();
		boolean flag = codeDictService.hasCateGoryId( categoryId );
		if( flag ) {
			model.set( "checke_result", "false" );
		}
		else {
			model.set( "checke_result", "true" );
		}
		return model;

	}
}
