package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public User createUser(User user) {
        entityManager.persist(user);
        return user;
    }
}
