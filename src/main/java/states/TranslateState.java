package states;

import actions.Notifications;
import bot.Bot;
import org.json.XML;
import org.telegram.telegrambots.meta.api.objects.Message;
import translator.Translator;
import translator.YandexTranslator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslateState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException, IOException {
        long chatId = message.getChatId();
        String currState = bot.getStateForUser(chatId);
        if (currState != null && currState.equals("TRANSLATE")) {
            // CHECKING WHETHER THE TEXT IS WRITTEN IN RUSSIAN STARTS
            Pattern patternRu = Pattern.compile(
                    "[" +                   //начало списка допустимых символов
                            "а-яА-ЯёЁ" +    //буквы русского алфавита
                            "\\d" +         //цифры
                            "\\s" +         //знаки-разделители (пробел, табуляция и т.д.)
                            "\\p{Punct}" +  //знаки пунктуации
                            "]" +           //конец списка допустимых символов
                            "*");           //допускается наличие указанных символов в любом количестве
            Matcher matcherRu = patternRu.matcher(command);
            // CHECKING WHETHER THE TEXT IS WRITTEN IN RUSSIAN ENDS

            Translator translator = new YandexTranslator();
            String translatedText;
            if (matcherRu.matches()) {
                translatedText = translator.translate(command, "ru", "en");
            }
            else {
                translatedText = translator.translate(command, "en", "ru");
            }
            bot.sendMsg(message, XML.unescape(translatedText));

            bot.setStateForUser("OTHER", chatId);
        }
        else {
            bot.setStateForUser("TRANSLATE", chatId);
            bot.sendMsg(message, "Введите текст");
        }
    }
}
