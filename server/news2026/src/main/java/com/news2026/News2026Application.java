package com.news2026;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class News2026Application {

	public static void main(String[] args) {
		SpringApplication.run(News2026Application.class, args);
	}

	@Component
	public static class DatabaseSchemaUpdater implements CommandLineRunner {
		@Autowired
		private JdbcTemplate jdbcTemplate;

		@Override
		public void run(String... args) throws Exception {
			try {
				jdbcTemplate.execute("ALTER TABLE app_user ALTER COLUMN points TYPE double precision;");
				System.out.println(">>> Database column 'points' altered successfully to double precision.");
			} catch (Exception e) {
				System.out.println(">>> Altering 'points' column status: " + e.getMessage());
			}
			try {
				jdbcTemplate.execute("ALTER TABLE app_user ALTER COLUMN token TYPE varchar(1000);");
				System.out.println(">>> Database column 'token' altered successfully to varchar(1000).");
			} catch (Exception e) {
				System.out.println(">>> Altering 'token' column status: " + e.getMessage());
			}
		}
	}
}
