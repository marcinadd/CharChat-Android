package com.marcinadd.charchat.user.avatar;

public class AvatarService {
    private static final AvatarService ourInstance = new AvatarService();

    private AvatarService() {
    }

    public static AvatarService getInstance() {
        return ourInstance;
    }


}
