package cn.com.higinet.tms.base.entity.common;

import lombok.Data;

@Data
public class SelectItem {

	String value;
	String label;

	public SelectItem( String value, String label ) {
		this.value = value;
		this.label = label;
	}
}
