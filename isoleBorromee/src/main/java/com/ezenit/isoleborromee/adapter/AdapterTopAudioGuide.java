package com.ezenit.isoleborromee.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.adapter.AdapterAudioGuide.ViewHolder;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.TopBaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class AdapterTopAudioGuide extends BaseExpandableListAdapter implements Filterable{

	
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG = AdapterTopAudioGuide.class.getName();
	// ===========================================================
	// Fields
	// ===========================================================
	
	private ArrayList<TopBaseAudioGuide> audioGuides;	
	private ArrayList<TopBaseAudioGuide> filteredAudioGuides;
	private ImageLoader		imgLoader;
	
	private final String galleryFolder;
	private final String codiceStr;
	private final String noAudioStr;
	
	private long		 selectAudioGuideId;
	private String		 selectedParentCode;
	
	private AudioGuideFilter filter;
			
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterTopAudioGuide(Context context, Museum museum
			,ArrayList<TopBaseAudioGuide> audioGuides) {
		// TODO Auto-generated constructor stub
		this.audioGuides 	= audioGuides;
		this.imgLoader   	= ImageLoader.getInstance();
		
		this.filteredAudioGuides = new ArrayList<TopBaseAudioGuide>();
		this.filteredAudioGuides.addAll(audioGuides);
		
		this.galleryFolder 	=  AppIsole.getPathForMuseum(context, museum)+File.separator+AppIsole.PAID_GUIDE_GALLERY;
		this.codiceStr 	 	= context.getString(R.string.code);
		this.noAudioStr = context.getString(R.string.nr_audio);
		this.selectedParentCode = new String();
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
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return filteredAudioGuides.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(filteredAudioGuides.size()>0)
		return filteredAudioGuides.get(groupPosition)
				.getChildAudioGuides().size();
		return 0;
	}

	@Override
	public TopBaseAudioGuide getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return filteredAudioGuides.get(groupPosition);
	}

	@Override
	public BaseAudioGuide getChild(int groupPosition, int childPosition) {
		
		return filteredAudioGuides.get(groupPosition)
				.getChildAudioGuides().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		if(filteredAudioGuides.size()>0)
		return filteredAudioGuides.get(groupPosition)
				.hashCode();
		else return -1;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		
		return filteredAudioGuides.get(groupPosition)
				.getChildAudioGuides().get(childPosition).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}
	
	

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
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
			holder.fieldNumber   = (TextView) convertView.findViewById(R.id.fieldChildNumber);
			convertView.setTag(holder);
		}
		else{
			holder =  (ViewHolder) convertView.getTag();
		}
		TopBaseAudioGuide audioGuide = filteredAudioGuides.get(groupPosition);
		
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
		
		int childCount = audioGuide.getChildAudioGuides().size();
		
		if(childCount>0){
			
			holder.fieldNumber.setText(String.valueOf(childCount));
			holder.fieldDetails.setText(Html.fromHtml(noAudioStr+" <b>"+childCount+"</b>"));
			holder.icDisclosure.setImageResource(isExpanded?R.drawable.ic_close_accordian:R.drawable.ic_open_accordian);
						
			boolean selected = isExpanded?false:audioGuide.getCodeNo().equals(selectedParentCode);
			holder.fieldTitle.setSelected(selected);
			holder.fieldDetails.setSelected(selected);
			holder.icDisclosure.setSelected(selected);
			holder.fieldNumber.setSelected(selected);
			if(selected){
				convertView.setBackgroundResource(R.drawable.bg_list_row_white_selected);
			}
			else{
				convertView.setBackgroundResource(R.drawable.bg_list_row_white);
			}
			
		}
		else{
			holder.fieldNumber.setText("");
			holder.fieldDetails.setText(Html.fromHtml(codiceStr+" <b>"+audioGuide.getCodeNo()+"</b>"));
			holder.icDisclosure.setImageResource(R.drawable.ic_list_arrow);
			
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
		}
		
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
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
			convertView.findViewById(R.id.fieldChildNumber).setVisibility(View.INVISIBLE);
			convertView.setTag(holder);
		}
		else{
			holder =  (ViewHolder) convertView.getTag();
		}
		TopBaseAudioGuide parentAudio = filteredAudioGuides.get(groupPosition);
		
		if(parentAudio.hasChildren()){
	
			
			BaseAudioGuide audioGuide = parentAudio.getChildAudioGuides().get(childPosition);
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
			holder.fieldDetails.setText(Html.fromHtml(codiceStr+" <b>"+audioGuide.getCodeNo()+" ("+parentAudio.getTitle()+")</b>"));
			boolean selected = selectAudioGuideId==audioGuide.getId();
			holder.fieldTitle.setSelected(selected);
			holder.fieldDetails.setSelected(selected);
			holder.icDisclosure.setSelected(selected);
			
			if(selected){
				convertView.setBackgroundResource(R.drawable.bg_list_row_white_selected);
			}
			else{
				convertView.setBackgroundResource(R.drawable.bg_list_row_child);
			}
		}
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
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
	
	private class AudioGuideFilter extends Filter
	 {
	 
	   @Override
	   protected FilterResults performFiltering(CharSequence constraint) {
	 
	    constraint = constraint.toString().toLowerCase(AppIsole.getAppLocale());
	    FilterResults result = new FilterResults();
	    if(!TextUtils.isEmpty(constraint))
	    {
	    	ArrayList<TopBaseAudioGuide> filteredItems = new ArrayList<TopBaseAudioGuide>();
	 
	    	for(int i = 0, l = audioGuides.size(); i < l; i++)
	    	{
	    		TopBaseAudioGuide audioGuide = audioGuides.get(i);
	    		
//	    		audioGuide.filterChildren(constraint);
	    		
//	    		if(audioGuide.toString()
//	    				.toLowerCase(AppIsole.getAppLocale())
//	    				.contains(constraint)){	    			
//	    			filteredItems.add(audioGuide);
//	    		}
	    		if(audioGuide.hasChildren()){
	    			ArrayList<BaseAudioGuide> childAudios = audioGuide.getChildAudioGuides();
	    			if(childAudios!=null&&childAudios.size()>0){
	    				for(BaseAudioGuide childAudio:childAudios){
	    					if(childAudio.toString()
	    		    				.toLowerCase(AppIsole.getAppLocale())
	    		    				.contains(constraint))
	    	    			{	    			
	    		    			filteredItems.add(new TopBaseAudioGuide(childAudio));
	    	    			}
	    				}
	    			}
	    		}
	    		else{
	    			if(audioGuide.toString()
		    				.toLowerCase(AppIsole.getAppLocale())
		    				.contains(constraint))
	    			{	    			
		    			filteredItems.add(audioGuide);
	    			}
	    		}
	    		
	    		
	    	}
	    	result.count = filteredItems.size();
	    	result.values = filteredItems;
	    }
	    else
	    {	
//	    	synchronized(this)
//	    	{
	    		for(TopBaseAudioGuide audioGuide:audioGuides){
	    			audioGuide.resetChildren();
	    		}
	    		result.values = audioGuides;
	    		result.count = audioGuides.size();
//	    	}
	    }	
	    return result;
	   }
	 
	   @SuppressWarnings("unchecked")
	   @Override
	   protected void publishResults(CharSequence constraint, 
	     FilterResults results) {
	 		    	    	
	    	 // Now we have to inform the adapter about the new list filtered
	        if (results.count == 0){
	        	filteredAudioGuides.clear();
	        	notifyDataSetChanged();
	        }
	        else {
	        	filteredAudioGuides = (ArrayList<TopBaseAudioGuide>)results.values;
	            notifyDataSetChanged();
	        }
	   }
	  }


	public int getPosition(BaseAudioGuide guide) {
		int i=0;
		for(TopBaseAudioGuide currentGuide:filteredAudioGuides){
			if(currentGuide.getCodeNo().equals(guide.getCodeNo())){
				return i;
			}
			else{
				ArrayList<BaseAudioGuide> baseAudioGuides = currentGuide.getChildAudioGuides();
				for(BaseAudioGuide audioGuide:baseAudioGuides){
					if(audioGuide.getCodeNo().equals(guide.getCodeNo())){
						return i;
					}
				}
			}
			i++;
			
		}
		return -1;
	}

	public void setParentCode(String codeNo) {
		this.selectedParentCode = codeNo;
	}
	
	public String getSelectedParentCode() {
		return selectedParentCode;
	}
	
}
