package com.personal.sirestproxy.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.personal.sirestproxy.service.ApiService;
import com.personal.sirestproxy.service.DailyMenu;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    ApiService apiService;
    @Autowired
    DailyMenu dailyMenu;

    @PostConstruct
    public void init() {
        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        int day = today.getDayOfMonth();
        int month = today.getMonthValue();
        Object lunch = apiService.sirest(month, day, 2);
        if (lunch != null) {
            dailyMenu.setLunchMenu(lunch);
            Object dinner = apiService.sirest(month, day, 3);
            dailyMenu.setDinnerMenu(dinner);
        }
    }

    @GetMapping("getMenu")
    public ResponseEntity<Object> getItem(@RequestParam int month, @RequestParam int day, @RequestParam int type,
            @RequestHeader(value = "Origin", required = false) String origin) {
        if (origin == null) {
            return null;
        }
        ResponseEntity<Object> res = getMenu(month, day, type);
        return res;
    }

    private ResponseEntity<Object> getMenu(int month, int day, int type){
        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        int day2=today.getDayOfMonth();
        int month2=today.getMonthValue();
        if(day2 == day && month2 == month && type == 2) {
            Object res=dailyMenu.getLunchMenu();
            if(res==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fecha sin menu");
            }else{
                return ResponseEntity.ok(res);
            }
        } else if (day2 == day && month2 == month && type == 3) {
            Object res=dailyMenu.getDinnerMenu();
            if(res==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fecha sin menu");
            }else{
                return ResponseEntity.ok(res);
            }
        } else {
            Object res=apiService.sirest(month, day, type);
            if(res==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fecha sin menu");
            }else{
                return ResponseEntity.ok(res);
            }
        }
    }
}
