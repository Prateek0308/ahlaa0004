package com.cst2335.ahla0004;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This class is the first page of the application
 *
 * @author 16139
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity {



    String stringUrl="https://api.openweathermap.org/data/2.5/weather?q=TORONTO&appid=7e943c97096a9784391a981c4d878b22&units=metric";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EditText cityTextField = findViewById(R.id.cityTextField);
        Button forecastbutton = findViewById(R.id.forecastbutton);


        forecastbutton.setOnClickListener(v ->
        {
            String cityName=cityTextField.getText().toString();
            AlertDialog dialog=new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Getting Forecast")
                    .setMessage("We are calling people in "+cityName+" to look outside their windows and tell us whats the weather like over there.")
                    .setView(new ProgressBar(MainActivity.this))
                    .show();

            Executor newThread = Executors.newSingleThreadExecutor();

            newThread.execute( () ->
            {

                try
                {

                    stringUrl= "https://api.openweathermap.org/data/2.5/weather?q="+URLEncoder.encode(cityName,"UTF-8")+"&appid=7e943c97096a9784391a981c4d878b22&units=metric";
                    URL url = new URL(stringUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String text = (new BufferedReader(
                            new InputStreamReader(in, StandardCharsets.UTF_8)))
                            .lines()
                            .collect(Collectors.joining("\n"));


                    JSONObject theDocument = new JSONObject( text);

                    JSONArray weatherArray = theDocument.getJSONArray ( "weather" );
                    JSONObject position0=weatherArray.getJSONObject(0);
                    JSONObject mainObject = theDocument.getJSONObject( "main" );

                    String description=position0.getString("description");
                    String iconName=position0.getString("icon");
                    double current=mainObject.getDouble("temp");
                    double min=mainObject.getDouble("temp_min");
                    double max=mainObject.getDouble("temp_max");
                    int humidity=mainObject.getInt("humidity");
                    Bitmap image;
                    File file=new File(iconName+".png");
                    if(file.exists())
                    {
                        image=BitmapFactory.decodeFile(getFilesDir()+"/"+iconName+".png");
                    }
                    else
                    {
                        image=downloadImage(iconName);
                    }




                    runOnUiThread(() -> {

                        TextView txttemp=findViewById(R.id.temp);
                        TextView txtmaxtemp=findViewById(R.id.maxtemp);
                        TextView txtmintemp=findViewById(R.id.mintemp);
                        TextView txthumidity=findViewById(R.id.humidity);
                        TextView txtdescription=findViewById(R.id.description);

                        ImageView imgIcon=findViewById(R.id.icon);

                        txttemp.setText("The current temperature is "+current);
                        txtmaxtemp.setText("The max temperature is "+max);
                        txtmintemp.setText("The min temperature is "+min);
                        txthumidity.setText("The humidity is "+humidity);
                        txtdescription.setText(description);




                        imgIcon.setImageBitmap(image);

                        dialog.hide();
                    });


                }
                catch (IOException | JSONException e)
                {
                    e.printStackTrace();

                    e.printStackTrace();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run () {
                            dialog.hide();
                            Toast.makeText(MainActivity.this,"No Forecast found for "+cityName,Toast.LENGTH_LONG).show();
                        }
                    });
                }



            } );



        });


    }


    public  Bitmap  downloadImage(String iconName)
    {

        try
        {
            Bitmap image = null;
            URL imgUrl = new URL( "https://openweathermap.org/img/w/" + iconName + ".png" );
            HttpURLConnection connection = (HttpURLConnection) imgUrl.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                image = BitmapFactory.decodeStream(connection.getInputStream());
                saveImage(image,iconName);
                return image;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public  void  saveImage(Bitmap image,String iconName)
    {
        FileOutputStream fOut = null;
        try {
            fOut = openFileOutput( iconName + ".png", Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }




}