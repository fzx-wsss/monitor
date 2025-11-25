package com.wsss.registry.seconds;

import com.wsss.monitor.utils.IOUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@RestController
public class MonitorController {

    @Resource
    private SourceStatistician sourceStatistician;

    @RequestMapping(value = "/seconds/monitor", method = RequestMethod.GET)
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<String> secondDataList = sourceStatistician.createInfluxData();
            PrintWriter out = resp.getWriter();
            for (String s : secondDataList) {
                out.write(s);
                out.flush();
            }
            out.close();
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            IOUtils.close(resp.getWriter());
        }
    }
}
