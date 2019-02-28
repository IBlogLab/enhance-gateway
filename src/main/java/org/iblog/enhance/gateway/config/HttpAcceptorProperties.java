package org.iblog.enhance.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author shaoxiao.xu
 * @date 2018/12/25 15:04
 */
@ConfigurationProperties("quicktron.acceptor.http")
@Validated
public class HttpAcceptorProperties {
    private boolean start = true;
    private int order;

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
