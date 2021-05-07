package actions;

import bot.Bot;
import database.Database;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimerTask;

public class Notifications extends TimerTask {
    private Bot bot;
    public static String MONDAY_NOTIFICATION_EVEN_WEEK = "";
    public static String MONDAY_NOTIFICATION_NOT_EVEN_WEEK = "";
    public static String WEDNESDAY_NOTIFICATION_EVEN_WEEK = "";
    public static String WEDNESDAY_NOTIFICATION_NOT_EVEN_WEEK = "";
    public static String THURSDAY_NOTIFICATION_EVEN_WEEK = "";
    public static String THURSDAY_NOTIFICATION_NOT_EVEN_WEEK = "";
    public static String FRIDAY_NOTIFICATION_EVEN_WEEK = "";
    public static String FRIDAY_NOTIFICATION_NOT_EVEN_WEEK = "";
    public static String WEEKEND_NOTIFICATION = "";

    @Override
    public void run() {
        try {
            sendNotifications();
        } catch (Exception ex) {
            System.out.println("error running thread " + ex.getMessage());
        }
    }

    public void setNotificationMode(long chatId) throws SQLException {
        boolean currentMode = getNotificationMode(chatId);
        PreparedStatement statement = Database.getConnection().prepareStatement("UPDATE public.users SET mode=? WHERE chat_id=?");
        try {
            if (currentMode) statement.setInt(1, 0);
            else statement.setInt(1, 1);
            statement.setInt(2, (int) chatId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean getNotificationMode(long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT mode FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int mode = resultSet.getInt(1);
        return mode == 1;
    }

    private void sendNotifications() throws TelegramApiException, IOException, URISyntaxException, SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT chat_id, mode, name FROM public.users");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int chatId = resultSet.getInt(1);
            int isOn = resultSet.getInt(2);
            String name = resultSet.getString(3);
            if (isOn == 1) {
                Calendar c = Calendar.getInstance();
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                String text = name + ", доброе утро! ";

                if (dayOfWeek == 2) {
                    if (isWeekEven()) text += MONDAY_NOTIFICATION_EVEN_WEEK;
                    else text += MONDAY_NOTIFICATION_NOT_EVEN_WEEK;
                }
                else if (dayOfWeek == 4) {
                    if (isWeekEven()) text += WEDNESDAY_NOTIFICATION_EVEN_WEEK;
                    else text += WEDNESDAY_NOTIFICATION_NOT_EVEN_WEEK;
                }
                else if (dayOfWeek == 5) {
                    if (isWeekEven()) text += THURSDAY_NOTIFICATION_EVEN_WEEK;
                    else text += THURSDAY_NOTIFICATION_NOT_EVEN_WEEK;
                }
                else if (dayOfWeek == 6) {
                    if (isWeekEven()) text += FRIDAY_NOTIFICATION_EVEN_WEEK;
                    else text += FRIDAY_NOTIFICATION_NOT_EVEN_WEEK;
                }
                else text += WEEKEND_NOTIFICATION;

                text += Weather.getWeather("", chatId);
                text += "\n";

                SendMessage ad = new SendMessage();
                ad.setChatId(String.valueOf(chatId));
                ad.setText(text);
                bot.execute(ad);
            }
        }
    }

    public static void setNotificationText()  {
        WEEKEND_NOTIFICATION = "Сегодня у вас ничего нет \uD83D\uDE01\n";
        MONDAY_NOTIFICATION_EVEN_WEEK = "Сегодня у вас:\n" +
                "9:40 - лекция по социологии\n" +
                "13:45 - диффуры лекция очно\n" +
                "15:40 - диффуры практика очно\n";
        WEDNESDAY_NOTIFICATION_EVEN_WEEK = "Сегодня у вас:\n" +
                "10:00 - лекция по ОТХД\n";
        WEDNESDAY_NOTIFICATION_NOT_EVEN_WEEK = "Сегодня у вас:\n" +
                "19:00 - лекция по нейронкам\n";
        THURSDAY_NOTIFICATION_EVEN_WEEK = "Сегодня у вас:\n" +
                "13:45 - семинар по ДВС\n";
        FRIDAY_NOTIFICATION_EVEN_WEEK = "Сегодня у вас:\n" +
                "13:45 - практика по ЭлФункцАн\n" +
                "15:40 - лаба по нейронным сетям\n";
        MONDAY_NOTIFICATION_NOT_EVEN_WEEK = "Сегодня у вас:\n" +
                "13:45 - диффуры лекция очно\n" +
                "15:40 - социология практика очно\n";
        THURSDAY_NOTIFICATION_NOT_EVEN_WEEK = "Сегодня у вас:\n" +
                "11:35 - лекция по ДВС\n";
        FRIDAY_NOTIFICATION_NOT_EVEN_WEEK = "Сегодня у вас:\n" +
                "13:45 - практика по нейронным сетям\n";
    }

    public boolean isWeekEven() {
        String eventStr = "2020-08-01T00:00:00Z";
        DateTimeFormatter fmt = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        Instant event = fmt.parse(eventStr, Instant::from);
        Instant now = Instant.now();
        Duration diff = Duration.between(now, event);
        long days = Math.abs(diff.toDays());
        return (days/7) % 2 == 0;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
