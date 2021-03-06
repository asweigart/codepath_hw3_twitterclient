package com.codepath.apps.basictwitter.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.controllers.EndlessScrollListener;
import com.codepath.apps.basictwitter.dialogs.ComposeTweetDialog;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserTimelineActivity extends ActionBarActivity {
    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter aTweets;
    private ListView lvTweets;

    private SwipeRefreshLayout swipeContainer;

    private String thisUserId;
    private ImageView ivUTUserPhoto;
    private TextView tvUTUsername;
    private TextView tvUTRealName;
    private TextView tvUTTagline;
    private TextView tvUTNumFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_timeline);

        ivUTUserPhoto = (ImageView) findViewById(R.id.ivUTUserPhoto);
        tvUTUsername = (TextView) findViewById(R.id.tvUTUsername);
        tvUTRealName = (TextView) findViewById(R.id.tvUTRealName);
        tvUTTagline = (TextView) findViewById(R.id.tvUTTagline);
        tvUTNumFollowers = (TextView) findViewById(R.id.tvUTNumFollowers);

        // populate user stats
        thisUserId = getIntent().getStringExtra("userid");
        this.client = TwitterApplication.getRestClient();
        client.getUsersShow(thisUserId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Picasso.with(getApplicationContext()).load(response.getString("profile_image_url")).into(ivUTUserPhoto);
                    tvUTUsername.setText(response.getString("screen_name"));
                    tvUTRealName.setText(response.getString("name"));
                    tvUTTagline.setText(response.getString("description"));
                    tvUTNumFollowers.setText(String.valueOf(response.getInt("followers_count")) + " followers, following " + String.valueOf(response.getString("friends_count")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject responseObject) {
                Toast.makeText(UserTimelineActivity.this, getResources().getString(R.string.Unable_to_connect_to_the_internet_right_now), Toast.LENGTH_SHORT).show();

                // clear out list and populate from SQL db
                aTweets.clear();
                List<Tweet> cachedTweets = new Select()
                        .from(Tweet.class)
                        .orderBy("uid DESC")
                        .execute();
                aTweets.addAll(cachedTweets);
                swipeContainer.setRefreshing(false);
            }


        });

        // get rest client and populate the timeline
        this.lvTweets = (ListView) findViewById(R.id.lvUserTimelineTweets);
        this.client = TwitterApplication.getRestClient();
        this.populateTimeline();

        // set up infinite scrolling for the timeline
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int totalItemsCount) {
                customLoadMoreDataFromApi(totalItemsCount);
            }
        });

        // set up the ArrayList for tweets and the ListView adapter
        this.tweets = new ArrayList<Tweet>();
        this.aTweets = new TweetArrayAdapter(this, this.tweets); //new ArrayAdapter<Tweet>(this, android.R.layout.simple_list_item_1, this.tweets);
        lvTweets.setAdapter(aTweets);

        // set up swiping
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.UTSwipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_blue_dark);

        // set up click handler for clicking on a tweet
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(UserTimelineActivity.this, TweetDisplayActivity.class);
                Tweet tweet = aTweets.getItem(position);
                i.putExtra("tweet", tweet);
                startActivity(i);
            }
        });
    }

    public void customLoadMoreDataFromApi(int offset) {
        // find the last tweet, its id will be the max id.
        long max_id = aTweets.getItem(aTweets.getCount() - 1).getUid() - 1; // minus 1 so it doesn't repeat the last tweet
        client.getUserTimeline(max_id, thisUserId, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);

                aTweets.addAll(tweets); // TODO - add to beginning, only add if not in the list already
                for (int i = 0; i < tweets.size(); i++) {
                    tweets.get(i).getUser().save();
                    tweets.get(i).save();
                    //Log.e("SAVED TWEET", tweets.get(i).toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject responseObject) {
                Toast.makeText(UserTimelineActivity.this, getResources().getString(R.string.Unable_to_connect_to_the_internet_right_now), Toast.LENGTH_SHORT).show(); // TODO CHANGE THIS MESSAGE
            }


        });
    }

    public void populateTimeline() {
        client.getUserTimeline(0, thisUserId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // first go through and delete all the current tweets from the adapter AND the sqlite db
                /*while (aTweets.getCount() > 0) {
                    Tweet tweetToDelete = aTweets.getItem(0);
                    //Log.e("DELETING", tweetToDelete.toString());
                    new Delete().from(Tweet.class).where("uid = ?", tweetToDelete.getId()).execute();
                    aTweets.remove(tweetToDelete);
                }*/
                new Delete().from(Tweet.class).execute();
                new Delete().from(User.class).execute();
                aTweets.clear();

                // Download the latest tweets
                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);

                aTweets.addAll(tweets); // TODO - add to beginning, only add if not in the list already
                for (int i = 0; i < tweets.size(); i++) {
                    tweets.get(i).getUser().save();
                    tweets.get(i).save();

                    //Log.e("SAVED TWEET", tweets.get(i).toString());
                }

                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject responseObject) {
                Toast.makeText(UserTimelineActivity.this, getResources().getString(R.string.Unable_to_connect_to_the_internet_right_now), Toast.LENGTH_SHORT).show();

                // clear out list and populate from SQL db
                aTweets.clear();
                List<Tweet> cachedTweets = new Select()
                        .from(Tweet.class)
                        .orderBy("uid DESC")
                        .execute();
                aTweets.addAll(cachedTweets);
                swipeContainer.setRefreshing(false);
            }


        });
    }

    public void postStatus() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_timeline, menu);
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

    public boolean onComposeTweet(MenuItem mi) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetDialog.newInstance().show(fm, "fragment_compose_tweet");
        return true;
    }


    private Boolean isNetworkAvailable() {
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMan.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
