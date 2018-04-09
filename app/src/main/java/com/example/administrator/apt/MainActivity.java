package com.example.administrator.apt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.annomation.DIActivity;
import com.example.annomation.DIView;

@DIActivity
public class MainActivity extends AppCompatActivity {

    @DIView(R.id.textview)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DIMainActivity.bindView(this);
        initView();
    }

    private void initView() {
        mTextView.setText("TimCoder");
    }
}
