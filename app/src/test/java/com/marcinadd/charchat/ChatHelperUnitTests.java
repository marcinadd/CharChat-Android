package com.marcinadd.charchat;

import com.google.firebase.Timestamp;
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatHelper;

import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class ChatHelperUnitTests {
    private String id = "hkljhl";
    private String idA = "abcdef";
    private String usernameA = "user";
    private String idB = "ml053890nd";
    private String usernameB = "root";
    private String senderUid = "i5ubr5j34iigfda";
    private String text = "sample text";
    private String imagePath = "/images/sample";


    @Test
    public void whenCreateMessageFromMap_shouldReturnMessage() {
        Date date = new Date();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(FieldNames.SENDER_UID.toString(), senderUid);
        data.put(FieldNames.CREATED_AT.toString(), new Timestamp(date));
        data.put(FieldNames.TEXT.toString(), text);
        data.put(FieldNames.IMAGE_PATH.toString(), imagePath);
        Message message = ChatHelper.getInstance().createMessageFromMap(data, id);

        assertEquals(message.getId(), id);
        assertEquals(message.getText(), text);
        assertEquals(message.getImageUrl(), imagePath);
        assertEquals(message.getCreatedAt(), date);
        assertEquals(message.getUser().getId(), senderUid);
    }

    @Test
    public void whenCreateUserFromMap_shouldReturnUser() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(FieldNames.ID.toString(), id);
        data.put(FieldNames.USERNAME.toString(), usernameA);
        ChatHelper.getInstance().createUserFromMap(data);
    }

    @Test
    public void whenCreateChatFromMap_shouldReturnChat() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> creator = new LinkedHashMap<>();
        creator.put(FieldNames.ID.toString(), idA);
        creator.put(FieldNames.USERNAME.toString(), usernameA);

        Map<String, Object> receiver = new LinkedHashMap<>();
        receiver.put(FieldNames.ID.toString(), idB);
        receiver.put(FieldNames.USERNAME.toString(), usernameB);

        data.put(FieldNames.CREATOR.toString(), creator);
        data.put(FieldNames.RECEIVER.toString(), receiver);

        data.put(FieldNames.CREATOR_HIDDEN.toString(), true);
        Date date = new Date();
        data.put(FieldNames.CREATED_AT.toString(), new Timestamp(date));

        Chat chat = ChatHelper.getInstance().createChatFromMap(data, id);

        assertEquals(chat.getId(), id);
        assertEquals(chat.getCreatedAt(), date);
        assertEquals(chat.getCreator().getId(), idA);
        assertEquals(chat.getCreator().getName(), usernameA);
        assertEquals(chat.getReceiver().getId(), idB);
        assertEquals(chat.getReceiver().getName(), usernameB);
        assertTrue(chat.isCreatorHidden());
    }

    @Test
    public void whenCreateDialogAsUserA_shouldReturnDialog() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> creator = new LinkedHashMap<>();
        creator.put(FieldNames.ID.toString(), idA);
        creator.put(FieldNames.NAME.toString(), usernameA);

        Map<String, Object> receiver = new LinkedHashMap<>();
        receiver.put(FieldNames.ID.toString(), idB);
        receiver.put(FieldNames.NAME.toString(), usernameB);

        data.put(FieldNames.CREATOR.toString(), creator);
        data.put(FieldNames.RECEIVER.toString(), receiver);

        Message message = new Message(id, text, new User(idA, usernameA, null), new Date());
        Dialog dialog = ChatHelper.getInstance().createDialog(data, id, idA, message);

        assertEquals(dialog.getId(), id);
        assertEquals(dialog.getLastMessage(), message);
        assertEquals(dialog.getDialogName(), usernameB);
    }

    @Test
    public void whenCreateDialogAsUserB_shouldReturnDialog() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> creator = new LinkedHashMap<>();
        creator.put(FieldNames.ID.toString(), idA);
        creator.put(FieldNames.NAME.toString(), usernameA);

        Map<String, Object> receiver = new LinkedHashMap<>();
        receiver.put(FieldNames.ID.toString(), idB);
        receiver.put(FieldNames.NAME.toString(), usernameB);

        data.put(FieldNames.CREATOR.toString(), creator);
        data.put(FieldNames.RECEIVER.toString(), receiver);

        Message message = new Message(id, text, new User(idA, usernameA, null), new Date());
        Dialog dialog = ChatHelper.getInstance().createDialog(data, id, idB, message);

        assertEquals(dialog.getId(), id);
        assertEquals(dialog.getLastMessage(), message);
        assertEquals(dialog.getDialogName(), usernameA);
    }


}
