package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

import cn.com.higinet.tms.base.util.Stringz;

public interface EsListener<T> {

	String listenerId = Stringz.randomUUID();
	StringBuffer logBuffer = new StringBuffer();

	/**
	 * 提交前调用
	 * @allList 全部数据
	 * */
	void before( Long executionId, List<T> allList );

	/**
	 * 只有在有提交成功的数据时调用
	 * @param successList 只是成功的那一部分数据
	 * */
	void onSuccess( Long executionId, List<T> successList );

	/**
	 * 只有在有提交失败的数据时调用
	 * @param errorList 只是失败的那一部分数据
	 * */
	void onError( Long executionId, List<T> errorList );

	/**
	 * 提交完毕后调用
	 * 无论成功失败
	 * @allList 全部数据
	 * */
	void after( Long executionId, List<T> allList );
}
