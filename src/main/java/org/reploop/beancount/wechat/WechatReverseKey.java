package org.reploop.beancount.wechat;

import org.reploop.beancount.ReverseKey;

public record WechatReverseKey(String status, String method) implements ReverseKey {
}
