package cn.com.higinet.tms.manager.modules.channel.service;

import cn.com.higinet.tms.base.entity.common.RequestModel;

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
    Map<String, Object> insertChannel(RequestModel requestModel);

    /**
     * 查询渠道信息
     *
     * @return
     */
    Map<String, Object> getChannelInfo(RequestModel requestModel);

    /**
     * 编辑渠道信息
     *
     * @return
     */
    Map<String, Object> updateChannel(RequestModel requestModel);

    /**
     * 删除渠道信息
     *
     * @return
     */
    Map<String, Object> deleteChannel(RequestModel requestModel);

    /**
     * 渠道信息重复性校验
     *
     * @return
     */
    Map<String, Object> checkChannelInfo(RequestModel requestModel);


}
