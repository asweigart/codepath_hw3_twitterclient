package com.codepath.apps.basictwitter.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.models.Tweet;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
    public TweetArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    private static class ViewHolder {
        TextView tvText;
        TextView tvScreenName;
        TextView tvRealName;
        TextView tvCreatedAt;
        ImageView ivProfileImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tweet tweet = getItem(position);

        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.tweet_item, parent, false);
            vh.tvText = (TextView) convertView.findViewById(R.id.tvText);
            vh.tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
            vh.tvRealName = (TextView) convertView.findViewById(R.id.tvUTRealName);
            vh.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
            vh.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        // populate the tweet item views
        vh.tvText.setText(tweet.getText());
        vh.tvRealName.setText(tweet.getUser().getName());
        vh.tvScreenName.setText(tweet.getUser().getScreenName());
        vh.tvCreatedAt.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(vh.ivProfileImage); // TODO - cache this image?

        return convertView;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
