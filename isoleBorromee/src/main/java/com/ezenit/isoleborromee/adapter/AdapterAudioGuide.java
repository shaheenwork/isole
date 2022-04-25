package com.ezenit.isoleborromee.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class AdapterAudioGuide extends BaseAdapter implements Filterable{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private ArrayList<BaseAudioGuide> audioGuides;
	private ArrayList<BaseAudioGuide> filteredAudioGuides;
	private ImageLoader imgLoader;
	
	private final String galleryFolder;
	
	private AudioGuideFilter filter;
	
	private long		 selectAudioGuideId;
	
	private final String codiceStr;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterAudioGuide(Context context, Museum museum
			,String langShort,ArrayList<BaseAudioGuide> audioGuides) {
		this.imgLoader 		= ImageLoader.getInstance();
		this.galleryFolder 	=  AppIsole.getPathForMuseum(context, museum)+File.separator+AppIsole.PAID_GUIDE_GALLERY;
		this.audioGuides 	= new ArrayList<BaseAudioGuide>();
		this.audioGuides.addAll(audioGuides);
		this.filteredAudioGuides = new ArrayList<BaseAudioGuide>();
		this.filteredAudioGuides.addAll(audioGuides);
		this.codiceStr = context.getString(R.string.code)+": ";
	}
	
	public void changeSource(ArrayList<BaseAudioGuide> guides){
		audioGuides.clear();
		filteredAudioGuides.clear();
		audioGuides.addAll(guides);
		filteredAudioGuides.addAll(guides);
		notifyDataSetChanged();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public void setSelection(long id){
		this.selectAudioGuideId = id;
		notifyDataSetChanged();
	}
	
	public void setSelection(BaseAudioGuide guide){
		this.selectAudioGuideId = guide.getId();
		notifyDataSetChanged();
	}
	
	public long getSelectAudioGuideId() {
		return selectAudioGuideId;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return filteredAudioGuides.size();
	}
	@Override
	public BaseAudioGuide getItem(int position) {
		// TODO Auto-generated method stub
		return filteredAudioGuides.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return filteredAudioGuides.get(position).hashCode();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder =null;
		if(convertView==null){
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.row_audio_guide, parent,false);
			
			holder = new ViewHolder();
			holder.fieldDetails = (TextView) convertView.findViewById(R.id.fieldDetails);
			holder.fieldTitle   = (TextView) convertView.findViewById(R.id.fieldTitle);
			holder.progressbar  = (ProgressBar) convertView.findViewById(R.id.imgProgress);
			holder.imgAudioGuide = (ImageView) convertView.findViewById(R.id.imgAudioGuide);
			holder.icDisclosure  = (ImageView) convertView.findViewById(R.id.imgDetail);
			convertView.setTag(holder);
		}
		else{
			holder =  (ViewHolder) convertView.getTag();
		}
		BaseAudioGuide audioGuide = filteredAudioGuides.get(position);
		
		final ProgressBar progressbar = holder.progressbar;
		imgLoader.displayImage("file://"+galleryFolder+File.separator+audioGuide.getCodeNo()+"-01.jpg"
			, holder.imgAudioGuide, new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					progressbar.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					progressbar.setVisibility(View.INVISIBLE);
					((ImageView)view).setImageResource(R.drawable.bg_missing_pic);
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					progressbar.setVisibility(View.INVISIBLE);
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub
					progressbar.setVisibility(View.INVISIBLE);
				}
			});
		holder.fieldTitle.setText(audioGuide.getTitle());
		holder.fieldDetails.setText(codiceStr+audioGuide.getCodeNo());
		boolean selected = selectAudioGuideId==audioGuide.getId();
		holder.fieldTitle.setSelected(selected);
		holder.fieldDetails.setSelected(selected);
		holder.icDisclosure.setSelected(selected);
		if(selected){
			convertView.setBackgroundResource(R.drawable.bg_list_row_white_selected);
		}
		else{
			convertView.setBackgroundResource(R.drawable.bg_list_row_white);
		}
		return convertView;
	}

	@Override
	public Filter getFilter() {
		if(filter==null)
			filter = new AudioGuideFilter();
		return filter;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	static class ViewHolder{
		ImageView imgAudioGuide;
		TextView  fieldTitle;
		TextView  fieldDetails;
		ProgressBar progressbar;
		ImageView icDisclosure;
		TextView  fieldNumber;
		
	}
	
	
	private class AudioGuideFilter extends Filter
	 {
	 
	   @Override
	   protected FilterResults performFiltering(CharSequence constraint) {
	 
	    constraint = constraint.toString().toLowerCase(AppIsole.getAppLocale());
	    FilterResults result = new FilterResults();
	    if(constraint != null && constraint.toString().length() > 0)
	    {
	    	ArrayList<BaseAudioGuide> filteredItems = new ArrayList<BaseAudioGuide>();
	 
	    	for(int i = 0, l = audioGuides.size(); i < l; i++)
	    	{
	    		BaseAudioGuide audioGuide = audioGuides.get(i);
	    		if(audioGuide.toString().toLowerCase(AppIsole.getAppLocale()).contains(constraint))
	    			filteredItems.add(audioGuide);
	    		}
	    		result.count = filteredItems.size();
	    		result.values = filteredItems;
	    	}
	    	else
	       {	
	    		synchronized(this)
	    		{
	    			result.values = audioGuides;
	    			result.count = audioGuides.size();
	    		}
	    	}	
	    	return result;
	   }
	 
	   @SuppressWarnings("unchecked")
	   @Override
	   protected void publishResults(CharSequence constraint, 
	     FilterResults results) {
	 
		    filteredAudioGuides = (ArrayList<BaseAudioGuide>)results.values;
	    	notifyDataSetChanged();
	   }
	  }


	public int getPosition(BaseAudioGuide guide) {
		int i=0;
		for(BaseAudioGuide currentGuide:filteredAudioGuides){
			if(currentGuide.getCodeNo().equals(guide.getCodeNo())){
				return i;
			}
			i++;
			
		}
		return -1;
	}



	

	
	

	
}
