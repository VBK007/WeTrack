package nr.king.familytracker.scheduler;

import akka.actor.ActorSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;

@Service
public class CronJob extends QuartzJobBean {

    @Autowired
    private ApplicationContext applicationContext;

    private ActorSystem actorSystem;

    private final static Logger logger = LogManager.getLogger(CronJob.class);

    @PostConstruct
    public void postConstruct() throws URISyntaxException, InstantiationException {
        actorSystem = applicationContext.getBean(ActorSystem.class);
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        Long userId = mergedJobDataMap.getLong("userId");
        String task = mergedJobDataMap.getString("task");
    }
}
