package com.codepath.apps.basictwitter.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String name;
    private String screenName;
    private long uid;
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
}
