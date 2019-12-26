package com.example.devin.listview_slide_pull;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CustomListView lvDectRecord;
    private List<String> dataList = new ArrayList<>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvDectRecord = findViewById(R.id.lv_dect_record);
        for (int i = 0; i < 36; i++) {
            dataList.add("item" + i);
        }

        adapter = new MyAdapter();
        lvDectRecord.setAdapter(adapter);
        lvDectRecord.setOnRefreshListener(new CustomListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_LONG).show();
                        dataList.add(0, "下拉刷新出的元素");
                        adapter.notifyDataSetChanged();
                        lvDectRecord.finishRefresh();
                    }
                }, 1200);
            }
        });

        lvDectRecord.setOnLoadMoreListener(new CustomListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "上拉加载成功", Toast.LENGTH_LONG).show();
                        dataList.add("上拉追加的元素");
                        adapter.notifyDataSetChanged();
                        lvDectRecord.finishLoadMore();
                    }
                }, 1200);
            }
        });
    }

    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText(dataList.get(position));
            tv.setTextSize(16);
            return tv;
        }
    }
}
