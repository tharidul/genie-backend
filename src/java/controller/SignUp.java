package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject respJson = new JsonObject();
        respJson.addProperty("success", false);

//        JsonObject reqestJson = gson.fromJson(request.getReader(), JsonObject.class);;
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String mobile = request.getParameter("mobile");
        String password = request.getParameter("password");

        Part avatarImage = request.getPart("avatarImage");

        if (mobile.isEmpty()) {
            respJson.addProperty("message", "Please enter your mobile number.");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            respJson.addProperty("message", "The mobile number provided is invalid. Please enter a valid number.");
        } else if (password.isEmpty()) {
            respJson.addProperty("message", "Please enter your password.");
        } else if (!Validations.isPasswordValid(password)) {
            respJson.addProperty("message", "The password provided does not meet the required criteria. Please enter a valid password.");
        } else if (firstName.isEmpty()) {
            respJson.addProperty("message", "Please provide your first name.");
        } else if (lastName.isEmpty()) {
            respJson.addProperty("message", "Please provide a valid last name.");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));

            if (!criteria1.list().isEmpty()) {

                respJson.addProperty("message", "This Mobile Number Alrady Used..!");

            } else {

                User user = new User();
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegisteredDateTime(new Date());

                User_Status us = (User_Status) session.load(User_Status.class, 2);
                user.setUserStatus(us);

                session.save(user);
                session.beginTransaction().commit();

                if (avatarImage.getName() != null) {

                    String serverPath = request.getServletContext().getRealPath("");
                    String newApplicationPath = serverPath.replace("build" + File.separator + "web", "web");

                    String avatarImagePath = newApplicationPath + File.separator + "AvatarImages" + File.separator + mobile + ".png";

                    File file = new File(avatarImagePath);
                    Files.copy(avatarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                }

                respJson.addProperty("success", true);
                respJson.addProperty("message", "registration Complete");

                session.close();
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(respJson));
    }

}
