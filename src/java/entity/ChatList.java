package entity;
import entity.BlockStatus;
import entity.User;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "chat_list")
public class ChatList implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "f_user_id", nullable = false)
    private User f_user;

    @ManyToOne
    @JoinColumn(name = "t_user_id", nullable = false)
    private User t_user;

    @ManyToOne
    @JoinColumn(name = "block_status_id")
    private BlockStatus blockStatus;

    // Default constructor
    public ChatList() {
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getF_user() {
        return f_user;
    }

    public void setF_user(User f_user) {
        this.f_user = f_user;
    }

    public User getT_user() {
        return t_user;
    }

    public void setT_user(User t_user) {
        this.t_user = t_user;
    }

    public BlockStatus getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(BlockStatus blockStatus) {
        this.blockStatus = blockStatus;
    }
}
