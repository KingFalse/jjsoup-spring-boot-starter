package me.kagura;

import java.util.Base64;

public abstract class FollowProcess {

    public String result;

    public static FollowProcess FollowProcessBase64Image() {
        return new FollowProcess() {
            @Override
            public String doProcess(HttpConnection httpConnection) {
                this.result = "data:image/png;base64," + Base64.getEncoder().encodeToString(httpConnection.response().bodyAsBytes());
                return this.result;
            }
        };
    }

    public abstract String doProcess(HttpConnection httpConnection);

    public void doException(Exception e) throws Exception {
        throw e;
    }

    public boolean isSuccess(HttpConnection httpConnection) {
        return true;
    }

}
