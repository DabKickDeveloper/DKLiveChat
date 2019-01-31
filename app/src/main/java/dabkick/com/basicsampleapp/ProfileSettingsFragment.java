package dabkick.com.basicsampleapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dabkick.engine.Public.CallbackListener;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class ProfileSettingsFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.profile_user_name)
    AppCompatTextView mUserName;
    @BindView(R.id.profile_img_view)
    AppCompatImageView mProfileImgView;
    @BindView(R.id.profile_pic_edit_view)
    AppCompatTextView mEditTextBtn;

    public static final int PERMISSIONS_REQUEST_CAMERA = 0;
    public static final int CAMERA_REQUEST_CODE = 100;
    boolean isCameraPermissionGranted;
    File mProfileImgFile = null;

    public static ProfileSettingsFragment newInstance() {
        ProfileSettingsFragment fragment = new ProfileSettingsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_profile_settings, container, false);

        ButterKnife.bind(this, view);
        mEditTextBtn.setOnClickListener(this);

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(false);
        }

        mUserName.setText("Username: " + PreferenceHandler.getUserName(BaseActivity.mCurrentActivity));
        if (!PreferenceHandler.getUserProfileImg(BaseActivity.mCurrentActivity).trim().isEmpty())
            Picasso.get().load(Uri.parse(PreferenceHandler.getUserProfileImg(BaseActivity.mCurrentActivity))).placeholder(R.drawable.avatar_img).error(R.drawable.avatar_img).into(mProfileImgView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            isCameraPermissionGranted = false;
            requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            isCameraPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                isCameraPermissionGranted = true;
                takePicture();
                break;
        }
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (isCameraPermissionGranted) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                mProfileImgFile = new File(dir, "DabkickProfileImg.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mProfileImgFile));
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
            }
        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Picasso.get().load(Uri.fromFile(mProfileImgFile)).placeholder(R.drawable.avatar_img).error(R.drawable.avatar_img).into(mProfileImgView);
                PreferenceHandler.setUserProfileImg(BaseActivity.mCurrentActivity, Uri.fromFile(mProfileImgFile).toString());
                //to be used. currently causing app crash
                SplashScreenActivity.dkLiveChat.updateUserProfilePicture(mProfileImgFile.getAbsolutePath(), new CallbackListener() {
                    @Override
                    public void onSuccess(String s, Object... objects) {
                        Toast.makeText(BaseActivity.mCurrentActivity, "Successfully uploaded profile img", Toast.LENGTH_LONG).show();
                        PreferenceHandler.setUserProfileImg(getActivity(), s);

                    }

                    @Override
                    public void onError(String s, Object... objects) {

                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(true);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_pic_edit_view) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                isCameraPermissionGranted = false;
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
            } else {
                takePicture();
            }
        }
    }
}
