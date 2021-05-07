package states;

import actions.Notifications;
import actions.Weather;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class LocationState extends AbstractState{
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException {
        String city = command;
        long chatId = message.getChatId();
        city = command.replaceAll("/location", "");
        if (city.length() == 0) {
            bot.sendMsg(message, "Введите свой город после /location через пробел");
        }
        else {
            city = city.substring(1, city.length());
            Weather.setCity(city, chatId);
            String currCity = Weather.getCity(chatId);
            bot.sendMsg(message, "Теперь город проживания - " + currCity + "!\n");
        }
    }
}
