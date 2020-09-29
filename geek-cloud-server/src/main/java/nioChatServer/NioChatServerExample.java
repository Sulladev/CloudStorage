package nioChatServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioChatServerExample implements Runnable {
    //ждёт входящих подключений
    private ServerSocketChannel serverSocketChannel;
    //На Селекторе регистрируем события. Селектор параллельно слушает всех.
    // Как только кто-то что-то присылает - появляется событие которое мы обрабатываем
    private Selector selector;
    // Рабочий байт-буффер
    private ByteBuffer buf = ByteBuffer.allocate(256);
    // Клиенты индексируются с индекса 1
    private int acceptedClientIndex = 1;
    // Стандартное приветствие
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Добро пожаловать в чат!\n".getBytes());

    NioChatServerExample() throws IOException {
        //Создаём сервер-сокет канал. .open() - создание объекта
        this.serverSocketChannel = ServerSocketChannel.open();
        //Слушаем всех на поорту 8189
        this.serverSocketChannel.socket().bind(new InetSocketAddress(8189));
        //Работаем не в блокирующем режиме. Потенциально можем включить блокировку
        this.serverSocketChannel.configureBlocking(false);
        //Создаём селектор
        this.selector = Selector.open();
        //Сервер-сокет канал ты регаешься на селекторе(селектор реагирует на твои события)
        //на какие собыьтя? На OP_ACCEPT. Когда кто-то подключается к серваку - Селектор оповестит об этом
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("Сервер запущен (Порт: 8189)");
            //Создаём итератор
            Iterator<SelectionKey> iter;
            //Информация о событии. Что произошло и на каком канале.
            SelectionKey key;
            //до тех пор пока сервак включен
            while (this.serverSocketChannel.isOpen()) {
                //Селектор садись и жди пока у тебя не сработают события
                selector.select();
                //как только приходят события мы получаем итератор по этим событиям
                iter = this.selector.selectedKeys().iterator();
                //до тех пор пока есть необработанное событие
                while (iter.hasNext()) {
                    //получаем ссылку на это событие
                    key = iter.next();
                    //выкидываем его из списка
                    iter.remove();
                    //Если кто-то подключился - то мы обрабатываем как accept
                    if (key.isAcceptable()) this.handleAccept(key);
                    //Если сработал readable - обрабатываем как чтение
                    if (key.isReadable()) this.handleRead(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        //Из ключа выдёргиаем Сокет Канал.
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        //Присваиваем имя
        String clientName = "Клиент #" + acceptedClientIndex;
        //Увеличиваем индекс
        acceptedClientIndex++;
        //Работаем с клиентов в вне-блокирующем режиме
        sc.configureBlocking(false);
        //Хотим что бы этот сокет канал зарегестрировался на Селекторе.
        // Селектор должен реагировать на сообщения пришедшии с этого канала,
        // и в качестве key.attachment добавляем регистрационный номер клиента
        sc.register(selector, SelectionKey.OP_READ, clientName);
        //Отправляем приветствие
        sc.write(welcomeBuf);
        //Возвращаем position в начало буффера.
        welcomeBuf.rewind();
        System.out.println("Подключился новый клиент " + clientName);
    }
    //Клиент что-то прислал и мы хотим это как-то поработать
    private void handleRead(SelectionKey key) throws IOException {
        //Из Selection.key мы выдергиваем канал
        SocketChannel ch = (SocketChannel) key.channel();
        //Формируем Стринг Билдер
        StringBuilder sb = new StringBuilder();
        //Очищаем работчий буффер
        buf.clear();
        //не было прочитанно не одного байта
        int read = 0;
        //начинаем вычитывать эти данные. Буфер заполняется
        while ((read = ch.read(buf)) > 0) {
            //Делаем флип, чтобы из буфера данные вычитывать
            buf.flip();
            //Формируем массив по размеру сообщения в этом буфере
            byte[] bytes = new byte[buf.limit()];
            //Перекидываем данные из буфера в массив
            buf.get(bytes);
            //По массиву формируем строчку и лепим ее к Стринг Билдеру
            sb.append(new String(bytes));
            //Чистим буффер
            buf.clear();
        }
        String msg;
        // ch.read(buf) < 0  - значит мы ничего не успели прочитать, соединение закрылось
        if (read < 0) {
            msg = key.attachment() + " покинул чат\n";
            ch.close();
        } else {
            //при регистрации добавили номер мклиента в key.attachment и печатаем
            //собранную ранее строчку
            msg = key.attachment() + ": " + sb.toString();
        }

        System.out.println(msg);
        //сформировав сообщение - то что нужно всем разослать - от даем его с broadcastMessage
        broadcastMessage(msg);
    }

    private void broadcastMessage(String msg) throws IOException {
        //Метод .wrap() - позволяет байтовый массив завернуть в байт буфер
        ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
        //Перебераем selectionkey в Selector - т.е перебираем всех пользователей
        for (SelectionKey key : selector.keys()) {
            //Если клую валидный, т.е клиент с нами еще общается
            // и канал канал который зареген на селекторе является Сокет Каналом
            // (т.е это обычный пользователь, а не сервак)
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                //Тогда мы получаем ссылку на канал к какому -то клиенту
                SocketChannel sch = (SocketChannel) key.channel();
                //Пишем в канал то что написано в msgBuf (пришедшее нам сообщение)
                sch.write(msgBuf);
                //Делаем rewind что бы следующий клиент получил сообшение с начала
                msgBuf.rewind();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //В отдельном треде запускаем чат. Почему Сервак в отдельном канале?
        new Thread(new NioChatServerExample()).start();
    }
}
