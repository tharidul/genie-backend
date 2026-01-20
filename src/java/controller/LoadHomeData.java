package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.ChatList;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);
        responseJson.addProperty("message", "Unable to process your request");

        Session session = null;
        Transaction transaction = null; // Declare transaction
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction(); // Start the transaction

            String userID = request.getParameter("id");
            User user = (User) session.get(User.class, Integer.parseInt(userID));

            // Check if user exists
            if (user != null) {
                // Load user's status
                User_Status us = user.getUserStatus();
                if (us != null) {
                    user.setUserStatus(us);
                }

                // Fetch the chat list for the user
                Criteria chatListCriteria = session.createCriteria(ChatList.class);
                chatListCriteria.add(Restrictions.eq("f_user", user)); // Corrected field reference
                List<ChatList> chatListEntries = chatListCriteria.list();

                JsonArray jsonChatArray = new JsonArray();

                for (ChatList chatListEntry : chatListEntries) {
                    User friend = chatListEntry.getT_user(); // Use getter to get the friend user

                    // Fetch the last chat between the current user and this friend
                    Criteria chatCriteria = session.createCriteria(Chat.class);
                    chatCriteria.add(
                            Restrictions.or(
                                    Restrictions.and(
                                            Restrictions.eq("from_user_id", user),
                                            Restrictions.eq("to_user_id", friend)
                                    ),
                                    Restrictions.and(
                                            Restrictions.eq("from_user_id", friend),
                                            Restrictions.eq("to_user_id", user)
                                    )
                            )
                    );

                    chatCriteria.addOrder(Order.desc("date_time")); // Order by date_time
                    chatCriteria.setMaxResults(1); // Limit to the last chat

                    JsonObject chatItem = new JsonObject();
                    chatItem.addProperty("other_user_ID", friend.getId());
                    chatItem.addProperty("otherMobile", friend.getMobile());
                    chatItem.addProperty("other_user_name", friend.getFirstName() + " " + friend.getLastName());
                    chatItem.addProperty("other_user_status", friend.getUserStatus() != null && friend.getUserStatus().getId() == 1);

                    System.out.println(friend.getUserStatus().getId() == 1);

                    String serverPath = request.getServletContext().getRealPath("");
                    String otherUserAvatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + friend.getMobile() + ".png";
                    File otherUserAvatarImageFile = new File(otherUserAvatarImagePath);

                    chatItem.addProperty("avatar_image_found", otherUserAvatarImageFile.exists());

                    if (!otherUserAvatarImageFile.exists()) {
                        chatItem.addProperty("other_user_avatar", friend.getFirstName().charAt(0) + "" + friend.getLastName().charAt(0));
                    }

                    List<Chat> chatList = chatCriteria.list();

//                     Prepare the last message and datetime if available
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                    if (chatList.isEmpty()) {
                        chatItem.addProperty("message", "Let's Start a new Conversation");
                        chatItem.addProperty("dateTime", dateFormat.format(user.getRegisteredDateTime()));
                        chatItem.addProperty("chat_status", 1);
                    } else {
                        Chat lastChat = chatList.get(0);
                        chatItem.addProperty("message", lastChat.getMessgage()); // Fixed method name
                        chatItem.addProperty("dateTime", dateFormat.format(lastChat.getDate_time()));
                        if (lastChat.getFrom_user_id().equals(user)) {

                            if (lastChat.getChat_status_id().getId() == 1) {
                                chatItem.addProperty("chat_status", true);
                            } else {
                                chatItem.addProperty("chat_status", false);
                            }
                        }

                    }

                    jsonChatArray.add(chatItem);
                }

                // Prepare final response
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Success");
                responseJson.add("currentUser", gson.toJsonTree(user));
                responseJson.add("jsonChatArray", gson.toJsonTree(jsonChatArray));
            } else {
                responseJson.addProperty("message", "User not found"); // User not found response
            }

            transaction.commit(); // Commit the transaction
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback on error
            }
            e.printStackTrace(); // Log the exception for debugging
        } finally {
            if (session != null) {
                session.close(); // Ensure session is closed
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
