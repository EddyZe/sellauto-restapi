package ru.eddyz.sellautorestapi.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.Ad;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    Optional<Ad> findByCarVin(String vin);

    @Query(value = "select a from Ad a join User u on a.user.userId=u.userId where u.userId=:userId")
    List<Ad> findAdUser(@Param("userId") Long userId);

    @Query(value = "select a from Ad a join Account acc on acc.user.userId=a.user.userId where acc.email=:email")
    List<Ad> findAdUserByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into favorite_ads(ad_id, user_id) values (:adId, :userId)")
    void addFavorite(@Param("userId") Long userId, @Param("adId") Long adId);


    @Transactional
    @Query(nativeQuery = true, value = "select a.* from favorite_ads fa join ad a on fa.ad_id = a.ad_id where fa.user_id=:userId")
    List<Ad> favoriteAdByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from favorite_ads where ad_id=:adId and user_id=:userId")
    void deleteFavorite(@Param("adId") Long adId, @Param("userId") Long userId);
}
