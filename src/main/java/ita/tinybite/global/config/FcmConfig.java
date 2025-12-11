package ita.tinybite.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FcmConfig {

	@Value("${fcm.file_path}")
	private String fcmConfigPath;

	@PostConstruct
	public void initialize() {
		if (!FirebaseApp.getApps().isEmpty()) {
			log.info("Firebase app has been initialized successfully.");
			return;
		}
		try {
			ClassPathResource resource = new ClassPathResource(fcmConfigPath);
			try (InputStream stream = resource.getInputStream()) {
				FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(stream))
					.build();

				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
					log.info("Firebase app has been initialized successfully.");
				}
			}
		} catch (IOException e) {
			log.error("Error initializing Firebase app: Firebase 설정 파일을 읽을 수 없습니다.", e);
			throw new IllegalStateException("Firebase 초기화 실패: 설정 파일 오류", e);
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		try {
			return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
		} catch (IllegalStateException e) {
			log.error("FirebaseMessaging Bean 등록 실패", e);
			throw e;
		}
	}

}
