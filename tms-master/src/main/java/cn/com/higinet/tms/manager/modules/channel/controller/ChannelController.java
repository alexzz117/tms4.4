/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.modules.channel.controller;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.manager.modules.channel.service.ChannelService;

import javax.servlet.http.HttpServletRequest;

/**
 * 渠道控制类
 *
 * @author wangsy
 * @version 1.0.0, 2018-2-2
 */
@RestController("channelController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/channel")
public class ChannelController extends ApplicationObjectSupport {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 显示渠道列表
     *
     * @param reqs
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Model channelListAction(@RequestParam Map<String, Object> reqs) {
        Model model = new Model();
        List<Map<String, Object>> channelList = channelService.queryAllChannel();
        model.setList(channelList);
        return model;
    }

    /**
     * 插入新渠道
     *
     * @param reqs
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Model addChannelAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        Map<String, Object> result = channelService.insertChannel(reqs);
        model.putAll(result);
        return model;
    }

    /**
     * 获取渠道信息
     *
     * @param reqs
     * @return
     */
    @RequestMapping(value = "/getChannel", method = RequestMethod.POST)
    public Model getChannelAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        Map<String, Object> result = channelService.getChannelInfo(reqs);
        model.addAttribute("info",result);
        return model;
    }

    /**
     * 编辑渠道信息
     *
     * @param reqs
     * @return
     */
    @RequestMapping(value = "/mod", method = RequestMethod.POST)
    public Model modChannelAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        Map<String, Object> result = channelService.updateChannel(reqs);
        model.putAll(result);
        return model;
    }

    /**
     * 删除渠道信息
     *
     * @param reqs
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public Model delChannelAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        Map<String, Object> result = channelService.deleteChannel(reqs);
        model.putAll(result);
        return model;
    }
}