package com.ezenit.utils;

import java.util.Locale;

public class LanguageConstants {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String ENGLISH = "en";
	public static final String FRENCH  = "fr";
	public static final String GERMAN  = "de";
	public static final String ITALIAN = "it";
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	
	public static String[] getAllLanguages(){
		return new String[]{ENGLISH,FRENCH,GERMAN,ITALIAN};
	}
	
	public static String convertToShort(String language){
		language = language.trim().toLowerCase(Locale.UK);
		
		if(language.equals("deutch")){
			return GERMAN;
		}
		else if(language.equals("italiano")){
			return ITALIAN;
		}
		else if(language.equals("français")){
			return FRENCH;
		}
		
		return ENGLISH;
	}
	
	public static String convertToShort(Locale locale){
		if(locale.equals(Locale.GERMAN)){
			return GERMAN;
		}
		else if(locale.equals(Locale.ITALIAN)){
			return ITALIAN;
		}
		else if(locale.equals(Locale.FRENCH)){
			return FRENCH;
		}
		
		return ENGLISH;
	}
	
	public static Locale getLocale(String shortLanguage){
		if(shortLanguage.equals(GERMAN)){
			return Locale.GERMAN;
		}
		else if(shortLanguage.equals(FRENCH)){
			return Locale.FRENCH;
		}
		else if(shortLanguage.equals(ITALIAN)){
			return Locale.ITALIAN;
		}
		return Locale.ENGLISH;
	}
	
	public static String getAppLanguage(String shortLanguage){
		if(shortLanguage.equals(GERMAN)){
			return "deutch";
		}
		else if(shortLanguage.equals(FRENCH)){
			return "français";
		}
		else if(shortLanguage.equals(ITALIAN)){
			return "italiano";
		}
		return "english";
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
