package com.ezenit.isoleborromee.adapter;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class AdapterFragmentTablet extends FragmentStatePagerAdapter{

	

	
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private ArrayList<Fragment> fragments;
	// ===========================================================
	// Constructors
	// ===========================================================
	public AdapterFragmentTablet(FragmentManager fm) {
		super(fm);
		fragments = new ArrayList<Fragment>();
		
	}
	
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void addFragment(Fragment fragment){
		fragments.add(fragment);
		notifyDataSetChanged();
	}
	
	public void removeFragment(Fragment fragment){
		fragments.remove(fragment);
		notifyDataSetChanged();
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
