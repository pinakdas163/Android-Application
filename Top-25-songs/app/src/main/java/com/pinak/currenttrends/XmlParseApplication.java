package com.pinak.currenttrends;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Pinak on 22-07-2016.
 */
public class XmlParseApplication {
    private String xmlData;
    private ArrayList<ParseApplication> list;

    public XmlParseApplication(String xmlData) {
        this.xmlData = xmlData;
        list=new ArrayList<ParseApplication>();
    }

    public String getXmlData() {
        return xmlData;
    }

    public ArrayList<ParseApplication> getList() {
        return list;
    }

    public boolean process()
    {
        boolean status=true;
        ParseApplication record=null;
        boolean inEntry =false;
        String textValue="";

        try {
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();
            xpp.setInput(new StringReader(this.xmlData));
            int eventType=xpp.getEventType();

            while (eventType!=XmlPullParser.END_DOCUMENT)
            {
                String tagName= xpp.getName();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        //Log.d("Parse Application", "start tag " + tagName);
                        if (tagName.equalsIgnoreCase("entry"))
                        {
                            inEntry=true;
                            record=new ParseApplication();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue=xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        //Log.d("Parse Application", "end tag " + tagName);
                        if (inEntry) {
                            if (tagName.equalsIgnoreCase("entry")) {
                                list.add(record);
                                inEntry = false;
                            } else if (tagName.equalsIgnoreCase("name")) {
                                record.setName(textValue);
                            } else if (tagName.equalsIgnoreCase("artist")) {
                                record.setArtist(textValue);
                            } else if (tagName.equalsIgnoreCase("releaseDate")) {
                                record.setReleaseDate(textValue);
                            }
                        }
                        break;
                    default:
                        //nothing to do
                }
                eventType=xpp.next();
            }
        }
        catch (Exception e)
        {
            status=false;
            e.printStackTrace();
        }
        for (ParseApplication app: list)
        {
            Log.d("Parse Data", " ********** ");
            Log.d("Parse Data", app.getName());
            Log.d("Parse Data", app.getArtist());
            Log.d("Parse Data", app.getReleaseDate());
        }
        return true;
    }
}
