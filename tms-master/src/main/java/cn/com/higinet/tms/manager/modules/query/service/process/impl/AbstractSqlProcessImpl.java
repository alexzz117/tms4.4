package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cn.com.higinet.tms.manager.common.ApplicationContextUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.query.common.model.Column;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Member;
import cn.com.higinet.tms.manager.modules.query.common.model.Pagination;
import cn.com.higinet.tms.manager.modules.query.common.model.Table;
import cn.com.higinet.tms.manager.modules.query.service.QueryService;

public abstract class AbstractSqlProcessImpl extends QueryDataProcessCommon {

	@Autowired
	@Qualifier("dynamicSimpleDao")
	protected SimpleDao dynamicSimpleDao;

	@Autowired
	protected QueryService queryService;
	
	/**
	 * sql语句中参数集合
	 */
	protected Map<String, Object> sqlConds = null;
	/**
	 * 记录同一参数名的个数
	 * Map<String(参数名), Integer(个数)>
	 */
	protected Map<String, Integer> paramSizeMap = null;
	/**
	 * 组织sql时，是否是使用参数型
	 */
	protected static final boolean useParamType = true;
	
	static TreeMap<String, Integer> map_sql_type = new TreeMap<String, Integer>();
	static
	{
		map_sql_type.put("STRING", Types.VARCHAR);
		map_sql_type.put("INT", Types.INTEGER);
		map_sql_type.put("LONG", Types.BIGINT);
		map_sql_type.put("FLOAT", Types.DOUBLE);
		map_sql_type.put("DOUBLE", Types.DOUBLE);
		map_sql_type.put("MONEY", Types.DOUBLE);
		map_sql_type.put("TIME", Types.BIGINT);
		map_sql_type.put("DATETIME", Types.BIGINT);
		map_sql_type.put("IP", Types.VARCHAR);
		map_sql_type.put("DEVID", Types.VARCHAR);
		map_sql_type.put("USERID", Types.VARCHAR);
		map_sql_type.put("ACC", Types.VARCHAR);
		map_sql_type.put("CODE", Types.VARCHAR);
	}

	public Object dataProcess(Object... args) {
		JsonDataProcess process = (JsonDataProcess) args[0];
		HttpServletRequest request = (HttpServletRequest) args[1];
		
		Map<String, Object> conds = new HashMap<String, Object>();//sql语句中参数集合
		
		String sql = this.statementProcess(process, conds, request);
		if (!conds.containsKey("currentTimeMillis")) {
			conds.put("currentTimeMillis", System.currentTimeMillis());
		}
		Pagination pagination = process.getCustom().getResult().getPagination();

		int pageindex = CmcStringUtil.isBlank(request.getParameter("pageindex")) 
				? 1 : Integer.parseInt(request.getParameter("pageindex"));
		int pagesize = CmcStringUtil.isBlank(request.getParameter("pagesize")) 
				? 10 : Integer.parseInt(request.getParameter("pagesize"));		
		if (pagination.getPageable()) {// 是否分页
			pagesize = pagination.getPagesize() == null ? pagesize : pagination.getPagesize();
		} else {
			pagesize = Integer.MAX_VALUE;
		}
		SimpleDao simpleDao = dynamicSimpleDao;
		String dsName = process.getCustom().getStmt().getDsName();
		if (!CmcStringUtil.isBlank(dsName)) {
			dsName = String.format("%sSimpleDao", dsName);
			if (ApplicationContextUtil.containsBean(dsName)) {
				simpleDao = ApplicationContextUtil.getBean(dsName, SimpleDao.class);
			}
		}
		Page<Map<String, Object>> result = simpleDao.pageQuery(sql, conds, pageindex, pagesize, new Order());
		super.dataProcess(process, result.getList());
		return result;
	}

