package com.codepath.apps.basictwitter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "Users")
public class User extends Model implements Parcelable {
    public User() {}

    @Column(name = "name")
    private String name;

    @Column(name = "screenName")
    private String screenName;

    @Column(name = "uid", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;

    @Column(name = "profileImageUrl")
    private String profileImageUrl;

    public static User fromJSON(JSONObject jsonObject) {
        User u = new User();
        try {

            u.name = jsonObject.getString("name");
            u.screenName = jsonObject.getString("screen_name");
            u.uid = jsonObject.getLong("id");
            u.profileImageUrl = jsonObject.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return u;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public long getUid() {
        return uid;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    private User(Parcel source) {
        this.name = source.readString();
        this.screenName = source.readString();
        this.uid = source.readLong();
        this.profileImageUrl = source.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public  void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.screenName);
        dest.writeLong(this.uid);
        dest.writeString(this.profileImageUrl);
    }

    @SuppressWarnings("unused")
    public static final Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
