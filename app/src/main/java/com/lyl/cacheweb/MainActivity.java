package com.lyl.cacheweb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTxtHost;
    private EditText mEdtUrl;
    private Button mBtnSreach;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtHost = (TextView) findViewById(R.id.host);
        mEdtUrl = (EditText) findViewById(R.id.edt_url);
        mBtnSreach = (Button) findViewById(R.id.btn_sreach);

        mBtnSreach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sreachUrl();
            }
        });

        mEdtUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    sreachUrl();
                }
                return false;
            }
        });

        mTxtHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = mTxtHost.getText().toString().trim();
                if (host.startsWith("https")) {
                    mTxtHost.setText("http://");
                } else {
                    mTxtHost.setText("https://");
                }
            }
        });
    }

    private void sreachUrl() {
        String edt = mEdtUrl.getText().toString().trim();
        if (edt.startsWith("https") || edt.startsWith("http")) {
            mUrl = edt;
        } else {
            mUrl = mTxtHost.getText().toString() + edt;
        }

        Intent intent = new Intent(MainActivity.this, Html5Activity.class);
        if (!TextUtils.isEmpty(edt)) {
            Bundle bundle = new Bundle();
            bundle.putString("url", mUrl);
            intent.putExtra("bundle", bundle);
        }
        startActivity(intent);
    }
}
