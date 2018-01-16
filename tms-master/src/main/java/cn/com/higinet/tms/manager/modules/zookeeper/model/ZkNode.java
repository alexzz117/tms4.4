package cn.com.higinet.tms.manager.modules.zookeeper.model;

public class ZkNode {

	String value;
	String label;
	String path;

	public String getValue() {
		return value;
	}

	public void setValue( String value ) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel( String label ) {
		this.label = label;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}

}
