package com.example.application.views.main;

import com.example.application.model.dto.IpDTO;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@PageTitle("Main")
@Route(value = "")
public class MainView extends VerticalLayout {

    private static final byte MAX_THREADS = 5;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
//    private final Grid<TaskItem> grid = new Grid<>(TaskItem.class);

    public MainView() {
        Button ipButton = getIpButton();

        setMargin(true);
        setHorizontalComponentAlignment(Alignment.START, ipButton);

        add(ipButton);
    }

    private Button getIpButton() {
        final UI ui = UI.getCurrent();
        Button ipButton = new Button("My IP");

        AtomicInteger orderIndex = new AtomicInteger();

        ipButton.addClickListener(_e -> {
            int orderThread = orderIndex.getAndIncrement();
            openBeginNotification(orderThread + 1);

            executor.submit(() -> {
                try {
                    long sleepTime = (long) (Math.random() * (10 - 5) + 5);

                    Thread.sleep(sleepTime * 1000);

                    IpDTO ip = restTemplate.getForObject("http://ip.jsontest.com/", IpDTO.class);
                    System.out.printf("%d: %s\n", orderThread, ip);

                    ui.access(() -> getFinishNotification(orderThread + 1).open());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        return ipButton;
    }

    private void openBeginNotification(int taskNumber) {
        Notification notification;
        if (executor.getActiveCount() == MAX_THREADS) {
            // thread add to front
            notification = getWaitNotification(taskNumber);
        }  else {
            // thread run
            notification = getRunNotification(taskNumber);
        }
        notification.open();
    }

    private Notification getRunNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": Běží", NotificationVariant.LUMO_PRIMARY);
    }

    private Notification getWaitNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": Zařazena do fronty", NotificationVariant.LUMO_CONTRAST);
    }

    private Notification getFinishNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": Skončila", NotificationVariant.LUMO_SUCCESS);
    }

    private Notification getNotification(String notificationText, NotificationVariant variant) {
        Notification notification = new Notification(notificationText, 1000);
        notification.addThemeVariants(variant);

        return notification;
    }
}
