package service;

import model.Session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // HashMap which save sessions
    private final ConcurrentHashMap<Long, Session> sessions;

    public SessionManager() {
        sessions = new ConcurrentHashMap<>();
    }

    public synchronized boolean isUserConnected(long userID) {
        return sessions.containsKey(userID);
    }

    public synchronized void addSession(Session session) {
        // Check if user have a session connected
        if (isUserConnected(session.getUserID())) {
            // If have a session open, it will close it
            Session oldSession = sessions.get(session.getUserID());
            oldSession.sendCloseSignal();
            sessions.remove(session.getUserID());
        }
        // Add the new session
        sessions.put(session.getUserID(), session);
    }

    public synchronized Session getSession(long userID) {
        return sessions.get(userID);
    }

    public synchronized void closeSession(long userID){
        sessions.get(userID).sendCloseSignal();
        sessions.remove(userID);
    }
}
