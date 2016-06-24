package me.jakemoritz.animebuzz.mal_api;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Series;

public class AnimeListXMLHandler extends DefaultHandler {

    private final static String TAG = AnimeListXMLHandler.class.getSimpleName();

    ArrayList<Series> userList = null;
    Series tempSeries = null;
    String currentValue = "";
    boolean currentElement = false;
    boolean ignoreSeries = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = true;
        currentValue = "";

        if (localName.matches("anime")) {
            ignoreSeries = false;
            tempSeries = new Series();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement) {
            currentValue = currentValue + new String(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = false;

        if (!ignoreSeries) {
            if (localName.matches("my_status")) {
                if (!currentValue.matches("2")) {
                    ignoreSeries = true;
                    Log.d("TAG", "ignoring");

                } else {
                    Log.d("TAG", "currently watching");
                }
            } else if (localName.matches("series_animedb_id")) {
                Log.d("TAG", currentValue);
            } else if (localName.matches("series_title")) {
                Log.d("TAG", currentValue);
            }
        }

    }
}
