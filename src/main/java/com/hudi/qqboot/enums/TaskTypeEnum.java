package com.hudi.qqboot.enums;

/**
 * @author hudi
 * @date 11 2月 2026 17:45
 */
public enum TaskTypeEnum {
    OTHER("其他"),
    CANCEL_TICKET("取消预定"),
    QUERY_TICKET("查询机票"),
    MODIFY_TICKET("修改预定");

    private String name;

    TaskTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
