package com.personal.sirestproxy.components;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.personal.sirestproxy.service.DailyMenu;
import com.personal.sirestproxy.service.ApiService;


@Component
public class DailyAct {

    @Autowired
    DailyMenu dailyMenu;
    @Autowired
    ApiService apiService;


    @Scheduled(cron = "0 0 0 * * *", zone = "America/Bogota")
    public void actualizarValor() {
        ZonedDateTime today= ZonedDateTime.now(ZoneId.of("America/Bogota"));
        int day=today.getDayOfMonth();
        int month=today.getMonthValue();
        dailyMenu.setLunchMenu(apiService.sirest(month, day, 2));
        dailyMenu.setDinnerMenu(apiService.sirest(month, day, 3));
    }    
}
