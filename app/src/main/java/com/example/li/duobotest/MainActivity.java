package com.example.li.duobotest;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mSendButton;
    private EditText mSendInfoView;
    private TextView  mReceiveInfoView;
    private RecyclerView mRecycleView;
    private RecycleAdapter mAdapter;
    private String sendInfo;
    private final static String IP_ADDRESS = "239.255.255.250";
    private final static int PORT = 3721;
    private final static String TAG = "MainActivity";
    private String content,ip;
    private WifiManager.MulticastLock multicastLock;
    private List<String> contents = new ArrayList<>();
    private List<String> ip_lists = new ArrayList<>();
    private static final int MDNS_PORT = Integer.parseInt(System.getProperty("net.mdns.port", "5353"));   //把自己作为热点可能会用到的MDNS，即多播DNS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowMulticast();
        mSendButton = (Button)findViewById(R.id.activity_main_send);
        mSendInfoView = (EditText) findViewById(R.id.activity_main_send_info);
        mSendButton.setOnClickListener(this);
        mRecycleView = (RecyclerView)findViewById(R.id.activity_main_recycleView);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(manager);

        receiveBroad();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        multicastLock.release();
    }

    //打开设备多播锁，大部分手机默认是不打开，省电
    private void allowMulticast(){
        WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        multicastLock=wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    }

    @Override
    public void onClick(View v) {
         switch(v.getId()){
             case R.id.activity_main_send:
                 sendInfo = mSendInfoView.getText().toString();
                 initBroad();
                 break;
         }
    }

    //初始化组播，发送信息
    public void initBroad(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    MulticastSocket mSocket = new MulticastSocket(PORT);
                    InetAddress group = InetAddress.getByName(IP_ADDRESS);
                    if (!group.isMulticastAddress()) {
                        throw new Exception("地址不是多播地址");
                    }
                    byte[] buff = sendInfo.getBytes("utf-8");
                    //加入多播组，发送方与接收方处于同一组时，接收方可抓取多播报文信息
                    mSocket.joinGroup(group);
                    /**
                     * 下面方法把自己作为热点打开，然后开始收发组播，测试不起作用
                     */
//                    mSocket.joinGroup(new InetSocketAddress(group,PORT),NetworkInterface.getByInetAddress(group));
//                    mSocket.joinGroup(new InetSocketAddress(InetAddress.getByName("224.0.0.251"),MDNS_PORT),NetworkInterface.getByInetAddress(group));
                    //每一个报文最多被路由转发n次，当数字变成0时，该报文被丢弃
                    mSocket.setTimeToLive(4);
                    //设定UDP报文（内容、内容长度、多播组、端口号）
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, group, PORT);
                    mSocket.send(packet);
                    Log.i(TAG, "initBroad:------------>>>>>>>报文发送完毕");
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //接受信息
    public void receiveBroad(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    InetAddress group = InetAddress.getByName(IP_ADDRESS);
                    if ( !group.isMulticastAddress()){
                        throw new Exception("请使用多播地址");
                    }
                    while(true){
                        MulticastSocket receiveCast = new MulticastSocket(PORT);
                        receiveCast.joinGroup(group);
                        byte[] buff = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buff,buff.length,group,PORT);
                        receiveCast.receive(packet);//此方法是个阻塞方法，一直等条件触发
                         ip = packet.getAddress().toString();
                         content = new String(packet.getData(),packet.getOffset(),packet.getLength());
                        Log.i(TAG, "内容为-------------》" + content + "获取到的对方IP地址为-------》" + ip);
                        contents.add(content);
                        ip_lists.add(ip);
                        if (content != null){
                            mhandler.sendEmptyMessage(1);
                        }
                        receiveCast.close();
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private Handler mhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1){
                //收到消息之后再构造RecycleView适配器
                mAdapter = new RecycleAdapter(ip_lists,contents);
                mRecycleView.setAdapter(mAdapter);
                mRecycleView.scrollToPosition(contents.size());
                mRecycleView.smoothScrollToPosition(contents.size());
                boolean is = mRecycleView.canScrollVertically(-1);
            }
            return false;
        }
    });
}
