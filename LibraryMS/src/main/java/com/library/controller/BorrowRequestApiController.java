package com.library.controller;

import com.library.dto.BorrowApprovalResponse;
import com.library.dto.BorrowDecisionRequest;
import com.library.dto.BorrowRequestCreateRequest;
import com.library.dto.BorrowRequestResponse;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowRequest;
import com.library.exception.BusinessRuleException;
import com.library.security.LibraryUserDetails;
import com.library.service.BorrowRequestService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/borrow")
public class BorrowRequestApiController {

    private final BorrowRequestService borrowRequestService;

    public BorrowRequestApiController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BorrowRequestResponse> createBorrowRequest(
            @Valid @RequestBody BorrowRequestCreateRequest request,
            @AuthenticationPrincipal LibraryUserDetails principal) {
        BorrowRequest created = borrowRequestService.createPendingRequest(
                principal.getUserId(),
                request.getBookId(),
                request.getDurationDays());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public List<BorrowRequestResponse> getPendingRequests() {
        return borrowRequestService.listPendingForLibrarian().stream().map(BorrowRequestApiController::toResponse).toList();
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public BorrowApprovalResponse approveRequest(
            @PathVariable("id") String requestId,
            @RequestBody(required = false) BorrowDecisionRequest request,
            @AuthenticationPrincipal LibraryUserDetails principal) {
        BorrowDecisionRequest decision = request != null ? request : new BorrowDecisionRequest();
        BorrowRecord record = borrowRequestService.approveWithDuration(
                requestId,
                principal.getUserId(),
                decision.getDueDate(),
                decision.getDurationDays());
        BorrowApprovalResponse response = new BorrowApprovalResponse();
        response.setRequestId(record.getBorrowRequest().getRequestId());
        response.setStatus(record.getBorrowRequest().getStatus().name());
        response.setBorrowRecordId(record.getRecordId());
        response.setDueDate(record.getDueDate());
        response.setOverdue(record.getReturnedAt() == null && LocalDate.now().isAfter(record.getDueDate()));
        return response;
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @PathVariable("id") String requestId,
            @AuthenticationPrincipal LibraryUserDetails principal) {
        borrowRequestService.reject(requestId, principal.getUserId());
        return ResponseEntity.ok(Map.of("status", "REJECTED", "requestId", requestId));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request body.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private static BorrowRequestResponse toResponse(BorrowRequest request) {
        BorrowRequestResponse response = new BorrowRequestResponse();
        response.setRequestId(request.getRequestId());
        response.setBookId(request.getBook().getBookId());
        response.setStudentId(request.getStudent().getUserId());
        response.setStatus(request.getStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setRequestedDurationDays(request.getRequestedDurationDays());
        return response;
    }
}
