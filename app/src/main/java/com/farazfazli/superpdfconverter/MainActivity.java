package com.farazfazli.superpdfconverter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends Activity {
    private static final String convertToPDF = "http://www.FreeHTMLtoPDF.com/?convert=";
    private static final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/SuperPDFConverter/";
    public String url;
    public File directoryPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/SuperPDFConverter/");
    private EditText urlField;
    private Button convertToPDFButton;
    private Button viewHistory;
    private Button deleteHistory;
    private TextView history;
    private String website;
    private URL urlToConvert;
    private Uri uriToConvert;
    private DownloadManager manager;
    private long downloadId;
    private int count = 0;

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

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
                try {
                    url = convertToPDF + URLEncoder.encode(website, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(MainActivity.this, "Must be a valid URL!", Toast.LENGTH_SHORT).show();
                }
                if (website.length() > 3 && Patterns.WEB_URL.matcher(website).matches()) {
                    convertToPDF(url);
                } else {

                    Toast.makeText(MainActivity.this, "Must contain more than 3 characters be a valid URL!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHistory = (Button) findViewById(R.id.viewHistory);
        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AndroidFileBrowser.class);
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
        updateNumberOfFiles();
    }

    public void updateNumberOfFiles() {
        File f = new File(path);
        File[] files = f.listFiles();
        count = 0;
        if (files != null) {
            for (File file : files) {
                count++;
            }

            if (count != 0){
                if (count == 1){
                    history.setText(count + " PDF converted ran!\nPress View History to view it!");
                } else if(count > 1){
                    history.setText(count + " PDFs Converted!\nPress View History to view them!");
                }
            }
        }
        else {
            history.setText("No history available!");
        }
    }

    public void convertToPDF(String url) {
        Intent intent = new Intent(this, PDFManager.class);
        intent.putExtra("WEBSITE_NAME", website);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    public void setPath(String filePath) {
        history.setText("Path to file: " + filePath);
    }

    public void openFile(String pdfFilename) {
        File pdf = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/SuperPDFConverter/" + pdfFilename);
        if (pdf.exists()) {
            setPath(pdf.getPath());
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
/*                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("http://docs.google.com/gview?embedded=true&url=" + url));
                startActivity(intent);
            }
*/
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error!")
                    .setMessage("PDF not found! Please try again!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNumberOfFiles();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("pdf_filename")) {
                String pdfFilename = extras.getString("pdf_filename");
                extras.remove("pdf_filename");
                if (!pdfFilename.isEmpty()) {
                    openFile(pdfFilename);
                    getIntent().removeExtra("pdf_filename");
                    pdfFilename = "";
                }
            }
        }
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
