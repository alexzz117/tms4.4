package cn.com.higinet.tms.manager.modules.query.service;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.query.common.function.impl.SystemParamValueFunction;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service("alarmEventQueryService")
@Transactional
public class AlarmEventQueryService {

    @Autowired
    @Qualifier("offlineSimpleDao")
    private SimpleDao offlineSimpleDao;

    @Autowired
    @Qualifier("dynamicSimpleDao")
    private SimpleDao dynamicSimpleDao;

    @Autowired
    @Qualifier("onlineSimpleDao")
    private SimpleDao onlineSimpleDao;

    @Autowired
    @Qualifier("getSystemParamValue")
    private SystemParamValueFunction systemParamValueFunction;

    public Page<Map<String, Object>> queryAlarmEventData(RequestModel requestModel) {
        String modelType = requestModel.getString("modelType");
        String sql = null;
        if("alarmEventSend".equals(modelType)){
            //报警事件分派查询
            sql = "SELECT DISTINCT CONCAT(FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d' ), ' ', '00:00:00') operate_time, FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d %H:%i:%s') end_time, traffic.txncode, traffic.userid, u.username, traffic.txntype, (SELECT CONCAT(channel.channelname, '--', tab.tab_desc) FROM tms_dp_channel channel, tms_com_tab tab WHERE traffic.txntype = tab.tab_name AND tab.chann = channel.channelid) TXNNAME, traffic.txntime, CONCAT( IFNULL((SELECT country.countryname FROM TMS_COUNTRY country WHERE traffic.countrycode = country.countrycode), '未知'), '-', IFNULL((SELECT region.regionname FROM TMS_REGION region WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知'), '-', IFNULL((SELECT city.cityname FROM TMS_CITY city WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode AND traffic.citycode = city.citycode), '未知')) LOCATION, IFNULL(traffic.iscorrect, '2') iscorrect, (select DP_NAME from tms_com_disposal dp where traffic.disposal=dp.DP_CODE)  disposal, traffic.txnstatus, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.assignid = o.OPERATOR_ID) assign_name, traffic.assigntime, traffic.assigntime m_assigntime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_name,(select o.OPERATOR_ID from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_id, traffic.opertime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.auditid = o.OPERATOR_ID) audit_name, traffic.audittime,traffic.audittime m_audittime, traffic.psstatus, traffic.strategy, traffic.deviceid,traffic.sessionid FROM TMS_RUN_TRAFFICDATA traffic LEFT JOIN TMS_RUN_USER u ON traffic.userid = u.userid WHERE 1 = 1 and (traffic.psstatus = '00' or traffic.psstatus = '02' || traffic.psstatus = '04')  and traffic.HITRULENUM !=0 ";
        } else if ("alarmEventExecute".equals(modelType)){
            sql = "SELECT DISTINCT CONCAT(FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d' ), ' ', '00:00:00') operate_time, FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d %H:%i:%s') end_time, traffic.txncode, traffic.userid, u.username, traffic.txntype, (SELECT CONCAT(channel.channelname, '--', tab.tab_desc) FROM tms_dp_channel channel, tms_com_tab tab WHERE traffic.txntype = tab.tab_name AND tab.chann = channel.channelid) TXNNAME, traffic.txntime, CONCAT( IFNULL((SELECT country.countryname FROM TMS_COUNTRY country WHERE traffic.countrycode = country.countrycode), '未知'), '-', IFNULL((SELECT region.regionname FROM TMS_REGION region WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知'), '-', IFNULL((SELECT city.cityname FROM TMS_CITY city WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode AND traffic.citycode = city.citycode), '未知')) LOCATION, IFNULL(traffic.iscorrect, '2') iscorrect, (select DP_NAME from tms_com_disposal dp where traffic.disposal=dp.DP_CODE)  disposal, traffic.txnstatus, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.assignid = o.OPERATOR_ID) assign_name, traffic.assigntime, traffic.assigntime m_assigntime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_name,(select o.OPERATOR_ID from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_id, traffic.opertime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.auditid = o.OPERATOR_ID) audit_name, traffic.audittime,traffic.audittime m_audittime, traffic.psstatus, traffic.strategy, traffic.deviceid,traffic.sessionid FROM TMS_RUN_TRAFFICDATA traffic LEFT JOIN TMS_RUN_USER u ON traffic.userid = u.userid WHERE 1 = 1 and traffic.psstatus in ('02', '04') and traffic.operid=:loginOperatorId";
        } else {
            sql = "SELECT DISTINCT CONCAT(FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d' ), ' ', '00:00:00') operate_time, FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d %H:%i:%s') end_time, traffic.txncode, traffic.userid, u.username, traffic.txntype, (SELECT CONCAT(channel.channelname, '--', tab.tab_desc) FROM tms_dp_channel channel, tms_com_tab tab WHERE traffic.txntype = tab.tab_name AND tab.chann = channel.channelid) TXNNAME, traffic.txntime, CONCAT( IFNULL((SELECT country.countryname FROM TMS_COUNTRY country WHERE traffic.countrycode = country.countrycode), '未知'), '-', IFNULL((SELECT region.regionname FROM TMS_REGION region WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知'), '-', IFNULL((SELECT city.cityname FROM TMS_CITY city WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode AND traffic.citycode = city.citycode), '未知')) LOCATION, IFNULL(traffic.iscorrect, '2') iscorrect, (select DP_NAME from tms_com_disposal dp where traffic.disposal=dp.DP_CODE)  disposal, traffic.txnstatus, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.assignid = o.OPERATOR_ID) assign_name, traffic.assigntime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_name,(select o.OPERATOR_ID from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_id, traffic.opertime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.auditid = o.OPERATOR_ID) audit_name, traffic.audittime, traffic.psstatus, traffic.strategy, traffic.deviceid,traffic.sessionid FROM TMS_RUN_TRAFFICDATA traffic LEFT JOIN TMS_RUN_USER u ON traffic.userid = u.userid WHERE 1 = 1 and traffic.HITRULENUM !=0 ";
        }

        String txncode = requestModel.getString("txncode");
        String userid = requestModel.getString("userid");
        String username = requestModel.getString("username");
        String deviceid = requestModel.getString("deviceid");
        String ipaddr = requestModel.getString("ipaddr");
        String txntype = requestModel.getString("txntype");
        String disposal = requestModel.getString("disposal");
        String iscorrect = requestModel.getString("iscorrect");
        String txnstatus = requestModel.getString("txnstatus");
        String psstatus = requestModel.getString("psstatus");
        String countrycode = requestModel.getString("countrycode");
        String regioncode = requestModel.getString("regioncode");
        String citycode = requestModel.getString("citycode");
        String startDateStr = requestModel.getString("startDate");
        String endDateStr = requestModel.getString("endDate");
        if(StringUtils.isNotEmpty(txncode)) {
            sql += " and traffic.txncode = :txncode";
        }
        if(StringUtils.isNotEmpty(userid)) {
            sql += " and traffic.userid = :userid";
        }
        if(StringUtils.isNotEmpty(username)) {
            sql += " and u.username = :username";
        }
        if(StringUtils.isNotEmpty(deviceid)) {
            sql += " and traffic.deviceid = :deviceid";
        }
        if(StringUtils.isNotEmpty(ipaddr)) {
            sql += " and traffic.ipaddr = :ipaddr";
        }
        if(StringUtils.isNotEmpty(txntype)) {
            String[] txnTypes = txntype.split(",");
            List<String> txnTypeList = new ArrayList<>(Arrays.asList(txnTypes));
            requestModel.put("txnTypeList", txnTypeList);
            sql += " and traffic.txntype in (:txnTypeList)";
        }
        if(StringUtils.isNotEmpty(disposal)) {
            sql += " and traffic.disposal = :disposal";
        }
        if(StringUtils.isNotEmpty(iscorrect)) {
            sql += " and ifnull(traffic.iscorrect,2) = :iscorrect";
        }
        if(StringUtils.isNotEmpty(txnstatus)) {
            sql += " and traffic.txnstatus = :txnstatus";
        }
        if(StringUtils.isNotEmpty(psstatus)) {
            sql += " and traffic.psstatus = :psstatus";
        }
        if(StringUtils.isNotEmpty(startDateStr) && StringUtils.isNotEmpty(endDateStr)) {
            long startDate = Long.parseLong(startDateStr);
            long endDate = Long.parseLong(endDateStr);

            requestModel.put("startTimeStamp", startDate);
            requestModel.put("endTimeStamp", endDate);
            sql += " and traffic.txntime between :startTimeStamp and :endTimeStamp";
        }
        if(StringUtils.isNotEmpty(countrycode)) {
            sql += " and traffic.countrycode = :countrycode";
        }
        if(StringUtils.isNotEmpty(regioncode)) {
            sql += " and traffic.regioncode = :regioncode";
        }
        if(StringUtils.isNotEmpty(citycode)) {
            sql += " and traffic.citycode = :citycode";
        }


        //获取country真实表名
        String tableNameSql = "select concat('', case t.SUCC_COUNT when 0 then '_N' else (select case c.IS_SUFFIX when 0 then '' when 1 then '_N' else '_N' end TAB_NAME from (select a.IS_SUFFIX from TMS_MGR_IPLOG a where a.IPLOG_ID=(select max(b.IPLOG_ID) from TMS_MGR_IPLOG b where b.OPERATE_RESULT=1)) c) end) TAB_NAME from (select count(*) SUCC_COUNT from TMS_MGR_IPLOG where OPERATE_RESULT=1) t";
        List<Map<String, Object>> tableNameList = offlineSimpleDao.queryForList(tableNameSql, new HashMap());
        String tableName = Objects.toString(tableNameList.get(0).get("TAB_NAME"), "_N");
        String countryTableName = "TMS_MGR_COUNTRY" + tableName;
        String regionTableName = "TMS_MGR_REGION" + tableName;
        String cityTableName = "TMS_MGR_CITY" + tableName;

        sql = sql.replaceAll("TMS_COUNTRY", countryTableName);
        sql = sql.replaceAll("TMS_REGION", regionTableName);
        sql = sql.replaceAll("TMS_CITY", cityTableName);

        Order order = new Order();
        order.desc("traffic.txntime");
        Page<Map<String, Object>> page = offlineSimpleDao.pageQuery(sql, requestModel, order);
        String timeout = Objects.toString(systemParamValueFunction.execute("300000", new String[]{"alarmProcessTimeout"}), "300000");
        String currentTime = String.valueOf(System.currentTimeMillis());

        for(Map<String, Object> loopMap : page.getList()) {
            loopMap.put("timeout", timeout);
            loopMap.put("currentTime", currentTime);
        }

        return page;
    }

