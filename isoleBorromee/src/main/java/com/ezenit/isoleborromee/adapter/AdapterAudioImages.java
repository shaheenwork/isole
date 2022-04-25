package com.ezenit.isoleborromee.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableImgRel.Image;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.fragments.FragmentImage;

public class AdapterAudioImages extends FragmentPagerAdapter{

	
	

	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private ArrayList<Image> images;
	
	private String galleryPath;
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterAudioImages(Context context,FragmentManager fm
			,Museum museum,ArrayList<Image> images) {
		super(fm);
		this.galleryPath = AppIsole.getPathForMuseum(context, museum)
				+File.separator+AppIsole.PAID_GUIDE_GALLERY;
		this.images = images;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.size();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return images.get(position).hashCode();
	}


	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return FragmentImage.getInstance("file://"+galleryPath
				+File.separator+images.get(position).getName());
	}
	
	
	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
