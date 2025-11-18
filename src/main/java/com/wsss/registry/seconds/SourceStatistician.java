package com.wsss.registry.seconds;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wsss.monitor.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * source统计员
 */
@Slf4j
@Component
public class SourceStatistician {
    @Value("${seconds.monitor.size:3}")
    private int size = 3;
    @Value("${seconds.monitor.send.seconds:6}")
    private int sendSeconds;
    @Value("${seconds.monitor.machine.millis:60000}")
    private int machineMillis;
    @Value("${seconds.monitor.url:}")
    private String monitorUrl;
    private int n;
    private AtomicReferenceArray<SecondData> secondDataLinkedList = new AtomicReferenceArray<>((int) Math.pow(2, size));
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("monitor-thread").build());

    @PostConstruct
    public void init() {
        secondDataLinkedList = new AtomicReferenceArray<>((int) Math.pow(2, size));
        n = (int) Math.pow(2, size) - 1;
        executorService.scheduleWithFixedDelay(() -> {
            Monitor.TimeContext context = Monitor.timer("seconds_monitor_send");
            try {
                if(monitorUrl == null || monitorUrl.equals("")) {
                    return;
                }
                List<String> list = createInfluxData();
                for (String s : list) {
                    sendHttp(s);
                }
            }catch (Exception e) {
                log.error("createInfluxData error:", e);
            } finally {
                context.end();
            }
        }, sendSeconds, sendSeconds, TimeUnit.SECONDS);
    }

    /**
     * 记录
     */
    public void record(String key, String tag, double count, long totalTime) {
        try {
            SecondData secondData = createOrGet();
            SourceData sourceData = secondData.get(key, tag);
            if (sourceData == null) {
                sourceData = secondData.put(key, tag, new SourceData());
            }
            sourceData.record(count, totalTime);
        } catch (Exception e) {
            log.error("record error.request:", e);
        }
    }

    private SecondData createOrGet() {
        long nowTime = System.currentTimeMillis() / 1000;
        int index = (int) (nowTime & n);
        SecondData currentSecondData = secondDataLinkedList.get(index);
        while (currentSecondData == null || currentSecondData.getTime() != nowTime) {
            SecondData tmp = new SecondData(nowTime);
            secondDataLinkedList.compareAndSet(index, currentSecondData, tmp);
            currentSecondData = secondDataLinkedList.get(index);
        }
        return currentSecondData;
    }


    /**
     * 数据拉取
     *
     * @return List<SecondData>
     */
    public AtomicReferenceArray<SecondData> pull() {
        return secondDataLinkedList;
    }

    public void sendHttp(String data) {
        try {
            // 创建 URL 对象
            URL url = new URL(monitorUrl);
            // 打开连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置请求方法
            conn.setRequestMethod("POST");
            // 设置请求头
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            // 允许输出
            conn.setDoOutput(true);
            // 写入数据
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes("UTF-8"));
                os.flush();
            }

            // 处理响应
            int responseCode = conn.getResponseCode();

            // 读取响应
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            // 断开连接
            conn.disconnect();
        } catch (Exception e) {
            log.error("record error data:{}", data, e);
        }
    }

    public List<String> createInfluxData() {
        Long secondTime = System.currentTimeMillis() / 1000;
        List<String> list = new ArrayList<>(secondDataLinkedList.length());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secondDataLinkedList.length(); i++) {
            SecondData secondData = secondDataLinkedList.get(i);
            if (secondData == null || secondTime == secondData.getTime()) continue;

            for (Map.Entry<String, SourceData> entry : secondData.getData().entrySet()) {
                String key = entry.getKey();
                SourceData sourceData = entry.getValue();
                append(sb, key, null, secondData, sourceData);

            }
            for (Map.Entry<String, ConcurrentHashMap<String, SourceData>> entry : secondData.getTagData().entrySet()) {
                String key = entry.getKey();
                for (Map.Entry<String, SourceData> entry1 : entry.getValue().entrySet()) {
                    String tag = entry1.getKey();
                    SourceData sourceData = entry1.getValue();
                    append(sb, key, tag, secondData, sourceData);
                }
            }
            list.add(sb.toString());
            sb.setLength(0);
        }
        return list;
    }

    private void append(StringBuilder sb, String key, String tag, SecondData secondData, SourceData sourceData) {
        sb.append(key).append(",").append("host=").append(Monitor.IP);;
        if (tag != null) {
            sb.append(",").append("tag=").append(tag);
        }
        sb.append(" ").append("tps=").append(sourceData.getCounter());
        long max = sourceData.getMax();
        if (max > 0) {
            sb.append(",")
                    .append("tp99=").append(sourceData.getTP99()).append(",")
                    .append("tp90=").append(sourceData.getTP90()).append(",")
                    .append("tp50=").append(sourceData.getTP50()).append(",")
                    .append("max=").append(sourceData.getMax());
        }
        sb.append(" ").append(TimeUnit.SECONDS.toNanos(secondData.getTime())).append("\n");
    }


    public static void main(String[] args) {
        SourceStatistician sourceStatistician = new SourceStatistician();
        sourceStatistician.monitorUrl = "http://10.48.2.15:8086/write?db=monitor";
        String s = "seconds_system_cpu_used,host=10.205.9.121 tps=10,tp99=10,tp90=10,tp50=5,max=10 1733986663000000000\n";
        sourceStatistician.sendHttp(s);
        sourceStatistician.sendHttp(s);
    }
}