    public List<Map<String, Object>> queryAlarmEventOperateDetail(RequestModel requestModel) {
        String sql = "select traffic.*, concat(ifnull((select country.countryname from TMS_COUNTRY country where traffic.countrycode = country.countrycode), '未知'), '-', ifnull((select region.regionname from TMS_REGION region where traffic.regioncode = region.regioncode), '未知'), '-', ifnull((select city.cityname from TMS_CITY city where traffic.citycode = city.citycode), '未知')) LOCATION from TMS_RUN_TRAFFICDATA traffic where 1=1";
        //获取country真实表名
        String tableNameSql = "select concat('', case t.SUCC_COUNT when 0 then '_N' else (select case c.IS_SUFFIX when 0 then '' when 1 then '_N' else '_N' end TAB_NAME from (select a.IS_SUFFIX from TMS_MGR_IPLOG a where a.IPLOG_ID=(select max(b.IPLOG_ID) from TMS_MGR_IPLOG b where b.OPERATE_RESULT=1)) c) end) TAB_NAME from (select count(*) SUCC_COUNT from TMS_MGR_IPLOG where OPERATE_RESULT=1) t";
        List<Map<String, Object>> tableNameList = offlineSimpleDao.queryForList(tableNameSql, new HashMap());
        String tableName = Objects.toString(tableNameList.get(0).get("TAB_NAME"), "_N");
        String countryTableName = "TMS_MGR_COUNTRY" + tableName;
        String regionTableName = "TMS_MGR_REGION" + tableName;
        String cityTableName = "TMS_MGR_CITY" + tableName;

        sql = sql.replaceAll("TMS_COUNTRY", countryTableName);
        sql = sql.replaceAll("TMS_REGION", regionTableName);
        sql = sql.replaceAll("TMS_CITY", cityTableName);

        sql += " and traffic.txncode = :txncode";

        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

    public List<Map<String, Object>> queryAlarmEventHandleDetail(RequestModel requestModel) {
        String sql = "select ps.ps_id, co.login_name as ps_opername, ps.ps_type, ps.ps_result, ps.ps_info, ps.ps_time from TMS_MGR_ALARM_PROCESS ps inner join CMC_OPERATOR co on ps.ps_operid = co.operator_id where 1 = 1";

        sql += " and ps.txn_code = :txncode";

        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

    public List<Map<String, Object>> queryAlarmEventUserDetail(RequestModel requestModel) {
        String sql = "select u.* from TMS_RUN_USER u where u.userid = :userid";

        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

    public List<Map<String, Object>> queryAlarmEventDeviceDetail(RequestModel requestModel) {

        String sql = "select * from TMS_DFP_DEVICE where device_id = :deviceid";

        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

    public List<Map<String, Object>> queryAlarmEventSessionDetail(RequestModel requestModel) {

        String sql = "select CONCAT(FROM_UNIXTIME(s.finishtime / 1000, '%Y-%m-%d'), ' ', '00:00:00') operate_time, FROM_UNIXTIME(s.starttime / 1000, '%Y-%m-%d %H:%i:%s') session_start_time, FROM_UNIXTIME(s.finishtime / 1000, '%Y-%m-%d %H:%i:%s') session_end_time, TMS_RUN_SESSION.*, concat(ifnull((select country.countryname from TMS_COUNTRY country where s.countrycode = country.countrycode), '未知'), '-', ifnull((select region.regionname from TMS_REGION region where s.regioncode = region.regioncode), '未知'), '-', ifnull((select city.cityname from TMS_CITY city where s.citycode = city.citycode), '未知')) LOCATION from TMS_RUN_SESSION where sessionid = :sessionid";

        //获取country真实表名
        String tableNameSql = "select concat('', case t.SUCC_COUNT when 0 then '_N' else (select case c.IS_SUFFIX when 0 then '' when 1 then '_N' else '_N' end TAB_NAME from (select a.IS_SUFFIX from TMS_MGR_IPLOG a where a.IPLOG_ID=(select max(b.IPLOG_ID) from TMS_MGR_IPLOG b where b.OPERATE_RESULT=1)) c) end) TAB_NAME from (select count(*) SUCC_COUNT from TMS_MGR_IPLOG where OPERATE_RESULT=1) t";
        List<Map<String, Object>> tableNameList = offlineSimpleDao.queryForList(tableNameSql, new HashMap());
        String tableName = Objects.toString(tableNameList.get(0).get("TAB_NAME"), "_N");
        String countryTableName = "TMS_MGR_COUNTRY" + tableName;
        String regionTableName = "TMS_MGR_REGION" + tableName;
        String cityTableName = "TMS_MGR_CITY" + tableName;

        sql = sql.replaceAll("TMS_COUNTRY", countryTableName);
        sql = sql.replaceAll("TMS_REGION", regionTableName);
        sql = sql.replaceAll("TMS_CITY", cityTableName);

        List<Map<String, Object>> list = onlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

    public Page<Map<String, Object>> queryAlarmEventdeviceFingerDetail(RequestModel requestModel) {

        String sql = "SELECT P.PROP_NAME,p.PROP_TYPE,P.PROP_COMMENT,AP.STORECOLUMN" +
                " FROM TMS_DFP_APP_PROPERTIES AP,TMS_DFP_PROPERTY P WHERE AP.PROP_ID=P.PROP_ID AND AP.APP_ID = :APP_ID";
        Map<String, Object> conds = new HashMap<String, Object>();
        conds.put("device_id", requestModel.get("device_id"));
        Map<String, Object> deviceMap = offlineSimpleDao.retrieve("TMS_DFP_DEVICE", conds);
        if (!CmcMapUtil.isEmpty(deviceMap)) {
//            String appId = CmcMapUtil.getString(deviceMap, "APP_ID");
            requestModel.put("APP_ID", deviceMap.get("APP_ID"));
            Page<Map<String, Object>> appPropPage = offlineSimpleDao.pageQuery(sql, requestModel, new Order());
            List<Map<String, Object>> appPropList = appPropPage.getList();
            for (Map<String, Object> appPropMap : appPropList) {
                String column = CmcMapUtil.getString(appPropMap, "STORECOLUMN");
                appPropMap.put("DEVICE_ID", CmcMapUtil.getString(deviceMap, "DEVICE_ID"));
                if (!CmcStringUtil.isBlank(column)) {
                    appPropMap.put("PROP_VALUE", CmcMapUtil.getString(deviceMap, column));
                }
            }
            return appPropPage;
        }
        return null;
    }

    public List<Map<String, Object>> getShowFields(String tabName){
        String parentSql = "select q.tab_name, q.tab_type, q.view_cond, q.base_tab, q.parent_tab from tms_com_tab q where q.tab_name in (" + TransCommon.arr2str(TransCommon.cutToIds(tabName)) + ")";
        parentSql = "select t." + DBConstant.TMS_COM_TAB.TAB_NAME + " from (" + parentSql + ") t";
        String sql = "select * from " + DBConstant.TMS_COM_FD + " fd where fd." + DBConstant.TMS_COM_FD_TAB_NAME + " in (" + parentSql + ")" + " and (fd." + DBConstant.TMS_COM_FD_FD_NAME + " is not null or fd." + DBConstant.TMS_COM_FD_FD_NAME + " <> '')" + " order by fd."
                + DBConstant.TMS_COM_FD_TAB_NAME + ", fd." + DBConstant.TMS_COM_FD_ORDERBY;// 查询表字段及其父表字段信息
        return onlineSimpleDao.queryForList(sql);
    }


}
