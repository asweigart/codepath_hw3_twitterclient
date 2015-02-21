package com.codepath.apps.basictwitter.activities;

import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.models.Tweet;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TweetDisplayActivity extends ActionBarActivity {
    private Tweet tweetToDisplay;

    private TextView tvDetailText;
    private TextView tvDetailScreenName;
    private TextView tvDetailRealName;
    private TextView tvDetailCreatedAt;
    private ImageView ivDetailProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_display);

        tweetToDisplay = getIntent().getParcelableExtra("tweet");

        tvDetailText = (TextView) findViewById(R.id.tvDetailText);
        tvDetailScreenName = (TextView) findViewById(R.id.tvDetailScreenName);
        tvDetailRealName = (TextView) findViewById(R.id.tvDetailRealName);
        tvDetailCreatedAt = (TextView) findViewById(R.id.tvDetailCreatedAt);
        ivDetailProfileImage = (ImageView) findViewById(R.id.ivDetailProfileImage);

        // populate the tweet item views
        tvDetailText.setText(tweetToDisplay.getText());
        tvDetailRealName.setText(tweetToDisplay.getUser().getName());
        tvDetailScreenName.setText(tweetToDisplay.getUser().getScreenName());
        tvDetailCreatedAt.setText(getRelativeTimeAgo(tweetToDisplay.getCreatedAt()));
        Picasso.with(this).load(tweetToDisplay.getUser().getProfileImageUrl()).into(ivDetailProfileImage); // TODO - cache this image?

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
