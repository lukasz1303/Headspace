package com.example.headspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.github.stefanodp91.android.circularseekbar.CircularSeekBar;
import com.github.stefanodp91.android.circularseekbar.OnCircularSeekBarChangeListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class PlayerActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private TextView mTimeTextView;
    private TextView mMinuteMeditationTextView;
    private TextView mRunStreakTextView;
    private TextView mQuoteTextView;
    private ImageView mImageViewClose;
    private String mMediaDuration;
    private ImageView mImageViewPlay;
    private Runnable mRunnable;
    private Handler mHandler;
    private CircularSeekBar mSeekBar;
    private Notification mNotification;
    private ViewFlipper mViewFlipper;
    private int mMediaPlayerDuration;
    private boolean mMediaPlayerReady = false;
    private boolean mVideoViewFullscreen = false;
    private boolean meditationCompleted = false;
    private static final String NOTIFICATION_CHANNEL_ID = "channel";
    private final int NOTIFICATION_ID = 1;
    private ServiceConnection mConnection;
    private VideoView mVideoView;
    private MediaController mController;


    private MediaPlayer.OnCompletionListener mOnCompletionListener = (new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            saveCompleteMeditation();
            releaseMediaPlayer();
            deleteNotification();
            mViewFlipper.showNext();
            meditationCompleted = true;
            mImageViewClose = findViewById(R.id.image_view_close_3);
            mImageViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    });

    private MediaPlayer.OnCompletionListener mOnCompletionListenerVideoView = (new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mViewFlipper.showNext();
            mController = null;
            mVideoViewFullscreen = false;
        }
    });

    private MediaPlayer.OnPreparedListener mOnPreparedListener = (new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayerDuration = mMediaPlayer.getDuration();
            mMediaPlayerReady = true;
            initSeekBar();
        }
    });

    private MediaPlayer.OnErrorListener mOnErrorListener = (new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mMediaPlayer = null;
            mImageViewPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            return true;
        }
    });

    public AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = (new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mMediaPlayerReady) {
                        mMediaPlayer.start();
                        mImageViewPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseMediaPlayer();
                    deleteNotification();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mMediaPlayer.pause();
                    mImageViewPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    break;
            }
        }
    });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Objects.requireNonNull(getSupportActionBar()).hide();

        super.onCreate(savedInstanceState);

        createNotificationChannel();

        setContentView(R.layout.activity_player);
        mImageViewPlay = findViewById(R.id.image_play_button);
        mMinuteMeditationTextView = findViewById(R.id.tv_minutes_meditated);
        mRunStreakTextView = findViewById(R.id.tv_run_streak);
        mQuoteTextView = findViewById(R.id.tv_quote);

        TextView mTitleTextView = findViewById(R.id.tv_title);
        final TextView mSessionTextView = findViewById(R.id.tv_session);
        mTimeTextView = findViewById(R.id.tv_time);
        mViewFlipper = findViewById(R.id.view_flipper);
        mImageViewClose = findViewById(R.id.image_view_close);
        mImageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
                mVideoViewFullscreen = false;
                mViewFlipper.showNext();
                mImageViewClose = findViewById(R.id.image_view_close_2);
                mImageViewClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        });
        mHandler = new Handler();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int rawId = getIntent().getIntExtra("RAW_ID", 0);
        if (getIntent().hasExtra("Category")) {
            String category = getIntent().getStringExtra("Category");
            mTitleTextView.setText(category);
            String session = "Session " + getIntent().getStringExtra("Day").split(" ")[1];
            mSessionTextView.setText(session);
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(this, rawId);
                mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
                mMediaPlayer.setOnErrorListener(mOnErrorListener);
                mMediaDuration = millisecondsToTime(rawId);
                String initTime = "0:00 / " + mMediaDuration;
                mTimeTextView.setText(initTime);
            }
        }

        RelativeLayout mRelativeLayoutStart = findViewById(R.id.relative_layout_start);
        mRelativeLayoutStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
                mVideoViewFullscreen = false;
                mController = null;
                mViewFlipper.showNext();
                mImageViewClose = findViewById(R.id.image_view_close_2);
                mImageViewClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        });

        if (getIntent().hasExtra("Video_Id")) {
            mVideoView = findViewById(R.id.video_view);
            TextView textViewTitleStart = findViewById(R.id.tv_title_start);
            textViewTitleStart.setText(getIntent().getStringExtra("Category"));

            String uriPath = "android.resource://" + getPackageName() + "/" + getIntent().getIntExtra("Video_Id", 0);
            Uri uri = Uri.parse(uriPath);
            mVideoView.setVideoURI(uri);

            mController = new MediaController(this);
            mController.setAnchorView(mVideoView);
            mController.hide();
            mController.setVisibility(View.GONE);

            mVideoView.setMediaController(mController);
            mVideoView.setOnCompletionListener(mOnCompletionListenerVideoView);

            mImageViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            mVideoView.seekTo(10);
            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mController.setVisibility(View.VISIBLE);
                    TextView textViewTapAnywhere = findViewById(R.id.tv_tap_anywhere);
                    textViewTapAnywhere.setVisibility(View.GONE);
                    TextView textViewWatchVideo = findViewById(R.id.tv_watch_video);
                    textViewWatchVideo.setVisibility(View.GONE);
                    ImageView imageViewPlayVideo = findViewById(R.id.image_view_play_video);
                    imageViewPlayVideo.setVisibility(View.GONE);

                    mImageViewClose.setBackgroundResource(R.drawable.background_rectangle_black_transparent);

                    mImageViewClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mVideoView.stopPlayback();
                            mController = null;
                            mViewFlipper.showNext();
                            mImageViewClose = findViewById(R.id.image_view_close_2);
                            mVideoViewFullscreen=false;
                            mImageViewClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            });
                        }
                    });
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
                    params.width = metrics.widthPixels;
                    params.height = metrics.heightPixels;
                    mVideoViewFullscreen = true;
                    mVideoView.setLayoutParams(params);
                    mVideoView.start();
                    mController.show();
                    return true;
                }
            });
        } else{
            mViewFlipper.showNext();
            mImageViewClose = findViewById(R.id.image_view_close_2);
            mImageViewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        mImageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediaPlayer == null) {
                    int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mMediaPlayer = MediaPlayer.create(PlayerActivity.this, rawId);
                        mMediaPlayer.setOnErrorListener(mOnErrorListener);
                        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
                        createNotification();
                        if (mMediaPlayerReady) {
                            mMediaPlayer.start();
                        }
                        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                        mImageViewPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    }
                } else {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mImageViewPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    } else {
                        createNotification();
                        if (mMediaPlayerReady) {
                            mMediaPlayer.start();
                        }
                        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                        mImageViewPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    }
                }
            }


        });

        mSeekBar = findViewById(R.id.seek);
        mSeekBar.setIndicatorEnabled(false);
        mSeekBar.setEnabled(false);
        mSeekBar.setOnRoundedSeekChangeListener(new OnCircularSeekBarChangeListener() {
            /**
             * Progress change
             * @param CircularSeekBar seekBar
             * @param progress the progress
             */
            @Override
            public void onProgressChange(CircularSeekBar CircularSeekBar, float progress) {
                if (progress > 99) {
                    progress = 99;
                }
                if (progress < 0) {
                    progress = 0;
                }
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        if (mMediaPlayerReady) {
                            mMediaPlayer.seekTo((int) progress * mMediaPlayerDuration / 100);
                        }
                        if (mMediaPlayerReady) {
                            mMediaPlayer.start();
                        }
                    } else {
                        if (mMediaPlayerReady) {
                            mMediaPlayer.seekTo((int) progress * mMediaPlayerDuration / 100);
                        }
                    }
                }
            }

            /**
             * Indicator touched
             * @param CircularSeekBar seekBar
             */
            @Override
            public void onStartTrackingTouch(CircularSeekBar CircularSeekBar) {

            }

            /**
             * Indicator released
             * @param CircularSeekBar seekBar
             */
            @Override
            public void onStopTrackingTouch(CircularSeekBar CircularSeekBar) {

            }
        });
    }

    private void initSeekBar() {

        final float mediaPlayerDuration = (float) mMediaPlayerDuration;
        mSeekBar.setEnabled(true);
        mSeekBar.setProgress(0);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {

                    int mCurrentPosition = (int) ((float) mMediaPlayer.getCurrentPosition() / mediaPlayerDuration * 100.0f);// In milliseconds

                    String time = calculateTime(mMediaPlayer) + " / " + mMediaDuration;
                    mTimeTextView.setText(time);
                    if (mMediaPlayer.isPlaying()) {
                        mSeekBar.setProgress(mCurrentPosition);
                    }
                }
                mHandler.postDelayed(mRunnable, 250);
            }
        };
        mHandler.postDelayed(mRunnable, 250);
    }

    private String calculateTime(MediaPlayer player) {
        int time_sec = player.getCurrentPosition() / 1000;
        String minute = String.valueOf(time_sec / 60);
        int sec = time_sec % 60;
        String seconds;
        if (sec < 10) {
            seconds = "0" + sec;
        } else {
            seconds = String.valueOf(sec);
        }
        return minute + ":" + seconds;
    }

    private String millisecondsToTime(int id) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        final AssetFileDescriptor afd = this.getResources().openRawResourceFd(id);
        mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);

        int time_sec = millSecond / 1000;
        String minute = String.valueOf(time_sec / 60);
        int sec = time_sec % 60;
        String seconds;
        if (sec < 10) {
            seconds = "0" + sec;
        } else {
            seconds = String.valueOf(sec);
        }
        return minute + ":" + seconds;
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mSeekBar.resetPivot();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();

            mMediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            mImageViewPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            mHandler.removeCallbacks(mRunnable);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        if (mConnection != null) {
            unbindService(mConnection);
        }

        deleteNotification();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            String description = "channel for streaming meditation";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        if (mConnection == null) {
            mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder binder) {
                    ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(
                            PlayerActivity.this, KillNotificationsService.class));
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(PlayerActivity.this, NOTIFICATION_CHANNEL_ID);
                    builder.setContentText("Your meditation is streaming");
                    builder.setSmallIcon(R.drawable.launcher_circle);
                    builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                    builder.setOngoing(true);
                    Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                    intent.setAction(Intent.ACTION_MAIN);

                    PendingIntent pendingIntent = PendingIntent.getActivity(PlayerActivity.this, 0,
                            intent, 0);
                    builder.setContentIntent(pendingIntent);

                    mNotification = builder.build();

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(PlayerActivity.this);
                    notificationManagerCompat.notify(NOTIFICATION_ID, mNotification);
                }


                public void onServiceDisconnected(ComponentName className) {
                }

            };
            bindService(new Intent(PlayerActivity.this,
                            KillNotificationsService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void deleteNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private void saveCompleteMeditation() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        Set<String> set = new HashSet<>(pref.getStringSet(getIntent().getStringExtra("Category"), new HashSet<String>()));
        set.add(getIntent().getStringExtra("Day"));

        int minutesMeditated = pref.getInt("Minutes Meditated", 0);
        String runStreakDate = pref.getString("Run Streak Date", String.valueOf(Calendar.getInstance().getTime()));
        int numberRunStreak = pref.getInt("Number Run Streak", 0);
        int minutes = mMediaPlayerDuration / 60000;
        int rounded = 5 * Math.round(minutes / 5.0f);
        minutesMeditated += rounded;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.setTime(Calendar.getInstance().getTime());
        cal.add(Calendar.DATE, -1); //minus number would decrement the days
        Date yesterday = cal.getTime();
        String yesterdayString = dateFormat.format(yesterday);

        if (yesterdayString.equals(runStreakDate) || numberRunStreak == 0) {
            numberRunStreak += 1;
        } else if (!String.valueOf(Calendar.getInstance().getTime()).equals(runStreakDate)) {
            numberRunStreak = 1;
        }

        mMinuteMeditationTextView.setText(String.valueOf(minutesMeditated));
        mRunStreakTextView.setText(String.valueOf(numberRunStreak));
        mQuoteTextView.setText(getResources().getStringArray(R.array.quotes_array)[new Random().nextInt(33)]);

        editor.putStringSet(getIntent().getStringExtra("Category"), set);
        editor.putInt("Minutes Meditated", minutesMeditated);
        editor.putInt("Number Run Streak", numberRunStreak);
        editor.putString("Run Streak Date", String.valueOf(dateFormat.format(Calendar.getInstance().getTime())));
        editor.apply();
    }

    @Override
    public void finish() {
        if (meditationCompleted) {
            Intent returnIntent = new Intent();
            int position = Integer.parseInt(getIntent().getStringExtra("Day").split(" ")[1]);
            returnIntent.putExtra("position", position);

            setResult(RESULT_OK, returnIntent); //By not passing the intent in the result, the calling activity will get null data.
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (mVideoViewFullscreen) {
            mVideoViewFullscreen = false;
            mController = null;
            mVideoView.stopPlayback();
            mViewFlipper.showNext();
        } else {
            if(!meditationCompleted){
                finish();
            }
            super.onBackPressed();
        }
    }
}
