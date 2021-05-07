package states;

import actions.Notifications;
import bot.Bot;
import database.Database;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StartOrHelpState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException {
        Connection connection = Database.getConnection();
        String helpMessage = "Вас приветствует " + bot.getBotUsername() + "!\n" +
                "Доступны следующие команды:\n" +
                "/settings - настройки бота\n" +
                "/weather city - погода для города, название которого Вы ввели (если в настройках задан город, то его писать не обязательно)\n" +
                "/time - текущее время\n" +
                "/couples - пары на сегодня\n" +
                "/game - игра правда или ложь, состоящая из 5 вопросов\n" +
                "/translate - переводит текст. Если текст введен на русском, то переводит на английский, а если на английском, то на русский.\n" +
                "/remind day.month hour:minutes text - создается напоминание (Пример: /remind 02.04 23:00 проснуться). Если просто написать" +
                "/remind, то выведутся напоминания, которые на данный момент установлены\n" +
                "Если что-то не работает, то напишите /start\n";
        bot.sendMsg(message, helpMessage);
        long chatId = message.getChatId();

        if (command.contains("/start")) {

            // INSERT DATA
            String inserting = "INSERT INTO public.users(chat_id, name, location, mode) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = connection.prepareStatement(inserting);
            try {
                pst.setInt(1, (int) chatId);
                pst.setString(2, message.getFrom().getFirstName());
                pst.setString(3, "Moscow");
                pst.setInt(4, 0);
                pst.executeUpdate();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }

        }
    }
}
