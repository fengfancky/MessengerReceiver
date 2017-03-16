package com.example.office.messengerreceiver.interfacee;

/**
 * Created by WORK on 2017/2/15.
 */

public interface ChatInterfacea {
   void OnMessage(String text);   //消息回调
   void OnLoginSocket(int tag);   //连接回调
}
