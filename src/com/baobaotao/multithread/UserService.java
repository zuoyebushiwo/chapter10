package com.baobaotao.multithread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScoreService scoreService;

}
