package com.example.chaihongwei.androidpagetab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PageTabView pageTabView = findViewById(R.id.pageTabView);

        List<PageTabView.TabItem> tabItems = new ArrayList() {
            {
                add(new PageTabView.TabItem("已处理"));
                add(new PageTabView.TabItem("处理中"));
                add(new PageTabView.TabItem("未处理"));
            }
        };

        pageTabView.setTabItems(tabItems);
        pageTabView.setSelectItemIndex(2);

        pageTabView.setOnTabChangedListener(new PageTabView.OnTabChangedListener() {
            @Override
            public void onChanged(int selectedIndex) {
                Toast.makeText(MainActivity.this, "" + selectedIndex, Toast.LENGTH_LONG).show();
            }
        });
    }
}
