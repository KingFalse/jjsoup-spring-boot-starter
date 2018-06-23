package me.kagura;

public abstract class FollowProcess {

    public String result;

    public abstract String doProcess(HttpConnection httpConnection);

    public void doException(Exception e) throws Exception {
        throw e;
    }

    public boolean isSuccess(HttpConnection httpConnection) {
        return true;
    }

}
