package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        
        

        JsonObject respJson = new JsonObject();
        respJson.addProperty("success", false);

        JsonObject reqestJson = gson.fromJson(request.getReader(), JsonObject.class);

        String mobile = reqestJson.get("mobile").getAsString();
        String password = reqestJson.get("password").getAsString();

        if (mobile.isEmpty()) {
            respJson.addProperty("message", "Please Enter Mobile Number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            respJson.addProperty("message", "Please Enter Valid Mobile Number");
        } else if (password.isEmpty()) {
            respJson.addProperty("message", "Please Enter Mobile Number");
        } else if (!Validations.isPasswordValid(password)) {
            respJson.addProperty("message", "Please Enter Valid Password");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));
            criteria1.add(Restrictions.eq("password", password));

            if (!criteria1.list().isEmpty()) {

                User user = (User) criteria1.uniqueResult();
                respJson.addProperty("success", true);
                respJson.addProperty("message", "Sign In Success");
                respJson.add("user", gson.toJsonTree(user));

            } else {

                respJson.addProperty("message", "Invalid Crednitials");

                session.close();
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(respJson));
    }
}
