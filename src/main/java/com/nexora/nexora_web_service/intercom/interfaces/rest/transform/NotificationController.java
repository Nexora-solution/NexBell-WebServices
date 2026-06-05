package com.nexora.nexora_web_service.intercom.interfaces.rest.transform;

import com.nexora.nexora_web_service.intercom.domain.model.commands.MarkNotificationAsReadCommand;
import com.nexora.nexora_web_service.intercom.domain.model.queries.GetNotificationHistoryQuery;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomCommandService;
import com.nexora.nexora_web_service.intercom.domain.services.IntercomQueryService;
import com.nexora.nexora_web_service.intercom.interfaces.rest.resources.NotificationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/intercom/notifications")
@Tag(name = "Notifications", description = "Query and update push notifications dispatches status")
public class NotificationController {

    private final IntercomQueryService queryService;
    private final IntercomCommandService commandService;

    public NotificationController(IntercomQueryService queryService, IntercomCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping("/apartment/{id}")
    @Operation(summary = "Get the notifications sent to an apartment")
    public ResponseEntity<List<NotificationResource>> getNotificationsByApartment(@PathVariable Long id) {
        var query = new GetNotificationHistoryQuery(id);
        var dispatches = queryService.handle(query);
        var resources = dispatches.stream()
                .map(notif -> new NotificationResource(notif.getId(), notif.getStatus(), notif.getSentAt(), notif.isRead()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification dispatch as read by the resident")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        var command = new MarkNotificationAsReadCommand(id);
        commandService.handle(command);
        return ResponseEntity.ok("Notification marked as read");
    }
}
