package vn.vnpay.demo.config.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo.common.PropertiesFactory;
import vn.vnpay.demo.exception.RejectedExecution;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPoolConfig {
    private final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);
    private final Executor executor;

    public ThreadPoolConfig() {
        int corePoolSize = 5;
        int maximumPoolSize = 10;
        long keepAliveTime = 30;
        int queueCapacity = 100;
        TimeUnit unit = TimeUnit.SECONDS;
        try {
            corePoolSize = Integer.parseInt(PropertiesFactory.getFromProperties("thead.pool.corePoolSize"));
            maximumPoolSize = Integer.parseInt(PropertiesFactory.getFromProperties("thead.pool.maximumPoolSize"));
            keepAliveTime = Integer.parseInt(PropertiesFactory.getFromProperties("thead.pool.keepAliveTime"));
            queueCapacity = Integer.parseInt(PropertiesFactory.getFromProperties("thead.pool.queueCapacity"));
        } catch (Exception e) {
            logger.error("Can not read value for ThreadPoolConfig from resource with root cause ", e);
            logger.info("Parameters of ThreadPoolConfig are used with default values ");
        }
//        ThreadFactory threadFactory  =  new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread t = new Thread(r);
//
//                t.setUncaughtExceptionHandler((t1, e) -> LoggerFactory.getLogger(t1.getName()).error(e.getMessage(), e));
//
//                return t;
//            }
//        };
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueCapacity);
        executor = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                new RejectedExecution());
    }

    private static final class InstanceHolder {
        private static final ThreadPoolConfig instance = new ThreadPoolConfig();
    }

    public static Executor getExecutor() {
        ThreadPoolConfig instance = ThreadPoolConfig.InstanceHolder.instance;
        return instance.executor;
    }

}
