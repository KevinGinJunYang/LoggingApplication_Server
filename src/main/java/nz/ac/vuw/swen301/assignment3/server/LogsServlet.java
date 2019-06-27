package nz.ac.vuw.swen301.assignment3.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum AppenderLevels
{
    ALL,DEBUG,INFO,WARN,ERROR,FATAL,TRACE,OFF
}

public class LogsServlet extends HttpServlet {

    public static List<LogEvent> storedLogs = new ArrayList<>();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String logLimit = req.getParameter("limit");
        String logValue = req.getParameter("level");

        if (logValue==null || logLimit == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if (!EnumUtils.isValidEnum(AppenderLevels.class, logValue)){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            try {
                int limitParsed = Integer.parseInt(logLimit);
                List<LogEvent> logsToConvert = new ArrayList<>();
                int count = 0;
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                for (LogEvent event : storedLogs) {
                    if(logValue.equals("FATAL") && count < limitParsed){
                        if(event.getLevel().equals("FATAL")){
                            count++;
                            logsToConvert.add(event);
                        }

                    } else if (logValue.equals("ERROR") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR")){
                            count++;
                            logsToConvert.add(event);
                        }
                    } else if (logValue.equals("WARN") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR") || event.getLevel().equals("WARN")){
                            count++;
                            logsToConvert.add(event);
                        }

                    } else if (logValue.equals("INFO") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR") || event.getLevel().equals("WARN") || event.getLevel().equals("INFO")){
                            count++;
                            logsToConvert.add(event);
                        }

                    } else if (logValue.equals("DEBUG") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR") || event.getLevel().equals("WARN") || event.getLevel().equals("INFO") || event.getLevel().equals("DEBUG")){
                            count++;
                            logsToConvert.add(event);
                        }

                    } else if (logValue.equals("TRACE") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR") || event.getLevel().equals("WARN") || event.getLevel().equals("INFO") || event.getLevel().equals("DEBUG") || event.getLevel().equals("TRACE")){
                            count++;
                            logsToConvert.add(event);
                        }

                    }else if (logValue.equals("ALL") && count < limitParsed){
                        if(event.getLevel().equals("FATAL") || event.getLevel().equals("ERROR") || event.getLevel().equals("WARN") || event.getLevel().equals("INFO") || event.getLevel().equals("DEBUG") || event.getLevel().equals("TRACE") || event.getLevel().equals("ALL")){
                            count++;
                            logsToConvert.add(event);
                        }
                    } else if (logValue.equals("OFF") && count < limitParsed){
                        count++;
                    }
                }
                PrintWriter out = resp.getWriter();
                Arrays.sort(new List[]{logsToConvert});
                String jsonArray = new Gson().toJson(logsToConvert);
                System.out.println(jsonArray);
                out.println(jsonArray);
            }
            catch (NumberFormatException x) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper object = new ObjectMapper();
        String data = IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8.name());

        if(data != null){
            try {
                List<LogEvent> logEvents = object.readValue(data , new TypeReference<List<LogEvent>>() {});
                int count = 0;
                boolean existingID = false;
                for (LogEvent event : logEvents) {
                    for (LogEvent log : storedLogs) {
                        if (log.getId().equals(event.getId())) {
                            existingID = true;
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        }
                    }
                    if (!existingID) {
                        storedLogs.add(event);
                        count++;
                        existingID = false;
                    }
                }
                if (count == logEvents.size()) {
                    resp.sendError(HttpServletResponse.SC_CREATED, "Log added");
                } else resp.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate log found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public List<LogEvent> getLogs() {
        return storedLogs;
    }

}

