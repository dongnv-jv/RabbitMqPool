package vn.vnpay.demo.exception;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RejectedExecution implements RejectedExecutionHandler {
    private final Logger logger = LoggerFactory.getLogger(RejectedExecution.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.info(" {} is Rejected", r.toString());

    }
}
