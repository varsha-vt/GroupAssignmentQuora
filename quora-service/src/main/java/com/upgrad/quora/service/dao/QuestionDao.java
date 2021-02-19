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

    public Question editQuestion(Question questionEntity) {
        return entityManager.merge(questionEntity);
    }

    public Question getQuestionByUserId(String userId) {
        try {
            return entityManager.createNamedQuery("questionByUserId",Question.class).setParameter("userId",userId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Question getQuestionByUuid(String uuid) {
        try {
            return entityManager.createNamedQuery("questionByUUID",Question.class).setParameter("uuid",uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void deleteQuestion(Question questionEntity){
        entityManager.remove(questionEntity);
    }
}
