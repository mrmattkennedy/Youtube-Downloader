package android.youtube_dl;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HomeScreen extends AppCompatActivity implements VideoAdapter.ItemClickListener {

    private EditText urlText;

    private TextView videoListActionText;
    private Switch videoListAction;
    private boolean watchVideo = true;

    private URL searchUrl;
    private HttpsURLConnection urlConnection;
    private BufferedReader reader;
    private StringBuilder buffer;

    private RecyclerView videoList;
    private VideoAdapter adapter;
    private List<String> videoTitles;
    private List<String> videoChannels;
    private List<String> videoThumbnailURLs;
    private List<String> videoIDs;

    private final Pattern pattern = Pattern.compile(
            "http(?:s)?://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)",
            Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        videoListActionText = findViewById(R.id.videoListActionText);
        videoListAction = findViewById(R.id.videoListAction);

        videoListActionText.setVisibility(View.INVISIBLE);
        videoListAction.setVisibility(View.INVISIBLE);

        videoListAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (videoListAction.isChecked()) {
                    videoListActionText.setText(getResources().getString(R.string.action_on));
                    watchVideo = false;
                } else {
                    videoListActionText.setText(getResources().getString(R.string.action_off));
                    watchVideo = true;
                }
            }
        });

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

                        if (!matcher.matches()) {
                            videoListActionText.setVisibility(View.VISIBLE);
                            videoListAction.setVisibility(View.VISIBLE);
                            new VideoSearch().execute();
                        }
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });
        //location is storage/emulated/0/Android/data/files
    }

    public void setupRecyclerView()
    {
        RecyclerView recyclerView = findViewById(R.id.videoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VideoAdapter(this, videoTitles, videoChannels, videoThumbnailURLs, videoIDs);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(View view, int position) {
        String id = adapter.getID(position);
        if (watchVideo) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            try {
                startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                startActivity(webIntent);
            }
        }  else {
            new VideoDownload().execute(id);
        }
    }


    public class VideoDownload extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params) {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket ControlSocket = null;
            DataOutputStream outToServer = null;
            DataInputStream inFromServer = null;
            try {
                int port1 = 12345;
                String ip = "192.168.0.7";
               // String ip = "localhost";
                ControlSocket = new Socket(ip, port1);
                outToServer = new DataOutputStream(ControlSocket.getOutputStream());
                inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                System.out.println("You are connected to the raspberry pi!");
                System.out.println(ip);

                System.out.print("Enter a URL:\t");
                // Send the command to the server.
                outToServer.writeBytes(params[0] + '\n');
                // Read status code with readInt(), which blocks until all 3 bytes read.
                //Need only 3 bytes, as the status is 3 characters long
                byte[] statusBytes = new byte[3];
                inFromServer.read(statusBytes);
                int statusCode = Integer.parseInt(new String(statusBytes, "UTF-8"));

                // If status code is 550 (error).
                if (statusCode == 550) {
                    System.out.println("Did not work.");
                }
                else if (statusCode == 200)
                {
                    float percent_done = 0;
                    String tempPercent = "";
                    short count = 0;
                    while (count < 2) {
                        byte[] percentBytes = new byte[5];
                        inFromServer.read(percentBytes);
                        tempPercent = new String(percentBytes, "UTF-8").trim();
                        if (tempPercent.contentEquals("255")) {
                            ControlSocket.close();
                            return null;
                        }
                        outToServer.writeBytes("200" + '\n');
                        percent_done = Float.parseFloat(tempPercent);
                        if (tempPercent.equals("100")) {
                            count++;
                        }
                        System.out.println(percent_done);
                    }
                    //System.out.println(inFromServer.skip(10));

                    byte[] tempSizeBuffer = new byte[10];
                    inFromServer.read(tempSizeBuffer);
                    String tempSize = new String(tempSizeBuffer, "UTF-8").trim().replaceAll("[^\\d.]", "");;
                    int size = Integer.parseInt(tempSize);
                    System.out.println("Size is " + Integer.toString(size));


                    int time_to_wait = size / 10000000;
                    try
                    {
                        Thread.sleep(time_to_wait * 5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                   // DataInputStream inData = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                    //byte[] dataIn = new byte[size];

                    byte[] dataIn = new byte[16];
                    ArrayList<Byte> tempBytes = new ArrayList<Byte>();
                    System.out.println("Reading");
                    while (inFromServer.available() > 0)
                    {
                        inFromServer.read(dataIn);
                        for (byte tb : dataIn)
                            tempBytes.add(tb);
                    }

                    System.out.println(tempBytes.size());

                    // Reads bytes from the inData stream and places them in dataIn byte array.
                    //inFromServer.readFully(dataIn); File structure different on linux vs windows
                    outToServer.writeBytes("200" + '\n');
                    // Use FileOutputStream to write byes to new file.
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {

                        //If it isn't mounted - we can't write into it.
                        return null;
                    }

                    //String filePath = System.getProperty("user.dir") + "/" + "test.mp4";
                    File file = new File(getExternalFilesDir(null), "test.mp4");
                    byte[] result = new byte[tempBytes.size()];
                    for (int i = 0; i < tempBytes.size(); i++)
                        result[i] = tempBytes.get(i);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(result);
                    }
                    System.out.println("Done");
                }
                ControlSocket.close();
            }
            catch (Exception e)
            {
                try{
                    System.out.println("Available: " + Integer.toString(inFromServer.available()));
                    e.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
            return null;
        }
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
                videoIDs = new ArrayList<String>();

                JSONObject objects = new JSONObject(result);
                JSONArray jArray = objects.getJSONArray("items");
                for(int i=0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);
                    JSONObject id = jObject.getJSONObject(getResources().getString(R.string.id_object));
                    JSONObject snippet = jObject.getJSONObject(getResources().getString(R.string.video_info_tag));
                    JSONObject thumbnails = snippet.getJSONObject(getResources().getString(R.string.thumbnail_info_tag));
                    JSONObject defaultThumbnail = thumbnails.getJSONObject(getResources().getString(R.string.thumbnail_size));

                    try {
                        String tempTitle = URLDecoder.decode(snippet.getString("title").replace("&#39;s", "'"), "UTF-8");
                        String tempChannel = URLDecoder.decode(snippet.getString("channelTitle").replace("&#39;s", "'"), "UTF-8");
                        String tempThumbnailURL = defaultThumbnail.getString("url");
                        String tempId = id.getString(getResources().getString(R.string.id_tag));

                        videoTitles.add(tempTitle);
                        videoChannels.add(tempChannel);
                        videoThumbnailURLs.add(tempThumbnailURL);
                        videoIDs.add(tempId);
                        //videoURLs.add()
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                setupRecyclerView();
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        }
    }
}
