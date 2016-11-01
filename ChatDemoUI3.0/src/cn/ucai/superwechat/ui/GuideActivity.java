package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.ucai.superwechat.R;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
    }

    public void login(View view) {
        startActivity(new Intent(this,LoginActivity.class));
    }

    public void register(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
    }
}
