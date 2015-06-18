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

import java.util.Vector;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.htc.dotdesign.R;

public class GridViewActivityBase extends Activity{
    protected GridView mGrid = null;
    protected ViewAdapter mAdapter = null;
    protected ActionBar mActionBar = null;
    protected TextView mEmptyTextView = null;
    protected LinearLayout mFooter = null;
    protected TextView mDeletebutton = null;
    protected TextView mCanclebutton = null;
    protected Vector<ThemeInformationItem> mInformationItems = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.themes_gridview);
        if (mAdapter == null) {
            mAdapter = new ViewAdapter(this);
        }
        mGrid = (GridView) findViewById(R.id.panel_picker_grid);
        mEmptyTextView = (TextView)findViewById(R.id.emptyview);
        mFooter = (LinearLayout)findViewById(R.id.footer);
        mDeletebutton = (TextView)findViewById(R.id.btn_delete);
        mCanclebutton = (TextView)findViewById(R.id.btn_cancel);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInformationItems != null) {
            for (int i = 0; i < mInformationItems.size(); i++) {
                ThemeInformationItem item = mInformationItems.get(i);
                Bitmap bitmap = item.getThumbnailBmp();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            mInformationItems.clear();
            mInformationItems = null;
        }
    }

    protected class ViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        
        public ViewAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return (mInformationItems == null) ? 0 :mInformationItems.size();
        }

        public Object getItem(int position) {
            return mInformationItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final ThemeInformationItem item = mInformationItems.get(position);
            if (convertView == null) {
                // Regular items
                convertView = inflater.inflate(R.layout.themes_gridview_item,parent,false);
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.panel_item_title);
                holder.iv = (ImageView) convertView.findViewById(R.id.panel_item_preview);
                holder.iv.setBackgroundColor(android.graphics.Color.BLACK);

                // Delete
                holder.ivDelete = (ImageView) convertView.findViewById(R.id.panel_picker_delete);
                holder.ivDeleteMask = (ImageView) convertView.findViewById(R.id.panel_picker_mask);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.iv.setImageBitmap(item.getThumbnailBmp());
            if (item.IsDelete()) {
                holder.ivDeleteMask.setVisibility(View.VISIBLE);
                holder.ivDelete.setVisibility(View.VISIBLE);
            } else {
                holder.ivDeleteMask.setVisibility(View.GONE);
                holder.ivDelete.setVisibility(View.GONE);
            }
            return convertView;
        }
        
        private class ViewHolder {
            TextView tv;
            ImageView iv;
            ImageView ivDelete;
            ImageView ivDeleteMask;
        }
    }
}
