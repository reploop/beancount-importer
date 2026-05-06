package org.reploop.beancount;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Data
public class BillRecord {
    protected LocalDateTime createdAt;
    protected String category;
    protected String peer;
    protected String peerAccount;
    protected String goods;
    protected Type type;
    protected BigDecimal amount;
    protected String method;
    protected String status;
    protected String order;
    protected String merchantOrder;
    protected String remarks;

    /**
     * For additional information
     */
    protected Map<String, String> metadata = new HashMap<>();

    public void putMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return metadata.get(key);
    }
}
