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
import java.util.Iterator;
import java.util.Vector;

import com.htc.dotdesign.R;
import com.htc.dotdesign.util.DotDesignUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;


public class DeleteThemeActivity extends GridViewActivityBase implements OnClickListener{
	private static final String LOG_PREFIX = "[DeleteThemeActivity] ";
	
    private final static int MENU_SELECT_ALL = Menu.FIRST;
    private final static int MENU_DESELECT_ALL = Menu.FIRST + 1;
    private final static String CANCEL_TAG = "cancel";
    private final static String DELETE_TAG = "delete";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (mDeletebutton != null) {
            mDeletebutton.setOnClickListener(this);
            mDeletebutton.setTag(DELETE_TAG);
            mDeletebutton.setEnabled(false);
            mDeletebutton.setBackgroundResource(R.drawable.footer_selector);
        }
        if (mCanclebutton != null) {
            mCanclebutton.setBackgroundResource(R.drawable.footer_selector);
            mCanclebutton.setOnClickListener(this);
            mCanclebutton.setTag(CANCEL_TAG);
        }
        if (mFooter != null) {
            mFooter.setVisibility(View.VISIBLE);
        }
		initActionBar();
		prepareitems();
		if (mGrid != null) {
			mGrid.setAdapter(mAdapter);
			mGrid.setOnItemClickListener(mOnClickListener);
			mGrid.setSelector(R.drawable.grid_selector);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int delete_cnt = 0;
        for (int i = 0; i < mInformationItems.size(); i++) {
            if (mInformationItems.get(i).IsDelete()) {
                delete_cnt++;
            }
        }

        if (menu != null) {
            if (delete_cnt == 0) {
                menu.findItem(MENU_SELECT_ALL).setEnabled(true);
                menu.findItem(MENU_DESELECT_ALL).setEnabled(false);
            } else if (delete_cnt == mInformationItems.size()) {
                menu.findItem(MENU_SELECT_ALL).setEnabled(false);
                menu.findItem(MENU_DESELECT_ALL).setEnabled(true);
            } else {
                menu.findItem(MENU_SELECT_ALL).setEnabled(true);
                menu.findItem(MENU_DESELECT_ALL).setEnabled(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SELECT_ALL, 11, R.string.theme_menu_select_all);
        menu.add(0, MENU_DESELECT_ALL, 12, R.string.theme_menu_deselect_all);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SELECT_ALL:
            	doSelectAll();
                break;
            case MENU_DESELECT_ALL:
            	doDeSelectAll();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        for (File lst : fileList) {
            String filename = "";
            ThemeInformationItem item = new ThemeInformationItem();
            String thumbPngName = lst.getName();
            String[] temp = thumbPngName.split("_");
            if (temp.length > 0) {
                filename = temp[0] + "_";
            }
            String photoPath = lst.getAbsolutePath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap thumbnail = BitmapFactory.decodeFile(photoPath, options);
            item.setThumbnailBmp(thumbnail);
            item.setFileName(filename);
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
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			if (position < mInformationItems.size()) {
				ThemeInformationItem item = mInformationItems.get(position);
				item.setDelete(!item.IsDelete());
				ImageView iv_mask = (ImageView) arg1.findViewById(R.id.panel_picker_mask);
				ImageView iv = (ImageView) arg1.findViewById(R.id.panel_picker_delete);

				if (iv != null && iv_mask != null) {
					if (item.IsDelete()) {
						iv_mask.setVisibility(View.VISIBLE);
						iv.setVisibility(View.VISIBLE);
					} else {
						iv_mask.setVisibility(View.GONE);
						iv.setVisibility(View.GONE);
					}
				}
				updateDeleteButton();
			} else {
			}
		}
	};
	
	private void doSelectAll() {
        for (int i = 0; i < mInformationItems.size(); i++) {
            mInformationItems.get(i).setDelete(true);
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        updateDeleteButton();
    }

    private void doDeSelectAll() {
        for (int i = 0; i < mInformationItems.size(); i++) {
        	mInformationItems.get(i).setDelete(false);
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        updateDeleteButton();
    }
    
    private void initActionBar() {
        mActionBar = getActionBar();
        if(mActionBar != null) {
        	mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(getResources().getString(R.string.title_delete_theme));
            mActionBar.show();	
        }
    }
    
    private void updateDeleteButton() {
        if (mDeletebutton != null && mInformationItems != null) {
            int cnt = 0;
            for (int i = 0; i < mInformationItems.size(); i++) {
                if (mInformationItems.get(i).IsDelete()) {
                    cnt++;
                }
            }
            if (cnt > 0) {
                String text = getResources().getString(R.string.btn_delete);
                text += "(" + cnt + ")";
                mDeletebutton.setText(text);
                mDeletebutton.setEnabled(true);
            } else {
            	mDeletebutton.setText(R.string.btn_delete);
            	mDeletebutton.setEnabled(false);
            }
        }
    }
    
    private void doDeleteTheme() {
        for (Iterator<ThemeInformationItem> iter = mInformationItems.iterator(); iter.hasNext();) {
            ThemeInformationItem infoItem = iter.next();
            if (infoItem.IsDelete()) {
                File file = null;
                boolean isDeleteThumb = false;
                boolean isDeleteOriginal = false;
                boolean isDeleteColor = false;
                String fileName = infoItem.getFileName();
                String thumbnailfile = fileName + DotDesignConstants.THUMBNAIL_FILE_NAME;
                file = getFileStreamPath(thumbnailfile);
                if (file != null && file.exists()) {
                    isDeleteThumb = file.delete();
                }
                String originalfile = fileName + DotDesignConstants.ORIGINAL_FILE_NAME;
                file = getFileStreamPath(originalfile);
                if (file != null && file.exists()) {
                    isDeleteOriginal = file.delete();
                }
                String colorfile = fileName + DotDesignConstants.COLOR_FILE_NAME;
                file = getFileStreamPath(colorfile);
                if (file != null && file.exists()) {
                    isDeleteColor = file.delete();
                }
                if (isDeleteThumb || isDeleteOriginal || isDeleteColor) {
                    Bitmap bitmap = infoItem.getThumbnailBmp();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                    iter.remove();
                }
            }
        }
    }

	@Override
	public void onClick(View view) {
		if (view.getTag() == CANCEL_TAG) {
			finish();
		} else if (view.getTag() == DELETE_TAG) {
			doDeleteTheme();
			setResult(RESULT_OK);
			finish();
		}

	}
}
