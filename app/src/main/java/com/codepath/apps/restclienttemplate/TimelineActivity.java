package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    public static final String TAG ="TimelineActivity";
    public static int REQUEST_CODE = 20;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    Button btLogOut;
    private SwipeRefreshLayout swipeContainer;
    FloatingActionButton compose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // To be called only once
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);
        // Look up swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        //find the Recycler view
        rvTweets = findViewById(R.id.rvTweets);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                populateHomeTimeline();
                // Make sure you call swipeContainer.setRefreshing(false)
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // initialize the list of tweet and Adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view set up: layout manager and Adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);
        Log.i("what", "what is going on"+tweets);
        populateHomeTimeline();


        compose = findViewById(R.id.compose);
        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(adapter.context, ComposeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnLogOut){
            // compose icon has been selected
            // Navigate to the compose activity
            client.clearAccessToken(); // forget who's logged in
            finish(); // navigate backwards to Login screen
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode== REQUEST_CODE && resultCode == RESULT_OK){
            // Get data from the intent i.e the tweet object
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //Update the recycler view with the new tweet
            // Modify data source of tweets
            tweets.add(0, tweet);
            // Update the Adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "OnSuccess!"+ json.toString());
                adapter.clear();
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    Log.i("idk", jsonArray.toString());
                } catch (JSONException e) {
                    Log.e(TAG, "json exception", e);
                }


            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "OnFailure!"+ response, throwable);

            }
        });
    }

}