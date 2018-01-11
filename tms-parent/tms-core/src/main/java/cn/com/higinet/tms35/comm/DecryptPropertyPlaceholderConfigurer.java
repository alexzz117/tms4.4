package cn.com.higinet.tms35.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * 解密Properties配置文件中的加密项
 * 使用说明：替换原spring配置文件中加载properties文件的配置内容，如：
 * 	<context:property-placeholder location="classpath:server.properties" />
 * 替换为
 * <!-- 加载配置文件,并对其中的加密项进行解密 -->
	<bean id="decryptPropertyPlaceholderConfigurer" class="cn.com.higinet.tms35.comm.DecryptPropertyPlaceholderConfigurer">
		<property name="locations">
			<list>
				<value>classpath:server.properties</value>
			</list>
		</property>
		<!-- 需要解密的参数项 -->
		<property name="decrypts">
			<list>
				<value>tms.jdbc.password,tmp.jdbc.password</value>
			</list>
		</property>
		<!-- 密钥文件 -->
		<property name="keyLocation" value="classpath:tmskey.key" />
		<property name="fileEncoding" value="UTF-8" />
	</bean>
 * @author lining
 */
public class DecryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	/**
	 * 需要解密的配置文件集合
	 */
	private Resource[] locations;
	/**
	 * 每个配置文件中需要解密的参数名, 逗号隔开
	 */
	private String[] decrypts;
	/**
	 * 密钥文件
	 */
	private Resource keyLocation;
	/**
	 * 文件编码
	 */
	private String fileEncoding;

	public Resource[] getLocations() {
		return locations;
	}

	public void setLocations(Resource[] locations) {
		this.locations = locations;
	}

	public String[] getDecrypts() {
		return decrypts;
	}

	public void setDecrypts(String[] decrypts) {
		this.decrypts = decrypts;
	}

	public Resource getKeyLocation() {
		return keyLocation;
	}

	public void setKeyLocation(Resource keyLocation) {
		this.keyLocation = keyLocation;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	@Override
	protected void loadProperties(Properties props) throws IOException {
		if (this.locations != null) {
			PropertiesPersister persister = new DefaultPropertiesPersister();
			Key key = null;
			if (keyLocation == null) {
				// 密钥文件为空, 获取默认密钥
				key = DESEncryptUtil.getDefaultKey();
			} else {
				// 密钥文件不为空, 加载密钥
				key = DESEncryptUtil.getKey(keyLocation.getInputStream());
			}
			for (int i = 0, len = this.locations.length; i < len; i++) {
				Resource location = this.locations[i];
				String decProp = (this.decrypts != null && this.decrypts.length > i ? this.decrypts[i] : null);
				String[] decProps = (decProp == null || decProp.trim().length() == 0 ? null : decProp.split("\\,"));
				InputStream is = null;
				try {
					is = location.getInputStream();
					// 将配置文件的内容加载到props中,begin
					if (location.getFilename().endsWith(".xml")) {
						// XML文件
						persister.loadFromXml(props, is);
					} else {
						// 属性文件
						if (fileEncoding == null) {
							persister.load(props, new InputStreamReader(is, this.fileEncoding));
						} else {
							persister.load(props, is);
						}
					}
					// 将配置文件的内容加载到props中,end
					// 判断是否有需要解密的数据项
					if (decProps != null && decProps.length > 0) {
						for (int j = 0, jlen = decProps.length; j < jlen; j++) {
							String k = decProps[i];
							String v = props.getProperty(k);
							if (v != null && v.trim().length() > 0) {
								// 对参数值进行解密
								v = DESEncryptUtil.doDecrypt(key, v);
								props.put(k, v);
							}
						}
					}
				} finally {
					if (is != null) {
						is.close();
					}
				}
			}
		}
	}
}