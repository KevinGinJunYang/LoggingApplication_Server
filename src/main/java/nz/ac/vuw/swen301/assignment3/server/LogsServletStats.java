package nz.ac.vuw.swen301.assignment3.server;

import com.google.gson.Gson;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class LogsServletStats extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/vnd.ms-excel");
        LogsServlet logs = new LogsServlet();
        List<LogEvent> logsToConvert = logs.getLogs();
        String jsonArray = new Gson().toJson(logsToConvert);
        JSONArray array = new JSONArray(jsonArray);

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("LogStats");
        Row time = sheet.createRow(0);


        for(int i = 0; i < logsToConvert.size(); i++){
            time.createCell(0).setCellValue("Date:");
            time.createCell(i + 1).setCellValue(logsToConvert.get(i).timeStamp);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }
        Row logger = sheet.createRow(1);
        for(int i = 0; i < logsToConvert.size(); i++){
            logger.createCell(0).setCellValue("Logger Name:");
            logger.createCell(i + 1).setCellValue(logsToConvert.get(i).logger);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }
        Row levels = sheet.createRow(2);
        for(int i = 0; i < logsToConvert.size(); i++){
            levels.createCell(0).setCellValue("Logger Level:");
            levels.createCell(i + 1).setCellValue(logsToConvert.get(i).level);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }
        Row threads = sheet.createRow(3);
        for(int i = 0; i < logsToConvert.size(); i++){
            threads.createCell(0).setCellValue("Log threads:");
            threads.createCell(i + 1).setCellValue(logsToConvert.get(i).thread);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }

        Row nameCount = sheet.createRow(4);
        for(int i = 0; i < logsToConvert.size(); i++){
            int count = 0;
            for(int j = 0; j < array.length(); j++) {
                JSONObject element = array.getJSONObject(j);
                String logName = element.getString("logger");
                if(logName.equals(logsToConvert.get(i).getLogger())) {
                    count ++;
                }
            }
            nameCount.createCell(0).setCellValue("Logger Count:");
            nameCount.createCell(i + 1).setCellValue(count);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }

        Row levelCount = sheet.createRow(5);
        for(int i = 0; i < logsToConvert.size(); i++){
            int count = 0;
            for(int j = 0; j < array.length(); j++) {
                JSONObject element = array.getJSONObject(j);
                String logName = element.getString("level");
                if(logName.equals(logsToConvert.get(i).getLevel())) {
                    count ++;
                }
            }
            levelCount.createCell(0).setCellValue("Level Count:");
            levelCount.createCell(i + 1).setCellValue(count);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }

        Row threadCount = sheet.createRow(6);
        for(int i = 0; i < logsToConvert.size(); i++){
            int count = 0;
            for(int j = 0; j < array.length(); j++) {
                JSONObject element = array.getJSONObject(j);
                String logName = element.getString("thread");
                if(logName.equals(logsToConvert.get(i).getThread())) {
                    count ++;
                }
            }
            threadCount.createCell(0).setCellValue("Thread Count:");
            threadCount.createCell(i + 1).setCellValue(count);
            sheet.autoSizeColumn(i + 1);
            sheet.autoSizeColumn(0);
        }


        ServletOutputStream stream = resp.getOutputStream();
        workbook.write(stream);
        workbook.close();

    }

}
