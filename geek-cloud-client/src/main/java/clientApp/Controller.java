package clientApp;

import common.AbstractMessage;
import common.CommandMessage;
import common.FileMessage;
import common.FileRequest;
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
    ListView<String> clientFileList;

    @FXML
    ListView<String> serverFileList;

    ArrayList<String> requestForServerFileList;

    String fileToDownload;

    String fileToUpload;

    String fileToDelete;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Network.sendMessage(new CommandMessage("/list"));
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
        refreshServerFileList();
        refreshClientFileList();
    }

//    private void consoleCommand (ActionEvent actionEvent) {
//        System.out.println(console.getText());
//        console.clear();
//    }

    private void writeFileFromServer(FileMessage fm) {
        try {
            Files.write(Paths.get("client_repository/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        fileToDownload = console.getText();
        if (serverFileList.getItems().size() > 0) {
            Network.sendMessage(new FileRequest(fileToDownload));
            console.clear();
            System.out.println("File received");
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        fileToUpload = console.getText();
        try {
            if (Files.exists(Paths.get("client_repository/" + fileToUpload))) {
                FileMessage fileMessage = new FileMessage(Paths.get("client_repository/" + fileToUpload));
                Network.sendMessage(fileMessage);
                System.out.println("File was sent");
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //доделать удаление файла
    public void pressOnDeleteOnClientBtn(ActionEvent actionEvent) {
        fileToDelete = console.getText();
        if (fileToDelete != null) {
            if (Files.exists(Paths.get("client_repository/" + fileToDelete))) {
                try {
                    Files.delete(Paths.get("client_repository/" + fileToDelete));
                    System.out.println("File " + fileToDelete + " was deleted");
                    fileToDelete = null;
                    refreshClientFileList();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pressOnDeleteOnServerBtn(ActionEvent actionEvent) {
        fileToDelete = console.getText();
        if (fileToDelete != null) {
            Network.sendMessage(new CommandMessage("/del " + fileToDelete));
            fileToDelete = null;
            refreshServerFileList();
        }
    }

    public void refreshClientFileList() {
        Platform.runLater(() -> {
            try {
                clientFileList.getItems().clear();
                Files.list(Paths.get("client_repository"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> clientFileList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFileList() {
          Platform.runLater(() -> {
              serverFileList.getItems().clear();

              for (String fileRequest : requestForServerFileList ) {
                  serverFileList.getItems().add(fileRequest);
              }
          });
    }


}

