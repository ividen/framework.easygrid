package com.kwanza.easygrid.map;

/*
 * #%L
 * easygrid
 * %%
 * Copyright (C) 2015 Kwanza
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Alexander Guzanov
 */
public class BigObject implements Serializable {
    private String sessionId;
    private Long menuId;
    private Long[] menuItems;
    private Integer page;
    private Boolean isLastPage;
    private Long paymentId;
    private List<String> params;

    transient final Random rnd = new Random();

    public BigObject() {
        sessionId = System.currentTimeMillis() + "";
        menuId = rnd.nextLong();
        int cnt = rnd.nextInt(2000);
        menuItems = new Long[cnt];
        for (int i = 0; i < cnt; i++) {
            menuItems[i] = rnd.nextLong();
        }
        page = rnd.nextInt();
        paymentId = rnd.nextLong();
        cnt = rnd.nextInt(50);
        params = new ArrayList<String>(cnt);
        for (int i = 0; i < cnt; i++) {
            params.add("" + rnd.nextLong());
        }

    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Long[] getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(Long[] menuItems) {
        this.menuItems = menuItems;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Boolean getLastPage() {
        return isLastPage;
    }

    public void setLastPage(Boolean lastPage) {
        isLastPage = lastPage;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
