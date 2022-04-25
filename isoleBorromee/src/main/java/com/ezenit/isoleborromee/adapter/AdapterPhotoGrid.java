package com.ezenit.isoleborromee.adapter;


import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery.GalleryImage;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AdapterPhotoGrid extends BaseAdapter {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG = AdapterPhotoGrid.class.getName();
	// ===========================================================
	// Fields
	// ===========================================================
	private ImageLoader imgLoader;
	private ArrayList<GalleryImage> images;
	private String galleryPath;
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterPhotoGrid(Context context,Museum museum,ArrayList<GalleryImage> images) {
		this.imgLoader  = ImageLoader.getInstance();
		
		this.galleryPath = AppIsole.getGalleryPath(context, museum);
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
		return images.size();
	}

	@Override
	public GalleryImage getItem(int position) {
		return images.get(position);
	}

	@Override
	public long getItemId(int position) {
		return images.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			imageView = (ImageView) inflater.inflate(R.layout.row_gallery_grid, parent,false);
//			imageView.setLayoutParams(new GridView.LayoutParams(600/3,600/3));
		} else {
			imageView = (ImageView) convertView;
		}

		imgLoader.displayImage("file://"+galleryPath+File.separator
				+images.get(position).getImgName(), imageView);

		return imageView;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================	
}
