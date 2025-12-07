import java.io.*;

public class FileImporter {
    public void importFile(String path) {
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            // do something
            byte[] buffer = new byte[1024];
            is.read(buffer);
        } catch (IOException e) {
            System.err.println("Failed to import");
            // Bug: swallowing original exception without proper logging or rethrow
        } finally {
            // Bug: not closing InputStream if open fails
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
