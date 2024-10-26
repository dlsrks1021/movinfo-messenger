package com.movinfo.messenger.model;

import java.util.Date;
import java.util.List;

public class Screen {
    private Date screenDate;
    private List<String> screentypes;
    public static String[] SCREEN_TYPE_LIST = {
        "SCREENX",
        "TEMPUR CINEMA",
        "GOLD CLASS",
        "IMAX",
        "4DX",
        "2D"
    };
    
    public Screen(Date screenDate, List<String> screentypes){
        this.screenDate = screenDate;
        this.screentypes = screentypes;
    }

    public Date getScreenDate() {
        return screenDate;
    }
    public void setScreenDate(Date screenDate) {
        this.screenDate = screenDate;
    }
    public List<String> getScreentypes() {
        return screentypes;
    }
    public void addScreentype(String screentype) {
        screentypes.add(screentype);
    }
}
