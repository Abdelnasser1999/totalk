package com.callberry.callingapp.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class PhoneUtils {
    public static String getCountryRegionFromPhone(Context paramContext) {
        TelephonyManager service = null;
        int res = paramContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
        if (res == PackageManager.PERMISSION_GRANTED) {
            service = (TelephonyManager) paramContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
        String code = null;
        if (service != null) {
            String str = service.getLine1Number();
            if (!TextUtils.isEmpty(str) && !str.matches("^0*$")) {
                code = parseNumber(paramContext, str);
            }
        }
        if (code == null) {
            if (service != null) {
                code = service.getNetworkCountryIso();
            }
            if (code == null) {
                code = paramContext.getResources().getConfiguration().locale.getCountry();
            }
        }
        if (code != null) {
            return code.toUpperCase();
        }
        return null;
    }

    private static String parseNumber(Context context, String paramString) {
        if (paramString == null) {
            return null;
        }
        PhoneNumberUtil numberUtil = PhoneNumberUtil.createInstance(context);
        String result;
        try {
            Phonenumber.PhoneNumber localPhoneNumber = numberUtil.parse(paramString, null);
            result = numberUtil.getRegionCodeForNumber(localPhoneNumber);
            if (result == null) {
                return null;
            }
        } catch (NumberParseException localNumberParseException) {
            return null;
        }
        return result;
    }

    public static boolean isPhoneNumberValid(Context context, String phoneNumber, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(context);
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, countryCode);
            return phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return false;
    }
}