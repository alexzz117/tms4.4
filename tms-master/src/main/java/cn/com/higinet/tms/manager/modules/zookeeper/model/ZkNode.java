package cn.com.higinet.tms.manager.modules.zookeeper.model;

public class ZkNode {

	String value;
	String label;
	String path;
	String type;
	boolean leaf;
	boolean root;

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}



	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}



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
