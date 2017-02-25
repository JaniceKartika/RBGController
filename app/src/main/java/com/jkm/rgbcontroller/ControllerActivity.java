package com.jkm.rgbcontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.SeekBar;

public class ControllerActivity extends AppCompatActivity {
    private static final String TAG = ControllerActivity.class.getSimpleName();
    private SeekBar sbRed, sbGreen, sbBlue;
    private Button btMode1, btMode2, btMode3;
    private Button btKeypad1, btKeypad2, btKeypad3, btKeypad4, btKeypad5, btKeypad6, btKeypad7, btKeypad8, btKeypad9;
    private AnalogueView avController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        sbRed = (SeekBar) findViewById(R.id.sb_red);
        sbGreen = (SeekBar) findViewById(R.id.sb_green);
        sbBlue = (SeekBar) findViewById(R.id.sb_blue);

        btMode1 = (Button) findViewById(R.id.bt_mode_1);
        btMode2 = (Button) findViewById(R.id.bt_mode_2);
        btMode3 = (Button) findViewById(R.id.bt_mode_3);

        btKeypad1 = (Button) findViewById(R.id.bt_keypad_1);
        btKeypad2 = (Button) findViewById(R.id.bt_keypad_2);
        btKeypad3 = (Button) findViewById(R.id.bt_keypad_3);
        btKeypad4 = (Button) findViewById(R.id.bt_keypad_4);
        btKeypad5 = (Button) findViewById(R.id.bt_keypad_5);
        btKeypad6 = (Button) findViewById(R.id.bt_keypad_6);
        btKeypad7 = (Button) findViewById(R.id.bt_keypad_7);
        btKeypad8 = (Button) findViewById(R.id.bt_keypad_8);
        btKeypad9 = (Button) findViewById(R.id.bt_keypad_9);

        avController = (AnalogueView) findViewById(R.id.av_controller);

        avController.setOnMoveListener(new AnalogueView.OnMoveListener() {
            @Override
            public void onHalfMoveInDirection(double polarAngle) {

            }

            @Override
            public void onMaxMoveInDirection(double polarAngle) {

            }
        });
    }
}