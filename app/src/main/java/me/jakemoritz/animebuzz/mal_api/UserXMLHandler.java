package me.jakemoritz.animebuzz.mal_api;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UserXMLHandler extends DefaultHandler{

    UserMaster user = null;
    String currentValue = "";
    boolean currentElement = false;

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        currentElement = false;

        if (localName.matches("id")){
            user.setId(Integer.valueOf(currentValue));
        } else if (localName.matches("username")){
            user.setUsername(currentValue);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (currentElement){
            currentValue = currentValue + new String(ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        currentElement = true;
        currentValue = "";
        if (localName.matches("user")){
            user = new UserMaster();
        }
    }
}
