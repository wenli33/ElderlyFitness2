package com.example.elderlyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    TextView stateText;
    TextView countText;
    ImageView stateImage;
    private SoundPool           soundPool;
    private SensorManager       sensorManager;
    private SensorEventListener sensorEventListener;
    boolean                     isStarted       = false;  //判斷是否開始
    boolean                     isStoped        = false;  //判斷是否結束
    boolean                     isTimed2        = false;  //開始計算平舉的時間
    boolean                     isTimed3        = false;  //開始計算垂直落下後的時間
    boolean                     step1           = false;  //垂直落下到平舉的狀態
    boolean                     step2           = false;  //平舉的狀態
    boolean                     step3           = false;  //平舉到垂直落下的狀態
    boolean                     isFinished      = false;  //做完一個動作
    long                        currentTime2    = 0;    //平舉一開始的時間
    long                        currentTime3    = 0;    //垂直落下一開始的時間
    static final float          NS2S            = 1.0f/1000000000.0f;
    float                       timestamp;
    float                       angle[]         = new float[3];
    int                         state           = -1;
    int                         excellent       = 0;
    int                         good            = 0;
    int                         soundID;
    static final double         ANGLE_EXCELLENT = 60.0;
    static final double         ANGLE_GOOD      = 45.0;
    static final double         ANGLE_FALL      = 60.0;
    static final double         GYROSCOPE_RAISE = 0.2;
    //常數
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stateText = (TextView)findViewById(R.id.state);
        countText = (TextView)findViewById(R.id.count);
        stateImage = (ImageView)findViewById(R.id.stateImage);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Button buttonStart  = (Button)findViewById(R.id.start);
        Button buttonStop = (Button)findViewById(R.id.stop);
        buttonStart.setOnClickListener(start);
        buttonStop.setOnClickListener(stop);
        if (Build.VERSION.SDK_INT >= 21) {
            soundPool = new SoundPool.Builder().build();
            soundID = soundPool.load(MainActivity.this, R.raw.music1, 1);//載入音頻檔案
        }
    }

    {
        sensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if(isStarted == false) {
                        return;
                    }
                    if(timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;
                        angle[0] = angle[0] + event.values[0] * dT;
                        angle[1] = angle[1] + event.values[1] * dT;
                        angle[2] = angle[2] + event.values[2] * dT;
                        //三軸角度
                        float anglex = (float) Math.toDegrees(angle[0]);
                        float angley = (float) Math.toDegrees(angle[1]);
                        float anglez = (float) Math.toDegrees(angle[2]);
                        Log.d("MainActivity", "angleX------------>" + anglex);
                        Log.d("MainActivity", "angleY------------>" + angley);
                        Log.d("MainActivity", "angleZ------------>" + anglez);

                        //判斷從垂直落下到平舉的狀態
                        if(step1 == false) {
                            //優秀
                            if((anglex >= ANGLE_EXCELLENT) || (angley >= ANGLE_EXCELLENT) || (anglez >= ANGLE_EXCELLENT)) {
                                state = 2;
                                //平舉中
                                if((Math.abs(event.values[0]) < GYROSCOPE_RAISE) && (Math.abs(event.values[1]) < GYROSCOPE_RAISE) && (Math.abs(event.values[2]) < GYROSCOPE_RAISE)) {
                                    //開始計算平舉時間
                                    if(isTimed2 == false) {
                                        currentTime2 = System.currentTimeMillis();
                                        Log.d("MainActivity", "currentTime2 = " + currentTime2);
                                        isTimed2 = true;
                                    }
                                    //判斷平舉狀態
                                    if(step2 == false) {
                                        while(System.currentTimeMillis() - currentTime2 < 5000){
                                            if((Math.abs(event.values[0]) < GYROSCOPE_RAISE) && (Math.abs(event.values[1]) < GYROSCOPE_RAISE) && (Math.abs(event.values[2]) < GYROSCOPE_RAISE)){
                                                Log.d("MainActivity", "state2 = " + state);
                                            }
                                            else{
                                                if(state == 2) {
                                                    state = 1;
                                                }
                                            }
                                        }
                                        //平舉完後過5秒要垂直落下，恢復初始值
                                        if(System.currentTimeMillis()- currentTime2 >= 5000) {
                                            if(isStoped == false) {
                                                sound();
                                                //優秀狀態
                                                if(state == 2) {
                                                    stateText.setText("優秀d(`･∀･)b");
                                                    Log.d("MainActivity","優秀d(`･∀･)b");
                                                    isFinished = true;
                                                    stateImage.setImageResource(R.drawable.excellent);
                                                    angle[0] = 0;
                                                    angle[1] = 0;
                                                    angle[2] = 0;
                                                    step3 = true;
                                                }
                                                //普通狀態
                                                else if(state == 1) {
                                                    stateText.setText("不錯喔(ゝ∀･)b");
                                                    Log.d("MainActivity", "不錯喔(ゝ∀･)b");
                                                    isFinished = true;
                                                    stateImage.setImageResource(R.drawable.good);
                                                    angle[0] = 0;
                                                    angle[1] = 0;
                                                    angle[2] = 0;
                                                    step3 = true;
                                                }
                                                isStoped = true;
                                            }
                                        }
                                    }
                                }
                            }
                            //普通
                            else if((anglex <= ANGLE_EXCELLENT && anglex >= ANGLE_GOOD) || (angley <= ANGLE_EXCELLENT && angley >= ANGLE_GOOD) || (anglez <= ANGLE_EXCELLENT && anglez >= ANGLE_GOOD)) {
                                state = 1;
                                //平舉中
                                if(Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE) {
                                    //開始計算平舉時間
                                    if(isTimed2 == false) {
                                        currentTime2 = System.currentTimeMillis();
                                        Log.d("MainActivity", "currentTime2 = " + currentTime2);
                                        isTimed2 = true;
                                    }
                                    //判斷平舉狀態
                                    if(step2 == false) {
                                        while(System.currentTimeMillis() - currentTime2 < 5000){
                                            if(Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE){
                                                Log.d("MainActivity", "state2 = " + state);
                                            }
                                        }
                                        //平舉完後過5秒要垂直落下，恢復初始值
                                        if(System.currentTimeMillis()- currentTime2 >= 5000) {
                                            if(isStoped == false) {

                                                sound();
                                                //優秀狀態
                                                if(state == 2) {
                                                    stateText.setText("優秀d(`･∀･)b");
                                                    Log.d("MainActivity", "優秀d(`･∀･)b");
                                                    stateImage.setImageResource(R.drawable.excellent);
                                                    angle[0] = 0;
                                                    angle[1] = 0;
                                                    angle[2] = 0;
                                                    anglex = 0;
                                                    angley = 0;
                                                    anglez = 0;
                                                    step3 = true;
                                                    Log.d("MainActivity", "angle[0] = " + angle[0] + "\nangle[1] = " + angle[1] + "\nangle[2] = " + angle[2]);

                                                }
                                                //普通狀態
                                                else if(state == 1) {
                                                    stateText.setText("不錯喔(ゝ∀･)b");
                                                    Log.d("MainActivity", "不錯喔(ゝ∀･)b");
                                                    stateImage.setImageResource(R.drawable.good);
                                                    angle[0] = 0;
                                                    angle[1] = 0;
                                                    angle[2] = 0;
                                                    anglex = 0;
                                                    angley = 0;
                                                    anglez = 0;
                                                    step3 = true;
                                                    Log.d("MainActivity", "angle[0] = " + angle[0] + "\nangle[1] = " + angle[1] + "\nangle[2] = " + angle[2]);
                                                }
                                                isStoped = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //判斷平舉到垂直落下的狀態
                        if(step3 == true) {
                            //優秀
                            if(state == 2) {
                                Log.d("MainActivity", "Math.abs(anglex) = " + Math.abs(anglex) + "\nMath.abs(angley) = " + Math.abs(angley) + "\nMath.abs(anglez) = " + Math.abs(anglez));
                                if(Math.abs(anglex) >= ANGLE_FALL || Math.abs(angley)>= ANGLE_FALL || Math.abs(anglez) >= ANGLE_FALL) {
                                    //垂直落下中
                                    if(Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE) {
                                        if (isTimed3 == false) {
                                            currentTime3 = System.currentTimeMillis();
                                            Log.d("MainActivity", "currentTime3 = " + currentTime3);
                                            isTimed3 = true;
                                        }
                                        //開始計算垂直落下的時間
                                        while (System.currentTimeMillis() - currentTime3 < 5000) {
                                            if (Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE) {
                                                Log.d("MainActivity", "state2 = " + state);
                                            }
                                        }
                                        //超過5秒後完成1次動作
                                        if (System.currentTimeMillis() - currentTime3 >= 5000) {

                                            sound();
                                            state = -1;
                                            angle[0] = 0;
                                            angle[1] = 0;
                                            angle[2] = 0;
                                            anglex = 0;
                                            angley = 0;
                                            anglez = 0;
                                            isStoped  = false;
                                            isTimed2  = false;
                                            isTimed3  = false;
                                            step1     = false;
                                            step2     = false;
                                            step3     = false;
                                            currentTime2 = 0;
                                            excellent++;
                                            int count = excellent + good;
                                            countText.setText("很棒喔!已經完成" + count + "次了!");
                                            Log.d("MainActivity", "很棒喔!已經完成" + count + "次了!");
                                            stateImage.setImageResource(R.drawable.normal);
                                        }
                                    }
                                }
                            }
                            //普通
                            else if(state == 1) {
                                //垂直落下中
                                if((Math.abs(anglex) <= ANGLE_FALL && Math.abs(anglex) >= ANGLE_GOOD) || (Math.abs(angley) <= ANGLE_FALL && Math.abs(angley) >= ANGLE_GOOD) || (Math.abs(anglez) <= ANGLE_FALL && Math.abs(anglez) >= ANGLE_GOOD)) {
                                    if(Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE) {
                                        if (isTimed3 == false) {
                                            currentTime3 = System.currentTimeMillis();
                                            Log.d("MainActivity", "currentTime3 = " + currentTime3);
                                            isTimed3 = true;
                                        }
                                        //開始計算垂直落下的時間
                                        while (System.currentTimeMillis() - currentTime3 < 5000) {
                                            if (Math.abs(event.values[0]) < GYROSCOPE_RAISE && Math.abs(event.values[1]) < GYROSCOPE_RAISE && Math.abs(event.values[2]) < GYROSCOPE_RAISE) {
                                                Log.d("MainActivity", "state2 = " + state);
                                            }
                                        }
                                        //超過5秒後完成1次動作
                                        if (System.currentTimeMillis() - currentTime3 >= 5000) {
                                            sound();
                                            state = -1;
                                            angle[0] = 0;
                                            angle[1] = 0;
                                            angle[2] = 0;
                                            anglex = 0;
                                            angley = 0;
                                            anglez = 0;
                                            isStoped  = false;
                                            isTimed2  = false;
                                            isTimed3  = false;
                                            step1     = false;
                                            step2     = false;
                                            step3     = false;
                                            currentTime2 = 0;
                                            currentTime3 = 0;
                                            good++;
                                            int count = excellent + good;
                                            countText.setText("很棒喔!已經完成" + count + "次了!");
                                            Log.d("MainActivity", "很棒喔!已經完成" + count + "次了!");
                                            stateImage.setImageResource(R.drawable.normal);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    timestamp = event.timestamp;
                }

            }

            //當量測值的精準度改變時會被呼叫
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    View.OnClickListener start = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isStarted == true){
                return;
            }
            isStarted = true;
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        }
    };

    View.OnClickListener stop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isStarted == false) {
                return;
            }
            isStarted = false;
            isStoped  = false;
            isTimed2  = false;
            step1     = false;
            step2     = false;
            currentTime2 = 0;
            currentTime3 = 0;
            stateText.setText("您的動作優秀為" + excellent + "次\n很好為" + good + "次");
            stateImage.setImageResource(R.drawable.normal);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        }
    };

    private void sound() {
        soundPool.play(
                soundID,
                0.5f,      //左耳道音量0~1
                0.5f,      //右耳道音量0~1
                0,         //播放優先權
                0,         //循環模式0是1次,-1是無限次,0以上就是n+1次
                1          //撥放速度0~2
        );
    }
    /*
    public void sound(View v){
        sound();
    }
    */
    //啟動感測器
    @Override
    protected void onResume() {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        Log.d("MainActivity", "onResume");
    }
    //當程式進入背景或結束時就不會讀取感測器，取消註冊
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        Log.d("MainActivity" , "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        Log.d("MainActivity" , "onDestroy");
        super.onDestroy();
    }
}