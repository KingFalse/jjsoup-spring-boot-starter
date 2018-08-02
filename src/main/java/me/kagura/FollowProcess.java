package me.kagura;

import org.jsoup.Connection;

import java.util.Base64;

public abstract class FollowProcess {

    public String result;

    public static FollowProcess FollowProcessBase64Image() {
        return new FollowProcess() {
            @Override
            public String doProcess(Connection connection, LoginInfo loginInfo) {
                this.result = "data:image/png;base64," + Base64.getEncoder().encodeToString(connection.response().bodyAsBytes());
                return this.result;
            }
        };
    }

    public abstract String doProcess(Connection connection, LoginInfo loginInfo);

    public void doException(Exception e) throws Exception {
        throw e;
    }

    public boolean isSuccess(HttpConnection httpConnection) {
        return true;
    }

}
