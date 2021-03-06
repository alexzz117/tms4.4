package cn.com.higinet.tms.manager.modules.query.controller;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
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
import java.util.Map;

@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/query/alarmEvent/")
public class AlarmEventQueryController {

    private static final Logger logger = LoggerFactory.getLogger( AlarmEventQueryController.class );

    @Autowired
    private AlarmEventQueryService alarmEventQueryService;

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public Model showQueryResultAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute( ManagerConstants.SESSION_KEY_OPERATOR );
        String operId = CmcStringUtil.objToString( operator.get( DBConstant.CMC_OPERATOR_OPERATOR_ID ) );
        reqs.put("loginOperatorId", operId);
        model.setPage(alarmEventQueryService.queryAlarmEventData(reqs));

        return model;
    }

    @RequestMapping(value = "/operateDetail", method = RequestMethod.POST)
    public Model showQueryOperateDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventOperateDetail(reqs));
        String txnType = reqs.getString("txnTypeShowFields");
        model.set("showFields", alarmEventQueryService.getShowFields(txnType));
        return model;
    }

    @RequestMapping(value = "/handleDetail", method = RequestMethod.POST)
    public Model showQueryHandleDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventHandleDetail(reqs));

        return model;
    }

    @RequestMapping(value = "/userDetail", method = RequestMethod.POST)
    public Model showQueryUserDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventUserDetail(reqs));

        return model;
    }

    @RequestMapping(value = "/deviceDetail", method = RequestMethod.POST)
    public Model showQueryDeviceDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventDeviceDetail(reqs));

        return model;
    }

    @RequestMapping(value = "/deviceFingerDetail", method = RequestMethod.POST)
    public Model showQueryDeviceFingerDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setPage(alarmEventQueryService.queryAlarmEventdeviceFingerDetail(reqs));

        return model;
    }

    @RequestMapping(value = "/sessionDetail", method = RequestMethod.POST)
    public Model showQuerySessionDetailAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();

        model.setRow(alarmEventQueryService.queryAlarmEventSessionDetail(reqs));

        return model;
    }



}
