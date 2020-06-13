package skku.edu.elephantory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String GOOGLE_ACCOUNT = "google_account";
    private GoogleSignInAccount mGoogleSignInAccount;
    private GoogleSignInClient mGoogleSignInClient;
    private Drawer drawer;

    private ArrayList<Dictionary> mArrayList;
    private CustomAdapter mAdapter;
    private int count = -1;

    /* View */
    private View contentView;
    private View loadingView;

    /* animation */
    private int animationDuration;

    /* from Analyze */
    EditText editText;
    static RequestQueue requestQueue;
    JobList jobList;
    RecyclerView recyclerView;
    JobAdapter adapter;
    DBHelper dbHelper;
    String dbName = "job_history";
    String tableName;
    SQLiteDatabase db;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentView = findViewById(R.id.pullToRefresh);
        loadingView = (ProgressBar) findViewById(R.id.loading_spinner); // Final so we can access it from the other thread
        loadingView.setVisibility(View.GONE);

        // Retrieve and cache the system's default "short" animation time.
        animationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInAccount = getIntent().getParcelableExtra(GOOGLE_ACCOUNT);

        // TCP Connection to Hadoop Cluster

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                       @Override
                                       public void onRefresh() {
//                refreshData();
            makeRequest();
            contentView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                           public void run() {
                try{
                    Thread.sleep(5000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        crossfade();
                    }
                });
            }

            }).start();

            pullToRefresh.setRefreshing(false);
        }
        });

        editText = findViewById(R.id.editTextURL);

        Button button_db= findViewById(R.id.buttonDB);
        button_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDB = new Intent(getApplicationContext(), Database.class);
                startActivity(intentDB);
            }
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        recyclerView = findViewById(R.id.recyclerview_main_list); // XML 레이아웃에 정의한 리싸이클러뷰 객체 참조
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);

        adapter = new JobAdapter();
        recyclerView.setAdapter(adapter); // 리싸이클러뷰에 어댑터 설정

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // 여기서 text file 보여주기
//                Dictionary dict = mArrayList.get(position);
//                Toast.makeText(getApplicationContext(), dict.getId()+' '+dict.getEnglish()+' '+dict.getKorean(), Toast.LENGTH_LONG).show();
                String job_id = jobList.jobResult.jobResultList.get(position).job_id;
                Log.d("onClick", "///// position: " + position + ", " + jobList.jobResult.jobResultList.get(position).job_id);
                Intent intent = new Intent(getBaseContext(), ShowTextFile.class);

                intent.putExtra("job_id", job_id);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        createDatabase(dbName);
        setDrawer();

        Button buttonCollectData = findViewById(R.id.buttonCollectData);
        buttonCollectData.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intentCollectData = new Intent(getApplicationContext(), CollectData.class);
                startActivity(intentCollectData);
            }
        });

        Button buttonAnalyzeData = findViewById(R.id.buttonMR);
        buttonAnalyzeData.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intentAnalyzeData = new Intent(getApplicationContext(), MapReduceRunner.class);
                startActivity(intentAnalyzeData);
            }
        });
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                        Log.d("MainActivity", "Child is selected ....");
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    private void setDrawer() {
        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem gotoHomeItem = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        PrimaryDrawerItem gotoReportItem = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_report);
        SecondaryDrawerItem signOutItem = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.drawer_item_sign_out);

/*
        // Create the AccountHeader
     /*   AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(mGoogleSignInAccount.getDisplayName())
                                .withEmail(mGoogleSignInAccount.getEmail())
                                .withIcon(mGoogleSignInAccount.getPhotoUrl())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //create the drawer and remember the `Drawer` result object
        drawer = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
//                .withToolbar(toolbar)
                .addDrawerItems(
                        gotoHomeItem,
                        gotoReportItem,
                        new DividerDrawerItem(),
                        signOutItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch ((int)drawerItem.getIdentifier()) {
                            case 2:
                                gotoReportActivity();
                                break;
                            case 3:
                                signOut();
                                break;
                        }

                        return true;
                    };
                })
                .build();
*/
    } /* set drawer */

    private void gotoReportActivity() {
        Intent intent=new Intent(MainActivity.this,ReportActivity.class);
        intent.putExtra(MainActivity.GOOGLE_ACCOUNT, mGoogleSignInAccount);
        startActivity(intent);
    }

    private void signOut() {

        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //On Succesfull signout we navigate the user back to LoginActivity
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    /* loading fade */

    private void crossfade() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        contentView.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        loadingView.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    /* from Analyze */
    public void makeRequest() {
        String url = editText.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        printDebug("응답 -> " + response);

                        processResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        printDebug("에러 -> " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
        printDebug("요청 보냄.");
    }

    public void processResponse(String response) {
        Gson gson = new Gson();
        jobList = gson.fromJson(response, JobList.class);

        printDebug("Total number of jobs : " + jobList.jobResult.jobResultList.size());

        for (int i = 0; i < jobList.jobResult.jobResultList.size(); i++) {
            Job job = jobList.jobResult.jobResultList.get(i);

            adapter.addItem(job);
        }

        adapter.notifyDataSetChanged();

        // After get data from web, then insert data to DB
        insertDB();
    }

    private void createDatabase(String name) {
        printDebug("///// createDatabase()");
        // DBHelper 객체 생성
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        printDebug("///// Database=" + name);
    }

    private void insertDB() {
        Job tmpJob;
        String sql;

        for(int i = 0; i < jobList.jobResult.jobResultList.size(); i++) {
            tmpJob = jobList.jobResult.jobResultList.get(i);
            sql = String.format("INSERT OR IGNORE INTO Job VALUES(NULL, '%s', '%s', '%s', '%s');",
                    tmpJob.job_id, tmpJob.name, tmpJob.user, tmpJob.elapsed_time);
            db.execSQL(sql);
        }
        Toast.makeText(MainActivity.this, "Insert to DB: Success", Toast.LENGTH_SHORT).show();
    }


    private AlertDialog makeRequestDialog(CharSequence title, CharSequence message,
                                          CharSequence titleButtonYes, CharSequence titleButtonNo) {
        AlertDialog.Builder requestDialog = new AlertDialog.Builder(this);
        requestDialog.setTitle(title);
        requestDialog.setMessage(message);
        requestDialog.setPositiveButton(titleButtonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "It will take 5 seconds.", Toast.LENGTH_SHORT).show();

                /*
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Analyze.this, "Request completed.", Toast.LENGTH_SHORT).show();
                    }
                }, 5000);
                 */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                makeRequest();
            }
        });



        requestDialog.setNegativeButton(titleButtonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        return requestDialog.create();
    }


    public void printDebug(String data) {
        Log.d("Analyze", data);
    }
}
