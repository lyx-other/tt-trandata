package com.lyx.entity;

import cn.hutool.extra.tokenizer.engine.analysis.AnalysisResult;
import lombok.Data;

@Data
public class Student
{
    private String name;

    private String sex;

    private String phone;

    private String idCard;

    private String province1;

    private String city1;

    private String con1;

    private String street1;

    private String addreddDetal1;

    private String province2;

    private String city2;

    private String con2;

    private String street2;

    private String addreddDetal2;

    private String province3;

    private String city3;

    private String con3;

    private String street3;

    private String addreddDetal3;

    private String pname1;

    private String pcard1;

    private String phone1;

    private String pname2;

    private String pcard2;

    private String phone2;

    private String pname3;

    private String pcard3;

    private String phone3;

    private String pname4;

    private String pcard4;

    private String phone4;

    private String pname5;

    private String pcard5;

    private String phone5;

    private String pname6;

    private String pcard6;

    private String phone6;

    private Student()
    {
    }

    public static Student newInstance()
    {
        return new Student();
    }
}
