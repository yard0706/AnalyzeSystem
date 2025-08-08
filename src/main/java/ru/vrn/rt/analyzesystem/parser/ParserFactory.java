package ru.vrn.rt.analyzesystem.parser;

import java.io.File;

public class ParserFactory {
    public void execute(File file) {
        System.out.println("parser execute:"+file.getAbsoluteFile());
    }
}
