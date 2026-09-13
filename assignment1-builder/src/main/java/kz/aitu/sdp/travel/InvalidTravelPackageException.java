package kz.aitu.sdp.travel;

import java.util.List;

public class InvalidTravelPackageException extends IllegalStateException {

    private final transient List<String> errors;

    public InvalidTravelPackageException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
