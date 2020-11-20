package com.callberry.callingapp.util;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CountryParser {

    private Activity activity;

    public CountryParser(Activity activity) {
        this.activity = activity;
    }

    public void readCountriesFromJSON(CountryParserListener listener) {
        ArrayList<Country> countries = new ArrayList<>();
        JSONObject object = getJSON(activity, "countries.json");
        JSONArray jsonArray = null;
        try {
            jsonArray = object.getJSONArray("countries");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonobject = jsonArray.getJSONObject(i);
                String name = jsonobject.getString("name");
                String iso = jsonobject.getString("code");
                String dialCode = jsonobject.getString("dial_code");
                String flag = getUnicode(activity, iso);
                Country country = new Country(iso, name, dialCode, flag);
                countries.add(country);
            }
            listener.onComplete(countries);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR: ", e.getMessage());
        }
    }

    private static String getUnicode(Activity activity, String code) {
        String flag = null;
        JSONObject json = getJSON(activity, "countries_flag.json");
        try {
            JSONObject country = json.getJSONObject(code);
            flag = country.getString("unicode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private static String getFlag(Activity activity, String code) {
        String flag = null;
        JSONObject json = getJSON(activity, "countries_flag.json");
        try {
            JSONObject country = json.getJSONObject(code);
            String unicode = country.getString("unicode");
            unicode = unicode.replace("U+", "0x");
            String[] codeArr = unicode.split(" ");
            int hex1 = Integer.parseInt(codeArr[0].substring(2), 16);
            int hex2 = Integer.parseInt(codeArr[1].substring(2), 16);
            flag = new String(Character.toChars(hex1)) + new String(Character.toChars(hex2));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private static JSONObject getJSON(Activity activity, String filename) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            return new JSONObject(json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static interface CountryParserListener {
        void onComplete(ArrayList<Country> countries);
    }

    public static class Country {
        String iso;
        String name;
        String dialCode;
        String flag;

        public Country(String iso, String name, String dialCode, String flag) {
            this.iso = iso;
            this.name = name;
            this.dialCode = dialCode;
            this.flag = flag;
        }

        public String getIso() {
            return iso;
        }

        public String getName() {
            return name;
        }

        public String getDialCode() {
            return dialCode;
        }

        public String getFlag() {
            return flag;
        }
    }
}
