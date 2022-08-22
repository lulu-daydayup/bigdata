package org.example.operator;

/**
 * Description:
 *
 * @date 2022/8/22 11:33 PM
 **/
public class Event {
    public String user;
    public String url;
    public Long timestamp;

    public Event() {
    }

    public Event(String user, String url, Long timestamp) {
        this.user = user;
        this.url = url;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Event{" +
                "user='" + user + '\'' +
                ", url='" + url + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
