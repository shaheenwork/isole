package com.ezenit.isoleborromee.fragments;

import com.ezenit.isoleborromee.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;


public class FragmentAlert extends DialogFragment{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	
	private OnDialogClickListener onDialogClickListener;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static FragmentAlert newInstance(int msg,String positiveButton,boolean bold) {
		return newInstance(-1, msg, null, positiveButton, bold);
    }
	
	public static FragmentAlert newInstance(int msg,String negativeButton,String positiveButton,boolean bold) {
		return newInstance(-1, msg, negativeButton, positiveButton, bold);
    }
	
	public static FragmentAlert newInstance(int msg,String negativeButton,String positiveButton) {
		return newInstance(-1, msg, negativeButton, positiveButton, false);
    }
	
	public static FragmentAlert newInstance(int title,int msg,String negativeButton,String positiveButton,boolean bold) {
		FragmentAlert frag = new FragmentAlert();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("msg", msg);
        args.putString("posButton", positiveButton);
        args.putString("negButton", negativeButton);
        args.putBoolean("bold", bold);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Fragment parentFragment = getParentFragment();
		if(parentFragment!=null&& parentFragment instanceof OnDialogClickListener){
			this.onDialogClickListener = (OnDialogClickListener) parentFragment;
		}
		else if(activity instanceof OnDialogClickListener){
			this.onDialogClickListener = (OnDialogClickListener) activity;
		}
	}
	

	// ===========================================================
	// Getter & Setter
	// ===========================================================
//	public void setOnDialogClickListener(
//			OnDialogClickListener onDialogClickListener) {
//		this.onDialogClickListener = onDialogClickListener;
//	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void show(FragmentManager manager, String tag) {
		// TODO Auto-generated method stub
		Log.d("show", "show fragmnt Manger");
		super.show(manager, tag);
		
	}
	
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title",-1);
        int msgId = getArguments().getInt("msg");
        final String posButton = getArguments().getString("posButton");
        final String negButton = getArguments().getString("negButton");
        
              
       LayoutInflater inflater = LayoutInflater.from(getActivity());
       TextView msg   = (TextView) inflater.inflate(R.layout.fr_alert, null);
        msg.setText(msgId);
        if(getArguments().getBoolean("bold")){
        	msg.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else{
        	msg.setTypeface(Typeface.DEFAULT);
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                
                .setView(msg);
                if(title!=-1){
                	builder.setTitle(title);
                }
                if(posButton!=null){
                	builder.setPositiveButton(posButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                   if(onDialogClickListener!=null)
                                	   onDialogClickListener.onPositiveClicked(getTag());
                                }
                            }
                        );
                }
                
                if(negButton!=null){
                	builder.setNegativeButton(negButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                	  if(onDialogClickListener!=null)
                                   	   onDialogClickListener.onNegativeClicked(getTag());
                                }
                            }
                        );
                }
              AlertDialog dialog = builder.create();
              if(title==-1)
              	dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
           return dialog;
    }
    
    
  
 //bibin set dismiss dialog   
    @Override
    public void onCancel(DialogInterface dialog) {
    	// TODO Auto-generated method stub
    	super.onCancel(dialog);
    	onDialogClickListener.onDismiss();
    	
    }
   
	// ===========================================================
	// Methods
	// ===========================================================
    
    public interface OnDialogClickListener{
    	public void onPositiveClicked(String tag);
    	public void onNegativeClicked(String tag);
   
  //add an interface of dismiss  	
    	public void onDismiss();
    }

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
