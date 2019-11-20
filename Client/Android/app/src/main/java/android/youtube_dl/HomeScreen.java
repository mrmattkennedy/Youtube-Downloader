package android.youtube_dl;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HomeScreen extends AppCompatActivity {

    private EditText urlText;

    private URL searchUrl;
    private HttpsURLConnection urlConnection;
    private BufferedReader reader;
    private StringBuilder buffer;

    private RecyclerView videoList;
    private VideoAdapter adapter;
    private List<String> videoTitles;
    private List<String> videoChannels;
    private List<String> videoThumbnailURLs;

    private final Pattern pattern = Pattern.compile(
            "http(?:s)?://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)",
            Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        urlText = findViewById(R.id.urlInput);
        videoList = findViewById(R.id.videoList);
        urlText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        String url = urlText.getText().toString();
                        Matcher matcher = pattern.matcher(url);

                        if (!matcher.matches())
                        {
                            new VideoSearch().execute();
                        }
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });
    }

    public void setupRecyclerView()
    {
        RecyclerView recyclerView = findViewById(R.id.videoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VideoAdapter(this, videoTitles, videoChannels, videoThumbnailURLs);
       // adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    public class VideoSearch extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... params) {
            try {
                searchUrl = new URL("https://www.googleapis.com/youtube/v3/search?key=" + getResources().getString(R.string.api_key) + "&maxResults=20&part=snippet&type=video&q=" + urlText.getText().toString());
                urlConnection = (HttpsURLConnection) searchUrl.openConnection();
                urlConnection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                buffer = new StringBuilder();
                if (br == null) {
                    return null;
                }
                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                    return null;

                return buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            try {
                videoTitles = new ArrayList<String>();
                videoChannels = new ArrayList<String>();
                videoThumbnailURLs = new ArrayList<String>();

                JSONObject objects = new JSONObject(result);
                JSONArray jArray = objects.getJSONArray("items");
                for(int i=0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);
                    JSONObject snippet = jObject.getJSONObject(getResources().getString(R.string.video_info_tag));
                    JSONObject thumbnails = snippet.getJSONObject(getResources().getString(R.string.thumbnail_info_tag));
                    JSONObject defaultThumbnail = thumbnails.getJSONObject(getResources().getString(R.string.thumbnail_default_size));

                    String tempTitle = snippet.getString("title");
                    String tempChannel = snippet.getString("channelTitle");
                    String tempThumbnailURL = defaultThumbnail.getString("url");

                    videoTitles.add(tempTitle);
                    videoChannels.add(tempChannel);
                    videoThumbnailURLs.add(tempThumbnailURL);

                }
                setupRecyclerView();
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        }
    }
}
