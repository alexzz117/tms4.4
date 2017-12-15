package cn.com.higinet.tms.manager.modules.query.service.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.ConditionUtil;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.query.common.model.Column;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Result;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom.Effect;
import cn.com.higinet.tms.manager.modules.query.service.QueryService;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

@Service("queryService")
@Transactional
public class QueryServiceImpl extends ApplicationObjectSupport implements QueryService {
	
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;

	@Autowired
	@Qualifier("cmcSimpleDao")
	private SimpleDao cmcSimpleDao;

	@Autowired
	private SimpleDao officialSimpleDao;// 自定义查询配置信息使用正式库数据源

	@Autowired
	private QueryDataProcess jsonDataProcess;

	@Autowired
	private SequenceService sequenceService;

	private static final String QUERY_URL_PREFIX = "/tms/query/show";

	/**
	 * 查询自定义查询列表数据
	 */
	public List<Map<String, Object>> listQuery(Map<String, String> conds) {
		String sql = "select * from " + DBConstant.TMS_COM_QUERY;
		conds.put(DBConstant.TMS_COM_QUERY_QUERY_ID, conds.get("queryId"));
		conds.put(DBConstant.TMS_COM_QUERY_QUERY_DESC, ConditionUtil.like(conds.get("queryDesc")));
		conds.put(DBConstant.TMS_COM_QUERY_CTIME, ConditionUtil.like(conds.get("createTime")));
		String where = ConditionUtil.and(conds, new String[][] { { "=", DBConstant.TMS_COM_QUERY_QUERY_ID, DBConstant.TMS_COM_QUERY_QUERY_ID }, { "like", DBConstant.TMS_COM_QUERY_QUERY_DESC, DBConstant.TMS_COM_QUERY_QUERY_DESC },
				{ "like", DBConstant.TMS_COM_QUERY_CTIME, DBConstant.TMS_COM_QUERY_CTIME } });
		sql += ConditionUtil.where(where);
		// sql += " order by " + DBConstant.TMS_COM_QUERY_CTIME + ", tcq." + DBConstant.TMS_COM_QUERY_QUERY_ID;
		sql += " order by QUERY_ID";
		return officialSimpleDao.queryForList(sql, conds);
	}

	public List<Map<String, Object>> listQueryGroup(Map<String, String> conds) {
		String sql = "select * from TMS_COM_QUERY_GROUP";
		conds.put("GROUP_ID", conds.get("groupId"));
		conds.put("GROUP_NAME", ConditionUtil.like(conds.get("groupName")));
		conds.put("PARENT_ID", conds.get("parentId"));
		String where = ConditionUtil.and(conds, new String[][] { { "=", "GROUP_ID", "GROUP_ID" }, { "like", "GROUP_NAME", "GROUP_NAME" }, { "=", "PARENT_ID", "PARENT_ID" } });
		sql += ConditionUtil.where(where);
		sql += " order by ORDERBY";
		return officialSimpleDao.queryForList(sql, conds);
	}

	/**
	 * 根据查询ID，获取自定义查询数据信息
	 */
	public Map<String, Object> getQueryById(long id) {
		String sql = "select * from " + DBConstant.TMS_COM_QUERY + " tcq where tcq." + DBConstant.TMS_COM_QUERY_QUERY_ID + " = ?";
		List<Map<String, Object>> list = officialSimpleDao.queryForList(sql, id);
		Map<String, Object> map = null;
		if (list != null && list.size() == 1) {
			map = list.get(0);
		}
		return map;
	}

	public Map<String, Object> createQuery(Map<String, Object> query) {
		long queryId = sequenceService.getSequenceId(DBConstant.SEQ_TMS_COM_QUERY_ID);
		query.put(DBConstant.TMS_COM_QUERY_QUERY_ID, queryId);
		query.put(DBConstant.TMS_COM_QUERY_CTIME, CalendarUtil.parseStringToDate(CalendarUtil.getCurrentDateTime(), CalendarUtil.FORMAT14));
		officialSimpleDao.create(DBConstant.TMS_COM_QUERY, query);
		return query;
	}

