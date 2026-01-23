package com.hudi.qqboot.vo;

/**
 * @author hudi
 * @date 04 1月 2026 16:56
 */
public class UserMessageVo {

    public UserMessageVo(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // 角色 ： user assistant
    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
