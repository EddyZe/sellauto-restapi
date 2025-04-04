package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.FeedBack;

import java.util.List;

@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, Long> {

    @Query("select f from FeedBack f join User u on u.userId = f.receiver.userId where u.userId = :userId")
    List<FeedBack> findReceivedFeedbackByUserId(Long userId);
}
