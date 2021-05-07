package states;

import actions.Notifications;
import actions.Reminder;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class RemindState extends AbstractState{
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws URISyntaxException, SQLException {
        Reminder.remind(message, bot);
    }
}
