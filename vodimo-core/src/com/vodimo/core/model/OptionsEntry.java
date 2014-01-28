package com.vodimo.core.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("OptionsEntry")
public class OptionsEntry {
		 
    @Id
    private ObjectId id;
	
	private Date quoteDate;
	
	private Date expirationDate;
	
	private String entityId;
	
	@Embedded(concreteClass = java.util.ArrayList.class)
	private List<Option> optionList;
	
	public Date getQuoteDate() {
		return quoteDate;
	}

	public void setQuoteDate(Date quoteDate) {
		this.quoteDate = quoteDate;
	}
	
	public List<Option> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<Option> optionList) {
		this.optionList = optionList;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	@Override
	public int hashCode() {
		return (getEntityId() + getExpirationDate() + getQuoteDate()).hashCode();
	}
	
	@Override
	public String toString() {
		return "OptionsEntry:{entityId:" + entityId + ", " 
				+ "quoteDate:" + quoteDate + ", " 
				+ "expirationDate:" + expirationDate 
				+ "_id:" + id;
	}
}
