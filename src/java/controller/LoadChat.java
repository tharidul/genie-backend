package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Tharidu_Lakmal
 */
@WebServlet(name = "LoadChat", urlPatterns = {"/LoadChat"})
public class LoadChat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            String current_user_id = request.getParameter("current_user_id");
            String other_user_id = request.getParameter("other_user_id");

            User current_user = (User) session.get(User.class, Integer.parseInt(current_user_id));
            User other_user = (User) session.get(User.class, Integer.parseInt(other_user_id));

            Criteria criteria1 = session.createCriteria(Chat.class);
            criteria1.add(
                    Restrictions.or(
                            Restrictions.and(
                                    Restrictions.eq("from_user_id", current_user),
                                    Restrictions.eq("to_user_id", other_user)
                            ),
                            Restrictions.and(
                                    Restrictions.eq("from_user_id", other_user),
                                    Restrictions.eq("to_user_id", current_user)
                            )
                    )
            );
            criteria1.addOrder(Order.asc("date_time"));

            List<Chat> chatList = criteria1.list();

            Chat_Status cs = (Chat_Status) session.get(Chat_Status.class, 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");

            JsonArray jsonChatArray = new JsonArray();

            for (Chat chat : chatList) {

                JsonObject responseJsonChatObject = new JsonObject();
                responseJsonChatObject.addProperty("message", chat.getMessgage());
                responseJsonChatObject.addProperty("datetime", dateFormat.format(chat.getDate_time()));

                if (chat.getFrom_user_id().getId() == other_user.getId()) {

                    responseJsonChatObject.addProperty("sender", "other");

                    if (chat.getChat_status_id().getId() == 2) {

                        chat.setChat_status_id(cs);
                        session.save(chat);

                    }
                } else {
                    responseJsonChatObject.addProperty("sender", "me");
                    responseJsonChatObject.addProperty("messageStatus", chat.getChat_status_id().getId());

                }
                jsonChatArray.add(responseJsonChatObject);
            }

            session.beginTransaction().commit();
            session.close();

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(jsonChatArray));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
