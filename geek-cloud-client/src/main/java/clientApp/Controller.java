package clientApp;

import common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField console;

    @FXML
    TextField getLogin;

    @FXML
    TextField getPassword;

    @FXML
    ListView<String> clientFileList;

    @FXML
    ListView<String> serverFileList;

    // список файлов с сервера
    ArrayList<String> requestForServerFileList;

    String fileToDownload;

    String fileToUpload;

    String fileToDelete;

    String login;

    String password;

    String userDir = "client_repository/";




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        writeFileFromServer(fm);
                    }
                    if (am instanceof CommandMessage) {
                        CommandMessage commandMessage = (CommandMessage) am;
                        getServerFileList(commandMessage);
                    }
                    refreshServerFileList();
                    refreshClientFileList();

                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshClientFileList();
    }

   // запись файла на сервер
    private void writeFileFromServer(FileMessage fm) {
        try {
            Files.write(Paths.get(userDir + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // получение спика файлов  с сервера завёрнутого в CommandMessage
    private void getServerFileList (CommandMessage commandMessage) {
        if (commandMessage.getCommand().startsWith("/")) {
            String[] command = commandMessage.getCommand().split(" ");
            if (command[0].equals("/list")) {
                requestForServerFileList = commandMessage.getCommandList();
            }
        } else {
            console.setText(commandMessage.getCommand());
        }
    }

    //отправляем логин и пароль на проверку
    public void pressOnLoginBtn(ActionEvent actionEvent) {
        login = getLogin.getText();
        password = getPassword.getText();
        if (login != null && password != null) {
            Network.sendMessage(new AuthorizationMessage("/auth " + login + "±" + password));
            login = null;
            password = null;
            getLogin.clear();
            getPassword.clear();

        }

    }

    //обновляем список файлов с севреа в GUI
    public void pressOnRefreshFilesBtn(ActionEvent actionEvent) {
        Network.sendMessage(new CommandMessage("/list"));

    }

    //реализация загрузки файла на клиента через GUI
    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        fileToDownload = console.getText();
        if (!serverFileList.getItems().isEmpty()) {
            Network.sendMessage(new FileRequest(fileToDownload));
            console.clear();
            System.out.println("File received");
        }
    }

    //реализация загрузки файла на сервер через GUI
    public void pressOnUploadBtn(ActionEvent actionEvent) {
        fileToUpload = console.getText();
        try {
            if (Files.exists(Paths.get(userDir + fileToUpload))) {
                FileMessage fileMessage = new FileMessage(Paths.get(userDir + fileToUpload));
                Network.sendMessage(fileMessage);
                console.clear();
                System.out.println("File was sent");
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //удаляем файлы с клиента. Папка + GUI
    public void pressOnDeleteOnClientBtn(ActionEvent actionEvent) {
        fileToDelete = console.getText();
        if (fileToDelete != null && Files.exists(Paths.get(userDir + fileToDelete))) {
            try {
                Files.delete(Paths.get(userDir + fileToDelete));
                System.out.println("File " + fileToDelete + " was deleted");
                fileToDelete = null;
                console.clear();
                refreshClientFileList();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //удаляем файлы с сервера. Папка + GUI
    public void pressOnDeleteOnServerBtn(ActionEvent actionEvent) {
        fileToDelete = console.getText();
        if (fileToDelete != null) {
            Network.sendMessage(new CommandMessage("/del " + fileToDelete));
            fileToDelete = null;
            console.clear();
            refreshServerFileList();
        }
    }

    //обновляем список файлов на клиенте
    public void refreshClientFileList() {
        Platform.runLater(() -> {
            try {
                clientFileList.getItems().clear();
                Files.list(Paths.get(userDir))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientFileList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //обновляем список файлов на сервере
    public void refreshServerFileList() {
          Platform.runLater(() -> {
              serverFileList.getItems().clear();

              for (String fileRequest : requestForServerFileList ) {
                  serverFileList.getItems().add(fileRequest);
              }
          });
    }
}

