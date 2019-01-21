package dabkick.com.basicsampleapp.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dabkick.engine.Public.MessageInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dabkick.com.basicsampleapp.R;
import dabkick.com.basicsampleapp.Utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.MessageHolder> {

    private List<MessageInfo> messageInfoList = new ArrayList<>();

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item_msg, viewGroup, false);

        return new MessageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder messageHolder, int i) {
        String name = messageInfoList.get(i).getUserName();
        //dont diplay user name if not set
        Timber.d("Value of user id is " + messageInfoList.get(i).getUserId());
        if (name != null && !name.trim().isEmpty())
            messageHolder.name.setText(messageInfoList.get(i).getUserName());
        else
            messageHolder.name.setText("anonymous"); //if user name is not set, make it gone

        messageHolder.msg.setText(messageInfoList.get(i).getChatMessage());

        //for profile pic
//        String profileImgUrl = messageInfoList.get(i).getImg();
//        Picasso.get().load("").placeholder(R.drawable.avatar_img).error(R.drawable.avatar_img).into(messageHolder.profileImg);

        //for time stamp
        if(!messageInfoList.get(i).isSystemMessage()) {
            try {
                long currentMsgTime = messageInfoList.get(i).getMessageTime();
                long prevMsgTime = 0L;
                if (i > 0) {
                    prevMsgTime = (messageInfoList.get(i - 1)).getMessageTime();
                }
                messageHolder.timeStamp.setText(Utils.millisToTime(currentMsgTime));
                setTimeTextVisibility(currentMsgTime, prevMsgTime, messageHolder.dateTextLayout);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.messageInfoList.size();
    }

    public MessageInfo getItem(int pos) {
        if (messageInfoList.size() > pos)
            return messageInfoList.get(pos);

        return null;
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView msg, name, timeStamp;
        AppCompatTextView dateTextLayout;
        CircleImageView profileImg;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.message_text_view);
            name = itemView.findViewById(R.id.user_name_text_view);
            profileImg = itemView.findViewById(R.id.profile_pic_img);
            timeStamp = itemView.findViewById(R.id.time_stamp_text_view);
            dateTextLayout = itemView.findViewById(R.id.date_time_stamp_layout);
        }
    }

    public void addMessage(MessageInfo messageInfo) {
        messageInfoList.add(messageInfo);
        notifyItemInserted(messageInfoList.size() - 1);
    }

    public void addAllMessages(List<MessageInfo> messageList) {
        messageInfoList.addAll(messageList);
        notifyDataSetChanged();
    }

    public void clearMsgs() {
        this.messageInfoList.clear();
    }

    private void setTimeTextVisibility(long currentMsgDate, long prevMsgDate, TextView timeText) {
        if (prevMsgDate == 0) {
            timeText.setVisibility(View.VISIBLE);
            timeText.setText(Utils.dateToString(currentMsgDate));
        } else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(currentMsgDate);
            cal2.setTimeInMillis(prevMsgDate);

            boolean sameDate = (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) &&
                    (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                    (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE));

            if (sameDate) {
                timeText.setVisibility(View.GONE);
            } else {
                timeText.setVisibility(View.VISIBLE);
                timeText.setText(Utils.dateToString(currentMsgDate));
            }
        }
    }
}

