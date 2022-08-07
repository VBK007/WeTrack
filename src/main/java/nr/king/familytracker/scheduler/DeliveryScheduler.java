package nr.king.familytracker.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryScheduler {

    private final static Logger logger = LogManager.getLogger(DeliveryScheduler.class);

    @Autowired
    private Scheduler scheduler;



    private JobDetail buildJobDetail(String task, Long userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("task", task);
        jobDataMap.put("userId", userId);
        return JobBuilder.newJob(CronJob.class)
                .usingJobData(jobDataMap)
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, String task, Long userId, String cronString) {
        return TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                .forJob(jobDetail)
                .withIdentity(new TriggerKey(userId + "_" + task))
                .build();
    }
}
