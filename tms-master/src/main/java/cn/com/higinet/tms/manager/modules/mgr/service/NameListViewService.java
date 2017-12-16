package cn.com.higinet.tms.manager.modules.mgr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * @author zhangfg
 * @author zhanglei.lei
 */
@Service("nameListViewService")
public class NameListViewService extends NameListService {

	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;

	public SimpleDao getSimpleDao() {
		return onlineSimpleDao;
	}
}
