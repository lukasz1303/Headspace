package com.example.headspace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MeditationAdapter extends RecyclerView.Adapter<MeditationAdapter.MeditationViewHolder> {

    private Context mContext;
    private Activity mActivity;
    private List<Meditation> mMeditations;
    private static final int REQUEST_CODE = 100;


    MeditationAdapter(Context context, List<Meditation> meditations, Activity activity){
        mContext = context;
        mMeditations = meditations;
        mActivity = activity;
    }


    void updateMeditationCompleted(int position){
        mMeditations.get(position).setCompleted();
        notifyItemChanged(position);
        notifyItemChanged(position+1);
    }


    @NonNull
    @Override
    public MeditationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meditation_item,parent,false);

        return new MeditationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MeditationViewHolder holder, final int position) {
        String day = mMeditations.get(position).getDay().split(" ")[1];
        holder.mTextViewNumberOfSession.setText(day);

        if(position == 0 && !mMeditations.get(position).isCompleted()){
                holder.mImageView.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                final int paddingBottom =  holder.mImageView.getPaddingBottom(), paddingLeft =  holder.mImageView.getPaddingLeft();
                final int paddingRight =  holder.mImageView.getPaddingRight(), paddingTop =  holder.mImageView.getPaddingTop();

                holder.mImageView.setBackgroundResource(R.drawable.background_circle_dark_grey);
                holder.mImageView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                setHolderOnClickListener(holder,position);
                setTextViewNextSessionOnClickListener(position);
        }

        else if (mMeditations.get(position).isCompleted()){
            holder.mImageView.setImageResource(R.drawable.ic_check_white_24dp);

            final int paddingBottom =  holder.mImageView.getPaddingBottom(), paddingLeft =  holder.mImageView.getPaddingLeft();
            final int paddingRight =  holder.mImageView.getPaddingRight(), paddingTop =  holder.mImageView.getPaddingTop();

            holder.mImageView.setBackgroundResource(R.drawable.background_circle_green);
            holder.mTextViewNumberOfSession.setTextColor(mContext.getResources().getColor(R.color.color_green_meditation_completed));
            holder.mImageView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            setHolderOnClickListener(holder,position);

        } else if (position>0 && !mMeditations.get(position).isCompleted() && mMeditations.get(position-1).isCompleted()){
            holder.mImageView.setImageResource(R.drawable.ic_play_arrow_white_24dp);

            final int paddingBottom =  holder.mImageView.getPaddingBottom(), paddingLeft =  holder.mImageView.getPaddingLeft();
            final int paddingRight =  holder.mImageView.getPaddingRight(), paddingTop =  holder.mImageView.getPaddingTop();

            holder.mImageView.setBackgroundResource(R.drawable.background_circle_dark_grey);
            holder.mImageView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            setHolderOnClickListener(holder,position);
            setTextViewNextSessionOnClickListener(position);
        }

    }

    private void setTextViewNextSessionOnClickListener(final int position) {
        LinearLayout textViewNextSession = mActivity.findViewById(R.id.button_next_session);
        textViewNextSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(),PlayerActivity.class);
                i.setAction(Intent.ACTION_SEND);
                i.putExtra("RAW_ID",mMeditations.get(position).getResourceId());
                i.putExtra("Category",mMeditations.get(position).getCategory());
                i.putExtra("Day",mMeditations.get(position).getDay());
                if (mMeditations.get(position).hasVideo()){
                    i.putExtra("Video_Id",mMeditations.get(position).getVideoId());
                }
                if (i.resolveActivity(mContext.getPackageManager()) != null) {
                    mActivity.startActivityForResult(i,REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mMeditations!= null){
            return mMeditations.size();
        }
        return 0;
    }

    static class MeditationViewHolder extends RecyclerView.ViewHolder{
        TextView mTextViewNumberOfSession;
        TextView mTextViewTime;
        ImageView mImageView;
        LinearLayout mLinearLayout;
        MeditationViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewNumberOfSession = itemView.findViewById(R.id.tv_number_of_session);
            mTextViewTime = itemView.findViewById(R.id.tv_time);
            mImageView = itemView.findViewById(R.id.image_view_meditation_play);
            mLinearLayout = itemView.findViewById(R.id.relative_layout_meditation);
        }
    }
    private void setHolderOnClickListener(MeditationViewHolder holder, final int position){
        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(),PlayerActivity.class);
                i.setAction(Intent.ACTION_SEND);
                i.putExtra("RAW_ID",mMeditations.get(position).getResourceId());
                i.putExtra("Category",mMeditations.get(position).getCategory());
                i.putExtra("Day",mMeditations.get(position).getDay());
                if (mMeditations.get(position).hasVideo()){
                    i.putExtra("Video_Id",mMeditations.get(position).getVideoId());
                }
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (i.resolveActivity(mContext.getPackageManager()) != null) {
                    mActivity.startActivityForResult(i,REQUEST_CODE);
                }
            }
        });
    }
}
