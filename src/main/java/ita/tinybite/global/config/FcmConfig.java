package ita.tinybite.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

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
			log.error("Error initializing Firebase app", e);
		}
	}

}
