package com.seoul.jiyeon.eyesofseoul;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class NewsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    TextToSpeech tts;
    TextView titleText;
    StringBuffer forAsynkTaskSb;
    String forAsynkTaskS;
    LinearLayout layout;
    GestureDetector gd = null;

    int count;
    int index = 0;
    int seq = 0;
    int display = 100; // 검색 결과 개수
    int intro = 0;

    String[] newstitle = new String[display];
    String[] newslink = new String[display];
    String[][] getAll = new String[display][2];
    String finalNewslink;
    String title, link;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gd.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        tts = new TextToSpeech(this,this);
        titleText = (TextView) findViewById(R.id.titleText);
        layout = (LinearLayout) findViewById(R.id.newslayout);
        permissionCheck();

        Intent intent = getIntent();
        seq = intent.getIntExtra("seq", 0);
        intro = intent.getIntExtra("intro",0);

        try {
            forAsynkTaskSb = new SearchAsynkTask().execute(forAsynkTaskS).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        forAsynkTaskS = forAsynkTaskSb.toString();

        String[][] newsarray;

        try {
            newsarray = newsJsonParser(forAsynkTaskS);
            int j=0;
            for(int i=0; i<count; i++) {

                Log.d("33333333333",newsarray[i][0]);
                Log.d("33333333333",newsarray[i][1]);
                if(newsarray[i][1].contains("https://news.naver.com")){
                    newstitle[j] = newsarray[i][0];
                    newslink[j] = newsarray[i][1];

                    if (newstitle[j].contains("<b>")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    } else if (newstitle[j].contains("</b>")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    } else if (newstitle[j].contains("&quot;")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    } else if (newstitle[j].contains("&amp;")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    } else if (newstitle[j].contains("&lt;")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    } else if (newstitle[j].contains("&gt;")) {
                        newstitle[j] = newstitle[j].replaceAll("<b>|</b>|&quot;|&amp;|&lt;|&gt;", "");
                    }
                    j++;
                }
            }

            for(int i=0; i<j; i++){
                Log.d("11111111111111",newstitle[i]);
                Log.d("2222222222222",newslink[i]);
            }

            if(newstitle[seq] == null){
                titleText.setText("목록 끝");
            }else{
                titleText.setText(newstitle[seq]);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        finalNewslink = newslink[seq];

        gd = new GestureDetector(layout.getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                startActivity(intent);
                finish();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), DescActivity.class);
                intent.putExtra("newslink", finalNewslink);
                intent.putExtra("seq",seq);
                intent.putExtra("intro",intro);
                startActivity(intent);
                finish();
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
                seq += 1;
                intent.putExtra("seq",seq);
                intent.putExtra("intro",intro);
                startActivity(intent);
                finish();
                super.onLongPress(e);
            }

        });
    }

    public String[][] newsJsonParser(String jsonString) throws JSONException {

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            count = jsonArray.length();

            for(int i=0; i<count; i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);

                getAll[i][0] = jObject.getString("title");
                getAll[i][1] = jObject.getString("link");

            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return getAll;

    }

    private void permissionCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onInit(int i) {
        String tts_desc;

        if(newstitle[seq] == null){
            tts_desc = "목록이 끝났습니다. 새로운 뉴스를 들으시려면 앱을 잠시 후 다시 실행해주세요. 초기 메뉴로 돌아가시려면 화면을 두 번 터치하세요.";
        }else {
            tts_desc = newstitle[seq]+". 이 뉴스를 들으시려면 한 번 터치, 다른 뉴스를 들으시려면 길게 터치해주세요";
        }

        tts.speak(tts_desc, TextToSpeech.QUEUE_FLUSH,null);
    }


    class SearchAsynkTask extends AsyncTask<String, String, StringBuffer> {
        @Override
        protected StringBuffer doInBackground(String... search) {
            StringBuffer sb = new StringBuffer();
            String clientID = "qoV_o0JFXTXLeGQHxVp5";
            String clientSecret = "NB1rRZLIVm";


            try {
                String text = URLEncoder.encode("서울시","UTF-8");
                String apiURL = "https://openapi.naver.com/v1/search/news?query="+ text + "&display=" + display + "&"; // JSON 결과

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientID);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200){  // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {    // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

            }catch (Exception e){
                e.printStackTrace();
            }

            return sb;
        }

        @Override
        protected void onPostExecute(StringBuffer jsonString) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}