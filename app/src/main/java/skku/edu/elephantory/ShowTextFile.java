package skku.edu.elephantory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShowTextFile extends AppCompatActivity {
    TextView txtRead;
    final static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sample/logfile.txt";
    String job_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_text_file);

        job_id = getIntent().getStringExtra("job_id");
        Log.d("ShowTextFile", "job_id: " + job_id);

        txtRead = (TextView)findViewById(R.id.textView_showResult);

        mOnFileRead(job_id);
    }

    public void mOnFileRead(String job_id){
        //Log.d("ShowTextFile", "/////// 파일경로: " + filePath);
        String filePath = job_id + ".txt";
        String read = ReadTextFile(filePath);
        txtRead.setText(read);
    }

    //경로의 텍스트 파일읽기
    public String ReadTextFile(String job_id){
        StringBuffer strBuffer = new StringBuffer();
        FileInputStream fis = null;
        try{
            //InputStream is = new FileInputStream(path);

            fis = openFileInput(job_id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line="";
            while((line=reader.readLine()) != null){
                strBuffer.append(line + "\n");
            }

            reader.close();
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
            return "";
        }
        return strBuffer.toString();
    }

}
