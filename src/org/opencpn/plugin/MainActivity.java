package org.opencpn.blahblah;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {

    Button installButton;
    TextView statusText;
    Activity mActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        setContentView(R.layout.activity_main);

        try {
            String dir = getExternalCacheDir().getAbsolutePath();

            final File path = new File(getExternalCacheDir(), "OCPN_logs");
            if (!path.exists()) {
                path.mkdir();
            }
            String spath = path.getAbsolutePath() + File.separator + "oapi_logcat" + ".txt";

            final File oldFile = new File(spath);
            if(oldFile.exists()){
                Log.i("oapi", "Delete logfile: " + spath);
                oldFile.delete();
            }

            Runtime.getRuntime().exec( "logcat " + "-f " + spath + " -s oapi ");

        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean bCompat = checkAppCompatibility();
        if(!bCompat){
            ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertTheme1);
            AlertDialog.Builder alert = new AlertDialog.Builder(ctw);
            alert.setTitle("Fugawi Charts Plugin");

            String message = getResources().getString(R.string.incompatible_dialog2);

            // Set the message and the  buttons.
            alert.setMessage( message );

            alert.setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            finish();
                        }
                    });

            // show it
            alert.show();

            installButton = (Button) findViewById(R.id.buttonInstall);
            installButton.setVisibility(View.GONE);

            return;
        }




        statusText = (TextView)findViewById(R.id.statusText);

        statusText.setText(R.string.prepare_string);


 
        // Move the required files from assets to cache directory
        // Also build the lst of files to transport by zip file
        List<String> fileList = new ArrayList<String>();

        try {

            final AssetManager assets = getAssets();

            // Find the plugin .so library in the asset root
            String[] assetNames = assets.list( "" );

            if(assetNames.length > 0) {
                for (int i = 0; i < assetNames.length; i++) {
                    if(assetNames[i].endsWith(".so")) {      // this is the plugin library
                        String fileName = getCacheDir() + "/plugins/" +assetNames[i];

                        fileList.add(fileName);
                        relocateAssetFile(getApplicationContext(), assetNames[i], "plugins", assetNames[i]);
                    }
                }
            }

            // Find the locale translations (.mo) in the asset "locale: folder
            assetNames = assets.list( "locale" );

            if(assetNames.length > 0) {
                for (int i = 0; i < assetNames.length; i++) {
                    String[] localeNames = assets.list( "locale/" + assetNames[i] );
                    if(localeNames.length > 0){
                        String moFile = "locale/" + assetNames[i] + "/" + localeNames[0];
                        String listFile = getCacheDir() + "/locale/" + assetNames[i] + "/LC_MESSAGES/opencpn-ofc_pi.mo";
                        fileList.add(listFile);

                        relocateAssetFile(getApplicationContext(), moFile, "locale/" + assetNames[i] + "/LC_MESSAGES", "opencpn-ofc_pi.mo");

                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        // Make the zip file
        zip(fileList, getCacheDir() + "/oapi_plugin.zip");

        statusText.setText(R.string.ready_string);

        addListenerOnButtonInstall();


    }


    public boolean checkAppCompatibility()
    {
        boolean bVal = false;
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++)
        {
            PackageInfo packInfo = packList.get(i);
            if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                if(appName.equals("OpenCPN")){
                    if(packInfo.versionCode >= 18){
                        bVal = true;
                    }
                }
                Log.e("App № " + Integer.toString(i), appName);
            }
        }

        return bVal;
    }


    public void addListenerOnButtonInstall() {

        installButton = (Button) findViewById(R.id.buttonInstall);

        installButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {


                statusText.setText(R.string.installing_string);
                installButton.setVisibility(View.INVISIBLE);


                sendFile("oapi_plugin.zip");


                statusText.setText(R.string.wait_confirm);



            }

        });

    }

    public String showHTMLAlertDialog( String title, String htmlString) {

        WebView wv = new WebView(getApplicationContext());

        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setMinimumFontSize(50);
        wv.loadData(htmlString, "text/html", "utf-8");


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                    }
                })
                .setNegativeButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setView(wv);

        // show it
        alertDialog.show();

        return ("OK");

    }

    public String displayHTMLAlertDialog( final String htmlString) {
        Log.i("oapi", "displayHTMLAlertDialog" + htmlString);


        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showHTMLAlertDialog( "Test", htmlString );

                }});
        }

        String ret = "OK";
        return ret;
    }



    public void Ointent(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "send to "));
    }

    public static File relocateAssetFile(Context context, String assetFile, String destDir, String destFile ) throws IOException {
        File cacheDir = new File(context.getCacheDir() + File.separator + destDir);
        if (!cacheDir.exists())
            cacheDir.mkdirs();


        File cacheFile = new File(context.getCacheDir() + File.separator + destDir + File.separator + destFile);

        try {
            InputStream inputStream = context.getAssets().open(assetFile);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open: " + assetFile, e);
        }
        return cacheFile;
    }

    public void sendFile(String filename) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setClassName("org.opencpn.opencpn", "org.opencpn.opencpn.OCPNPluginInstallerActivity");

        intent.setType("text/plain");

        String dirpath = String.valueOf(getApplicationContext().getCacheDir());
        File file = new File(dirpath + File.separator + filename);
        Uri uri = FileProvider.getUriForFile(this, getPackageName(), file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Workaround for Android bug.
        // grantUriPermission also needed for KITKAT,
        // see https://code.google.com/p/android/issues/detail?id=76683
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getApplicationContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }


        try {
            startActivityForResult(intent, 0);
        } catch ( ActivityNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void revokeFileReadPermission(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            String dirpath = context.getFilesDir() + File.separator + "directory";
            File file = new File(dirpath + File.separator + "file.txt");
            Uri uri = FileProvider.getUriForFile(context, "com.package.name.fileprovider", file);
            context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }



    public void zip(List<String> _files, String zipFileName) {
        try {
            int BUFFER = 1024;

            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.size(); i++) {
                String item = _files.get(i);
                Log.v("oapi", "Adding: " + item);
                FileInputStream fi = new FileInputStream(item);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(item.substring(item.lastIndexOf("/cache") + 7));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                statusText.setText(R.string.install_confirmed);
                //TextView restartView = (TextView) findViewById(R.id.restartText);
                //restartView.setVisibility(View.VISIBLE);

                installButton = (Button) findViewById(R.id.buttonInstall);
                installButton.setEnabled(false);
                installButton.setVisibility(View.INVISIBLE);


                Timer T=new Timer();
                T.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);


            }
        }
    }


    private void copyFile(InputStream inputStream, OutputStream outputStream)
            throws IOException
    {
        byte[] buffer = new byte[1024];

        int count;
        while ((count = inputStream.read(buffer)) > 0)
            outputStream.write(buffer, 0, count);
    }


}
