package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public User createUser(User user) {
        entityManager.persist(user);
        return user;
    }
    public User getUserByUserName(final String username) {
        try {
            return entityManager.createNamedQuery("userByUserName", User.class).setParameter("userName", username).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public User getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", User.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public User getUserByUUID(String userUUID) {
        try {
            return entityManager.createNamedQuery("userByUUID", User.class).setParameter("uuid", userUUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


}