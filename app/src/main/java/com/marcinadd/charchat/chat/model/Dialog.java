package com.marcinadd.charchat.chat.model;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.List;

public class Dialog implements IDialog {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private List<User> users;
    private IMessage lastMessage;
    private int unreadCount;


    public Dialog(String id, String dialogPhoto, String dialogName, List<User> users, IMessage lastMessage, int unreadCount) {
        this.id = id;
        this.dialogPhoto = dialogPhoto;
        this.dialogName = dialogName;
        this.users = users;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = message;
    }


    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    @Override
    public String toString() {
        return "Dialog{" +
                "id='" + id + '\'' +
                ", dialogPhoto='" + dialogPhoto + '\'' +
                ", dialogName='" + dialogName + '\'' +
                ", users=" + users +
                ", lastMessage=" + lastMessage +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
