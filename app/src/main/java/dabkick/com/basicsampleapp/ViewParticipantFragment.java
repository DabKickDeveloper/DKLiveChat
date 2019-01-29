package dabkick.com.basicsampleapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dabkick.engine.Public.CallbackListener;
import com.dabkick.engine.Public.UserInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dabkick.com.basicsampleapp.Adapters.FragmentCloseListener;
import dabkick.com.basicsampleapp.Adapters.ParticipantListAdapter;

public class ViewParticipantFragment extends Fragment {

    private Unbinder mUnbinder;

    @BindView(R.id.back_arrow_participant)
    AppCompatImageView mBackArrow;
    @BindView(R.id.room_name_text_view)
    AppCompatTextView mRoomNameTextView;
    @BindView(R.id.participant_list)
    RecyclerView mParticipantListView;
    ParticipantListAdapter participantListAdapter;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.no_users_text_view)
    AppCompatTextView mNoCurrentUsersText;

    String mRoomName = "";
    List<UserInfo> participantList = new ArrayList<UserInfo>();
    public FragmentCloseListener fragmentCloseListener;

    public ViewParticipantFragment() {
    }

    public static ViewParticipantFragment newInstance(String roomName) {
        ViewParticipantFragment fragment = new ViewParticipantFragment();
        Bundle args = new Bundle();
        args.putString("roomName", roomName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomName = getArguments().getString("roomName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_participant, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        mRoomNameTextView.setText(mRoomName);

        SplashScreenActivity.dkLiveChat.viewUsers(mRoomName, new CallbackListener() {
            @Override
            public void onSuccess(String s, Object... objects) {
                participantList = (List<UserInfo>) objects[0];
                if (participantList != null && participantList.size() > 0) {
                    setAdapter();
                    if (mNoCurrentUsersText != null)
                        mNoCurrentUsersText.setVisibility(View.GONE);
                } else {
                    if (mNoCurrentUsersText != null)
                        mNoCurrentUsersText.setVisibility(View.VISIBLE);
                }

                if(mProgressBar != null)
                    mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onError(String s, Object... objects) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "error loading participants", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    public void setAdapter() {
        participantListAdapter = new ParticipantListAdapter(participantList);
        if (mParticipantListView != null) {
            mParticipantListView.setAdapter(participantListAdapter);
            mParticipantListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }


    public void setFragmentCloseListener(FragmentCloseListener closeListener) {
        this.fragmentCloseListener = closeListener;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(false);
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (fragmentCloseListener != null)
                        fragmentCloseListener.handleFragmentClose();
                    backArrowClicked();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        participantList.clear();


        if (getActivity().getClass() == HomePageActivity.class) {
            ChatRoomFragment chatRoomFragment = (ChatRoomFragment) (BaseActivity.mCurrentActivity.getSupportFragmentManager()).findFragmentByTag("chatRoom");
            if (chatRoomFragment != null && chatRoomFragment.isVisible()) {

            } else {
                ((HomePageActivity) getActivity()).updateFloatingBtn(true);
            }
        }

    }

    @OnClick(R.id.back_arrow_participant)
    public void backArrowClicked() {
        if (fragmentCloseListener != null)
            fragmentCloseListener.handleFragmentClose();
        getActivity().onBackPressed();
    }
}
