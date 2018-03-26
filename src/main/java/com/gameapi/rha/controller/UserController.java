package com.gameapi.rha.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.gameapi.rha.models.Message;
import com.gameapi.rha.models.User;
import com.gameapi.rha.services.UserService;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = {"http://bf-balance.herokuapp.com", "http://localhost:3000"}, allowCredentials = "true")
@RequestMapping("/users")
public class UserController {


  static ObjectMapper mapper = new ObjectMapper();


  /**
   * Enum of status messages for response.
   * Elements from this enum are used for response.
   */
  @SuppressWarnings("CheckStyle")
  private enum UserStatus {
    SUCCESSFULLY_REGISTERED,
    SUCCESSFULLY_AUTHED,
    SUCCESSFULLY_LOGGED_OUT,
    SUCCESSFULLY_CHANGED,
    ACCESS_ERROR,
    WRONG_CREDENTIALS,
    NOT_UNIQUE_USERNAME,
    ALREADY_AUTHENTICATED,
    UNEXPECTED_ERROR
  }

  /**
   * User creation function.
   * @param user user to create
   * @param session session to input user
   * @param response response to answer
   * @return returns response
   */
  @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
  public ResponseEntity create(HttpSession session,
                               @RequestBody User user, HttpServletResponse response) {
    // Аутентифицированный пользователь не может зарегистрироваться

    if (session.getAttribute("user") != null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
              new Message(UserStatus.ALREADY_AUTHENTICATED,user.getUsername()));
    }

    if (UserService.putInMap(user) != null) {
      user.saltHash();
      sessionAuth(session, user);
      return ResponseEntity.status(HttpStatus.OK).body(
              new Message(UserStatus.SUCCESSFULLY_REGISTERED,user.getUsername()));
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.NOT_UNIQUE_USERNAME));
    }
  }

  /**
   * Function to authorise user.
   * @param user its user to authorise
   * @param session his session to change
   * @param response our response to user
   * @return response
   */
  @PostMapping(path = "/auth", consumes = "application/json", produces = "application/json")
  public ResponseEntity auth(@RequestBody User user,
                             HttpSession session, HttpServletResponse response)  {
    ResponseEntity respond;
    // Мы не можем дважды аутентицифироваться
    if (session.getAttribute("user") != null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
              new Message(UserStatus.ALREADY_AUTHENTICATED));
    }
    // Если неверные учетные данные
    if (!UserService.check(user.getEmail(), user.getPassword())) {
      return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.WRONG_CREDENTIALS));
    }

    sessionAuth(session, user);
    return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_AUTHED));
  }

  /**
   * Function to logout user.
   * @param request actually request
   * @param session session to logout
   * @param response response, that logs out
   * @return logout
   */
  @PostMapping(path = "/logout")
  public ResponseEntity logout(HttpServletRequest request,
                               HttpSession session, HttpServletResponse response) {

    // Мы не можем выйти, не войдя
    if (session.getAttribute("user") == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ACCESS_ERROR));
    }
    session.setAttribute("user", null);

    //завершаем сеанс, отвязываем связанные с ним объекты
    session.invalidate();
    return ResponseEntity.status(HttpStatus.OK).body(
            new Message(UserStatus.SUCCESSFULLY_LOGGED_OUT));

  }

  /**
   * This function gets paginated rating.
   * @param page page of rating to show
   * @param request request to respone
   * @param session session to check authentification
   * @param response response of rating
   * @return rating
   */
  @GetMapping(path = "/rating/{page}")
  public ResponseEntity rating(@PathVariable("page") Integer page,
                               HttpServletRequest request, HttpSession session,
                               HttpServletResponse response) {
    page--;
    Map<String,Integer> resp = new HashMap<>();
    // Мы не можем получить статистику, не войдя
    if (session.getAttribute("user") == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ACCESS_ERROR));
    }
    resp = UserService.rating(page);
    //завершаем сеанс, отвязываем связанные с ним объекты
    return ResponseEntity.status(HttpStatus.OK).body(resp);
  }



  /**
   * Function to get information about session and user.
   * @param session session to check
   * @return information
   */
  @GetMapping(path = "/info")
  public ResponseEntity info(HttpSession session) {
    // Если пользователь не аутертифицирован, то у него нет доступа к информации о текущей сессии
    if (session.getAttribute("user") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
              new Message(UserStatus.ACCESS_ERROR));
    }

    final User result = UserService.userInfo((String) session.getAttribute("user"));
    if (result == null) {
      // Этого быть не может
      return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.UNEXPECTED_ERROR));
    }
    result.setPassword(null);

    return ResponseEntity.status(HttpStatus.OK).body(new Message(result));
  }

  /**
   * Function to change user parameters.
   * @param user changed user
   * @param session user to change
   * @return changed user
   */
  @PostMapping(path = "/change")
  public ResponseEntity change(@RequestBody User user, HttpSession session) {

    // Без аутентификации нет доступа к изменению данных
    if (session.getAttribute("user") == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ACCESS_ERROR));
    }

    UserService.changeUser((String) session.getAttribute("user"), user);

    return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_CHANGED));
  }

  private static void sessionAuth(HttpSession session, User user) {
    session.setAttribute("user", user.getEmail());
    session.setMaxInactiveInterval(30 * 60);
  }
}
