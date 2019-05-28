package web.request;

import java.util.Map;

public interface Type {

    StringBuilder response(Map<String, Object> request);

}
