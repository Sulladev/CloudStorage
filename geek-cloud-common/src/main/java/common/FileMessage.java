package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String fileName;
    private  byte[] data;

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        // нао сделать разбивку файла на части
        data = Files.readAllBytes(path);
    }
}
