package com.isad.carbtracker;

/**
 * Private Class for storing categories of foods eaten.
 * @author weaver
 *
 */
public class FoodGroups {
	private float mGreen;
	private float mYellow;
	private float mRed;
	
	FoodGroups() {
		mGreen = 0;
		mYellow = 0;
		mRed = 0;
	}
	
	public void setGreen(float value) {
		this.mGreen = value;
	}
	
	public void setYellow(float value) {
		this.mYellow = value;
	}
	
	public void setRed(float value) {
		this.mRed = value;
	}
	
	public float getGreen() {
		return mGreen;
	}
	
	public float getYellow() {
		return mYellow;
	}
	
	public float getRed() {
		return mRed;
	}
}
