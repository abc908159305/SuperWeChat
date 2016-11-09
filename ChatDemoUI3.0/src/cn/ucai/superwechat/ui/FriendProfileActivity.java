package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.MFGT;

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

    User user = null;
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
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        if (user == null) {
            MFGT.finish(this);
            return;
        }
        initView();
    }

    private void initView() {
        mivBack.setVisibility(View.VISIBLE);
        mtvTitle.setVisibility(View.VISIBLE);
        mtvTitle.setText(getString(R.string.userinfo_txt_profile));
        setUserInfo();
        isFriend();
    }

    private void setUserInfo() {
        EaseUserUtils.setAppUserAvatar(this, user.getMUserName(), mivAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserNick(), mtvUserNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserName(), mtvUserName);
    }


    public void isFriend() {
        if (SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName())) {
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
                break;
            case R.id.iv_Back:
                MFGT.finish(this);
                break;
        }
    }
}
