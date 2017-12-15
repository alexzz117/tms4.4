package cn.com.higinet.tms35.core.cache;

import cn.com.higinet.tms35.core.dao.row_in_db_impl;

public class db_sendfile_log extends row_in_db_impl {

    public String logid;//日志ID
    public String srcaddr;//来源地址
    public String tagaddr;//目标地址
    public String srcfilename;//来源文件名
    public String tagfilename;//目标文件名
    public String status;//状态
    public long starttime;//发送开始时间
    public long endtime;//发送结束时间
    public long finishtime;//处理完成时间
}
