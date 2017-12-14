package cn.com.higinet.tms.manager.modules.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;

/**
 * Java XML校验测试
 */
public class XMLUtil {

	private static Log log = LogFactory.getLog(XMLUtil.class);
	
	public static void main(String[] args) {
//		validateXMLByXSD();
		/*Document doc = createFuncXml();
		Element root = doc.getRootElement();
		root.selectSingleNode("ID").setText("123");
		Element params = (Element)root.selectSingleNode("Params");
		Element param = params.addElement("Param");
		param.setText("dd");
		log.debug(doc.asXML());*/
		try {
			InputStream xsd = new FileInputStream(new File("E:/MyDocuments/workfolder/higinet/svn/tms2/branches/tms_V4.1.0.2/tms-web/src/resources/config/amlConfig.xsd"));
			InputStream xml = new FileInputStream(new File("E:/MyDocuments/workfolder/higinet/svn/tms2/branches/tms_V4.1.0.2/tms-web/src/resources/config/amlConfig.xml"));
			System.out.println(validateXMLByXSD(xsd, xml));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void toXml(String path) {
		try {
			Document document = createFuncXml();
			//document.setXMLEncoding("utf-8");

			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileWriter(new File(path)),
					format);
			// new FileOutputStream
			writer.write(document);

			writer.close();
		} catch (Exception e) {
			log.error("XMLUtil method toXml error!",e);
		}
	}
	
	/**
	 * 通过XSD校验XML
	 * @param xsdInputStream	xsd文档流
	 * @param xmlInputStream	xml文档流
	 */
	public static String validateXMLByXSD(InputStream xsdInputStream, InputStream xmlInputStream) {
		XMLErrorHandler errorHandler = null;
		try {
			// 建立schema工厂
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			// 建立验证文档文件对象，利用此文件对象所封装的文件进行schema验证
			// 利用schema工厂，接收验证文档文件对象生成Schema对象
			Schema schema = schemaFactory.newSchema(new StreamSource(xsdInputStream));
			// 通过Schema产生针对于此Schema的验证器，利用schenaFile进行验证
			Validator validator = schema.newValidator();
			// 得到验证的数据源
			Source source = new StreamSource(xmlInputStream);
			validator.setErrorHandler(errorHandler = new XMLErrorHandler());
			// 开始验证
			validator.validate(source);
			
		} catch (Exception e) {
			
		} finally {
			if (errorHandler != null && errorHandler.getErrors().hasContent()) {
				return errorHandler.getErrors().asXML();
			}
		}
		return null;
	}

	/**
	 * 通过XSD（XML Schema）校验XML
	 */
	public static void validateXMLByXSD(String xmlFileName,String filename,Map<String, Object> errorEntity) {

//		String xmlFileName = "D:\\schema\\conditions.xml";
        String xsdFileName = "D:\\schema\\RuleConditionCheck.xsd";
		// String xmlFileName = "D:\\schema\\conditions.xml";
		// String xsdFileName = "D:\\schema\\ruleCondition.xsd";
		try {
			// 创建默认的XML错误处理器
			XMLErrorHandler errorHandler = new XMLErrorHandler();
			// 获取基于 SAX 的解析器的实例
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// 解析器在解析时验证 XML 内容。
			factory.setValidating(true);
			// 指定由此代码生成的解析器将提供对 XML 名称空间的支持。
			factory.setNamespaceAware(true);
			// 使用当前配置的工厂参数创建 SAXParser 的一个新实例。
			SAXParser parser = factory.newSAXParser();
			// 创建一个读取工具
			SAXReader xmlReader = new SAXReader();
			// 获取要校验xml文档实例
			Document xmlDocument = (Document) xmlReader.read(new File(
					xmlFileName));
			// 设置 XMLReader 的基础实现中的特定属性。核心功能和属性列表可以在
			// [url]http://sax.sourceforge.net/?selected=get-set[/url] 中找到。
			parser.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			parser.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaSource",
					"file:" + xsdFileName);
			// 创建一个SAXValidator校验工具，并设置校验工具的属性
			SAXValidator validator = new SAXValidator(parser.getXMLReader());
			// 设置校验工具的错误处理器，当发生错误时，可以从处理器对象中得到错误信息。
			validator.setErrorHandler(errorHandler);
			// 校验
			validator.validate(xmlDocument);

			XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
			// 如果错误信息不为空，说明校验失败，打印错误信息
			if (errorHandler.getErrors().hasContent()) {
				writer.write(errorHandler.getErrors());	
				writer.close();
			} else {
				log.debug("Good! XML file transit XSD file validate succeed!");
			}
		} catch (Exception ex) {
			log.error("XML file: " + xmlFileName + " transit XSD file:"
					+ xsdFileName + "validate failed.Caused by:" ,ex);
			errorEntity.put("conderinfo", filename+"中元素格式不匹配");
		}
	}
	
//	//替换xml校验错误
//	public static void setErrorByXmlCheck(String errorinfo,String filename,Map<String, Object> errorEntity) {
//		errorinfo=errorinfo.replace("'","");
//		String line1="line"; 
//		String lineed="of"; 
//		String error="The element type "; 
//		String errored="must be terminated"; 
//		int i=0;
//		int j=0;
//		int k=0;
//		int n=0;
//		int u=0;
//		int d=0;
//		while(true){
//		 i = errorinfo.indexOf(line1, i)+1;
//		 j = errorinfo.indexOf(lineed, j)+1;
//		 u = errorinfo.indexOf(error, u)+1;
//		 d = errorinfo.indexOf(errored, d)+1;
//		 if(i<1)
//		 break;
//		 System.out.println(line1+"出现的位置是:\t"+i+"\t");
//		 System.out.println(lineed+"出现的位置是:\t"+j+"\t");
//		 System.out.println(error+"出现的位置是:\t"+u+"\t");
//		 System.out.println(errored+"出现的位置是:\t"+d+"\t");
//		 String ceshi2=errorinfo.substring(i,j);
//		 String ceshi3=errorinfo.substring(u,d);
//		 ceshi2=ceshi2.substring(4,5);
//		 ceshi3=ceshi3.substring(7,11);
//		 System.out.println(ceshi2);
//		 System.out.println(ceshi3);
//		 errorEntity.put("conderinfo", filename+"第"+ceshi2+"列"+"元素"+ceshi3+"出现错误");
//		}
//	}
	
