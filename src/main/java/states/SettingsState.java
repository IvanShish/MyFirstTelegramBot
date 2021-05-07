package states;

import actions.Notifications;
import actions.Weather;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class SettingsState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException {
        long chatId = message.getChatId();
        boolean isOn = notifications.getNotificationMode(chatId);
        String city = Weather.getCity(chatId);
        bot.sendMsg(message, "Напишите /notification, чтобы включить или отключить оповещения (на данный момент " +
                "оповещения " + (isOn ? "включены" : "отключены") +  ")\n" +
                "Напишите /location city, чтобы задать город проживания (на данный момент город проживания - " +
                city + ")\n");
    }
}
