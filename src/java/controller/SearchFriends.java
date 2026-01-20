import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SearchFriends", urlPatterns = {"/SearchFriends"})
public class SearchFriends extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseJson = new JsonObject();
        Gson gson = new Gson();

        String mobile = request.getParameter("mobile");

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);

            criteria1.add(Restrictions.eq("mobile", mobile));

            User user = (User) criteria1.uniqueResult();

            String serverPath = request.getServletContext().getRealPath("");
            String otherUserAvatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + user.getMobile() + ".png";
            File otherUserAvatarImageFile = new File(otherUserAvatarImagePath);

            responseJson.addProperty("avatar_image_found", otherUserAvatarImageFile.exists());

            if (!otherUserAvatarImageFile.exists()) {
                responseJson.addProperty("avatar_letters", user.getFirstName().charAt(0) + "" + user.getLastName().charAt(0));
            }

            responseJson.addProperty("name", user.getFirstName() + " " + user.getLastName());
            responseJson.addProperty("mobile", user.getMobile());
            responseJson.addProperty("id", user.getId());

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.getWriter().write(gson.toJson(responseJson));
    }

}