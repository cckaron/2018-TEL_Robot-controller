package com.venom.venomcontrol;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

    String sendStrEV3 = "";

    String sendStrESP = "";

    Button FWD, BACK, LEFT, RIGHT, CONNECT_EV3, CONNECT_ESP, GET, RELEASE, PULL, PUT, HOLD, FLATTEN, RING, GOTCHA;
    SeekBar seekBar;
    TextView ev3Status, espStatus, servoDegree;

    Boolean isRing = true;
    Boolean isGotcha = true;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        connection_Handler();

        btn_Handler();

        setEV3Button(false);
        setESPButton(false);
    }

    private void init(){
        CONNECT_EV3 = findViewById(R.id.btn_connectEV3);
        CONNECT_ESP = findViewById(R.id.btn_connectESP);
        FWD= findViewById(R.id.btn_front);
        BACK = findViewById(R.id.btn_back);
        LEFT = findViewById(R.id.btn_left);
        RIGHT = findViewById(R.id.btn_right);
        GET = findViewById(R.id.btn_get);
        RELEASE = findViewById(R.id.btn_release);
        PULL = findViewById(R.id.btn_pull);
        PUT = findViewById(R.id.btn_put);
        HOLD = findViewById(R.id.btn_hold);
        FLATTEN = findViewById(R.id.btn_flatten);
        RING = findViewById(R.id.btn_ring);
        GOTCHA = findViewById(R.id.btn_gotcha);

        ev3Status = findViewById(R.id.ev3_status_tv);
        espStatus = findViewById(R.id.esp_status_tv);
        seekBar = findViewById(R.id.seekBar);
        servoDegree = findViewById(R.id.degree_tv);

    }

    private void connection_Handler(){
        // Connect button initialize
        CONNECT_EV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (CONNECT_EV3.getText().equals("連線")) {
                        Thread socketEV3 = new Thread(_socketEV3);
                        socketEV3.start();
                    } else if (CONNECT_EV3.getText().equals("中斷連線")) {
                        try {
                            Log.w("Debug", "ClientSocket Close");
                            ev3_clientSocket.close();
                            ev3Status.setText("未連線");
                            CONNECT_EV3.setText("連線");
                            setEV3Button(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (CONNECT_EV3.getText().equals("重新連線")) {
                        Thread socketEV3 = new Thread(_socketEV3);
                        socketEV3.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        CONNECT_ESP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (CONNECT_ESP.getText().equals("連線")) {
                        Thread socketESP = new Thread(_socketESP);
                        socketESP.start();
                    } else if (CONNECT_ESP.getText().equals("中斷連線")) {
                        try {
                            Log.w("Debug", "esp_ClientSocket Close");
                            esp_clientSocket.close();
                            espStatus.setText("未連線");
                            CONNECT_ESP.setText("連線");
                            setESPButton(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (CONNECT_ESP.getText().equals("重新連線")) {
                        Thread socketESP = new Thread(_socketESP);
                        socketESP.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                            CONNECT_EV3.setText("中斷連線");
                            ev3Status.setText("已連線!");
                            setEV3Button(true);
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
                        CONNECT_EV3.setText("重新連線");
                        ev3Status.setText("連線失敗!");
                        setEV3Button(false);
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
                            CONNECT_ESP.setText("中斷連線");
                            espStatus.setText("已連線!");
                            setESPButton(true);
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
                        CONNECT_ESP.setText("重新連線");
                        espStatus.setText("連線失敗!");
                        setESPButton(false);
                    }
                });
            }
        }
    };

    Runnable _streamEV3 = new Runnable() {
        @Override
        public void run() {
            try {
                String tempStrEV3 = sendStrEV3 + "\r\n";

                char sendEV3[] = tempStrEV3.toCharArray();

                Log.d("Debug", "送出:" + tempStrEV3);

                //送出資料
                outbound.println(sendEV3);

                String inputLineEV3 = "";

                //接收資料
                while ((inputLineEV3  = inbound.readLine()) == null){
                }
                Log.d("Debug", "收到" + inputLineEV3);
            } catch (Exception e){
                System.err.println("IOException" + e);
            }
        }
    };

    Runnable _streamESP = new Runnable() {
        @Override
        public void run() {
            try {
                String tempStrESP = sendStrESP + "\r\n";

                char sendtoESP[] = tempStrESP.toCharArray();

                Log.d("Debug", "送出:" + tempStrESP);

                //送出資料
                outbound2.println(sendtoESP);

                String inputLineESP = "";

                //接收資料
                while ((inputLineESP  = inbound2.readLine()) == null){
                }
                Log.d("Debug", "收到" + inputLineESP);
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
                    sendStrEV3 = "FWD";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP";
                    sendEV3();
                }
                return false;
            }
        });


        BACK.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "BACK";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        LEFT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "LEFT";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        RIGHT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "RIGHT";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP";
                    sendEV3();
                }
                return false;
            }
        });

        GET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrESP = "1";
                    sendESP();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrESP = "100";
                    sendESP();
                }
                return false;
            }
        });

        RELEASE.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrESP = "2";
                    sendESP();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrESP = "100";
                    sendESP();
                }
                return false;
            }
        });

        PULL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "PULL";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP_PULL";
                    sendEV3();
                }
                return false;
            }
        });

        PUT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "PUT";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOP_PUT";
                    sendEV3();
                }
                return false;
            }
        });

        HOLD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "HOLD";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOPB";
                    sendEV3();
                }
                return false;
            }
        });

        FLATTEN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sendStrEV3 = "FLATTEN";
                    sendEV3();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sendStrEV3 = "STOPB";
                    sendEV3();
                }
                return false;
            }
        });

        RING.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRing){
                    isRing = false;
                    sendStrESP = "5";
                } else {
                    isRing = true;
                    sendStrESP = "6";
                }
                sendESP();
            }
        });

        GOTCHA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGotcha){
                    isGotcha = false;
                    sendStrESP = "3";
                } else {
                    isRing = true;
                    sendStrESP = "4";
                }
                sendESP();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int degree, boolean b) {
                // if degree equals zero, set it to 1000
                if (degree == 0) degree = 1000;

                sendStrESP = String.valueOf(degree);
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

        servoDegree.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
    }

    private void setEV3Button(boolean b){
        FWD.setEnabled(b);
        BACK.setEnabled(b);
        LEFT.setEnabled(b);
        RIGHT.setEnabled(b);
        HOLD.setEnabled(b);
        FLATTEN.setEnabled(b);
        PULL.setEnabled(b);
        PUT.setEnabled(b);
        GET.setEnabled(b);
        RELEASE.setEnabled(b);
    }

    private void setESPButton(boolean b){
        RING.setEnabled(b);
        GOTCHA.setEnabled(b);
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
