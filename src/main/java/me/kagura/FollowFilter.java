package me.kagura;

import org.jsoup.Connection;

public interface FollowFilter {

    void doFilter(Connection connection, LoginInfo loginInfo);

}
