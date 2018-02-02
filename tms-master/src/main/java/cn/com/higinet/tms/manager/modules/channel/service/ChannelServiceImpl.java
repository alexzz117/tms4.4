package cn.com.higinet.tms.manager.modules.channel.service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
        String sql = "SELECT * FROM TMS_DP_CHANNEL";
        List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql);
        return list;
    }

    /**
     * 插入新渠道
     *
     * @return
     */
    public Map<String, Object> insertChannel(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String checkResult = String.valueOf(checkChannelInfo(params).get("result"));
            if (StringUtils.equals("success", checkResult)) {
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
    public Map<String, Object> getChannelInfo(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        String channelid = String.valueOf(params.get("channelid"));
        String sql = "SELECT * FROM TMS_DP_CHANNEL cha where cha.CHANNELID = :channelid";
        List<Map<String, Object>> resultList = offlineSimpleDao.queryForList(sql, channelid);
        if (resultList.size() > 0) {
            resultMap = resultList.get(0);
        }
        return resultMap;
    }

    /**
     * 编辑渠道信息
     *
     * @return
     */
    public Map<String, Object> updateChannel(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String checkResult = String.valueOf(checkChannelInfo(params).get("result"));
            if (StringUtils.equals("success", checkResult)) {
                Map<String, Object> row = new HashMap<>();
                row.put("CHANNELNAME", params.get("channelname"));
                row.put("ORDERBY", params.get("orderby"));
                Map<String, Object> conds = new HashMap<>();
                conds.put("CHANNELID", params.get("channelid"));
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
    public Map<String, Object> deleteChannel(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int result = offlineSimpleDao.delete("TMS_DP_CHANNEL", params);
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
    public Map<String, Object> checkChannelInfo(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String op = String.valueOf(params.get("op"));   // 操作标识；add：添加；mod：编辑
            String sql = "SELECT * FROM TMS_DP_CHANNEL cha";
            if (StringUtils.equals("add", op)) {
                sql += " WHERE cha.CHANNELID =:channelid or cha.CHANNELNAME =:channelname or cha.ORDERBY =:orderby";
            } else {
                sql += " WHERE cha.CHANNELID !=:channelid and (cha.CHANNELNAME =:channelname or cha.ORDERBY =:orderby)";
            }
            List<Map<String, Object>> resultList = offlineSimpleDao.queryForList(sql, params);
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
