package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.parser.cond_par;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.Group;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Member;

/**
 * 处理手工sql
 * @author liining
 *
 */
@Service("manuSqlProcess")
public class ManuSqlProcessImpl extends AbstractSqlProcessImpl {
	private static Log log = LogFactory.getLog(ManuSqlProcessImpl.class);
	
	protected void procQueryFiledRegion(Custom custom) {}

	protected String procQueryConditionRegion(JsonDataProcess process, Map<String, Object> conds, HttpServletRequest request) {
		Custom custom = process.getCustom();
		Map<String, Object> fieldsMap = process.getFieldsMap();
		String expr = custom.getCond().getExpr();
		Set<Group> groups = custom.getCond().getGroups();
		Set<Integer> groupSet = this.getExprSet(expr);//查询条件可以为空的
		String[] groupArrays = new String[groups.size()];
		Map<String, Integer> pmSize = new HashMap<String, Integer>();//参数名个数存储集合
		int g = 0;
		try {
			for(Group group : groups){
				boolean letEmpty = groupSet == null ? false : groupSet.contains((g+1));//是否允许为空
				if(group.getType() == Member.Type.CBT || group.getType() == Member.Type.SCP){//可能为多个成员的
					Set<Member> members = group.getMembers();
					String memExpr = group.getExpr();
					Set<Integer> memSet = this.getExprSet(memExpr);
					String[] memArrays = new String[members.size()];
					int m = 0;
					for(Member member : members){
						String condSql = procQueryParam(member, pmSize, conds, fieldsMap, request);
						if(letEmpty){//参数可为空
							if(memSet == null || memSet.size() == 0){//未配置表达式
								memExpr = (CmcStringUtil.isBlank(memExpr) ? (m+1+"") : (memExpr + "*" + (m+1)));
							}
							memArrays[m] = condSql;
						}else{//参数不能为空
							if(CmcStringUtil.isBlank(condSql)){
								throw new TmsMgrWebException("查询条件["+group.getLabel()+"]必须全部填写");
							}
						}
						m++;
					}
					if(letEmpty){
						groupArrays[g] = cond_par.parser(memExpr, memArrays);
					}
				}else{//单个成员
					String condSql = procQueryParam(group, pmSize, conds, fieldsMap, request);
					if(letEmpty){//参数可为空
						groupArrays[g] = condSql;
					}else{//参数不可为空
						if(CmcStringUtil.isBlank(condSql)){
							throw new TmsMgrWebException("查询条件["+group.getLabel()+"]必须填写");
						}
					}
				}
				g++;
			}
			String condSql = null;
			if(!CmcStringUtil.isBlank(expr)){
				condSql = cond_par.parser(expr, groupArrays);
			}
			return condSql;
			//String sql = custom.getStmt().getContent() + (StringUtil.isBlank(condSql) ? "" : " and " + condSql);
			//custom.getStmt().setContent(sql);
		}catch(Exception e){
			log.error("Custom Query Manu Process Sql Query Condition." + e);
			throw new TmsMgrWebException(e.getMessage());
		}
	}
}