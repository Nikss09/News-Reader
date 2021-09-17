package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> title_list=new ArrayList<String>();
    static ArrayList<String> url_list=new ArrayList<String>();
    static ArrayAdapter<String> arrayAdapter;

    public class DownloadTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection urlConnection=null;
            try{
                url=new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                JSONArray jsonArray=new JSONArray(result);
                int numberOfItems=20;
                if(jsonArray.length()<20){
                    numberOfItems=jsonArray.length();
                }

                for(int i=0;i<numberOfItems;i++){
                    String articleId=jsonArray.getString(i);
                    url=new URL("https://hacker-news.firebaseio.com/v0/item/"+articleId+".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();

                    String articleInfo="";
                    while (data != -1) {
                        char current = (char) data;
                        articleInfo += current;
                        data = reader.read();
                    }
                    JSONObject jsonObject=new JSONObject(articleInfo);
                    if(!jsonObject.isNull("title")  && !jsonObject.isNull("url")){
                        String articleTitle=jsonObject.getString("title");
                        String articleUrl=jsonObject.getString("url");
                        title_list.add(articleTitle);
                        url_list.add(articleUrl);
                        /*url=new URL(articleUrl);
                        urlConnection= (HttpURLConnection) url.openConnection();
                        in=urlConnection.getInputStream();
                        reader=new InputStreamReader(in);
                        data = reader.read();

                        String articleContent="";
                        while (data != -1) {
                            char current = (char) data;
                            articleContent += current;
                            data = reader.read();
                        }

                        Log.i("HTML",articleContent);*/
                    }
                }

                Log.i("URL Content",result);
                for(int i=0;i<10;i++)
                    Log.i("******",""+title_list.get(i));
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ListView listView=findViewById(R.id.listView);
            TextView loading=findViewById(R.id.textView);

            arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,title_list);
            loading.setTextColor(Color.parseColor("#1E88E5"));
            loading.setTextSize(18);
            loading.setText("https://news.ycombinator.com/");
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent=new Intent(getApplicationContext(),webActivity.class);
                    intent.putExtra("url",url_list.get(i));
                    startActivity(intent);
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task=new DownloadTask();
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
            //arrayAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}