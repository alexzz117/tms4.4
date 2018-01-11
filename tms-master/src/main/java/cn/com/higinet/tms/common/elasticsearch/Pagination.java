package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public class Pagination<T> {
	
private int pageSize; // 每页显示多少条记录
    
    private int pageNo; //当前第几页数据
    
    private int totalRecord; // 一共多少条记录
    
    private int totalPage; // 一共多少页记录
    
    private List<T> dataList; //要显示的数据
    
    public Pagination(){
        
    }

    public Pagination(int pageSize, int currentPage, int totalRecord, int totalPage,
            List<T> dataList) {
        super();
        this.pageSize = pageSize;
        this.pageNo = currentPage;
        this.totalRecord = totalRecord;
        this.totalPage = totalPage;
        this.dataList = dataList;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalPage = this.totalRecord / this.pageSize;
        if(this.totalRecord % this.pageSize !=0){
            this.totalPage = this.totalPage + 1;
        }
        this.totalRecord = totalRecord;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

}
