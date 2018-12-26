package me.dm7.barcodescanner.zxing.sample;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class FullScannerActivity extends BaseScannerActivity implements
        ZXingScannerView.ResultHandler {

    private static final String[] TARGET_STRINGS = {
            "AM002",
            "36626",
            "E4972651",
            "DF01365",
            "DF00122",
            "DF01135",
            "DF02893",
            "8526",
            "4340",
            "900",
            "1295",
            "5812",
            "8779",
            "13214",
            "40",
            "661",
            "848",
            "849",
            "1883",
            "8524",
            "8530",
            "8978",
            "13233",
            "13249",
            "13271",
            "15658",
            "15664",
            "19753",
            "20386",
            "20418",
            "34083",
            "54684",
            "60400",
            "61698",
            "66044",
            "1713472",
            "9002377",
            "21119011",
            "D1115648",
            "PAS355",
            "U2S152800650",
            "46",
            "1297",
            "20421",
            "20430",
            "44576"
    };

    private static final long VIBE_DURATION = 200;
    private static final long RESUME_CAPTURE_DELAY = 1500;

    private static List<String> TARGET_LIST;
    private static final String FLASH_STATE = "FLASH_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false);
        } else {
            mFlash = false;
        }

        setContentView(R.layout.activity_simple_scanner);
        setupToolbar();

        TARGET_LIST = Arrays.asList(TARGET_STRINGS);

        InputStream is = getResources().openRawResource(R.raw.allbarcodes);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch(Exception e){e.printStackTrace();}
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();


        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.DATA_MATRIX);
        mScannerView.setFormats(formats);

        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.startCamera(-1);
        mScannerView.setResultHandler(this);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;

        if(mFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, R.string.flash_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_flash:
                mFlash = !mFlash;
                if(mFlash) {
                    item.setTitle(R.string.flash_on);
                } else {
                    item.setTitle(R.string.flash_off);
                }
                mScannerView.setFlash(mFlash);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleResult(Result rawResult) {

        String orig = rawResult.getText();
        String trimmed = "";
        try{
            int trimmedInt = Integer.parseInt(orig);
            trimmed += trimmedInt;
        } catch (Exception e){}


        if(TARGET_LIST.contains(trimmed) || TARGET_LIST.contains(orig)){
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {}
            Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG).show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getSystemService(Vibrator.class).vibrate(VIBE_DURATION);
            }
            Toast.makeText(this, rawResult.getText(), Toast.LENGTH_SHORT).show();
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(FullScannerActivity.this);
            }
        }, RESUME_CAPTURE_DELAY);


    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}
