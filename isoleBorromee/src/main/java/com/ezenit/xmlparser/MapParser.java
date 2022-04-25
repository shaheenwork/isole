package com.ezenit.xmlparser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;

import com.ezenit.isoleborromee.db.table.TableMap;
import com.ezenit.isoleborromee.db.table.TableSection;
import com.ezenit.isoleborromee.db.table.TableMap.Room;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TableSection.Section;
import com.ezenit.utils.LanguageConstants;
import com.ezenit.utils.MiscUtils;

public class MapParser extends DefaultHandler{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG_SECTION		= "Section";
	private static final String ATTRIB_INDEX 	= "index";
	private static final String ATTRIB_NAME 	= "name";
	private static final String ATTRIB_NAME_IT 	= "name_it";
	////////////bibin
	private static final String ATTRIB_NAME_DE 	= "name_de";
	private static final String ATTRIB_NAME_FR 	= "name_fr";
	////////////////
	private static final String TAG_ROOM 		  = "Room";
	private static final String TAG_ROOM_NO	 	  = "No";
	private static final String TAG_COORDS		  = "Coords";
	private static final String TAG_CIRCLE_COORDS = "circle";
	// ===========================================================
	// Fields
	// ===========================================================
	private Museum musuem;
	private int	   currentSection;
	
	private Room 			room;
	private StringBuilder builder = new StringBuilder();
	// ===========================================================
	// Constructors
	// ===========================================================
	public MapParser(Museum museum) {
		this.musuem = museum;
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
		super.startElement(uri, localName, qName, attributes);
		if(parseSection(localName,attributes)){
			
		}
		else if(localName.equals(TAG_ROOM)){
			room = new Room(musuem,currentSection);
		}
		else if(localName.equals(TAG_ROOM_NO)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_COORDS)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
		else if(localName.equals(TAG_CIRCLE_COORDS)){
			MiscUtils.reuseForBetterPerformance(builder);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		if(localName.equals(TAG_ROOM)){
			TableMap.createOrUpdate(room);
			room = null;
		}
		else if(localName.equals(TAG_ROOM_NO)){
			room.setRoomNo(builder.toString());
		}
		else if(localName.equals(TAG_COORDS)){
			room.setCoords(builder.toString());
		}
		else if(localName.equals(TAG_CIRCLE_COORDS)){
			room.setCircleCoords(builder.toString());
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	private boolean parseSection(String localName, Attributes attributes) {
		if(localName.equals(TAG_SECTION)){
			String sectionId = attributes.getValue(ATTRIB_INDEX);
			String name	   = attributes.getValue(ATTRIB_NAME);
			String nameIt  = attributes.getValue(ATTRIB_NAME_IT);
			if(!TextUtils.isEmpty(sectionId)){
				try{
					currentSection = Integer.parseInt(sectionId);
				}
				catch(Exception e){
					currentSection = 0;
				}
			}
			else{
				currentSection = 0;
			}
			Section section = new Section(currentSection, musuem, name, LanguageConstants.ENGLISH);
			Section sectionIt = new Section(currentSection, musuem, nameIt, LanguageConstants.ITALIAN);
			////bibin
			Section sectionDe = new Section(currentSection, musuem, nameIt, LanguageConstants.GERMAN);
			Section sectionFr = new Section(currentSection, musuem, nameIt, LanguageConstants.FRENCH);
///////////////////////////////////////////
			TableSection.insertOrUpdate(section);
			TableSection.insertOrUpdate(sectionIt);
			TableSection.insertOrUpdate(sectionDe);
			TableSection.insertOrUpdate(sectionFr);
			
			return true;
		}
		else{
			return false;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
