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

import java.util.Locale;
import java.util.Vector;

import com.htc.dotdesign.R;
import com.htc.dotdesign.DotDesignConstants.DrawingMode;
import com.htc.dotdesign.util.DotDesignUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class DotDesignTemplate extends GridViewActivityBase{
    private static final String LOG_PREFIX = "[DotDesignTemplate] ";
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

    private void initActionBar() {
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(getResources().getString(R.string.app_name));
            mActionBar.setSubtitle(getResources().getString(R.string.choose_template));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void prepareitems() {
        if (mInformationItems == null) {
            mInformationItems = new Vector<ThemeInformationItem>();
        } else {
            mInformationItems.clear();
        }
        AssetManager assetManager = getResources().getAssets();
        for (int idx = 1; idx <= 19; ++idx) {
            if (DotDesignTemplate.this.isDestroyed()) {
                Log.d(DotDesignConstants.LOG_TAG, LOG_PREFIX + "Activity is entering destory");
                break;
            }
            ThemeInformationItem item = new ThemeInformationItem();
            Intent intent = new Intent(this, DrawingActivity.class);
            if (idx == 1) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_01);
            } else if (idx == 2) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_02);
            } else if (idx == 3) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_03);
            } else if (idx == 4) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_04);
            } else if (idx == 5) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_05);
            } else if (idx == 6) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_06);
            } else if (idx == 7) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_07);
            } else if (idx == 8) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_08);
            } else if (idx == 9) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_09);
            } else if (idx == 10) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_10);
            } else if (idx == 11) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_11);
            } else if (idx == 12) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_12);
            } else if (idx == 13) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_13);
            } else if (idx == 14) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_14);
            } else if (idx == 15) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_15);
            } else if (idx == 16) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_16);
            } else if (idx == 17) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_17);
            } else if (idx == 18) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_18);
            } else if (idx == 19) {
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        R.drawable.dot_design_template_19);
            }
            intent.putExtra(DotDesignConstants.EXTRA_CURRENT_MODE, DrawingMode.MODE_DOTDESIGNTEMPLATE);
            item.setLauchIntent(intent);
            String fileName = String.format(Locale.US, "%s%02d%s", "dot_design_preview_", idx,
                    ".png");
            item.setThumbnailBmp(DotDesignUtil.getBitmapFromAsset(assetManager, fileName));
            if (mInformationItems != null) {
                mInformationItems.add(item);
            }
        }
    }
    
    protected AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            if (position < mInformationItems.size()) {
                ThemeInformationItem item = mInformationItems.get(position);
                Intent intent = item.getLaunchIntent();
                intent.putExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID,
                        intent.getIntExtra(DotDesignConstants.EXTRA_DOTDESIGN_TEMPLATE_ID, 0));
                setResult(RESULT_OK, intent);
                finish();
            } else {
            }
        }
    };
    
    private class ReadFilesTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            
        }

        @Override
        protected String doInBackground(String... value) {
            prepareitems();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (DotDesignTemplate.this.isDestroyed() || mEmptyTextView == null) {
                return;
            }
            if (mInformationItems == null || mInformationItems.size() == 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.GONE);
            }
            
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDlg();
    }
}
