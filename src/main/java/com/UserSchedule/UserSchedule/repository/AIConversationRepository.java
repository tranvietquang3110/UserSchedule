package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.entity.AIConversation;
import com.UserSchedule.UserSchedule.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {
    List<AIConversation> findByUserOrderByOrderAsc(User user);
    int countByUser(User user);
    void deleteByUser(User user);
}
