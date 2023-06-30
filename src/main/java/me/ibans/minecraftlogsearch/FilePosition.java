package me.ibans.minecraftlogsearch;

import java.io.File;

public class FilePosition {

    private final int position;
    private final File file;

    public FilePosition(int position, File file) {
        this.position = position;
        this.file = file;
    }

    public int getPosition() {
        return position;
    }

    public File getFile() {
        return file;
    }
}
