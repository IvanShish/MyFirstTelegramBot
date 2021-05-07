package game;

import bot.Bot;
import database.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TrueOrFalseGame {
    private static Map<Integer, Integer> score = new HashMap<>();

    public static void getQuestions(String difficulty, long chatId, Bot bot) throws IOException, URISyntaxException, SQLException, TelegramApiException {
        URL url = new URL("https://opentdb.com/api.php?amount=5&difficulty=" + difficulty + "&type=boolean");
        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }

        JSONObject object = new JSONObject(result);
        JSONArray arr = object.getJSONArray("results");

        delQuestions(chatId);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject tmp = arr.getJSONObject(i);
            setQuestion(XML.unescape(tmp.getString("question")), tmp.getString("correct_answer"), chatId);
//            System.out.println(XML.unescape(tmp.getString("question")));
//            System.out.println(tmp.getString("correct_answer"));
        }
        score.keySet().remove((int) chatId);
        score.put((int) chatId, 0);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Выбрана сложность \"" + difficulty + "\"");
        bot.execute(sendMessage);

        nextQuestion(getQuestion(chatId), chatId, bot);
    }

    private static void delQuestions(long chatId) throws SQLException {
        String deleting = "DELETE FROM public.quiz WHERE user_id=(SELECT id FROM public.users WHERE chat_id=?)";
        PreparedStatement pst = Database.getConnection().prepareStatement(deleting);
        try {
            pst.setInt(1, (int) chatId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void delQuestion(long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int userId = resultSet.getInt(1);

        String deleting = "DELETE FROM public.quiz WHERE user_id=? AND question=(SELECT question FROM public.quiz " +
                                    "WHERE user_id=? ORDER BY id ASC " +
                                    "LIMIT 1)";
        PreparedStatement pst = Database.getConnection().prepareStatement(deleting);
        try {
            pst.setInt(1, userId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void setQuestion(String question, String answer, long chatId) throws URISyntaxException, SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int userId = resultSet.getInt(1);
        String inserting = "INSERT INTO public.quiz(user_id, question, answer) VALUES (?, ?, ?)";
        PreparedStatement pst = Database.getConnection().prepareStatement(inserting);
        try {
            pst.setInt(1, userId);
            pst.setString(2, question);
            pst.setString(3, answer);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String getQuestion(long chatId) {
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id FROM public.users WHERE chat_id=?");
            statement.setInt(1, (int) chatId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int userId = resultSet.getInt(1);

            PreparedStatement selectQuestion = Database.getConnection().prepareStatement("SELECT question FROM public.quiz " +
                    "WHERE user_id=? ORDER BY id ASC " +
                    "LIMIT 1");
            selectQuestion.setInt(1, userId);
            ResultSet res = selectQuestion.executeQuery();
            if (res.next()) {
                return res.getString(1);
            }
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static void nextQuestion (String question, long chatId, Bot bot) throws URISyntaxException, SQLException, TelegramApiException {
        if (question == null) {
            endOfGame(chatId, bot);
        }
        bot.execute(InlineKeyboardMessages.inlineKeyBoardMessageForGame(chatId, bot, question));
    }

    public static void responseCollation (String answer, long chatId, Bot bot) throws TelegramApiException, URISyntaxException, SQLException {
        String correctAnswer;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id FROM public.users WHERE chat_id=?");
            statement.setInt(1, (int) chatId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int userId = resultSet.getInt(1);

            PreparedStatement selectAnswer = Database.getConnection().prepareStatement("SELECT answer FROM public.quiz " +
                    "WHERE user_id=? ORDER BY id ASC " +
                    "LIMIT 1");
            selectAnswer.setInt(1, userId);
            ResultSet resAns = selectAnswer.executeQuery();
            resAns.next();
            correctAnswer = resAns.getString(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (answer.equals(correctAnswer)) {
            score.put((int) chatId, score.get((int) chatId) + 1);
            sendMessage.setText("Вы ответили верно! Ваш счет: " + score.get((int) chatId));
        }
        else {
            sendMessage.setText("Вы ответили неверно! Ваш счет: " + score.get((int) chatId));
        }
        bot.execute(sendMessage);

        delQuestion(chatId);
        nextQuestion(getQuestion(chatId), chatId, bot);
    }

    private static void endOfGame(long chatId, Bot bot) throws SQLException {
        try {
            PreparedStatement selectBestScore = Database.getConnection().prepareStatement("SELECT best_score FROM public.users WHERE chat_id=?");
            selectBestScore.setInt(1, (int) chatId);
            ResultSet resultSet = selectBestScore.executeQuery();
            int bestScore = 0;
            if (resultSet.next()) bestScore = resultSet.getInt(1);
            if (score.get((int) chatId) > bestScore) {
                bestScore = score.get((int) chatId);
            }
            String updating = "UPDATE public.users SET best_score=? WHERE chat_id=?";
            PreparedStatement pst = Database.getConnection().prepareStatement(updating);
            pst.setInt(1, bestScore);
            pst.setInt(2, (int) chatId);
            pst.executeUpdate();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Игра завершена!\nВаш счет: " + score.get((int) chatId) + "\nВаш лучший счет: " + bestScore);
            bot.execute(sendMessage);
        } catch (SQLException | TelegramApiException throwables){
            throwables.printStackTrace();
        }
        bot.setStateForUser("OTHER", chatId);
    }
}
