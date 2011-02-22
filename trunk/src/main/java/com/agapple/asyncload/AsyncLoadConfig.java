package com.agapple.asyncload;

import java.util.HashMap;
import java.util.Map;

/**
 * 对应异步加载工具的关注点
 * 
 * @author jianghang 2011-1-22 上午12:06:48
 */
public class AsyncLoadConfig {

    public static final Long                DEFAULT_TIME_OUT = 3 * 1000L;
    private Long                            defaultTimeout   = DEFAULT_TIME_OUT; // 单位ms
    private Map<AsyncLoadMethodMatch, Long> matches;

    public AsyncLoadConfig(){
    }

    public AsyncLoadConfig(Long defaultTimeout){
        this.defaultTimeout = defaultTimeout;
    }

    // ===================== setter / getter ====================

    public Map<AsyncLoadMethodMatch, Long> getMatches() {
        if (matches == null) {
            matches = new HashMap<AsyncLoadMethodMatch, Long>();
            matches.put(AsyncLoadMethodMatch.TRUE, defaultTimeout); // 默认值为3秒
        }

        return matches;
    }

    public void setMatches(Map<AsyncLoadMethodMatch, Long> matches) {
        this.matches = matches;
    }

    public Long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(Long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

}
