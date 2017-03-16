package com.example.office.messengerreceiver.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.office.messengerreceiver.interfacee.ChatInterfacea;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by WORK on 2017/2/15.
 */

public class WebSocketDmUtils extends WebSocketListener {
    public   WebSocket webSocket=null;
    private ChatInterfacea mchatInterfacea=null;
    private static WebSocketDmUtils webSocketDmListenner=null;
    private String socketpath;
    public static final int SUCCEED=1;
    public static final int FAILURE=0;
    public static final int TIME_OUT=100;
    public static final int UNFOUND_SERVICE=200;
    public static final int OTHER_ERROR=300;
    @Override
    public void onOpen(WebSocket webSocket1, Response response) {
        webSocket=webSocket1;
//        mchatInterfacea.OnLoginSocket(SUCCEED);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.e("MESSAGE>>>>>>>",text);
        mchatInterfacea.OnMessage(text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        Log.e("Close:",code+reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if(mchatInterfacea!=null&&webSocket!=null){
            closeWebSocket();
          mchatInterfacea.OnLoginSocket(FAILURE);
        }
        if(t instanceof SocketTimeoutException){   //连接超时
             Log.i("erro",t.getMessage());
            mchatInterfacea.OnLoginSocket(TIME_OUT);
        }else if(t instanceof UnknownHostException){ //服务器主机未找到
            Log.i("erro",t.getMessage());
            mchatInterfacea.OnLoginSocket(UNFOUND_SERVICE);
        }else{
            mchatInterfacea.OnLoginSocket(OTHER_ERROR);//其他错误
            t.printStackTrace();

        }

    }


    /**
     * 初始化WebSocket服务器
     */
    public void run(ChatInterfacea chatInterfacea, String path) {
        if(TextUtils.isEmpty(socketpath)){
            mchatInterfacea=chatInterfacea;
            socketpath=path;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)//允许失败重试
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(socketpath).build();
        client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    /**
     * @param s
     * @return
     */
    public boolean sendMessage(String s){
        return webSocket.send(s);
    }

    public  void closeWebSocket(){
        socketpath=null;
        webSocketDmListenner=null;
        if(webSocket!=null)
        webSocket.close(1000,"主动关闭");
        Log.e("close","关闭成功");
    }

    /**
     * 获取全局的ChatWebSocket类
     * @return ChatWebSocket
        */
        public static WebSocketDmUtils getChartWebSocket(){
        if(webSocketDmListenner==null) {
            webSocketDmListenner =new WebSocketDmUtils();
        }
        return webSocketDmListenner;
    }

}
