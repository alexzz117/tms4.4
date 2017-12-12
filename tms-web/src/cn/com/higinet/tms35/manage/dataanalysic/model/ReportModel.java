package cn.com.higinet.tms35.manage.dataanalysic.model;

import java.util.List;

import cn.com.higinet.rapid.web.model.Model;

public class ReportModel extends Model {
	
	public static final String GEOCOORDMAPLIST = "geocoordmaplist";
	public static final String SOURCEMAPLIST = "sourcemaplist";
	public ReportModel() {
		super();
	}

	
	//城市坐标列表
	public Model setGeoCoordMaplist(List list) {
		set(GEOCOORDMAPLIST, list);
		return this;
	}
	
	//源点坐标列表
	public Model setSourceMaplist(List list) {
		set(SOURCEMAPLIST, list);
		return this;
	}
}