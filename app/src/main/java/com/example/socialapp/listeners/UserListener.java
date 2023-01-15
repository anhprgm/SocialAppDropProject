package com.example.socialapp.listeners;

import com.example.socialapp.models.User;

public interface UserListener {
    void onUserClicked(User user);
    void initiateVideoCall(User user);
    void initiateAudioCall(User user);
}
