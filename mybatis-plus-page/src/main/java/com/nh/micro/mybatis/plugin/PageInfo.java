package com.nh.micro.mybatis.plugin;



import org.apache.ibatis.session.RowBounds;

/**
 * 
 * @author ninghao
 *
 */
public class PageInfo extends RowBounds {
	public PageInfo(int pageNo, int pageRows) {
		super(pageNo, pageRows);
	}

	public PageInfo(int offset, int limit, String orderStr) {
		super(offset, limit);
		this.orderStr=orderStr;
	}
	private Long total = 0l;

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	private String orderStr = "";

	public String getOrderStr() {
		return orderStr;
	}

	public void setOrderStr(String orderStr) {
		this.orderStr = orderStr;
	}

}
