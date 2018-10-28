package com.venom.venomcontrol;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Socket ev3_clientSocket;
    Socket esp_clientSocket;
    BufferedReader inbound;
    PrintWriter outbound;
    BufferedReader inbound2;
    PrintWriter outbound2;

    String sendStr2 = "";

    String sendStr = "";

    Button FWD, BACK, LEFT, RIGHT, CONNECT, RESET, EV3_UP, EV3_DOWN;
    SeekBar seekBar;
    TextView ev3Status, espStatus, servoDegree;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        connection_Handler();

        btn_Handler();

        setDeviceButton(false);
    }

    private void init(){
        CONNECT = findViewById(R.id.btn_connect);
        FWD= findViewById(R.id.btn_front);
        BACK = findViewById(R.id.btn_back);
        LEFT = findViewById(R.id.btn_left);
        RIGHT = findViewById(R.id.btn_right);
        RESET = findViewById(R.id.btn_reset);
        EV3_UP = findViewById(R.id.btn_ev3_up);
        EV3_DOWN = findViewById(R.id.btn_ev3_down);
        ev3Status = findViewById(R.id.ev3_status_tv);
        espStatus = findViewById(R.id.esp_status_tv);
        seekBar = findViewById(R.id.seekBar);
        servoDegree = findViewById(R.id.degree_tv);

    }

    private void connection_Handler(){

        // Connect button initialize
        CONNECT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CONNECT.getText().equals("Connect")){
                    Thread socketEV3 = new Thread(_socketEV3);
                    Thread socketESP = new Thread(_socketESP);
                    socketEV3.start();
                    socketESP.start();
                }
                else if (ev3_clientSocket.isConnected() && esp_clientSocket.isConnected()){
                    try{
                        Log.w("Debug", "ClientSocket Close");
                        ev3_clientSocket.close();
                        esp_clientSocket.close();
                        CONNECT.setText("Connect");
                        setDeviceButton(false);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    Runnable _socketEV3= new Runnable() {
        @Override
        public void run() {
            try{

                ev3_clientSocket = new Socket("192.168.0.2", 1234);

                if (ev3_clientSocket.isConnected()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CONNECT.setText("Close");
                            ev3Status.setText("Connected!");
                            setDeviceButton(true);
                        }
                    });
                }

                Log.d("Debug", "is?" + ev3_clientSocket.isConnected());

                //Socket receive
                inbound = new BufferedReader(new InputStreamReader(
                        ev3_clientSocket.getInputStream()
                ));

                outbound = new PrintWriter(ev3_clientSocket.getOutputStream(), true);
            } catch (IOException ioe) {
                System.err.println("IOException:" + ioe);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CONNECT.setText("Connect");
                        ev3Status.setText("Disconnected!");
                        setDeviceButton(false);
                    }
                });
            }
        }
    };

    Runnable _socketESP= new Runnable() {
        @Override
        public void run() {
            try{

                esp_clientSocket = new Socket("192.168.0.3", 2345);

                if (esp_clientSocket.isConnected()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CONNECT.setText("Close");
                            espStatus.setText("Connected!");
                            setDeviceButton(true);
                        }
                    });
                }

                Log.d("Debug", "is?" + esp_clientSocket.isConnected());

                //Socket receive
                inbound2 = new BufferedReader(new InputStreamReader(
                        esp_clientSocket.getInputStream()
                ));

                outbound2 = new PrintWriter(esp_clientSocket.getOutputStream(), true);
            } catch (IOException ioe) {
                System.err.println("IOException:" + ioe);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CONNECT.setText("Connect");
                        espStatus.setText("Disconnected!");
                        setDeviceButton(false);
                    }
                });
            }
        }
    };

    Runnable _streamEV3 = new Runnable() {
        @Override
        public void run() {
            try {
                String sss = sendStr + "\r\n";

                char send[] = sss.toCharArray();

                Log.d("Debug", "送出:" + sss);

                //送出資料
                outbound.println(send);

                String inputLine = "";

                //接收資料
                while ((inputLine  = inbound.readLine()) == null){
                }
                Log.d("Debug", "收到" + inputLine);
            } catch (Exception e){
                System.err.println("IOException" + e);
            }
        }
    };

    Runnable _streamESP = new Runnable() {
        @Override
        public void run() {
            try {
                String sss2 = sendStr2 + "\r\n";

                char sendtoESP[] = sss2.toCharArray();

                Log.d("Debug", "送出:" + sss2);

                //送出資料
                outbound2.println(sendtoESP);

                String inputLine = "";

                //接收資料
                while ((inputLine  = inbound2.readLine()) == null){
                }
                Log.d("Debug", "收到" + inputLine);
            } catch (Exception e){
                System.err.println("IOException" + e);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void btn_Handler(){

        FWD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStr = "FWD";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStr = "STOP";
                    sendEV3();
                }
                return false;
            }
        });


        BACK.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStr = "BACK";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStr = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        LEFT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStr = "LEFT";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStr = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        RIGHT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStr = "RIGHT";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStr = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        RESET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStr = "STOP";
                sendEV3();
            }
        });

        EV3_UP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStr2 = "ninety";
                sendESP();
            }
        });

        EV3_DOWN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStr2 = "zero";
                sendESP();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int degree, boolean b) {
                sendStr2 = String.valueOf(degree);
                sendESP();
                servoDegree.setText("當前角度："+degree);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setDeviceButton(boolean b){
        FWD.setEnabled(b);
        BACK.setEnabled(b);
        LEFT.setEnabled(b);
        RIGHT.setEnabled(b);
        RESET.setEnabled(b);
        EV3_UP.setEnabled(b);
        EV3_DOWN.setEnabled(b);
    }

    private void sendEV3(){
        Thread streamEV3 = new Thread(_streamEV3);
        streamEV3.start();
    }

    private void sendESP(){
        Thread streamESP = new Thread(_streamESP);
        streamESP.start();
    }
}
