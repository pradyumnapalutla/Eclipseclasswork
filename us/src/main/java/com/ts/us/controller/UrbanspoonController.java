package com.ts.us.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.sun.javafx.sg.prism.NGShape.Mode;
import com.ts.us.dao.BranchDAO;
import com.ts.us.dao.CuisineDAO;
import com.ts.us.dao.FeedbackDAO;
import com.ts.us.dao.FeedbackTypeDAO;
import com.ts.us.dao.RecipeDAO;
import com.ts.us.dao.RestaurantDAO;
import com.ts.us.dao.UserDAO;
import com.ts.us.dto.Branch;
import com.ts.us.dto.Cuisine;
import com.ts.us.dto.Feedback;
import com.ts.us.dto.FeedbackType;
import com.ts.us.dto.Recipe;
import com.ts.us.dto.Restaurant;
import com.ts.us.dto.User;
import com.ts.us.exception.UrbanspoonException;
import com.ts.us.util.DateUtility;
import com.ts.us.util.FileUpload;

@Controller
public class UrbanspoonController {
	private static final String IMAGESLOCATION = "C:\\Users\\Deepika\\Desktop\\Eclipseclasswork\\Images";
	@Autowired
	private RestaurantDAO restaurantDAO;
	@Autowired
	private BranchDAO branchDAO;
	@Autowired
	private RecipeDAO recipeDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private CuisineDAO cuisineDAO;
	@Autowired
	private FeedbackDAO feedbackDAO;
	
	@RequestMapping("/home")
	public ModelAndView getRestaurant() {
		ModelAndView modelAndView = null;
		try {
			modelAndView = new ModelAndView("home");
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
		if (loginAs != null && loginAs.equals("user")) {
			User user = userDAO.getUser(user_id);
			if (user != null && user.getPassword().equals(password)) {
				session.setAttribute("loggedInUser", user);
				session.setAttribute("loggedInUserId", user.getId());
				modelAndView = new ModelAndView("userHome");
				List<Restaurant> restaurantsList;
				restaurantsList = restaurantDAO.getRestaurants(true);
				modelAndView.addObject("restaurantsList", restaurantsList);
			}
		} else if (loginAs != null && loginAs.equals("restaurant")) {
			Restaurant restaurant = restaurantDAO.getRestaurant(user_id, false);
			if (restaurant != null && restaurant.getPassword().equals(password)) {
				session.setAttribute("loggedInUser", restaurant);
				session.setAttribute("loggedInUserId", restaurant.getId());
				modelAndView = new ModelAndView("restaurantHome");
				List<Cuisine> cusineList = cuisineDAO.getCuisines(false);
				modelAndView.addObject("cuisineList", cusineList);
				List<Branch> branchList = branchDAO.getBranches(restaurant.getId(), true, true);
				modelAndView.addObject("branchList", branchList);
				List<Recipe> recipeList = recipeDAO.getRecipes();
			}
		}
		if (modelAndView == null) {
			modelAndView = new ModelAndView("home");
			List<Restaurant> restaurantsList = restaurantDAO.getRestaurants(true);
			modelAndView.addObject("restaurantsList", restaurantsList);
		}
		return modelAndView;
	}

	@RequestMapping(value = "/user_registration", method = RequestMethod.POST)
	public ModelAndView admissionForm(@RequestParam("first_name") String first_name,
			@RequestParam("last_name") String last_name, @RequestParam("password") String password,
			@RequestParam("email") String email, @RequestParam("gender") String gender,
			@RequestParam("mobile_number") String mobile_number) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		user.setName(first_name + " " + last_name);
		user.setEmail(email);
		user.setPassword(password);
		user.setMobileNumber(Long.parseLong(mobile_number));
		user.setGender(gender);
		user = userDAO.insert(user);
		return modelAndView;
	}

	@RequestMapping(value = "/user_registration1", method = RequestMethod.POST)
	public ModelAndView userRegistration(@ModelAttribute("user") User user) throws UrbanspoonException {
		userDAO.insert(user);
		return new ModelAndView("redirect:/home");
	}
	