	public static Document getDocumentByString(String textXml) {
		Document newdoc = null;
		try {
			newdoc = DocumentHelper.parseText(textXml);
		} catch (Exception e) {
			log.error("XMLUtil getDocumentByString error!", e);
		}
		return newdoc;
	}
	
	public static Document getDocumentByFile(String filepath) {
		Document newDoc = null;
		try {
			SAXReader reader = new SAXReader();
			newDoc = reader.read(new File(filepath));
		} catch (Exception e) {
			log.error("XMLUtil getDocumentByString error!", e);
		}
		return newDoc;
	}
	
	public static Document getDocumentByInputStream(InputStream ins) {
		Document newDoc = null;
		try {
			SAXReader reader = new SAXReader();
			newDoc = reader.read(ins);
		} catch (Exception e) {
			log.error("XMLUtil getDocumentByInputStream error!", e);
		}
		return newDoc;
	}

	public static Document createFuncXml() {
		String xmlTemp = ""
				+ "<Root>"
				+ "<NAME></NAME>"
				+ "<ID></ID>"
				+ "<GLOBALID></GLOBALID>"
				+ "<RISKTYPE></RISKTYPE>"
				+ "<DESC></DESC>"
				+ "<FRAGMENT></FRAGMENT>"
				+ "<CHECKFIELD></CHECKFIELD>"
				+ "<ORDERBY></ORDERBY>"
				+ "<Params>"
//				+ " <ParmGroupInfo>"
//				+ "  <PGID></PGID>"
//				+ "  <GLOBALID></GLOBALID>"
//				+ "  <GROUPNAME></GROUPNAME>"
//				+ "  <RWCONTROL></RWCONTROL>"
//				+ "  <ORDERBY></ORDERBY>"
//				+ "  <Params>"
//				+ "   <Param>"
//				+ "    <PID>Id</PID>"
//				+ "    <GLOBALID></GLOBALID>"
//				+ "    <PARAMNAME></PARAMNAME>"
//				+ "    <PARAMLABEL></PARAMLABEL>"
//				+ "    <DEFAULTVALUE></DEFAULTVALUE>"
//				+ "    <SOURCEID></SOURCEID>"
//				+ "    <RWCONTROL></RWCONTROL>"
//				+ "    <ORDERBY></ORDERBY>"
//				+ "    <SOURCETYPE></SOURCETYPE>"
//				+ "    <PARAMDESC></PARAMDESC>"
//				+ "   </Param>"
//				+ "  </Params>"
//				+ " </ParmGroupInfo>"
//				+ " <Param>"
//				+ "    <PID></PID>"
//				+ "    <GLOBALID></GLOBALID>"
//				+ "    <PARAMNAME></PARAMNAME>"
//				+ "    <PARAMLABEL></PARAMLABEL>"
//				+ "    <DEFAULTVALUE></DEFAULTVALUE>"
//				+ "    <SOURCEID></SOURCEID>"
//				+ "    <RWCONTROL></RWCONTROL>"
//				+ "    <ORDERBY></ORDERBY>"
//				+ "    <SOURCETYPE></SOURCETYPE>"
//				+ "    <PARAMDESC></PARAMDESC>"
//				+ "</Param>"
				+ "</Params>"
				+ "</Root>";
		
		return getDocumentByString(xmlTemp);
	}


}