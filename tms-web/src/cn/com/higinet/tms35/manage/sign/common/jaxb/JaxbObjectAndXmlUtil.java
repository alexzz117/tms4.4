package cn.com.higinet.tms35.manage.sign.common.jaxb;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbObjectAndXmlUtil {
	/**
	 * 将对象转换为xml
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public static <T> String objectToXml(T t) {
		try {
			JAXBContext context = JAXBContext.newInstance(t.getClass());
			Marshaller marshal = context.createMarshaller();
			marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// 格式化输出
			marshal.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// 编码格式,默认为utf-8
			marshal.setProperty(Marshaller.JAXB_FRAGMENT, true);// 是否省略xml头信息
			StringWriter writer = new StringWriter();
			marshal.marshal(t, writer);
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将xml转换为对象
	 * 
	 * @param classs
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T xmlToObject(Class<T> classs, String xml) {
		try {
			JAXBContext context = JAXBContext.newInstance(classs);
			Unmarshaller us = context.createUnmarshaller();
			StringReader reader = new StringReader(xml);
			return (T) us.unmarshal(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}