package com.tuandat.oceanfresh_backend.services.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tuandat.oceanfresh_backend.dtos.auth.UpdateUserDTO;
import com.tuandat.oceanfresh_backend.dtos.auth.UserDTO;
import com.tuandat.oceanfresh_backend.dtos.auth.UserLoginDTO;
import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
import com.tuandat.oceanfresh_backend.exceptions.InvalidPasswordException;
import com.tuandat.oceanfresh_backend.models.User;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(UserLoginDTO userLoginDT) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
    User getUserDetailsFromRefreshToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;

    Page<User> findAll(String keyword, Pageable pageable) throws Exception;
    void resetPassword(Long userId, String newPassword)
            throws InvalidPasswordException, DataNotFoundException;
    void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
    void changeProfileImage(Long userId, String imageName) throws Exception;
    String loginSocial(UserLoginDTO userLoginDTO) throws Exception;
}
