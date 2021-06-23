package actions;

import database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class Weather {
    public static final String MY_TOKEN = System.getenv("WEATHER_TOKEN");

    public static String getWeather(String message, long chatId) throws IOException, URISyntaxException, SQLException {
        message = message.replaceAll("/weather", "");
        try {
            message = message.replaceAll("⛅️ ", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        URL url;
        if (message.length() > 0) {
            message = message.substring(1, message.length());
            url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + message + "&units=metric&appid=" + MY_TOKEN);
        }
        else {
            String city = getCity(chatId);
            url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + MY_TOKEN);
        }

        Scanner in = new Scanner((InputStream) url.getContent());
        String result = "";
        while (in.hasNext()) {
            result += in.nextLine();
        }

        JSONObject object = new JSONObject(result);

        JSONObject main = object.getJSONObject("main");

        JSONArray arr = object.getJSONArray("weather");

        JSONObject tmp = arr.getJSONObject(0);

        HashMap<String, String> emojiWeather = new HashMap<>();
        emojiWeather.put("Clear", "☀️");
        emojiWeather.put("Clouds", "☁️");
        emojiWeather.put("Rain", "\uD83C\uDF27");
        emojiWeather.put("Snow", "\uD83C\uDF28");
        emojiWeather.put("Drizzle", "\uD83C\uDF27");
        emojiWeather.put("Extreme", "⛈");
        emojiWeather.put("Mist", "\uD83C\uDF25");
        emojiWeather.put("Thunderstorm", "\uD83C\uDF29");
        emojiWeather.put("Atmosphere", "\uD83C\uDF25");


        return  "Погода в " + object.getString("name") + " следующая:\n" +
                "Температура: " + main.getDouble("temp") + "C " + emojiWeather.get(tmp.get("main")) + "\n" +
                "Влажность: " + main.getDouble("humidity") + "%\n";
    }

    public static void setCity(String c, long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("UPDATE public.users SET location=? WHERE chat_id=?");
        try {
            statement.setString(1, c);
            statement.setInt(2, (int) chatId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static String getCity(long chatId) throws SQLException {
        PreparedStatement statement = Database.getConnection().prepareStatement("SELECT location FROM public.users WHERE chat_id=?");
        statement.setInt(1, (int) chatId);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }
}
