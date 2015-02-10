package com.baobaotao.connleak;

import java.sql.Connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("jdbcUserService")
public class JdbcUserService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	public void logon(String userName) {
		Connection conn = null;
		try {
			// ①.直接从数据源获取连接，后续程序没有显示释放该连接
			conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
			// Connection conn = jdbcTemplate.getDataSource().getConnection();
			String sql = "UPDATE t_user SET last_logon_time=? WHERE user_name =?";
			jdbcTemplate.update(sql, System.currentTimeMillis(), userName);
			Thread.sleep(1000);// ②模拟程序代码的执行时间
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DataSourceUtils.releaseConnection(conn,
					jdbcTemplate.getDataSource());
		}
	}

	/**
	 * 以异步线程的方式执行JdbcUserService#logon()方法，以模拟多线程环境
	 * 
	 * @param userService
	 * @param userName
	 */
	public static void asynchrLogon(JdbcUserService userService, String userName) {
		UserServiceRunner runner = new UserServiceRunner(userService, userName);
		runner.start();
	}

	private static class UserServiceRunner extends Thread {

		private JdbcUserService userService;
		private String userName;

		public UserServiceRunner(JdbcUserService userService, String userName) {
			this.userService = userService;
			this.userName = userName;
		}

		@Override
		public void run() {
			userService.logon(userName);
		}

	}

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void reportConn(BasicDataSource basicDataSource) {
		System.out.println("连接数[active:idle]-["
				+ basicDataSource.getNumActive() + ":"
				+ basicDataSource.getNumIdle() + "]");
	}

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"com/baobaotao/connleak/applicatonContext.xml");
		JdbcUserService userService = (JdbcUserService) ctx
				.getBean("jdbcUserService");

		BasicDataSource basicDataSource = (BasicDataSource) ctx
				.getBean("dataSource");
		JdbcUserService.reportConn(basicDataSource);

		JdbcUserService.asynchrLogon(userService, "tom");
		JdbcUserService.sleep(500);
		JdbcUserService.reportConn(basicDataSource);

		JdbcUserService.sleep(2000);
		JdbcUserService.reportConn(basicDataSource);

		JdbcUserService.asynchrLogon(userService, "john");
		JdbcUserService.sleep(500);
		JdbcUserService.reportConn(basicDataSource);

		JdbcUserService.sleep(2000);
		JdbcUserService.reportConn(basicDataSource);

	}

}
