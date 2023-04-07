package com.jimi.common.redis;

import com.jimi.enums.CustomExceptionType;
import com.jimi.exception.CustomException;
import org.apache.commons.lang3.StringUtils;


/**
 * @author trjie
 */
public class CacheKey {

	private static final String UNDERLINE = "_";
	
	public static String generateKey(String prefix, String ... data) {
		StringBuilder sb  = new StringBuilder(prefix);
		if (StringUtils.isAnyBlank(data)) {
			throw new CustomException(CustomExceptionType.DATA_OPERATION_EXCEPTION, "缺少必要后缀");
		}
		sb.append(String.join(UNDERLINE, data));
		return sb.toString();
		
	}
}