	@PostMapping("restaurantRegistration")
	public ModelAndView restaurantRegistration(@ModelAttribute("restaurant") Restaurant restaurant, 
			@RequestParam("file") CommonsMultipartFile commonsMultipartFile) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("home");
		Restaurant restaurant1 = restaurantDAO.insert(restaurant);
		if(restaurant1.getId() > 0) {
			FileUpload.uploadImage(IMAGESLOCATION+"restaurant", commonsMultipartFile, restaurant1.getId()+".jpg");
			restaurantDAO.updateLogoAddress(restaurant1.getId(), restaurant1.getId()+".jpg");
		}
		return modelAndView;
	}
	
	@RequestMapping("/branch_feedback")
	public ModelAndView branchFeedback(@RequestParam("restaurant_id") int restaurantId, @RequestParam("branch_id") int branchId) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("userHome");
		Branch branch = branchDAO.getBranch(branchId, false);
		Restaurant restaurant = restaurantDAO.getRestaurant(restaurantId, false);		
		modelAndView.addObject("branch",branch);
		modelAndView.addObject("restaurant",restaurant);
		modelAndView.addObject("feedbackTypeList",new FeedbackTypeDAO().getFeedbackTypes());
		return modelAndView;		
	}
	
	@RequestMapping(value = "/branch_feedback_form", method = RequestMethod.POST)
	public ModelAndView branchFeedbackForm(@RequestParam("branch_id") int branch_id, @RequestParam("comments") String comments,
			@RequestParam("rating") int rating,@RequestParam("visited_Date") String visitedDate,
			@RequestParam("feedback_type_id") int feedbackTypeId,HttpServletRequest request) throws UrbanspoonException {		
		ModelAndView modelAndView = new ModelAndView("userHome");
		Feedback feedback = new Feedback();
		Branch branch = new Branch();
		branch.setId(branch_id);
		User user = new User();
		user.setId((long)request.getSession().getAttribute("loggedInUserId"));
		feedback.setBranch(branch);
		feedback.setUser(user);
		FeedbackType feedbackType = new FeedbackType();
		feedbackType.setId(feedbackTypeId);
		feedback.setFeedbackType(feedbackType);
		feedback.setComments(comments);
		feedback.setRatings(rating);
		//System.out.println(request.getParameter("visited_Date"));
		feedback.setVisitedDate(DateUtility.convertStringToDate(visitedDate));
		feedback.setFeedbackDate(new Date());
		feedback = feedbackDAO.insertBranchFeedback(feedback);
		List<Restaurant> restaurantsList;
		restaurantsList = restaurantDAO.getRestaurants(true);
		modelAndView.addObject("restaurantsList", restaurantsList);
		return modelAndView;
	}
	
	@RequestMapping("/recipe_feedback")
	public ModelAndView recipeFeedback(@RequestParam("recipe_id") int recipeId, @RequestParam("branch_id") int branchId,
			@RequestParam("restaurant_id") int restaurantId) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("userHome");
		Restaurant restaurant = restaurantDAO.getRestaurant(restaurantId, false);
		Branch branch = branchDAO.getBranch(branchId, false);
		Recipe recipe = recipeDAO.getRecipe(recipeId);
		modelAndView.addObject("branch",branch);
		modelAndView.addObject("restaurant",restaurant);
		modelAndView.addObject("recipe",recipe);
		return modelAndView;	
	}
	
	@PostMapping("/recipe_feedback_form")
	public ModelAndView recipeFeedbackForm(@RequestParam("branch_id") int branchId,@RequestParam("recipe_id") int recipeId, @RequestParam("comments") String comments,
			@RequestParam("rating") int rating,@RequestParam("visited_Date") String visitedDate,
			HttpServletRequest request) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("userHome");
		Feedback feedback = new Feedback();
		Branch branch = new Branch();
		branch.setId(branchId);
		User user = new User();
		user.setId((long)request.getSession().getAttribute("loggedInUserId"));
		feedback.setBranch(branch);
		feedback.setUser(user);
		Recipe recipe = new Recipe();
		recipe.setId(recipeId);
		feedback.setRecipe(recipe);
		feedback.setComments(comments);
		feedback.setRatings(rating);
		feedback.setVisitedDate(DateUtility.convertStringToDate(visitedDate));
		feedback.setFeedbackDate(new Date());
		feedback = feedbackDAO.insertRecipeFeedback(feedback);
		List<Restaurant> restaurantsList;
		restaurantsList = restaurantDAO.getRestaurants(true);
		modelAndView.addObject("restaurantsList", restaurantsList);
		return modelAndView;		
	}
	
	@PostMapping("/AddCuisine")
	public ModelAndView addCuisine(@RequestParam("name") String CuisineName, @RequestParam("country") String country) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("restaurantHome");
		Cuisine cuisine = new Cuisine();
		cuisine.setName(CuisineName);
		cuisine.setCountry(country);
		cuisine = cuisineDAO.insert(cuisine);
		return modelAndView;		
	}
	
	@PostMapping("Add_Recipe")
	public ModelAndView addRecipe(@RequestParam("name") String RecipeName, @RequestParam("description") String description,
			@RequestParam("type") String type,@RequestParam("cuisine_id") String cuisine_id) throws UrbanspoonException {
		ModelAndView modelAndView = new ModelAndView("restaurantHome");
		Recipe recipe = new Recipe();
		recipe.setName(RecipeName);
		recipe.setDescription(description);
		String recipeType = type;
		if (recipeType.equals("Veg")) {
			recipe.setVeg(true);
		}
		int cuisineId = Integer.parseInt(cuisine_id);
		recipe = recipeDAO.insert(cuisineId, recipe);
		return modelAndView;		
	}
	
	
}
