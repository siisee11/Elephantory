package skku.edu.elephantory;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MapReduceRunner extends AppCompatActivity {
    public static final int port = 54900;
    public static final String tcpHost = "192.168.0.34";
    String TAG = "socketTest";
    public static Socket socket;

    public Button startButton;
    public Button grepButton;
    public Button wcButton;
    public Button sortButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapreduce);

        startButton = (Button)findViewById(R.id.buttonMRStart);
        grepButton = (Button)findViewById(R.id.buttonGrep);
        wcButton = (Button)findViewById(R.id.buttonWC);
        sortButton = (Button)findViewById(R.id.buttonSort);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        startButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                AnalyzeThread analyzeThread = new AnalyzeThread("hello.sh");
                analyzeThread.start();
            }

        });

        grepButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapReduceRunner.this);

                builder.setTitle("Put InputFile").setMessage("반갑습니다");
                builder.setPositiveButton("확인", null);
                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();

                alertDialog.show();
                AnalyzeThread analyzeThread = new AnalyzeThread("run_grep.sh");
                analyzeThread.start();
            }

        });

        wcButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                AnalyzeThread analyzeThread = new AnalyzeThread("run_wc.sh");
                analyzeThread.start();
            }

        });

        sortButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                AnalyzeThread analyzeThread = new AnalyzeThread("run_sort.sh");
                analyzeThread.start();
            }

        });
    }

    class AnalyzeThread extends Thread {
        public String hadoopCmd;

        public AnalyzeThread(String Cmd){
            hadoopCmd = Cmd;
        }

        public void connectToHadoopCluster(String hostName, int port) throws IOException {
            Log.d(TAG, "### Read To Socket Connect : " + hostName + " " + port);
            socket = new Socket(hostName, port);
            Log.d(TAG, "### Success To Socket Connect : " + hostName + " " + port);

        }

        public void disconnectToHadoopCluster() throws IOException {
            socket.close();
        }

        public void setHadoopCommand(String _hadoopCmd){
            hadoopCmd += " ";
            hadoopCmd += _hadoopCmd;
        }

        public void sendHadoopCommand() throws IOException {
            BufferedWriter writeBuf = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));

            writeBuf.write(hadoopCmd);
            writeBuf.newLine();
            writeBuf.flush();
        }

        public void getHadoopOutput() throws IOException {

            BufferedReader readBuf = new BufferedReader(new InputStreamReader( socket.getInputStream()));

            String output = readBuf.readLine();
        }

        public void run(){
            try{

                this.connectToHadoopCluster(tcpHost, port);
                this.setHadoopCommand("hello.sh");
                this.sendHadoopCommand();
                this.disconnectToHadoopCluster();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}