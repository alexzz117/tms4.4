package cn.com.higinet.tms.manager.modules.query.service;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
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
    @Qualifier("onlineSimpleDao")
    private SimpleDao onlineSimpleDao;

    public Page<Map<String, Object>> queryAlarmEventData(RequestModel requestModel) {
        String sql = "SELECT DISTINCT CONCAT(FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d' ), ' ', '00:00:00') operate_time, FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d %H:%i:%s') end_time, traffic.txncode, traffic.userid, u.username, traffic.txntype, (SELECT CONCAT(channel.channelname, '--', tab.tab_desc) FROM tms_dp_channel channel, tms_com_tab tab WHERE traffic.txntype = tab.tab_name AND tab.chann = channel.channelid) TXNNAME, traffic.txntime, CONCAT( IFNULL((SELECT country.countryname FROM TMS_COUNTRY country WHERE traffic.countrycode = country.countrycode), '未知'), '-', IFNULL((SELECT region.regionname FROM TMS_REGION region WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知'), '-', IFNULL((SELECT city.cityname FROM TMS_CITY city WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode AND traffic.citycode = city.citycode), '未知')) LOCATION, IFNULL(traffic.iscorrect, '2') iscorrect, (select DP_NAME from tms_com_disposal dp where traffic.disposal=dp.DP_CODE)  disposal, traffic.txnstatus, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.assignid = o.OPERATOR_ID) assign_name, traffic.assigntime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.operid = o.OPERATOR_ID) oper_name, traffic.opertime, (select o.LOGIN_NAME from CMC_OPERATOR o where traffic.auditid = o.OPERATOR_ID) audit_name, traffic.audittime, traffic.psstatus, traffic.strategy FROM TMS_RUN_TRAFFICDATA traffic LEFT JOIN TMS_RUN_USER u ON traffic.userid = u.userid WHERE 1 = 1 and traffic.HITRULENUM !=0 ";

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

        String txncode = requestModel.getString("txncode");

        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, requestModel);
        return list;
    }

}
