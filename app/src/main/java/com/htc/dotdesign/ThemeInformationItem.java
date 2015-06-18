/*
 * Copyright (C) 2015 HTC Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htc.dotdesign;


import android.content.Intent;
import android.graphics.Bitmap;

class ThemeInformationItem {
    private Bitmap mThumbnailBmp = null;
    private Bitmap mOriginalBmp = null;
    private String mFilename = null;
    private Intent mIntent;
    private ColorArray array;
    private boolean mIsDelete = false;

    public ThemeInformationItem() {
    	
    }
    
    public void setThumbnailBmp(Bitmap bitmap) {
    	this.mThumbnailBmp = bitmap;
    }
    
    public void setOriginalBmp(Bitmap bitmap) {
    	this.mOriginalBmp = bitmap;
    }
    
    public void setColorArray(ColorArray array) {
    	this.array = array;
    }
    
    public void setLauchIntent(Intent intent) {
    	this.mIntent = intent;
    }
    
    public void setFileName(String name) {
    	this.mFilename = name;
    }
    
    public void setDelete(boolean delete) {
    	this.mIsDelete = delete;
    }
    
    public Bitmap getThumbnailBmp() {
    	return this.mThumbnailBmp;
    }
    
    public Bitmap getOriginalBmp() {
    	return this.mOriginalBmp;
    }
    
    public ColorArray getColorArray() {
    	return this.array;
    }
    
    public Intent getLaunchIntent() {
    	return this.mIntent;
    }
    
    public String getFileName() {
    	return this.mFilename;
    }
    
    public boolean IsDelete() {
    	return this.mIsDelete;
    }
}
