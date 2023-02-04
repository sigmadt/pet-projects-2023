package ru.itmo.java.message.view;

import ru.itmo.java.message.tracker.FileData;

import java.io.Serializable;

public class FileDataView implements Serializable {
    private final int id;
    private final String name;
    private final long size;

    public FileDataView(FileData fileData) {
        this.id = fileData.getId();
        this.name = fileData.getName();
        this.size = fileData.getSize();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public FileData toFileData() {
        return FileData.newBuilder()
                .setId(id)
                .setName(name)
                .setSize(size)
                .build();
    }
}
