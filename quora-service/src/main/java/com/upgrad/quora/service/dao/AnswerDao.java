package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Answer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Answer createAnswer(Answer answer) {
        entityManager.persist(answer);
        return answer;
    }


    public Answer getAnswerId(final String answerId) {
        try {
            return entityManager.createNamedQuery("answerByUUID", Answer.class).setParameter("uuid", answerId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void editAnswer(Answer answer) {
        entityManager.merge(answer);
    }

    public Answer deleteAnswer( String answerId) {
        Answer deleteAnswer = getAnswerId(answerId);
        if (deleteAnswer != null) {
            entityManager.remove(deleteAnswer);
        }
        return deleteAnswer;
    }

    public List<Answer> getAllAnswersToQuestion(final String questionId) {
        return entityManager.createNamedQuery("answerByQuestionId", Answer.class).setParameter("questionId", questionId).getResultList();
    }
}
