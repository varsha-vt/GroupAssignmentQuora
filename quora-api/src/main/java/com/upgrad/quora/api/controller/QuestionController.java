package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") final String accessToken, QuestionRequest questionRequest) throws AuthorizationFailedException {
        Question questionEntity = new Question();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity = questionService.createQuestion(accessToken, questionEntity);
        QuestionResponse questionResponse = new QuestionResponse();
        questionResponse.setId(questionEntity.getUuid());
        questionResponse.setStatus("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException {
        List<Question> questionList = questionService.getAllQuestions(accessToken);
        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<>();

        for (Question question : questionList) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.setId(question.getUuid());
            questionDetailsResponse.setContent(question.getContent());
            questionDetailsResponseList.add(questionDetailsResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponseList, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@RequestHeader("authorization") final String accessToken, @PathVariable("questionId") final String questionID, QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {
        Question question = new Question();
        question.setContent(questionEditRequest.getContent());
        question.setUuid(questionID);
        question = questionService.editQuestionContent(accessToken, questionID, question);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse();
        questionEditResponse.setId(question.getUuid());
        questionEditResponse.setStatus("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}")
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@RequestHeader("authorization") final String accessToken, @PathVariable("questionId") final String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {
        Question question = questionService.deleteQuestion(accessToken, questionId);

        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse();
        questionDeleteResponse.setId(question.getUuid());
        questionDeleteResponse.setStatus("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }
    }
