package translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class YandexTranslator implements Translator {
    private static final Logger logger = LoggerFactory.getLogger(YandexTranslator.class);
    private static Translator translator;

    public YandexTranslator() {

    }

    public static Translator getInstance() {
        if (translator == null) {
            translator = new YandexTranslator();
            logger.info("Translator instance created");
        }
        return translator;
    }

    @Override
    public String translate(String text, String langFrom, String langTo) throws IOException {
        logger.debug("Starting translating text: {}. Translation lang: {}", text, langTo);
        String urlStr = "https://script.google.com/macros/s/AKfycbyKWb0VIY_qE2OEZV9Upiex70vrnHTAJu2388IOFCYrVl2v0Ic/exec" +
                "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}