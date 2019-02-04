package dabkick.com.basicsampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dabkick.engine.Public.CallbackListener;
import com.dabkick.engine.Public.DKLiveChat;
import com.dabkick.engine.Public.LiveChatCallbackListener;
import com.dabkick.engine.Public.MessageInfo;
import com.dabkick.engine.Public.UserInfo;
import com.dabkick.engine.Public.UserPresenceCallBackListener;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dabkick.com.basicsampleapp.Adapters.ChatMsgAdapter;
import dabkick.com.basicsampleapp.Adapters.FragmentCloseListener;
import dabkick.com.basicsampleapp.Model.Room;
import dabkick.com.basicsampleapp.Utils.Utils;

public class ChatRoomFragment extends Fragment {

    private static String mRoomName;

    @BindView(R.id.edittext)
    EditText editText;
    static RecyclerView chatListRecyclerView;
    @BindView(R.id.button)
    AppCompatImageView button;
    @BindView(R.id.back_arrow)
    AppCompatImageView backBtnImg;
    @BindView(R.id.room_name_text_view)
    AppCompatTextView mRoomTitle;
    @BindView(R.id.over_flow_icon)
    AppCompatImageView mOverFlowIcon;
    @BindView(R.id.view_participants_frag_container)
    FrameLayout mViewParticipantsFragContainer;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.user_count)
    AppCompatTextView mUserCount;
    @BindView(R.id.line)
    View line;
    @BindView(R.id.layout)
    LinearLayout editBoxLayout;
    @BindView(R.id.down_arrow)
    AppCompatImageView mNewMsgArrow;
    @BindView(R.id.date_time_stamp)
    AppCompatTextView mDateTimeStamp;


    static ChatMsgAdapter chatMsgAdapter;
    public LiveChatCallbackListener liveChatCallbackListener;
    public UserPresenceCallBackListener userPresenceCallBackListener;
    private boolean isUserAutoSubscribed = true;
    private View view;
    public FragmentCloseListener fragmentCloseListener;


    Object object = new Object();

    public ChatRoomFragment() {
    }

    public static ChatRoomFragment newInstance(String roomName, boolean editMode) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("roomName", roomName);
        args.putBoolean("editMode", editMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_chat_room, container, false);
        ButterKnife.bind(this, view);
        isUserAutoSubscribed = true;

        if (getArguments() != null) {
            mRoomName = getArguments().getString("roomName");

            if (getArguments().getBoolean("editMode")) {
                line.setVisibility(View.VISIBLE);
                editBoxLayout.setVisibility(View.VISIBLE);
            } else {
                line.setVisibility(View.GONE);
                editBoxLayout.setVisibility(View.GONE);
            }
        }

        if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(false);
        }

        mRoomTitle.setText(mRoomName);

        chatListRecyclerView = view.findViewById(R.id.chat_list);
        chatMsgAdapter = new ChatMsgAdapter();
        chatListRecyclerView.setAdapter(chatMsgAdapter);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //clear unread msg list
        if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
            Room room = ((HomePageActivity) getActivity()).mRoomListAdapter.getRoomItem(mRoomName);
            if (room != null) {
                room.clearUnreadMsgList();
                ((HomePageActivity) getActivity()).mRoomListAdapter.notifyDataSetChanged();
            }
        }

        try {
            if (getActivity().getClass() == HomePageActivity.class) {


                //STEP 5: Joining a room: Enter
                SplashScreenActivity.dkLiveChat.joinSession(mRoomName, createUserInfo(), new CallbackListener() {
                    @Override
                    public void onSuccess(String msg, Object... obj) {
                        //call subscribe here
                    }

                    @Override
                    public void onError(String msg, Object... obj) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (HomePageActivity.isNewRoomCreated) {
            HomePageActivity.isNewRoomCreated = false;
            SplashScreenActivity.dkLiveChat.chatRoomListener.createNewRoom(new CallbackListener() {
                @Override
                public void onSuccess(String s, Object... objects) {

                }

                @Override
                public void onError(String s, Object... objects) {

                }
            });
        }


        //STEP 7: Tracking users in the room: Entry, Exit, User Information Updates
        mUserCount.setText(String.valueOf(SplashScreenActivity.dkLiveChat.getNumberOfUsersLiveNow(mRoomName, new CallbackListener() {
            @Override
            public void onSuccess(String s, Object... objects) {
            }

            @Override
            public void onError(String s, Object... objects) {
                Log.d("onError", "s" + s);
            }
        })));


        if (((HomePageActivity) getActivity()).liveChatCallbackListener == null) {
            ((HomePageActivity) getActivity()).liveChatCallbackListener = new LiveChatCallbackListener() {
                @Override

                //STEP 6: Sending and Receiving Messages
                public void receivedChatMessage(String roomName, MessageInfo message) {
                    BaseActivity.mCurrentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (object) {
                                String name = PreferenceHandler.getUserName(BaseActivity.mCurrentActivity);
                                ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.setLatestRoomMsg(roomName, message.getChatMessage()/*, timestamp to be passed here*/);
                                if (roomName.equalsIgnoreCase(mRoomName)) {
                                    chatMsgAdapter.addMessage(message);
                                   /* if ((chatMsgAdapter.getItemCount() - ((LinearLayoutManager) chatListRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition()) > 2) {
                                        mNewMsgArrow.setVisibility(View.VISIBLE);
                                    } else {*/
                                    scrollToLatestMsg();
//                                    }
                                } else if (!message.getUserName().equalsIgnoreCase(name)) {
                                    //i am not in the same room as the msg received and am not the sender of the msg. So add it as unread msg
                                    if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
                                        Room room = ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.getRoomItem(roomName);
                                        if (room != null) {
                                            room.addUnreadMsg(message);
                                            ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.updateRoomUponNewMsg(room);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            };
        }

        if (((HomePageActivity) getActivity()).userPresenceCallBackListener == null) {
            ((HomePageActivity) getActivity()).userPresenceCallBackListener = new UserPresenceCallBackListener() {
                @Override

                //STEP 7: Tracking users in the room: Entry, Exit, User Information Updates

                public void userEntered(String roomName, UserInfo participant) {
                    //process user entry
                    String userEnteredMessage = participant.getName() + " entered the room";
                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setUserId(participant.getUserId());
                    messageInfo.setUserName(participant.getName());
                    messageInfo.setChatMessage(userEnteredMessage);
                    messageInfo.setSystemMessage(true);
                    if (participant.getProfilePicUrl() != null)
                        messageInfo.setProfilePicUrl(participant.getProfilePicUrl());
                    if (chatMsgAdapter != null) {
                        chatMsgAdapter.addMessage(messageInfo);
                        scrollToLatestMsg();
                    }
                }


                @Override

                //STEP 7: Tracking users in the room: Entry, Exit, User Information Updates
                public void userExited(String roomName, UserInfo participant) {
                    //process user exit
                    String userEnteredMessage = participant.getName() + " exited the room";
                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setUserId(participant.getUserId());
                    messageInfo.setUserName(participant.getName());
                    messageInfo.setChatMessage(userEnteredMessage);
                    messageInfo.setSystemMessage(true);
                    if (participant.getProfilePicUrl() != null)
                        messageInfo.setProfilePicUrl(participant.getProfilePicUrl());
                    if (chatMsgAdapter != null) {
                        chatMsgAdapter.addMessage(messageInfo);
                        scrollToLatestMsg();
                    }
                }


                @Override

                //STEP 7: Tracking users in the room: Entry, Exit, User Information Updates
                public void userDataUpdated(String roomName, UserInfo participant) {
                    //process user info change
                }
            };
        }


        if (!SplashScreenActivity.dkLiveChat.isSubscribed(mRoomName)) {
            mProgressBar.setVisibility(View.VISIBLE);

            //STEP 5: Joining a room: subscribe

            SplashScreenActivity.dkLiveChat.subscribe(mRoomName, ((HomePageActivity) getActivity()).liveChatCallbackListener, ((HomePageActivity) getActivity()).userPresenceCallBackListener, new CallbackListener() {
                @Override
                public void onSuccess(String msg, Object... obj) {
                    try {
                        BaseActivity.mCurrentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mProgressBar != null)
                                            mProgressBar.setVisibility(View.GONE);
                                        if (chatMsgAdapter != null)
                                            chatMsgAdapter.addAllMessages(SplashScreenActivity.dkLiveChat.chatEventListener.getChatMessages(mRoomName));

                                        if (chatListRecyclerView != null && chatMsgAdapter != null)
                                            chatListRecyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);

                                    }
                                }, 3000);

                            }
                        });
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onError(String msg, Object... obj) {
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }

        if (SplashScreenActivity.dkLiveChat.isSubscribed(mRoomName)) {
            if (SplashScreenActivity.dkLiveChat.getAllMessageList(mRoomName).size() == 0) {
                chatMsgAdapter.addAllMessages(SplashScreenActivity.dkLiveChat.chatEventListener.getChatMessages(mRoomName));
            } else {
                chatMsgAdapter.addAllMessages(SplashScreenActivity.dkLiveChat.getAllMessageList(mRoomName));
                if (((HomePageActivity) getActivity()).latestMessageList.size() > 0) {
                    chatMsgAdapter.addAllMessages(((HomePageActivity) getActivity()).latestMessageList);
                    ((HomePageActivity) getActivity()).latestMessageList.clear();
                }
            }
            chatListRecyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
        }

        chatListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

           /*     int pos = ((LinearLayoutManager) chatListRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                if (pos > 0 && pos < chatMsgAdapter.getItemCount()) {
                    MessageInfo messageInfo = chatMsgAdapter.getItem(pos);
                    Date msgDate = Utils.millisToDate(messageInfo.getMessageTime());
                    Date currentDate = Utils.millisToDate(System.currentTimeMillis());
                    if (msgDate.before(currentDate)) {
                        DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
                        mDateTimeStamp.setText((dateFormat.format(msgDate)).toString());
                        mDateTimeStamp.setVisibility(View.VISIBLE);
                    } else if (msgDate.compareTo(currentDate) == 0) {
                        //same day
                        mDateTimeStamp.setText("Today");
                        mDateTimeStamp.setVisibility(View.VISIBLE);
                    }
                }*/
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if ((chatMsgAdapter.getItemCount() - ((LinearLayoutManager) chatListRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition()) < 2) {
                    mNewMsgArrow.setVisibility(View.GONE);
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          getActivity().runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                                                      String message = editText.getText().toString().replaceAll("^\\s+|\\s+$", "");

                                                      sendMessage(mRoomName, message);
                                                      Utils.hideKeyboard(getActivity());
                                                      chatListRecyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
                                                  } else {
                                                      Toast.makeText(BaseActivity.mCurrentActivity, "Please enter message", Toast.LENGTH_LONG).show();
                                                  }
                                              }
                                          });
                                      }
                                  }
        );

        fragmentCloseListener = new FragmentCloseListener() {
            @Override
            public void handleFragmentClose() {
                if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
                    ((HomePageActivity) getActivity()).updateFloatingBtn(false);
                }

                if (getView() != null) {
                    getView().setFocusableInTouchMode(true);
                    getView().requestFocus();
                    getView().setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {

                            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                                showAlertDialogWhileExiting();
                                return true;
                            }
                            return false;
                        }
                    });

                }
            }
        };


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() == null) {
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    //backBtnClicked();
                    showAlertDialogWhileExiting();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.down_arrow)
    public void scrollToLatestMsg() {
        BaseActivity.mCurrentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CHatRoomFrag", "scrollToLatest" + chatMsgAdapter.getItemCount());
                Utils.hideKeyboard(BaseActivity.mCurrentActivity);
                chatListRecyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
            }
        });
    }

    @OnClick(R.id.back_arrow)
    public void backBtnClicked() {
        Utils.hideKeyboard(getActivity());
        ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.notifyDataSetChanged();
        if (SplashScreenActivity.dkLiveChat.isSubscribed(mRoomName)) {
            showAlertDialogWhileExiting();
            return;
        } else {

            //STEP 8: Exiting a room: Leave
            SplashScreenActivity.dkLiveChat.leaveSession(mRoomName, new CallbackListener() {
                @Override
                public void onSuccess(String s, Object... objects) {

                }

                @Override
                public void onError(String s, Object... objects) {

                }
            });
            getActivity().onBackPressed();
        }


    }

    public void showAlertDialogWhileExiting() {
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.mCurrentActivity, R.style.ExitChatRoomDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.subscribe_layout, null);
        builder.setView(dialogView);
        android.support.v7.app.AlertDialog alertDialog = builder.create();

        Button subscribe = dialogView.findViewById(R.id.subscribe);
        Button unSubscribe = dialogView.findViewById(R.id.unsubscribe);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SplashScreenActivity.dkLiveChat.leaveSession(mRoomName, new CallbackListener() {
                    @Override
                    public void onSuccess(String s, Object... objects) {

                    }

                    @Override
                    public void onError(String s, Object... objects) {

                    }
                });
                getActivity().onBackPressed();
                alertDialog.dismiss();
                //do nothing else as the user will remain subscribed
            }
        });

        unSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //STEP 8: Exiting a room: Leave
                SplashScreenActivity.dkLiveChat.leaveSession(mRoomName, new CallbackListener() {
                    @Override
                    public void onSuccess(String s, Object... objects) {

                    }

                    @Override
                    public void onError(String s, Object... objects) {

                    }
                });

                //STEP 8: Exiting a room: unsubscribe
                SplashScreenActivity.dkLiveChat
                        .unSubscribe(mRoomName, liveChatCallbackListener, userPresenceCallBackListener, new CallbackListener() {
                            @Override
                            public void onSuccess(String msg, Object... obj) {
                                if (((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter != null) {
                                    Room room = ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.getRoomItem(mRoomName);
                                    if (room != null)
                                        ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.updateRoomUponUnsubscribe(room);
                                }
                            }

                            @Override
                            public void onError(String msg, Object... obj) {
                            }
                        });

                getActivity().onBackPressed();
                alertDialog.dismiss();
            }
        });
        builder.setCancelable(true);
        alertDialog.show();
    }

    public void sendMessage(String roomName, final String message) {
        if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
            DKLiveChat dkLiveChat = SplashScreenActivity.dkLiveChat;
            if (dkLiveChat == null)
                return;

            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setChatMessage(message);
            if (PreferenceHandler.getUserProfileImg(BaseActivity.mCurrentActivity) != null) {
                messageInfo.setProfilePicUrl(PreferenceHandler.getUserProfileImg(BaseActivity.mCurrentActivity));
            }
            //STEP 6: Sending and Receiving Messages
            dkLiveChat.chatEventListener.sendMessage(roomName, messageInfo, new CallbackListener() {
                @Override
                public void onSuccess(String msg, Object... obj) {
                    Log.d("ChatRoomActivity", "onSuccess chat msg");
                    editText.setText("");
                    if (getView() != null) {
                        getView().setFocusableInTouchMode(true);
                        getView().requestFocus();
                        getView().setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {

                                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                                    showAlertDialogWhileExiting();
                                    return true;
                                }
                                return false;
                            }
                        });

                    }
                }

                @Override
                public void onError(String msg, Object... obj) {
                    Log.d("ChatRoomActivity", "onError chat msg");
                }
            });

        }
    }

    @OnClick(R.id.over_flow_icon)
    public void onClickOverflowMenu() {
        PopupMenu popup = new PopupMenu(getActivity(), mOverFlowIcon);
        popup.getMenuInflater().inflate(R.menu.chat_tool_bar_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.view_participants:
                        ViewParticipantFragment participantFragment = ViewParticipantFragment.newInstance(mRoomName);
                        participantFragment.setFragmentCloseListener(fragmentCloseListener);
                        android.support.v4.app.FragmentTransaction transaction = ((AppCompatActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.view_participants_frag_container, participantFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;

                    case R.id.unsubscribe:
                        SplashScreenActivity.dkLiveChat
                                .unSubscribe(mRoomName, liveChatCallbackListener, userPresenceCallBackListener, new CallbackListener() {
                                    @Override
                                    public void onSuccess(String msg, Object... obj) {
                                        //move room to last pos
                                        backBtnClicked();
                                        if (((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter != null) {
                                            Room room = ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.getRoomItem(mRoomName);
                                            if (room != null)
                                                ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.updateRoomUponUnsubscribe(room);
                                        }
                                    }

                                    @Override
                                    public void onError(String msg, Object... obj) {

                                    }
                                });


                        break;
                }

                return true;
            }
        });
        popup.show();//showing popup menu
    }

    private UserInfo createUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setAppSpecificUserID(UUID.randomUUID().toString());
        userInfo.setName(PreferenceHandler.getUserName(getActivity()));
        return userInfo;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Room room = ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.getRoomItem(mRoomName);
        if (room != null) {
            ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.moveRoomToTop(room);
        }
        mRoomName = "";

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(true);
        }

        if (chatMsgAdapter != null) {
            chatMsgAdapter.clearMsgs();
            chatMsgAdapter = null;
        }
    }

    public String getRoomName() {
        return mRoomName;
    }

}
