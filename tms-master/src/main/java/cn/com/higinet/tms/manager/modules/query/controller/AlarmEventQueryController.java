package cn.com.higinet.tms.manager.modules.query.controller;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.query.service.AlarmEventQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/query/alarmEvent/")
public class AlarmEventQueryController {

    private static final Logger logger = LoggerFactory.getLogger( AlarmEventQueryController.class );

    @Autowired
    private AlarmEventQueryService alarmEventQueryService;

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public Model showQueryResultAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setPage(alarmEventQueryService.queryAlarmEventData(reqs));

        return model;
    }

    @RequestMapping(value = "/operateDetail", method = RequestMethod.POST)
    public Model showQueryOperateDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventOperateDetail(reqs));

        return model;
    }



}
