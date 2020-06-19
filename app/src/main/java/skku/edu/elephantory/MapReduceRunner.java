package skku.edu.elephantory;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapreduce);

        startButton = (Button)findViewById(R.id.buttonMRStart);
        grepButton = (Button)findViewById(R.id.buttonGrep);
        wcButton = (Button)findViewById(R.id.buttonWC);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        startButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                AnalyzeThread analyzeThread = new AnalyzeThread("");
                analyzeThread.start();
            }

        });

        grepButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String[] hCmd = {"run_grep.sh"};
                LinearLayout dialog_group = new LinearLayout((MapReduceRunner.this));
                dialog_group.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapReduceRunner.this);
                final EditText input = new EditText(getApplicationContext());
                input.setHint("Input");
                final EditText output = new EditText(getApplicationContext());
                output.setHint("Output");
                final EditText word = new EditText(getApplicationContext());
                word.setHint("Word");

                dialog_group.addView(input);
                dialog_group.addView(output);
                dialog_group.addView(word);
                builder.setView(dialog_group);
                builder.setTitle("Put InputFile");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputFile = input.getText().toString();
                        String wordToFind = word.getText().toString();
                        String outputFile = output.getText().toString();
                        hCmd[0] += (" /" + inputFile + " /" + outputFile + " \"" + wordToFind + "\"");
                        Log.d(TAG, hCmd[0]);
                        AnalyzeThread analyzeThread = new AnalyzeThread(hCmd[0]);
                        analyzeThread.start();
                    }
                });

                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }

        });

        wcButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String[] hCmd = {"run_wc.sh"};
                LinearLayout dialog_group = new LinearLayout((MapReduceRunner.this));
                dialog_group.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapReduceRunner.this);
                final EditText input = new EditText(getApplicationContext());
                input.setHint("Input");
                final EditText output = new EditText(getApplicationContext());
                output.setHint("Output");

                dialog_group.addView(input);
                dialog_group.addView(output);
                builder.setView(dialog_group);
                builder.setTitle("Put InputFile");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputFile = input.getText().toString();
                        String outputFile = output.getText().toString();
                        hCmd[0] += (" /" + inputFile + " /" + outputFile);
                        Log.d(TAG, hCmd[0]);
                        AnalyzeThread analyzeThread = new AnalyzeThread(hCmd[0]);
                        analyzeThread.start();

                        final Handler handler = new Handler();
                        new Thread(new Runnable() {
                            public void run() {
                                try{
                                    Thread.sleep(2000);
                                }
                                catch (Exception e) { } // Just catch the InterruptedException

                                handler.post(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MapReduceRunner.this);
                                        builder2.setTitle("find result");
                                        builder2.setMessage("WordCount Success\n");
                                        builder2.setPositiveButton("Save", null);
                                        builder2.setNegativeButton("Finish", null);
                                        AlertDialog alertDialog2 = builder2.create();

                                        alertDialog2.show();
                                    }
                                });
                            }

                        }).start();
                    }
                });

                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

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