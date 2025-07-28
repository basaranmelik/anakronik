package com.badsector.anakronik.mapper;

import com.badsector.anakronik.dto.UserDto;
import com.badsector.anakronik.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * User veritabanı nesnesini, istemciye gönderilecek DTO formatına çevirir.
     * @param user Veritabanından gelen entity.
     * @return İstemciye gönderilecek DTO.
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        // DTO'yu manuel olarak oluşturup alanları dolduruyoruz.
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}