package cn.com.higinet.tms.base.entity.common;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DictCategory {

	String id;
	String name;
	String info;
	List<Map<String, Object>> items;

}
