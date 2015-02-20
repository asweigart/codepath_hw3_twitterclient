package com.codepath.apps.basictwitter.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.activities.TimelineActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;


public class ComposeTweetDialog extends DialogFragment {
    private EditText etNewTweet;
    private Button btnPostTweet;
    private TextView tvCharsRemaining;

    public static String DRAFT_TWEET = "Draft Tweet";
    public static String DRAFT_TWEET_TEXT = "Draft Tweet Text";
    public static ColorStateList originalTextColor;

    //Context mContext;

    public ComposeTweetDialog() { }

    /*public ComposeTweetDialog(final Context context) {
        mContext = context;
    }*/

    public static ComposeTweetDialog newInstance() {
        return new ComposeTweetDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_compose_tweet, container);
        /*LinearLayout ll=(LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.fragment_compose_tweet, null);
        getActivity().setContentView(ll);
        View view = ll;*/

        etNewTweet = (EditText) view.findViewById(R.id.etNewTweet);
        btnPostTweet = (Button) view.findViewById(R.id.btnPostTweet);
        tvCharsRemaining = (TextView) view.findViewById(R.id.tvCharsRemaining);

        originalTextColor = tvCharsRemaining.getTextColors();

        getDialog().setTitle(getResources().getString(R.string.Compose));

        // add TextWatcher to track how many characters have been typed
        etNewTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Save text as draft in SharedPreferences as it is typed
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(DRAFT_TWEET, 0).edit();
                editor.putString(DRAFT_TWEET_TEXT, s.toString());
                editor.apply();

                // update the character count display and enable/disable tweet button
                int charsRemaining = 140 - etNewTweet.getText().toString().length();
                tvCharsRemaining.setText(String.valueOf(charsRemaining));
                if (charsRemaining < 0 || charsRemaining == 140) {
                    btnPostTweet.setEnabled(false);
                    tvCharsRemaining.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    btnPostTweet.setEnabled(true);
                    tvCharsRemaining.setTextColor(originalTextColor);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // focus edit text and bring up soft keyboard
        etNewTweet.requestFocus();

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // setup onClick for the Tweet button
        btnPostTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO - check for internet connectivity
                //Toast.makeText(getActivity(), "Debug: Post Tweet", Toast.LENGTH_SHORT).show();
                TwitterApplication.getRestClient().postStatus(etNewTweet.getText().toString(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        etNewTweet.setText("");
                        ((TimelineActivity)getActivity()).populateTimeline();
                        dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Unable_to_post_status), Toast.LENGTH_SHORT).show();
                        Log.e("TWITTER ERROR", error.toString());
                        dismiss();
                    }
                });

            }
        });
        return view;
    }

}
