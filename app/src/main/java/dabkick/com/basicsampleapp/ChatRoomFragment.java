package dabkick.com.basicsampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import butterknife.Unbinder;
import dabkick.com.basicsampleapp.Adapters.ChatMsgAdapter;
import dabkick.com.basicsampleapp.Model.Room;
import dabkick.com.basicsampleapp.Utils.Utils;

public class ChatRoomFragment extends Fragment {

    private static String mRoomName;

    private Unbinder unbinder;

    @BindView(R.id.edittext)
    EditText editText;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
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


    static ChatMsgAdapter chatMsgAdapter;
    public LiveChatCallbackListener liveChatCallbackListener;
    public UserPresenceCallBackListener userPresenceCallBackListener;
    private boolean isUserAutoSubscribed = true;
    private View view;

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
        unbinder = ButterKnife.bind(this, view);


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

        Log.d("chatRoom", "roomTitle: " + mRoomName);
        mRoomTitle.setText(mRoomName);

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(false);
        }

        chatMsgAdapter = new ChatMsgAdapter();
        recyclerView.setAdapter(chatMsgAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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

        if (SplashScreenActivity.dkLiveChat.isSubscribed(mRoomName)) {
            chatMsgAdapter.addAllMessages(SplashScreenActivity.dkLiveChat.getAllMessageList(mRoomName));
            recyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
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

        SplashScreenActivity.dkLiveChat.getNumberOfUsersLiveNow(mRoomName, new CallbackListener() {
            @Override
            public void onSuccess(String s, Object... objects) {
                if (mUserCount.getVisibility() == View.GONE)
                    mUserCount.setVisibility(View.VISIBLE);
                mUserCount.setText(s);
            }

            @Override
            public void onError(String s, Object... objects) {
                Log.d("onError", "s" + s);
            }
        });

        if (((HomePageActivity)getActivity()).liveChatCallbackListener == null) {
            ((HomePageActivity)getActivity()).liveChatCallbackListener = new LiveChatCallbackListener() {
                @Override
                public void receivedChatMessage(String roomName, MessageInfo message) {
                    BaseActivity.mCurrentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String name = PreferenceHandler.getUserName(BaseActivity.mCurrentActivity);
                            ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.setLatestRoomMsg(roomName, message.getChatMessage());
                            if (roomName.equalsIgnoreCase(mRoomName)) {
                                //happening sometimes
                                if (recyclerView == null) {
                                    recyclerView = view.findViewById(R.id.recycler);
                                    recyclerView.setAdapter(chatMsgAdapter);
                                }
                                chatMsgAdapter.addMessage(message);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.scrollToPosition((chatMsgAdapter.getItemCount() - 1));
                                    }
                                }, 200);
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
                    });
                }
            };
        }

        if (((HomePageActivity)getActivity()).userPresenceCallBackListener == null) {
            ((HomePageActivity)getActivity()).userPresenceCallBackListener = new UserPresenceCallBackListener() {
                @Override
                public void userEntered(String roomName, UserInfo participant) {
                    //process user entry
                    String userEnteredMessage = participant.getName() + " entered the room";
                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setUserId(participant.getUserId());
                    messageInfo.setUserName(participant.getName());
                    messageInfo.setChatMessage(userEnteredMessage);
                    messageInfo.setSystemMessage(true);
                    chatMsgAdapter.addMessage(messageInfo);
                }


                @Override
                public void userExited(String roomName, UserInfo participant) {
                    //process user exit
                    String userEnteredMessage = participant.getName() + " exited the room";
                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setUserId(participant.getUserId());
                    messageInfo.setUserName(participant.getName());
                    messageInfo.setChatMessage(userEnteredMessage);
                    messageInfo.setSystemMessage(true);
                    chatMsgAdapter.addMessage(messageInfo);
                }


                @Override
                public void userDataUpdated(String roomName, UserInfo participant) {
                    //process user info change
                }
            };
        }


        if (!SplashScreenActivity.dkLiveChat.isSubscribed(mRoomName)) {
            mProgressBar.setVisibility(View.VISIBLE);
            SplashScreenActivity.dkLiveChat.subscribe(mRoomName, liveChatCallbackListener, userPresenceCallBackListener, new CallbackListener() {
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
                                        if (recyclerView != null)
                                            recyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);

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
                                                      recyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
                                                  } else {
                                                      Toast.makeText(BaseActivity.mCurrentActivity, "Please enter message", Toast.LENGTH_LONG).show();
                                                  }
                                              }
                                          });
                                      }
                                  }
        );

        return view;
    }

    public void initLiveChatCallbackListener() {
        liveChatCallbackListener = new LiveChatCallbackListener() {
            @Override
            public void receivedChatMessage(String roomName, MessageInfo message) {
                BaseActivity.mCurrentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String name = PreferenceHandler.getUserName(BaseActivity.mCurrentActivity);
                        ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.setLatestRoomMsg(roomName, message.getChatMessage());
                        if (roomName.equalsIgnoreCase(mRoomName)) {
                            //happening sometimes
                            if (recyclerView == null) {
                                recyclerView = view.findViewById(R.id.recycler);
                                recyclerView.setAdapter(chatMsgAdapter);
                            }
                            chatMsgAdapter.addMessage(message);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.scrollToPosition((chatMsgAdapter.getItemCount() - 1));
                                }
                            }, 200);
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
                });
            }
        };
    }

    public void initUserPresenceCallbackListener() {
        userPresenceCallBackListener = new UserPresenceCallBackListener() {
            @Override
            public void userEntered(String roomName, UserInfo participant) {
                //process user entry
                String userEnteredMessage = participant.getName() + " entered the room";
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setUserId(participant.getUserId());
                messageInfo.setUserName(participant.getName());
                messageInfo.setChatMessage(userEnteredMessage);
                messageInfo.setSystemMessage(true);
                chatMsgAdapter.addMessage(messageInfo);
            }


            @Override
            public void userExited(String roomName, UserInfo participant) {
                //process user exit
                String userEnteredMessage = participant.getName() + " exited the room";
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setUserId(participant.getUserId());
                messageInfo.setUserName(participant.getName());
                messageInfo.setChatMessage(userEnteredMessage);
                messageInfo.setSystemMessage(true);
                chatMsgAdapter.addMessage(messageInfo);
            }


            @Override
            public void userDataUpdated(String roomName, UserInfo participant) {
                //process user info change
            }
        };
    }

    @OnClick(R.id.down_arrow)
    public void scrollToLatestMsg() {
        Utils.hideKeyboard(getActivity());
        recyclerView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
    }

    @OnClick(R.id.back_arrow)
    public void backBtnClicked() {
        Utils.hideKeyboard(getActivity());
        getActivity().onBackPressed();
        ((HomePageActivity) BaseActivity.mCurrentActivity).mRoomListAdapter.notifyDataSetChanged();
        SplashScreenActivity.dkLiveChat.leaveSession(mRoomName, new CallbackListener() {
            @Override
            public void onSuccess(String s, Object... objects) {

            }

            @Override
            public void onError(String s, Object... objects) {

            }
        });
    }

    public void sendMessage(String roomName, final String message) {
        if (BaseActivity.mCurrentActivity.getClass() == HomePageActivity.class) {
            DKLiveChat dkLiveChat = SplashScreenActivity.dkLiveChat;
            if (dkLiveChat == null)
                return;
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setChatMessage(message);

            messageInfo.setUserId(dkLiveChat.getUserId());
            dkLiveChat.chatEventListener.sendMessage(roomName, messageInfo, new CallbackListener() {
                @Override
                public void onSuccess(String msg, Object... obj) {
                    Log.d("ChatRoomActivity", "onSuccess chat msg");
                    editText.setText("");
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
        mRoomName = "";
        Snackbar.make(view, "You are auto-subscribed to this room", Snackbar.LENGTH_LONG).show();

        if (getActivity().getClass() == HomePageActivity.class) {
            ((HomePageActivity) getActivity()).updateFloatingBtn(true);
        }

        if (chatMsgAdapter != null) {
            chatMsgAdapter.clearMsgs();
            chatMsgAdapter = null;
        }
        unbinder.unbind();
    }
}
