package com.example.application.views.main;

import com.example.application.model.dto.IpDTO;
import com.example.application.shared.enums.ETaskState;
import com.example.application.shared.items.TaskItem;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@PageTitle("Main")
@Route(value = "")
public class MainView extends VerticalLayout {

    private static final byte MAX_THREADS = 5;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
    private final Grid<TaskItem> grid = new Grid<>(TaskItem.class, false);
    private ArrayList<TaskItem> tasks = new ArrayList<>();
    final UI ui = UI.getCurrent();

    public MainView() {
        Button ipButton = getIpButton();

        setMargin(true);
        configureGrid();

        grid.setItems(tasks);

        add(ipButton, grid);
    }

    private Button getIpButton() {
        Button ipButton = new Button("My IP");

        AtomicInteger orderIndex = new AtomicInteger();

        ipButton.addClickListener(_e -> {
            int taskNumber = orderIndex.incrementAndGet();
            openBeginNotification(taskNumber);

            executor.submit(() -> {
                try {
                    // update task state
                    tasks.stream()
                            .filter(taskItem -> taskItem.getTaskNumber() == taskNumber)
                            .findFirst()
                            .ifPresent(taskItem -> {
                                System.out.println(taskItem);
                                taskItem.setTaskState(ETaskState.RUN);
                            });
                    updateGrid();

                    long sleepTime = (long) (Math.random() * (10 - 5) + 5);

                    Thread.sleep(sleepTime * 1000);

                    IpDTO ip = restTemplate.getForObject("http://ip.jsontest.com/", IpDTO.class);
                    System.out.printf("%d: %s\n", taskNumber, ip);

                    tasks.removeIf(taskItem -> taskItem.getTaskNumber() == taskNumber);

                    ui.access(() -> {
                        getFinishNotification(taskNumber).open();
                    });
                    updateGrid();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        return ipButton;
    }

    private void configureGrid() {
        grid.addColumn(TaskItem::getTaskNumber).setHeader("Task number");
        grid.addColumn(taskItem -> taskItem.getTaskState().getCzText()).setHeader("State");
    }

    private void openBeginNotification(int taskNumber) {
        Notification notification;
        if (executor.getActiveCount() == MAX_THREADS) {
            // thread add to front
            notification = getWaitNotification(taskNumber);
            tasks.add(new TaskItem(taskNumber, ETaskState.WAIT));
        }  else {
            // thread run
            notification = getRunNotification(taskNumber);
            tasks.add(new TaskItem(taskNumber, ETaskState.RUN));
        }
        notification.open();
        updateGrid();
    }

    private void updateGrid() {
        ui.access(() -> grid.setItems(tasks));
    }

    private Notification getRunNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": " + ETaskState.RUN.getCzText(), NotificationVariant.LUMO_PRIMARY);
    }

    private Notification getWaitNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": " + ETaskState.WAIT.getCzText(), NotificationVariant.LUMO_CONTRAST);
    }

    private Notification getFinishNotification(int taskNumber) {
        return getNotification("Úloha " + taskNumber + ": " + ETaskState.FINISH.getCzText(), NotificationVariant.LUMO_SUCCESS);
    }

    private Notification getNotification(String notificationText, NotificationVariant variant) {
        Notification notification = new Notification(notificationText, 1000);
        notification.addThemeVariants(variant);

        return notification;
    }
}
