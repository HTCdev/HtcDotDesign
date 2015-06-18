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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import com.htc.dotdesign.DotDesignConstants.DrawingMode;
import com.htc.dotdesign.util.DotDesignUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class MainPage extends GridViewActivityBase{
	private static final String LOG_PREFIX = "[MainPage] ";
	private final static int MENU_DELETE_THEME = Menu.FIRST;
    private final static int MENU_ADD_THEME = Menu.FIRST + 1;
    private AlertDialog mAlertDialog = null;
    private boolean mbDataIsChange = false;
    
    private ProgressDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar();
		new ReadFilesTask().execute("");
		if (mGrid != null) {
			mGrid.setAdapter(mAdapter);
			mGrid.setOnItemClickListener(mOnClickListener);
			mGrid.setSelector(R.drawable.grid_selector);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mbDataIsChange) {
			new ReadFilesTask().execute("");	
			mbDataIsChange = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissLoadingDlg();
	}
	
	private void initActionBar() {
        mActionBar = getActionBar();
        if(mActionBar != null) {
        	mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setTitle(getResources().getString(R.string.app_name));
            mActionBar.setSubtitle(getResources().getString(R.string.my_sketch));
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.show();	
        }
    }
	
	private void showLoadingDlg() {
	    if(mDialog != null) {
	        mDialog.dismiss();
	        mDialog = null;
	    }
    	mDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_DARK);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem addMenuItem = menu.add(0,MENU_ADD_THEME,9,"");
        if(addMenuItem != null) {
        	addMenuItem.setIcon(R.drawable.dot_design_btn_add_dark).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);   
        }
        menu.add(0, MENU_DELETE_THEME, 10, R.string.theme_menu_delete);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mInformationItems == null) {
			return false;
		}
		if (mInformationItems.size() <= 0) {
			menu.findItem(MENU_DELETE_THEME).setEnabled(false);
		}
		else {
			menu.findItem(MENU_DELETE_THEME).setEnabled(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case MENU_DELETE_THEME:
                launchDeleteThemePage();
                break;
            case MENU_ADD_THEME:
                selectDrawerSourceDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	private void launchDeleteThemePage() {
		Intent intent = new Intent();
		intent.setClass(this, DeleteThemeActivity.class);
		startActivityForResult(intent,DotDesignConstants.REQUEST_START_DELETE_PAGE);
	}
	
	private void prepareitems() {
		Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "prepareitems");
		if (mInformationItems == null) {
            mInformationItems = new Vector<ThemeInformationItem>();
        } else {
            mInformationItems.clear();
        }
		ArrayList<ThemeInformationItem> tempItemList = new ArrayList<ThemeInformationItem>();
		File orignfile = getFilesDir();
		String dirPath = orignfile.getAbsolutePath();
        File dPath = new File(dirPath);
        ArrayList<File> fileList = DotDesignUtil.getListFiles(dPath);
        Intent intent;
        for (File lst : fileList) {
            if (MainPage.this.isDestroyed()) {
                Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "Activity is entering destory");
                break;
            }
        	String filename = "";
        	ThemeInformationItem item = new ThemeInformationItem();
        	String thumbPngName = lst.getName();
        	String [] temp = thumbPngName.split("_");
        	if(temp.length > 0) {
        		filename = temp[0] + "_";
        	}
            String photoPath = lst.getAbsolutePath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap thumbnail = BitmapFactory.decodeFile(photoPath, options);
            item.setThumbnailBmp(thumbnail);
            intent = new Intent(this, DrawingActivity.class);
            intent.putExtra(DotDesignConstants.EXTRA_FILE_NAME, filename);
            intent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_GRIDVIEW);
            item.setLauchIntent(intent);
            tempItemList.add(item);
        }
        if (tempItemList != null) {
            Collections.reverse(tempItemList);
        }
        if (mInformationItems != null) {
            for (ThemeInformationItem items : tempItemList) {
                mInformationItems.add(items);
            }
        }
	}
	
	private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            if (position < mInformationItems.size()) {
                ThemeInformationItem item = mInformationItems.get(position);
                MainPage.this.startActivityForResult(item.getLaunchIntent(), DotDesignConstants.REQUEST_START_DRAW_VIEW);
            } else {
                Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "Can't launch Sketch");
            }
        }
    };
    
    private void selectDrawerSourceDialog() {
    	if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
		String[] selectPhotoFrom = getResources().getStringArray(
				R.array.items_select_template_from);
		mAlertDialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
				.setTitle(R.string.title_choose_template_from)
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						if (mAlertDialog != null) {
							mAlertDialog.dismiss();
							mAlertDialog = null;
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
								    Intent startintent = new Intent(MainPage.this, DotDesignTemplate.class);
								    startActivityForResult(startintent,DotDesignConstants.REQUEST_GET_TEMPLATE);
								    break;
								}
								case 1: {
								    Intent startintent = new Intent(MainPage.this, DrawingActivity.class);
                                    startintent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_FREESKETCH);
                                    startintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivityForResult(startintent, DotDesignConstants.REQUEST_START_DRAW_VIEW);
                                    break;   
								}
								case 2: {
								    DotDesignUtil.selectTemplateFromGallery(MainPage.this);
                                    break;   
								}
								case 3: {
								    DotDesignUtil.selectTemplateFromCamera(MainPage.this);
                                    break;   
								}
								default:
									break;
								}
							}
						}).create();
		mAlertDialog.show();
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "onActivityResult:" + requestCode);
        switch (requestCode) {
            case DotDesignConstants.REQUEST_GET_PHOTO_FROM_GALLERY: {
                DotDesignUtil.getCropPhotoByGallery(this,data);
                break;
            }
            case DotDesignConstants.REQUEST_GET_PHOTO_FROM_CAMERA: {
                DotDesignUtil.getCropPhotoByCamera(this,data);
                break;
            }
            case DotDesignConstants.REQUEST_START_DRAW_VIEW: {
                mbDataIsChange = true;
                Toast.makeText(this, getResources().getString(R.string.toast_message),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case DotDesignConstants.REQUEST_START_DELETE_PAGE: {
                mbDataIsChange = true;
                break;
            }
            case DotDesignConstants.REQUEST_CROP_IMG_IN_GALLERY_FROM_CAMERA: {
                if (data == null)
                    return;
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                File[] fArray = f.listFiles();
                if (f.isDirectory() && (fArray != null)) {
                	for (File temp : fArray) {
                        if (temp != null && temp.getName().equals(DotDesignConstants.TEMP_FILE_NAME)) {
                            f = temp;
                            break;
                        }
                    }
                    Intent startintent = new Intent(MainPage.this, DrawingActivity.class);
                    startintent.putExtra(DotDesignConstants.EXTRA_IMAGE_PATH, f.getAbsolutePath());
                    startintent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_CAMERA);
                    startintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivityForResult(startintent, DotDesignConstants.REQUEST_START_DRAW_VIEW);	
                }
                break;
            }
            case DotDesignConstants.REQUEST_CROP_IMG_IN_GALLERY: {
                if (data == null)
                    return;
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                File[] fArray = f.listFiles();
                if (f.isDirectory() && (fArray != null)) {
                	for (File temp : fArray) {
                        if (temp != null && temp.getName().equals(DotDesignConstants.TEMP_FILE_NAME)) {
                            f = temp;
                            break;
                        }
                    }
                    Intent startintent = new Intent(MainPage.this, DrawingActivity.class);
                    startintent.putExtra(DotDesignConstants.EXTRA_IMAGE_PATH, f.getAbsolutePath());
                    startintent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_GALLERY);
                    startintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivityForResult(startintent, DotDesignConstants.REQUEST_START_DRAW_VIEW);	
                }
                break;
            }
            case DotDesignConstants.REQUEST_GET_TEMPLATE: {
                if (data != null) {
                    int templateid = data.getIntExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID, 0);
                    if (templateid != 0) {
                        Intent startintent = new Intent(MainPage.this, DrawingActivity.class);
                        startintent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_DOTDESIGNTEMPLATE);
                        startintent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,templateid);
                        startActivityForResult(startintent, DotDesignConstants.REQUEST_START_DRAW_VIEW);
                    }
                }
                break;
            }
        }
    }
	
	private class ReadFilesTask extends AsyncTask<String, Void, String> {
    	
    	@Override
		protected void onPreExecute() {
    		showLoadingDlg();
		}
    	
		@Override
        protected String doInBackground(String... value) {
            if (MainPage.this.isDestroyed()) {
                return null;
            }
            prepareitems();
            return null;
        }

		@Override
		protected void onPostExecute(String result) {
			if(MainPage.this.isDestroyed() || mEmptyTextView == null) {
				return;
			}
			if(mInformationItems == null || mInformationItems.size() == 0) {
				mEmptyTextView.setVisibility(View.VISIBLE);
			} else {
				mEmptyTextView.setVisibility(View.GONE);
			}
			dismissLoadingDlg();
			if(mAdapter != null) {
				mAdapter.notifyDataSetChanged();	
			}
		}
    	
    }	
}
