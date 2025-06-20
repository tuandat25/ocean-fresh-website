// package com.tuandat.oceanfresh_backend.services.user;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.dao.DataIntegrityViolationException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.tuandat.oceanfresh_backend.components.JwtTokenUtils;
// import com.tuandat.oceanfresh_backend.components.LocalizationUtils;
// import com.tuandat.oceanfresh_backend.dtos.auth.UpdateUserDTO;
// import com.tuandat.oceanfresh_backend.dtos.auth.UserDTO;
// import com.tuandat.oceanfresh_backend.dtos.auth.UserLoginDTO;
// import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
// import com.tuandat.oceanfresh_backend.exceptions.ExpiredTokenException;
// import com.tuandat.oceanfresh_backend.exceptions.InvalidPasswordException;
// import com.tuandat.oceanfresh_backend.exceptions.PermissionDenyException;
// import com.tuandat.oceanfresh_backend.models.Role;
// import com.tuandat.oceanfresh_backend.models.Token;
// import com.tuandat.oceanfresh_backend.models.User;
// import com.tuandat.oceanfresh_backend.repositories.RoleRepository;
// import com.tuandat.oceanfresh_backend.repositories.TokenRepository;
// import com.tuandat.oceanfresh_backend.repositories.UserRepository;
// import com.tuandat.oceanfresh_backend.utils.MessageKeys;
// import static com.tuandat.oceanfresh_backend.utils.ValidationUtils.isValidEmail;

// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
// @Service
// public class UserService implements IUserService{
//     private final UserRepository userRepository;
//     private final RoleRepository roleRepository;
//     private final TokenRepository tokenRepository;
//     private final PasswordEncoder passwordEncoder;
//     private final JwtTokenUtils jwtTokenUtil;
//     private final AuthenticationManager authenticationManager;
//     private final LocalizationUtils localizationUtils;

//     @Override
//     @Transactional
//     public User createUser(UserDTO userDTO) throws Exception {
//         //register user
//         if (!userDTO.getPhoneNumber().isBlank() && userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
//             throw new DataIntegrityViolationException("Số điện thoại đã tồn tại");
//         }
//         if (!userDTO.getEmail().isBlank() && userRepository.existsByEmail(userDTO.getEmail())) {
//             throw new DataIntegrityViolationException("Email đã tồn tại");
//         }
//         Role role =roleRepository.findById(userDTO.getRoleId())
//                 .orElseThrow(() -> new DataNotFoundException(
//                         localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));


//         if (role.getName().equalsIgnoreCase(Role.ADMIN)) {
//             throw new PermissionDenyException("Không được phép đăng ký tài khoản quản trị viên");
//         }
//         //convert from userDTO => user
//         User newUser = User.builder()
//                 .fullName(userDTO.getFullName())
//                 .phoneNumber(userDTO.getPhoneNumber())
//                 .email(userDTO.getEmail())
//                 .password(userDTO.getPassword())
//                 .address(userDTO.getAddress())
//                 .dateOfBirth(userDTO.getDateOfBirth())
//                 .facebookAccountId(userDTO.getFacebookAccountId())
//                 .googleAccountId(userDTO.getGoogleAccountId())
//                 .active(true)
//                 .build();

//         newUser.setRole(role);

//         if (!userDTO.isSocialLogin()) {
//             String password = userDTO.getPassword();
//             String encodedPassword = passwordEncoder.encode(password);
//             newUser.setPassword(encodedPassword);
//         }
//         return userRepository.save(newUser);
//     }


//     @Override
//     public String login(UserLoginDTO userLoginDTO) throws Exception {
//         Optional<User> optionalUser = Optional.empty();

//         // Kiểm tra người dùng qua số điện thoại
//         if (userLoginDTO.getPhoneNumber() != null && !userLoginDTO.getPhoneNumber().isBlank()) {
//             optionalUser = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber());
//         }

//         // Nếu không tìm thấy người dùng bằng số điện thoại, thử tìm qua email
//         if (optionalUser.isEmpty() && userLoginDTO.getEmail() != null) {
//             optionalUser = userRepository.findByEmail(userLoginDTO.getEmail());
//         }

//         // Nếu không tìm thấy người dùng, ném ngoại lệ
//         if (optionalUser.isEmpty()) {
//             throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
//         }

//         User existingUser = optionalUser.get();

//         // Kiểm tra tài khoản có bị khóa không
//         if (!existingUser.isActive()) {
//             throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
//         }

//         // Tạo JWT token cho người dùng
//         return jwtTokenUtil.generateToken(existingUser);
//     }


//     @Override
//     public String loginSocial(UserLoginDTO userLoginDTO) throws Exception {
//         Optional<User> optionalUser = Optional.empty();
//         Role roleUser = roleRepository.findByName(Role.USER)
//                 .orElseThrow(() -> new DataNotFoundException(
//                         localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));

//         // Kiểm tra Google Account ID
//         if (userLoginDTO.isGoogleAccountIdValid()) {
//             optionalUser = userRepository.findByGoogleAccountId(userLoginDTO.getGoogleAccountId());

//             // Tạo người dùng mới nếu không tìm thấy
//             if (optionalUser.isEmpty()) {
//                 User newUser = User.builder()
//                         .fullName(Optional.ofNullable(userLoginDTO.getFullname()).orElse(""))
//                         .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
//                         .avatarUrl(Optional.ofNullable(userLoginDTO.getAvatarUrl()).orElse(""))
//                         .role(roleUser)
//                         .googleAccountId(userLoginDTO.getGoogleAccountId())
//                         .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
//                         .active(true)
//                         .build();

