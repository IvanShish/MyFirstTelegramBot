package states;

import actions.Notifications;
import bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Calendar;

public class CouplesState extends AbstractState {
    @Override
    public void makeAction(Notifications notifications, Bot bot, String command, Message message) throws SQLException, URISyntaxException {
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String text = "";

        if (dayOfWeek == 2) {
            if (notifications.isWeekEven()) text += Notifications.MONDAY_NOTIFICATION_EVEN_WEEK;
            else text += Notifications.MONDAY_NOTIFICATION_NOT_EVEN_WEEK;
        }
        else if (dayOfWeek == 4) {
            if (notifications.isWeekEven()) text += Notifications.WEDNESDAY_NOTIFICATION_EVEN_WEEK;
            else text += Notifications.WEDNESDAY_NOTIFICATION_NOT_EVEN_WEEK;
        }
        else if (dayOfWeek == 5) {
            if (notifications.isWeekEven()) text += Notifications.THURSDAY_NOTIFICATION_EVEN_WEEK;
            else text += Notifications.THURSDAY_NOTIFICATION_NOT_EVEN_WEEK;
        }
        else if (dayOfWeek == 6) {
            if (notifications.isWeekEven()) text += Notifications.FRIDAY_NOTIFICATION_EVEN_WEEK;
            else text += Notifications.FRIDAY_NOTIFICATION_NOT_EVEN_WEEK;
        }
        else text += Notifications.WEEKEND_NOTIFICATION;
        bot.sendMsg(message, text);
    }
}
