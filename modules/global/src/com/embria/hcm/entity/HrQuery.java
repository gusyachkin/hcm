package com.embria.hcm.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Column;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|name")
@Table(name = "HCM_HR_QUERY")
@Entity(name = "hcm$HrQuery")
public class HrQuery extends StandardEntity {
    private static final long serialVersionUID = -6386822771055578831L;

    @Column(name = "NAME")
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



}