package com.marcinadd.charchat.people.service;

import com.marcinadd.charchat.chat.model.User;

import java.util.List;

public interface OnPeopleSearchLoadedListener {
    void onPeopleLoadedListener(List<User> users);
}
