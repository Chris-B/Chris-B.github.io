package http;

import com.google.gson.Gson;
import file.FileHandler;

public class HttpConfig {

    public String title;
    public String author;
    public String description;
    public String charset;

    public HttpConfig() {}
    public static HttpConfig serialize(String fileLocation) {
        String jsonStr = FileHandler.fileToString(fileLocation);
        return new Gson().fromJson(jsonStr, HttpConfig.class);
    }

}
