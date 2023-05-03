package com.gumingnc.mars_support;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        var manager = NotificationGroupManager.getInstance();
        var group = manager.getNotificationGroup("Custom Notification Group");
        var notification = group.createNotification("Hello world", NotificationType.INFORMATION);
        notification.notify(e.getProject());
    }
}
