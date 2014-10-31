package com.bluedot.pointapp.list;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluedotinnovation.android.pointapp.R;

public class CheckListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private ArrayList<HeaderItem> mGroups;
	private ArrayList<ArrayList<ListItem>> mChildren;

	public CheckListAdapter(Context context, ArrayList<HeaderItem> groups,
			ArrayList<ArrayList<ListItem>> children) {
		mContext = context;
		mGroups = groups;
		mChildren = children;
	}

	public void setZonesAndFences(ArrayList<HeaderItem> groups,
			ArrayList<ArrayList<ListItem>> children) {
		mGroups = groups;
		mChildren = children;
		notifyDataSetChanged();
	}

	@Override
	public ListItem getChild(int i, int j) {
		return mChildren.get(i).get(j);
	}

	@Override
	public long getChildId(int i, int j) {
		return 0;
	}

	@Override
	public View getChildView(int i, int j, boolean isLastChild, View view,
			ViewGroup viewGroup) {
		View v = view;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_row, viewGroup, false);
		}

		TextView text = (TextView) v.findViewById(R.id.txt_fence_name);
		text.setText(getChild(i, j).toString());
		ImageView imgView = (ImageView) v.findViewById(R.id.img_checked_in);
		imgView.setVisibility(getChild(i, j).isCheckedIn() ? View.VISIBLE
				: View.INVISIBLE);
		return v;
	}

	@Override
	public int getChildrenCount(int count) {
		return mChildren.get(count).size();
	}

	@Override
	public HeaderItem getGroup(int i) {
		return mGroups.get(i);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int id) {
		return id;
	}

	@Override
	public View getGroupView(int i, boolean isExpanded, View view,
			ViewGroup viewGroup) {
		View v = view;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_header, viewGroup, false);
		}

		TextView text = (TextView) v.findViewById(R.id.txt_zone_name);
		text.setText(getGroup(i).toString());
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int i, int j) {
		return false;
	}

}
