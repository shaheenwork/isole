package com.ezenit.customview;

import java.io.File;

import com.ezenit.isoleborromee.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class IsoleTextView extends androidx.appcompat.widget.AppCompatTextView{

	// ===========================================================
	// Constants
	// ===========================================================
	
	private static Typeface opensans_bold;
	private static Typeface opensans_italic;
	private static Typeface opensans_semibold;
	private static Typeface opensans_regular;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public IsoleTextView(Context context) {
		super(context);
		initInnerFonts(context);
	}
	
	
	public IsoleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initInnerFonts(context);
		init(attrs);
	}
	
	public IsoleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initInnerFonts(context);
		init(attrs);
		
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public static Typeface getFontBold(){
		return opensans_bold;
	}
	
	public static Typeface getFontItalic(){
		return opensans_italic;
	}
	
	public static Typeface getFontSemiBold(){
		return opensans_semibold;
	}
	
	public static Typeface getFontRegular(){
		return opensans_regular;
	}
	
	
	public static Typeface getFont(String name){
		if(name.equals("semibold")){
			return opensans_semibold;
		}
		else if(name.equals("italic")){
			return opensans_italic;
		}
		else if(name.equals("bold")){
			return opensans_bold;
		}
		else{
			return opensans_regular;
		}
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	private void init(AttributeSet attrs) {
		if (attrs!=null) {
			/*shn change TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IsoleTextView);
			 String fontStyle = a.getString(R.styleable.IsoleTextView_fontStyle);
			 if (fontStyle!=null) {
				 setTypeface(getFont(fontStyle));
			 }
			 a.recycle();*/
		}
	}
	
	private static void initInnerFonts(Context context){
		if(opensans_bold==null)
			opensans_bold = Typeface.createFromAsset(context.getAssets(), "fonts"+File.separator+"opensans_bold.ttf");
		if(opensans_italic==null)
			opensans_italic = Typeface.createFromAsset(context.getAssets(), "fonts"+File.separator+"opensans_italic.ttf");
		if(opensans_semibold==null)
			opensans_semibold = Typeface.createFromAsset(context.getAssets(), "fonts"+File.separator+"opensans_semibold.ttf");
		if(opensans_regular==null)
			opensans_regular = Typeface.createFromAsset(context.getAssets(), "fonts"+File.separator+"opensans_regular.ttf");
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
