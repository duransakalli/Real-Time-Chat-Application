package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    String userName, otherName;
    TextView chatUserName;
    ImageView sendimage;
    ImageView backimage;
    EditText chatUserText;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    RecyclerView chatRecycview;
    MesajAdapter mesajAdapter;
    List<MesajModel> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        define();
        loadMessage();
    }

    public void define()
    {
        list  = new ArrayList<>();
        userName = getIntent().getExtras().getString("userName");
        otherName = getIntent().getExtras().getString("otherName");

        Log.i("Recieved : ", userName+"--"+otherName);
        chatUserName = (TextView)findViewById(R.id.chatUserName);
        backimage = (ImageView)findViewById(R.id.backimage);
        sendimage = (ImageView)findViewById(R.id.sendimage);
        chatUserText = (EditText)findViewById(R.id.chatUserText);

        chatUserName.setText(otherName);

        backimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.putExtra("user", userName);
                startActivity(intent);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();

        sendimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mesaj = chatUserText.getText().toString();
                chatUserText.setText("");
                sendMessage(mesaj);
            }
        });

        chatRecycview = findViewById(R.id.chatRecycview);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(ChatActivity.this,1);
        chatRecycview.setLayoutManager(layoutManager);
        mesajAdapter = new MesajAdapter(ChatActivity.this,list,ChatActivity.this,userName);
        chatRecycview.setAdapter(mesajAdapter);
    }

    public void sendMessage(String text)
    {
       final String key = reference.child("Messages").child(userName).child(otherName).push().getKey();

        final HashMap messageMap = new HashMap<>();
        messageMap.put("text",text);
        messageMap.put("from",userName);
    reference.child("Messages").child(userName).child(otherName).child(key).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful())
            {
                reference.child("Messages").child(otherName).child(userName).child(key).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            }
        }
    });
    }

    public void loadMessage()
    {
        reference.child("Messages").child(userName).child(otherName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MesajModel mesajModel = dataSnapshot.getValue(MesajModel.class);
                list.add(mesajModel);
                mesajAdapter.notifyDataSetChanged();
                chatRecycview.scrollToPosition(list.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
