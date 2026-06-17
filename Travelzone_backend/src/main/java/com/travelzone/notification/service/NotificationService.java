package com.travelzone.notification.service;

import com.travelzone.exception.ResourceNotFoundException;
import com.travelzone.notification.dto.NotificationResponse;
import com.travelzone.notification.entity.Notification;
import com.travelzone.notification.repository.NotificationRepository;
import com.travelzone.user.entity.User;
import com.travelzone.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * REQUIRES_NEW — always runs in its own independent transaction.
     * A failure here will NEVER roll back the caller's transaction
     * (e.g. GuideBookingService, PaymentService).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(User user, String title, String message, String type) {
        try {
            Notification n = new Notification();
            n.setUser(user);
            n.setTitle(title);
            n.setMessage(message);
            n.setType(type);
            notificationRepository.save(n);
        } catch (Exception e) {
            // Log but never propagate — notifications must never break business logic
            System.err.println("[NotificationService] Failed to save notification: " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendToUserId(Long userId, String title, String message, String type) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            send(user, title, message, type);
        } catch (Exception e) {
            System.err.println("[NotificationService] Failed to send to userId " + userId + ": " + e.getMessage());
        }
    }

    // Legacy method — kept for compatibility
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(String email, String message) {
        try {
            System.out.println("Notification sent to " + email + ": " + message);
            User user = findUser(email);
            Notification n = new Notification();
            n.setUser(user);
            n.setTitle("Notification");
            n.setMessage(message);
            n.setType("SYSTEM");
            notificationRepository.save(n);
        } catch (Exception e) {
            System.err.println("[NotificationService] notifyUser failed for " + email + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::map).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = findUser(email);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    public void markAllRead(String email) {
        User user = findUser(email);
        notificationRepository.markAllReadByUserId(user.getId());
    }

    public void markOneRead(Long notifId, String email) {
        User user = findUser(email);
        Notification n = notificationRepository.findById(notifId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId()))
            throw new ResourceNotFoundException("Notification not found");
        n.setRead(true);
    }

    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse map(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getTitle(), n.getMessage(),
                n.getType(), n.isRead(), n.getCreatedAt());
    }
}