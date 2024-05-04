package org.reploop.beancount.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.Map;

public class AccountMapping {

    private static final String mapping = """
            {
                "assets": {
                    "招商银行(5407)": "Assets:Card:CMB:2459",
                    "招商银行储蓄卡(2459)": "Assets:Card:CMB:2459",
                    "零钱": "Assets:WeChat:Pocket"
                },
                "expenses": {
                        "DEFAULT": "Expenses:DailyNecessities",
                        "余额宝-自动转入": "Assets:Alipay:Yuebao",
                        "水滴筹": "Expenses:",
                        "亿方物业": "Expenses:RealEstate",
                        "交通出行": "Expenses:Transportation",
                        "北京一卡通": "Expenses:Transportation",
                        "商业服务": "Expenses:BusinessService",
                        "天眼查": "Expenses:BusinessService",
                        "顺丰速运": "Expenses:BusinessService",
                        "信用借还": "Expenses:Repayment",
                        "京东商城平台商户": "Expenses:Repayment",
                        "充值缴费": "Expenses:Payment",
                        "医疗健康": "Expenses:HealthCare",
                        "医院": "Expenses:HealthCare",
                        "微保": "Expenses:HealthCare",
                        "急诊抢救中心": "Expenses:HealthCare",
                        "医学检验": "Expenses:HealthCare",
                        "投资理财": "Expenses:Investment",
                        "文化休闲": "Expenses:CultureLeisure",
                        "圆明园": "Expenses:CultureLeisure",
                        "亚马逊电子书包月服务": "Expenses:CultureLeisure:Books",
                        "日用百货": "Expenses:DailyNecessities",
                        "美宜佳": "Expenses:DailyNecessities",
                        "超市": "Expenses:DailyNecessities",
                        "便利店": "Expenses:DailyNecessities",
                        "LAWSON": "Expenses:DailyNecessities",
                        "柒一拾壹": "Expenses:DailyNecessities",
                        "欢朋商场": "Expenses:DailyNecessities",
                        "收款方备注:二维码收款": "Expenses:DailyNecessities",
                        "爱车养车": "Expenses:CarCare",
                        "停车费": "Expenses:CarCare",
                        "停车缴费": "Expenses:CarCare",
                        "好停车服务": "Expenses:CarCare",
                        "瑞海花园客服中心": "Expenses:CarCare",
                        "生活服务": "Expenses:LifeServices",
                        "转账红包": "Expenses:MoneyTransfer",
                        "酒店旅游": "Expenses:HotelTravel",
                        "天时同城": "Expenses:HotelTravel",
                        "临春岭": "Expenses:HotelTravel",
                        "云港科技": "Expenses:HotelTravel",
                        "皇包车": "Expenses:HotelTravel",
                        "三亚顶悦要客旅行有限公司": "Expenses:HotelTravel",
                        "三亚凤凰岭文化旅游有限公司": "Expenses:HotelTravel",
                        "三亚迎朋酒店公寓管理有限公司": "Expenses:HotelTravel",
                        "北京市香山公园": "Expenses:HotelTravel",
                        "餐饮美食": "Expenses:FoodBeverage",
                        "和易副食商店": "Expenses:FoodBeverage",
                        "熟食": "Expenses:FoodBeverage",
                        "撒拉花儿": "Expenses:FoodBeverage",
                        "主食厨房": "Expenses:FoodBeverage",
                        "包子": "Expenses:FoodBeverage",
                        "美团平台商户": "Expenses:FoodBeverage",
                        "美团收银": "Expenses:FoodBeverage",
                        "魏学磊": "Expenses:FoodBeverage",
                        "肯德基": "Expenses:FoodBeverage",
                        "餐饮店": "Expenses:FoodBeverage",
                        "望京小腰": "Expenses:FoodBeverage",
                        "餐厅": "Expenses:FoodBeverage",
                        "肉夹馍": "Expenses:FoodBeverage",
                        "烤串": "Expenses:FoodBeverage",
                        "农夫山泉": "Expenses:FoodBeverage",
                        "汉堡王": "Expenses:FoodBeverage",
                        "美团/大众点评点餐订单": "Expenses:FoodBeverage:Takeaway",
                        "成都阳光颐和物业管理有限公司三亚分公司": "Expenses:FoodBeverage",
                        "舌尖上的嘿小面": "Expenses:FoodBeverage",
                        "汤面饭": "Expenses:FoodBeverage",
                        "火锅": "Expenses:FoodBeverage",
                        "饺子": "Expenses:FoodBeverage",
                        "牛奶": "Expenses:FoodBeverage",
                        "米粉": "Expenses:FoodBeverage",
                        "和番丼饭": "Expenses:FoodBeverage",
                        "拉面": "Expenses:FoodBeverage",
                        "水果": "Expenses:FoodBeverage",
                        "星巴克": "Expenses:FoodBeverage:Coffee",
                        "天猫超市": "Expenses:DailyNecessities",
                        "售货机": "Expenses:DailyNecessities",
                        "耳机": "Expenses:DigitalEquipment:Audio",
                        "火车票": "Expenses:Transportation:Railway",
                        "打车": "Expenses:Transportation:Taxi",
                        "王鑫Sherry": "Assets:Receivables:WangXinSherry",
                        "朴老师": "Assets:Receivables:Design",
                        "PtrkTao": "Assets:Receivables:Design",
                        "EZFIX沈晓明": "Expenses:DigitalAppliances",
                        "美团充电宝": "Expenses:DigitalAppliances",
                        "腾讯云费用账户": "Expenses:CloudService",
                        "转账备注": "Expenses:Other",
                        "光辉岁月": "Expenses:Other",
                        "张玉荣": "Expenses:OutdoorSports",
                        "水": "Expenses:OutdoorSports",
                        "发出群红包": "Expenses:RedPacket:WeChat"
                },
                "income": {
                },
                "liabilities": {
                    "中信银行(7661)":"Liabilities:CreditCard:CITIC:7661",
                    "中信银行信用卡(7661)":"Liabilities:CreditCard:CITIC:7661",
                    "浦发银行(0083)":"Liabilities:CreditCard:SPD:0083",     
                    "浦发银行信用卡(0083)":"Liabilities:CreditCard:SPD:0083",        
                    "浦发银行(0083)":"Liabilities:CreditCard:SPD:0083",        
                    "广发银行信用卡(2893)":"Liabilities:CreditCard:CGB:2893"          
                }
            }
            """;

    private static final Map<String, Map<String, String>> mappings;

    static {
        var mapper = JsonMapper.builder()
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .build();
        try {
            var map = mapper.readValue(mapping, new TypeReference<Map<String, Map<String, String>>>() {
            });
            mappings = Collections.unmodifiableMap(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static String account(AccountType accountType, String method) {
        return mappings.getOrDefault(accountType.name().toLowerCase(), Collections.emptyMap()).get(method);
    }
}
