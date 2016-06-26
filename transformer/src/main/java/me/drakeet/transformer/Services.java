package me.drakeet.transformer;

/**
 * @author drakeet
 */
public class Services {

    private Services() {
    }


    public static MessageService messageService() {
        return new MessageService();
    }
}
