package com.liu.simplebutterknife;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liu.simple_butterknife_annotation.SimpleBindView;

public class MainActivity extends AppCompatActivity {

    @SimpleBindView(R.id.btn_main_content)
    TextView mTextContent;
    @SimpleBindView(R.id.btn_main_test)
    Button mBtnTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SimpleButterKnife.bind(this);
    }

    public void onBtnClick(View view) {
        mTextContent.setText(mBtnTest.getText().toString());
    }
}
