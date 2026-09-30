package com.kanhe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Kanhe {
    public static final String MOD_ID = "kanhe";
    public static final Logger LOGGER = LoggerFactory.getLogger("Kanhe");

    /** 26.4 新增的查询参数，用于在服务器地址里携带连接密码。 */
    public static final String PROPERTY_ID = "_id";

    /** 客户端已经知道这个服务器、但密码不对时，回给它的提示。 */
    public static final String KEY_WRONG_CODE = "disconnect.kanhe.wrong";

    private Kanhe() {
    }
}
