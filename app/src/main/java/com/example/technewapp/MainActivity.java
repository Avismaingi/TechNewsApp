package com.example.technewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> articleIds = new ArrayList<String>();
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> link = new ArrayList<String>();

    ArrayAdapter<String> arrayAdapter;

    ListView listView;

    SQLiteDatabase myDatabase;


    public void refresh(View view) {
        articleIds.clear();
        titles.clear();
        link.clear();
        listView.setAdapter(null);

        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, link VARCHAR)");
        myDatabase.execSQL("DELETE FROM articles");

        DownloadTask task1 = new DownloadTask();
        String url = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
        String result = null;
        try {
            result = task1.execute(url).get();
            result = result.substring(1, result.length() - 1);
            ArrayList<String> tempArticleIds = new ArrayList<String>(Arrays.asList(result.split(", ")));

            Random rand = new Random();

            int numberOfElements = 15;

            for (int i = 0; i < numberOfElements; i++) {
                int randomIndex = rand.nextInt(tempArticleIds.size());
                String randomElement = tempArticleIds.get(randomIndex);
                articleIds.add(randomElement);
                tempArticleIds.remove(randomIndex);
            }
//            Log.i("Result", String.valueOf(articleIds));

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (articleIds.size() > 0) {
            for (int i = 0; i < articleIds.size(); i++) {
                DownloadTask task2 = new DownloadTask();
                String newUrl = "https://hacker-news.firebaseio.com/v0/item/" + articleIds.get(i) + ".json?print=pretty";

                String newResult = null;
                try {
                    newResult = task2.execute(newUrl).get();

                    Pattern p1 = Pattern.compile("\"title\" : \"(.*?)\",");
                    Matcher m1 = p1.matcher(newResult);
                    while (m1.find()) {
                        titles.add(m1.group(1));
                    }

                    Pattern p2 = Pattern.compile("\"url\" : \"(.*?)\"");
                    Matcher m2 = p2.matcher(newResult);
                    while (m2.find()) {
                        link.add(m2.group(1));
                    }

                    String sql = "INSERT INTO articles(articleId, title, link) VALUES (?, ?, ?)";
                    SQLiteStatement statement = myDatabase.compileStatement(sql);
                    statement.bindString(1, articleIds.get(i));
                    statement.bindString(2, titles.get(i));
                    statement.bindString(3, link.get(i));

                    statement.execute();

//                    Log.i("Titles", String.valueOf(titles));
//                    Log.i("URLS", String.valueOf(link));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.i("Link", String.valueOf(i));
                Log.i("Link", link.get(i));
                Intent intent = new Intent(getApplicationContext(), webActivity.class);
                intent.putExtra("link", link.get(i));
                startActivity(intent);
            }
        });

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        myDatabase = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, link VARCHAR)");

        Cursor c = myDatabase.rawQuery("SELECT * FROM articles", null);
        int titleIndex = c.getColumnIndex("title");
        int linkIndex = c.getColumnIndex("link");

        if (c.moveToFirst()) {
            Log.i("Test", "Code reached here bro");
            do {
                titles.add(c.getString(titleIndex));
                link.add(c.getString(linkIndex));
            } while (c.moveToNext());

        } else {

            DownloadTask task1 = new DownloadTask();
            String url = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
            String result = null;
            try {
                result = task1.execute(url).get();
                result = result.substring(1, result.length() - 1);
                ArrayList<String> tempArticleIds = new ArrayList<String>(Arrays.asList(result.split(", ")));

                Random rand = new Random();

                int numberOfElements = 15;

                for (int i = 0; i < numberOfElements; i++) {
                    int randomIndex = rand.nextInt(tempArticleIds.size());
                    String randomElement = tempArticleIds.get(randomIndex);
                    articleIds.add(randomElement);
                    tempArticleIds.remove(randomIndex);
                }
//            Log.i("Result", String.valueOf(articleIds));

            } catch (Exception e) {
                e.printStackTrace();
            }


            if (articleIds.size() > 0) {
                myDatabase.execSQL("DELETE FROM articles");
                for (int i = 0; i < articleIds.size(); i++) {
                    DownloadTask task2 = new DownloadTask();
                    String newUrl = "https://hacker-news.firebaseio.com/v0/item/" + articleIds.get(i) + ".json?print=pretty";

                    String newResult = null;
                    try {
                        newResult = task2.execute(newUrl).get();

                        Pattern p1 = Pattern.compile("\"title\" : \"(.*?)\",");
                        Matcher m1 = p1.matcher(newResult);
                        while (m1.find()) {
                            titles.add(m1.group(1));
                        }

                        Pattern p2 = Pattern.compile("\"url\" : \"(.*?)\"");
                        Matcher m2 = p2.matcher(newResult);
                        while (m2.find()) {
                            link.add(m2.group(1));
                        }

                        String sql = "INSERT INTO articles(articleId, title, link) VALUES (?, ?, ?)";
                        SQLiteStatement statement = myDatabase.compileStatement(sql);
                        statement.bindString(1, articleIds.get(i));
                        statement.bindString(2, titles.get(i));
                        statement.bindString(3, link.get(i));

                        statement.execute();
                        Log.i("Titles", String.valueOf(titles));
                        Log.i("URLS", String.valueOf(link));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.i("Link", String.valueOf(i));
                Log.i("Link", link.get(i));
                Intent intent = new Intent(getApplicationContext(), webActivity.class);
                intent.putExtra("link", link.get(i));
                startActivity(intent);
            }
        });
    }
}