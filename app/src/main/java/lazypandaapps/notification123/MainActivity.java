package lazypandaapps.notification123;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    int file_size;
    private NotificationManager mNotifyManager;
    private Notification.Builder mBuilder;
    int id = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = (Button) findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                EditText etUrl = (EditText) findViewById(R.id.editText);
                Downloader downloadTask = new Downloader();

                downloadTask.execute(etUrl.getText().toString());


                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new Notification.Builder(MainActivity.this);
                mBuilder.setContentTitle("Download")
                        .setContentText("Download in progress")
                        .setSmallIcon(R.mipmap.ic_launcher);

                new Downloader().execute();
            }
        });
    }

    private boolean isNetworkAvailable(){
        boolean available = false;
        /** Getting the system's connectivity service */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        /** Getting active network interface  to get the network's status */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo !=null && networkInfo.isAvailable())
            available = true;

        /** Returning the status of the network */
        return available;
    }

    private Bitmap downloadUrl(String strUrl) throws IOException {
        Bitmap bitmap=null;
        InputStream iStream = null;
        try{


            URL url = new URL(strUrl);
            /** Creating an http connection to communcate with url */
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            /** Connecting to url */
            urlConnection.connect();
            file_size = (urlConnection.getContentLength())/1024;

            /** Reading data from url */
            iStream = urlConnection.getInputStream();

            /** Creating a bitmap from the stream returned from the url */
            bitmap = BitmapFactory.decodeStream(iStream);

        }catch(Exception e){
            Log.d("Download Exception", e.toString());
        }finally{
            iStream.close();
        }
        return bitmap;
    }

    private class Downloader extends AsyncTask<String, Integer, Bitmap> {
            Bitmap bitmap=null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }
        @Override
        protected Bitmap doInBackground(String... url) {
            try{
                bitmap = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            int i;
            for (i = 0; i <= file_size; i += 50)
                // Sets the progress indicator completion percentage
                publishProgress(Math.min(i, 100));
            try {
                // Sleep for 5 seconds
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                Log.d("TAG", "sleep failure");
            }
            return bitmap;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            mBuilder.setProgress(100, values[0], false);
            mNotifyManager.notify(id, mBuilder.build());
            super.onProgressUpdate(values);
        }

        /*@Override
        protected Integer doInBackground(Void... params) {
            int i;
            for (i = 0; i <= 100; i += 5) {
                // Sets the progress indicator completion percentage
                publishProgress(Math.min(i, 100));
                try {
                    // Sleep for 5 seconds
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.d("TAG", "sleep failure");
                }
            }
            return null;
        }*/

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
           // mBuilder.setContentText("Download complete");
            // Removes the progress bar
           // mBuilder.setProgress(0, 0, false);
           // mNotifyManager.notify(id, mBuilder.build());

            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
            ImageView iView = (ImageView) findViewById(R.id.imageView);

            /** Displaying the downloaded image */
            iView.setImageBitmap(result);

            /** Showing a message, on completion of download process */
            Toast.makeText(getBaseContext(), "Image downloaded successfully", Toast.LENGTH_SHORT).show();
        }
    }
}