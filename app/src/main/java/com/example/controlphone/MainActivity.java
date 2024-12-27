package com.example.controlphone;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Socket clientSocket;
    private BufferedWriter bw;
    private JSONObject jsonWrite;
    private ConnectThread connectThread;

    public class ConnectThread extends Thread {
        private String address;
        private int port;
        private boolean connected = false;

        public ConnectThread(String address , int port){
            this.address = address;
            this.port = port;
        }
        @Override
        public void run() {
            while (!connected) {
                try {
                    clientSocket = new Socket(address, port);
                    connected = true;
                    Log.d("TCP", "Client: Connected to server");
                } catch (IOException e) {
                    Log.e("TCP", "S: Error", e);
                }
            }
        }

        public boolean isConnected() {
            return connected;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonExpression = findViewById(R.id.buttonExpression);
        RadioGroup radioGroupExpressions = findViewById(R.id.radioGroupExpressions);
        EditText editTextMessage2 = findViewById(R.id.editTextMessage2);
        EditText editTextMessage3 = findViewById(R.id.editTextMessage3);
        Button buttonSocket = findViewById(R.id.buttonSocket);
        Button buttonLookAtUsers = findViewById(R.id.buttonLookAtUsers);

        buttonSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = editTextMessage2.getText().toString();
                int port = Integer.parseInt(editTextMessage3.getText().toString());

                connectThread = new ConnectThread(address, port);
                connectThread.start();
            }
        });

        buttonExpression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroupExpressions.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                String message = editTextMessage.getText().toString();
                String expression = "";
                if (radioButton != null) {
                    expression = radioButton.getText().toString();
                }
                sendToZenbo(message, expression);
            }
        });

        buttonLookAtUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectThread != null && connectThread.isConnected()) {
                    sendLookAtUsersCommand();
                } else {
                    Toast.makeText(MainActivity.this, "Not connected to server", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToZenbo(String msg, String expression) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 確保連線已建立
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        Log.d("sendToZenbo","Connected!!");
                        jsonWrite = new JSONObject();
                        try {
                            jsonWrite.put("msg", msg);
                            jsonWrite.put("expression", expression);

                            bw.write(jsonWrite.toString()+"\n");
                            bw.flush();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Message sent to Zenbo:\n" + jsonWrite.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 連線建立失敗的處理
                        Log.e("TCP", "Connection failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendLookAtUsersCommand() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 確保連線已建立
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        Log.d("sendLookAtUsersCommand", "Connected!!");
                        jsonWrite = new JSONObject();
                        try {
                            jsonWrite.put("msg", "look");  // 使用 "look" 指令

                            bw.write(jsonWrite.toString() + "\n");
                            bw.flush();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "LookAtUsers command sent to Zenbo", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 連線建立失敗的處理
                        Log.e("TCP", "Connection failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
