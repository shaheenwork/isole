package com.ezenit.isoleborromee.adapter;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezenit.customview.TouchImageView;
import com.ezenit.isoleborromee.model.IImage;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class FullScreenImageAdapter extends PagerAdapter {

	private ArrayList<? extends IImage> images;
	private LayoutInflater inflater;
	private OnImageClickListener imgClickListener;
	private String 	 folderPath;
	
	private ImageLoader imgLoader = ImageLoader.getInstance();
	
	// constructor
	public FullScreenImageAdapter(String folderPath,ArrayList<? extends IImage> imagePaths) 
	{
		this.images = imagePaths;
		
		if(!folderPath.startsWith("file://"))
			this.folderPath = "file://"+folderPath;
	
	}
	
	 public void setOnImageClickListener(OnImageClickListener imgClickListener) 
	 {
			this.imgClickListener = imgClickListener;
	 }
	
	
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
        TouchImageView imgView = (TouchImageView) root.findViewById(R.id.imgDisplay);
        IImage image = images.get(position);
        
        
        final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        final TextView    fieldImageStatus = (TextView) root.findViewById(R.id.fieldImageStatus);
        
        imgLoader.displayImage(folderPath+File.separator+image.getImgName(), imgView, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				progressBar.setVisibility(View.VISIBLE);	
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				progressBar.setVisibility(View.VISIBLE);	
				fieldImageStatus.setVisibility(View.VISIBLE);
				fieldImageStatus.setText(R.string.image_unavailable);
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				progressBar.setVisibility(View.GONE);				
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				fieldImageStatus.setVisibility(View.VISIBLE);
				fieldImageStatus.setText(R.string.image_unavailable);
				
			}
		});
        
        imgView.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View arg0) 
			{
				if(imgClickListener!=null)
					imgClickListener.onImageClicked(images.get(position));
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
	
	
	
     
     public interface OnImageClickListener
     {
	   public void onImageClicked(IImage image);
     }
	
	
	
	private void initInflater(View container){
		if(inflater==null)
	        inflater = LayoutInflater.from(container.getContext());
	}
	

}
