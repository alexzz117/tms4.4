package cn.com.higinet.tms35.core.eval;


public class eval_row {
	private StringBuffer data;
	private String disposal;
	private float score;
	
	public eval_row() {
		data = new StringBuffer();
	}
	
	public void append(Object o) {
		if (data.length() > 0)
			data.append(",");
		data.append(String.valueOf(o));
	}
	
	public int get_data_length() {
		return data.length();
	}
	
	public String get_data() {
		return data.toString();
	}
	
	public String[] get_datas() {
		return get_data().split("\\,");
	}
	
	public void set_disposal(String dp) {
		this.disposal = dp;
	}
	
	public String get_disposal() {
		return disposal;
	}
	
	public void set_score(float score) {
		this.score = score;
	}
	
	public float get_score() {
		return score;
	}
}