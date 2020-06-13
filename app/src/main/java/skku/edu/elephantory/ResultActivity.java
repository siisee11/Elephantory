package skku.edu.elephantory;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private View contentView;
    private View loadingView;
    private int animationDuration;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String id = "";
        String korean = "";
        String english = "";

        Bundle extras = getIntent().getExtras();

        id = extras.getString("id");
        korean = extras.getString("korean");
        english = extras.getString("english");


        TextView textView = (TextView) findViewById(R.id.textView_result);

        String str = id + '\n' + english + '\n' + korean;
        textView.setText(str);
    }

}

