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

import java.io.File;
import java.util.Locale;

import com.htc.dotdesign.R;
import com.htc.dotdesign.DotDesignConstants.DrawingMode;
import com.htc.dotdesign.util.DotDesignUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DrawingActivity extends Activity {
    private static final String LOG_PREFIX = "[DrawingActivity] ";
    private DrawingView drawingView = null;
    private ImageView background = null;
    private ImageView mDotMask = null;
    private ImageView mFakeImg = null;
    private Bitmap mDrawingBitmap = null;
    private DrawingMode mCurrentMode = DrawingMode.MODE_DOTDESIGNTEMPLATE;
    private Uri mImageUri = null;
    private RelativeLayout root = null;
    
    private ProgressDialog mDialog = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onCreate");
        super.onCreate(savedInstanceState);
        bindToolBoxService();
        setContentView(R.layout.drawing_layout);
        root = (RelativeLayout)findViewById(R.id.root);
        background = (ImageView)findViewById(R.id.background);
        mDotMask = (ImageView)findViewById(R.id.dot_mask);
        mFakeImg = (ImageView)findViewById(R.id.fakeimg);
        drawingView = (DrawingView)findViewById(R.id.drawing);
        drawingView.initparams(this);
        getIntentData();
        /*ImageButton mainButton = (ImageButton) findViewById(R.id.main_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                TextView text = (TextView) findViewById(R.id.main_text);
                text.setText("Button!!");
            }
        });*/
        
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        
        getWindow().getDecorView().setSystemUiVisibility(flags);

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView
            .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
            {

                @Override
                public void onSystemUiVisibilityChange(int visibility)
                {
                    if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
    }
    
    @Override
    protected void onResume() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onResume");
        super.onResume();
        if(mToolBoxService != null && !mToolBoxService.isToolBarShow()) {
            mToolBoxService.showHideToolbar(true);
        }
    }
    
    @Override
    protected  void onPause() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onPause");
        super.onPause();
        if(mToolBoxService != null && mToolBoxService.isToolBarShow()) {
            mToolBoxService.showHideToolbar(false);
        }
    }
    
    @Override
    public void onDestroy() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onDestroy");
        super.onDestroy();
        unbindToolBoxService();
        dismissLoadingDlg();
        if(drawingView != null) {
        	drawingView.reset();
        }
        if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
			mDrawingBitmap.recycle();
			mDrawingBitmap = null;
		}
    }
    
    @Override
    public void onBackPressed() {
        if (drawingView != null) {
            drawingView.exitDialog();
        }
    }

    public Bitmap takeScreenshot() {
    	Bitmap bitmap = null;
    	if(root != null) {
    		root.buildDrawingCache();
            bitmap = root.getDrawingCache();
    	}
    	return bitmap;
    }
    
    public void setFakeScreen(Bitmap bitmap) {
    	if(mFakeImg != null) {
    		mFakeImg.setImageBitmap(bitmap);
    		mFakeImg.setVisibility(View.VISIBLE);
    	}
    }
	
	public void setDotMask() {
		if (mDotMask != null) {
            if (DotDesignUtil.isFWVGA(this)) {
                mDotMask.setBackgroundResource(R.drawable.mask_bitmap_repeat_alpha_fwvga);
            } else {
                mDotMask.setBackgroundResource(R.drawable.mask_bitmap_repeat_alpha);
            }
            mDotMask.setVisibility(View.VISIBLE);
        }
	}
	
	public void hideDotMask() {
		if (mDotMask != null) {
            mDotMask.setVisibility(View.GONE);
        }
	}
	
    private void getIntentData() {
        Intent intent = getIntent();
        mCurrentMode = (DrawingMode) intent
                .getSerializableExtra(DotDesignConstants.EXTRA_CURRENT_MODE);
        if (mCurrentMode == DrawingMode.MODE_DOTDESIGNTEMPLATE) {
            int templateid = intent.getIntExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,0);
            String imagefilename = DotDesignUtil.getFileName();
            mDrawingBitmap = DotDesignUtil.readImgfromResource(this, templateid);
            updateDrawingViewUI(mDrawingBitmap, null, imagefilename, mCurrentMode, false);
        } else if (mCurrentMode == DrawingMode.MODE_FREESKETCH) {
            setFreeSketch();
        } else if (mCurrentMode == DrawingMode.MODE_GALLERY
                || mCurrentMode == DrawingMode.MODE_CAMERA) {
            if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
                mDrawingBitmap.recycle();
                mDrawingBitmap = null;
            }
            String imagefilename = DotDesignUtil.getFileName();
            String filepath = intent.getStringExtra(DotDesignConstants.EXTRA_IMAGE_PATH);
            mDrawingBitmap = DotDesignUtil.ReadImgfromInternal(filepath);
            updateDrawingViewUI(mDrawingBitmap, null, imagefilename, mCurrentMode, false);
        } else if (mCurrentMode == DrawingMode.MODE_GRIDVIEW) {
            if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
                mDrawingBitmap.recycle();
                mDrawingBitmap = null;
            }
            String filename = intent.getStringExtra(DotDesignConstants.EXTRA_FILE_NAME);
            String originalImgPath = filename + "original.png";
            boolean isFreeSketch = false;
            File orignfile = getFilesDir();
            String dirPath = String.format(Locale.US, "%s%s%s", orignfile.getAbsolutePath(),
                    File.separator, originalImgPath);
            File file = new File(dirPath);
            if (file.exists()) {
                isFreeSketch = false;
                mDrawingBitmap = DotDesignUtil.ReadImgfromInternal(dirPath);
            }
            else {
                // If original.png is not exist, it is freesketch.
                isFreeSketch = true;
            }
            String coloarrayfilename = filename + "color";
            ColorArray colorarray = DotDesignUtil.getColorArray(coloarrayfilename,
                    DrawingActivity.this);
            updateDrawingViewUI(mDrawingBitmap, colorarray, filename, mCurrentMode, isFreeSketch);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    
    private void updateDrawingViewUI(Bitmap bitmap,ColorArray colorarray, String filename, DrawingMode mode,
            boolean isfreesketch) {
        if (isfreesketch) {
            drawingView.updateTemplateImage(bitmap, filename, DrawingMode.MODE_FREESKETCH);
            background.setBackgroundColor(android.graphics.Color.BLACK);
            background.setAlpha(1.0f);
        }
        else {
            drawingView.updateTemplateImage(bitmap, filename, mode);
            if (background != null) {
                background.setImageBitmap(bitmap);
                background.setAlpha(0.3f);
            }
        }
        if (colorarray != null) {
            drawingView.updateColorArray(colorarray.getArray());
        }
    }
    
    protected ToolBoxService mToolBoxService = null;
    protected ToolBoxServiceConn mToolBoxServiceConn = null;
    
    private class ToolBoxServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onServiceConnected(), packageName = " + className.getPackageName() + ", className = " + className.getClassName());
            // Getting the binder and activating ToolBoxler instantly
            ToolBoxService.ToolBoxBinder binder = (ToolBoxService.ToolBoxBinder) service;
            mToolBoxService = binder.getService();
            if (mToolBoxService != null) {
            } else {
                Log.w(DotDesignConstants.LOG_TAG, LOG_PREFIX + "binder.getService return null!!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onServiceDisconnected");
            mToolBoxService = null;
        }
    };
    
    private void bindToolBoxService() {
        if (mToolBoxServiceConn == null) {
            mToolBoxServiceConn = new ToolBoxServiceConn();
            Intent intent = new Intent("com.htc.dotdesign.bind_tool_box_service");
            intent.setClassName("com.htc.dotdesign", "com.htc.dotdesign.ToolBoxService");
            Log.v(DotDesignConstants.LOG_TAG, LOG_PREFIX + "BINDING ToolBoxService");
            if (!bindService(intent, mToolBoxServiceConn, Context.BIND_AUTO_CREATE)) {
                Log.v(DotDesignConstants.LOG_TAG, LOG_PREFIX + "FAILED TO BIND TO ToolBoxService!");
            }
        } else {
            Log.v(DotDesignConstants.LOG_TAG, LOG_PREFIX + "Service already bound");
        }
    }
    
    private void unbindToolBoxService() {
        Log.v(DotDesignConstants.LOG_TAG, LOG_PREFIX + "UNBINDING ToolBoxService");
        this.unbindService(mToolBoxServiceConn);
        mToolBoxServiceConn = null;
    }
    
    private void showLoadingDlg() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new ProgressDialog(DrawingActivity.this, ProgressDialog.THEME_HOLO_DARK);
        mDialog.setMessage(getResources().getString(R.string.loading));
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    private void dismissLoadingDlg() {
    	if(mDialog != null) {
    		mDialog.dismiss();
    		mDialog = null;
		}	
    }
    
    public boolean isToolBarShow() {
        if (mToolBoxService != null) {
            return mToolBoxService.isToolBarShow();
        }
        return false;
    }
    
    public boolean isToolBarOpen() {
        if (mToolBoxService != null) {
            return mToolBoxService.isToolBarOpen();
        }
        return false; 
    }
    
    public void setFreeSketch() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "setFreeSketch");
        String filename = DotDesignUtil.getFileName();
        if (background != null) {
            background.setImageBitmap(null);
            background.setBackgroundColor(android.graphics.Color.BLACK);
            background.setAlpha(1.0f);
        }
        if (drawingView != null) {
            drawingView.updateTemplateImage(null, filename, DrawingMode.MODE_FREESKETCH);
        }
    }
    
    public void selectTemplate() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "selectTemplate");
        Intent startintent = new Intent(DrawingActivity.this,
                DotDesignTemplate.class);
        startActivityForResult(startintent,DotDesignConstants.REQUEST_GET_TEMPLATE);
    }

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onActivityResult:" + requestCode);
        switch (requestCode) {
            case DotDesignConstants.REQUEST_GET_PHOTO_FROM_GALLERY:
                DotDesignUtil.getCropPhotoByGallery(this, data);
                break;
            case DotDesignConstants.REQUEST_GET_PHOTO_FROM_CAMERA: {
                DotDesignUtil.getCropPhotoByCamera(this, data);
                break;
            }
            case DotDesignConstants.REQUEST_CROP_IMG_IN_GALLERY_FROM_CAMERA: {
                if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
                    mDrawingBitmap.recycle();
                    mDrawingBitmap = null;
                }
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                File[] fArray = f.listFiles();
                if (f.isDirectory() && fArray != null) {
                    for (File temp : fArray) {
                        if (temp != null && temp.getName().equals(DotDesignConstants.TEMP_FILE_NAME)) {
                            f = temp;
                            break;
                        }
                    }
                    mDrawingBitmap = DotDesignUtil.ReadImgfromInternal(f.getAbsolutePath());
                    background.setImageBitmap(mDrawingBitmap);
                    background.setAlpha(0.3f);
                    String filename = DotDesignUtil.getFileName();
                    drawingView.updateTemplateImage(mDrawingBitmap, filename, DrawingMode.MODE_CAMERA);
                }
                break;
            }
            case DotDesignConstants.REQUEST_CROP_IMG_IN_GALLERY: {
                if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
                    mDrawingBitmap.recycle();
                    mDrawingBitmap = null;
                }
                if (data == null)
                    return;
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                File[] fArray = f.listFiles();
                if (f.isDirectory() && fArray != null) {
                    for (File temp : fArray) {
                        if (temp != null && temp.getName().equals(DotDesignConstants.TEMP_FILE_NAME)) {
                            f = temp;
                            break;
                        }
                    }
                    mDrawingBitmap = DotDesignUtil.ReadImgfromInternal(f.getAbsolutePath());
                    background.setImageBitmap(mDrawingBitmap);
                    background.setAlpha(0.3f);
                    String filename = DotDesignUtil.getFileName();
                    drawingView.updateTemplateImage(mDrawingBitmap, filename,
                            DrawingMode.MODE_GALLERY);
                }
                break;
            }
            case DotDesignConstants.REQUEST_GET_TEMPLATE: {
                if (mDrawingBitmap != null && !mDrawingBitmap.isRecycled()) {
                    mDrawingBitmap.recycle();
                    mDrawingBitmap = null;
                }
                if (data != null) {
                    int templateid = data
                            .getIntExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID, 0);
                    if (templateid != 0) {
                        String imagefilename = DotDesignUtil.getFileName();
                        mDrawingBitmap = DotDesignUtil.readImgfromResource(this, templateid);
                        updateDrawingViewUI(mDrawingBitmap, null, imagefilename,
                                DrawingMode.MODE_DOTDESIGNTEMPLATE, false);
                    }
                }
                break;
            }
        }
    }
    
    
}
