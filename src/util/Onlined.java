package util;

public enum Onlined {

    SUCCESS("Success!"),
    WRONG_PASSWORD("Wrong password!"),
    USER_NOT_EXIST("User dosen't exist!"),
    USER_HAS_EXIST("User has been existed!"),
    INVALID_VALUE("Invalid name or password!");

    private String message;
    private Onlined(String msg) {
        message = msg;
    }

    public static void main(String[] args) {
        Onlined info = Onlined.INVALID_VALUE;
        System.out.println(info);
    }
}