//                 // Lưu người dùng mới
//                 newUser = userRepository.save(newUser);
//                 optionalUser = Optional.of(newUser);
//             }
//         }
//         // Kiểm tra Facebook Account ID
//         else if (userLoginDTO.isFacebookAccountIdValid()) {
//             optionalUser = userRepository.findByFacebookAccountId(userLoginDTO.getFacebookAccountId());

//             // Tạo người dùng mới nếu không tìm thấy
//             if (optionalUser.isEmpty()) {
//                 User newUser = User.builder()
//                         .fullName(Optional.ofNullable(userLoginDTO.getFullname()).orElse(""))
//                         .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
//                         .avatarUrl(Optional.ofNullable(userLoginDTO.getAvatarUrl()).orElse(""))
//                         .role(roleUser)
//                         .facebookAccountId(userLoginDTO.getFacebookAccountId())
//                         .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
//                         .active(true)
//                         .build();

//                 // Lưu người dùng mới
//                 newUser = userRepository.save(newUser);
//                 optionalUser = Optional.of(newUser);
//             }
//         } else {
//             throw new IllegalArgumentException("Thông tin tài khoản mạng xã hội không hợp lệ.");
//         }

//         User user = optionalUser.get();

//         // Kiểm tra nếu tài khoản bị khóa
//         if (!user.isActive()) {
//             throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
//         }

//         // Tạo JWT token cho người dùng
//         return jwtTokenUtil.generateToken(user);
//     }

//     @Transactional
//     @Override
//     public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
//         // Find the existing user by userId
//         User existingUser = userRepository.findById(userId)
//                 .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

//         // Check if the phone number is being changed and if it already exists for another user
//         /*
//         String newPhoneNumber = updatedUserDTO.getPhoneNumber();
//         if (!existingUser.getPhoneNumber().equals(newPhoneNumber) &&
//                 userRepository.existsByPhoneNumber(newPhoneNumber)) {
//             throw new DataIntegrityViolationException("Phone number already exists");
//         }
//        */
//         // Update user information based on the DTO
//         if (updatedUserDTO.getFullName() != null) {
//             existingUser.setFullName(updatedUserDTO.getFullName());
//         }
//         /*
//         if (newPhoneNumber != null) {
//             existingUser.setPhoneNumber(newPhoneNumber);
//         }
//         */
//         if (updatedUserDTO.getAddress() != null) {
//             existingUser.setAddress(updatedUserDTO.getAddress());
//         }
//         if (updatedUserDTO.getDateOfBirth() != null) {
//             existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
//         }
//         if (updatedUserDTO.isFacebookAccountIdValid()) {
//             existingUser.setFacebookAccountId(updatedUserDTO.getFacebookAccountId());
//         }
//         if (updatedUserDTO.isGoogleAccountIdValid()) {
//             existingUser.setGoogleAccountId(updatedUserDTO.getGoogleAccountId());
//         }


//         // Update the password if it is provided in the DTO
//         if (updatedUserDTO.getPassword() != null
//                 && !updatedUserDTO.getPassword().isEmpty()) {
//             if(!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
//                 throw new DataNotFoundException("Mật khẩu và mật khẩu xác nhận lại không khớp");
//             }
//             String newPassword = updatedUserDTO.getPassword();
//             String encodedPassword = passwordEncoder.encode(newPassword);
//             existingUser.setPassword(encodedPassword);
//         }
//         //existingUser.setRole(updatedRole);
//         // Save the updated user
//         return userRepository.save(existingUser);
//     }

//     @Override
//     public User getUserDetailsFromToken(String token) throws Exception {
//         if(jwtTokenUtil.isTokenExpired(token)) {
//             throw new ExpiredTokenException("Token đã hết hạn");
//         }
//         String subject = jwtTokenUtil.getSubject(token);
//         Optional<User> user;
//         user = userRepository.findByPhoneNumber(subject);
//         if (user.isEmpty() && isValidEmail(subject)) {
//             user = userRepository.findByEmail(subject);
//         }
//         return user.orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
//     }
//     @Override
//     public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
//         Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
//         return getUserDetailsFromToken(existingToken.getToken());
//     }

//     @Override
//     public Page<User> findAll(String keyword, Pageable pageable) {
//         return userRepository.findAll(keyword, pageable);
//     }

//     @Override
//     @Transactional
//     public void resetPassword(Long userId, String newPassword)
//             throws InvalidPasswordException, DataNotFoundException {
//         User existingUser = userRepository.findById(userId)
//                 .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
//         String encodedPassword = passwordEncoder.encode(newPassword);
//         existingUser.setPassword(encodedPassword);
//         userRepository.save(existingUser);
//         //reset password => clear token
//         List<Token> tokens = tokenRepository.findByUser(existingUser);
//         for (Token token : tokens) {
//             tokenRepository.delete(token);
//         }
//     }

//     @Override
//     @Transactional
//     public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
//         User existingUser = userRepository.findById(userId)
//                 .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
//         existingUser.setActive(active);
//         userRepository.save(existingUser);
//     }

//     @Override
//     @Transactional
//     public void changeProfileImage(Long userId, String imageName) throws Exception {
//         User existingUser = userRepository.findById(userId)
//                 .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
//         existingUser.setAvatarUrl(imageName);
//         userRepository.save(existingUser);
//     }
// }



