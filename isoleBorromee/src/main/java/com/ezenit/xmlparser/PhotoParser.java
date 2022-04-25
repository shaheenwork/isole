package com.ezenit.xmlparser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.database.sqlite.SQLiteDatabase;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery.GalleryImage;
import com.ezenit.utils.MiscUtils;

public class PhotoParser extends DefaultHandler{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG_PHOTO 		= "Photo";
	private static final String TAG_CODE		= "No";
	private static final String TAG_HEADING 	= "HD";
	private static final String TAG_DESCRIPTION = "Desc";
	// ===========================================================
	// Fields
	// ===========================================================
	public static final StringBuilder builder = new StringBuilder();
	
	private GalleryImage galleryImage;
	
	private final Museum museum;
	private final String languageShort;
	private final SQLiteDatabase db;
	// ===========================================================
	// Constructors
	// ===========================================================
	public PhotoParser(Museum museum,String languageShort) {
		this.museum = museum;
		this.languageShort = languageShort;
		this.db = AppIsole.getDB();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals(TAG_PHOTO)){
			galleryImage = new GalleryImage(museum,languageShort);
		}
		else if(localName.endsWith(TAG_CODE)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_HEADING)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_DESCRIPTION)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.equals(TAG_PHOTO)){
			TablePhotoGallery.createOrUpdate(db, galleryImage);
		}
		else if(localName.equals(TAG_CODE)){
			galleryImage.setCode(builder.toString());
		}
		else if(localName.equals(TAG_HEADING)){
			galleryImage.setTitle(builder.toString());
		}
		else if(localName.equals(TAG_DESCRIPTION)){
			galleryImage.setDescription(builder.toString());
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
	// Inner and Anonymous Classes
	// ===========================================================
}
