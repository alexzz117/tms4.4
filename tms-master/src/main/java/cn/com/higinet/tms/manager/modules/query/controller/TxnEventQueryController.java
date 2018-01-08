package cn.com.higinet.tms.manager.modules.query.controller;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.manager.modules.query.service.TxnEventQueryService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/query/tnxEvent/")
public class TxnEventQueryController {
    private static final Logger logger = LoggerFactory.getLogger(AlarmEventQueryController.class);

    @Autowired
    TxnEventQueryService txnEventQueryService;

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public Model showQueryResultAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        model.setPage(txnEventQueryService.queryTxnEventData(reqs));
        return model;
    }
    @RequestMapping(value = "/txnStatQuery", method = RequestMethod.POST)
    public Model txnStatQueryAction(@RequestBody RequestModel reqs, HttpServletRequest request) {
        Model model = new Model();
        model.setList(txnEventQueryService.queryTxnStatData(reqs));
        return model;
    }
}
