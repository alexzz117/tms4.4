package cn.com.higinet.tms.base.constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Text {
	String value() default "";
}