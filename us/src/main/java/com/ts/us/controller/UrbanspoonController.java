package com.ts.us.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ts.us.dao.BranchDAO;
import com.ts.us.dao.CuisineDAO;
import com.ts.us.dao.RecipeDAO;
import com.ts.us.dao.RestaurantDAO;
import com.ts.us.dao.UserDAO;
import com.ts.us.dto.Branch;
import com.ts.us.dto.Cuisine;
import com.ts.us.dto.Recipe;
import com.ts.us.dto.Restaurant;
import com.ts.us.dto.User;
import com.ts.us.exception.UrbanspoonException;

@Controller
public class UrbanspoonController {
	@RequestMapping("/home")
	public ModelAndView getRestaurant() {
		ModelAndView modelAndView = null;
		try {
			modelAndView = new ModelAndView("home");
			RestaurantDAO restaurantDAO = new RestaurantDAO();
			List<Restaurant> restaurantsList;		
			restaurantsList = restaurantDAO.getRestaurants(true);
			modelAndView.addObject("restaurantsList", restaurantsList);
			modelAndView.addObject("user", new User());
			modelAndView.addObject("restaurant", new Restaurant());
		} catch (UrbanspoonException e) {
			e.printStackTrace();
		}		
		return modelAndView;
	}

	@PostMapping("/login")
	public ModelAndView login(@RequestParam("user_id") String user_id, @RequestParam("password") String password,
			@RequestParam("loginAs") String loginAs, HttpServletRequest request) throws UrbanspoonException {
		HttpSession session = request.getSession();
		ModelAndView modelAndView = null;
		if(loginAs != null && loginAs.equals("user")) {
			UserDAO userDAO = new UserDAO();
			User user = userDAO.getUser(user_id);
			if(user != null && user.getPassword().equals(password)) {
				session.setAttribute("loggedInUser", user);
				session.setAttribute("loggedInUserId", user.getId());
				modelAndView = new ModelAndView("userHome");
				RestaurantDAO restaurantDAO = new RestaurantDAO();
				List<Restaurant> restaurantsList;		
				restaurantsList = restaurantDAO.getRestaurants(true);
				modelAndView.addObject("restaurantsList", restaurantsList);
			}			
		}
		else if(loginAs != null && loginAs.equals("restaurant")) {
			RestaurantDAO restaurantDAO = new RestaurantDAO();
			Restaurant restaurant = restaurantDAO.getRestaurant(user_id, false);
			if(restaurant != null && restaurant.getPassword().equals(password))
			{
				session.setAttribute("loggedInUser", restaurant);
				session.setAttribute("loggedInUserId", restaurant.getId());
				modelAndView = new ModelAndView("home");
				CuisineDAO cuisineDAO = new CuisineDAO();
				List<Cuisine> cusineList = cuisineDAO.getCuisines(false);
				modelAndView.addObject("cuisineList", cusineList);
				BranchDAO branchDAO = new BranchDAO();
				List<Branch> branchList = branchDAO.getBranches(restaurant.getId(),true, true);
				modelAndView.addObject("branchList", branchList);
				RecipeDAO recipeDAO = new RecipeDAO();
				List<Recipe> recipeList = recipeDAO.getRecipes();
			}
		}
		if(modelAndView == null) {
			modelAndView = new ModelAndView("home");
			RestaurantDAO restaurantDAO = new RestaurantDAO();
			List<Restaurant> restaurantsList = restaurantDAO.getRestaurants(true);
			modelAndView.addObject("restaurantsList", restaurantsList);
		}
		return modelAndView;
	}
	
	@RequestMapping(value = "/user_registration", method = RequestMethod.POST)
	public ModelAndView admissionForm(@RequestParam("first_name") String first_name, @RequestParam("last_name") String last_name, @RequestParam("password") String password,
			@RequestParam("email") String email, @RequestParam("gender") String gender,@RequestParam("mobile_number") String mobile_number) {
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		user.setName(first_name +" " +last_name);
		
		return null;		
	}
}
