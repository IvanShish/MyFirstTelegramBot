package states;

import actions.Notifications;
import actions.Weather;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class WeatherState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) {
        long chatId = message.getChatId();
        try {
            bot.sendMsg(message, Weather.getWeather(message.getText(), chatId));
        } catch (IOException | URISyntaxException | SQLException e) {
            bot.sendMsg(message, "Город не найден!");
        }
    }
}
