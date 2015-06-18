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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class ToolBoxService extends Service {
    private static final String LOG_PREFIX = "[ToolBoxService] ";
    
    static public final String TOOL_BAR_ACTION_STATUS = "tool_bar_action_status";
    static public final String EXTRA_CURR_FUNC = "curr_func"; // put mCurrFun into Serializable extra
    static public final String EXTRA_CURR_COLOR = "curr_color"; // int, color code
    static public final String EXTRA_CURR_BRUSH_SIZE = "curr_brush_size"; // put mCurrBrushSize into Serializable extra
    static public final String EXTRA_MENNU_ITEM = "menu_item"; // String, "clear_all", "insert_images", "set_theme", "share", "exit"
    
    static public final String SHOW_HIDE_TOOL_BAR = "show_hide_tool_bar";
    static public final String EXTRA_SHOW_HIDE = "show_hide"; // boolean, true to show, false to hide tool bar
    
    static public final String USER_START_DRAWING = "user_starting_drawing";
    
    static public final String MENU_ITEM_CLEAR_ALL_STR = "clear_all";
    static public final String MENU_ITEM_INSERT_IMAGES_STR = "insert_images";
    static public final String MENU_ITEM_SET_THEME_STR = "set_theme";
    static public final String MENU_ITEM_SHARE_STR = "share";
    static public final String MENU_ITEM_EXIT_STR = "exit";
    static public final int MENU_ITEM_CLEAR_ALL = 0;
    static public final int MENU_ITEM_INSERT_IMAGES = 1;
    static public final int MENU_ITEM_SET_THEME = 2;
    static public final int MENU_ITEM_SHARE = 3;
    static public final int MENU_ITEM_EXIT = 4;
    
    
    private WindowManager mWindowManager = null;
    private LocalBroadcastManager mLocalBroadcastManager = null;
    private View mToolBarParent = null;
    private View mToolBar = null;
    private View mPalette = null;
    private View mEraser = null;
    private View mMenu = null;
    private View mCurrExtend = null;
    //private View mBrushSize = null;
    //private View mBrushColor = null;
    
    private View mDragButton = null;
    private ImageView mCurrFuncIcon = null;
    private GradientDrawable mDragBtnColorIcon = null;
    private Drawable mDragBtnEraserIcon = null;
    private ImageButton mBtnPalette = null;
    private ImageButton mBtnEraser = null;
    private ImageButton mBtnVirtualDot = null;
    private ImageButton mBtnMenu = null;
    //private int mDragButtonWidth = 0;
    private int mDragButtonHeight = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mArrowWidth = 0;
    private boolean mbIsAnimationPlaying = false;
    private Animation mLeftIn = null;
    private Animation mLeftOut = null;
    private Animation mRightIn = null;
    private Animation mRightOut = null;
    private int mAnimationDuration = 400;
    private float mAnimationFactor = 4.0f;
    private WindowManager.LayoutParams mDragButtonParams = null;
    private WindowManager.LayoutParams mToolBarParams = null;
    private int mCurrBrushColor = 0;
    private ImageView mCurrBrushColorView = null;
    SparseIntArray mBtnColor = new SparseIntArray();
    
    private boolean mIsToolBarOpen = false;
    private boolean mIsToolBarExtend = false;
    private boolean mIsVirtualDotOpen = false;

    private FunType mCurrPaintFun = FunType.Fun_Palette;
    private FunType mCurrFun = FunType.Fun_Palette;
    public enum FunType {
        Fun_Palette,
        Fun_Eraser,
        Fun_VirtualDot,
        Fun_Menu
    }
    
    private BrushSize mCurrBrushSize = BrushSize.Size_1x1;
    public enum BrushSize {
        Size_1x1,
        Size_2x2
    }
    
    private LayoutMode mCurrLayoutMode = LayoutMode.LEFT_SIDE;
    public enum LayoutMode {
        LEFT_SIDE,
        RIGHT_SIDE
    }
    
    // Binder for our service.
    private IBinder mBinder = new ToolBoxBinder();
    
    // the most simple Binder implementation
    public class ToolBoxBinder extends Binder {
        public ToolBoxService getService() {
            return ToolBoxService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onBind, action: " + intent.getAction());
        if (intent.getAction().equals("com.htc.dotdesign.bind_tool_box_service")) {
            return mBinder;
        } else {
            return null;
        }
    }
    
    @Override
    public boolean onUnbind (Intent intent) {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onUnbind, action: " + intent.getAction());
        if (intent.getAction().equals("com.htc.dotdesign.bind_tool_box_service")) {
        }
        return false;
    }

    @Override
    public void onCreate() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onCreate");
        super.onCreate();
        
        Resources res = getResources();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        if (mLocalBroadcastManager != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(SHOW_HIDE_TOOL_BAR);
            filter.addAction(USER_START_DRAWING);
            mLocalBroadcastManager.registerReceiver(mToolBarReceiver, filter);
        }
        
        // Get full window size
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        
        // Get arrow width
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, R.drawable.dot_design_popupmenu_arrow, bitmapOptions);
        mArrowWidth = bitmapOptions.outWidth;
        
        // Initialize LayoutParams
        mDragButtonParams = new WindowManager.LayoutParams(
                res.getDimensionPixelSize(R.dimen.drag_button_width),
                res.getDimensionPixelSize(R.dimen.drag_button_height),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mDragButtonParams.gravity = Gravity.TOP | Gravity.START;
        mDragButtonParams.x = 0;
        mDragButtonParams.y = mScreenHeight / 2;
        
        mToolBarParams = new WindowManager.LayoutParams(
                res.getDimensionPixelSize(R.dimen.tool_bar_width),
                res.getDimensionPixelSize(R.dimen.tool_bar_height),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mToolBarParams.alpha = 0.9f;
        mToolBarParams.gravity = Gravity.TOP | Gravity.START;
        mToolBarParams.x = 0;
        mToolBarParams.y = mDragButtonParams.y;
        
        // Initialize drag button width and height
        //mDragButtonWidth = res.getDimensionPixelSize(R.dimen.drag_button_width);
        mDragButtonHeight = res.getDimensionPixelSize(R.dimen.drag_button_height);
        mDragBtnColorIcon = (GradientDrawable) res.getDrawable(R.drawable.round_button);
        mDragBtnEraserIcon = res.getDrawable(R.drawable.dot_design_circle_shape_dark);
        
        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDragButton = inflater.inflate(R.layout.drag_button, null);
        mToolBarParent = inflater.inflate(R.layout.tool_bar, null);
        mPalette = inflater.inflate(R.layout.palette, null);
        mEraser = inflater.inflate(R.layout.eraser, null);
        mMenu = inflater.inflate(R.layout.menu, null);

        initToolBar();
        initMenu();
        initAnimation();
        initBrushColor();
        initDragButton();

        mWindowManager.addView(mToolBarParent, mToolBarParams);
        mWindowManager.addView(mDragButton, mDragButtonParams);
        mToolBarParent.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onDestroy");
        super.onDestroy();
        if (mToolBarParent != null && mToolBarParent.isShown()) {
            mWindowManager.removeView(mToolBarParent);
        }
        if (mDragButton != null && mDragButton.isShown()) {
            mWindowManager.removeView(mDragButton);
        }
        if (mCurrExtend != null && mCurrExtend.isShown()) {
            mWindowManager.removeView(mCurrExtend);
        }
        
        if (mLocalBroadcastManager != null && mToolBarReceiver != null) {
            mLocalBroadcastManager.unregisterReceiver(mToolBarReceiver);
        }
    }
    
    private void setToolPanelVisibility(boolean bShow) {
        if (mCurrExtend != null) {
            mWindowManager.removeView(mCurrExtend);
            mCurrExtend = null;
        }
        
        if (bShow) {
            mIsToolBarExtend = true;
            if (mCurrFun == FunType.Fun_Palette) {
                Resources res = getResources();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.START;
                if (mDragButtonParams.y < mScreenHeight / 2) {
                    params.gravity = Gravity.TOP | params.gravity;
                    params.y = mDragButtonParams.y + mDragButtonHeight;
                    View top_arrow = mPalette.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.VISIBLE);
                    View bottom_arrow = mPalette.findViewById(R.id.bottom_arrow);
                    bottom_arrow.setVisibility(View.GONE);
                } else {
                    params.gravity = Gravity.BOTTOM | params.gravity;
                    params.y = mScreenHeight - mDragButtonParams.y;
                    View top_arrow = mPalette.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.GONE);
                    View bottom_arrow = mPalette.findViewById(R.id.bottom_arrow);
                    bottom_arrow.setVisibility(View.VISIBLE);
                }
                int[] locations = new int[2];
                mBtnPalette.getLocationOnScreen(locations);
                int x = locations[0];
                params.x = (x + mBtnPalette.getWidth()/2) - (mArrowWidth/2 + res.getDimensionPixelSize(R.dimen.h02));
                
                mPalette.setVisibility(View.VISIBLE);
                mWindowManager.addView(mPalette, params);
                mCurrExtend = mPalette;
                initBrushSize();
            } else if (mCurrFun == FunType.Fun_Eraser) {
                Resources res = getResources();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.START;
                if (mDragButtonParams.y < mScreenHeight / 2) {
                    params.gravity = Gravity.TOP | params.gravity;
                    params.y = mDragButtonParams.y + mDragButtonHeight;
                    View top_arrow = mEraser.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.VISIBLE);
                    View bottom_arrow = mEraser.findViewById(R.id.bottom_arrow);
                    bottom_arrow.setVisibility(View.GONE);
                } else {
                    params.gravity = Gravity.BOTTOM | params.gravity;
                    params.y = mScreenHeight - mDragButtonParams.y;
                    View top_arrow = mEraser.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.GONE);
                    View bottom_arrow = mEraser.findViewById(R.id.bottom_arrow);
                    bottom_arrow.setVisibility(View.VISIBLE);
                }
                int[] locations = new int[2];
                mBtnEraser.getLocationOnScreen(locations);
                int x = locations[0];
                params.x = (x + mBtnEraser.getWidth()/2) - (mArrowWidth/2 + res.getDimensionPixelSize(R.dimen.h02));
                
                mEraser.setVisibility(View.VISIBLE);
                mWindowManager.addView(mEraser, params);
                mCurrExtend = mEraser;
                initBrushSize();
            } else if (mCurrFun == FunType.Fun_VirtualDot) {
                
            } else if (mCurrFun == FunType.Fun_Menu) {
                Resources res = getResources();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.END;
                if (mDragButtonParams.y < mScreenHeight / 2) {
                    params.gravity = Gravity.TOP | params.gravity;
                    params.y = mDragButtonParams.y + mDragButtonHeight;
                    // Set top_arrow to visible and align to parent right.
                    View top_arrow = mMenu.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams arrowParams = (LinearLayout.LayoutParams) top_arrow.getLayoutParams();
                    arrowParams.setMarginEnd(res.getDimensionPixelSize(R.dimen.h02));
                    arrowParams.gravity = Gravity.END;
                    top_arrow.setLayoutParams(arrowParams);
                    // Set bottom_arrow to gone
                    View bottom_arrow = mMenu.findViewById(R.id.bottom_arrow);
                    bottom_arrow.setVisibility(View.GONE);
                } else {
                    params.gravity = Gravity.BOTTOM | params.gravity;
                    params.y = mScreenHeight - mDragButtonParams.y;
                    // Set top_arrow to gone
                    View top_arrow = mMenu.findViewById(R.id.top_arrow);
                    top_arrow.setVisibility(View.GONE);
                    // Set bottom_arrow to visible and align to parent right.
                    View bottom_arrow = mMenu.findViewById(R.id.bottom_arrow);
                    LinearLayout.LayoutParams arrowParams = (LinearLayout.LayoutParams) bottom_arrow.getLayoutParams();
                    arrowParams.setMarginEnd(res.getDimensionPixelSize(R.dimen.h02));
                    arrowParams.gravity = Gravity.END;
                    bottom_arrow.setLayoutParams(arrowParams);
                    bottom_arrow.setVisibility(View.VISIBLE);
                }
                int[] locations = new int[2];
                mBtnMenu.getLocationOnScreen(locations);
                int x = locations[0];
                //params.x = (x + mBtnMenu.getWidth()/2) - (mArrowWidth/2 + res.getDimensionPixelSize(R.dimen.h02));
                params.x = mScreenWidth - (x + mBtnMenu.getWidth()/2) - (mArrowWidth/2 + res.getDimensionPixelSize(R.dimen.h02));
                
                mMenu.setVisibility(View.VISIBLE);
                mWindowManager.addView(mMenu, params);
                mCurrExtend = mMenu;
            }
        } else {
            mIsToolBarExtend = false;
        }
        
        updateToolBarFunIconColor();
    }
    
    private void initDragButton() {
        //Initialize drag button
        if (mDragButton != null) {
            mCurrFuncIcon = (ImageView) mDragButton.findViewById(R.id.curr_func_icon);
            
            if (mDragBtnColorIcon != null) {
                mDragBtnColorIcon.setColor(mCurrBrushColor);
                mCurrFuncIcon.setBackground(mDragBtnColorIcon);
            }

            mDragButton.setOnTouchListener(new OnTouchListener() {
                //private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private boolean mIsMoving = false;
                private boolean mIsTryMoving = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onTouch");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onTouch, ACTION_DOWN");
                            //initialX = mDragButtonParams.x;
                            initialY = mDragButtonParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onTouch, ACTION_UP");
                            if (!mIsMoving && !mIsTryMoving) {
                                if (!mbIsAnimationPlaying) {
                                    if (mIsToolBarOpen) {
                                        if (mIsToolBarExtend) {
                                            setToolPanelVisibility(false);
                                        } else {
                                            mToolBar.clearAnimation();
                                            if (mCurrLayoutMode == LayoutMode.LEFT_SIDE) {
                                                playAnimation(mToolBar, mLeftOut);
                                            } else if (mCurrLayoutMode == LayoutMode.RIGHT_SIDE) {
                                                playAnimation(mToolBar, mRightOut);
                                            }
                                        }
                                    } else {
                                        if (mCurrLayoutMode == LayoutMode.LEFT_SIDE) {
                                            mToolBarParams.gravity = Gravity.START;
                                        } else if (mCurrLayoutMode == LayoutMode.RIGHT_SIDE) {
                                            mToolBarParams.gravity = Gravity.END;
                                        }
                                        mToolBarParams.gravity = Gravity.TOP | mToolBarParams.gravity;
                                        mToolBarParams.x = 0;
                                        mToolBarParams.y = mDragButtonParams.y;
                                        mWindowManager.updateViewLayout(mToolBarParent, mToolBarParams);
                                        mToolBarParent.setVisibility(View.VISIBLE);

                                        mToolBar.clearAnimation();
                                        mToolBar.setVisibility(View.VISIBLE);
                                        if (mCurrLayoutMode == LayoutMode.LEFT_SIDE) {
                                            playAnimation(mToolBar, mLeftIn);
                                        } else if (mCurrLayoutMode == LayoutMode.RIGHT_SIDE) {
                                            playAnimation(mToolBar, mRightIn);
                                        }
                                    }
                                }
                            } else {
                                mIsTryMoving = false;
                                mIsMoving = false;

                                /*if (mDragButtonParams.x <= mScreenWidth / 2) {
                                    mDragButtonParams.x = 0;
                                    mWindowManager.updateViewLayout(mDragButton, mDragButtonParams);
                                    mCurrLayoutMode = LayoutMode.LEFT_SIDE;
                                } else {
                                    mDragButtonParams.x = mScreenWidth - mDragButtonWidth;
                                    mWindowManager.updateViewLayout(mDragButton, mDragButtonParams);
                                    mCurrLayoutMode = LayoutMode.RIGHT_SIDE;
                                }*/
                            }
                            // mDragButton.setBackgroundResource(R.drawable.floating2);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onTouch, ACTION_MOVE, mToolBox.getHeight()" + mToolBar.getHeight());
                            if (Math.abs(event.getRawX() - initialTouchX) > 10 ||
                                    Math.abs(event.getRawY() - initialTouchY) > 10) {
                                mIsTryMoving = true;
                            }
                            if (/*!mIsToolBarExtend && */mIsTryMoving) {
                                mIsMoving = true;
                                // paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                                mDragButtonParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                                if (mDragButtonParams.y < 0) {
                                    mDragButtonParams.y = 0;
                                } else if (mDragButtonParams.y > mScreenHeight - mDragButtonHeight) {
                                    mDragButtonParams.y = mScreenHeight - mDragButtonHeight;
                                }

                                mWindowManager.updateViewLayout(mDragButton, mDragButtonParams);
                                // mDragButton.setBackgroundResource(R.drawable.ic_launcher);

                                if (mIsToolBarOpen) {
                                    mToolBarParams.y = mDragButtonParams.y;
                                    mWindowManager.updateViewLayout(mToolBarParent, mToolBarParams);
                                }
                                
                                if (mIsToolBarExtend) {
                                    setToolPanelVisibility(false);
                                }
                            }
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void updateToolBarFunIconColor() {
        Resources res = getResources();
        Drawable bgPalette = mBtnPalette.getBackground();
        Drawable bgEraser = mBtnEraser.getBackground();
        Drawable bgVirtualDot = mBtnVirtualDot.getBackground();
        Drawable bgMenu = mBtnMenu.getBackground();
        
        if (mCurrPaintFun == FunType.Fun_Palette) {
            bgPalette.setColorFilter(res.getColor(R.color.common_category_color), Mode.SRC_ATOP);
            bgEraser.clearColorFilter();
        } else {
            bgPalette.clearColorFilter();
            bgEraser.setColorFilter(res.getColor(R.color.common_category_color), Mode.SRC_ATOP);
        }
        
        if (mIsVirtualDotOpen) {
            bgVirtualDot.setColorFilter(res.getColor(R.color.common_category_color), Mode.SRC_ATOP);
        } else {
            bgVirtualDot.clearColorFilter();
        }
        
        if (mCurrExtend == mMenu) {
            bgMenu.setColorFilter(res.getColor(R.color.common_category_color), Mode.SRC_ATOP);
        } else {
            bgMenu.clearColorFilter();
        }
    }

    private void initToolBar() {
        if (mToolBarParent != null) {
            Resources res = getResources();
            
            mToolBar = mToolBarParent.findViewById(R.id.tool_bar);
            mBtnPalette = (ImageButton) mToolBar.findViewById(R.id.btn_palette);
            mBtnEraser = (ImageButton) mToolBar.findViewById(R.id.btn_eraser);
            mBtnVirtualDot = (ImageButton) mToolBar.findViewById(R.id.btn_virtual_dot);
            mBtnMenu = (ImageButton) mToolBar.findViewById(R.id.btn_menu);
            
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBtnPalette.getLayoutParams();
            int leftMargin = res.getDimensionPixelSize(R.dimen.drag_button_width) + res.getDimensionPixelSize(R.dimen.margin_xs_3);
            params.setMargins(leftMargin, 0, 0, 0);
            mBtnPalette.setLayoutParams(params);
            
            if (mButtonListener != null) {
                mBtnPalette.setOnClickListener(mButtonListener);
                mBtnEraser.setOnClickListener(mButtonListener);
                mBtnVirtualDot.setOnClickListener(mButtonListener);
                mBtnMenu.setOnClickListener(mButtonListener);
            }
            
            updateToolBarFunIconColor();
        }
    }
    
    private void initMenu() {
        if (mMenu != null) {
            ListView menuListView = (ListView) mMenu.findViewById(R.id.menu);
            menuListView.setAdapter(new ListAdapter() {

                @Override
                public void unregisterDataSetObserver(DataSetObserver observer) {
                }

                @Override
                public void registerDataSetObserver(DataSetObserver observer) {
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean hasStableIds() {
                    return true;
                }

                @Override
                public int getViewTypeCount() {
                    return 1;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View listItem = convertView;
                    if (listItem == null) {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        listItem = inflater.inflate(R.layout.menu_listitem, null);
                    }
                    TextView text = (TextView) listItem.findViewById(R.id.item_text);

                    switch (position) {
                        case MENU_ITEM_CLEAR_ALL:
                            text.setText(R.string.menu_clean_all);
                            break;
                        case MENU_ITEM_INSERT_IMAGES:
                            text.setText(R.string.menu_insert);
                            break;
                        case MENU_ITEM_SET_THEME:
                            text.setText(R.string.menu_set_theme);
                            break;
                        case MENU_ITEM_SHARE:
                            text.setText(R.string.menu_share);
                            break;
                        case MENU_ITEM_EXIT:
                            text.setText(R.string.menu_exit);
                            break;
                        default:
                            break;
                    }
                    return listItem;
                }

                @Override
                public int getItemViewType(int position) {
                    return 0;
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public Object getItem(int position) {
                    return position;
                }

                @Override
                public int getCount() {
                    return 5;
                }

                @Override
                public boolean isEnabled(int position) {
                    return true;
                }

                @Override
                public boolean areAllItemsEnabled() {
                    return true;
                }
            });

            menuListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onItemClick, position: " + position + ", id: " + id);
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, FunType.Fun_Menu);
                    switch (position) {
                        case MENU_ITEM_CLEAR_ALL:
                            intent.putExtra(EXTRA_MENNU_ITEM, MENU_ITEM_CLEAR_ALL_STR);
                            break;
                        case MENU_ITEM_INSERT_IMAGES:
                            intent.putExtra(EXTRA_MENNU_ITEM, MENU_ITEM_INSERT_IMAGES_STR);
                            break;
                        case MENU_ITEM_SET_THEME:
                            intent.putExtra(EXTRA_MENNU_ITEM, MENU_ITEM_SET_THEME_STR);
                            break;
                        case MENU_ITEM_SHARE:
                            intent.putExtra(EXTRA_MENNU_ITEM, MENU_ITEM_SHARE_STR);
                            break;
                        case MENU_ITEM_EXIT:
                            intent.putExtra(EXTRA_MENNU_ITEM, MENU_ITEM_EXIT_STR);
                            showHideToolbar(false);
                            break;
                        default:
                            break;
                    }
                    mLocalBroadcastManager.sendBroadcast(intent);
                    setToolPanelVisibility(false);
                }
            });
        }
    }
    
    private void closeToolBar() {
        if (mIsToolBarOpen && !mbIsAnimationPlaying) {
            if (mIsToolBarExtend) {
                setToolPanelVisibility(false);
            }
            
            mToolBar.clearAnimation();
            if (mCurrLayoutMode == LayoutMode.LEFT_SIDE) {
                playAnimation(mToolBar, mLeftOut);
            } else if (mCurrLayoutMode == LayoutMode.RIGHT_SIDE) {
                playAnimation(mToolBar, mRightOut);
            }
        }
    }
    
    public boolean isToolBarOpen() {
        return mIsToolBarOpen;
    }

    private void playAnimation(View view, Animation animation) {
        if (view != null && animation != null) {
            mbIsAnimationPlaying = true;
            view.startAnimation(animation);
        } else {
            if (view == null) {
                Log.w(DotDesignConstants.LOG_TAG, LOG_PREFIX + "playAnimation, view can't be null!");
            }
            if (view == null) {
                Log.w(DotDesignConstants.LOG_TAG, LOG_PREFIX + "playAnimation, animation can't be null!");
            }
        }
    }

    private void initAnimation() {
        mLeftIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, (float)-1.0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mLeftIn.setDuration(mAnimationDuration);
        mLeftIn.setInterpolator(new DecelerateInterpolator(mAnimationFactor));
        mLeftIn.setRepeatCount(0);
        mLeftIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mIsToolBarOpen = true;
                mbIsAnimationPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
        
        mLeftOut = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, (float) -1.0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mLeftOut.setDuration(mAnimationDuration);
        mLeftOut.setInterpolator(new DecelerateInterpolator(mAnimationFactor));
        mLeftOut.setRepeatCount(0);
        mLeftOut.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mToolBar.setVisibility(View.GONE);
                mToolBarParent.setVisibility(View.INVISIBLE);
                mIsToolBarOpen = false;
                mbIsAnimationPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
        
        mRightIn = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, (float)1.0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mRightIn.setDuration(mAnimationDuration);
        mRightIn.setInterpolator(new DecelerateInterpolator(mAnimationFactor));
        mRightIn.setRepeatCount(0);
        mRightIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mIsToolBarOpen = true;
                mbIsAnimationPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
        
        mRightOut = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, (float) 1.0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mRightOut.setDuration(mAnimationDuration);
        mRightOut.setInterpolator(new DecelerateInterpolator(mAnimationFactor));
        mRightOut.setRepeatCount(0);
        mRightOut.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mToolBar.setVisibility(View.GONE);
                mToolBarParent.setVisibility(View.INVISIBLE);
                mIsToolBarOpen = false;
                mbIsAnimationPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }
    
    private void initBrushSize() {
        View extendView = null;
        if (mCurrFun == FunType.Fun_Palette) {
            extendView = mPalette;
        } else {
            extendView = mEraser;
        }
        if (extendView != null) {
            ImageView button = null;
            button = (ImageView) extendView.findViewById(R.id.btn_1x1);
            if (button != null) {
                button.setOnClickListener(mButtonListener);
            }
            
            button = (ImageView) extendView.findViewById(R.id.btn_2x2);
            if (button != null) {
                button.setOnClickListener(mButtonListener);
            }
            
            updateBrushColor();
        }
    }
    
    private void updateBrushColor() {
        Resources resources = getResources();
        ImageView button = null;
        GradientDrawable drawable = null;
        
        View extendView = null;
        if (mCurrFun == FunType.Fun_Palette) {
            extendView = mPalette;
        } else {
            extendView = mEraser;
        }
        if (extendView != null) {
            button = (ImageView) extendView.findViewById(R.id.btn_1x1);
            if (button != null) {
                //drawable = (GradientDrawable) button.getBackground();
                drawable = (GradientDrawable) button.getDrawable();
                if (mCurrBrushSize == BrushSize.Size_1x1) {
                    drawable.setColor(resources.getColor(R.color.brush_size_enable_color));
                } else if (mCurrBrushSize == BrushSize.Size_2x2) {
                    drawable.setColor(resources.getColor(R.color.brush_size_disable_color));
                }
            }
            
            button = (ImageView) extendView.findViewById(R.id.btn_2x2);
            if (button != null) {
                //drawable = (GradientDrawable) button.getBackground();
                drawable = (GradientDrawable) button.getDrawable();
                if (mCurrBrushSize == BrushSize.Size_1x1) {
                    drawable.setColor(resources.getColor(R.color.brush_size_disable_color));
                } else if (mCurrBrushSize == BrushSize.Size_2x2) {
                    drawable.setColor(resources.getColor(R.color.brush_size_enable_color));
                }
            }
        }
    }
    
    private void initBrushColor() {
        Resources res = getResources();
        
        final int size = 4 * 3;
        int[] btn_id = new int[size];
        btn_id[0] = R.id.btn_color_11;
        btn_id[1] = R.id.btn_color_12;
        btn_id[2] = R.id.btn_color_13;
        btn_id[3] = R.id.btn_color_14;
        btn_id[4] = R.id.btn_color_21;
        btn_id[5] = R.id.btn_color_22;
        btn_id[6] = R.id.btn_color_23;
        btn_id[7] = R.id.btn_color_24;
        btn_id[8] = R.id.btn_color_31;
        btn_id[9] = R.id.btn_color_32;
        btn_id[10] = R.id.btn_color_33;
        btn_id[11] = R.id.btn_color_34;
        
        int[] color_id = new int[size];
        color_id[0] = R.color.palette_color_11;
        color_id[1] = R.color.palette_color_12;
        color_id[2] = R.color.palette_color_13;
        color_id[3] = R.color.palette_color_14;
        color_id[4] = R.color.palette_color_21;
        color_id[5] = R.color.palette_color_22;
        color_id[6] = R.color.palette_color_23;
        color_id[7] = R.color.palette_color_24;
        color_id[8] = R.color.palette_color_31;
        color_id[9] = R.color.palette_color_32;
        color_id[10] = R.color.palette_color_33;
        color_id[11] = R.color.palette_color_34;
        
        if (res != null) {
            ImageView button = null;
            GradientDrawable drawable = null;
            for (int i = 0; i < size; i++) {
                button = (ImageView) mPalette.findViewById(btn_id[i]);
                if (button != null) {
                    drawable = (GradientDrawable) button.getDrawable();
                    drawable.setColor(res.getColor(color_id[i]));
                    mBtnColor.put(btn_id[i], res.getColor(color_id[i]));
                    button.setOnClickListener(mButtonListener);
                    
                    if (i == 0) {
                        mCurrBrushColorView = button;
                        mCurrBrushColor = res.getColor(color_id[i]);
                        setSelectedColor(mCurrBrushColorView);
                    }
                }
            }
        }
    }
    
    private void setSelectedColor(ImageView button) {
        Drawable select = getResources().getDrawable(R.drawable.dot_design_select);
        select.setColorFilter(getResources().getColor(R.color.overlay_color), Mode.SRC_IN);
        
        ImageView selectedIcon = (ImageView) mPalette.findViewById(R.id.selected);
        selectedIcon.setBackground(select);
        
        Resources res = getResources();
        int id = button.getId();
        int buttonLeft = button.getLeft();
        int buttonTop = button.getTop();
        int m1 = res.getDimensionPixelSize(R.dimen.margin_l);
        int m2 = res.getDimensionPixelSize(R.dimen.margin_m);
        int colorSize = res.getDimensionPixelSize(R.dimen.hv01);
        if (id == R.id.btn_color_11 ||
                id == R.id.btn_color_12 ||
                id == R.id.btn_color_13 ||
                id == R.id.btn_color_14) {
            buttonTop = m2;
        } else if (id == R.id.btn_color_21 ||
                id == R.id.btn_color_22 ||
                id == R.id.btn_color_23 ||
                id == R.id.btn_color_24) {
            buttonTop = m2 + colorSize + m1;
        } else {
            buttonTop = m2 + 2*colorSize + 2*m1;
        }
        
        if (id == R.id.btn_color_11 ||
                id == R.id.btn_color_21 ||
                id == R.id.btn_color_31) {
            buttonLeft = m2;
        } else if (id == R.id.btn_color_12 ||
                id == R.id.btn_color_22 ||
                id == R.id.btn_color_32) {
            buttonLeft = 3*m2 + colorSize;
        } else if (id == R.id.btn_color_13 ||
                id == R.id.btn_color_23 ||
                id == R.id.btn_color_33) {
            buttonLeft = 5*m2 + 2*colorSize;
        } else {
            buttonLeft = 7*m2 + 3*colorSize;
        }
        
        int widthDiff = res.getDimensionPixelSize(R.dimen.select_color_width) - colorSize;
        int heightDiff = res.getDimensionPixelSize(R.dimen.select_color_height) - colorSize;
        int marginLeft = buttonLeft - (widthDiff/2);
        int marginTop = buttonTop - (heightDiff/2);
        
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectedIcon.getLayoutParams();
        params.setMargins(marginLeft, marginTop, 0, 0);
        selectedIcon.setLayoutParams(params);
        selectedIcon.setVisibility(View.VISIBLE);
    } 
    
    private ButtonListener mButtonListener = new ButtonListener();

    class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_palette: {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_palette");
                    boolean bShow = true;
                    if (mIsToolBarExtend && mCurrFun == FunType.Fun_Palette) {
                        bShow = false;
                    }
                    mCurrPaintFun = FunType.Fun_Palette;
                    mCurrFun = FunType.Fun_Palette;
                    setToolPanelVisibility(bShow);
                    
                    mCurrFuncIcon.setBackground(mDragBtnColorIcon);
                    
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, mCurrFun);
                    intent.putExtra(EXTRA_CURR_COLOR, mCurrBrushColor);
                    intent.putExtra(EXTRA_CURR_BRUSH_SIZE, mCurrBrushSize);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    break;
                }
                case R.id.btn_eraser: {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_eraser");
                    boolean bShow = true;
                    if (mIsToolBarExtend && mCurrFun == FunType.Fun_Eraser) {
                        bShow = false;
                    }
                    mCurrPaintFun = FunType.Fun_Eraser;
                    mCurrFun = FunType.Fun_Eraser;
                    setToolPanelVisibility(bShow);
                    
                    mCurrFuncIcon.setBackground(mDragBtnEraserIcon);
                    
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, mCurrFun);
                    intent.putExtra(EXTRA_CURR_BRUSH_SIZE, mCurrBrushSize);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    break;
                }
                case R.id.btn_virtual_dot: {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_virtual_dot");
                    mIsVirtualDotOpen = !mIsVirtualDotOpen;
                    mCurrFun = FunType.Fun_VirtualDot;
                    updateToolBarFunIconColor();
                    closeToolBar();
                    
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, mCurrFun);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    break;
                }
                case R.id.btn_menu: {
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_menu");
                    boolean bShow = true;
                    if (mIsToolBarExtend && mCurrFun == FunType.Fun_Menu) {
                        bShow = false;
                    }
                    mCurrFun = FunType.Fun_Menu;
                    setToolPanelVisibility(bShow);
                    break;
                }
                case R.id.btn_1x1:
                case R.id.btn_2x2: {
                    if (v.getId() == R.id.btn_1x1) {
                        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_1x1");
                        mCurrBrushSize = BrushSize.Size_1x1;
                    } else if (v.getId() == R.id.btn_2x2) {
                        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, btn_2x2");
                        mCurrBrushSize = BrushSize.Size_2x2;
                    }
                    updateBrushColor();
                    
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, mCurrFun);
                    if (mCurrFun == FunType.Fun_Palette) {
                        intent.putExtra(EXTRA_CURR_COLOR, mCurrBrushColor);
                    }
                    intent.putExtra(EXTRA_CURR_BRUSH_SIZE, mCurrBrushSize);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    break;
                }
                case R.id.btn_color_11:
                case R.id.btn_color_12: 
                case R.id.btn_color_13: 
                case R.id.btn_color_14: 
                case R.id.btn_color_21:
                case R.id.btn_color_22: 
                case R.id.btn_color_23: 
                case R.id.btn_color_24: 
                case R.id.btn_color_31:
                case R.id.btn_color_32: 
                case R.id.btn_color_33: 
                case R.id.btn_color_34: {
                    mCurrBrushColor = mBtnColor.get(v.getId());
                    if (v instanceof ImageView) {
                        mCurrBrushColorView.setBackground(null);
                        mCurrBrushColorView = (ImageView) v;
                        setSelectedColor(mCurrBrushColorView);
                    }

                    mDragBtnColorIcon.setColor(mCurrBrushColor);
                    mCurrFuncIcon.setBackground(mDragBtnColorIcon);
                    
                    Intent intent = new Intent(TOOL_BAR_ACTION_STATUS);
                    intent.putExtra(EXTRA_CURR_FUNC, mCurrFun);
                    intent.putExtra(EXTRA_CURR_COLOR, mCurrBrushColor);
                    intent.putExtra(EXTRA_CURR_BRUSH_SIZE, mCurrBrushSize);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    
                    String hexColor = String.format("#%06X", (0xFFFFFF & mCurrBrushColor));
                    Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onClick, BrushColor changes to: " + hexColor);
                    break;
                }
                default:
                    break;
            }
        }
    }
    
    private BroadcastReceiver mToolBarReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.d(DotDesignConstants.LOG_TAG, "mToolBarReceiver.onReceive, action: " + action);
                if (SHOW_HIDE_TOOL_BAR.equals(action)) {
                    boolean bShow = intent.getBooleanExtra(EXTRA_SHOW_HIDE, true);
                    showHideToolbar(bShow);
                } else if (USER_START_DRAWING.equals(action)) {
                    closeToolBar();
                }
            }
        }
    };
    
    void showHideToolbar(boolean bShow) {
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "showHideToolbar, bShow: " + bShow);
        if (bShow) {
            if (mDragButton != null && !mDragButton.isShown()) {
                mWindowManager.addView(mToolBarParent, mToolBarParams);
                mWindowManager.addView(mDragButton, mDragButtonParams);
                mToolBarParent.setVisibility(View.INVISIBLE);
                updateToolBarFunIconColor();
            } 
        } else {
            if (mToolBarParent != null) {
                mWindowManager.removeView(mToolBarParent);
            }
            if (mDragButton != null) {
                mWindowManager.removeView(mDragButton);
            }
            if (mCurrExtend != null && mCurrExtend.isShown()) {
                mWindowManager.removeView(mCurrExtend);
                mCurrExtend = null;
            }
            mIsToolBarOpen = false;
            mIsToolBarExtend = false;
        }
    }
    
    public boolean isToolBarShow() {
        if (mDragButton != null && mDragButton.isShown()) {
            return true;
        } else {
            return false;
        }
    }
}