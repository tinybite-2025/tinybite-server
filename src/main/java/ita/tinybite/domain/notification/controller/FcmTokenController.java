package ita.tinybite.domain.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ita.tinybite.domain.notification.dto.request.FcmTokenRequest;
import ita.tinybite.domain.notification.service.FcmTokenService;
import ita.tinybite.domain.notification.service.facade.NotificationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class FcmTokenController {

	private final FcmTokenService fcmTokenService;
	private final NotificationFacade notificationFacade;

	// token 이미 존재 시 업데이트 해줌
	@PostMapping("/token")
	public ResponseEntity<Void> registerToken(@RequestBody @Valid FcmTokenRequest request,
		@RequestHeader(name = "User-ID") Long currentUserId) {
		fcmTokenService.saveOrUpdateToken(currentUserId, request.token());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/Test")
	public ResponseEntity<Void> test(@RequestParam Long userId) {
		notificationFacade.notifyTest(userId);
		return ResponseEntity.ok().build();
	}
}
