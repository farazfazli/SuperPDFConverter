package com.farazfazli.superpdfconverter;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    private EditText urlField;
    private Button convertToPDFButton;
    private Button viewHistory;
    private Button deleteHistory;
    private TextView history;
    private String website;
    private String url;
    private URL urlToConvert;
    private Uri uriToConvert;
    private DownloadManager manager;
    private long downloadId;
    private static final String convertToPDF = "http://www.FreeHTMLtoPDF.com/?convert=";
    private static final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/SuperPDFConverter/";
    public File directoryPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/SuperPDFConverter/");
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlField = (EditText) findViewById(R.id.urlField);
        history = (TextView) findViewById(R.id.history);

        convertToPDFButton = (Button) findViewById(R.id.grabPdf);
        convertToPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                website = urlField.getText().toString();
                url = convertToPDF + website;
                convertToPDF(url);
            }
        });

        viewHistory = (Button) findViewById(R.id.viewHistory);
        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileChooser.class);
                startActivity(intent);
            }
        });

        deleteHistory = (Button) findViewById(R.id.deleteHistory);
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning!");
                builder.setMessage("Are you sure you want to delete your converted PDFs?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (count == 0) {
                            Toast.makeText(MainActivity.this, "Nothing to delete!", Toast.LENGTH_SHORT).show();
                        } else {
                            deleteDirectory(directoryPath);
                            history.setText("No PDFs Saved!");
                            Toast.makeText(MainActivity.this, "Deleted files!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        getFile();
    }
    public void convertToPDF(String url) {
            convertToPDFButton.setText("Wait!");
            convertToPDFButton.setClickable(false);
            convertToPDFButton.setAlpha(.5f);
            try {
                urlToConvert = new URL(url);
                uriToConvert = Uri.parse(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (uriToConvert != null) {
                DownloadManager.Request request = new DownloadManager.Request(uriToConvert);
                request.setTitle(website + ".pdf");
                request.setDescription("PDF for: " + website);
                request.setMimeType("application/pdf");
// in order for this if to run, you must use the android 3.2 to compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/SuperPDFConverter/", website + ".pdf");
// get download service and enqueue file
                manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                downloadId = manager.enqueue(request);
                try {
                    urlToConvert = new URL(url);
                    uriToConvert = Uri.parse(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                PackageManager packageManager = this.getPackageManager();
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {
                        getFile();
                        openPDF();
                        convertToPDFButton.setText("Grab PDF!");
                        convertToPDFButton.setClickable(true);
                        convertToPDFButton.setAlpha(1f);
                    }
                };
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                MainActivity mainActivity = new MainActivity();
            }
        }

        public void openPDF() {
            getFile();
            File pdf = new File(path + website + ".pdf");
            if (pdf.exists()) {
                Uri path = Uri.fromFile(pdf);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this,
                            "No application available to view PDF!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    public void getFile() {
        File f = new File(path);
        File[] files = f.listFiles();
        count = 0;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                count++;
//                File file = files[i];

//                if (file.isDirectory()) {
//                    getFile(file.getAbsolutePath());
//                }
            }

            if (count != 0){
                if (count == 1){
                    history.setText(count + " PDF!\nPress View History to view it!");
                } else if(count > 1){
                    history.setText(count + " PDFs\nPress View History to view them!");
                }
            }
        }
        else {
            history.setText("No PDFs saved!");
        }
    }
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return(path.delete());
    }

/*    @Override
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
    }*/
}
