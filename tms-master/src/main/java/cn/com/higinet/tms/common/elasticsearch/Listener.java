package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public interface Listener<T> {

	void onSuccess( List<T> idList );

	void onError( List<T> idList );
}
