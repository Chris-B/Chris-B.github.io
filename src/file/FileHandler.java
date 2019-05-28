package file;

import java.io.*;

public class FileHandler {

    public static byte[] getWebFile(String path) {
        BufferedInputStream stream;
        byte[] fBytes;
        try {
            File file = new File("view".concat(path));
            stream = new BufferedInputStream(new FileInputStream(file));
            fBytes = new byte[stream.available()];
            stream.read(fBytes, 0, stream.available());
        } catch (Exception e) {
            return null;
        }
        return fBytes;
    }

}
