package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Modifying(clearAutomatically = true)
    @Query(
            "DELETE FROM Notification n WHERE n.recipient.userId = :uid OR (n.sender IS NOT NULL AND n.sender.userId = :uid)")
    void deleteAllForUser(@Param("uid") String userId);
}
