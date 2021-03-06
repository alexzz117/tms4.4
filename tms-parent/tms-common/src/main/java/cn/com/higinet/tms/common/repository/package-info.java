/**
 * 1、一种结构化数据
 * 2、能够根据每个客户的不同，定义不同的属性，属性有具体的类型（限制java原生提供的类型，不能自定义类型）
 * 3、具有序列化接口，这类数据可以进行序列化和反序列化，能够存储在集中缓存中
 * 4、具有持久化接口，这类数据可以被持久化
 * 5、需要更新接口，数据更新需要一定灵活性
 * 6、这类结构化数据，每一个row都一定有一个能唯一标识此row的ID（key），会作为持久化的ID或者是缓存的ID使用
 * 7、持久化的时候，每个字段的类型都为字符串类型，不做复杂类型转换，转换只在使用的时候转换
 * 
 * 本包只提供基本的处理流程和接口，并不会提供具体实现，例如初始化器、部分个性化转换器、持久化器。
 * 可以提供默认的序列化器、基于YAML文件的初始化器。
 */
package cn.com.higinet.tms.common.repository;