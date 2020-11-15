package com.example.adate.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.adate.R;
import com.example.adate.model.ChatModel;
import com.example.adate.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button buttonSend;
    private EditText editTextMsg;

    private String uid;
    private String chatRoomUid;
    private RecyclerView recyclerView;

    private FirebaseDatabase database;
    private DatabaseReference chatroomsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        database = FirebaseDatabase.getInstance();
        chatroomsRef = database.getReference().child("chatrooms");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 나
        destinationUid = getIntent().getStringExtra("destinationUid"); //상대
        buttonSend = findViewById(R.id.messageActivity_button);
        editTextMsg = findViewById(R.id.messageActivity_editText);
        recyclerView = findViewById(R.id.messageActivity_recyclerview);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);


                if(chatRoomUid == null) {
                    buttonSend.setEnabled(false);
                    chatroomsRef.push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                        }
                    });
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editTextMsg.getText().toString();
                    chatroomsRef.child(chatRoomUid).child("comments").push().setValue(comment);
                }

            }
        });
        checkChatRoom();

    }

    //처음 chatroom이 만들어질 때
    void checkChatRoom() {
        chatroomsRef.orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid)) {
                        chatRoomUid = dataSnapshot.getKey();
                        buttonSend.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new MessageRecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.MessageViewHolder> {

        List<ChatModel.Comment> comments;
        UserModel userModel; //채팅 상대의 사진 및 대화내용 띄우기 위해 필요
        public MessageRecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userModel = snapshot.getValue(UserModel.class); //destination user정보
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        public void getMessageList() {
            chatroomsRef.child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        comments.add(dataSnapshot.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);


            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

            if(comments.get(position).uid.equals(uid)) { // 나
                holder.textViewMessage.setText(comments.get(position).message);
                holder.textViewMessage.setBackgroundResource(R.drawable.rightbubble);
                holder.linearLayoutDestination.setVisibility(recyclerView.INVISIBLE);
                holder.textViewMessage.setTextSize(25);
            } else { // 상대
                Picasso.get().load(userModel.profileImageUrl).into(holder.imageViewProfile);
                holder.textViewName.setText(userModel.userName);
                holder.linearLayoutDestination.setVisibility(View.VISIBLE);
                holder.textViewMessage.setBackgroundResource(R.drawable.leftbubble);
                holder.textViewMessage.setText(comments.get(position).message);
                holder.textViewMessage.setTextSize(25);
            }

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewMessage;
            public TextView textViewName;
            public ImageView imageViewProfile;
            public LinearLayout linearLayoutDestination;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);

                textViewMessage = itemView.findViewById(R.id.messageItem_textView_message);
                textViewName = itemView.findViewById(R.id.messageItem_textview_name);
                imageViewProfile = itemView.findViewById(R.id.messageItem_imageview_profile);
                linearLayoutDestination = itemView.findViewById(R.id.messageItem_linearlayout_destination);

            }
        }
    }

}