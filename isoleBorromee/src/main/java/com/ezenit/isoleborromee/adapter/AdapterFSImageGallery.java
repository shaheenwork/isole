package com.ezenit.isoleborromee.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableImgRel.Image;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class AdapterFSImageGallery extends PagerAdapter{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private ArrayList<Image> images;
	private LayoutInflater inflater;
	private String 	 folderPath;
	
	private ImageLoader imgLoader = ImageLoader.getInstance();
	
	private OnImageClickListener imageClickListener;
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterFSImageGallery(Context context,Museum museum,ArrayList<Image> images) 
	{
		this.images     = images;
		this.folderPath = AppIsole.getPaidAudioGalleryFolder(context, museum);
		
		if(!folderPath.startsWith("file://"))
			this.folderPath = "file://"+folderPath;
	
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setOnImageClickListener(OnImageClickListener imageClickListener) {
		this.imageClickListener = imageClickListener;
	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public int getCount() {
		return this.images.size();
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) 
	{
	    return view == ((RelativeLayout) object);
	}
		
	@Override
	public Object instantiateItem(ViewGroup container, final int position)
	{	
		initInflater(container);
	    View root = inflater.inflate(R.layout.fr_photogallery_image, container,false);
	    ImageView imgView = (ImageView) root.findViewById(R.id.imgDisplay);
	    
	    Image image = images.get(position);
	   
	    imgLoader.displayImage(folderPath+File.separator+image.getName(), imgView, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
				
			}
		});
	    imgView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(imageClickListener!=null)
					imageClickListener.onImageClicked();
			}
		});
	          
	    container.addView(root);
	   
	    
	   return root;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) 
	{
	    ((ViewPager) container).removeView((RelativeLayout) object);
	}
	// ===========================================================
	// Methods
	// ===========================================================
	private void initInflater(View container){
		if(inflater==null)
	        inflater = LayoutInflater.from(container.getContext());
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	
		
		 
		
	public interface OnImageClickListener{
		public void onImageClicked();
	}
		
		
		
		
		
	     
	     
		
		
		

		

	

}
