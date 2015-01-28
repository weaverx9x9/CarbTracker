package com.isad.carbtracker;

import java.util.Date;

public class WeightPoint implements Comparable<WeightPoint> {
	private Date mDate;
	private Double mWeight;
	
	public WeightPoint(Date date, Double weight) {
		this.mDate = date;
		this.mWeight = weight;
	}
	
	public Date getDate() {
		return mDate;
	}
	
	public Double getWeight() {
		return mWeight;
	}
	
	@Override
	public int compareTo(WeightPoint another) {
		return mWeight.compareTo(another.getWeight());
	}
}
