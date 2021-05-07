package bot;

import actions.Notifications;
import com.vdurmont.emoji.EmojiParser;
import database.Database;
import game.TrueOrFalseGame;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import states.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Bot extends TelegramLongPollingBot {
    private static Notifications notifications = new Notifications();

    public Bot() throws TelegramApiException {
        Notifications.setNotificationText();
        notifications.setBot(this);

        setCalendar();
    }

    // Метод возвращает имя бота, указанное при регистрации.
    @Override
    public String getBotUsername() {
        return "VeryAmbitiousBot";
    }

    // Метод возвращает token бота для связи с сервером Telegram
    @Override
    public String getBotToken() {
        return "";
    }

    // Метод для приема сообщений.
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        AbstractState state = null;

        if (message != null && message.hasText()) {
            String command = message.getText();
            if (command.contains("/help") || command.contains("/start")) {
                state = new StartOrHelpState();
            }

            else if (command.contains("/settings")) {
                state = new SettingsState();
            }

            else if (command.contains("/weather")) {
                state = new WeatherState();
            }

            else if (command.contains("/time")) {
                state = new TimeState();
            }

            else if (command.contains("/remind")) {
                state = new RemindState();
            }

            else if (command.contains("/notification")) {
                state = new NotificationState();
            }

            else if (command.contains("/location")) {
                state = new LocationState();
            }

            else if (command.contains("/couples")) {
                state = new CouplesState();
            }

            else if (command.contains("/translate")) {
                state = new TranslateState();
            }

            else if (command.contains("/todo")) {
                // TODO сделать todo list
            }

            else if (command.contains("/game")) {
                state = new GameState();
            }

            else {
                if (getStateForUser(message.getChatId()).equals("TRANSLATE")) {
                    state = new TranslateState();
                }
                else {
                    sendMsg(message, "Каво?");
                }
            }

            assert state != null;
            state.makeAction(notifications, this, command, message);
        }
        else if(update.hasCallbackQuery()){
            String data = update.getCallbackQuery().getData();
            if (getStateForUser(update.getCallbackQuery().getMessage().getChatId()).equals("DIFFICULTY")) {
                try {
                    TrueOrFalseGame.getQuestions(data, update.getCallbackQuery().getMessage().getChatId(), this);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (getStateForUser(update.getCallbackQuery().getMessage().getChatId()).equals("GAME")) {
                try {
                    TrueOrFalseGame.responseCollation(data, update.getCallbackQuery().getMessage().getChatId(), this);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
            setButtons(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        String helpButton = EmojiParser.parseToUnicode("\uD83E\uDDD0 /help");
        String settingsButton = EmojiParser.parseToUnicode(":gear: /settings");
        String translateButton = EmojiParser.parseToUnicode("✍️ /translate");
        String weatherButton = EmojiParser.parseToUnicode("⛅️ /weather");
        String remindButton = EmojiParser.parseToUnicode("\uD83E\uDD13/remind");
        String couplesButton = EmojiParser.parseToUnicode("\uD83D\uDCDA/couples");
        String gameButton = EmojiParser.parseToUnicode("\uD83C\uDFAD/game");
        keyboardFirstRow.add(new KeyboardButton(helpButton));
        keyboardFirstRow.add(new KeyboardButton(settingsButton));
        keyboardFirstRow.add(new KeyboardButton(translateButton));
        keyboardSecondRow.add(new KeyboardButton(weatherButton));
        keyboardSecondRow.add(new KeyboardButton(remindButton));
        keyboardSecondRow.add(new KeyboardButton(couplesButton));
        keyboardThirdRow.add(new KeyboardButton(gameButton));

        keyboardRowList.add(keyboardFirstRow);
        keyboardRowList.add(keyboardSecondRow);
        keyboardRowList.add(keyboardThirdRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    private void setCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Timer time = new Timer(); // Instantiate Timer Object

        try {
            time.scheduleAtFixedRate(notifications, calendar.getTime(), TimeUnit.HOURS.toMillis(24)); // period = 24 hours
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStateForUser(String state, long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("UPDATE public.users SET state=? WHERE chat_id=?");
        try {
            statement.setString(1, state);
            statement.setInt(2, (int) chatId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getStateForUser(long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT state FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }
}
