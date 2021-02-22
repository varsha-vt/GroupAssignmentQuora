package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnswerService {

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionDao questionDao;

    /*
        The methods in this class use the validateUserAuthentication method to validate the accessToken.
        If token is valid the required business logic is implemented and the corresponding dao classes are called.
     */

    //This method is called by the Answer Controller when the createAnswer API is called
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer createAnswer(Answer answer, String authorization, String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization, "User is signed out.Sign in first to post an answer");
        Question question = questionDao.getQuestionByUuid(questionId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        answer.setDate(ZonedDateTime.now());
        answer.setUser(userAuthEntity.getUser());
        answer.setUuid(UUID.randomUUID().toString());
        answer.setQuestion(question);

        return answerDao.createAnswer(answer);

    }

    //This method is called by the Answer Controller when the editAnswerContent API is called
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer editAnswer(String authorization, String answerId, String newAnswer) throws AnswerNotFoundException, AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization, "User is signed out.Sign in first to edit an answer");
        Answer answer = answerDao.getAnswerId(answerId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if (!answer.getUser().getUuid().equals(userAuthEntity.getUser().getUuid())) {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Only the answer owner can edit the answer");
        }
        answer.setAns(newAnswer);
        answerDao.editAnswer(answer);
        return answer;
    }

    //This method is called by the Answer Controller when the deleteAnswer API is called
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer deleteAnswer( String answerId, String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization, "User is signed out.Sign in first to delete an answer");

        Answer answer = answerDao.getAnswerId(answerId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if (userAuthEntity.getUser().getRole().equals("admin") || answer.getUser().getUuid().equals(userAuthEntity.getUser().getUuid())) {
            return answerDao.deleteAnswer(answerId);
        } else {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Only the answer owner or admin can delete the answer");
        }
    }

    //This method is called by the Answer Controller when the getALLAnsewerstoQuestion API is called
    public List<Answer> getAllAnswersToQuestion(String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization, "User is signed out.Sign in first to get the answers");
        Question question = questionDao.getQuestionByUuid(questionId);
        if(question == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersToQuestion(questionId);
    }
    }
