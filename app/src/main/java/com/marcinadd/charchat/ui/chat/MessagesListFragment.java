package com.marcinadd.charchat.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatHelper;
import com.marcinadd.charchat.chat.service.ChatService;
import com.marcinadd.charchat.chat.service.listener.OnMessagesLoadedListener;
import com.marcinadd.charchat.chat.service.listener.OnNewChatMessageArrivedListener;
import com.marcinadd.charchat.image.ImageService;
import com.marcinadd.charchat.image.MyImageLoader;
import com.marcinadd.charchat.image.listener.OnImageUploadedListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessagesListFragment extends Fragment implements MessageInput.InputListener, OnMessagesLoadedListener, OnNewChatMessageArrivedListener, MessageInput.AttachmentsListener, OnImageUploadedListener, MessagesListAdapter.OnLoadMoreListener {

    private String chatId;
    private String anotherUserUid;
    private String currentUserUid;

    private MessagesListAdapter<Message> adapter;
    private FirebaseUser firebaseUser;
    private ListenerRegistration registration;

    private DocumentSnapshot oldestMessageSnapshot;
    private static final String IMAGES = "images";

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
        adapter = new MessagesListAdapter<>(firebaseUser.getUid(), new MyImageLoader(getActivity()));
        messagesList.setAdapter(adapter);

        MessageInput messageInput = mView.findViewById(R.id.input);
        messageInput.setInputListener(this);
        messageInput.setAttachmentsListener(this);

        ChatService.getInstance().getMessagesForSpecificChat(chatId, oldestMessageSnapshot, this);
        registration = ChatService.getInstance().listenToNewMessages(chatId, this);
        adapter.setLoadMoreListener(this);
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
    public void onMessagesLoaded(List<Message> messages, DocumentSnapshot oldestMessageSnapshot) {
        adapter.addToEnd(messages, false);
        this.oldestMessageSnapshot = oldestMessageSnapshot;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        ChatService.getInstance().getMessagesForSpecificChat(chatId, oldestMessageSnapshot, this);
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

    @Override
    public void onAddAttachments() {
        ImagePicker.with(this)
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Config.RC_PICK_IMAGES && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            for (Image image : images) {
                ImageService.getInstance().uploadImageByPath(image.getPath(), IMAGES, this);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageUploaded(String serverPath) {
        Message message = new Message(null, "<image>", new User(currentUserUid, null, null), new Date(), serverPath);
        ChatService.getInstance().sendMessage(message, chatId, anotherUserUid);
    }
}
