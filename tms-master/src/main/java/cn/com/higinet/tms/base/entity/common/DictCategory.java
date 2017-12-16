package cn.com.higinet.tms.base.entity.common;

import java.util.List;
import java.util.Map;

public class DictCategory {

	String id;
	String name;
	String info;
	List<Map<String, Object>> items;

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo( String info ) {
		this.info = info;
	}

	public List<Map<String, Object>> getItems() {
		return items;
	}

	public void setItems( List<Map<String, Object>> items ) {
		this.items = items;
	}

}
