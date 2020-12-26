package com.example.headspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MeditationAdapter mAdapter;
    private LinearLayout mLinearLayoutNextSession;
    private List<Meditation> mMeditations;
    private Activity mActivity;
    private Context mContext;
    private static final int REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mRecyclerView = findViewById(R.id.my_recycler_view_meditation);
        TextView mTextViewCategoryName = findViewById(R.id.tv_category_name);
        mLinearLayoutNextSession = findViewById(R.id.button_next_session);

        String categoryName = getIntent().getStringExtra("categoryName");
        mTextViewCategoryName.setText(categoryName);

        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRecyclerView.setLayoutManager(layoutManager);


        mMeditations = new ArrayList<>(initListMeditations());

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        HashSet mSet = (HashSet) pref.getStringSet(mMeditations.get(0).getCategory(), new HashSet<String>());
        for (Meditation meditation:mMeditations){
            assert mSet != null;
            if(mSet.contains(meditation.getDay())){
                meditation.setCompleted();
            }
        }
        mContext = this;
        mActivity = CategoryActivity.this;
        mAdapter = new MeditationAdapter(mContext,mMeditations,mActivity);
        mRecyclerView.setAdapter(mAdapter);


        assert mSet != null;
        if (mSet.size() == mMeditations.size()){
            GradientDrawable bgShape = (GradientDrawable)mLinearLayoutNextSession.getBackground();
            bgShape.setColor(getResources().getColor(R.color.white));
            TextView textView = findViewById(R.id.tv_next_session);
            textView.setText(R.string.repeat_course);
            textView.setTextColor(getResources().getColor(R.color.color_text_quotes));
            ImageView imageView = findViewById(R.id.image_view_next_session);
            imageView.setImageResource(R.drawable.ic_refresh_black_24dp);
            configureNextSessionButton();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            final int position = data.getIntExtra("position",0);
            mAdapter.updateMeditationCompleted(position-1);
            if (position<mMeditations.size()){
                mLinearLayoutNextSession.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(),PlayerActivity.class);
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra("RAW_ID",mMeditations.get(position).getResourceId());
                        i.putExtra("Category",mMeditations.get(position).getCategory());
                        i.putExtra("Day",mMeditations.get(position).getDay());
                        if (mMeditations.get(position).hasVideo()){
                            i.putExtra("Video_Id",mMeditations.get(position).getVideoId());
                        }
                        if (i.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(i,REQUEST_CODE);
                        }
                    }
                });
            } else {
                GradientDrawable bgShape = (GradientDrawable)mLinearLayoutNextSession.getBackground();
                bgShape.setColor(getResources().getColor(R.color.white));
                TextView textView = findViewById(R.id.tv_next_session);
                textView.setText(R.string.repeat_course);
                textView.setTextColor(getResources().getColor(R.color.color_text_quotes));
                ImageView imageView = findViewById(R.id.image_view_next_session);
                imageView.setImageResource(R.drawable.ic_refresh_black_24dp);
               configureNextSessionButton();

            }
        }
    }

    private void configureNextSessionButton(){
        final Activity activity1 = this;
        mLinearLayoutNextSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity1);

                builder.setMessage("You'll have to redo each session to unlock them again.")
                        .setTitle("Are you sure you want to reset this pack?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                        pref.edit().remove(mMeditations.get(0).getCategory()).apply();

                        mMeditations = new ArrayList<>(initListMeditations());
                        mAdapter = new MeditationAdapter(mContext,mMeditations,mActivity);
                        mRecyclerView.setAdapter(mAdapter);

                        GradientDrawable bgShape = (GradientDrawable)mLinearLayoutNextSession.getBackground();
                        bgShape.setColor(getResources().getColor(R.color.color_dark_grey));
                        TextView textView = findViewById(R.id.tv_next_session);
                        textView.setText(R.string.next_session);
                        textView.setTextColor(getResources().getColor(R.color.white));
                        ImageView imageView = findViewById(R.id.image_view_next_session);
                        imageView.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    }
                });
                builder.setNeutralButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    private List<Meditation> initListMeditations(){
        List<Meditation> meditations = new ArrayList<>();
        String categoryName = getIntent().getStringExtra("categoryName");
        assert categoryName != null;
        switch (categoryName){
            case "Basics":
                meditations.add(new Meditation("Basics", "Day 1", R.raw.basics_day_1, R.raw.basics_intro));
                meditations.add(new Meditation("Basics", "Day 2", R.raw.basics_day_2));
                meditations.add(new Meditation("Basics", "Day 3", R.raw.basics_day_3, R.raw.basics_intro_day_3));
                meditations.add(new Meditation("Basics", "Day 4", R.raw.basics_day_4));
                meditations.add(new Meditation("Basics", "Day 5", R.raw.basics_day_5));
                meditations.add(new Meditation("Basics", "Day 6", R.raw.basics_day_6));
                meditations.add(new Meditation("Basics", "Day 7", R.raw.basics_day_7));
                meditations.add(new Meditation("Basics", "Day 8", R.raw.basics_day_8));
                meditations.add(new Meditation("Basics", "Day 9", R.raw.basics_day_9));
                meditations.add(new Meditation("Basics", "Day 10", R.raw.basics_day_10));
                break;
            case  "Basics 2":
                meditations.add(new Meditation("Basics 2", "Day 1", R.raw.basics_2_day_1));
                meditations.add(new Meditation("Basics 2", "Day 2", R.raw.basics_2_day_2));
                meditations.add(new Meditation("Basics 2", "Day 3", R.raw.basics_2_day_3));
                meditations.add(new Meditation("Basics 2", "Day 4", R.raw.basics_2_day_4));
                meditations.add(new Meditation("Basics 2", "Day 5", R.raw.basics_2_day_5));
                meditations.add(new Meditation("Basics 2", "Day 6", R.raw.basics_2_day_6));
                meditations.add(new Meditation("Basics 2", "Day 7", R.raw.basics_2_day_7));
                meditations.add(new Meditation("Basics 2", "Day 8", R.raw.basics_2_day_8));
                meditations.add(new Meditation("Basics 2", "Day 9", R.raw.basics_2_day_9));
                meditations.add(new Meditation("Basics 2", "Day 10", R.raw.basics_2_day_10));
                break;

            case  "Basics 3":
                meditations.add(new Meditation("Basics 3", "Day 1", R.raw.basics_3_day_1, R.raw.basics_3_intro));
                meditations.add(new Meditation("Basics 3", "Day 2", R.raw.basics_3_day_2));
                meditations.add(new Meditation("Basics 3", "Day 3", R.raw.basics_3_day_3));
                meditations.add(new Meditation("Basics 3", "Day 4", R.raw.basics_3_day_4));
                meditations.add(new Meditation("Basics 3", "Day 5", R.raw.basics_3_day_5));
                meditations.add(new Meditation("Basics 3", "Day 6", R.raw.basics_3_day_6));
                meditations.add(new Meditation("Basics 3", "Day 7", R.raw.basics_3_day_7));
                meditations.add(new Meditation("Basics 3", "Day 8", R.raw.basics_3_day_8));
                meditations.add(new Meditation("Basics 3", "Day 9", R.raw.basics_3_day_9));
                meditations.add(new Meditation("Basics 3", "Day 10", R.raw.basics_3_day_10));
                break;
            case  "Productivity":
                meditations.add(new Meditation("Productivity", "Day 1", R.raw.productivity_day_1));
                meditations.add(new Meditation("Productivity", "Day 2", R.raw.productivity_day_2));
                meditations.add(new Meditation("Productivity", "Day 3", R.raw.productivity_day_3));
                meditations.add(new Meditation("Productivity", "Day 4", R.raw.productivity_day_4));
                meditations.add(new Meditation("Productivity", "Day 5", R.raw.productivity_day_5));
                meditations.add(new Meditation("Productivity", "Day 6", R.raw.productivity_day_6));
                meditations.add(new Meditation("Productivity", "Day 7", R.raw.productivity_day_7));
                meditations.add(new Meditation("Productivity", "Day 8", R.raw.productivity_day_8));
                meditations.add(new Meditation("Productivity", "Day 9", R.raw.productivity_day_9));
                meditations.add(new Meditation("Productivity", "Day 10", R.raw.productivity_day_10));
                break;
        }
        return meditations;
    }

}
