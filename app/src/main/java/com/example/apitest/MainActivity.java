// MainActivity.java

package com.example.apitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    // 닉네임 체크를 위한 응답을 받는 INTERFACE만들기
    interface  RequestCheckNickName{
        // Get 요청을 주는 주소는 /api/check/nickname.php
        @GET("/api/check/nickname.php")
        Call<Retrofit_Class> checkNickname(@Query("nickname") String nickname);
    }

    private RequestCheckNickName requestNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText text = findViewById(R.id.editTextText);

        // HtttpUrLConnection 버튼
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // StrictMode 설정: 네트워크 작업을 메인 스레드에서 실행 (테스트용)
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                try {
                    URL url = new URL("http://192.168.0.11:5600/api/check/nickname.php?nickname=" + text.getText());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();


                        // JSON 파싱
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String message = "HttpUrLConnection : " + jsonResponse.getString("message");
                        boolean result = jsonResponse.getBoolean("result");
                        boolean error = jsonResponse.getBoolean("error");

                        System.out.println("message : " + message);

                        // UI 업데이트 (Toast 메시지)
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "GET request failed", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // retrofit 버튼
        Button button2 = findViewById(R.id.button2);

        // Retrofit 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.11:5600")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        requestNickName = retrofit.create(RequestCheckNickName.class);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String nickname = text.getText().toString();
                    requestNickName.checkNickname(nickname).enqueue(new Callback<Retrofit_Class>() {
                        @Override
                        public void onResponse(Call<Retrofit_Class> call, Response<Retrofit_Class> response) {

                            String message = "Retrofit : " + response.body().message.toString();
                            boolean result = response.body().result.booleanValue();
                            boolean error = response.body().error.booleanValue();

                            System.out.println("message : " + message);

                            // UI 업데이트 (Toast 메시지)
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());

                        }

                        @Override
                        public void onFailure(Call<Retrofit_Class> call, Throwable t) {

                        }
                    });
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });


        // OkHttp 버튼
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();
                String nickname = text.getText().toString();
                String url = "http://192.168.0.11:5600/api/check/nickname.php?nickname=" + nickname;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Request failed", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                String message = "OkHttp: " + jsonResponse.getString("message");
                                boolean result = jsonResponse.getBoolean("result");
                                boolean error = jsonResponse.getBoolean("error");

                                System.out.println("message: " + message);

                                // UI 업데이트 (Toast 메시지)
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "GET request failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        });


    }

}
