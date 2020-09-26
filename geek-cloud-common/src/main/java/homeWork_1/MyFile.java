package homeWork_1;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class MyFile implements Serializable {
    private String name;
    private long size;
    private byte[] bytes;

    public MyFile (Path path) {
        this.name = path.getFileName().toString();
        try {
            this.size = Files.size(path);
            this.bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println("Something wrong with file");
            e.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}


//    Как организовать клиент-серверное взаимодействие?
//    Сервер работает как фабрика сервер - сокетов под каждый сокет со стороны клиента.
//        По аналогии с сетевым чатом планирую создать модули Server, Client, Network и Message .
//        Для сетевого взаимодействия буду использовать сериализацию, библиотеки java.nio, Netty.
//
//        Как и в каком виде передавать файлы?
//        Обьектами с помощью сериализации.

//        Как пересылать большие файлы?
//        Целиком или дробить их на части. Наверное с Netty это буддет удобно реализовать
//
//        Как пересылать служебные команды?
//        Создать  абстракцию Message с подклассами Command и File.
//
//        В БД можно хранить много чего. Дл начала никнейм, пароль, логин


