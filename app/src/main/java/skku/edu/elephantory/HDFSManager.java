package skku.edu.elephantory;

import android.content.DialogInterface;
import android.os.Bundle;
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

public class HDFSManager extends AppCompatActivity {
    public static final int port = 54900;
    public static final String tcpHost = "192.168.0.34";
    String TAG = "socketTest";
    public final String hadoopInputDataPath = "/Users/ash1209/hadoop_input/";
    public static Socket socket;
    public Button uploadButton;
    public Button lsButton;
    public Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdfsmanager);

        uploadButton = (Button)findViewById(R.id.buttonUpload);
        lsButton = (Button)findViewById(R.id.buttonls);
        downloadButton = (Button)findViewById(R.id.buttonDownload);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        uploadButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String[] hCmd = {"upload.sh"};
                LinearLayout dialog_group = new LinearLayout((HDFSManager.this));
                dialog_group.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder builder = new AlertDialog.Builder(HDFSManager.this);
                final EditText input = new EditText(getApplicationContext());
                input.setHint("File Name");

                dialog_group.addView(input);
                builder.setView(dialog_group);
                //builder.setTitle("Put InputFile");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputFile = input.getText().toString();
                        hCmd[0] += (" " + hadoopInputDataPath + inputFile);
                        Log.d(TAG, hCmd[0]);


                        HDFSClientThread hdfsClientThread = new HDFSClientThread(hCmd[0]);
                        hdfsClientThread.start();
                    }
                });

                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }

        });

        lsButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                HDFSClientThread hdfsClientThread = new HDFSClientThread("ls.sh");
                hdfsClientThread.start();
            }

        });
        downloadButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String[] hCmd = {"download.sh"};
                LinearLayout dialog_group = new LinearLayout((HDFSManager.this));
                dialog_group.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder builder = new AlertDialog.Builder(HDFSManager.this);
                final EditText input = new EditText(getApplicationContext());
                input.setHint("File Name");

                dialog_group.addView(input);
                builder.setView(dialog_group);
                //builder.setTitle("Put InputFile");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputFile = input.getText().toString();
                        hCmd[0] += ("/"+inputFile);
                        Log.d(TAG, hCmd[0]);


                        HDFSClientThread hdfsClientThread = new HDFSClientThread(hCmd[0]);
                        hdfsClientThread.start();
                    }
                });

                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        });
    }

    class HDFSClientThread extends Thread {
        public String hadoopCmd;
        public HDFSClientThread(String _hadoopCmd){

            hadoopCmd = _hadoopCmd;
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
            hadoopCmd = _hadoopCmd;
        }

        public void sendHadoopCommand() throws IOException {
            BufferedWriter writeBuf = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));

            writeBuf.write(hadoopCmd);
            writeBuf.newLine();
            writeBuf.flush();
        }

        public String getHadoopOutput() throws IOException {

            if(hadoopCmd.equals("ls.sh")) {
                BufferedReader readBuf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String output = readBuf.readLine();
                return output;
            } else if(hadoopCmd.equals("download.sh")){
                /* Get File From Socket Connection */

                return "Download is Completed";
            }

            return "Success";

        }

        public void run(){
            try{
                String res;
                this.connectToHadoopCluster(tcpHost, port);
                this.sendHadoopCommand();
                /*
                res = this.getHadoopOutput();
                AlertDialog.Builder builder = new AlertDialog.Builder(HDFSManager.this);
                builder.setTitle("hadoop Output");
                builder.setMessage(res);
                builder.setPositiveButton("Ok", null);

                AlertDialog alertDialog = builder.create();

                alertDialog.show();*/
                this.disconnectToHadoopCluster();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}