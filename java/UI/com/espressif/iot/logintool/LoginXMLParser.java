package com.espressif.iot.logintool;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.espressif.iot.logintool.Platform.Type;

import android.content.Context;
import android.util.Xml;

class LoginXMLParser {
    private static final String ENCODING_UTF8 = "UTF-8";

    private static final String TAG_PLATFORM = "Platform";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_APP_ID = "AppId";
    private static final String ATTR_APP_KEY = "AppKey";
    private static final String ATTR_APP_SECRET = "AppSecret";

    private static final String FILE_NAME = "EspLogin.xml";

    private final Context mContext;

    LoginXMLParser(Context context) {
        mContext = context;
    }

    List<Platform> parse() {
        try {
            InputStream is = mContext.getAssets().open(FILE_NAME);
            if (is == null) {
                return null;
            }

            List<Platform> result = new LinkedList<Platform>();
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(is, ENCODING_UTF8);
            Platform.Builder builder = new Platform.Builder();
            for (int eventType = xpp.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        final String nameStart = xpp.getName();
                        if (nameStart.equals(TAG_PLATFORM)) {
                            builder = new Platform.Builder();
                            int attrCount = xpp.getAttributeCount();
                            for (int i = 0; i < attrCount; i++) {
                                String attrName = xpp.getAttributeName(i);
                                String attrValue = xpp.getAttributeValue(i);
                                if (attrName.equals(ATTR_NAME)) {
                                    if (attrValue.equals(Type.QQ.name())) {
                                        builder.setType(Type.QQ);
                                    }
                                } else if (attrName.equals(ATTR_APP_ID)) {
                                    builder.setAppId(attrValue);
                                } else if (attrName.equals(ATTR_APP_KEY)) {
                                    builder.setAppKey(attrValue);
                                } else if (attrName.equals(ATTR_APP_SECRET)) {
                                    builder.setAppSecret(attrValue);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        final String nameEnd = xpp.getName();
                        if (nameEnd.equals(TAG_PLATFORM)) {
                            result.add(builder.create());
                        }
                        break;
                }
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return null;
    }
}
