# codepath_hw3_twitterclient
Homework 3 for CodePath Android - A Simple Twitter Client

Time spent: 17 hours

Completed user stories:

 * [X] Required: User can sign in to Twitter using OAuth login.
 * [X] Required: User should be displayed the username, name, and body for each tweet
 * [X] Required: User should be displayed the relative timestamp for each tweet "8m", "7h"
 * [X] Required: User can view more tweets as they scroll with infinite pagination
 * [X] Required: User can click a “Compose” icon in the Action Bar on the top right
 * [X] Required: User can then enter a new tweet and post this to twitter
 * [X] Required: User is taken back to home timeline with new tweet visible in timeline

Optional features:

 * [X] Links in tweets are clickable and will launch the web browser (see autolink)
 * [X] User can see a counter with total number of characters left for tweet
 * [X] User can refresh tweets timeline by pulling down to refresh (i.e pull-to-refresh)
 * [X] User can open the twitter app offline and see last loaded tweets
 * [X] Tweets are persisted into sqlite and can be displayed from the local DB
 * [X] User can tap a tweet to display a "detailed" view of that tweet
 * [ ] User can select "reply" from detail view to respond to a tweet
 * [ ] Improve the user interface and theme the app to feel "twitter branded"
 * [ ] User can see embedded image media within the tweet detail view
 * [X] Compose activity is replaced with a modal overlay

My added features:
 * [X] Save unfinished tweet drafts.

Known bugs:
 * [X] android:descendantFocusability="blocksDescendants" does not work on the main tweet's TextView after autoLink is enabled. Possible fix described here: https://stackoverflow.com/questions/21891008/a-textview-in-a-listview-s-row-cannot-click-after-setting-descendantfocusabilit

 ![Video Walkthrough](hw3.gif)