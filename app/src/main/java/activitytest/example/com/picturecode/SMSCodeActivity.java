package activitytest.example.com.picturecode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import activitytest.example.com.picturecode.R;
import activitytest.example.com.picturecode.RegisterCodeTimerService;
import cn.bmob.newsmssdk.BmobSMS;

import cn.bmob.newsmssdk.exception.BmobException;

import cn.bmob.newsmssdk.listener.RequestSMSCodeListener;

import cn.bmob.newsmssdk.listener.VerifySMSCodeListener;

import static activitytest.example.com.picturecode.R.id.code;


/**
 * Created by Administrator on 2017/5/13 0013.
 */

public class SMSCodeActivity extends AppCompatActivity implements View.OnClickListener{

    private Context mContext;
    private Intent mIntent;
    private Button btnCountdown, btnSend,btn;
    private EditText etPhone, etNumber,et;
    private ImageView image;
    private String codeStr;
    private CodeUtils codeUtils;
    private String strPhoneNumber;

    // 广播接收者
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case RegisterCodeTimerService.IN_RUNNING:
                    if (btnCountdown.isEnabled())
                        btnCountdown.setEnabled(false);
                    // 正在倒计时
                    btnCountdown.setText("获取验证码(" + intent.getStringExtra("time") + ")");
                    break;
                case RegisterCodeTimerService.END_RUNNING:
                    // 完成倒计时
                    btnCountdown.setEnabled(true);
                    btnCountdown.setText(R.string.yanzhengma);

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smscode_layout);
        mContext = this;
        // SMS初始化
        BmobSMS.initialize(mContext, "3d9d9f910c51b02eea3d605178911aa5");
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册广播
        registerReceiver(mUpdateReceiver, updateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除注册
        unregisterReceiver(mUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 注册广播
    private static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RegisterCodeTimerService.IN_RUNNING);
        intentFilter.addAction(RegisterCodeTimerService.END_RUNNING);
        return intentFilter;
    }

    // 初始化界面
    private void init() {
        mIntent = new Intent(mContext, RegisterCodeTimerService.class);
        btnCountdown = (Button) findViewById(R.id.get_code);
        btnSend = (Button) findViewById(R.id.register);
        etPhone = (EditText) findViewById(R.id.phone_number);
        etNumber = (EditText) findViewById(code);
        image = (ImageView) findViewById(R.id.image);
        et = (EditText) findViewById(R.id.et);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        btnCountdown.setOnClickListener(this);
        btnSend.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.get_code:
                strPhoneNumber = etPhone.getText().toString();
                if (strPhoneNumber == null || "".equals(strPhoneNumber) || strPhoneNumber.length() !=11) {
                    Toast.makeText(this, "电话号码输入有误", Toast.LENGTH_SHORT).show();
                }
                codeStr = et.getText().toString().trim();
                Log.e("codeStr", codeStr);
                if (null == codeStr || TextUtils.isEmpty(codeStr)) {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = codeUtils.getCode();
                Log.e("code", code);
                if (code.equalsIgnoreCase(codeStr)) {
                    Toast.makeText(this, "验证码正确", Toast.LENGTH_SHORT).show();
                    // 将按钮设置为不可用状态
                    btnCountdown.setEnabled(false);
                    // 启动倒计时的服务
                    startService(mIntent);
                    // 通过requestSMSCode方式给绑定手机号的该用户发送指定短信模板的短信验证码
                    BmobSMS.requestSMSCode(mContext, etPhone.getText().toString(), "仅测试", new RequestSMSCodeListener() {
                        @Override
                        public void done(Integer smsId, BmobException ex) {
                            if (ex == null) {//验证码发送成功
                                Log.e("bmob", "短信id：" + smsId);//用于查询本次短信发送详情
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.register:
                String number = etNumber.getText().toString();
                if (number != null && number.length() == 6){
                    //通过verifySmsCode方式可验证该短信验证码
                    BmobSMS.verifySmsCode(mContext,etPhone.getText().toString(), number, new VerifySMSCodeListener() {
                        @Override
                        public void done(BmobException ex) {
                            if(ex==null){//短信验证码已验证成功
                                Log.e("bmob", "验证通过");
                                Toast.makeText(mContext, "提交验证码成功", Toast.LENGTH_SHORT).show();
                                //Intent intent = new Intent(SMSCodeActivity.this, TabPagerActivity.class);验证成功的点击事件跳转
                                //startActivity(intent);
                            }else{
                                Log.e("bmob", "验证失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                            }
                        }
                    });
                }else {
                    Toast.makeText(mContext, "验证码长度不正确", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn:
                codeUtils = CodeUtils.getInstance();
                Bitmap bitmap = codeUtils.createBitmap();
                image.setImageBitmap(bitmap);

                break;
        }
    }
}