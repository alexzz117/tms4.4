package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public interface Listener<T> {

	void before( List<T> allList );

	void onSuccess( List<T> successList );

	void onError( List<T> errorList );

	void after( List<T> allList );
}
