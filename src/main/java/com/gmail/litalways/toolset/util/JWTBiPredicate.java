package com.gmail.litalways.toolset.util;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * @author IceRain
 * @since 2023/09/22
 */
@Slf4j
public class JWTBiPredicate<T> implements BiPredicate<Claim, DecodedJWT> {

    private final T value;

    public JWTBiPredicate(T value) {
        this.value = value;
    }

    /**
     * 验证claim实体与token是否深度一致
     */
    @Override
    public boolean test(Claim claim, DecodedJWT decodedJWT) {
        if (claim.asMap() != null && value instanceof Map<?,?>) {
            // check as map
            return deepCheck(claim.asMap(), value);
        } else if (claim.asList(Object.class) != null && value instanceof Collection<?>) {
            // check as list
            return deepCheck(claim.asList(Object.class), value);
        } else {
            // decoded value not map or list, just false to equal
            return false;
//            throw new IllegalArgumentException(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.java.type.to.check.claims") + " " + claim.getClass() + " " + value.getClass());
        }
    }

    private boolean deepCheck(Object claim, Object value) {
        if (claim == null && value == null) {
            return true;
        } else if (claim == null || value == null) {
            return false;
        } else if (claim instanceof Map<?,?> claimMap && value instanceof Map<?,?> valueMap) {
            if (claimMap.size() != valueMap.size()) {
                return false;
            }
            Set<?> keySet = claimMap.keySet();
            for (Object key : keySet) {
                if (valueMap.containsKey(key)) {
                    Object claimVal = claimMap.get(key);
                    Object valueVal = valueMap.get(key);
                    boolean result = deepCheck(claimVal, valueVal);
                    if (!result) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else if (claim instanceof Collection<?> claimList && value instanceof Collection<?> valueList) {
            if (claimList.size() != valueList.size()) {
                return false;
            }
            Iterator<?> claimIt = claimList.iterator();
            Iterator<?> valueIt = valueList.iterator();
            while (claimIt.hasNext() && valueIt.hasNext()) {
                Object claimNext = claimIt.next();
                Object valueNext = valueIt.next();
                boolean result = deepCheck(claimNext, valueNext);
                if (!result) {
                    return false;
                }
            }
            return true;
        } else if (claim instanceof Boolean && value instanceof Boolean) {
            return value.equals(claim);
        } else if (claim instanceof Integer && value instanceof Integer) {
            return value.equals(claim);
        } else if (claim instanceof Long && value instanceof Long) {
            return value.equals(claim);
        } else if (claim instanceof Double && value instanceof Double) {
            return value.equals(claim);
        } else if (claim instanceof String && value instanceof String) {
            return value.equals(claim);
        } else {
//            return false;
            throw new IllegalArgumentException(MessageUtil.getMessage("encrypt.jwt.tip.unsupported.java.type.to.check.claims") + " " + claim.getClass() + " " + value.getClass());
        }
    }

}
