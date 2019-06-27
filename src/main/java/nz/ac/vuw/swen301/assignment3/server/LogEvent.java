package nz.ac.vuw.swen301.assignment3.server;

public class LogEvent extends LogsServlet {

    public String id;
    public String message;
    public String timeStamp;
    public String thread;
    public String logger;
    public String level;
    public String errorDetails;

    public LogEvent(String id, String message, String timestamp, String thread,
                      String logger, String level, String errorDetails) {
        this.id = id;
        this.message = message;
        this.timeStamp = timestamp;
        this.thread = thread;
        this.logger = logger;
        this.level = level;
        this.errorDetails = errorDetails;
    }


    public LogEvent() {
    }


    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public static long getCurrentTimeMilli() {
        return System.currentTimeMillis();
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getThread() {
        return thread;
    }

    public String getLogger() {
        return logger;
    }

    public String getLevel() {
        return level;
    }

    public String getErrorDetails() {
        return errorDetails;
    }


    public String toString() {
        return "" + this.getId() + "\n"
                + this.getMessage() + "\n"
                + getTimeStamp() + "\n"
                + this.getThread() + "\n"
                + this.getLogger() + "\n"
                + this.getLevel() + "\n"
                + this.getErrorDetails() + "";
    }

}
