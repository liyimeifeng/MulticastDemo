package com.example.li.duobotest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 2017/2/6 0006.
 */

public class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "RecycleAdapter";
    private List<String> contents ;
    private List<String> ip_lists;

    public RecycleAdapter(List<String> ip,List<String> contents){
        Log.i(TAG, "RecycleAdapter: ");
        this.contents = contents;
        this.ip_lists = ip;
        notifyDataSetChanged();   //立即通知刷新recyclerview
    }


    public void clearData(){
//        contents.removeAll(contents.subList());
//        ip_lists.removeAll()
        contents.clear();
        ip_lists.clear();
        notifyItemMoved(0,ip_lists.size());         //通知清除数据，试过并没有什么卵用
        notifyItemRangeRemoved(0,contents.size());
        notifyItemRangeChanged(0,contents.size());
        notifyDataSetChanged();   //立即通知刷新recyclerview，important！！！！！

    }
//    public void addAll(List<String> con) {
//        this.contents.addAll(con);
//    }

    public RecycleAdapter(){
        super();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: ");
        if (holder instanceof MyViewHolder){
            MyViewHolder viewHolder = (MyViewHolder)holder;
            viewHolder.mTextView.setText("收到来自" + ip_lists.get(position) + "的消息："+ contents.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        MyViewHolder holder = new MyViewHolder(inflater.inflate(R.layout.content_layout,parent,false));
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mTextView;

        public MyViewHolder(View itemView){
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.activity_main_receive_info);
        }
    }
}
