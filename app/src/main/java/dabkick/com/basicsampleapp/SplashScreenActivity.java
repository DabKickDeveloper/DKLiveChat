package dabkick.com.basicsampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dabkick.engine.DKServer.DeveloperData;
import com.dabkick.engine.Public.Authentication;
import com.dabkick.engine.Public.CallbackListener;
import com.dabkick.engine.Public.DKLiveChat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dabkick.com.basicsampleapp.Utils.APIInterface;
import dabkick.com.basicsampleapp.Utils.RetrofitInit;
import dabkick.com.basicsampleapp.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SplashScreenActivity extends BaseActivity {

    @BindView(R.id.done_btn)
    AppCompatButton mDoneBtn;
    @BindView(R.id.user_name_edit_text)
    AppCompatEditText mUserNameEditText;
    @BindView(R.id.dev_key_edit_text)
    AppCompatEditText mUserDevKeyEdiText;
    @BindView(R.id.dev_id_edit_text)
    AppCompatEditText mUserDevId;
    @BindView(R.id.user_id_edit_text)
    AppCompatEditText mUserId;

    @BindView(R.id.container)
    RelativeLayout mNameEditTextContainer;
    private Unbinder unbinder;

    public static DKLiveChat dkLiveChat;
    private String devId, devKey;
    private Authentication authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        unbinder = ButterKnife.bind(this);
        setUserDetailsIfUpdated();
        mUserDevKeyEdiText.setText("3d8a7db548d5d91447d64d09a37f12");
        mUserDevId.setText("DK09aff676f38011e88a1a06f");
    }

    public void setUserDetailsIfUpdated() {
        String name = PreferenceHandler.getUserName(SplashScreenActivity.this);
        mUserNameEditText.setText(name);
        mUserNameEditText.setSelection(name.length());

        String devKey = PreferenceHandler.getDevKey(SplashScreenActivity.this);
        mUserDevKeyEdiText.setText(devKey);
        mUserDevKeyEdiText.setSelection(devKey.length());

        String devId = PreferenceHandler.getDevId(SplashScreenActivity.this);
        mUserDevId.setText(devId);
        mUserDevId.setSelection(devId.length());

        String userId = PreferenceHandler.getUserId(SplashScreenActivity.this);
        mUserId.setText(userId);
        mUserId.setSelection(userId.length());

    }

    public void initEngine() {
        String enteredUserId;
        if (!mUserId.getText().toString().trim().isEmpty()) {
            enteredUserId = mUserId.getText().toString();
        } else {
            enteredUserId = "";
        }

        if (enteredUserId.equalsIgnoreCase("")) {
            devId = "DK09aff676f38011e88a1a06f";
            devKey = "3d8a7db548d5d91447d64d09a37f12";
            authentication = new Authentication("DK09aff676f38011e88a1a06f", "3d8a7db548d5d91447d64d09a37f12");
            dkLiveChat = new DKLiveChat(this, authentication, new CallbackListener() {
                @Override
                public void onSuccess(String s, Object... objects) {
                    PreferenceHandler.setUserId(SplashScreenActivity.this, enteredUserId);
                    PreferenceHandler.setUserName(SplashScreenActivity.this, mUserNameEditText.getText().toString().trim());
                    PreferenceHandler.setUserId(SplashScreenActivity.this, enteredUserId);
                    PreferenceHandler.setDevId(BaseActivity.mCurrentActivity, devId);
                    PreferenceHandler.setDevKey(BaseActivity.mCurrentActivity, devKey);
                    launchHomePage();
                }

                @Override
                public void onError(String s, Object... objects) {
                    mDoneBtn.setEnabled(true);
                    Toast.makeText(BaseActivity.mCurrentActivity, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Retrofit retrofit = new RetrofitInit(getApplicationContext()).initializeRetrofit();
            APIInterface devIdKeyInterface = retrofit.create(APIInterface.class);
            Call<DeveloperData> developerDataCall = devIdKeyInterface.getDevIdandKey(enteredUserId);
            developerDataCall.enqueue(new Callback<DeveloperData>() {
                @Override
                public void onResponse(Call<DeveloperData> call, Response<DeveloperData> response) {
                    if (response.body() != null) {
                        authentication = new Authentication(response.body().getDeveloperId(), response.body().getDeveloperKey());
                        devId = response.body().getDeveloperId();
                        devKey = response.body().getDeveloperKey();
                        initDKLiveChat(enteredUserId);
                    }
                }

                @Override
                public void onFailure(Call<DeveloperData> call, Throwable t) {
                    mDoneBtn.setEnabled(true);
                    Toast.makeText(BaseActivity.mCurrentActivity, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initDKLiveChat(String enteredUserId) {
        dkLiveChat = new DKLiveChat(SplashScreenActivity.this, authentication, new CallbackListener() {
            @Override
            public void onSuccess(String s, Object... objects) {
                PreferenceHandler.setUserId(SplashScreenActivity.this, enteredUserId);
                PreferenceHandler.setUserName(SplashScreenActivity.this, mUserNameEditText.getText().toString().trim());
                PreferenceHandler.setDevId(BaseActivity.mCurrentActivity, devId);
                PreferenceHandler.setDevKey(BaseActivity.mCurrentActivity, devKey);
                launchHomePage();
            }

            @Override
            public void onError(String s, Object... objects) {
                mDoneBtn.setEnabled(true);
                Toast.makeText(BaseActivity.mCurrentActivity, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.done_btn)
    public void doneClick() {
        //initialize engine
        mDoneBtn.setEnabled(false);
        initEngine();
    }

    public void launchHomePage() {
        Intent intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //hides kb
        Utils.hideKeyboard(SplashScreenActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
