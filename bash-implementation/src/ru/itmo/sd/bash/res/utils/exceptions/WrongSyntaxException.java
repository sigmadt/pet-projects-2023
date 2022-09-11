package ru.itmo.sd.bash.res.utils.exceptions;


public class WrongSyntaxException extends RuntimeException {
    private final String message;

    public WrongSyntaxException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return
                String
                        .format(
                                "This command can not be processed.\n Here is the message : %s",
                                message
                        );
    }

}

