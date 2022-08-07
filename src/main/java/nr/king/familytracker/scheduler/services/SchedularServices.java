package nr.king.familytracker.scheduler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.repo.UpdateNotificationRepo;
import nr.king.familytracker.utils.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SchedularServices {
    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;


    @Autowired
    private UpdateNotificationRepo updateNotificationRepo;

    private static final Logger logger = LogManager.getLogger(SchedularServices.class);


    @PostConstruct
    public void dotheMethod() {
        demoServiceMethod();
    }

    @Scheduled(cron = "0 */3 * * *")
    public void demoServiceMethod() {
        try {
            new Thread(()->updateNotificationRepo.doPushNotifcation()).start();
        } catch (Exception exception) {
            logger.error("Exception in schedualr " + exception.getMessage(), exception);
        }
    }






}
