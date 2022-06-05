package com.example.application.views.main;

import com.example.application.model.dto.IpDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@PageTitle("Main")
@Route(value = "")
public class MainView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public MainView() {
        Button ipButton = getIpButton();

        setMargin(true);
        setHorizontalComponentAlignment(Alignment.START, ipButton);

        add(ipButton);
    }

    private Button getIpButton() {
        Button ipButton = new Button("My IP");
        AtomicInteger orderIndex = new AtomicInteger();

        ipButton.addClickListener(_e -> {
            executor.submit(() -> {
                try {
                    int orderNumber = orderIndex.getAndIncrement();
                    int sleepTime = (int) (Math.random() * (10 - 5) + 5);
                    System.out.printf("%d: %ds\n", orderNumber, sleepTime);

                    Thread.sleep(sleepTime * 1000L);

                    IpDTO ip = restTemplate.getForObject("http://ip.jsontest.com/", IpDTO.class);
                    System.out.printf("%d: %s\n", orderNumber, ip);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        return ipButton;
    }
}
