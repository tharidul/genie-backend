package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
import entity.ChatList; // Import ChatList entity
import entity.BlockStatus; // Import BlockStatus entity
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction; // Import Transaction
import org.hibernate.Criteria; // Import Criteria
import org.hibernate.criterion.Restrictions; // Import Restrictions

@WebServlet(name = "SendMessage", urlPatterns = {"/SendMessage"})
public class SendMessage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("success", false);

        Transaction transaction = null; // Declare Transaction

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction(); // Start a transaction

            String current_user_id = request.getParameter("current_user_id");
            String other_user_id = request.getParameter("other_user_id");
            String message = request.getParameter("message");

            User current_user = (User) session.get(User.class, Integer.parseInt(current_user_id));
            User other_user = (User) session.get(User.class, Integer.parseInt(other_user_id));

            // Check if User B (other_user) already has User A (current_user) as a friend
            Criteria criteria = session.createCriteria(ChatList.class);
            criteria.add(Restrictions.eq("f_user", other_user)); // Check if other_user (User B) has current_user (User A) as a friend
            criteria.add(Restrictions.eq("t_user", current_user));
            ChatList chatListEntry = (ChatList) criteria.uniqueResult();

            // If no chat list entry exists, add current_user (User A) as a friend to other_user (User B)
            if (chatListEntry == null) {
                ChatList newChatListEntry = new ChatList();
                newChatListEntry.setF_user(other_user); // User B is the friend of User A
                newChatListEntry.setT_user(current_user); // User A is added to User B's friend list

                // Load the BlockStatus entity (assuming 1 means unblocked)
                BlockStatus blockStatus = (BlockStatus) session.load(BlockStatus.class, 1);
                newChatListEntry.setBlockStatus(blockStatus);

                // Save the new chat list entry for User B's friend list
                session.save(newChatListEntry);
            }

            // Create the chat entry
            Chat_Status cs = (Chat_Status) session.get(Chat_Status.class, 2);
            Chat chat = new Chat();
            chat.setChat_status_id(cs);
            chat.setDate_time(new Date());
            chat.setFrom_user_id(current_user);
            chat.setMessgage(message);
            chat.setTo_user_id(other_user);

            session.save(chat);
            transaction.commit(); // Commit the transaction

            responseJsonObject.addProperty("success", true);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback the transaction in case of an error
            }
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJsonObject));
    }
}
