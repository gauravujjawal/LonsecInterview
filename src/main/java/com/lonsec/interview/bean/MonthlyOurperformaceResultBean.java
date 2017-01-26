package com.lonsec.interview.bean;

import java.util.Date;

public class MonthlyOurperformaceResultBean implements Comparable<MonthlyOurperformaceResultBean> {

	public String fundName;
	public Date date;
	public double excess;
	public String outPerform;
	public double returnPercentage;

	public String getFundName() {
		return fundName;
	}

	public void setFundName(String fundName) {
		this.fundName = fundName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getExcess() {
		return excess;
	}

	public void setExcess(double excess) {
		this.excess = excess;
	}

	public String getOutPerform() {
		return outPerform;
	}

	public void setOutPerform(String outPerform) {
		this.outPerform = outPerform;
	}

	public double getReturnPercentage() {
		return returnPercentage;
	}

	public void setReturnPercentage(double returnPercentage) {
		this.returnPercentage = returnPercentage;
	}

	@Override
	public int compareTo(MonthlyOurperformaceResultBean compBean) {
		try {
			double retVal = compBean.getReturnPercentage();
			/* For Ascending order */
			return this.returnPercentage < retVal ? -1 : this.returnPercentage > retVal ? 1 : 0;
		} catch (Exception e) {
			return -1;
		}
		/* For Descending order do like this */
		// return beanExcess-this.excess;
	}
}
