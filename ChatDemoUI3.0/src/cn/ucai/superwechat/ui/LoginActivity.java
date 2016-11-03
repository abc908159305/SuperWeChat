/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.db.SuperWeChatDBManager;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Login screen
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE_SETNICK = 1;
    @Bind(R.id.iv_Back)
    ImageView mivBack;
    @Bind(R.id.tv_Title)
    TextView mtvTitle;
    @Bind(R.id.username)
    EditText musername;
    @Bind(R.id.password)
    EditText mpassword;

    private boolean progressShow;
    private boolean autoLogin = false;
    ProgressDialog pd = null;
    String currentUsername;
    String currentPassword;
    LoginActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 若已登陆，则直接跳转到MainActivity
        if (SuperWeChatHelper.getInstance().isLoggedIn()) {
            autoLogin = true;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            return;
        }
        setContentView(R.layout.em_activity_login);
        ButterKnife.bind(this);
        mContext = this;
        initView();
        setListener();

    }

    private void setListener() {
        // 若用户名改变，则清空密码
        musername.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mpassword.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        //填充登陆账号
        if (SuperWeChatHelper.getInstance().getCurrentUsernName() != null) {
            musername.setText(SuperWeChatHelper.getInstance().getCurrentUsernName());
        }
        mivBack.setVisibility(View.VISIBLE);
        mtvTitle.setVisibility(View.VISIBLE);
        mtvTitle.setText(R.string.login);
    }


    public void login() {
        if (!EaseCommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        currentUsername = musername.getText().toString().trim();
        currentPassword = mpassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        progressShow = true;
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "EMClient.getInstance().onCancel");
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));
        pd.show();

        loginEMServer();
    }

    private void loginEMServer() {
        // After logout，the DemoDB may still be accessed due to async callback, so the DemoDB will be re-opened again.
        // close it before login to make sure DemoDB not overlap
        SuperWeChatDBManager.getInstance().closeDB();

        // reset current user name before login
        SuperWeChatHelper.getInstance().setCurrentUserName(currentUsername);

        final long start = System.currentTimeMillis();
        // call login method
        Log.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(currentUsername, MD5.getMessageDigest(currentPassword), new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                loginAppServer();
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress");
            }

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login: onError: " + code);
                if (!progressShow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loginAppServer() {
        NetDao.login(this, currentUsername, currentPassword, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                L.e(TAG,"s======"+s);
                if (s != null && s != "") {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        User user = (User) result.getRetData();
                        L.e(TAG,"user======"+user);
                        if (user != null) {
                            UserDao dao = new UserDao(mContext);
                            dao.saveUser(user);
                            SuperWeChatHelper.getInstance().setCurrentUser(user);
                            loginSuccess();
                        }
                    } else {
                        pd.dismiss();
                        L.e(TAG, "login fail,,,,,," + result.getRetCode());
                    }
                } else {
                    pd.dismiss();
                }
                //loginSuccess();
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
                L.e(TAG,"error======"+error);
            }
        });
    }

    private void loginSuccess() {
        // ** manually load all local groups and conversation
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();

        // update current user's display name for APNs
        boolean updatenick = EMClient.getInstance().updateCurrentUserNick(
                SuperWeChatApplication.currentUserNick.trim());
        if (!updatenick) {
            Log.e("LoginActivity", "update current user nick fail");
        }

        if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
            pd.dismiss();
        }
        // get user's info (this should be get from App's server or 3rd party service)
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        startActivity(intent);

        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }
    }


    @OnClick({R.id.btn_login, R.id.tv_Register, R.id.tv_hint,R.id.iv_Back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.tv_Register:
                MFGT.gotoRegister(this);
                break;
            case R.id.tv_hint:
                CommonUtils.showLongToast("暂时没有办法呢");
                break;
            case R.id.iv_Back:
                MFGT.finish(this);
                break;
        }
    }
}
