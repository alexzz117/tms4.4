package cn.com.higinet.tms.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FastjsonConfig extends WebMvcConfigurerAdapter {
	private static final Logger log = LoggerFactory.getLogger( FastjsonConfig.class );
	
	/*@Bean
	public HttpMessageConverters fastJsonHttpMessageConverters() {
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures( SerializerFeature.PrettyFormat );
		fastConverter.setFastJsonConfig( fastJsonConfig );
		HttpMessageConverter<?> converter = fastConverter;
		return new HttpMessageConverters( converter );
	}*/

	@Override
	public void configureMessageConverters( List<HttpMessageConverter<?>> converters ) {
		super.configureMessageConverters( converters );
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures( SerializerFeature.PrettyFormat );
		fastConverter.setFastJsonConfig( fastJsonConfig );
		converters.add( fastConverter );
		log.info( "Fastjson : "+ JSON.toJSONString( fastConverter ) ); 
	}
}
