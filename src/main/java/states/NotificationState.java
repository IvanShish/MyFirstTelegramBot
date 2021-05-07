package states;

import actions.Notifications;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class NotificationState extends AbstractState{
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws URISyntaxException, SQLException {
        long chatId = message.getChatId();
        notifications.setNotificationMode(chatId);
        boolean isOn = notifications.getNotificationMode(chatId);
        bot.sendMsg(message, "Оповещения " + (isOn ? "включены" : "отключены") +  "!\n");
    }
}
