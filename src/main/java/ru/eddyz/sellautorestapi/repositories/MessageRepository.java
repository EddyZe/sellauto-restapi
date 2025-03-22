package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Message;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByCreatedAt(LocalDateTime localDateTime);



    @Query("select m from Message m join Chat c on c.chatId=m.chat.chatId where c.chatId=:chatId")
    List<Message> findMessageByChatId(@Param("chatId") Long chatId);
}
