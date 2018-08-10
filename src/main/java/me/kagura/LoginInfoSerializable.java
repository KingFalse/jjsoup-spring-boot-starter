package me.kagura;

public interface LoginInfoSerializable {

    void setLoginInfo(LoginInfo loginInfo) throws Exception;

    LoginInfo getLoginInfo(String key) throws Exception;

}
