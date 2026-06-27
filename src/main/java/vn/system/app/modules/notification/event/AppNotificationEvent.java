package vn.system.app.modules.notification.event;

import java.util.List;

public class AppNotificationEvent {
    private final List<String> recipientIds;
    private final String module;
    private final String type;
    private final String content;
    private final String actionLink;

    public AppNotificationEvent(List<String> recipientIds, String module, String type, String content, String actionLink) {
        this.recipientIds = recipientIds;
        this.module = module;
        this.type = type;
        this.content = content;
        this.actionLink = actionLink;
    }

    public List<String> getRecipientIds() {
        return recipientIds;
    }

    public String getModule() {
        return module;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getActionLink() {
        return actionLink;
    }
}
