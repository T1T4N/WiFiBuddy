package com.titantech.wifibuddy.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robert on 23.01.2015.
 */
public class User implements Parcelable {
    private String userId;
    private String email;
    private String password;
    private Set<AccessPoint> accessPoints;

    public User(String userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        accessPoints = new HashSet<AccessPoint>();
    }

    public User(Parcel parc) {
        this.userId = parc.readString();
        this.email = parc.readString();
        this.password = parc.readString();
        accessPoints = new HashSet<AccessPoint>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return userId.equals(user.userId);
    }

    public String getUserId() {
        return userId;
    }

    public static User nullUser() {
        return new User("", "", "");
    }

    public static User genericUnauthorized() {
        return new User("unauthorized", "unauthorized", "unauthorized");
    }

    public static User genericExisting() {
        return new User("exists", "exists", "exists");
    }

    public Map<String, String> getData() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("id", userId);
        data.put("email", email);
        data.put("password", password);
        return data;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<AccessPoint> getAccessPoints() {
        return new ArrayList<AccessPoint>(accessPoints);
    }

    public void addAccessPoint(AccessPoint ap) {
        accessPoints.add(ap);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.email);
        dest.writeString(this.password);
    }
}
