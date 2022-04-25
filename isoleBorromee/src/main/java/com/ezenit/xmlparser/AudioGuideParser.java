package com.ezenit.xmlparser;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.utils.MiscUtils;

public class AudioGuideParser extends DefaultHandler {
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String TAG_AUDIO_GUIDE = "Guide";
	
	private static final String TAG_CODE		= "No";
	private static final String TAG_TITLE		= "Title";
	private static final String TAG_DESCRIPTION = "Desc";
	
	
	// ===========================================================
	// Fields
	// ===========================================================
	private AudioGuide audioGuide;
	
	private Museum	   museum;
	private String 	   language;
	private boolean	   free;
	
	private final StringBuilder builder = new StringBuilder();
		
	// ===========================================================
	// Constructors
	// ===========================================================
	public AudioGuideParser(boolean free,Museum museum,String language) {
		this.free 		= free;
		this.museum 	= museum;
		this.language 	= language;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals(TAG_AUDIO_GUIDE)){
			audioGuide = new AudioGuide(museum,language,free);
		}
		else if(localName.equals(TAG_CODE)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_TITLE)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_DESCRIPTION)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		Log.d("endElement", "endElement: "+localName);
		super.endElement(uri, localName, qName);
		if(localName.equals(TAG_AUDIO_GUIDE)){
			TableAudioGuide.createOrUpdate(audioGuide);
			audioGuide = null;
		}
		else if(localName.equals(TAG_CODE)){
			audioGuide.setId(getValue());
		}
		else if(localName.equals(TAG_TITLE)){
			audioGuide.setTitle(getValue());
		}
		else if(localName.equals(TAG_DESCRIPTION)){
			audioGuide.setDescription(getValue());
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		builder.append(ch,start,length);
	}
	// ===========================================================
	// Methods
	// ===========================================================
	private String getValue(){
		return builder.toString().trim();
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
