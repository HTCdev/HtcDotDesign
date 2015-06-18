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

import com.htc.dotdesign.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public abstract class DotImage extends RelativeLayout {
    private static final String LOG_PREFIX = "[DotImage] ";
    private static final String LOG_TAG = "DotMatrix";

    private static boolean sIsInit = false;
    protected static float sDotPixelHeight;
    protected static float sDotPixelWidth;
    protected static int sInnerFrameWidth;
    protected static int sInnerFrameHeight;
    protected static Paint sPaint = new Paint();

    public static int sBackgroundColor;
    public static int sTextColor;
    public static int sIconColor;
    
    private Context mContext = null;
    protected int mRowSize;
    protected int mColSize;
    protected int[][] mImgDotMatrix = null;
    
    public DotImage(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DotImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DotImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    protected abstract void initImgDotMatrix();

    private void init() {
        String className = (getClass() != null ? getClass().getName() : "");
        Log.d(LOG_TAG, LOG_PREFIX + "init " + className);

        Resources res = getResources();
        if (res == null || mContext == null) {
            Log.d(LOG_TAG, LOG_PREFIX + "init, res or mContext is null!!");
            return;
        }

        if (!sIsInit) {
            sIsInit = true;
//			WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//			if(window != null) {
//				Display display = window.getDefaultDisplay();
//				Point size = new Point();
//				display.getRealSize(size);
//				sInnerFrameHeight = size.y;
//				sInnerFrameWidth = size.x;
//			}
            sDotPixelWidth = res.getDimensionPixelSize(R.dimen.dot_pixel_width);
            sDotPixelHeight = res.getDimensionPixelSize(R.dimen.dot_pixel_height);
            sInnerFrameWidth = res.getDimensionPixelSize(R.dimen.inner_frame_width);
            sInnerFrameHeight = res.getDimensionPixelSize(R.dimen.inner_frame_height);
           // sDotPixelWidth = (float)(sInnerFrameWidth / 27.0);
            //sDotPixelHeight = (float)(sInnerFrameHeight / 48.0);
            if (sPaint != null) {
                sPaint.setAntiAlias(true);
            }
            sBackgroundColor = Color.TRANSPARENT;
            sTextColor = Color.WHITE;
        }      
        setBackgroundColor(Color.TRANSPARENT);
    }
    
    protected void createImgDotMatrix(int imgPixelWidth, int imgPixelHeight) {
        //Log.d(CoverService.LOG_TAG, LOG_PREFIX + "imgPixelWidth = " + imgPixelWidth + ", imgPixelHeight = " + imgPixelHeight);
        
        mColSize = (int)(imgPixelWidth / sDotPixelWidth);
        mRowSize = (int)(imgPixelHeight / sDotPixelHeight);
        
        mImgDotMatrix = new int[mRowSize][mColSize];
        
        setLayoutParams(new RelativeLayout.LayoutParams(imgPixelWidth, imgPixelHeight));
    }
    
    protected void setPaintColor(int color) {
        sPaint.setColor(color);         
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.d(CoverService.LOG_TAG, LOG_PREFIX + "onDraw");
        if (this.getVisibility() != VISIBLE) {
            return;
        }
        if (mImgDotMatrix == null || sPaint == null) {
            //Log.d(CoverService.LOG_TAG, LOG_PREFIX + "onDraw, mImgDotMatrix or mPaint is null!!");
            return;
        }
        float left = 0;
        float top = 0;
        float right = sDotPixelWidth;
        float bottom = sDotPixelHeight;

        for (int row = 0; row < mRowSize; ++row) {
            for (int col = 0; col < mColSize; ++col) {
                setPaintColor(mImgDotMatrix[row][col]);
                canvas.drawRect(left, top, right, bottom, sPaint);
                left += sDotPixelWidth;
                right += sDotPixelWidth;
            }
            left = 0;
            right = sDotPixelWidth;
            top += sDotPixelHeight;
            bottom += sDotPixelHeight;
        }
    }

    public void resetImgDotMatrixValue() {
        if (mImgDotMatrix != null) {
            for (int row = 0; row < mRowSize; ++row) {
                for (int col = 0; col < mColSize; ++col) {
                    mImgDotMatrix[row][col] = sBackgroundColor;
                }
            }
        }
    }
}
