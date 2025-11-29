package com.personal.sirestproxy.service;

import org.springframework.stereotype.Service;

@Service 
public class DailyMenu {

    private Object lunchMenu;

    private Object dinnerMenu;

    public Object getLunchMenu() {
        return lunchMenu;
    }
    
    public Object getDinnerMenu() {
        return dinnerMenu;
    }

    public void setLunchMenu(Object lunchMenu) {
        this.lunchMenu = lunchMenu;
    }

    public void setDinnerMenu(Object dinnerMenu) {
        this.dinnerMenu = dinnerMenu;
    }

}
