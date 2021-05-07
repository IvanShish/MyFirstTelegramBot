package states;

import actions.Notifications;
import bot.Bot;
import game.InlineKeyboardMessages;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class GameState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException, IOException {
        String currState = bot.getStateForUser(message.getChatId());
        if (currState == null || !currState.equals("DIFFICULTY")) {
            try {
                bot.execute(InlineKeyboardMessages.inlineKeyBoardMessageForDifficulty(message.getChatId(), bot));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
//        else {
//            try {
//                bot.execute(InlineKeyboardMessages.inlineKeyBoardMessageForGame(message.getChatId(), bot));
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
