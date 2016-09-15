package com.pinak.currenttrends;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String mfilePath;
    private Button button;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DownloadData downloadData=new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=25/xml");
        button=(Button) findViewById(R.id.pressButton);
        listView=(ListView) findViewById(R.id.listView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                XmlParseApplication parseApplication=new XmlParseApplication(mfilePath);
                parseApplication.process();
                ArrayAdapter<ParseApplication> arrayAdapter=new ArrayAdapter<ParseApplication>(MainActivity.this,
                        R.layout.list_items,parseApplication.getList());
                listView.setAdapter(arrayAdapter);
            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class DownloadData extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... strings) {

            mfilePath = downloadXML(strings[0]);
            if (mfilePath == null) {
                Log.d("Download Data: ", "mfilePath is empty");
            }
            return mfilePath;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Download Data: ", "onPostExecute " + result);
        }

        private String downloadXML(String param) {
            StringBuilder tempBuilder = new StringBuilder();
            try {
                URL url = new URL(param);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d("Download Data: ", "Response code" + String.valueOf(response));
                InputStream is = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                int charRead;
                char[] inputbuffer = new char[500];
                while (true) {
                    charRead = inputStreamReader.read(inputbuffer);
                    if (charRead <= 0) {
                        break;
                    }
                    tempBuilder.append(String.copyValueOf(inputbuffer, 0, charRead));
                }

                return tempBuilder.toString();
            } catch (IOException e) {
                Log.d("Download Data: ", "mfilePath is empty" + e.getMessage());
            }
            catch (SecurityException e)
            {
                Log.d("Download Data: ", "Security exception need permission" + e.getMessage());
            }
            return null;
        }
    }
}
