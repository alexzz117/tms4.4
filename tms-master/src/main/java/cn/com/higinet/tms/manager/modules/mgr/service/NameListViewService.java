package cn.com.higinet.tms.manager.modules.mgr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * @author zhangfg
 */
@Service("nameListViewService")
public class NameListViewService extends NameListService {

	@Autowired
	private SimpleDao officialSimpleDao;

	public SimpleDao getSimpleDao() {
		return officialSimpleDao;
	}
}
