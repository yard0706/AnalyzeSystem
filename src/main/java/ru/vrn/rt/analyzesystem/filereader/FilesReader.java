package ru.vrn.rt.analyzesystem.filereader;

import java.io.IOException;

public interface FilesReader {
    String readFirstLines(String filePath, int lines) throws IOException;
}
