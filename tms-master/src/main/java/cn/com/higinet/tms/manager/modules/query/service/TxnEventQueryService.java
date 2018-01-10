package cn.com.higinet.tms.manager.modules.query.service;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("txnEventQueryService")
@Transactional
public class TxnEventQueryService {

    @Autowired
    @Qualifier("offlineSimpleDao")
    private SimpleDao offlineSimpleDao;

    @Autowired
    @Qualifier("onlineSimpleDao")
    private SimpleDao onlineSimpleDao;

    public Page<Map<String, Object>> queryTxnEventData(RequestModel requestModel) {
        String sql = "SELECT DISTINCT CONCAT(FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d'), ' ', '00:00:00') operate_time, FROM_UNIXTIME(traffic.txntime / 1000, '%Y-%m-%d %H:%i:%s') end_time, traffic.txncode, traffic.txntype, traffic.userid, traffic.sessionid, (SELECT CONCAT( channel.channelname, '--', tab.tab_desc ) FROM tms_dp_channel channel, tms_com_tab tab WHERE traffic.txntype = tab.tab_name AND tab.chann = channel.channelid) TXNNAME, traffic.txntime, CONCAT( IFNULL( (SELECT country.countryname FROM TMS_COUNTRY country WHERE traffic.countrycode = country.countrycode), '未知' ), '-', IFNULL( (SELECT region.regionname FROM TMS_REGION region WHERE traffic.countrycode = region.countrycode AND traffic.regioncode = region.regioncode), '未知' ), '-', IFNULL( (SELECT city.cityname FROM TMS_CITY city WHERE traffic.countrycode = city.countrycode AND traffic.regioncode = city.regioncode AND traffic.citycode = city.citycode), '未知' ) ) LOCATION, traffic.deviceid, traffic.ipaddr, traffic.iseval, traffic.ismodelrisk, traffic.hitrulenum, traffic.score, ifnull(traffic.iscorrect, '2') iscorrect, (select DP_NAME from tms_com_disposal dp where traffic.disposal=dp.DP_CODE)  disposal, ifnull(traffic.confirmrisk,2) confirmrisk, traffic.txnstatus FROM TMS_RUN_TRAFFICDATA traffic LEFT JOIN TMS_RUN_USER u ON traffic.userid = u.userid LEFT JOIN tms_run_ruletrig r ON traffic.TXNCODE = r.TXNCODE WHERE 1 = 1";

        String ruleid = requestModel.getString("ruleid");
        String txncode = requestModel.getString("txncode");
        String userid = requestModel.getString("userid");
        String username = requestModel.getString("username");
        String ipaddr = requestModel.getString("ipaddr");
        String deviceid = requestModel.getString("deviceid");
        String sessionid = requestModel.getString("sessionid");
        String txntype = requestModel.getString("txntype");
        String disposal = requestModel.getString("disposal");
        String iseval = requestModel.getString("iseval");
        String iscorrect = requestModel.getString("iscorrect");
        String confirmrisk = requestModel.getString("confirmrisk");
        String txnstatus = requestModel.getString("txnstatus");
        String countrycode = requestModel.getString("countrycode");
        String regioncode = requestModel.getString("regioncode");
        String citycode = requestModel.getString("citycode");
        String startTimeStr = requestModel.getString("operate_time");
        String endTimeStr = requestModel.getString("end_time");
        String payaccount = requestModel.getString("payaccount");
        String recaccount = requestModel.getString("recaccount");

        if (StringUtils.isNotEmpty(ruleid)) {
            sql += " and r.ruleid = :ruleid";
        }
        if (StringUtils.isNotEmpty(txncode)) {
            sql += " and traffic.txncode = :txncode";
        }
        if (StringUtils.isNotEmpty(userid)) {
            sql += " and traffic.userid = :userid";
        }
        if (StringUtils.isNotEmpty(username)) {
            sql += " and u.username = :username";
        }
        if (StringUtils.isNotEmpty(deviceid)) {
            sql += " and traffic.deviceid = :deviceid";
        }
        if (StringUtils.isNotEmpty(sessionid)) {
            sql += " and traffic.sessionid = :sessionid";
        }

        if (StringUtils.isNotEmpty(ipaddr)) {
            sql += " and traffic.ipaddr = :ipaddr";
        }
        if (StringUtils.isNotEmpty(txntype)) {
            String[] txnTypes = txntype.split(",");
            List<String> txnTypeList = new ArrayList<>(Arrays.asList(txnTypes));
            requestModel.put("txnTypeList", txnTypeList);
            sql += " and traffic.txntype in (:txnTypeList)";
        }
        if (StringUtils.isNotEmpty(disposal)) {
            sql += " and traffic.disposal = :disposal";
        }
        if (StringUtils.isNotEmpty(iseval)) {
            sql += " and traffic.iseval = :iseval";
        }
        if (StringUtils.isNotEmpty(iscorrect)) {
            sql += " and ifnull(traffic.iscorrect,2) = :iscorrect";
        }
        if (StringUtils.isNotEmpty(confirmrisk)) {
            sql += " and ifnull(traffic.confirmrisk,2) = :confirmrisk";
        }
        if (StringUtils.isNotEmpty(txnstatus)) {
            sql += " and traffic.txnstatus = :txnstatus";
        }
        if (StringUtils.isNotEmpty(startTimeStr) && StringUtils.isNotEmpty(endTimeStr)) {
            long startTime = Long.parseLong(startTimeStr);
            long endTime = Long.parseLong(endTimeStr);

            requestModel.put("startTimeStamp", startTime);
            requestModel.put("endTimeStamp", endTime);
            sql += " and traffic.txntime between :startTimeStamp and :endTimeStamp";
        }
        if (StringUtils.isNotEmpty(countrycode)) {
            sql += " and traffic.countrycode = :countrycode";
        }
        if (StringUtils.isNotEmpty(regioncode)) {
            sql += " and traffic.regioncode = :regioncode";
        }
        if (StringUtils.isNotEmpty(citycode)) {
            sql += " and traffic.citycode = :citycode";
        }
        if (StringUtils.isNotEmpty(payaccount)) {
            sql += " and traffic.text1 = :payaccount";
        }
        if (StringUtils.isNotEmpty(recaccount)) {
            sql += " and traffic.text2 = :recaccount";
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

    public List<Map<String, Object>> queryTxnStatData(RequestModel requestModel) {
        String txnCode = requestModel.getString("txnCode");

        Map<String, Object> conds = new HashMap<String, Object>();
        conds.put("txncode", txnCode);
        Map<String, Object> txnMap = onlineSimpleDao.retrieve(DBConstant.TMS_RUN_TRAFFICDATA, conds);
        if (!CmcMapUtil.isEmpty(txnMap)) {
            String txnType = CmcMapUtil.getString(txnMap,
                    DBConstant.TMS_RUN_TRAFFICDATA_TXNTYPE);
            String sql = "select " + DBConstant.TMS_COM_STAT_STAT_DESC + ", "
                    + DBConstant.TMS_COM_STAT_STORECOLUMN
                    + " from TMS_COM_STAT where "
                    + DBConstant.TMS_COM_STAT_STAT_TXN + " in ("
                    + TransCommon.arr2str(TransCommon.cutToIds(txnType)) + ")"
                    + " and (" + DBConstant.TMS_COM_STAT_STORECOLUMN + " is not null"
                    + " or " + DBConstant.TMS_COM_STAT_STORECOLUMN + " <> '')"
                    + " order by " + DBConstant.TMS_COM_STAT_STAT_ID;
            List<Map<String, Object>> statList = onlineSimpleDao.queryForList(sql);
            for (Map<String, Object> statMap : statList) {
                String column = CmcMapUtil.getString(statMap, DBConstant.TMS_COM_STAT_STORECOLUMN);
                Object value = CmcMapUtil.getObject(txnMap, column);
                statMap.put("STOREVALUE", value);
            }
            return statList;
        }
        return null;
    }
}
