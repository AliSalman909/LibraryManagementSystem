package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.ReservationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentReservationController {

    private final ReservationService reservationService;

    public StudentReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** Shows the student's own reservations (all statuses). */
    @GetMapping("/student/reservations")
    public String myReservations(
            @AuthenticationPrincipal LibraryUserDetails principal,
            Model model) {
        model.addAttribute("reservations", reservationService.listForStudent(principal.getUserId()));
        return "student/reservations";
    }

    /** Creates a reservation for an unavailable book. */
    @PostMapping("/student/reservations")
    public String createReservation(
            @RequestParam("bookId") String bookId,
            @RequestParam(name = "durationDays", defaultValue = "14") Integer durationDays,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            var reservation = reservationService.createReservation(principal.getUserId(), bookId, durationDays);
            redirectAttributes.addFlashAttribute("flashSuccess",
                    "Reservation created! You are #" + reservation.getQueuePosition() + " in the queue.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "flashError",
                    "Reservation could not be saved right now due to a data conflict. Please refresh and try again.");
        }
        return "redirect:/student/reservations";
    }

    /** Cancels a student's own reservation. */
    @PostMapping("/student/reservations/{id}/cancel")
    public String cancelReservation(
            @PathVariable("id") String reservationId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelByStudent(reservationId, principal.getUserId());
            redirectAttributes.addFlashAttribute("flashSuccess", "Reservation cancelled.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                    "flashError",
                    "Reservation could not be cancelled right now due to a data conflict. Please refresh and try again.");
        }
        return "redirect:/student/reservations";
    }
}
