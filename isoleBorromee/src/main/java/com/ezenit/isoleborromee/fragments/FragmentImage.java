package com.ezenit.isoleborromee.fragments;

import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class FragmentImage extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String ARG_PATH = "com.ezenit.isoleborromee.fragments.FragmentImage.ARG_PATH";
	// ===========================================================
	// Fields
	// ===========================================================
	private static ImageLoader imgLoader = ImageLoader.getInstance();
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static FragmentImage getInstance(String path){
		FragmentImage fragment = new FragmentImage();
		Bundle args = new Bundle();
		args.putString(ARG_PATH, path);
		fragment.setArguments(args);
		return fragment;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_image_view, null);
		
		ImageView img = (ImageView) root.findViewById(R.id.img);
		final ProgressBar progress = (ProgressBar) root.findViewById(R.id.imgProgress);
		
		String filePath = getArguments().getString(ARG_PATH);
		
		imgLoader.displayImage(filePath, img, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
				progress.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				progress.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				// TODO Auto-generated method stub
				progress.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				progress.setVisibility(View.INVISIBLE);
			}
		});
		
		return root;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
