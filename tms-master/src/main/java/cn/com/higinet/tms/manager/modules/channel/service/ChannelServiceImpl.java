package cn.com.higinet.tms.manager.modules.channel.service;

import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 渠道服务实现类
 *
 * @author wangsy
 * @version 1.0.0 2018-02-02
 */
@Service("ChannelService")
public class ChannelServiceImpl extends ApplicationObjectSupport implements ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    @Qualifier("offlineSimpleDao")
    private SimpleDao offlineSimpleDao;

    @Autowired
    @Qualifier("onlineSimpleDao")
    private SimpleDao onlineSimpleDao;


    /**
     * 查询所有渠道
     *
     * @return
     */
    public List<Map<String, Object>> queryAllChannel() {
        String sql = "SELECT ch.* ,(SELECT COUNT(ta.TAB_NAME) FROM  TMS_COM_TAB ta where ta.CHANN = ch.CHANNELID) num FROM  TMS_DP_CHANNEL ch";
        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql);
        return list;
    }

    /**
     * 插入新渠道
     *
     * @return
     */
    public Map<String, Object> insertChannel(RequestModel requestModel) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String checkResult = String.valueOf(checkChannelInfo(requestModel).get("result"));
            if (StringUtils.equals("success", checkResult)) {
                Map<String,Object> params = new HashMap<>();
                params.put("channelid",requestModel.get("channelid"));
                params.put("channelname",requestModel.get("channelname"));
                params.put("orderby",requestModel.get("orderby"));
                offlineSimpleDao.create("TMS_DP_CHANNEL", params);
                resultMap.put("result", "success");
            } else {
                resultMap.put("result", "has");
            }

        } catch (Exception e) {
            resultMap.put("result", "error");
        }
        return resultMap;
    }

    /**
     * 查询渠道信息
     *
     * @return
     */
    public Map<String, Object> getChannelInfo(RequestModel requestModel) {
        Map<String, Object> resultMap = new HashMap<>();
        String sql = "SELECT * FROM TMS_DP_CHANNEL cha where ";
        List<String> paramList = new LinkedList<>();
        if (requestModel.containsKey("channelid") && StringUtils.isNotBlank(requestModel.getString("channelid"))) {
            paramList.add("cha.CHANNELID = :channelid");
        }
        if (requestModel.containsKey("channelname") && StringUtils.isNotBlank(requestModel.getString("channelname"))) {
            paramList.add("cha.CHANNELNAME = :channelname");
        }
        if (requestModel.containsKey("orderby") && StringUtils.isNotBlank(requestModel.getString("orderby"))) {
            paramList.add("cha.ORDERBY = :orderby");
        }
        if (paramList.size() > 0) {
            sql += StringUtils.join(paramList, " and ");
            List<Map<String, Object>> resultList = offlineSimpleDao.queryForList(sql, requestModel);
            if (resultList.size() > 0) {
                resultMap = resultList.get(0);
            }
        }
        return resultMap;
    }

    /**
     * 编辑渠道信息
     *
     * @return
     */
    public Map<String, Object> updateChannel(RequestModel requestModel) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String checkResult = String.valueOf(checkChannelInfo(requestModel).get("result"));
            if (StringUtils.equals("success", checkResult)) {
                Map<String, Object> row = new HashMap<>();
                row.put("CHANNELNAME", requestModel.getString("channelname"));
                row.put("ORDERBY", requestModel.getString("orderby"));
                Map<String, Object> conds = new HashMap<>();
                conds.put("CHANNELID", requestModel.getString("channelid"));
                int result = offlineSimpleDao.update("TMS_DP_CHANNEL", row, conds);
                if (result > 0) {
                    resultMap.put("result", "success");
                } else {
                    resultMap.put("result", "failed");
                }
            } else {
                resultMap.put("result", "has");
            }

        } catch (Exception e) {
            resultMap.put("result", "error");
        }
        return resultMap;
    }

    /**
     * 删除渠道信息
     *
     * @return
     */
    public Map<String, Object> deleteChannel(RequestModel requestModel) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Map<String, Object>> tabs = offlineSimpleDao.queryForList("SELECT * FROM  `tms_com_tab` where CHANN =:channelid", requestModel);
            if (tabs.size() > 0) {
                resultMap.put("result", "has");
                return resultMap;
            }
            int result = offlineSimpleDao.delete("TMS_DP_CHANNEL", requestModel);
            if (result > 0) {
                resultMap.put("result", "success");
            } else {
                resultMap.put("result", "failed");
            }
        } catch (Exception e) {
            resultMap.put("result", "error");
        }
        return resultMap;
    }

    /**
     * 渠道信息重复性校验
     *
     * @return
     */
    public Map<String, Object> checkChannelInfo(RequestModel requestModel) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String op = requestModel.getString("op");   // 操作标识；add：添加；mod：编辑
            String sql = "SELECT * FROM TMS_DP_CHANNEL cha WHERE ";
            if (StringUtils.equals("add", op)) {
                sql += "cha.CHANNELID =:channelid or cha.CHANNELNAME =:channelname or cha.ORDERBY =:orderby";
            } else {
                sql += "cha.CHANNELID !=:channelid and (cha.CHANNELNAME =:channelname or cha.ORDERBY =:orderby)";
            }
            List<Map<String, Object>> resultList = offlineSimpleDao.queryForList(sql, requestModel);
            if (resultList.size() > 0) {
                resultMap.put("result", "failed");
            } else {
                resultMap.put("result", "success");
            }
        } catch (Exception e) {
            resultMap.put("result", "error");
        }
        return resultMap;

    }

}
