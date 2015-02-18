package com.titantech.wifibuddy.models;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.provider.WifiContentProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Robert on 23.01.2015.
 */
public class AccessPoint implements Parcelable {
    private int internalId;
    private String id;
    private String publisherId;
    private String publisherMail;
    private String bssid;
    private String name;
    private String password;
    private String securityType;
    private int privacyType;
    private double latitude;
    private double longitude;
    private Date lastAccessed;

    public AccessPoint(String id, String publisherId, String bssid, String name,
                       String password, String securityType, int privacyType,
                       double latitude, double longitude, String lastAccessed) {
        this(-1, id, publisherId, null, bssid, name, password,
            securityType, privacyType, latitude, longitude, lastAccessed);
    }

    public AccessPoint(String id, String publisherId, String publisherMail, String bssid, String name,
                       String password, String securityType, int privacyType,
                       double latitude, double longitude, String lastAccessed) {
        this(-1, id, publisherId, publisherMail, bssid, name, password,
            securityType, privacyType, latitude, longitude, lastAccessed);
    }

    public AccessPoint(int internalId, String id, String publisherId, String publisherMail, String bssid, String name,
                       String password, String securityType, int privacyType,
                       double latitude, double longitude, String lastAccessed) {
        this.internalId = internalId;
        this.id = id;
        this.publisherId = publisherId;
        this.publisherMail = publisherMail;
        this.bssid = bssid;
        this.name = name;
        this.password = password;
        this.securityType = securityType;
        this.privacyType = privacyType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastAccessed = parseDate(lastAccessed.substring(0, 19));
    }
    public AccessPoint(AccessPoint ap){
        this.internalId = ap.internalId;
        this.id = ap.id;
        this.publisherId = ap.publisherId;
        this.publisherMail = ap.publisherMail;
        this.bssid = ap.bssid;
        this.name = ap.name;
        this.password = ap.password;
        this.securityType = ap.securityType;
        this.privacyType = ap.privacyType;
        this.latitude = ap.latitude;
        this.longitude = ap.longitude;
        this.lastAccessed = ap.lastAccessed;
    }

    public AccessPoint(Parcel parc) {
        this.internalId = parc.readInt();
        this.id = parc.readString();
        this.publisherId = parc.readString();
        this.publisherMail = parc.readString();
        this.bssid = parc.readString();
        this.name = parc.readString();
        this.password = parc.readString();
        this.securityType = parc.readString();
        this.privacyType = parc.readInt();
        this.latitude = parc.readDouble();
        this.longitude = parc.readDouble();
        this.lastAccessed = parseDate(parc.readString());
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (this.id != null) {
            values.put(WifiDbOpenHelper.COLUMN_ID, this.id);
        }
        values.put(WifiDbOpenHelper.COLUMN_PUBLISHER, this.publisherId);
        if (this.publisherMail != null)
            values.put(WifiDbOpenHelper.COLUMN_PUBLISHER_MAIL, this.publisherMail);

        values.put(WifiDbOpenHelper.COLUMN_BSSID, this.bssid);
        values.put(WifiDbOpenHelper.COLUMN_NAME, this.name);
        values.put(WifiDbOpenHelper.COLUMN_PASSWORD, this.password);
        values.put(WifiDbOpenHelper.COLUMN_SECURITY, this.securityType);
        values.put(WifiDbOpenHelper.COLUMN_PRIVACY, this.privacyType);
        values.put(WifiDbOpenHelper.COLUMN_LAT, this.latitude);
        values.put(WifiDbOpenHelper.COLUMN_LON, this.longitude);
        values.put(WifiDbOpenHelper.COLUMN_LASTACCESS, Utils.formatDate(this.lastAccessed));

        return values;
    }

    private Date parseDate(String date) {
        Date ret = null;
        try {
            ret = Utils.parseDate(date);
        } catch (Exception e) {
            e.printStackTrace();
            ret = new Date();
        }
        return ret;
    }

    public static Uri getBaseContentUriFromPrivacy(int privacyType){
        if(privacyType == 1) {
            return WifiContentProvider.CONTENT_URI_PRIVATE;
        } else {
            return WifiContentProvider.CONTENT_URI_PUBLIC;
        }
    }
    public Uri getContentUriFromPrivacy(){
        if(privacyType == 1) {
            return Uri.parse(WifiContentProvider.CONTENT_URI_PRIVATE + "/" + internalId);
        } else {
            return Uri.parse(WifiContentProvider.CONTENT_URI_PUBLIC + "/" + internalId);
        }
    }

    public String getId() {
        return id;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrivacyType(int privacyType) {
        this.privacyType = privacyType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public String getPublisherMail() {
        return publisherMail;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setPublisherMail(String publisherMail) {
        this.publisherMail = publisherMail;
    }

    public String getBssid() {
        return bssid;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurityType() {
        return securityType;
    }

    public int getPrivacyType() {
        return privacyType;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public static final Parcelable.Creator<AccessPoint> CREATOR = new Parcelable.Creator<AccessPoint>() {
        @Override
        public AccessPoint createFromParcel(Parcel source) {
            return new AccessPoint(source);
        }

        @Override
        public AccessPoint[] newArray(int size) {
            return new AccessPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.internalId);
        dest.writeString(this.id);
        dest.writeString(this.publisherId);
        dest.writeString(this.publisherMail);
        dest.writeString(this.bssid);
        dest.writeString(this.name);
        dest.writeString(this.password);
        dest.writeString(this.securityType);
        dest.writeInt(this.privacyType);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(Utils.formatDate(this.lastAccessed));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPoint that = (AccessPoint) o;

        if (internalId != that.internalId) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (privacyType != that.privacyType) return false;
        if (bssid != null ? !bssid.equals(that.bssid) : that.bssid != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (publisherId != null ? !publisherId.equals(that.publisherId) : that.publisherId != null)
            return false;
        if (securityType != null ? !securityType.equals(that.securityType) : that.securityType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = internalId;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (publisherId != null ? publisherId.hashCode() : 0);
        result = 31 * result + (bssid != null ? bssid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (securityType != null ? securityType.hashCode() : 0);
        result = 31 * result + privacyType;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
