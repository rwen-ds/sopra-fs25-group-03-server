package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class ContactDTO {
    private Long id;
    private String username;
//    private String lastMessage;

    public ContactDTO(Long id, String username) {
        this.id = id;
        this.username = username;
//        this.lastMessage = lastMessage;
    }

    public ContactDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

//    public String getLastMessage() {
//        return lastMessage;
//    }
//
//    public void setLastMessage(String lastMessage) {
//        this.lastMessage = lastMessage;
//    }
}
