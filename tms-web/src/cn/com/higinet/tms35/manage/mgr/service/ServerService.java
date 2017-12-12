package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

public interface ServerService {

	List<Map<String, Object>> listServer(Map<String, String> conds);
}
