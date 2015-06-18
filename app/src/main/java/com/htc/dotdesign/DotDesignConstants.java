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

public class DotDesignConstants {
	public final static String LOG_TAG = "DotDrop";
	public final static String shareName = "share.png";
	public final static String shareFolderName = "dotview_images";
	public final static int REQUEST_GET_PHOTO_FROM_GALLERY = 100;
	public final static int REQUEST_GET_PHOTO_FROM_CAMERA = 101;
	public final static int REQUEST_START_DRAW_VIEW = 102;
	public final static int REQUEST_START_DELETE_PAGE = 103;
    public final static int REQUEST_CROP_IMG_IN_GALLERY_FROM_CAMERA = 104;
    public final static int REQUEST_CROP_IMG_IN_GALLERY = 105;
    public final static int REQUEST_GET_TEMPLATE = 106;
    
	public final static String EXTRA_FILE_NAME = "filename";
	public final static String EXTRA_DOTDESIGN_TEMPLATE_ID = "templateid";
	public final static String EXTRA_IMAGE_URI = "uri";
	public final static String EXTRA_IMAGE_PATH = "image_path";
	public final static String EXTRA_CURRENT_MODE = "mode";
	public final static String EXTRA_BITMAP_ARRAY = "bitmap_array";
	public final static String EXTRA_IS_LAUNCH_FROM_MAINPAGE = "launch_from_mainpage";
	
	public final static String TEMP_FILE_NAME = "dotdroptemp.png";
	public final static String THUMBNAIL_FILE_NAME = "thumb.png";
	public final static String ORIGINAL_FILE_NAME = "original.png";
	public final static String COLOR_FILE_NAME = "color";
	public final static String NOTIFY_DOTVIEW_TO_UPDATE_WALLPAPER = "com.htc.litebrite.actions.THEME_CHANGE_UPDATE";
	public final static int DEFAULT_PHOTO_WIDTH = 1080;
	public final static int DEFAULT_PHOTO_HEIGHT = 1920;
	public final static int THUMBNAIL_PHOTO_WIDTH = 354;
	public final static int THUMBNAIL_PHOTO_HEIGHT = 634;
	public final static int SHARE_PHOTO_WIDTH = 540;
    public final static int SHARE_PHOTO_HEIGHT = 960;
	public final static int GET_PHOTO_FROM_GALLERY = 1000;
	public final static int SAVE_IMAGE = 1001;
	public final static int CROP_USERDEFINE = 32513;
	
	public enum DrawingMode {
	    MODE_NONE,MODE_DOTDESIGNTEMPLATE,MODE_FREESKETCH,MODE_GALLERY,MODE_CAMERA,MODE_GRIDVIEW
    }
}
