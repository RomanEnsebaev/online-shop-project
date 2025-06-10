package org.onlineshop.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
@PropertySource("classpath:application.properties")
public class JdbcConfig implements DisposableBean {

    private ConnectionPool pool;

    @Bean
    public ConnectionPool connectionPool(@Value("${jdbc.url}")  String url,
                                         @Value("${jdbc.user}") String user,
                                         @Value("${jdbc.pass}") String pass,
                                         @Value("${pool.size:10}") int size) throws Exception {

        ConnectionPool.init(url, user, pass, size);
        return ConnectionPool.get();
    }


    @Override
    public void destroy() throws Exception {
        ConnectionPool.get().closeAll();
    }
}
