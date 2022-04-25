package com.mapsaurus.paneslayout;

import androidx.fragment.app.Fragment;

public interface FragmentLauncher {
	
	public void addFragment(Fragment prevFragment, Fragment newFragment);
	public void addFragment(Fragment prevFragment, Fragment newFragment, String tag);
	
}
