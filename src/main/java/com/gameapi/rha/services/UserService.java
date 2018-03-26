package com.gameapi.rha.services;

import com.gameapi.rha.models.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//import com.gameapi.rha.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.ResultSet;


/**
 * UserService is a class to operate with params from UserController.
 */
@Service
@Transactional
public class UserService {

  private static JdbcTemplate jdbc;
  /**
  * UserService default constructor specialized.
  */
  private UserService() { }

  /**
  *Main, but temporary database.
  */
  private static ConcurrentHashMap<String, User> map =
         new ConcurrentHashMap<>();
  /**
  * rating table.
  */
  public static Map<String, Integer> RatingTable = new HashMap<>();

  /**
   * Insertion into DB with SQL.
   * @param user is user to create
   * @return
   */
  public static User createUser(User user) {
    jdbc.update((PreparedStatementCreator) user);
    return user;
  }


  /**
  *Insertion into DB.
  */
  public static User putInMap(User user) {
    if (map.containsKey(user.getEmail())) {
      return null;
    }
    map.put(user.getEmail(), user);
    return user;
  }

  /**
  * function rating returns rating by it's page.
  * @param  page  Is used to tell the pagenum.
  * @return rating result
  */
  public static Map<String,Integer> rating(Integer page) {
    final Iterator itr = map.entrySet().iterator();

    if (page == null) {
      page = 0;
    }
    Integer elements = 2;
    page = page * elements;
    while (page-- > 0 && itr.hasNext()) {
      itr.next();
    }
    final Map<String,Integer> result = new HashMap<>();

    boolean isEmptyPage = true;

    while (elements-- > 0 && itr.hasNext()) {
      isEmptyPage = false;
      Map.Entry<String,User> entry = (Map.Entry<String, User>) itr.next();
      result.put(entry.getValue().getUsername(), entry.getValue().getRating());
    }
    if (isEmptyPage) {
      return null;
    }
    return result;
  }

  public static Boolean check(String email, String password)  {
    return (map.containsKey(email) && map.get(email).checkPassword(password));
  }

  public static User userInfo(String email) {
    return map.get(email);
  }

  /**
  *ratingBuilder is used for Getting rating table full.
  */
  public static void ratingBuilder() {
    for (Map.Entry<String,User> user:map.entrySet()) {
      RatingTable.put(user.getValue().getUsername(),user.getValue().getRating());
    }
  }

  /**
   * Change user is function to change current user in session.
   * @param prevUser is previous user
   * @param newUser is new user
   */
  public static void changeUser(String prevUser, User newUser) {

    final User prev = map.get(prevUser);

    // Такого быть не должно
    if (prev == null) {
      return;
    }
    map.get(prevUser).setEmail(newUser.getEmail());
    map.get(prevUser).setPassword(newUser.getPassword());
  }
}
