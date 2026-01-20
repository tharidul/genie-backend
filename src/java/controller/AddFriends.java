package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.BlockStatus;
import entity.ChatList;
import entity.User; // Import User entity
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction; // Import Transaction
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AddFriends", urlPatterns = {"/AddFriends"})
public class AddFriends extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseJson = new JsonObject();
        Gson gson = new Gson();

        String mid = request.getParameter("mid");
        String fid = request.getParameter("fid");

        if (mid.equals(fid)) {
            responseJson.addProperty("error", "You cannot add yourself as a friend.");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseJson));
            return; // Stop further execution
        }

        Session session = null;
        Transaction transaction = null; // Declare Transaction

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction(); // Start a transaction

            User me = (User) session.get(User.class, Integer.parseInt(mid));
            User friend = (User) session.get(User.class, Integer.parseInt(fid));

            if (me == null || friend == null) {
                responseJson.addProperty("error", "User not found");
            } else {
                // Create criteria to find existing ChatList entry
                Criteria criteria1 = session.createCriteria(ChatList.class);
                criteria1.add(Restrictions.eq("f_user", me));
                criteria1.add(Restrictions.eq("t_user", friend));

                // Reuse the result to avoid querying twice
                ChatList chatListEntry = (ChatList) criteria1.uniqueResult();

                // Add the result to the JSON response
                if (chatListEntry == null) {
                    // Create a new ChatList entry if not exists
                    chatListEntry = new ChatList();
                    chatListEntry.setF_user(me);
                    chatListEntry.setT_user(friend);

                    // Load the BlockStatus entity (assuming 1 means unblocked)
                    BlockStatus blockStatus = (BlockStatus) session.load(BlockStatus.class, 1);
                    chatListEntry.setBlockStatus(blockStatus);

                    // Save the new chat list entry
                    session.save(chatListEntry);
                    transaction.commit(); // Commit the transaction
                    responseJson.addProperty("message", "Added.");
                    responseJson.addProperty("success", true);
                } else {
                    responseJson.addProperty("success", false);
                    responseJson.addProperty("message", "Already exists.");
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback the transaction in case of an error
            }
            e.printStackTrace(); // Log the error
            responseJson.addProperty("error", "An error occurred while processing the request.");
        } finally {
            if (session != null) {
                session.close(); // Close the session to avoid memory leaks
            }
        }

        // Send the JSON response back to the client
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
