package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.parser.cond_par;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.query.common.model.Column;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.Group;
import cn.com.higinet.tms.manager.modules.query.common.model.Handle;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Member;

/**
 * 自动处理sql
 * @author liining
 *
 */
@Service("autoSqlProcess")
public class AutoSqlProcessImpl extends AbstractSqlProcessImpl {
	private static Log log = LogFactory.getLog(AutoSqlProcessImpl.class);
	
	protected void procQueryFiledRegion(Custom custom) {
		StringBuilder filedBuilder = new StringBuilder();
		String content = custom.getStmt().getContent();
		Set<Column> columns = custom.getResult().getColumns();
		for(Column column : columns) {
			String csName = column.getCsName();
			Handle handle = column.getHandle();
			if(handle != null && !CmcStringUtil.isBlank(handle.getDb())) {
				filedBuilder.append(handle.getDb().replaceFirst("\\?", column.getFdName())).append(" as ");
			}
			filedBuilder.append(csName).append(", ");
		}
		String filed = filedBuilder.toString();
		filed = filed.length() > 2 ? filed.substring(0, filed.length()-2) : filed;
		content = content.trim();
		String sql = "select " + filed + (content.substring(0, 4).toLowerCase().indexOf("from") != -1 ? " " : " from ") + content;
		custom.getStmt().setContent(sql);
	}
	
	protected String procQueryConditionRegion(JsonDataProcess process, Map<String, Object> conds, HttpServletRequest request) {
		Custom custom = process.getCustom();
		String expr = custom.getCond().getExpr();
		if(!CmcStringUtil.isBlank(expr)) {//表达式不为空
			Map<String, Object> fieldsMap = process.getFieldsMap();
			Map<String, Integer> pmSize = new HashMap<String, Integer>();//参数名个数存储集合
			Set<Group> groups = custom.getCond().getGroups();
			Group[] aGroups = groups.toArray(new Group[groups.size()]);
			String[] groupArrays = new String[groups.size()];
			Set<Integer> groupSet = this.getExprSet(expr);
			try {
				for(Integer e : groupSet){
					int i = (e - 1);
					if(i >= groups.size()){
						throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[expr]中设置["+e+"]，在[custom]->[cond]->[groups]中找不到索引");
					}
					Group group = aGroups[i];
					if(group.getType() == Member.Type.CBT || group.getType() == Member.Type.SCP){//区间或组合,多个成员
						Set<Member> members = group.getMembers();
						Member[] aMembers = members.toArray(new Member[members.size()]);
						String memExpr = group.getExpr();
						String[] memArrays = new String[members.size()];
						Set<Integer> memSet = this.getExprSet(memExpr);
						if(memSet == null || memSet.size() == 0){//成员组合表达式为空
							memExpr = "";
							for(int m = 0; m < aMembers.length; m++){
								memExpr += ("*" + (m+1));
								memArrays[m] = procQueryParam(aMembers[m], pmSize, conds, fieldsMap, request);
							}
							memExpr = memExpr.substring(1, memExpr.length());
						}else{
							for(Integer m : memSet){
								int b = (m - 1);
								if(b >= members.size()){
									throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]节点的第"+(e)+"条数据中，[expr]中设置["+m+"]，在其下的[members]中找不到索引");
								}
								Member member = aMembers[b];
								if(member.getType() == Member.Type.CBT || member.getType() == Member.Type.SCP){
									throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[groups]节点的第"+(e)+"条数据中，[members]节点的第["+m+"]条数据中，[type]不能设置成scp、cbt");
								}
								memArrays[b] = procQueryParam(member, pmSize, conds, fieldsMap, request);
							}
						}
						groupArrays[i] = cond_par.parser(memExpr, memArrays);
					}else{
						groupArrays[i] = procQueryParam(group, pmSize, conds, fieldsMap, request);
					}
				}
				return cond_par.parser(expr, groupArrays);
			}catch(Exception e){
				log.error("Custom Query Auto Process Sql Query Condition." + e);
				throw new TmsMgrWebException(e.getMessage());
			}
		}
		return null;
	}
	
	public static void main(String[] args){
		String expr = "1*3*4+2";
		String[] ars = new String[4];
		ars[0] = "0";
		ars[1] = "";
		ars[2] = "2";
		try {
			System.out.println(cond_par.parser(expr, ars));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}