package com.library.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.RegistrationRequest;
import com.library.entity.enums.RequestStatus;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, String> {

    @Query(
            "SELECT rr FROM RegistrationRequest rr JOIN FETCH rr.user u WHERE rr.status = :status ORDER BY rr.submittedAt ASC")
    List<RegistrationRequest> findAllByStatusWithUser(@Param("status") RequestStatus status);

    @Query("SELECT rr FROM RegistrationRequest rr JOIN FETCH rr.user WHERE rr.requestId = :id")
    Optional<RegistrationRequest> findByIdWithUser(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rr FROM RegistrationRequest rr JOIN FETCH rr.user WHERE rr.requestId = :id")
    Optional<RegistrationRequest> findByIdWithUserForUpdate(@Param("id") String id);

    Optional<RegistrationRequest> findByUser_UserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rr FROM RegistrationRequest rr JOIN FETCH rr.user WHERE rr.user.userId = :userId")
    Optional<RegistrationRequest> findByUserUserIdForUpdate(@Param("userId") String userId);

    @Query("SELECT rr FROM RegistrationRequest rr JOIN FETCH rr.user u WHERE u.userId IN :userIds")
    List<RegistrationRequest> findAllByUser_UserIdInWithUser(@Param("userIds") Collection<String> userIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RegistrationRequest rr WHERE rr.user.userId = :uid")
    void deleteAllForUser(@Param("uid") String userId);
}
