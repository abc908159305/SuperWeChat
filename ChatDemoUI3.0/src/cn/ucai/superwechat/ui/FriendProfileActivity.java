package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        if (user == null) {
            MFGT.finish(this);
        }
        initView();
    }

    private void initView() {
        mivBack.setVisibility(View.VISIBLE);
        mtvTitle.setVisibility(View.VISIBLE);
        mtvTitle.setText(getString(R.string.userinfo_txt_profile));
        setUserInfo();
    }
    private void setUserInfo() {
        EaseUserUtils.setAppUserAvatar(this,user.getMUserName(),mivAvatar);
        EaseUserUtils.setAppUserNick(user.getMUserName(),mtvUserNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserName(),mtvUserName);
    }
    @OnClick(R.id.iv_Back)
    public void onBack() {
        MFGT.finish(this);
    }
}
