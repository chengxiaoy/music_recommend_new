package org.chengy.crawler.model;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class BaseModel {

	@Id
	private String id;

	private String community;

	private Date createDate;

	private Date updateDate;

	public BaseModel() {
		createDate = new Date();
		updateDate = new Date(createDate.getTime());
	}

	public String getId() {
		return id;
	}


	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
