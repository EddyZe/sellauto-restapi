package ru.eddyz.sellautorestapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.eddyz.sellautorestapi.entities.Chat;

import java.util.List;


@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    @Query(nativeQuery = true, value = "delete from user_chats where user_id=:userId and chat_id=:chatId")
    void deleteLinksChat(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from user_chats where chat_id=:chatId")
    void deleteLinksChat(@Param("chatId") Long chatId);

    @Query(nativeQuery = true, value = "select from user_chats uc join usr u on uc.user_id = u.user_id where u.user_id=:userId")
    List<Chat> findUserChats(@Param("userId") Long userId);

    List<Chat> findByAdAdId(Long id);
}
