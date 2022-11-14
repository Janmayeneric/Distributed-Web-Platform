package object;

public class Notification {

    private String content;
    private boolean seen;
    private long time;

    Notification() {}

    Notification(String content) {
        this.content = content;
        this.seen = false;
        this.time = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public boolean isSeen() {
        return seen;
    }

    public long getTime() {
        return time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
