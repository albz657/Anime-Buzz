package me.jakemoritz.animebuzz.api.mal;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import me.jakemoritz.animebuzz.api.mal.backup_models.MALAnimeXMLModel;
import me.jakemoritz.animebuzz.api.mal.backup_models.MALXMLModel;
import me.jakemoritz.animebuzz.api.mal.backup_models.UserInfoXMLModel;

public class UserXmlHandler extends DefaultHandler {

    private MALXMLModel malxmlModel;
    private StringBuilder stringBuilder;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        stringBuilder.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("myanimelist")){
            this.malxmlModel = new MALXMLModel();
        } else if (localName.equalsIgnoreCase("myinfo")){
            this.malxmlModel.setUserInfoXMLModel(new UserInfoXMLModel());
        } else if (localName.equalsIgnoreCase("anime")){
            this.malxmlModel.getMALAnimeXMLModelList().add(new MALAnimeXMLModel());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (this.malxmlModel != null){
            if (localName.equalsIgnoreCase("")){

            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        stringBuilder = new StringBuilder();
    }

    public MALXMLModel getMalxmlModel() {
        return malxmlModel;
    }
}
