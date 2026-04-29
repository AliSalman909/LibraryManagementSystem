package com.library.repository;

import com.library.entity.DeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, String> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DeletionRequest d WHERE d.user.userId = :uid")
    void deleteAllForUser(@Param("uid") String userId);
}
