package com.example.realtimechat.presence;

import com.example.realtimechat.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{userId}/presence")
    ApiResponse<PresenceResponse> presence(@PathVariable UUID userId) {
        return ApiResponse.ok("Presence found", presenceService.get(userId));
    }
}
