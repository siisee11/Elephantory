package skku.edu.elephantory;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.iconics.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResultActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final int PICKFILE_RESULT_CODE = 42;
    private static final String TAG = "RESULT";

    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        textView = (TextView) findViewById(R.id.textView_result);

//        performFileSearch();

        Button read_button = findViewById(R.id.read_button);
        read_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
            }
        });
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                // User pick the file
                Uri uri = data.getData();
                String fileContent = readTextFile(uri);
                Toast.makeText(this, fileContent, Toast.LENGTH_LONG).show();
            } else {
                Log.i(TAG, data.toString());
            }
        }
    }

    public void LaterFunction(Uri uri) {
        BufferedReader br;
        FileOutputStream os;
        try {
            br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            //WHAT TODO ? Is this creates new file with
            //the name NewFileName on internal app storage?
            os = openFileOutput("newFileName", Context.MODE_PRIVATE);
            String line = null;
            while ((line = br.readLine()) != null) {
                os.write(line.getBytes());
                Log.w("nlllllllllllll",line);
            }
            br.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readTextFile(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}

