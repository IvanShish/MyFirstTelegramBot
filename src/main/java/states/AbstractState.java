package states;

import actions.Notifications;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public abstract class AbstractState {
    Bot bot;

    public abstract void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException, IOException;
}
