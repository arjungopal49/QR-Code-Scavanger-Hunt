import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ConstraintLayout mainScreen, introScreen;
    ProgressBar progressBar;

    PendingIntent myPendingIntent;
    AlarmManager alarmManager;
    BroadcastReceiver myBroadcastReceiver;

    private Timer mainTimer;
    private int seconds;
    private boolean inGame = false;
    private boolean scanning = false;
    private Button scanButton, backButton, playAgainButton, currentHintsButton, newHintsButton, playNowButton;
    private String qrCode;
    private int level = 0;
    private String[] qrLinks = {"https://www.youtube.com/watch?v=dQw4w9WgXcQ", "https://www.youtube.com/watch?v=6_b7RDuLwcI", "https://www.youtube.com/watch?v=34Ig3X59_qA", "https://www.youtube.com/watch?v=o6piTG5EdhQ", "https://www.youtube.com/watch?v=SbYXkOAoZpI"};
    private String[] puzzle = {"","","","",""};
    private String[] hint = {"Hint A","Hint B","Hint C","Hint D","Hint E"};
    private String[] puzzleHint = {"","","","",""};
    TextView txtname, progressTextView, hintTextView, timerDisplay;
    EditText textInput1, textInput2,textInput3,textInput4,textInput5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.activity_main_previewView);
        scanButton = findViewById(R.id.scanButton);
        backButton = findViewById(R.id.backButton);
        playAgainButton = findViewById(R.id.playAgainButton);
        playNowButton = findViewById(R.id.playNowButton);
        currentHintsButton = findViewById(R.id.currentHintsButton);
        newHintsButton = findViewById(R.id.newHintsButton);
        mainScreen = findViewById(R.id.mainScreen);
        introScreen = findViewById(R.id.introScreen);
        progressBar = findViewById(R.id.progressBar);
        progressTextView = findViewById(R.id.progressTextView);
        hintTextView = findViewById(R.id.hintTextView);
        timerDisplay = findViewById(R.id.timerDisplay);
        textInput1 = findViewById(R.id.textInput1);
        textInput2 = findViewById(R.id.textInput2);
        textInput3 = findViewById(R.id.textInput3);
        textInput4 = findViewById(R.id.textInput4);
        textInput5 = findViewById(R.id.textInput5);



        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();

        loadHints();
        System.out.println(hint[0] + hint[1] + hint[2] + hint[3] + hint[4]);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainScreen.setVisibility(View.INVISIBLE);
                backButton.setVisibility(View.VISIBLE);
                scanning = true;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainScreen.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.INVISIBLE);
                scanning = false;
            }
        });

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAgainButton.setVisibility(View.INVISIBLE);
                introScreen.setVisibility(View.VISIBLE);
                mainScreen.setVisibility(View.INVISIBLE);
            }
        });

        currentHintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introScreen.setVisibility(View.INVISIBLE);
                mainScreen.setVisibility(View.VISIBLE);
                playGame();
            }
        });

        newHintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textInput1.setVisibility(View.VISIBLE);
                textInput2.setVisibility(View.VISIBLE);
                textInput3.setVisibility(View.VISIBLE);
                textInput4.setVisibility(View.VISIBLE);
                textInput5.setVisibility(View.VISIBLE);
                playNowButton.setVisibility(View.VISIBLE);

            }
        });

        playNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hint[0] = textInput1.getText().toString();
                hint[1] = textInput2.getText().toString();
                hint[2] = textInput3.getText().toString();
                hint[3] = textInput4.getText().toString();
                hint[4] = textInput5.getText().toString();
                textInput1.setVisibility(View.INVISIBLE);
                textInput2.setVisibility(View.INVISIBLE);
                textInput3.setVisibility(View.INVISIBLE);
                textInput4.setVisibility(View.INVISIBLE);
                textInput5.setVisibility(View.INVISIBLE);
                playNowButton.setVisibility(View.INVISIBLE);
                introScreen.setVisibility(View.INVISIBLE);
                mainScreen.setVisibility(View.VISIBLE);
                playGame();
            }
        });

        createNotification();
    }


    public void shufflePuzzle() {
        System.arraycopy(qrLinks, 0, puzzle, 0, qrLinks.length);
        System.arraycopy(hint, 0, puzzleHint, 0, hint.length);

        Random rnd = ThreadLocalRandom.current();
        for (int i = puzzleHint.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = puzzleHint[index];
            puzzleHint[index] = puzzleHint[i];
            puzzleHint[i] = a;
            String b = puzzle[index];
            puzzle[index] = puzzle[i];
            puzzle[i] = b;
        }
    }


    public void playGame(){
        saveHints();
        shufflePuzzle();
        inGame = true;
        level = 0;
        hintTextView.setText(puzzleHint[level]);
        progressTextView.setText(level + "/5");
        progressBar.setProgress((level*100)/5);
        hintTextView.setTextColor(Color.rgb(0, 255, 0));
        startStopWatch();
    }


    public void saveHighScore(int score)
    {
        File file= null;
        String highScore = String.valueOf(score);

        FileOutputStream fileOutputStream = null;
        try {
            highScore = highScore + " ";
            file = getFilesDir();
            fileOutputStream = openFileOutput("Score.txt", Context.MODE_PRIVATE); //MODE PRIVATE
            fileOutputStream.write(highScore.getBytes());
//            Toast.makeText(this, "Successfully Saved \n" + "Path --" + file + "\tScore.txt", Toast.LENGTH_SHORT).show();

            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int loadHighScore()
    {
        try {
            FileInputStream fileInputStream =  openFileInput("Score.txt");
            int read = -1;
            StringBuffer buffer = new StringBuffer();
            while((read =fileInputStream.read())!= -1){
                buffer.append((char)read);
            }
            Log.d("Score", buffer.toString());

            return Integer.parseInt(buffer.substring(0,buffer.indexOf(" ")));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
//        Toast.makeText(this,"Loaded", Toast.LENGTH_SHORT).show();
    }

    public void startStopWatch(){
        mainTimer = new Timer();
        seconds = 0;
        mainTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seconds++;
                        timerDisplay.setText(seconds+"s");
                    }
                });

            }
        },0,1000);
    }

    public void checkCode(){
        if (inGame && scanning && qrCode.equals(puzzle[level])){
            mainScreen.setVisibility(View.VISIBLE);
            nextLevel();
        }
    }

    public void nextLevel(){
        backButton.setVisibility(View.INVISIBLE);
        scanning = false;
        level++;
        progressTextView.setText(level + "/5");
        progressBar.setProgress((level*100)/5);
        if (level == 5){
            endGame();
        } else {
            hintTextView.setText(puzzleHint[level]);
        }
    }

    public void endGame(){
        mainTimer.cancel();
        if (loadHighScore() == -1 || seconds < loadHighScore()) {
            saveHighScore(seconds);
        }
        hintTextView.setText("Game Over in " + seconds+"s \n \nBest Score: " + loadHighScore() + "s");
        hintTextView.setTextColor(Color.rgb(255, 0, 0));
        playAgainButton.setVisibility(View.VISIBLE);
        inGame = false;
    }


    public void createNotification(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 49);
        c.set(Calendar.SECOND, 10);
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        myBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                sendNotification("QR Scavenger Hunt", "Try beating your time of " + loadHighScore() + "s!");
            }
        };

        registerReceiver(myBroadcastReceiver, new IntentFilter("com.learntodroid.androidqrcodescanner") );

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        myPendingIntent = PendingIntent.getBroadcast( this, 0, new Intent("com.learntodroid.androidqrcodescanner"),0 );


        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), myPendingIntent);


    }

    private void UnregisterAlarmBroadcast()
    {
        alarmManager.cancel(myPendingIntent);
        getBaseContext().unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    private void sendNotification(String title, String content) {
        String NOTIFICATION_CHANNEL_ID = "edmt_multiple_location";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[] {0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }





    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                qrCode = _qrCode;
                checkCode();
            }

            @Override
            public void qrCodeNotFound() {
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }



    public void saveHints()
    {
        File file= null;
        String savedHint = hint[0] + "*" + hint[1] + "**" + hint[2] + "***"  + hint[3] + "****" + hint[4];
        System.out.println(savedHint);

        FileOutputStream fileOutputStream = null;
        try {

            file = getFilesDir();
            fileOutputStream = openFileOutput("Hints.txt", Context.MODE_PRIVATE); //MODE PRIVATE
            fileOutputStream.write(savedHint.getBytes());
//            Toast.makeText(this, "Successfully Saved \n" + "Path --" + file + "\tScore.txt", Toast.LENGTH_SHORT).show();

            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadHints()
    {
        try {
            FileInputStream fileInputStream =  openFileInput("Hints.txt");
            int read = -1;
            StringBuffer buffer = new StringBuffer();
            while((read =fileInputStream.read())!= -1){
                buffer.append((char)read);
            }
            Log.d("Hints", buffer.toString());

            hint[0] = buffer.substring(0,buffer.indexOf("*"));
            hint[1] = buffer.substring(buffer.indexOf("*") + 1,buffer.indexOf("**"));
            hint[2] = buffer.substring(buffer.indexOf("**") + 2,buffer.indexOf("***"));
            hint[3] = buffer.substring(buffer.indexOf("***") + 3,buffer.indexOf("****"));
            hint[4] = buffer.substring(buffer.indexOf("****") + 4,buffer.length());


        } catch (Exception e) {
            e.printStackTrace();
        }

//        Toast.makeText(this,"Loaded", Toast.LENGTH_SHORT).show();
    }
}
