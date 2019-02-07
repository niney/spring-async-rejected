package niney.async.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@EnableAsync
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Configuration
    public static class AsyncConfiguration implements AsyncConfigurer {

        private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

        @Override
        @Bean(name = "taskExecutor")
        public Executor getAsyncExecutor() {
            log.debug("Creating Async Task Executor");
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(5);
            executor.setQueueCapacity(10);
            executor.setThreadNamePrefix("Niney-Executor-");
            return new ExceptionHandlingAsyncTaskExecutor(executor);
        }

        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return new SimpleAsyncUncaughtExceptionHandler();
        }
    }

    @RestController
    @RequestMapping("/api")
    public static class AsyncController {

        private AsyncService asyncService;

        public AsyncController(AsyncService asyncService) {
            this.asyncService = asyncService;
        }

        @GetMapping("/hello")
        public String hello() {
            asyncService.asyncMethodWithReturnType();
            return "hello";
        }
    }

    @Service
    public static class AsyncService {

        private final Logger log = LoggerFactory.getLogger(getClass());

        @Async
        public Future<String> asyncMethodWithReturnType() {
            System.out.println("Execute method asynchronously - "
                    + Thread.currentThread().getName());
            try {
                Thread.sleep(10000);
                return new AsyncResult<String>("hello world !!!!");
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            return null;
        }

    }

}