	/**
	 * 语句综合处理
	 * 
	 * @param custom
	 * @param request
	 */
	private String statementProcess(JsonDataProcess process, Map<String, Object> conds, HttpServletRequest request) {
		this.procQueryFiledRegion(process.getCustom());
		this.procQueryTableRegion(process.getCustom());
		String sql = process.getCustom().getStmt().getContent();
		String condSql = this.procQueryConditionRegion(process, conds, request);
		String orderSql = this.procQueryOrderRegion(process.getCustom().getResult().getColumns());
		if (!CmcStringUtil.isBlank(condSql)) {
			sql += (" and " + condSql);
		}
		if (!CmcStringUtil.isBlank(orderSql)) {
			sql += (" order by " + orderSql);
		}
		return sql;
	}

	/**
	 * 处理查询字段部分
	 * 
	 * @param custom
	 */
	protected abstract void procQueryFiledRegion(Custom custom);

	/**
	 * 处理查询表部分
	 * 
	 * @param custom
	 */
	protected void procQueryTableRegion(Custom custom){
		String sql = custom.getStmt().getContent();
		for(Table table : custom.getDbData().getTables()) {
			Map<String, Object> tabMap = queryService.getQueryTableByTabName(table.getName());
			if(tabMap != null && !tabMap.isEmpty()){
				String tabType = MapUtil.getString(tabMap, TMS_COM_TAB.TAB_TYPE);
				if(!tabType.equals(StaticParameters.TAB_TYPE_1)) {
					String tabName = MapUtil.getString(tabMap, TMS_COM_TAB.TAB_NAME);
					String baseTab = MapUtil.getString(tabMap, TMS_COM_TAB.BASE_TAB);
					String viewCond = MapUtil.getString(tabMap, TMS_COM_TAB.VIEW_COND);
					String regex = "\\s+("+tabName+")\\s+";
					String vRegex = "\\s*select\\s+[\\s\\S]*\\s+from\\s+[\\s\\S]*(\\s+where\\s+[\\s\\S]*)?";
					Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);//不区分大小写
					Matcher m = pattern.matcher(sql);
					Pattern vPattern = Pattern.compile(vRegex, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);//不区分大小写
					Matcher vm = vPattern.matcher(viewCond);
					StringBuffer sb = new StringBuffer();
					while(m.find()) {
						String cond = vm.find() ? "(" + viewCond + ")" : this.getViewCond(tabName, tabType, viewCond, baseTab);
						m.appendReplacement(sb, (" " + cond + " "));
					}
					m.appendTail(sb);
					sql = sb.toString();
				}
			}
		}
		custom.getStmt().setContent(sql);
	}

	/**
	 * 处理查询条件
	 * 
	 * @param custom
	 * @param conds
	 * @param request
	 */
	protected abstract String procQueryConditionRegion(JsonDataProcess process, Map<String, Object> conds, HttpServletRequest request);
	
	protected String procQueryOrderRegion(Set<Column> columns) {
		StringBuffer sb = new StringBuffer();
		for(Column column : columns) {
			String csName = column.getCsName();
			if(column.getOrder() != null) {
				sb.append(csName).append(" ").append(column.getOrder().getValue()).append(", ");
			}
		}
		if (sb.length() > 0) sb.setLength(sb.length() - 2);
		return sb.toString();
	}
	
	/**
	 * 获取表达式中所定义的查询条件
	 * @param expr	表达式
	 * @return
	 */
	protected Set<Integer> getExprSet(String expr) {
		if(CmcStringUtil.isBlank(expr)) return null;
		Set<Integer> set = new LinkedHashSet<Integer>();
		String regex = "[\\d]+";
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(expr);
		while (m.find()) {
			set.add(Integer.parseInt(m.group()));
		}
		return set;
	}
	
	protected String getViewCond(String tabName, String tabType, String viewCond, String baseTab) {
		String sql = null;
		if (CmcStringUtil.isBlank(viewCond)) {
			sql = baseTab;
		} else {
			StringBuilder fdBuilder = new StringBuilder();
			List<Map<String, Object>> fdList = queryService.getFieldsByTabName(tabName, tabType);
			for(Map<String, Object> fdMap : fdList) {
				String fdName = MapUtil.getString(fdMap, DBConstant.TMS_COM_FD_FD_NAME);
				fdBuilder.append(fdName).append(", ");
			}
			String fdstr = fdBuilder.toString();
			fdstr = fdstr.substring(0, fdstr.length()-2);
			sql = "(select " + fdstr + " from " + baseTab + " where " + viewCond + ")";
		}
		return sql;
	}
	
	/**
	 * 处理一个查询条件成员所生成的sql语句
	 * @param member	条件成员
	 * @param pmSize	参数名个数
	 * @param conds		参数值集合
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String procQueryParam(Member member, Map<String, Integer> pmSize, Map<String, Object> conds, Map<String, Object> fieldsMap, HttpServletRequest request){
		String paramName = member.getName();
		Integer size = 0;
		if(pmSize.containsKey(paramName)){
			size = pmSize.get(paramName);
		}
		String[] paramValues = request.getParameterValues(paramName);
		if(paramValues != null && paramValues.length > size){
			
			String paramValue = paramValues[size];//参数值
			if(CmcStringUtil.isBlank(paramValue)) return null;
			
			int len = paramValues.length;
			if(member.getType() == Member.Type.IN || member.getType() == Member.Type.NOTIN){//in、not in 语句
				paramValue = procQueryOperatorForIn(member, (useParamType ? (paramName + (len == 1 ? "" : ("_" + size))) : paramName), paramValue, conds);
			}else if(member.getType() == Member.Type.LIKE || member.getType() == Member.Type.NOTLIKE){
				
			}else{
				if(useParamType){
					Map<String, Object> fdMap = (Map<String, Object>) fieldsMap.get(member.getFdName());
					String scParamName = paramName + (len == 1 ? "" : ("_" + size));
					conds.put(scParamName, typeConve(String.valueOf(fdMap.get("TYPE")), transformValueByHandleForMethod(paramValue, member.getHandle())));
					paramValue = transformValueByHandleForDb(":" + scParamName, member.getHandle());
				}
			}
			String condSql = member.getCsName() + member.getType().getOperator().replaceFirst("\\?", paramValue);
			pmSize.put(paramName, ++size);
			return condSql;
		}
		return null;
	}
	
	/**
	 * 处理查询操作符为in的
	 * 包括(in、not in)
	 * @param member
	 * @param paramName
	 * @param paramValue
	 * @param conds
	 */
	protected String procQueryOperatorForIn(Member member, String paramName, String paramValue, Map<String, Object> conds) {
		String spellStr = "";
		String[] values = paramValue.split("\\,");
		for(int i=0;i<values.length;i++){
			if(useParamType){
				String scParamName = paramName + "_" + i; 
				spellStr += (","+transformValueByHandleForDb(":" + scParamName, member.getHandle()));
				conds.put(scParamName, transformValueByHandleForMethod(values[i], member.getHandle()));
			}else{
				spellStr += (",'" + transformValueByHandleForMethod(values[i], member.getHandle())+"'");
			}
		}
		spellStr = spellStr.substring(1, spellStr.length());
		return spellStr;
	}
	
	protected Object typeConve(String type, Object value) {
		int sql_type = map_sql_type.get(type.toUpperCase());
		Object conve_value = null;
		String str_value = String.valueOf(value);
		if (sql_type == Types.VARCHAR) {
			conve_value = str_value;
		} else if (sql_type == Types.INTEGER) {
			conve_value = Integer.parseInt(str_value);
		} else if (sql_type == Types.BIGINT) {
			conve_value = Long.parseLong(str_value);
		} else if (sql_type == Types.DOUBLE) {
			conve_value = Double.parseDouble(str_value);
		} else {
			return value;
		}
		return conve_value;
	}
	
	public static void main(String[] args){
		String expr = "1*2+3*15!20(30+230)";
		
		AutoSqlProcessImpl sql = new AutoSqlProcessImpl();
		Set<Integer> set = sql.getExprSet(expr);
		//Integer[] is = set.toArray(new Integer[set.size()]);
		Object[] is = set.toArray();
		for(Object i : is){
			System.out.println(i);
		}
		
		expr = "> ?";
		String value = "1,3,5";
		System.out.println(expr.replaceAll("\\?", value));
	}
}
