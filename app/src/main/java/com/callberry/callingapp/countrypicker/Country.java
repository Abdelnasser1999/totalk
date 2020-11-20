package com.callberry.callingapp.countrypicker;

import android.os.Parcel;
import android.os.Parcelable;

public class Country implements Parcelable {

    private String iso;
    private String name;
    private String dialCode;
    private String flag;

    public Country(String iso, String name, String dialCode, String flag) {
        this.iso = iso;
        this.name = name;
        this.dialCode = dialCode;
        this.flag = flag;
    }

    protected Country(Parcel in) {
        iso = in.readString();
        name = in.readString();
        dialCode = in.readString();
        flag = in.readString();
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(iso);
        dest.writeString(name);
        dest.writeString(dialCode);
        dest.writeString(flag);
    }
}
