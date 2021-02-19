package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Question;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Question createQuestion(Question questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<Question> getAllQuestions(){
        try{
            return entityManager.createNamedQuery("getAllQuestions", Question.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
