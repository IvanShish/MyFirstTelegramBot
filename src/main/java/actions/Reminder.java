package actions;

import bot.Bot;
import database.Database;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Reminder {
    public static void remind(Message message, Bot bot) throws URISyntaxException, SQLException {
        long chatId = message.getChatId();
        String mes = message.getText();
        mes = mes.replaceAll("/remind", "");
        try {
            mes = mes.replaceAll("\uD83E\uDD13", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mes.length() == 0) {
            String txt = "Ваши напоминания:\n";
            PreparedStatement statement = Database.getConnection().prepareStatement("SELECT reminder FROM public.reminders WHERE user_id=" +
                    "(SELECT id FROM public.users WHERE chat_id=?)");
            statement.setInt(1, (int) chatId);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                txt += resultSet.getString(1) + "\n";
            }
            bot.sendMsg(message, txt);
            return;
        }

        int day = 0;
        int month = 0;
        int hour = 0;
        int minute = 0;
        String txt = "";
        try {
            mes = mes.substring(1, mes.length());
            day = Integer.parseInt(Character.toString(mes.charAt(0)) + Character.toString(mes.charAt(1)));
            mes = mes.substring(3, mes.length());
            month = Integer.parseInt(Character.toString(mes.charAt(0)) + Character.toString(mes.charAt(1)));
            mes = mes.substring(3, mes.length());
            hour = Integer.parseInt(Character.toString(mes.charAt(0)) + Character.toString(mes.charAt(1)));
            mes = mes.substring(3, mes.length());
            minute = Integer.parseInt(Character.toString(mes.charAt(0)) + Character.toString(mes.charAt(1)));
            mes = mes.substring(3, mes.length());
            txt += mes;
        } catch (Exception e) {
            bot.sendMsg(message, "Неправильно написано напоминание!");
        }

        String x = txt + " " + String.valueOf(day) + "." + String.valueOf(month) + " в " + String.valueOf(hour) +
                ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute));
        bot.sendMsg(message, "Успешно!\nНапомню " + x);


        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int userId = resultSet.getInt(1);
        String inserting = "INSERT INTO public.reminders(user_id, reminder) VALUES (?, ?)";
        PreparedStatement pst = Database.getConnection().prepareStatement(inserting);
        try {
            pst.setInt(1, userId);
            pst.setString(2, x);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        if (hour == 1) hour = 22;   // As Moscow Standard Time: GMT+3
        else if (hour == 2) hour = 23;
        else if (hour == 0) hour = 20;
        else hour -= 3;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Timer time = new Timer();

        String finalTxt = txt;
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    bot.sendMsg(message, finalTxt);
                    String deleting = "DELETE FROM public.reminders WHERE user_id=? AND reminder=?";
                    PreparedStatement pst = Database.getConnection().prepareStatement(deleting);
                    try {
                        pst.setInt(1, userId);
                        pst.setString(2, x);
                        pst.executeUpdate();
                    } catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                    }
                } catch (Exception ex) {
                    System.out.println("error running thread " + ex.getMessage());
                }
            }
        }, calendar.getTime());
    }
}
