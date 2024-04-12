package com.javierjordanluque.healthtrackr.ui;

import androidx.lifecycle.ViewModel;

import com.javierjordanluque.healthtrackr.models.User;

public class SessionViewModel extends ViewModel {
    private User userSession;

    public User getUserSession() {
        return userSession;
    }

    public void setUserSession(User userSession) {
        this.userSession = userSession;
    }
}
