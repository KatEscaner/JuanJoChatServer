import model.Message;

import java.io.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue {
    private final ConcurrentHashMap<Long, Queue<Message>> userQueues;
    private final GroupDAO groupDAO = new GroupDAO();

    File file = new File("src/last_message_id.txt");
    long lastId;
    public MessageQueue() {
        this.userQueues = new ConcurrentHashMap<>();
        try{
            if (!file.exists()) {
                file.createNewFile();
                saveLastId(0L);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            lastId = Long.parseLong(br.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void addMessage(Message message, List<Long> users) {
        message.setId(lastId);
        for(Long userID : users) {
            Queue<Message> queue = userQueues.get(userID);
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
                userQueues.put(userID, queue);
            }
            userQueues.get(userID).add(message);
        }
        lastId++;
        saveLastId(lastId);
    }

    public synchronized Message getNextMessage(Long userID) {
        Queue<Message> queue = userQueues.get(userID);
        if (queue != null) {
            return queue.peek();
        }
        return null;
    }

    public synchronized void removeMessage(Long userID) {
        Queue<Message> queue = userQueues.get(userID);
        if (queue != null) {
            queue.poll();
        }
    }

    public synchronized boolean checkIfHaveMessages(Long userID){
        Queue<Message> queue = userQueues.get(userID);
        if(queue != null)
            return queue.size() > 0;
        return false;
    }

    public void saveLastId(Long lastIdToSave){
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(Long.toString(lastIdToSave));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
