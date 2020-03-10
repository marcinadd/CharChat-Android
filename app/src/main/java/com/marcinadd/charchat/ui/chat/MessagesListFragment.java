package com.marcinadd.charchat.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marcinadd.charchat.R;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatHelper;
import com.marcinadd.charchat.chat.service.ChatService;
import com.marcinadd.charchat.chat.service.listener.OnMessagesLoadedListener;
import com.marcinadd.charchat.chat.service.listener.OnNewChatMessageArrivedListener;
import com.marcinadd.charchat.image.ImageService;
import com.marcinadd.charchat.image.listener.OnImageUploadedListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessagesListFragment extends Fragment implements MessageInput.InputListener, OnMessagesLoadedListener, OnNewChatMessageArrivedListener, MessageInput.AttachmentsListener, OnImageUploadedListener {

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
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        adapter = new MessagesListAdapter<>(firebaseUser.getUid(), new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                StorageReference storageReference = firebaseStorage.getReference().child(url);
                Glide.with(MessagesListFragment.this)
                        .load(storageReference)
                        .into(imageView);
            }
        });
        messagesList.setAdapter(adapter);

        MessageInput messageInput = mView.findViewById(R.id.input);
        messageInput.setInputListener(this);
        messageInput.setAttachmentsListener(this);

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
                ImageService.getInstance().uploadImageByPath(image.getPath(), this);
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
