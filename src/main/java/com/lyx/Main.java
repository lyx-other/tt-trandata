package com.lyx;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.JsonNode;
import com.lyx.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main
{
    /**
     * 要生成的文件路径
     */
    public static final String DEST_FILE_PATH = "/Users/lgf/my-dir/download/result/" + IdUtil.fastSimpleUUID() + ".xlsx";

    /**
     * 模板文件路径
     */
    public static final String TEMPLATE_FILE_PATH = "/Users/lgf/my-dir/software/cache/dropbox/Dropbox/电脑数据/事务/他人事务/婷婷-数据转换/程序读取文件/模板文件.xlsx";

    /**
     * 要读取的文件路径
     */
    public static final String FILE_PATH = "/Users/lgf/my-dir/software/cache/dropbox/Dropbox/电脑数据/事务/他人事务/婷婷-数据转换/程序读取文件/原文件-Java读取版.xlsx";

    public static final RestTemplate REST_TEMPLATE = new RestTemplate();

    public static void main(String[] args)
    {
        List<List<Map<String, Object>>> excelDataList = readExcel();
        List<Student> studentList = excelDataList.stream().map(Main::oneRowList2String).collect(Collectors.toList());

        FileUtil.mkParentDirs(DEST_FILE_PATH);
        EasyExcel.write(DEST_FILE_PATH).withTemplate(TEMPLATE_FILE_PATH).sheet().doFill(studentList);
    }

    /**
     * 读取excel数据
     * @return excel数据
     */
    public static List<List<Map<String, Object>>> readExcel()
    {
        ExcelReader reader = ExcelUtil.getReader(FILE_PATH);
        List<Map<String, Object>> excelDataList = reader.readAll();
        List<List<Map<String, Object>>> rowGroupList = CollUtil.split(excelDataList, 3);

        return rowGroupList;
    }

    /**
     * 三行数据转换为一个学生
     * @param rowList 行数据
     * @return 一个学生
     */
    public static Student oneRowList2String(List<Map<String, Object>> rowList)
    {
        Student student = Student.newInstance();
        student.setName(Convert.toStr(rowList.get(0).get("name")));
        student.setSex(Convert.toStr(rowList.get(0).get("sex")));
        student.setIdCard(Convert.toStr(rowList.get(0).get("idcard")));

        // 居住地址
        {
            Map<String, Object> address = rowList.stream().filter(el -> StrUtil.equals(Convert.toStr(el.get("addressType")), "现住址")).findFirst().get();
            if (!StrUtil.equals(Convert.toStr(address.get("addressDetail")), "无"))
            {
                StringBuilder builder = new StringBuilder();
                builder.append(address.get("countary"))
                        .append(address.get("zhen"))
                        .append(address.get("addressDetail"));
                String[] threeData = getAreaByAddress(builder.toString());
                student.setProvince1(threeData[0]);
                student.setCity1(threeData[1]);
                student.setCon1(threeData[2]);
                student.setStreet1(Convert.toStr(address.get("zhen")));
                student.setAddreddDetal1(Convert.toStr(address.get("addressDetail")));
            }
        }

        // 户籍地址
        {
            Map<String, Object> address = rowList.stream().filter(el -> StrUtil.equals(Convert.toStr(el.get("addressType")), "户籍住址")).findFirst().get();
            if (!StrUtil.equals(Convert.toStr(address.get("addressDetail")), "无"))
            {
                StringBuilder builder = new StringBuilder();
                builder.append(address.get("countary"))
                        .append(address.get("zhen"))
                        .append(address.get("addressDetail"));
                String[] threeData = getAreaByAddress(builder.toString());
                student.setProvince2(threeData[0]);
                student.setCity2(threeData[1]);
                student.setCon2(threeData[2]);
                student.setStreet2(Convert.toStr(address.get("zhen")));
                student.setAddreddDetal2(Convert.toStr(address.get("addressDetail")));
            }
        }

        // 暂地址
        {
            Map<String, Object> address = rowList.stream().filter(el -> StrUtil.equals(Convert.toStr(el.get("addressType")), "暂住址")).findFirst().get();
            if (!StrUtil.equals(Convert.toStr(address.get("addressDetail")), "无"))
            {
                StringBuilder builder = new StringBuilder();
                builder.append(address.get("countary"))
                        .append(address.get("zhen"))
                        .append(address.get("addressDetail"));
                String[] threeData = getAreaByAddress(builder.toString());
                student.setProvince3(threeData[0]);
                student.setCity3(threeData[1]);
                student.setCon3(threeData[2]);
                student.setStreet3(Convert.toStr(address.get("zhen")));
                student.setAddreddDetal3(Convert.toStr(address.get("addressDetail")));
            }
        }

        // 共同居住人
        List<OtherPeople> otherPeopleList = rowList.stream()
                                                    .flatMap
                                                    (
                                                        el ->
                                                        {
                                                            OtherPeople otherPeople1 = new OtherPeople(Convert.toStr(el.get("fname")), Convert.toStr(el.get("fcardNumber")), Convert.toStr(el.get("fphoneNumber")));
                                                            OtherPeople otherPeople2 = new OtherPeople(Convert.toStr(el.get("sname")), Convert.toStr(el.get("scardNumber")), Convert.toStr(el.get("sphoneNumber")));
                                                            return CollUtil.newArrayList(otherPeople1, otherPeople2).stream();
                                                        }
                                                    )
                                                    .collect(Collectors.toList());
        for (int i = 0; i <= otherPeopleList.size()-1; i++)
        {
            OtherPeople thePeople = otherPeopleList.get(i);
            ReflectUtil.setFieldValue(student, "pname"+(i+1), thePeople.getName());
            ReflectUtil.setFieldValue(student, "pcard"+(i+1), thePeople.getCard());
            ReflectUtil.setFieldValue(student, "phone"+(i+1), thePeople.getPhone());
        }

        return student;
    }

    /**
     * 详细地址
     * @param address 详细地址
     * @return 省市区
     */
    private static String[] getAreaByAddress(String address)
    {
        String url = "https://restapi.amap.com/v3/geocode/geo?address={address}&output=JSON&key=a23b77b278ece3a9103bd800a4e5fff2";

        JsonNode body;
        try
        {
            body = REST_TEMPLATE.getForObject(url, JsonNode.class, address);
        }
        catch (Exception e)
        {
            return new String[]{StrUtil.EMPTY, StrUtil.EMPTY, StrUtil.EMPTY};
        }
        if (Objects.isNull(body.get("count")) || body.get("count").asInt() == 0)
        {
            return new String[]{StrUtil.EMPTY, StrUtil.EMPTY, StrUtil.EMPTY};
        }

        return new String[]{
                body.get("geocodes").get(0).get("province").asText(),
                body.get("geocodes").get(0).get("city").asText(),
                body.get("geocodes").get(0).get("district").asText()};
    }

    @Data
    @AllArgsConstructor
    public static class OtherPeople
    {
        private String name;

        private String card;

        private String phone;
    }
}
