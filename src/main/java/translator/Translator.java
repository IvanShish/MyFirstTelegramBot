package translator;

import java.io.IOException;

public interface Translator {
    String translate(String text, String langFrom, String langTo) throws IOException;
}
