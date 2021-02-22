package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.GenericErrorCode;
import com.upgrad.quora.service.common.UnexpectedException;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.*;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Base64;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public User signup(User user) throws SignUpRestrictedException {

        // 'SGR-001'
        if (userDao.getUserByUserName(user.getUserName()) != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }
        // 'SGR-002'
        if (userDao.getUserByEmail(user.getEmail()) != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }

        String password = user.getPassword();
        if (password != null) {
            String[] encryptedText = cryptographyProvider.encrypt(password);
            user.setSalt(encryptedText[0]);
            user.setPassword(encryptedText[1]);
            user.setRole(QuoraUtil.NON_ADMIN_ROLE);
        }
        return userDao.createUser(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signin(String authorization) throws AuthenticationFailedException {

        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(QuoraUtil.BASIC_TOKEN)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(QuoraUtil.COLON);
            String username = decodedArray[0];
            String password = decodedArray[1];
            User user = userDao.getUserByUserName(username);

            // 'ATH-001'
            if (user == null) {
                throw new AuthenticationFailedException("ATH-001", "This username does not exist");
            }

            final String encryptedPassword = cryptographyProvider.encrypt(password, user.getSalt());
            if (encryptedPassword.equals(user.getPassword())) {

                JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
                UserAuthEntity userAuthTokenEntity = new UserAuthEntity();
                userAuthTokenEntity.setUser(user);
                final ZonedDateTime now = ZonedDateTime.now();
                final ZonedDateTime expiresAt = now.plusHours(8);
                userAuthTokenEntity.setAccessToken(jwtTokenProvider.generateToken(user.getUuid(), now, expiresAt));
                userAuthTokenEntity.setLoginAt(now);
                userAuthTokenEntity.setExpiresAt(expiresAt);
                userAuthTokenEntity.setUuid(user.getUuid());
                return userDao.createAuthToken(userAuthTokenEntity);

            } else { // 'ATH-002'
                throw new AuthenticationFailedException("ATH-002", "Password failed");
            }

        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            GenericErrorCode genericErrorCode = GenericErrorCode.GEN_001;
            throw new UnexpectedException(genericErrorCode, ex);
        }
    }

    public Boolean isUserSessionValid(UserAuthEntity userAuthEntity) {
        return (userAuthEntity != null && userAuthEntity.getLogoutAt() == null);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String getUserUUID(String authorization) throws SignOutRestrictedException {
        String[] bearerToken = authorization.split(QuoraUtil.BEARER_TOKEN);
        if (bearerToken != null && bearerToken.length > 1) {
            authorization = bearerToken[1];
        }
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);
        if (isUserSessionValid(userAuthEntity)) {
            userAuthEntity.setLogoutAt(ZonedDateTime.now());
            userDao.updateUserAuthEntity(userAuthEntity);
            return userAuthEntity.getUuid();
        }
        // 'SGR-001'
        throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
    }

    //Common method that will be used by all endpoints to validate the accessToken
    public UserAuthEntity validateUserAuthentication(String authorization, String athr002Message)
            throws AuthorizationFailedException {
        String[] bearerToken = authorization.split(QuoraUtil.BEARER_TOKEN);
        if (bearerToken != null && bearerToken.length > 1) {
            authorization = bearerToken[1];
        }
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);
        if (userAuthEntity == null) { // "ATHR-001"
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        if (userAuthEntity.getLogoutAt() != null) { // "ATHR-002"
            throw new AuthorizationFailedException("ATHR-002", athr002Message);
        }
        return userAuthEntity;
    }

    public User getUser(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = validateUserAuthentication(authorization,
                "User is signed out.Sign in first to get user details");
        User user = userDao.getUserByUUID(userUuid);
        if (user == null) { // "USR-001"
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        }
        return user;
    }

}
