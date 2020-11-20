package com.callberry.callingapp.countrypicker;

import android.app.Activity;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class JSONParser {
    public static ArrayList<Country> countries(Activity activity) {
        ArrayList<Country> countries = new ArrayList<>();
        JSONObject object = openFile(activity, "countrieswithflag.json");
        JSONArray jsonArray = null;
        try {
            jsonArray = object.getJSONArray("countries");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONObject countryJSON = obj.getJSONObject("country");
                String name = countryJSON.getString("name");
                String iso = countryJSON.getString("iso");
                String flag = flag(countryJSON.getString("flag"));
                String dialCode = countryJSON.getString("dialCode");
                Country country = new Country(iso, name, dialCode, flag);
                countries.add(country);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR: ", e.getMessage());
        }
        return countries;
    }

    private static String flag(String unicode) {
        String flag = null;
        unicode = unicode.replace("U+", "0x");
        String[] codeArr = unicode.split(" ");
        int hex1 = Integer.parseInt(codeArr[0].substring(2), 16);
        int hex2 = Integer.parseInt(codeArr[1].substring(2), 16);
        flag = new String(Character.toChars(hex1)) + new String(Character.toChars(hex2));
        return flag;
    }

    public static JSONObject openFile(Activity activity, String filename) {
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
}
