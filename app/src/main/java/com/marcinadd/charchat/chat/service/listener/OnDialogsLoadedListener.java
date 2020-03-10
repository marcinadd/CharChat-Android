package com.marcinadd.charchat.chat.service.listener;

import com.marcinadd.charchat.chat.model.Dialog;

import java.util.List;

public interface OnDialogsLoadedListener {
    void onDialogsLoaded(List<Dialog> dialogs);
}
