package com.ezenit.isoleborromee.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.adapter.AdapterPhotoGrid;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery.GalleryImage;
import com.ezenit.isoleborromee.R;

public class FragmentPhotoGrid extends Fragment{

	// ===========================================================
	// Constants
	// ===========================================================
	private static final String ARG_MUSEUM 	 = "com.ezenit.isoleborromee.fragments.FragmentPhotoGrid.ARG_MUSUEM";
	private static final String ARG_LANGUAGE = "com.ezenit.isoleborromee.fragments.FragmentPhotoGrid.ARG_LANGUAGE";
	
	private static final String TAG			 = FragmentPhotoGrid.class.getName();
	
	public  static final int    NUM_COLUMNS  = 3;
	
	// ===========================================================
	// Fields
	// ===========================================================
	private FragmentPhotoGridListener fragmentPhotoGridListener;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentPhotoGrid getInstance(Museum museum,String language)
	{
		FragmentPhotoGrid fragment = new FragmentPhotoGrid();
		Bundle args = new Bundle();
		args.putString(ARG_MUSEUM, museum.getShortName());
		args.putString(ARG_LANGUAGE, language);
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
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            this.fragmentPhotoGridListener = (FragmentPhotoGridListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentPhotoGridListener");
        }
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fr_photogallery_grid, container, false);
		
		
		final GridView gridView = (GridView)view.findViewById(R.id.grid_view);
		
		String language = getArguments().getString(ARG_LANGUAGE);
		
		SQLiteDatabase db = AppIsole.getDB();
		Museum museum 	= Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
		ArrayList<GalleryImage> images = TablePhotoGallery.getGalleryImages(db, museum, language);
		
		final AdapterPhotoGrid adapter = new AdapterPhotoGrid(getActivity(), museum, images);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if(fragmentPhotoGridListener!=null)
					fragmentPhotoGridListener.onPhotoClicked(position,adapter.getItem(position));
			}
		});
		gridView.setNumColumns(NUM_COLUMNS);
		
		
		return view;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	

	public interface FragmentPhotoGridListener{
		public void onPhotoClicked(int index,GalleryImage image);
	}

	

}
