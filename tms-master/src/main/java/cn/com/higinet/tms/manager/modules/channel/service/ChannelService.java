package cn.com.higinet.tms.manager.modules.channel.service;

import java.util.List;
import java.util.Map;


/**
 * @author wangsy
 * @version 1.0.0 2018-02-02
 * @disctripte 渠道服务接口类
 */
public interface ChannelService {

    /**
     * 查询所有渠道
     *
     * @return
     */
    List<Map<String, Object>> queryAllChannel();

    /**
     * 插入新渠道
     *
     * @return
     */
    Map<String, Object> insertChannel(Map<String, Object> params);

    /**
     * 查询渠道信息
     *
     * @return
     */
    Map<String, Object> getChannelInfo(Map<String, Object> params);

    /**
     * 编辑渠道信息
     *
     * @return
     */
    Map<String, Object> updateChannel(Map<String, Object> params);

    /**
     * 删除渠道信息
     *
     * @return
     */
    Map<String, Object> deleteChannel(Map<String, Object> params);

    /**
     * 渠道信息重复性校验
     *
     * @return
     */
    Map<String, Object> checkChannelInfo(Map<String, Object> params);


}
