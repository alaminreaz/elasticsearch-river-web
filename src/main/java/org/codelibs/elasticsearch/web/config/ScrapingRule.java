package org.codelibs.elasticsearch.web.config;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.codelibs.elasticsearch.web.util.ParameterUtil;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.util.FieldUtil;
import org.seasar.robot.entity.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapingRule {
    private static final Logger logger = LoggerFactory
            .getLogger(ScrapingRule.class);

    final Map<String, Pattern> patternMap = new LinkedHashMap<String, Pattern>();

    final Map<String, Object> settingMap;

    final Map<String, Map<String, Object>> ruleMap;

    public ScrapingRule(final Map<String, Object> settingMap,
            final Map<String, Object> paramPatternMap,
            final Map<String, Map<String, Object>> ruleMap) {
        if (settingMap == null) {
            this.settingMap = Collections.emptyMap();
        } else {
            this.settingMap = settingMap;
        }
        this.ruleMap = ruleMap;
        for (final Map.Entry<String, Object> entry : paramPatternMap.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof String) {
                patternMap.put(entry.getKey(),
                        Pattern.compile(value.toString()));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("patternMap: " + patternMap);
        }
    }

    public boolean matches(final ResponseData responseData) {
        if (patternMap.isEmpty()) {
            return false;
        }

        try {
            final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(responseData
                    .getClass());
            for (final Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
                final Field field = beanDesc.getField(entry.getKey());
                final Object value = FieldUtil.get(field, responseData);
                if (value == null
                        || !entry.getValue().matcher(value.toString())
                                .matches()) {
                    return false;
                }
            }
            return true;
        } catch (final Exception e) {
            logger.warn("Invalid parameters: " + responseData, e);
            return false;
        }
    }

    public Map<String, Map<String, Object>> getRuleMap() {
        return ruleMap;
    }

    public <T, V> T getSetting(final String key, final T defaultValue) {
        return ParameterUtil.getValue(settingMap, key, defaultValue);
    }
}
