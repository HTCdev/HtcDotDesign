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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.htc.dotdesign.DotDesignConstants.DrawingMode;
import com.htc.dotdesign.ToolBoxService.BrushSize;
import com.htc.dotdesign.ToolBoxService.FunType;
import com.htc.dotdesign.util.DotDesignUtil;
import com.htc.dotviewprocessing.WallpaperMaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class DrawingView extends DotImage {
    private static final String LOG_PREFIX = "[DrawingView] ";
	private float clickX = 0, clickY = 0;
	private boolean isUserPressFinsih = false;
	private boolean isShowDotMask = false;
	private float mPreX = -1;
	private float mPreY = -1;
	private Mode mCurrentMode = Mode.BRUSH_1X1;
	private DrawingMode mCurrentDrawMode = DrawingMode.MODE_DOTDESIGNTEMPLATE;
	private Context mContext = null;
	private int mCurrBrushColor = Color.TRANSPARENT;
	private int mEraseColor = Color.TRANSPARENT;
	private AlertDialog mAlertDialog = null;
	private DrawingActivity mActivity = null;
	private Bitmap mScaledPhoto = null;
	private Bitmap mDrawingBitmap = null;
	private ProgressDialog mLoadingDialog = null;
	private String mFileName = "";

	public enum Mode {
		BRUSH_1X1, BRUSH_2X2, ERASER_1X1, ERASER_2X2
	}

	public DrawingView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	private void init() {
        if (mContext != null) {
            mCurrBrushColor = mContext.getResources().getColor(R.color.palette_color_11);
        }
		createImgDotMatrix(sInnerFrameWidth, sInnerFrameHeight);
		resetImgDotMatrixValue();
		registerForToolBoxChange(mContext);
	}

	public void initparams(DrawingActivity activity) {
		mActivity = activity;
	}

    public void reset() {
        unregisterForToolBoxChange(mContext);
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        dismissLoadingDlg();
        mCurrBrushColor = Color.TRANSPARENT;
        mEraseColor = Color.TRANSPARENT;
    }

//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//        if (mImgDotMatrix == null || sPaint == null) {
//            return;
//        }
//        float left = 0;
//        float top = 0;
//        float right = sDotPixelWidth;
//        float bottom = sDotPixelHeight;
//
//        for (int row = 0; row < mRowSize; ++row) {
//            for (int col = 0; col < mColSize; ++col) {
//                setPaintColor(mImgDotMatrix[row][col]);
//                canvas.drawRect(left, top, right, bottom, sPaint);
//                left += sDotPixelWidth;
//                right += sDotPixelWidth;
//            }
//            left = 0;
//            right = sDotPixelWidth;
//            top += sDotPixelHeight;
//            bottom += sDotPixelHeight;
//        }
//	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        clickX = event.getX();
        clickY = event.getY();
        int x = (int) (clickY / sDotPixelWidth);
        int y = (int) (clickX / sDotPixelHeight);
        if (isShowDotMask) {
            mEraseColor = Color.BLACK;
        }
        else {
            mEraseColor = Color.TRANSPARENT;
        }
        
        if (mActivity != null && mActivity.isToolBarOpen()) {
            Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "ToolBarOpen");
            closeToolBar();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if ((x >= 0 && x < 48) && (y >= 0 && y < 27)) {
                mPreX = x;
                mPreY = y;
                if (mCurrentMode == Mode.BRUSH_1X1) {
                    mImgDotMatrix[x][y] = mCurrBrushColor;
                } else if (mCurrentMode == Mode.BRUSH_2X2) {
                    mImgDotMatrix[x][y] = mCurrBrushColor;
                    if (y + 1 < 27) {
                        mImgDotMatrix[x][y + 1] = mCurrBrushColor;
                    }
                    if (x + 1 < 48) {
                        mImgDotMatrix[x + 1][y] = mCurrBrushColor;
                    }
                    if (x + 1 < 48 && y + 1 < 27) {
                        mImgDotMatrix[x + 1][y + 1] = mCurrBrushColor;
                    }
                } else if (mCurrentMode == Mode.ERASER_1X1) {
                    mImgDotMatrix[x][y] = mEraseColor;
                } else if (mCurrentMode == Mode.ERASER_2X2) {
                    mImgDotMatrix[x][y] = mEraseColor;
                    if (y + 1 < 27) {
                        mImgDotMatrix[x][y + 1] = mEraseColor;
                    }
                    if (x + 1 < 48) {
                        mImgDotMatrix[x + 1][y] = mEraseColor;
                    }
                    if (x + 1 < 48 && y + 1 < 27) {
                        mImgDotMatrix[x + 1][y + 1] = mEraseColor;
                    }
                }

                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (x != mPreX || y != mPreY) {
                if ((x >= 0 && x < 48) && (y >= 0 && y < 27)) {
                    mPreX = x;
                    mPreY = y;
                    if (mCurrentMode == Mode.BRUSH_1X1) {
                         mImgDotMatrix[x][y] = mCurrBrushColor;
                    } else if (mCurrentMode == Mode.BRUSH_2X2) {
                        mImgDotMatrix[x][y] = mCurrBrushColor;
                        if (y + 1 < 27) {
                            mImgDotMatrix[x][y + 1] = mCurrBrushColor;
                        }
                        if (x + 1 < 48) {
                            mImgDotMatrix[x + 1][y] = mCurrBrushColor;
                        }
                        if (x + 1 < 48 && y + 1 < 27) {
                            mImgDotMatrix[x + 1][y + 1] = mCurrBrushColor;
                        }
                    } else if (mCurrentMode == Mode.ERASER_1X1) {
                        mImgDotMatrix[x][y] = mEraseColor;
                    } else if (mCurrentMode == Mode.ERASER_2X2) {
                        mImgDotMatrix[x][y] = mEraseColor;
                        if (y + 1 < 27) {
                            mImgDotMatrix[x][y + 1] = mEraseColor;
                        }
                        if (x + 1 < 48) {
                            mImgDotMatrix[x + 1][y] = mEraseColor;
                        }
                        if (x + 1 < 48 && y + 1 < 27) {
                            mImgDotMatrix[x + 1][y + 1] = mEraseColor;
                        }
                    }
                    invalidate();
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

	@Override
	protected void initImgDotMatrix() {
		// TODO Auto-generated method stub

	}

	private void registerForToolBoxChange(Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ToolBoxService.TOOL_BAR_ACTION_STATUS);
		if (context != null) {
			LocalBroadcastManager.getInstance(context).registerReceiver(
					mToolBarReceiver, filter);
		}
	}

	private void unregisterForToolBoxChange(Context context) {
		if (context != null) {
			LocalBroadcastManager.getInstance(context).unregisterReceiver(
					mToolBarReceiver);
		}
	}

	private BroadcastReceiver mToolBarReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onReceive:" + action);
				if (!TextUtils.isEmpty(action)) {
					if (ToolBoxService.TOOL_BAR_ACTION_STATUS.equals(action)) {
						Serializable serializable = intent
								.getSerializableExtra(ToolBoxService.EXTRA_CURR_FUNC);
						FunType curtype = null;
						BrushSize curBrush = null;
						if (serializable != null
								&& serializable instanceof FunType) {
							curtype = (FunType) serializable;
						}
						Serializable brushSerializable = intent
								.getSerializableExtra(ToolBoxService.EXTRA_CURR_BRUSH_SIZE);
						if (brushSerializable != null
								&& brushSerializable instanceof BrushSize) {
							curBrush = (BrushSize) brushSerializable;
						}
						if (curtype == FunType.Fun_Palette) {
							if (curBrush == BrushSize.Size_1x1) {
								mCurrentMode = Mode.BRUSH_1X1;
							} else if (curBrush == BrushSize.Size_2x2) {
								mCurrentMode = Mode.BRUSH_2X2;
							}
							mCurrBrushColor = intent.getIntExtra(
									ToolBoxService.EXTRA_CURR_COLOR,
									Color.TRANSPARENT);
						} else if (curtype == FunType.Fun_Eraser) {
							if (curBrush == BrushSize.Size_1x1) {
								mCurrentMode = Mode.ERASER_1X1;
							} else if (curBrush == BrushSize.Size_2x2) {
								mCurrentMode = Mode.ERASER_2X2;
							}
                        } else if (curtype == FunType.Fun_VirtualDot) {
                            if (!isShowDotMask) {
                                if (mActivity != null) {
                                    mActivity.setDotMask();
                                }
                                isShowDotMask = true;
                                for (int row = 0; row < mRowSize; ++row) {
                                    for (int col = 0; col < mColSize; ++col) {
                                        if (mImgDotMatrix[row][col] == Color.TRANSPARENT) {
                                            mImgDotMatrix[row][col] = Color.BLACK;
                                        }
                                    }
                                }
                            }
                            else {
                                if (mActivity != null) {
                                    mActivity.hideDotMask();
                                }
                                isShowDotMask = false;
                                for (int row = 0; row < mRowSize; ++row) {
                                    for (int col = 0; col < mColSize; ++col) {
                                        if (mImgDotMatrix[row][col] == Color.BLACK) {
                                            mImgDotMatrix[row][col] = Color.TRANSPARENT;
                                        }
                                    }
                                }
                            }
                            invalidate();
                        } else if (curtype == FunType.Fun_Menu) {
							String menuitem = intent
									.getStringExtra(ToolBoxService.EXTRA_MENNU_ITEM);
							if (menuitem
									.equals(ToolBoxService.MENU_ITEM_CLEAR_ALL_STR)) {
								clearDrawingView();
							} else if (menuitem
									.equals(ToolBoxService.MENU_ITEM_INSERT_IMAGES_STR)) {
								selectPhotoSourceDialog();
							} else if (menuitem
									.equals(ToolBoxService.MENU_ITEM_SET_THEME_STR)) {
								setAsDotViewTheme();
							} else if (menuitem
									.equals(ToolBoxService.MENU_ITEM_SHARE_STR)) {
								sharePicture();
							} else if (menuitem
									.equals(ToolBoxService.MENU_ITEM_EXIT_STR)) {
								exitDialog();
							}
						}
					}
				}
			}
		}

	};

	public void setAsDotViewTheme() {
		if (mContext == null) {
			return;
		}
		Bitmap bitmap = null;
		Bitmap photoARGB = null;
		try {
			bitmap = handlePicture();
			if (bitmap != null) {
				int[] photoData = new int[bitmap.getWidth()
						* bitmap.getHeight()];
				bitmap.getPixels(photoData, 0, bitmap.getWidth(), 0, 0,
						bitmap.getWidth(), bitmap.getHeight());
				photoARGB = Bitmap.createBitmap(bitmap.getWidth(),
						bitmap.getHeight(), Bitmap.Config.ARGB_8888);
				photoARGB.setPixels(photoData, 0, photoARGB.getWidth(), 0, 0,
						photoARGB.getWidth(), photoARGB.getHeight());

				WallpaperMaker wallpaper = new WallpaperMaker();
				mScaledPhoto = wallpaper.convertDotViewWallpaper(photoARGB, 27,
						48);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				mScaledPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				Intent intent = new Intent(DotDesignConstants.NOTIFY_DOTVIEW_TO_UPDATE_WALLPAPER);
				intent.putExtra(DotDesignConstants.EXTRA_BITMAP_ARRAY, byteArray);
				String filename = DotDesignUtil.getFileName() + "DotDrop";
				intent.putExtra(DotDesignConstants.EXTRA_FILE_NAME, filename);
				mContext.sendBroadcast(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
			if (mScaledPhoto != null && !mScaledPhoto.isRecycled()) {
				mScaledPhoto.recycle();
				mScaledPhoto = null;
			}
			if (photoARGB != null && !photoARGB.isRecycled()) {
				photoARGB.recycle();
				photoARGB = null;
			}
		}
        if (mActivity != null) {
            Toast.makeText(mActivity, getResources().getString(R.string.toast_theme_applied),
                    Toast.LENGTH_SHORT).show();
        }
	}

	private void clearDrawingView() {
		resetImgDotMatrixValue();
		invalidate();
	}

	@Override
    public void resetImgDotMatrixValue() {
	    if (mImgDotMatrix != null) {
            for (int row = 0; row < mRowSize; ++row) {
                for (int col = 0; col < mColSize; ++col) {
                    if(isShowDotMask) {
                        mImgDotMatrix[row][col] = Color.BLACK;
                    }
                    else {
                        mImgDotMatrix[row][col] = sBackgroundColor;   
                    }
                }
            }
        }
    }

    private void selectPhotoSourceDialog() {
        if (mContext == null) {
            return;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        String[] selectPhotoFrom = getResources().getStringArray(
                R.array.items_select_template_from);
        mAlertDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK)
                .setTitle(R.string.title_choose_template_from)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                            mAlertDialog = null;
                            showHideToolBar(true);
                        }
                    };
                })
                .setItems(selectPhotoFrom,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (mAlertDialog != null) {
                                    mAlertDialog.dismiss();
                                    mAlertDialog = null;
                                }
                                switch (which) {
                                    case 0: {
                                        selectTemplate();
                                    }
                                        break;
                                    case 1: {
                                        selectFreeSketch();
                                    }
                                        break;
                                    case 2: {
                                        DotDesignUtil.selectTemplateFromGallery(mActivity);
                                    }
                                        break;
                                    case 3: {
                                        DotDesignUtil.selectTemplateFromCamera(mActivity);
                                    }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create();
        mAlertDialog.show();
        showHideToolBar(false);
    }

	public void exitDialog() {
		if (mContext == null) {
			return;
		}
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
		mAlertDialog = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_DARK)
				.setTitle(R.string.title_exit).setCancelable(true)
				.setMessage(R.string.exit_dialog_message)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						if (mAlertDialog != null) {
							mAlertDialog.dismiss();
							mAlertDialog = null;
							showHideToolBar(true);
						}
					};
				})
				.setNegativeButton(R.string.button_not_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mActivity != null) {
							mActivity.finish();	
						}
					}
				})
				.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isUserPressFinsih = true;
						new HandleFilesTask().execute("");
					}
				})
				.create();
		mAlertDialog.show();
		showHideToolBar(false);
	}
	
	private void selectFreeSketch() {
		if(mActivity != null) {
			mActivity.setFreeSketch();
		}
		showHideToolBar(true);
	}
	
	private void selectTemplate() {
	    if(mActivity != null) {
            mActivity.selectTemplate();
        }
        showHideToolBar(true);
	}

	public void updateTemplateImage(Bitmap bitmap,String filename,DrawingMode mode) {
		mDrawingBitmap = bitmap;
		mFileName = filename;
		mCurrentDrawMode = mode;
		//resetImgDotMatrixValue();
	}
	
	public void updateColorArray(int[][]array) {
		if(array != null && array.length > 0) {
			mImgDotMatrix = array;
			invalidate();	
		}
	}

    private void sharePicture() {
        if (mContext == null) {
            return;
        }
        Bitmap bitmap = null;
        Bitmap scaledBmp = null;
        try {
            bitmap = handlePicture();
            scaledBmp = Bitmap.createScaledBitmap(bitmap, DotDesignConstants.SHARE_PHOTO_WIDTH,
                    DotDesignConstants.SHARE_PHOTO_HEIGHT, false);
            DotDesignUtil.SaveImagetoExternal(scaledBmp, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scaledBmp != null && !scaledBmp.isRecycled()) {
                scaledBmp.recycle();
                scaledBmp = null;
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        File tmpFile = new File(mContext.getExternalFilesDir(null),
                DotDesignConstants.shareName);
        Intent shareintent = new Intent(Intent.ACTION_SEND);
        shareintent.setType("image/png");
        shareintent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile));
        mContext.startActivity(Intent.createChooser(shareintent, "Share"));
    }

    private Bitmap handlePicture() {
        Paint paint = new Paint();
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int[][] mCloneArray = new int[mRowSize][mColSize];
        float left = 0;
        float top = 0;
        float right = sDotPixelWidth;
        float bottom = sDotPixelHeight;
        for (int row = 0; row < mRowSize; ++row) {
            for (int col = 0; col < mColSize; ++col) {
                if (mImgDotMatrix[row][col] == Color.TRANSPARENT) {
                    mCloneArray[row][col] = Color.BLACK;
                } else {
                    mCloneArray[row][col] = mImgDotMatrix[row][col];
                }
                paint.setColor(mCloneArray[row][col]);
                canvas.drawRect(left, top, right, bottom, paint);
                left += sDotPixelWidth;
                right += sDotPixelWidth;
            }
            left = 0;
            right = sDotPixelWidth;
            top += sDotPixelHeight;
            bottom += sDotPixelHeight;
        }
        return bitmap;
    }
	
    private class HandleFilesTask extends AsyncTask<String, Void, String> {

        Bitmap viewBitmap = null;

        @Override
        protected void onPreExecute() {
            showLoadingDlg();
            if (isShowDotMask && mActivity != null) {
                Bitmap tempBitmap = mActivity.takeScreenshot();
				if (tempBitmap != null) {
					Bitmap bitmap = tempBitmap.copy(Bitmap.Config.ARGB_8888, false);
					mActivity.setFakeScreen(bitmap);
				}
            }
            if (mCurrentDrawMode != DrawingMode.MODE_FREESKETCH) {
                for (int row = 0; row < mRowSize; ++row) {
                    for (int col = 0; col < mColSize; ++col) {
                        if (mImgDotMatrix[row][col] == Color.BLACK) {
                            mImgDotMatrix[row][col] = Color.TRANSPARENT;
                        }
                    }
                }
                invalidate();
                if (mActivity != null) {
                    viewBitmap = mActivity.takeScreenshot();
                }
            }

            else {
                viewBitmap = handlePicture();
            }
        }

        @Override
        protected String doInBackground(String... uri) {
            storeColorArray();
            Bitmap bitmap = null;
            if (viewBitmap != null) {
                bitmap = Bitmap.createScaledBitmap(viewBitmap,
                        DotDesignConstants.THUMBNAIL_PHOTO_WIDTH,
                        DotDesignConstants.THUMBNAIL_PHOTO_HEIGHT, false);
                if (!viewBitmap.isRecycled()) {
                    viewBitmap.recycle();
                    viewBitmap = null;
                }
            }
            if (bitmap != null) {
                DotDesignUtil.SaveImagetoInternal(mContext, bitmap, mFileName
                        + DotDesignConstants.THUMBNAIL_FILE_NAME);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            if (mDrawingBitmap != null) {
                DotDesignUtil.SaveImagetoInternal(mContext, mDrawingBitmap, mFileName
                        + DotDesignConstants.ORIGINAL_FILE_NAME);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mActivity != null && mActivity.isDestroyed()) {
                return;
            }
            dismissLoadingDlg();
            if (isUserPressFinsih && mActivity != null) {
                isUserPressFinsih = false;
                mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
        }
    }
	
    private void showLoadingDlg() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
        mLoadingDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
        mLoadingDialog.setMessage(getResources().getString(R.string.saving));
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }
    
    private void dismissLoadingDlg() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }
	
	private void storeColorArray() {
		ColorArray colorarray = new ColorArray();
		colorarray.setArray(mImgDotMatrix);
		FileOutputStream fos = null;
		try {
			fos = mContext.openFileOutput(mFileName + "color", Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(fos);
			if (os != null) {
				os.writeObject(colorarray);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
    private void showHideToolBar(boolean bShow) {
        if (mActivity != null) {
            if (bShow) {
                if (!mActivity.isToolBarShow()) {
                    sendBroadcastToShowHide(bShow);
                }
            }
            else {
                if (mActivity.isToolBarShow()) {
                    sendBroadcastToShowHide(bShow);
                }
            }
        }
    }
    
    private void sendBroadcastToShowHide(boolean bShow) {
        Intent intent = new Intent(ToolBoxService.SHOW_HIDE_TOOL_BAR);
        intent.putExtra(ToolBoxService.EXTRA_SHOW_HIDE, bShow);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
	
	private void closeToolBar() {
	    Intent intent = new Intent(ToolBoxService.USER_START_DRAWING);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}
	
}
