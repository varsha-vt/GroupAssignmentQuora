package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBusinessService userBusinessService;

    /*
    The methods in this class use the validateUserAuthentication method to validate the accessToken.
    If token is valid the required business logic is implemented and the corresponding dao classes are called.
 */

    //This method is called by the Admin Controller when deleteUser API is called.
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteUser(String userId, String authorization) throws AuthorizationFailedException, UserNotFoundException {
        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out");

        if (!QuoraUtil.ADMIN_ROLE.equalsIgnoreCase(userAuthEntity.getUser().getRole())) {
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }
        User user = userDao.getUserByUUID(userId);
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
        }
        userDao.deleteUser(user);
        return user.getUuid();

    }

}