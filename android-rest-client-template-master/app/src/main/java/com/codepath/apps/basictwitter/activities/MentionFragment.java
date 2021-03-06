package com.codepath.apps.basictwitter.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.controllers.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MentionFragment extends Fragment {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter aTweets;
    private ListView lvTweets;

    private SwipeRefreshLayout swipeContainer;

    public static MentionFragment newInstance() {
        MentionFragment fragment = new MentionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MentionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mention, container, false);

        // get rest client and populate the timeline
        this.lvTweets = (ListView) v.findViewById(R.id.lvMentionFragTweets);
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
        this.aTweets = new TweetArrayAdapter(getActivity().getApplicationContext(), this.tweets); // TODO - is this the right context?
        lvTweets.setAdapter(aTweets);

        // set up swiping
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.mentionFragSwipeContainer);
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
                Intent i = new Intent(getActivity().getApplicationContext(), UserTimelineActivity.class); // TODO - is this the right activity?
                Tweet tweet = aTweets.getItem(position);
                i.putExtra("userid", Long.toString(tweet.getUser().getUid()));
                startActivity(i);

            }
        });

        return v;
    }


    public void customLoadMoreDataFromApi(int offset) {
        // find the last tweet, its id will be the max id.
        long max_id = aTweets.getItem(aTweets.getCount() - 1).getUid() - 1; // minus 1 so it doesn't repeat the last tweet
        client.getMentionsTimeline(max_id, new JsonHttpResponseHandler() {
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
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.Unable_to_connect_to_the_internet_right_now), Toast.LENGTH_SHORT).show(); // TODO - is this the right activity?
            }


        });
    }

    public void populateTimeline() {
        client.getMentionsTimeline(0, new JsonHttpResponseHandler() {
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
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.Unable_to_connect_to_the_internet_right_now), Toast.LENGTH_SHORT).show(); // TODO - is this correct?

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

    public void onClickUserPhoto(View v) {
        try {
            Intent i = new Intent(getActivity(), UserTimelineActivity.class);
            i.putExtra("userid", 0);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
