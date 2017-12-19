package cn.com.higinet.tms.manager.modules.sign.common.jaxb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cn.com.higinet.tms.engine.core.cond.date_tool;

/**
 * 日期时间类型参数值转换
 * @author lining
 */
public class JaxbDateAdapter extends XmlAdapter<String, Date> {

	@Override
	public Date unmarshal(String v) throws Exception {
		if (v == null)
			return null;
		return date_tool.parse(v);
	}

	@Override
	public String marshal(Date v) throws Exception {
		if (v == null)
			return null;
		DateFormat format = new SimpleDateFormat(date_tool.FMT_DTS);
		return format.format(v);
	}
}