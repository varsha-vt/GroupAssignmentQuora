package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
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
public class QuestionService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public Question createQuestion(String authorization, Question questionEntity) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization, "User is signed out.Sign in first to post a question");
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUser(userAuthEntity.getUser());
        return questionDao.createQuestion(questionEntity);
    }

    public List<Question> getAllQuestions(String authorization) throws AuthorizationFailedException {
        userBusinessService.validateUserAuthentication(authorization,"User is signed out.Sign in first to get all questions");
        return questionDao.getAllQuestions();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Question editQuestionContent(String authorization, String questionID, String editedContent) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity  = userBusinessService.validateUserAuthentication(authorization,"User is signed out.Sign in first to edit the question");
        Question question = questionDao.getQuestionByUuid(questionID);
        if(question == null) {
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(!question.getUser().getUuid().equals(userAuthEntity.getUser().getUuid())){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }
          question.setContent(editedContent);
          questionDao.editQuestion(question);
          return question;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Question deleteQuestion(String authorization, String questionID) throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,"User is signed out.Sign in first to delete the question");

        Question question = questionDao.getQuestionByUuid(questionID);
        if(question == null){
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if(!question.getUser().getUuid().equals(userAuthEntity.getUser().getUuid()) && !userAuthEntity.getUser().getRole().equals("admin")) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
        }
        questionDao.deleteQuestion(question);

        return question;
    }
}
