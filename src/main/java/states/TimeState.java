package states;

import actions.Notifications;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeState extends AbstractState{
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws IOException, SQLException, URISyntaxException {
        SimpleDateFormat moscowTime = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss z");
        moscowTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        bot.sendMsg(message, "Текущая дата: " + moscowTime.format(new Date()));
    }
}
