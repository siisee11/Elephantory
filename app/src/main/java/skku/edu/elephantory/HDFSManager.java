package skku.edu.elephantory;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    public static final String tcpHost = "192.168.0.69";
    String TAG = "socketTest";
    public final String hadoopInputDataPath = "/Users/ash1209/hadoop_input/";
    public static Socket socket;
    public Button uploadButton;
    public Button lsButton;
    public Button rmButton;
    public ImageView elephant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdfsmanager);

        uploadButton = (Button)findViewById(R.id.buttonUpload);
        lsButton = (Button)findViewById(R.id.buttonls);
        rmButton = (Button)findViewById(R.id.buttonrm);
        elephant = (ImageView)findViewById(R.id.rotateImage);

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
                        String inputFile = " " + hadoopInputDataPath + input.getText().toString();
                       // hCmd[0] += (" " + hadoopInputDataPath + inputFile);
                       // Log.d(TAG, hCmd[0]);


                        HDFSClientThread hdfsClientThread = new HDFSClientThread(hCmd[0], inputFile);
                        hdfsClientThread.start();

                        /* image rotate */
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                        elephant.setAnimation(animation);
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
                final String[] output = {""};
                final String[] hCmd = {"ls.sh"};
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
                        HDFSClientThread hdfsClientThread = new HDFSClientThread(hCmd[0], inputFile);
                        hdfsClientThread.start();
                        try {
                            hdfsClientThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        output[0] += hdfsClientThread.lsOutput;
                        String res = output[0];
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(HDFSManager.this);
                        builder2.setTitle("Search Result");
                        builder2.setMessage(res);
                        builder2.setPositiveButton("Ok", null);

                        AlertDialog alertDialog2 = builder2.create();

                        alertDialog2.show();
                    }
                });

                builder.setNegativeButton("취소", null);

                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }

        });

       rmButton.setOnClickListener(new Button.OnClickListener() {
           @Override
           public void onClick(View v) {
               final String[] output = {""};
               final String[] hCmd = {"rm.sh"};
               LinearLayout dialog_group = new LinearLayout((HDFSManager.this));
               dialog_group.setOrientation(LinearLayout.VERTICAL);
               AlertDialog.Builder builder = new AlertDialog.Builder(HDFSManager.this);
               final EditText input = new EditText(getApplicationContext());
               input.setHint("File Name to Delete");

               dialog_group.addView(input);
               builder.setView(dialog_group);
               //builder.setTitle("Put InputFile");
               builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       String inputFile = input.getText().toString();
                       HDFSClientThread hdfsClientThread = new HDFSClientThread(hCmd[0], inputFile);
                       hdfsClientThread.start();
                       try {
                           hdfsClientThread.join();
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }

                       output[0] += hdfsClientThread.lsOutput;
                       String res = output[0];
                       AlertDialog.Builder builder2 = new AlertDialog.Builder(HDFSManager.this);
                       builder2.setTitle("Search Result");
                       builder2.setMessage(res);
                       builder2.setPositiveButton("Ok", null);

                       AlertDialog alertDialog2 = builder2.create();

                       alertDialog2.show();
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
        public String hadoopCmdParam;
        public String runCmd;
        public String lsOutput;
        public HDFSClientThread(String _hadoopCmd, String _hadoopCmdParam){

            hadoopCmd = _hadoopCmd;
            hadoopCmdParam = _hadoopCmdParam;
        }

        public void connectToHadoopCluster(String hostName, int port) throws IOException {
            socket = new Socket(hostName, port);
        }

        public void disconnectToHadoopCluster() throws IOException {
            socket.close();
        }

        public void setHadoopCommand(){
            runCmd = hadoopCmd + " " + hadoopCmdParam;
        }

        public void sendHadoopCommand() throws IOException {
            BufferedWriter writeBuf = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));

            writeBuf.write(runCmd);
            writeBuf.newLine();
            writeBuf.flush();
        }

        public void getHadoopOutput() throws IOException {

            if(hadoopCmd.equals("ls.sh") || hadoopCmd.equals("rm.sh")) {
                BufferedReader readBuf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                lsOutput = readBuf.readLine();

            }
        }

        public void run(){
            try{
                this.connectToHadoopCluster(tcpHost, port);

                this.setHadoopCommand();
                this.sendHadoopCommand();

                this.getHadoopOutput();

                this.disconnectToHadoopCluster();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}