package com.example.office.messengerreceiver.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.office.messengerreceiver.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView textView,showMsg;
    private boolean running = false;
    private Button accept;
    private ServerSocket mServerSocket;
    private Socket socket;
    private AcceptThread mAcceptThread;
    private List<Socket> socketList=new ArrayList<>();
    private boolean quit;
    private StringBuffer stringBuffter=new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView= (TextView) findViewById(R.id.text);
        showMsg= (TextView) findViewById(R.id.show_msg);
        accept= (Button) findViewById(R.id.accept);
        textView.setText(getIpAddressString());
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAcceptThread = new AcceptThread();
                running = true;
                mAcceptThread.start();
            }
        });
    }

    //客户端连接监听线程
    private class AcceptThread extends Thread{
        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket(12333);//建立一个ServerSocket服务器端
                while (!quit){
                    socket = mServerSocket.accept();//阻塞直到有socket客户端连接
                    socketList.add(socket);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.write("login Socket\r\n");
                    writer.flush();
                    new Thread(new ReceiveRunable(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //接收来自客户端的消息

    private class ReceiveRunable implements Runnable{
        //定义当前线程所处理的socket
        Socket socket=null;
        BufferedReader bufferedReader=null;
        public ReceiveRunable(Socket s){
            this.socket=s;
            try{
                bufferedReader=new BufferedReader(new InputStreamReader(s.getInputStream()));
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            String read=null;
            while ((read=readFromClient())!=null){
                stringBuffter.append(read).append("  ");
                Message message=new Message();
                message.what=1;
                message.obj=stringBuffter;
                mHandler.sendMessage(message);
            }
        }
        private String readFromClient(){
            try{
                return bufferedReader.readLine();
            }catch (IOException e){
                socketList.remove(socket);
            }
            return  null;
        }
    }




    private Handler mHandler =new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    showMsg.setText(msg.obj.toString());
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        quit=true;
        socketList.clear();
    }

    public  String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
}
