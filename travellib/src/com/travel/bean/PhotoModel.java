package com.travel.bean;

import java.io.Serializable;

public class PhotoModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private String originalPath;
	private String originalPathBig;
	private boolean isChecked;

	public PhotoModel(String originalPath) {
		this.originalPath = originalPath;
	}

	public PhotoModel() {
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}

	public String getOriginalPathBig() {
		return originalPathBig;
	}

	public void setOriginalPathBig(String originalPathBig) {
		this.originalPathBig = originalPathBig;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}