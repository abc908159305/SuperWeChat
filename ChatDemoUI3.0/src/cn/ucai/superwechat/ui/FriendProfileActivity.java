package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Created by Administrator on 2016/11/7.
 */
public class FriendProfileActivity extends BaseActivity {
    @Bind(R.id.iv_Back)
    ImageView mivBack;
    @Bind(R.id.tv_Title)
    TextView mtvTitle;
    @Bind(R.id.iv_Avatar)
    ImageView mivAvatar;
    @Bind(R.id.tv_userNick)
    TextView mtvUserNick;
    @Bind(R.id.tv_userName)
    TextView mtvUserName;

    String username;
    User user = null;
    boolean isFriend;

    @Bind(R.id.btn_AddContact)
    Button mbtnAddContact;
    @Bind(R.id.btn_Message)
    Button mbtnMessage;
    @Bind(R.id.btn_Video_Message)
    Button mbtnVideoMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);
        username = getIntent().getStringExtra(I.User.USER_NAME);
        if (username == null) {
            MFGT.finish(this);
            return;
        }
        initView();
        user = SuperWeChatHelper.getInstance().getAppContactList().get(username);
        if (user == null) {
            isFriend = false;
        } else {
            setUserInfo();
            isFriend = true;
        }
        isFriend(isFriend);
        syncUserInfo();
    }

    private void syncUserInfo() {
        NetDao.syncUserInfo(this, username, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getListResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        user = (User) result.getRetData();
                        if (user != null) {
                            setUserInfo();
                            if (isFriend) {
                                SuperWeChatHelper.getInstance().saveAppContact(user);
                            }
                        } else {
                            syncFail();
                        }
                    } else {
                        syncFail();
                    }
                } else {
                    syncFail();
                }
            }

            @Override
            public void onError(String error) {
                syncFail();
            }
        });
    }

    private void syncFail() {
        MFGT.finish(this);
        return;
    }

    private void initView() {
        mivBack.setVisibility(View.VISIBLE);
        mtvTitle.setVisibility(View.VISIBLE);
        mtvTitle.setText(getString(R.string.userinfo_txt_profile));

    }

    private void setUserInfo() {
        EaseUserUtils.setAppUserAvatar(this, user.getMUserName(), mivAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserNick(), mtvUserNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserName(), mtvUserName);
    }


    public void isFriend(boolean isFriend) {
        if (isFriend) {
            mbtnMessage.setVisibility(View.VISIBLE);
            mbtnVideoMessage.setVisibility(View.VISIBLE);
        } else {
            mbtnAddContact.setVisibility(View.VISIBLE);
        }

    }

    @OnClick({R.id.btn_AddContact, R.id.btn_Message, R.id.btn_Video_Message,R.id.iv_Back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_AddContact:
                MFGT.gotoAddFriendMsg(this,user.getMUserName());
                break;
            case R.id.btn_Message:
                MFGT.gotoChat(this,user.getMUserName());
                break;
            case R.id.btn_Video_Message:
                if (!EMClient.getInstance().isConnected())
                    Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
                else {
                    startActivity(new Intent(this, VideoCallActivity.class).putExtra("username", user.getMUserName())
                            .putExtra("isComingCall", false));
                    // videoCallBtn.setEnabled(false);
                }
                break;
            case R.id.iv_Back:
                MFGT.finish(this);
                break;
        }
    }
}
