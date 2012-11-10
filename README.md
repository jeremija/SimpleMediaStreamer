# SimpleMediaStreamer

This is a simple last.fm radio player and artist biography viewer for Android.

You also need to download the SimpleLibrary project because SimpleMediaStreamer uses some methods from that library.

I didn't include the last.fm's API_KEY and API_SECRET because last.fm doesn't allow the use of radio API on mobile phones, so if you wish to use it, use it at your own risk. Please note that I wrote this code long before I noticed that restriction and I am sharing it here purely for educational purposes.

To make it work you should generate your own API key and Secret on http://www.last.fm/api/accounts and edit the LastfmManager.java file:

* set the API_KEY variable to your API key
* set the API_SECRET variable to your Secret

For a successfuly build you must also checkout the SimpleLibrary project!