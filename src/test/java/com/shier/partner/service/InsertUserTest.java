package com.shier.partner.service;

import com.shier.partner.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: shier
 * @date: 2023/02/04
 * 用户插入单元测试，注意打包时要删掉或忽略，不然打一次包就插入一次
 */

@SpringBootTest
public class InsertUserTest {

    @Resource
    private UserService userService;

    //线程设置
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * *
     * 循环插入用户 耗时：7260ms
     * *批量插入用户 1000000 耗时： 193747ms
     * 第一次：193747ms 3分十四秒
     * 第二次：352862ms
     * 第三次：172341
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假shier");
            user.setUserAccount("shier");
            user.setAvatarUrl("https://c-ssl.dtstatic.com/uploads/blog/202101/11/20210111220519_7da89.thumb.1000_0.jpeg");
            user.setProfile("fat cat");
            user.setGender(1);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("22288999@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("33322");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }

    /**
     * 并发批量插入用户1000000耗时：26830ms
     * 第一次：11106ms
     * 第三次：8392
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假shier");
                user.setUserAccount("shier");
                user.setAvatarUrl("https://c-ssl.dtstatic.com/uploads/blog/202101/11/20210111220519_7da89.thumb.1000_0.jpeg");
                user.setProfile("fat cat");
                user.setGender(1);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("22288999@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("33322");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}


