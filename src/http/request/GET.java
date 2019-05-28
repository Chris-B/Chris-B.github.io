package http.request;

import file.FileHandler;

import java.util.Map;

public class GET implements Type {

    @Override
    public StringBuilder response(Map<String, Object> request) {
        String path = request.get("uri").toString().trim();

        if (path.endsWith("/"))
            path = path.concat("index.html");

        System.out.println(path);

        byte[] response = FileHandler.getWebFile(path);

        if (response == null)
            return new StringBuilder();

        return new StringBuilder(new String(response));
    }

}
