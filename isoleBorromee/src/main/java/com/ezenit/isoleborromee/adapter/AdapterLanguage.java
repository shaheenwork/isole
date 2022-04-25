package com.ezenit.isoleborromee.adapter;

import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezenit.isoleborromee.R;

public class AdapterLanguage extends BaseAdapter{

	
	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private String[] listLanguage;
	private int		 selection   = NO_SELECTION;
	private int 	 layoutRowId;
	// ===========================================================
	// Constructors
	// ===========================================================

	public AdapterLanguage(Context context,boolean trimDivider) {
		this.listLanguage = context
								.getResources()
								.getStringArray(R.array.languages);
		initRowLayout(trimDivider);
	}
	
	public AdapterLanguage(Context context) {
		this.listLanguage = context
								.getResources()
								.getStringArray(R.array.languages);
		
		initRowLayout(false);
	}
	
	public AdapterLanguage(String[] listLanguage,boolean trimDivider){
		this.listLanguage = listLanguage;
		initRowLayout(trimDivider);
	}
	
	
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setSelection(int selection) {
		this.selection = selection;
		notifyDataSetChanged();
	}
	
	public int getSelection() {
		return selection;
	}
	
	public String getSelectedItem(){
		if(selection!=NO_SELECTION&&selection<listLanguage.length)
		{
			Log.d(getClass().getName(),"selected lang :" + listLanguage[selection]);
			return listLanguage[selection];
		}
		return null;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listLanguage.length;
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return listLanguage[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return listLanguage[position].hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView==null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(layoutRowId, parent,false);
			
			holder = new ViewHolder();
			holder.fieldLanguage = (TextView) convertView.findViewById(R.id.fieldLanguage);
			holder.imgSelected   = (ImageView) convertView.findViewById(R.id.imgTick);
			
			if(layoutRowId==R.layout.row_fr_language)
				holder.divider  = convertView.findViewById(R.id.divider);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		
		if(selection==position){
			holder.imgSelected.setVisibility(View.VISIBLE);
		}
		else{
			holder.imgSelected.setVisibility(View.INVISIBLE);
		}
		
		holder.fieldLanguage.setText(listLanguage[position]);
		
		if(holder.divider!=null){
			if(position==listLanguage.length-1)
				holder.divider.setVisibility(View.GONE);
			else
				holder.divider.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	private void initRowLayout(boolean trimDivider){
		if(trimDivider)
			layoutRowId = R.layout.row_fr_language;
		else
			layoutRowId = R.layout.row_language;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	static class ViewHolder{
		public TextView  fieldLanguage;
		public ImageView imgSelected;
		public View		 divider;
	}

	public void setSelection(String language) {
		int size = listLanguage.length;
		for(int i=0;i<size;i++){
			if(language.equals(listLanguage[i].trim().toLowerCase(Locale.UK))){
				selection = i;
				break;
			}
		}
	}
}