	public void updateQuery(Map<String, Object> query) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put(DBConstant.TMS_COM_QUERY_QUERY_ID, MapUtil.getLong(query, DBConstant.TMS_COM_QUERY_QUERY_ID));
		officialSimpleDao.update(DBConstant.TMS_COM_QUERY, query, conds);
	}

	public void deleteQuery(long queryId, HttpServletRequest request) {
		StringBuffer error = new StringBuffer();
		boolean isUsed = isUsedByQuery(queryId, error, request);
		if (isUsed) {
			throw new TmsMgrServiceException("此查询与系统菜单存在引用关系：" + error.toString());
		} else {
			String sql = "delete from " + DBConstant.TMS_COM_QUERY + " where " + DBConstant.TMS_COM_QUERY_QUERY_ID + " = ?";
			officialSimpleDao.executeUpdate(sql, queryId);
		}
	}

	public Map<String, Object> createQueryGroup(Map<String, Object> group) {
		String groupId = sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_QUERY_ID);
		group.put(DBConstant.TMS_COM_QUERY_GROUP_GROUP_ID, groupId);
		officialSimpleDao.create(DBConstant.TMS_COM_QUERY_GROUP, group);
		return group;
	}

	public void updateQueryGroup(Map<String, Object> group) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put(DBConstant.TMS_COM_QUERY_GROUP_GROUP_ID, group.get(DBConstant.TMS_COM_QUERY_GROUP_GROUP_ID));
		officialSimpleDao.update(DBConstant.TMS_COM_QUERY_GROUP, group, conds);
	}

	public void deleteQueryGroup(long groupId, HttpServletRequest request) {
		String sql = "select * from " + DBConstant.TMS_COM_QUERY + " q where q." + DBConstant.TMS_COM_QUERY_GROUP_ID + " = ?";
		List<Map<String, Object>> queryList = tmsSimpleDao.queryForList(sql, groupId);
		StringBuffer error = null;
		if (queryList != null && queryList.size() > 0) {
			for (Map<String, Object> query : queryList) {
				long queryId = MapUtil.getLong(query, DBConstant.TMS_COM_QUERY_QUERY_ID);
				error = new StringBuffer();
				if (isUsedByQuery(queryId, error, request)) {
					String queryDesc = MapUtil.getString(query, DBConstant.TMS_COM_QUERY_QUERY_DESC);
					throw new TmsMgrServiceException("分组不可删除，分组下[" + queryDesc + "]和系统菜单存在引用关系：" + error.toString());
				}
			}
		}
		this.deleteQueryAndGroup(groupId);
	}

	@Transactional
	public void deleteQueryAndGroup(long groupId) {
		String qsql = "delete from " + DBConstant.TMS_COM_QUERY + " where " + DBConstant.TMS_COM_QUERY_GROUP_ID + " = ?";
		String gsql = "delete from " + DBConstant.TMS_COM_QUERY_GROUP + " where " + DBConstant.TMS_COM_QUERY_GROUP_GROUP_ID + " = ?";
		officialSimpleDao.executeUpdate(qsql, groupId);
		officialSimpleDao.executeUpdate(gsql, groupId);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isUsedByQuery(long queryId, StringBuffer error, HttpServletRequest request) {
		if (queryId <= 0) {
			throw new TmsMgrServiceException("参数queryId不能为空！");
		}
		String sql = "select * from CMC_FUNC func where func.FUNC_TYPE in ('2') and func.CONF like '%" + QUERY_URL_PREFIX + "%queryId=" + queryId + "%'";
		List<Map<String, Object>> list = cmcSimpleDao.queryForList(sql);
		if (list != null && list.size() > 0) {// 此查询配置到了菜单中
			for (Map<String, Object> map : list) {
				String conf = MapUtil.getString(map, "CONF");
				Map<String, Object> params = this.getUrlParamToMap(conf);
				String thisQueryId = MapUtil.getString(params, "queryId");
				if (thisQueryId.equals(String.valueOf(queryId))) {
					if (error.length() > 0) {
						error.append("->");
					}
					error.append("[菜单：").append(MapUtil.getString(map, "FUNC_NAME")).append("]");
					return true;
				}
			}
		} else {// 此查询未配置菜单中，查询配置到菜单中的查询是否引用此查询
			sql = "select * from CMC_FUNC func where func.FUNC_TYPE in ('2') and func.CONF like '%" + QUERY_URL_PREFIX + "%'";
			List<Map<String, Object>> similarList = cmcSimpleDao.queryForList(sql);
			StringBuffer _error = null;
			Map paramterMap = new HashMap();
			paramterMap.putAll(request.getParameterMap());
			for (Map<String, Object> map : similarList) {
				String conf = MapUtil.getString(map, "CONF");
				Map<String, Object> params = this.getUrlParamToMap(conf);
				String thisQueryId = MapUtil.getString(params, "queryId");
				_error = new StringBuffer();
				_error.append("菜单：[").append(MapUtil.getString(map, "FUNC_NAME")).append("]").append("中，引用查询：");
				if (isUsedInQuery(queryId, thisQueryId, _error, paramterMap)) {
					error.append(_error.substring(0, _error.length() - 2));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否在查询内被引用
	 * 
	 * @param queryId
	 *            查询ID
	 * @param queryMap
	 *            判断是否引用queryId的查询信息
	 * @param error
	 *            错误信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isUsedInQuery(long queryId, String refQueryId, StringBuffer error, Map<String, String[]> paramterMap) {
		if (!CmcStringUtil.isBlank(refQueryId)) {
			error.append("[").append(refQueryId).append("]->");
			if (refQueryId.equals(String.valueOf(queryId))) {
				return true;
			}
			paramterMap.put("queryId", new String[] { refQueryId });
			try {
				JsonDataProcess jsonData = this.getCustomQueryDataProcess(paramterMap);
				if (jsonData == null) {
					return false;
				}
				Custom custom = jsonData.getCustom();
				String thisRefQueryId = null;
				if (custom.getDirect() == Custom.Direct.ENTITY && (custom.getEffect() == Effect.COMBTPAGE || custom.getEffect() == Effect.COMBTTAB)) {
					String content = custom.getStmt().getContent();
					String[] pages = content.split("\\;");
					for (int t = 0; t < pages.length; t++) {
						String page = pages[t];
						thisRefQueryId = null;
						if (custom.getEffect() == Effect.COMBTPAGE) {
							thisRefQueryId = page;
						} else if (custom.getEffect() == Effect.COMBTTAB) {
							thisRefQueryId = page.substring(0, page.indexOf("["));
						}
						if (thisRefQueryId.equals(String.valueOf(queryId))) {
							return true;
						}
					}
				}
				Map<String, Object> fieldsMap = jsonData.getFieldsMap();
				for (Map.Entry<String, Object> fieldEntry : fieldsMap.entrySet()) {
					String key = fieldEntry.getKey();
					Map<String, Object> fieldMap = (Map<String, Object>) fieldEntry.getValue();
					String link = MapUtil.getString(fieldMap, "LINK");
					if (!CmcStringUtil.isBlank(link)) {
						String tabAlias = MapUtil.getString(fieldMap, "TAB_ALIAS");
						Map<String, Object> params = this.getUrlParamToMap(link);
						thisRefQueryId = MapUtil.getString(params, "queryId");
						if (thisRefQueryId.equals(String.valueOf(queryId))) {
							error.append("中字段名[").append(tabAlias).append(".").append(key).append("]的[LINK]节点->");
							return true;
						}
					}
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * url地址中的参数字符串转成Map
	 * 
	 * @param url
	 * @return
	 */
	private Map<String, Object> getUrlParamToMap(String url) {
		int p = url.indexOf("?");
		if (p > -1) {
			String param = url.substring(p + 1);
			String[] params = param.split("\\&");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			for (int i = 0; i < params.length; i++) {
				String pam = params[i];
				int _p = pam.indexOf("=");
				if (_p > -1) {
					String _key = pam.substring(0, _p);
					String _value = pam.substring(_p + 1, pam.length());
					Object __value = _value;
					if (paramMap.containsKey(_key)) {
						Object value = paramMap.get(_key);
						if (value instanceof String) {
							__value = new String[] { String.valueOf(value), _value };
						} else if (value instanceof String[]) {
							String[] val = (String[]) value;
							__value = new String[val.length + 1];
							__value = val;
							((String[]) __value)[val.length] = _value;
						}
					}
					paramMap.put(_key, __value);
				}
			}
			return paramMap;
		} else {
			return null;
		}
	}

	/**
	 * 根据表名查询表信息
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */
	public Map<String, Object> getQueryTableByTabName(String tabName) {
		String tabSql = "select * from " + TMS_COM_TAB.TABLE_NAME + " tab where tab." + TMS_COM_TAB.TAB_NAME + " = ?";// 查询表信息
		List<Map<String, Object>> tabList = officialSimpleDao.queryForList(tabSql, tabName);
		Map<String, Object> tabMap = null;
		if (tabList != null && tabList.size() > 0) {
			tabMap = tabList.get(0);
			String type = MapUtil.getString(tabMap, TMS_COM_TAB.TAB_TYPE);
			if (!StaticParameters.TAB_TYPE_1.equals(type)) {
				String basetab = MapUtil.getString(tabMap, TMS_COM_TAB.BASE_TAB);
				if (CmcStringUtil.isBlank(basetab)) {
					String parentSql = "select q.tab_name, q.tab_type, q.view_cond, q.base_tab, q.parent_tab from tms_com_tab q where q.tab_name in (" + TransCommon.arr2str(TransCommon.cutToIds(tabName)) + ")";
					parentSql = "select distinct t." + TMS_COM_TAB.BASE_TAB + " from (" + parentSql + ") t where t." + TMS_COM_TAB.BASE_TAB + " is not null";
					List<Map<String, Object>> list = officialSimpleDao.queryForList(parentSql);
					basetab = MapUtil.getString(list.get(0), TMS_COM_TAB.BASE_TAB);
					tabMap.put(TMS_COM_TAB.BASE_TAB, basetab);
				}
			}
		}
		return tabMap;
	}

	public List<Map<String, Object>> getFieldsByTabName(String tabName, String tabType) {
		String sql = null;
		if (tabType.equals(StaticParameters.TAB_TYPE_1)) {
			sql = "select * from " + DBConstant.TMS_COM_FD + " fd where fd." + DBConstant.TMS_COM_FD_TAB_NAME + " = ? and (fd." + DBConstant.TMS_COM_FD_FD_NAME + " is not null or fd." + DBConstant.TMS_COM_FD_FD_NAME + " <> '') order by fd." + DBConstant.TMS_COM_FD_ORDERBY;
			return officialSimpleDao.queryForList(sql, tabName);
		} else {
			String parentSql = "select q.tab_name, q.tab_type, q.view_cond, q.base_tab, q.parent_tab from tms_com_tab q where q.tab_name in (" + TransCommon.arr2str(TransCommon.cutToIds(tabName)) + ")";
			parentSql = "select t." + TMS_COM_TAB.TAB_NAME + " from (" + parentSql + ") t";
			sql = "select * from " + DBConstant.TMS_COM_FD + " fd where fd." + DBConstant.TMS_COM_FD_TAB_NAME + " in (" + parentSql + ")" + " and (fd." + DBConstant.TMS_COM_FD_FD_NAME + " is not null or fd." + DBConstant.TMS_COM_FD_FD_NAME + " <> '')" + " order by fd."
					+ DBConstant.TMS_COM_FD_TAB_NAME + ", fd." + DBConstant.TMS_COM_FD_ORDERBY;// 查询表字段及其父表字段信息
			return officialSimpleDao.queryForList(sql);
		}
	}

	public JsonDataProcess getCustomQueryDataProcess(Map<String, String[]> paramterMap) {
		String[] qIds = null;
		if (null != paramterMap) {
			qIds = paramterMap.get("queryId");
		}

		// String queryId = paramterMap.get("queryId")[0];
		String queryId = ((null != qIds && qIds.length > 0) ? qIds[0] : null);
		if (CmcStringUtil.isBlank(queryId)) {
			return null;
		}
		Map<String, Object> queryMap = this.getQueryById(Long.valueOf(queryId));
		JsonDataProcess process = (JsonDataProcess) jsonDataProcess.dataProcess(queryMap, paramterMap);
		return process;
	}

	public Object getCustomQueryResultDataList(HttpServletRequest request) {
		JsonDataProcess process = this.getCustomQueryDataProcess(request.getParameterMap());
		if (process == null)
			return null;
		Object resultData = getApplicationContext().getBean(process.getBeanName(), QueryDataProcess.class).dataProcess(process, request);
		return resultData;
	}

	@SuppressWarnings("unchecked")
	public void exportList(String exportType, HttpServletRequest request, HttpServletResponse response) {
		OutputStream os = null;
		BufferedOutputStream buff = null;
		try {
			JsonDataProcess process = this.getCustomQueryDataProcess(request.getParameterMap());
			if (process == null)
				return;
			// process.getCustom().getResult().getPagination().setPageable(false);
			// 限制导出的记录数，否则可能会导致内存溢出
			
			String maxPageSize = PropertiesUtil.getPropInstance().get("maxPageSize");
			int pageSize = 10000;
			if (maxPageSize != null && maxPageSize.length() > 0) {
				pageSize = Integer.valueOf(maxPageSize);
				if (pageSize > 10000) {
					throw new TmsMgrServiceException("设置的导出记录数不能超过1万.");
				}
			}
			process.getCustom().getResult().getPagination().setPageable(true);
			process.getCustom().getResult().getPagination().setPagesize(pageSize);

			String expName = process.getCustom().getResult().getExport().getExpName();
			if (CmcStringUtil.isBlank(expName)) {
				expName = process.getCustom().getPageTitle() + "-" + CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT9);
			} else {
				SimpleDateFormat format = new SimpleDateFormat(expName);
				expName = format.format(new Date());
			}
			expName = new String(expName.getBytes("GB2312"), "iso8859_1");
			Page<Map<String, Object>> page = (Page<Map<String, Object>>) getApplicationContext().getBean(process.getBeanName(), QueryDataProcess.class).dataProcess(process, request);
			List<Map<String, Object>> dateList = page.getList();
			if (dateList == null || dateList.isEmpty()) {
				throw new TmsMgrServiceException("无可导出的数据.");
			}
			
			response.reset();
			os = response.getOutputStream();
			if ("xlsx".equalsIgnoreCase(exportType) || "xls".equalsIgnoreCase(exportType)) {
				response.setContentType("application/msexcel");
				if ("xlsx".equalsIgnoreCase(exportType)) {
					XSSFWorkbook workbook = createExcel2k7Workbook(process.getCustom(), dateList);
					response.setHeader("Content-disposition", "attachment; filename=" + expName + ".xlsx");
					workbook.write(os);
				} else if ("xls".equalsIgnoreCase(exportType)) {
					HSSFWorkbook workbook = createExcel2k3Workbook(process.getCustom(), dateList);
					response.setHeader("Content-disposition", "attachment; filename=" + expName + ".xls");
					workbook.write(os);
				}
			} else if ("csv".equalsIgnoreCase(exportType) || "txt".equalsIgnoreCase(exportType)) {
				response.setContentType("application/octet-stream");
				if ("csv".equalsIgnoreCase(exportType)) {
					response.setHeader("Content-disposition", "attachment; filename=" + expName + ".csv");
				} else if ("txt".equalsIgnoreCase(exportType)) {
					response.setHeader("Content-disposition", "attachment; filename=" + expName + ".txt");
				}
				buff = new BufferedOutputStream(os);
				StringBuffer write = new StringBuffer();
				String enter = "\r\n";
				Set<Column> cols = process.getCustom().getResult().getColumns();
				String[] fdKeys = new String[cols.size()];
				int t = 0;
				for (Column col : cols) {// 产生标题行,各个列用逗号分隔
					if (t > 0) {
						write.append(",");
					}
					write.append(col.getName());
					String fdKey = col.getCsName();
					int index = fdKey.indexOf(".");
					if (index != -1) {
						fdKey = fdKey.substring(index + 1, fdKey.length());
					}
					fdKeys[t] = fdKey;
					t++;
				}
				write.append(enter);
				for (int i = 0, len = dateList.size(); i < len; i++) {
					Map<String, Object> dataMap = dateList.get(i);
					for (int c = 0, clen = fdKeys.length; c < clen; c++) {
						if (c > 0) {
							if ("csv".equalsIgnoreCase(exportType)) {
								write.append("\t");
							}
							write.append(",");
						}
						String dataValue = MapUtil.getString(dataMap, fdKeys[c]);
						write.append(CmcStringUtil.isBlank(dataValue) ? " " : dataValue);
					}
					write.append(enter);
				}
				buff.write(write.toString().getBytes("GB2312"));
				buff.flush();
			}
		} catch (TmsMgrServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new TmsMgrServiceException("数据导出失败.", e);
		} finally {
			try {
				if (buff != null)
					buff.close();
				if (os != null)
					os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param custom
	 * @param dateList
	 * @return
	 */
	private XSSFWorkbook createExcel2k7Workbook(Custom custom, List<Map<String, Object>> dateList) {
		XSSFWorkbook workbook = new XSSFWorkbook();// 创建工作簿
		XSSFSheet sheet = workbook.createSheet();// 创建工作表
		// 创建字体格式
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		// 设置样式
		XSSFCellStyle cs = workbook.createCellStyle();
		cs.setFont(font);
		XSSFRow titleRow = sheet.createRow(0);
		Result result = custom.getResult();
		Set<Column> cols = result.getColumns();
		String[] fdKeys = new String[cols.size()];// 获取数据值的key
		int t = 0;
		for (Column col : cols) {// 产生标题行
			sheet.setColumnWidth(t, col.getWidth() * 100);
			String fdKey = col.getCsName();
			int index = fdKey.indexOf(".");
			if (index != -1) {
				fdKey = fdKey.substring(index + 1, fdKey.length());
			}
			fdKeys[t] = fdKey;
			XSSFCell cell = titleRow.createCell(t);
			cell.setCellValue(col.getName());
			cell.setCellStyle(cs);
			t++;
		}
		for (int i = 0, len = dateList.size(); i < len; i++) {// 产生数据行
			Map<String, Object> dataMap = dateList.get(i);
			XSSFRow dataRow = sheet.createRow(i + 1);
			for (int c = 0, clen = fdKeys.length; c < clen; c++) {
				XSSFCell cell = dataRow.createCell(c);
				cell.setCellValue(MapUtil.getString(dataMap, fdKeys[c]));
			}
		}
		return workbook;
	}

	/**
	 * @param custom
	 * @param dateList
	 * @return
	 */
	private HSSFWorkbook createExcel2k3Workbook(Custom custom, List<Map<String, Object>> dateList) {
		HSSFWorkbook workbook = new HSSFWorkbook();// 创建工作簿
		HSSFSheet sheet = workbook.createSheet();// 创建工作表
		// 创建字体格式
		HSSFFont font = workbook.createFont();
		font.setBoldweight((short) 2000);
		// 设置样式
		HSSFCellStyle cs = workbook.createCellStyle();
		cs.setFont(font);
		HSSFRow titleRow = sheet.createRow(0);
		Result result = custom.getResult();
		Set<Column> cols = result.getColumns();
		String[] fdKeys = new String[cols.size()];// 获取数据值的key
		int t = 0;
		for (Column col : cols) {// 产生标题行
			sheet.setColumnWidth(t, col.getWidth() * 100);
			String fdKey = col.getCsName();
			int index = fdKey.indexOf(".");
			if (index != -1) {
				fdKey = fdKey.substring(index + 1, fdKey.length());
			}
			fdKeys[t] = fdKey;
			HSSFCell cell = titleRow.createCell(t);
			cell.setCellValue(col.getName());
			cell.setCellStyle(cs);
			t++;
		}
		for (int i = 0, len = dateList.size(); i < len; i++) {// 产生数据行
			Map<String, Object> dataMap = dateList.get(i);
			HSSFRow dataRow = sheet.createRow(i + 1);
			for (int c = 0, clen = fdKeys.length; c < clen; c++) {
				HSSFCell cell = dataRow.createCell(c);
				cell.setCellValue(MapUtil.getString(dataMap, fdKeys[c]));
			}
		}
		return workbook;
	}
}