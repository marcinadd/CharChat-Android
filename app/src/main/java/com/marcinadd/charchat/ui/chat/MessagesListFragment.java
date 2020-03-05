package com.marcinadd.charchat.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatHelper;
import com.marcinadd.charchat.chat.service.ChatService;
import com.marcinadd.charchat.chat.service.OnMessagesLoadedListener;
import com.marcinadd.charchat.chat.service.OnNewChatMessageArrivedListener;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;
import java.util.List;

public class MessagesListFragment extends Fragment implements MessageInput.InputListener, OnMessagesLoadedListener, OnNewChatMessageArrivedListener {

    private String chatId;
    private String anotherUserUid;
    private String currentUserUid;

    private MessagesListAdapter<Message> adapter;
    private FirebaseUser firebaseUser;
    private ListenerRegistration registration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            anotherUserUid = MessagesListFragmentArgs.fromBundle(getArguments()).getUserUid();
            chatId = MessagesListFragmentArgs.fromBundle(getArguments()).getChatUid();
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserUid = firebaseUser.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_messages_list, container, false);
        MessagesList messagesList = mView.findViewById(R.id.messagesList);
        adapter = new MessagesListAdapter<>(firebaseUser.getUid(), null);
        messagesList.setAdapter(adapter);

        MessageInput messageInput = mView.findViewById(R.id.input);
        messageInput.setInputListener(this);

        ChatService.getInstance().getMessagesForSpecificChat(chatId, this);
        registration = ChatService.getInstance().listenToNewMessages(chatId, this);

        return mView;
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        Message message = new Message(null, input.toString(), new User(currentUserUid, null, null), new Date());
        adapter.addToStart(message, true);
        ChatService.getInstance().sendMessage(message, chatId, anotherUserUid);
        return true;
    }


    @Override
    public void onMessagesLoaded(List<Message> messages) {
        adapter.addToEnd(messages, false);
    }

    @Override
    public void onNewChatMessageArrived(ChatMessage chatMessage) {
        Message message = ChatHelper.getInstance().createMessageFromChatMessage(chatMessage);
        adapter.addToStart(message, true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        registration.remove();
    }
}
